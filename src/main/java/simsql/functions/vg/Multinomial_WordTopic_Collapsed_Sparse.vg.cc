

/*****************************************************************************
 *                                                                           *
 *  Copyright 2014 Rice University                                           *
 *                                                                           *
 *  Licensed under the Apache License, Version 2.0 (the "License");          *
 *  you may not use this file except in compliance with the License.         *
 *  You may obtain a copy of the License at                                  *
 *                                                                           *
 *      http://www.apache.org/licenses/LICENSE-2.0                           *
 *                                                                           *
 *  Unless required by applicable law or agreed to in writing, software      *
 *  distributed under the License is distributed on an "AS IS" BASIS,        *
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. *
 *  See the License for the specific language governing permissions and      *
 *  limitations under the License.                                           *
 *                                                                           *
 *****************************************************************************/


#include "VGFunction.h"
#include <gsl/gsl_rng.h>
#include <gsl/gsl_randist.h>
#include <math.h>
#include <gsl/gsl_errno.h>
#include <string>
#include <sstream>
#include <iostream>
#include <fstream>

using namespace std;

#define INITIAL_SIZE 50
#define WORD_INITIAL_SIZE 10000
//#define DEBUG

/**
 * A Multi-Multinomial VG function, used to get a purely document-based
 * Gibbs sampler for the Collapsed LDA.
 *    
 * This is the sparse form.
 **/

/** 
 * Input record, used by the takeParams() method.
 * Values could be NULL, meaning they were not present in the tuple.
 */
struct RecordIn {
  Vector *document;

  long *topic_id;
  long *topic_count;

  long *wtopic_id;
  long *word_id;
  long *topic_word_count;

  long *last_word_id;
  long *last_topic_id;
  long *last_count;

  double *alpha;
  double *beta;
};

/**
 * Output record, used by the outputVals().
 * If any of the values is NULL, then the user must allocate space for
 * it (the engine will de-allocate). 
 */
struct RecordOut {
  long *out_word_id;
  long *out_topic_id;
  long *out_count;
};


int runningError;
void handler (const char * reason,
	      const char * file,
	      int line,
	      int gsl_errno) {
  
  runningError = 1;
  fprintf(stderr, "Some error happened in Multinomial_WordTopic_Collapsed_Sparse...\n");
}


class Multinomial_WordTopic_Collapsed_Sparse: public VGFunction {
private:

  // RNG
  gsl_rng *rng;

  // are we good to sample?
  bool active;
  bool normalized;

  // document
  long *doc;
  unsigned int docLen;

  // topic counts
  long *topicCounts;

  // number of topics in the current document
  long numTopics;

  // size of the topicCounts data structure and each element in
  // wordTopicCounts.
  long topicCapacity;

  // word-topic counts
  long **wordTopicCounts;

  // word-topic probabilities
  double **wordTopicProbs;

  // word-topic counts from the last iteration
  long **lastWordTopicCounts;

  // word-topic counts aggrating on words
  long *totalWordTopicCounts;

  // word counts.
  long *wordCount;

  // capacity of the topicWordProb and wordCount arrays.
  long wordCapacity;

  // number of distinct words in the current document.
  long numWords;

  // map from index to wordID, size=numWords,wordCapacity
  long *indexToWord;

  // map from wordID to index, size=numIndices,indexCapacity
  long *wordToIndex;

  // number of wordIDs mapped to indices.
  long numIndices;

  // capacity of hte wordToIndex stucture.
  long indexCapacity;

  // iteration variables for outputVals
  long currentWord;
  long currentTopic;

  // the output structure for the multinomial RNG, size=numWords * numTopics
  unsigned int **outputCounts;

  // temporary output values for the records.
  long tempWordID;
  long tempTopicID;
  long tempOutCount;

  // prior
  double alpha;
  double beta;

  // -----------

