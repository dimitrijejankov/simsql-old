

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
#include <math.h>
#include <stdlib.h>
#include <time.h>
#include <string>
#include <sstream>
#include <iostream>
#include <fstream>
#include <map>
#include <vector>

using namespace std;

#define TOPIC_SIZE 10000
#define WORD_SIZE 60000
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
struct RecordIn
{
  //  Vector *document;

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
struct RecordOut
{
  long *out_word_id;
  long *out_topic_id;
  long *out_count;
};

class Multinomial_WordTopic_Collapsed_Sparse_WordCount : public VGFunction
{
private:
  // are we good to sample?
  bool active;
  bool normalized;
  bool validDoc;

  // topic counts
  long *topicCounts;

  // word-topic probabilities
  double *wordTopicProbs;

  // word-topic counts from the last iteration
  vector<map<int,int>> lastWordTopicCounts;

  // output-counts
  vector<map<int,int>> outputCounts;

  // word-topic counts aggregating on words
  long *totalWordTopicCounts;

  // iteration variables for outputVals
  long currentWord;
  long currentTopic;

  // temporary output values for the records.
  long tempWordID;
  long tempTopicID;
  long tempOutCount;

  // prior
  double alpha;
  double beta;

public:
  /** 
   * Constructor. Use this to declare your RNG and other important
   * structures.
   */
  Multinomial_WordTopic_Collapsed_Sparse_WordCount()
  {
    // create the arrays.
    topicCounts = (long *)malloc(TOPIC_SIZE * sizeof(long));
    totalWordTopicCounts = (long *)malloc(WORD_SIZE * sizeof(long));

    for (long i = 0; i < WORD_SIZE; i++)
    {
      lastWordTopicCounts.push_back(map<int,int>());
      outputCounts.push_back(map<int,int>());
    }

    currentWord = 0;
    currentTopic = 0;
  }

  /** Destructor. Deallocate everything from the constructor. */
  ~Multinomial_WordTopic_Collapsed_Sparse_WordCount()
  {
    // destroy the arrays
    free(topicCounts);
    free(totalWordTopicCounts);

    for (long i = 0; i < WORD_SIZE; i++)
    {
      lastWordTopicCounts[i].clear();
      outputCounts[i].clear();
    }
    lastWordTopicCounts.clear();
    outputCounts.clear();
  }

  void initializeSeed(long seed)
  {
    srand (time(NULL));
  }

  /** 
   * Finalizes the current trial and prepares the structures for
   * another fresh call to outputVals(). 
   */
  void finalizeTrial()
  {
    // set active again.
    active = (alpha >= 0) && (beta >= 0);
    currentWord = 0;
    currentTopic = 0;
  }

  /**
   * Clears the set of parameters for the first call to takeParams.
   * If possible, uses the default parameter set.
   */
  void clearParams()
  {
    // set inactive
    active = false;
    normalized = false;
    validDoc = false;

    // zero everything out.
    for (long i = 0; i < TOPIC_SIZE; i++)
    {
      topicCounts[i] = 0;
    }

    for (long i = 0; i < WORD_SIZE; i++)
    {
      totalWordTopicCounts[i] = 0;
      lastWordTopicCounts[i].clear();
      outputCounts[i].clear();
    }
  }

  void takeParams(RecordIn &input)
  {
    // topic counts.
    if (input.topic_id != NULL && input.topic_count != NULL)
    {
      topicCounts[*input.topic_id] = *input.topic_count;
    }

    // topic-word counts.
    if (input.wtopic_id != NULL && input.word_id != NULL && input.topic_word_count != NULL)
    {
      totalWordTopicCounts[*input.word_id] += *input.topic_word_count;
    }

    // word-topic counts from the last iteration
    if (input.last_word_id != NULL && input.last_topic_id != NULL && input.last_count != NULL)
    {
      lastWordTopicCounts[*input.last_word_id].insert(std::make_pair(*input.last_topic_id, *input.last_count));
      validDoc = true;
    }

    if (input.alpha != NULL)
    {
      alpha = *input.alpha;
    }
    if (input.beta != NULL)
    {
      beta = *input.beta;
    }

    printf("We have read in the parameters successfully!\n");
    fflush(stdout);

    active = (alpha >= 0) && (beta >= 0) && validDoc;
  }

