

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


# Makefile for building the Runtime Native JNI interface

INCLUDE=$JAVA_HOME/include
OUTDIR=/home/dimitrije/Documents/ninja
CC = /usr/bin/g++
ECHO = echo
CCFLAGS = -c -Wall -fPIC -O3
CLFLAGS = -lgsl -lgslcblas -ldl -shared -rdynamic
RM = rm -f

.SILENT: all

all: Matrix.h Matrix.cc

	$(ECHO) "\033[1m\033[31mBuilding the Runtime Native JNI methods.\033[0m"
	$(CC) -I $(INCLUDE) $(CCFLAGS) Matrix.cc -o Matrix.o
	$(CC) -o $(OUTDIR)/Matrix.so Matrix.o $(CLFLAGS)
	$(RM) Matrix.o

clean:
	$(RM) Matrix.so
