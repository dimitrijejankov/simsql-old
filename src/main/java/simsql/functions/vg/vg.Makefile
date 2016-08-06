

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


# Makefile for building all VG functions

ECHO = echo
RM = rm -f

SRCS = $(wildcard *.vg.cc)
FUNC = $(patsubst %.vg.cc,%,$(SRCS))

CCFLAGS = -c -fPIC -O3
CLFLAGS = -lgsl -lgslcblas -ldl -shared -rdynamic


.SILENT: all

all: $(FUNC)
%: %.vg.cc
	@$(ECHO) "\033[1m\033[31mBuilding VG function '$@'.\033[0m"
	@$(CC) -I $(INCLUDE) $(CCFLAGS) $@.vg.cc -o $@.vg.o
	@$(CC) -o $(OUTDIR)/$@.vg.so $@.vg.o $(CLFLAGS)
	@$(RM) $@.vg.o

clean:
	@$(RM) $(OUTDIR)/*.vg.o
