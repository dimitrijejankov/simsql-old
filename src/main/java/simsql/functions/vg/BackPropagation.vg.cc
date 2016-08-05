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
#include <gsl/gsl_blas.h>
#include <map>
#include <list>

#include "VGFunction.h"

struct RecordIn {
    long *layerID;
    long *neuronID;
    Vector *w;

    long *obs_layerID;
    long *obs_neuronID;
    double *o;

    Vector *x;
    Vector *y;
};

struct RecordOut {

    long *layerID;
    long *neuronID;

    Vector *gw;
    double *gb;
};

struct Neuron {
    long neuronId;
    gsl_vector* w;
};

void free_neuron(Neuron* n){
    gsl_vector_free(n->w);
    delete n;
}

/** VG Function class */
class BackPropagation : public VGFunction {
private:

    long current_layer;
    long current_neuron;

    gsl_vector* y;

    std::map<long, std::list<Neuron*> > neurons;
    std::map<long, gsl_vector* > activations;

    gsl_vector* delta;

    gsl_vector* b_gradient;
    gsl_matrix* w_gradient;

    long o_neuron;

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

    void storeActivation(long layer, long neuronID, double a) {
        if(activations.find(layer) != activations.end()){
            gsl_vector_set(activations[layer], neuronID, a);
        }
        else {
            gsl_vector* vt = gsl_vector_calloc(neurons[layer].size());
            gsl_vector_set(vt, neuronID, a);
            activations.insert(std::pair<long, gsl_vector* >(layer, vt));
        }
    }

    void storeActivationLayer(long layer, gsl_vector* vt) {
        activations.insert(std::pair<long, gsl_vector* >(layer, vt));
    }

    void cost_derivative(gsl_vector* output_activations, gsl_vector* y, gsl_vector* out) {
        gsl_vector_memcpy(out, output_activations);
        gsl_vector_sub(out, y);
    }

    void inner_cost_derivative(gsl_vector* new_delta) {

        gsl_vector_set_all(new_delta, 0.0f);

        long prev_layer = current_layer+1;

        for (std::list<Neuron*>::iterator it=neurons[prev_layer].begin(); it != neurons[prev_layer].end(); ++it) {
            gsl_vector* ws = (*it)->w;

            gsl_vector_scale(ws, delta->data[(*it)->neuronId]);
            gsl_vector_add(new_delta, ws);

            free_neuron(*it);
        }

        neurons.erase(prev_layer);
    }

    void calculate_sigmoid_prime(gsl_vector* y, gsl_vector* out) {
        gsl_vector_set_all(out, 1.0);
        gsl_vector_sub(out, y);
        gsl_vector_mul(out, y);
    }

    void calculate_output_layer(){
        current_layer = neurons.size() - 1;
        int current_layer_size = activations[current_layer]->size;

        delta = gsl_vector_calloc(current_layer_size);
        cost_derivative(activations[current_layer], y, delta);

        gsl_vector* sigmoid_prime = gsl_vector_calloc(current_layer_size);
        calculate_sigmoid_prime(activations[current_layer], sigmoid_prime);

        gsl_vector_mul(delta, sigmoid_prime);

        // I reuse the memory of the sigmoid_prime, so I don't have to re allocate the memory
        b_gradient = sigmoid_prime;
        gsl_vector_memcpy(b_gradient, delta);

        // matrix multiplication in blas is a sin!
        gsl_matrix_view delta_m = gsl_matrix_view_array(delta->data, delta->size, 1);
        gsl_matrix_view act_m = gsl_matrix_view_array(activations[current_layer-1]->data,
                                                      1, activations[current_layer-1]->size);
        w_gradient = gsl_matrix_calloc(delta->size, delta->size);

        gsl_blas_dgemm (CblasNoTrans, CblasNoTrans, 1.0, &delta_m.matrix, &act_m.matrix, 0.0, w_gradient);

        gsl_vector_free(activations[current_layer]);

        current_neuron = current_layer_size-1;
    }