  /** 
   * Produces the sample values. Returns 1 if there are more
   * records to be produced for the current sample, 0 otherwise. 
   */
  int outputVals(RecordOut &output)
  {
    // we active?
    if (!active)
    {
      return 0;
    }

    // is this the first sample?
    if (currentWord == 0 && currentTopic == 0)
    {
      // first, give enough space for wordTopicProbs
      wordTopicProbs = (double *)malloc(TOPIC_SIZE * sizeof(double));

      // second, we calculate some common statistics for sampling
      // n[m, (.)] --> topicCounts
      // n[(.), v]
      long sumWordTopicCounts = 0;
      for (long i = 0; i < WORD_SIZE; i++)
      {
        sumWordTopicCounts += totalWordTopicCounts[i];
      }

      // we'll sample everything at once.
      for (long i = 0; i < WORD_SIZE; i++)
      {
        // update the word probabilities according to the formula for collapsed lda
        // we need to sample the topic for each word one by one from a categorical distribution
        for (auto it = lastWordTopicCounts[i].begin(); it != lastWordTopicCounts[i].end(); ++it)
        {
          // sample for each word and update topicCounts
          for (long m = 0; m < it->second; m++)
          {
            double sumWordTopicProbs = 0.0;
            for (long k = 0; k < TOPIC_SIZE; k++)
            {
              if (k == it->first)
              {
                wordTopicProbs[k] = (topicCounts[k] - 1 + alpha) *
                                    (totalWordTopicCounts[i] - 1 + beta) /
                                    (sumWordTopicCounts - 1 + WORD_SIZE * beta);
              }
              else
              {
                wordTopicProbs[k] = (topicCounts[k] + alpha) *
                                    (totalWordTopicCounts[i] + beta) /
                                    (sumWordTopicCounts - 1 + WORD_SIZE * beta);
              }
              sumWordTopicProbs += wordTopicProbs[k];
              if (k > 0) wordTopicProbs[k] += wordTopicProbs[k - 1];
            }
            double u = ((double)rand() / RAND_MAX) * sumWordTopicProbs;
            for (int ii = 0; ii < TOPIC_SIZE; ii++)
            {
              if (u <= wordTopicProbs[ii])
              {
                outputCounts[i][ii]++;
                if (ii != it->first)
                {
                  topicCounts[ii]++;
                  topicCounts[it->first]--;
                }
                break;
              }
            }
          }
        }
      }
      free(wordTopicProbs);
    }

    // now that everything is sampled, we will output values.

    // make sure that the topics have positive counts and that we
    // haven't passed all the topics in a word/all the words in the
    // document.
    while (true)
    {

      // done with the topics in this word?
      if (currentTopic >= TOPIC_SIZE)
      {
        currentTopic = 0;
        currentWord++;
      }

      // done with the words in this document?
      if (currentWord >= WORD_SIZE)
      {
        active = false;
        return 0;
      }

      // have a non-zero topic count?
      if (outputCounts[currentWord].count(currentTopic) > 0)
      {
        // then we have an output tuple!
        tempWordID = currentWord;
        tempTopicID = currentTopic;
        tempOutCount = outputCounts[currentWord][currentTopic];

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
  VGSchema inputSchema()
  {
    return (VGSchema){
        10,
        {"integer", "integer", "integer", "integer", "integer", "integer", "integer", "integer random", "double", "double"},
        {"in_topic_id", "in_topic_count", "in_wtopic_id", "in_word_id", "in_word_topic_count", "last_word_id", "last_topic_id", "last_count", "alpha", "beta"}};
  }

  VGSchema outputSchema()
  {
    return (VGSchema){3, {"integer", "integer", "integer random"}, {"out_word_id", "out_topic_id", "out_count"}};
  }

  const char *getName()
  {
    return "Multinomial_WordTopic_Collapsed_Sparse_WordCount";
  }
};

/** External creation/destruction methods -- DO NOT MODIFY */
VGFunction *create()
{
  return (new Multinomial_WordTopic_Collapsed_Sparse_WordCount());
}

void destroy(VGFunction *vgFunction)
{
  delete (Multinomial_WordTopic_Collapsed_Sparse_WordCount *)vgFunction;
}