  void increaseTopics(long topicIn) {

    // update the number of topics based on the ID number.
    if (topicIn >= numTopics) {
      
#ifdef DEBUG
      printf("Num. topics increase from %ld to %ld.\n", numTopics, topicIn + 1);
      fflush(stdout);
#endif

      numTopics = topicIn + 1;
    }

    // update the capacity of the topic vectors.
    if (numTopics >= topicCapacity) {

      long oldCapacity = topicCapacity;

      topicCapacity = numTopics + (numTopics >> 1) + 1;

#ifdef DEBUG
      printf("Resizing topic array size from %ld to %ld...", oldCapacity, topicCapacity);
      fflush(stdout);
#endif

      // enlarge the array for the new capacity.
      topicCounts = (long *)realloc(topicCounts, topicCapacity * sizeof(long));

#ifdef DEBUG
      printf("done!\n");
      fflush(stdout);
#endif

      // set the empty spaces to zero.
      for (long i=oldCapacity;i<topicCapacity;i++) {
	topicCounts[i] = 0;
      }

      // enlarge the arrays in the word-topic structures.
#ifdef DEBUG
      printf("Enlarging the word-topic arrays...");
      fflush(stdout);
#endif

      for (long i=0;i<wordCapacity;i++) {
	wordTopicCounts[i] = (long *)realloc(wordTopicCounts[i], sizeof(long) * topicCapacity);
	lastWordTopicCounts[i] = (long *)realloc(lastWordTopicCounts[i], sizeof(long) * topicCapacity);
	outputCounts[i] = (unsigned int *)realloc(outputCounts[i], sizeof(unsigned int) * topicCapacity);

	for (long j=oldCapacity;j<topicCapacity;j++) {
	  wordTopicCounts[i][j] = 0;
	  lastWordTopicCounts[i][j] = 0;
	}
      }
      
#ifdef DEBUG
      printf("done!\n");
      fflush(stdout);
#endif

    }
  }

  void increaseWords(long wordIn) {
    
    // check the number of indices
    if (wordIn >= numIndices) {

#ifdef DEBUG
      printf("Increasing indices from %ld to %ld.\n", numIndices, wordIn + 1);
      fflush(stdout);
#endif

      numIndices = wordIn + 1;
    }

    // update the capacity of the indices
    if (numIndices >= indexCapacity) {

      long oldCapacity = indexCapacity;

      indexCapacity = numIndices + (numIndices >> 1) + 1;

#ifdef DEBUG
      printf("Increasing index capacity from %ld to %ld... ", oldCapacity, indexCapacity);
      fflush(stdout);
#endif

      // enlarge that array.
      wordToIndex = (long *)realloc(wordToIndex, indexCapacity * sizeof(long));

#ifdef DEBUG
      printf("done!\n");
      fflush(stdout);
#endif

      // -1 means 'not assigned'.
      for (long i=oldCapacity;i<indexCapacity;i++) {
	wordToIndex[i] = -1;
      }
    }

    // assign an index, if necessary.
    if (wordToIndex[wordIn] < 0) {

#ifdef DEBUG
      printf("Assigned index %ld to word %ld.\n", numWords, wordIn);
      fflush(stdout);
#endif
      
      // we assign the index 'numWords' to this word ID.
      wordToIndex[wordIn] = numWords;
      indexToWord[numWords] = wordIn;
      
      // increase for the next word.
      numWords++;
      
      // increase the wordspace, if necessary.
      if (numWords >= wordCapacity) {
	
	long oldCapacity = wordCapacity;
	wordCapacity = numWords + numWords + (numWords >> 1) + 1;
		
#ifdef DEBUG
	printf("Increasing word capacity from %ld to %ld...", oldCapacity, wordCapacity);
	fflush(stdout);
#endif

	// enlarge all the arrays.
	wordCount = (long *)realloc(wordCount, sizeof(long) * wordCapacity);
	indexToWord = (long *)realloc(indexToWord, sizeof(long) * wordCapacity);
	wordTopicCounts = (long **)realloc(wordTopicCounts, sizeof(long *) * wordCapacity);
	lastWordTopicCounts = (long **)realloc(lastWordTopicCounts, sizeof(long *) * wordCapacity);
	outputCounts = (unsigned int **)realloc(outputCounts, sizeof(unsigned int *) * wordCapacity);

#ifdef DEBUG
	printf("done!\n");
	fflush(stdout);      
#endif

#ifdef DEBUG
	printf("Enlarging all the word-topic arrays...");
	fflush(stdout);
#endif

	for (long i=oldCapacity;i<wordCapacity;i++) {
	  wordCount[i] = 0;
	  indexToWord[i] = -1;
	  wordTopicCounts[i] = (long *)malloc(sizeof(long) * topicCapacity);
	  lastWordTopicCounts[i] = (long *)malloc(sizeof(long) * topicCapacity);
	  outputCounts[i] = (unsigned int *)malloc(sizeof(unsigned int) * topicCapacity);

	  for (long j=0;j<topicCapacity;j++) {
	    wordTopicCounts[i][j] = 0;
	    lastWordTopicCounts[i][j] = 0;
	  }
	}

#ifdef DEBUG
	printf("done!\n");
	fflush(stdout);
#endif

      }
    }
  }

public:

