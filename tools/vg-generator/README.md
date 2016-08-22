##What Is a VGFunction?

VGFunctions are SimSql functions that are used in conjunction with the **with** clause to generate tuples from the provided input tuples.
The general syntax of using a VGFunction looks like this : 
~~~~
create table table[i](att_1, att_2...) as
    with name as vg_function_name
    (
      (
        select a1, a2... from t1,t2,....
      ),
      (
        select b1, b2... from t1,t2,....
      ),
      ...
    )
    select...
~~~~

Each VGFunction is a C++ compiled class located in **src/function/vg** folder, that has the following structure : 
~~~
/** 
The structure used for the input touples.
**/
struct RecordIn {
    type *name,
    ...
};

/**
The structure used for the output touples.
**/
struct RecordOut {
    type *name,
    ...
};

/** VG Function class */
class MyVGFunction : public VGFunction { 
public: 
/** Constructor. Use this to declare your RNG and other
  * important structures .
  */
	MyVGFunction() {}

/** Destructor. Deallocate everything from the constructor. */
	~MyVGFunction() {}

/** Initializes the RNG seed for a given call. */
	void initializeSeed(long seedValue) {}

/**
  * Clears the set of parameters for the first call to takeParams.
  * If possible, uses the default parameter set.
  */

	void clearParams() {
	}

/** Finalizes the current trial and prepares the structures for
  * another fresh call to outputVals(). */
	void finalizeTrial() {
	}
	
/**
  * Passes the parameter values. Might be called several times
  * for each group.NNNN
  */
	void takeParams(RecordIn &input) {
	}

/**
  * Produces the sample values. Returns 1 if there are more
  * records to be produced for the current sample, 0 otherwise.
  */
	int outputVals(RecordOut &output) {
		return 0;
	}

/** Schema information methods -- DO NOT MODIFY */
	VGSchema inputSchema() {
		return (VGSchema){number_of_input_arguments, {"type1", "type1"...}, {"argument1", "argument2",....}};
	}

	VGSchema outputSchema() {
		return (VGSchema){"number_of_output_arguments", {"type1", "type1"...}, {"argument1", "argument2",....}};
	}

	const char *getName() {
		return "MyVGFunction";
	}

};
/** External creation/destruction methods -- DO NOT MODIFY */
VGFunction *create() {
	return(new MyVGFunction());
}

void destroy(VGFunction *vgFunction) {
	delete (MyVGFunction *)vgFunction;
}

~~~

This directory contains a tool that can be used to generate mock-up VGFunctions that can be used for debugging or development.