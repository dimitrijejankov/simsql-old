#ifndef _UDFUNCTIONLIB_H
#define _UDFUNCTIONLIB_H

#include "VGFunction.h"

class RecordIn;
class RecordOut;

// The general abstract definition of a UD Function.
class UDFunction : public VGFunction {
private:
  RecordIn &recIn;
  bool active;

public:

  UDFunction() {
    active = false;
    recIn = NULL;
  }
  
  virtual int apply(RecordIn &rin, RecordOut &rout) = 0;
  virtual VGSchema inputSchema() = 0;
  virtual char* outputType() = 0;
  virtual const char *getName() = 0;

  void initializeSeed(long seedVal) {
    // do nothing.
  }

  void clearParams() {
    active = false;
  }

  void takeParams(RecordIn &rin) {
    recIn = rin;
    active = true;
  }

  int outputVals(RecordOut &rout) {
    if (!active)
      return 0;

    active = false;
    return apply(recIn, rout);
  }

  void finalizeTrial() {
    active = true;
  }

  VGSchema outputSchema() {
    return (VGSchema){1, {outputType()}, {"outValue"}};
  }
};

#endif // _UDFUNCTIONLIB_H