  /** 
   * Constructor. Use this to declare your RNG and other important
   * structures.
   */
  Multinomial_WordTopic_Collapsed_Sparse() {

    // create the RNG.
    rng = gsl_rng_alloc(gsl_rng_mt19937);

    // set the initial size of each container
    topicCapacity = INITIAL_SIZE;
    wordCapacity = WORD_INITIAL_SIZE;
    indexCapacity = INITIAL_SIZE;

    // create the arrays.
    topicCounts = (long *)malloc(topicCapacity * sizeof(long));
    wordCount = (long *)malloc(wordCapacity * sizeof(long));
    wordTopicCounts = (long **)malloc(wordCapacity * sizeof(long *));
  //  wordTopicProbs = (double **)malloc(wordCapacity * sizeof(double *));
    lastWordTopicCounts = (long **)malloc(wordCapacity * sizeof(long *));
    wordToIndex = (long *)malloc(indexCapacity * sizeof(long));
    indexToWord = (long *)malloc(wordCapacity * sizeof(long));
    outputCounts = (unsigned int **)malloc(wordCapacity * sizeof(unsigned int *));
    
    doc = (long *)malloc(wordCapacity * sizeof(long));

    for (long i=0;i<wordCapacity;i++) {
      wordTopicCounts[i] = (long *)malloc(topicCapacity * sizeof(long));
//      wordTopicProbs[i] = (double *)malloc(topicCapacity * sizeof(double));
      lastWordTopicCounts[i] = (long *)malloc(topicCapacity * sizeof(long));
      outputCounts[i] = (unsigned int *)malloc(topicCapacity * sizeof(unsigned int));
      indexToWord[i] = -1;
    }

    for (long i=0;i<indexCapacity;i++) {
      wordToIndex[i] = -1;
    }

    runningError = -1;
    currentWord = 0;
    currentTopic = 0;
  }

  /** Destructor. Deallocate everything from the constructor. */
  ~Multinomial_WordTopic_Collapsed_Sparse() {

    // destroy the RNG
    gsl_rng_free(rng);

    // destroy the arrays
    free(topicCounts);
    free(wordCount);
    free(indexToWord);
    free(wordToIndex);

    free(doc);

    for (long i=0;i<wordCapacity;i++) {
      free(wordTopicCounts[i]);
//      free(wordTopicProbs[i]);
      free(lastWordTopicCounts[i]);
      free(outputCounts[i]);
      wordTopicCounts[i] = NULL;
      lastWordTopicCounts[i] = NULL;
      outputCounts[i] = NULL;
    }

    free(wordTopicCounts);    
//    free(wordTopicProbs);    
    free(lastWordTopicCounts);    
    free(outputCounts);
  }

  void initializeSeed(long seed) {

    // set the seed
    gsl_rng_set(rng, seed);
  }

  /** 
   * Finalizes the current trial and prepares the structures for
   * another fresh call to outputVals(). 
   */
  void finalizeTrial() {

    // set active again.
    active = (numTopics > 0) && (numWords > 0);
    currentWord = 0;
    currentTopic = 0;
  }

