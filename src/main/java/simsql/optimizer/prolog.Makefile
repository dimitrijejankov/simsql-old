

# /*****************************************************************************
#  *                                                                           *
#  *  Copyright 2014 Rice University                                           *
#  *                                                                           *
#  *  Licensed under the Apache License, Version 2.0 (the "License");          *
#  *  you may not use this file except in compliance with the License.         *
#  *  You may obtain a copy of the License at                                  *
#  *                                                                           *
#  *      http://www.apache.org/licenses/LICENSE-2.0                           *
#  *                                                                           *
#  *  Unless required by applicable law or agreed to in writing, software      *
#  *  distributed under the License is distributed on an "AS IS" BASIS,        *
#  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. *
#  *  See the License for the specific language governing permissions and      *
#  *  limitations under the License.                                           *
#  *                                                                           *
#  *****************************************************************************/


# Makefile for building a Prolog executable for the optimizer.

SWIPL = swipl
RM = rm
OUTDIR = .

.SILENT: all

all: control_transformations.pl lib.pl plancost.pl graphoperations.pl loader.pl optimizer.pl transformations.pl
	$(SWIPL) -q -g "consult(['control_transformations.pl','lib.pl','plancost.pl', 'graphoperations.pl', 'loader.pl', 'optimizer.pl', 'transformations.pl']), qsave_program('$(OUTDIR)/optimizer.pro', [goal(go), stand_alone(true), toplevel(halt(1))]), halt(0)" -t "halt(1)"


clean:
	$(RM) $(OUTDIR)/optimizer.pro
