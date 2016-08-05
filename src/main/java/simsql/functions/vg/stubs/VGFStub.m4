#include <stdio.h>
#include "VGFunction.h"

/** 
  * Input record, used by the takeParams() method.
  * Values could be NULL, meaning they were not present in the tuple.
  */
struct RecordIn {
_M4_VG_DECLARE(_VG_INPUT_ATTS)
};

/**
  * Output record, used by the outputVals() method.
  * If any of the values is NULL, then the user must allocate space for
  * it (the engine will de-allocate). 
  */
struct RecordOut {
_M4_VG_DECLARE(_VG_OUTPUT_ATTS)
};

// ----------------------------------------------------------- // 

/** VG Function class */
class _VG_NAME : public VGFunction {

      private:
	/** RNG and structures go here. */


	// ----------------------------------------------------------- // 

      public:

	/** Constructor. Use this to declare your RNG and other
	  * important structures .
	  */
	_VG_NAME`'() {

	}

	/** Destructor. Deallocate everything from the constructor. */
	~_VG_NAME`'() {
	
	}

	/** Initializes the RNG seed for a given call. */
	void initializeSeed(long seedValue) {

	}

	/** Finalizes the current trial and prepares the structures for
	  * another fresh call to outputVals(). */
	void finalizeTrial() {

	}

	/**
	  * Clears the set of parameters for the first call to takeParams.
	  * If possible, uses the default parameter set.
	  */
	void clearParams() {

	}

	
	/** 
	  * Passes the parameter values. Might be called several times
	  * for each group. 
	  */ 
	void takeParams(RecordIn &input) {

	}

	/** 
	  * Produces the sample values. Returns 1 if there are more
	  * records to be produced for the current sample, 0 otherwise. 
	  */
	int outputVals(RecordOut &output) { 

	}

	// ----------------------------------------------------------- // 

	/** Schema information methods -- DO NOT MODIFY */
	VGSchema inputSchema() {
_M4_VG_RETURN(_VG_INPUT_ATTS)
	}

	VGSchema outputSchema() {
_M4_VG_RETURN(_VG_OUTPUT_ATTS)
	}

	const char *getName() {
	      return "_VG_NAME";
	}
};

// ----------------------------------------------------------- // 

/** External creation/destruction methods -- DO NOT MODIFY */
VGFunction *create() {
     return(new _VG_NAME`'());
}

void destroy(VGFunction *vgFunction) {
     delete (_VG_NAME *)vgFunction;
}