  /**
   * Clears the set of parameters for the first call to takeParams.
   * If possible, uses the default parameter set.
   */
  void clearParams() {

    // set inactive
    active = false;
    normalized = false;

    // zero everything out.
    for (long i=0;i<topicCapacity;i++) {
      topicCounts[i] = 0;
    }

    for (long i=0;i<wordCapacity;i++) {
      for (long j=0;j<topicCapacity;j++) {
	wordTopicCounts[i][j] = 0;
//	wordTopicProbs[i][j] = 0.0;
	lastWordTopicCounts[i][j] = 0;
      }

      wordCount[i] = 0;
      indexToWord[i] = -1;
    }

    for (long i=0;i<indexCapacity;i++) {
      wordToIndex[i] = -1;
    }

    numTopics = 0;
    numWords = 0;
    numIndices = 0;
  }

  void takeParams(RecordIn &input) {

    // document.
    if (input.document != NULL) {

      // TODO A better way to read in the document?

      /*
      // zero the counts
      for (int i=0;i<wordCapacity;i++) {
        wordCount[i] = 0;
      }

      docLen = 0;

      // make a pass through the document, counting the words and elements.
      for (int i=0;i<input.document->length;i++) {
        //        unsigned int pos = (unsigned int)gsl_vector_get(vdoc, i);
        unsigned int pos = (unsigned int)(input.document->value[i]);
        if (wordCount[pos] == 0) {
          doc[docLen] = pos;
          docLen++;
        }

        wordCount[pos]++;
      }
	*/
      for (int i=0;i<input.document->length;i++) {
	long pos = (long)(input.document->value[i]);
      	increaseWords(pos);
      	wordCount[wordToIndex[pos]]++;
      }

    }

    // topic counts.
    if (input.topic_id != NULL && input.topic_count != NULL) {
      
      increaseTopics(*input.topic_id);
      topicCounts[*input.topic_id] = *input.topic_count;
    }

    // topic-word counts.
    if (input.wtopic_id != NULL && input.word_id != NULL && input.topic_word_count != NULL) {
      
      increaseTopics(*input.wtopic_id);
      increaseWords(*input.word_id);
      wordTopicCounts[wordToIndex[*input.word_id]][*input.wtopic_id] = *input.topic_word_count;
    }

    // word-topic counts from the last iteration
    if (input.last_word_id != NULL && input.last_topic_id != NULL && input.last_count != NULL) {
      
      increaseTopics(*input.last_topic_id);
      increaseWords(*input.last_word_id);
      lastWordTopicCounts[wordToIndex[*input.last_word_id]][*input.last_topic_id] = *input.last_count;
    }

    if (input.alpha != NULL) {
        alpha = *input.alpha;
    }
    if (input.beta != NULL) {
        beta = *input.beta;
    }

    active = (numTopics > 0) && (numWords > 0);
  }