    void calculate_next_hidden_layer() {
        current_layer--;

        int current_layer_size = activations[current_layer]->size;

        gsl_vector* sigmoid_prime = gsl_vector_calloc(current_layer_size);
        calculate_sigmoid_prime(activations[current_layer], sigmoid_prime);

        gsl_vector* new_delta = gsl_vector_calloc(activations[current_layer]->size);
        inner_cost_derivative(new_delta);

        gsl_vector_mul(new_delta, sigmoid_prime);
        gsl_vector_free(delta);
        delta = new_delta;

        // I reuse the memory of the sigmoid_prime, so I don't have to re allocate the memory
        b_gradient = sigmoid_prime;
        gsl_vector_memcpy(b_gradient, delta);

        gsl_matrix_view delta_m = gsl_matrix_view_array(delta->data, delta->size, 1);
        gsl_matrix_view act_m = gsl_matrix_view_array(activations[current_layer-1]->data,
                                                      1, activations[current_layer-1]->size);

        w_gradient = gsl_matrix_calloc(delta->size, delta->size);

        gsl_blas_dgemm (CblasNoTrans, CblasNoTrans, 1.0, &delta_m.matrix, &act_m.matrix, 0.0, w_gradient);

        gsl_vector_free(activations[current_layer]);

        current_neuron = current_layer_size - 1;
    }

public:
/** Constructor. Use this to declare your RNG and other
  * important structures .
  */
    BackPropagation() {}

/** Destructor. Deallocate everything from the constructor. */
    ~BackPropagation() {}

/** Initializes the RNG seed for a given call. */
    void initializeSeed(long seedValue) {

    }

/**
  * Clears the set of parameters for the first call to takeParams.
  * If possible, uses the default parameter set.
  */

    void clearParams() {
        delta = NULL;
    }

/** Finalizes the current trial and prepares the structures for
  * another fresh call to outputVals(). */
    void finalizeTrial() {

        gsl_vector_free(delta);
        gsl_vector_free(b_gradient);
        gsl_matrix_free(w_gradient);

        gsl_vector_free(activations[-1]);

        for (std::list<Neuron*>::iterator it=neurons[0].begin(); it != neurons[0].end(); ++it) {
            free_neuron(*it);
        }
    }

/**
  * Passes the parameter values. Might be called several times
  * for each group.
  */
    void takeParams(RecordIn &input) {

        if (input.layerID != NULL && input.neuronID != NULL && input.w != NULL) {

            Neuron* n = new Neuron();
            n->neuronId = *input.neuronID;
            n->w = getVector(input.w);

            storeNeuron(*input.layerID, n);
        }

        if (input.y != NULL) {

            y = getVector(input.y);
            storeActivationLayer(-1, getVector(input.x));
        }

        if (input.obs_layerID != NULL && input.obs_neuronID && input.o != NULL) {
            storeActivation(*input.obs_layerID, *input.obs_neuronID, *input.o);
        }

    }

/**
  * Produces the sample values. Returns 1 if there are more
  * records to be produced for the current sample, 0 otherwise.
  */
    int outputVals(RecordOut &output) {

        // Are we done yet?
        if (current_layer == 0 && current_neuron == -1)
            return 0;

        // Gradient of the output layer
        if(delta == NULL) {
            calculate_output_layer();
        }

        // Gradient of the hidden layer
        if (current_neuron == -1) {

            calculate_next_hidden_layer();
        }

        o_neuron = current_neuron;

        output.layerID = &current_layer;
        output.neuronID = &o_neuron;
        output.gb = &b_gradient->data[current_neuron];

        output.gw = (Vector *)malloc(sizeof(Vector));
        output.gw->length = w_gradient->size1;
        output.gw->value = (double *)malloc(sizeof(double) * output.gw->length);
        memcpy(output.gw->value,
                w_gradient->data + w_gradient->size1 * current_neuron,
                sizeof(double) * output.gw->length);

        current_neuron--;

        return 1;
    }

/** Schema information methods -- DO NOT MODIFY */
    VGSchema inputSchema() {
        return (VGSchema){8, {"integer", "integer", "vector[a]", "integer", "integer", "double", "vector[a]", "vector[a]"},
                          {"layerID", "neuronID", "w", "obs_layerID", "obs_neuronID", "o", "x", "y"}};
    }

    VGSchema outputSchema() {
        return (VGSchema){4, {"integer", "integer", "vector[a]", "double"},
                          {"layerID", "neuronID", "gw", "gb"}};
    }

    const char *getName() {
        return "BackPropagation";
    }

};
/** External creation/destruction methods -- DO NOT MODIFY */
VGFunction *create() {
    return(new BackPropagation());
}

void destroy(VGFunction *vgFunction) {
    delete (BackPropagation *)vgFunction;
}