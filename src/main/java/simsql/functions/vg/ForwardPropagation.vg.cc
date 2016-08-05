/*****************************************************************************
 *                                                                           *
 *  Copyright 2014 Rice University                                           *
 *                                                                           *
 *  Licensed under the Apache License, Version 2.0 (the "License");          *
 *  you may not use this of except in compliance with the License.         *
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

#include <stdio.h>
#include <memory.h>
#include <map>
#include <list>
#include <memory>
#include <gsl/gsl_blas.h>
#include "VGFunction.h"

float sigmoid(float x) {
    float exp_value;
    float return_value;
    exp_value = exp((double) -x);

    return 1 / (1 + exp_value);
}

struct RecordIn {

    long *layerID;
    long *neuronID;
    Vector *w;
    double *b;

	Vector *x;

};

struct RecordOut {
	long *layerID;
	long *neuronID;
	double *o;
};

struct Neuron {
    long neuronId;
    gsl_vector* w;
    double bias;
};

void free_neuron(Neuron* n){
    gsl_vector_free(n->w);
    delete n;
}


/** VG Function class */
class ForwardPropagation : public VGFunction {
private:

    bool initialized;

    long current_layer;
    long neuronID;
    double o;

    std::map<long, std::list<Neuron*> > neurons;

    gsl_vector* x;
    gsl_vector* x_new;

    void storeNeuron(long layer, Neuron* n) {
        if(neurons.find(layer) != neurons.end()){
            neurons[layer].push_back(n);
        }
        else {
            std::list<Neuron*> ls;
            ls.push_back(n);
            neurons.insert(std::pair<long, std::list<Neuron*> >(layer, ls));
        }
    }

public:
/** Constructor. Use this to declare your RNG and other
  * important structures .
  */
	ForwardPropagation() {}

/** Destructor. Deallocate everything from the constructor. */
	~ForwardPropagation() {}

/** Initializes the RNG seed for a given call. */
	void initializeSeed(long seedValue) {}

/**
  * Clears the set of parameters for the first call to takeParams.
  * If possible, uses the default parameter set.
  */

	void clearParams() {
        current_layer = 0;
        initialized = false;
	}

/** Finalizes the current trial and prepares the structures for
  * another fresh call to outputVals(). */
	void finalizeTrial() {
        gsl_vector_free(x);
	}

/**
  * Passes the parameter values. Might be called several times
  * for each group.
  */
	void takeParams(RecordIn &input) {

        if (input.layerID != NULL || input.neuronID != NULL || input.w != NULL || input.b != NULL) {

            Neuron* n = new Neuron();
            n->neuronId = *input.neuronID;
            n->bias = *input.b;
            n->w = getVector(input.w);

            storeNeuron(*input.layerID, n);
        }

        if (input.x != NULL) {
            x = getVector(input.x);
        }
	}

/**
  * Produces the sample values. Returns 1 if there are more
  * records to be produced for the current sample, 0 otherwise.
  */
	int outputVals(RecordOut &output) {

        if (!initialized) {
            x_new = gsl_vector_calloc(neurons[current_layer].size());
            initialized = true;
        }

        if(neurons[current_layer].empty()) {
            current_layer++;

            gsl_vector_free(x);
            x = x_new;

            if(neurons.find(current_layer) == neurons.end()){
                return 0;
            }

            x_new = gsl_vector_calloc(neurons[current_layer].size());

            return outputVals(output);
        }

        Neuron* neuron = neurons[current_layer].back();
        neurons[current_layer].pop_back();

        neuronID = neuron->neuronId;

        double dot;

        gsl_blas_ddot(x, neuron->w, &dot);

        o = sigmoid(dot + neuron->bias);

        output.layerID = &current_layer;
        output.neuronID = &neuronID;
        output.o = &o;

        free_neuron(neuron);

        gsl_vector_set(x_new, neuronID, o);

        return 1;
	}

/** Schema information methods -- DO NOT MODIFY */
	VGSchema inputSchema() {
		return (VGSchema){5, {"integer", "integer", "vector[a]", "double", "vector[a]"}, {"layerID", "neuronID", "w", "b", "x"}};
	}

	VGSchema outputSchema() {
		return (VGSchema){3, {"integer", "integer", "double"}, {"layerID", "neuronID", "o"}};
	}

	const char *getName() {
		return "ForwardPropagation";
	}

};
/** External creation/destruction methods -- DO NOT MODIFY */
VGFunction *create() {
	return(new ForwardPropagation());
}

void destroy(VGFunction *vgFunction) {
	delete (ForwardPropagation *)vgFunction;
}