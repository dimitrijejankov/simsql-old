

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

#define DICTIONARY 60000

//#define DEBUG

/**
 * A Multi-Multinomial VG function, used to get a purely document-based
 * Gibbs sampler for the LDA.
 *
 * This is the vector-based form.
 */

/** 
 * Input record, used by the takeParams() method.
 * Values could be NULL, meaning they were not present in the tuple.
 */
struct RecordIn
{
  Vector *document;
  long *topicNum;
};

/**
 * Output record, used by the outputVals().
 * If any of the values is NULL, then the user must allocate space for
 * it (the engine will de-allocate). 
 */
struct RecordOut
{
  long *out_word_id;
  long *out_topic_id;
  long *out_count;
};

int runningError;
void handler(const char *reason,
             const char *file,
             int line,
             int gsl_errno)
{

  runningError = 1;
  fprintf(stderr, "Some error happened in Uniform_WordTopic_Collapsed_WordCount...\n");
}

class Uniform_WordTopic_Collapsed_WordCount : public VGFunction
{
private:
  // RNG
  gsl_rng *rng;

  // are we good to sample?
  bool active;

  // have we sampled already?
  bool sampled;

  // document
  long *doc;
  unsigned int docLen;

  // topic number
  long topicNumber;

  // word-counts
  long *wordCounts;

  // output-counts
  unsigned int **outputCounts;

  // iteration variables
  unsigned int currentWord;
  long currentTopic;

  // output variables
  long outWord;
  long outTopic;
  long outCount;

public:
  /** 
   * Constructor. Use this to declare your RNG and other important
   * structures.
   */
  Uniform_WordTopic_Collapsed_WordCount()
  {
    // create the RNG.
    rng = gsl_rng_alloc(gsl_rng_mt19937);

    doc = (long *)malloc(DICTIONARY * sizeof(long));
    wordCounts = (long *)malloc(DICTIONARY * sizeof(long));

    runningError = -1;
  }

  /** Destructor. Deallocate everything from the constructor. */
  ~Uniform_WordTopic_Collapsed_WordCount()
  {
    // destroy the RNG
    gsl_rng_free(rng);

    free(wordCounts);
    free(doc);
  }

  void initializeSeed(long seed)
  {
    // set the seed
    gsl_rng_set(rng, seed);
  }

  /** 
   * Finalizes the current trial and prepares the structures for
   * another fresh call to outputVals(). 
   */
  void finalizeTrial()
  {
    // set active again.
    active = (docLen > 0);
    sampled = false;
  }

  /**
   * Clears the set of parameters for the first call to takeParams.
   * If possible, uses the default parameter set.
   */
  void clearParams()
  {
    // set inactive
    active = false;
    sampled = false;
    docLen = 0;
  }

  void takeParams(RecordIn &input)
  {
    // document.
    if (input.document != NULL)
    {
      // zero the counts
      for (int i = 0; i < DICTIONARY; i++)
      {
        wordCounts[i] = 0;
      }
      docLen = 0;

      // make a pass through the document, counting the words and elements.
      for (int i = 0; i < input.document->length; i = i + 2)
      {
        long pos = (long)(input.document->value[i]);
        doc[docLen] = pos;
        docLen++;

        if (pos >= DICTIONARY)
          wordCounts = (long *)realloc(wordCounts, sizeof(long) * pos * 2);

        wordCounts[pos] = (long)(input.document->value[i + 1]);
      }
    }

    // uniform topic probabilities.
    if (input.topicNum != NULL)
    {
      topicNumber = *input.topicNum;
    }

    active = (docLen > 0);
  }

  /** 
   * Produces the sample values. Returns 1 if there are more
   * records to be produced for the current sample, 0 otherwise. 
   */
  int outputVals(RecordOut &output)
  {
    // are we active?
    if (!active)
    {
      return 0;
    }

    // are we NOT sampled already?
    if (!sampled)
    {
      // now, we are ready to sample everything.
      // gsl_vector_view topics1 = gsl_vector_view_array(workProbs, topicNumber);

      outputCounts = (unsigned int **)malloc(docLen * sizeof(unsigned int *));
      for (int i = 0; i < docLen; i++)
      {
        outputCounts[i] = (unsigned int *)malloc(topicNumber * sizeof(unsigned int));
      }

      for (int i = 0; i < docLen; i++)
      {

        // clear the count first
        for (int j = 0; j < topicNumber; j++)
          outputCounts[i][j] = 0;

        // now, sample from the uniform distribution.
        for (int j = 0; j < wordCounts[doc[i]]; j++)
          outputCounts[i][gsl_rng_uniform_int(rng, topicNumber)]++;
      }

      // now, set everything up for iteration
      currentWord = 0;
      currentTopic = 0;

      sampled = true;
    }

    while (true)
    {
      // done with the topics in this word?
      if (currentTopic >= topicNumber)
      {
        currentTopic = 0;
        currentWord++;
      }

      // done with the words in this document?
      if (currentWord >= docLen)
      {
        active = false;
        for (int i = 0; i < docLen; i++)
        {
          free(outputCounts[i]);
        }
        free(outputCounts);
        return 0;
      }

      // have a non-zero topic count?
      if (outputCounts[currentWord][currentTopic] > 0)
      {
        // then we have an output tuple!
        outWord = doc[currentWord];
        outTopic = currentTopic;
        outCount = outputCounts[currentWord][currentTopic];

        output.out_word_id = &outWord;
        output.out_topic_id = &outTopic;
        output.out_count = &outCount;

        // move to the next topic.
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
  VGSchema inputSchema()
  {
    return (VGSchema){
        2,
        {"vector[a]", "integer"},
        {"document", "topic_number"}};
  }

  VGSchema outputSchema()
  {
    return (VGSchema){3, {"integer", "integer", "integer random"}, {"out_word_id", "out_topic_id", "out_count"}};
  }

  const char *getName()
  {
    return "Uniform_WordTopic_Collapsed_WordCount";
  }
};

/** External creation/destruction methods -- DO NOT MODIFY */
VGFunction *create()
{
  return (new Uniform_WordTopic_Collapsed_WordCount());
}

void destroy(VGFunction *vgFunction)
{
  delete (Uniform_WordTopic_Collapsed_WordCount *)vgFunction;
}