  /** 
   * Produces the sample values. Returns 1 if there are more
   * records to be produced for the current sample, 0 otherwise. 
   */
  int outputVals(RecordOut &output) {

    // we active?
    if (!active) {
      return 0;
    }

    // is this the first sample?
    if (currentWord == 0 && currentTopic == 0) {

      // first, give enough space for wordTopicProbs
      wordTopicProbs = (double **)malloc(numWords * sizeof(double *)); 
      for (long i=0;i<numWords;i++) {
	      wordTopicProbs[i] = (double *)malloc(numTopics * sizeof(double));
      }

      // second, we calculate some common statistics for sampling

      // n[m, (.)] --> topicCounts
      
      // n[(.), v]
      totalWordTopicCounts = (long *)calloc(numWords, sizeof(long));
      long sumWordTopicCounts = 0;
      for (long i=0; i<numWords; i++) {
      	for (long j=0; j<numTopics; j++) {
		totalWordTopicCounts[i] += wordTopicCounts[i][j]; 
		sumWordTopicCounts += wordTopicCounts[i][j]; 
	}
      
      }

      // temporary output
      unsigned int* tmpOutput = (unsigned int *)malloc(numTopics * sizeof(unsigned int));

      // we'll sample everything at once.
      for (long i=0;i<numWords;i++) {

	// update the word probabilities according to the formula for collapsed lda
	// we need to sample the topic for each word one by one from a categorical distribution
	for (long j=0;j<numTopics;j++) {
		for (long m=0; m<lastWordTopicCounts[i][j]; m++) {
			for (long k=0;k<numTopics;k++) {
				if (k == j) {
					wordTopicProbs[i][k] = (topicCounts[k] - 1 + alpha) *
								(totalWordTopicCounts[i] - 1 + beta) / 
								(sumWordTopicCounts - 1 + numWords * beta);
				}
				else {
					wordTopicProbs[i][k] = (topicCounts[k] + alpha) *
								(totalWordTopicCounts[i] + beta) / 
								(sumWordTopicCounts - 1 + numWords * beta);
			
				}

			}

			// call the multinomial!
			runningError = -1;
			gsl_error_handler_t *oldhandler = gsl_set_error_handler(&handler);
			gsl_ran_multinomial(rng, numTopics, 1, wordTopicProbs[i], tmpOutput);
			gsl_set_error_handler(oldhandler);

			// if we've got an error, bail out.
			if (runningError != -1) {
			  active = false;
			  normalized = false;
			  free(totalWordTopicCounts);
			  for (long n=0;n<numWords;n++) {
			      free(wordTopicProbs[n]);
			  }
			  free(wordTopicProbs); 
			  free(tmpOutput);
			  return 0;
			}

		      	for (int ii=0;ii<numTopics;ii++) {
				if (tmpOutput[ii] > 0) {
					if (ii != j) {
						lastWordTopicCounts[i][ii]++;
						lastWordTopicCounts[i][j]--;
					}
			  		break;
				}
		      	}

		} // sample for each word
	}
	
      }
      free(totalWordTopicCounts);
      for (long i=0;i<numWords;i++) {
      	free(wordTopicProbs[i]);
      }
      free(wordTopicProbs); 
      free(tmpOutput);
    }

    // now that everything is sampled, we will output values.

    // make sure that the topics have positive counts and that we
    // haven't passed all the topics in a word/all the words in the
    // document.
    while (true) {

      // done with the topics in this word?
      if (currentTopic >= numTopics) {
	currentTopic = 0;
	currentWord++;
      }

      // done with the words in this document?
      if (currentWord >= numWords) {
	active = false;
	return 0;
      }

      // have a non-zero topic count?
      if (lastWordTopicCounts[currentWord][currentTopic] > 0) {

	// then we have an output tuple!
	tempWordID = indexToWord[currentWord];
	tempTopicID = currentTopic;
	tempOutCount = lastWordTopicCounts[currentWord][currentTopic];
	
	output.out_word_id = &tempWordID;
	output.out_topic_id = &tempTopicID;
	output.out_count = &tempOutCount;		

	// ove to the next topic.
	currentTopic++;

	// get back with the output tuple.
	return 1;
      }
      
      // if we got here, then we move to another topic and continue
      // looping.
      currentTopic++;
    }
    
  }

  /** Schema information methods -- DO NOT MODIFY */
  VGSchema inputSchema() {

    return (VGSchema){
      11, 
      {"vector[a]", "integer", "integer", "integer", "integer", "integer", "integer", "integer", "integer random", "double", "double"}, 
      {"document", "in_topic_id", "in_topic_count", "in_wtopic_id", "in_word_id", "in_word_topic_count", "last_word_id", "last_topic_id", "last_count", "alpha", "beta"}
    };
  }

  VGSchema outputSchema() {

    return (VGSchema){3, {"integer", "integer", "integer random"}, {"out_word_id", "out_topic_id", "out_count"}};
  }

  const char *getName() {
    return "Multinomial_WordTopic_Collapsed_Sparse";
  }
};

/** External creation/destruction methods -- DO NOT MODIFY */
VGFunction *create() {
  return(new Multinomial_WordTopic_Collapsed_Sparse());
}

void destroy(VGFunction *vgFunction) {
  delete (Multinomial_WordTopic_Collapsed_Sparse *)vgFunction;
}

