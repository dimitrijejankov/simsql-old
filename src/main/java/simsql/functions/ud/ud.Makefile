# Makefile for building individual UD functions

SRCS = $(wildcard *.ud.cc)
FUNC = $(patsubst %.ud.cc,%,$(SRCS))

ECHO = echo
RM = rm -f

CCFLAGS = -c -fPIC -O3
CLFLAGS = -lgsl -lgslcblas -ldl -shared -rdynamic

.SILENT: all

all: $(FUNC)
%: %.ud.cc
	@$(ECHO) "\033[1m\033[31mBuilding UD function '$@'.\033[0m"
	@$(CC) -I $(INCLUDE) $(CCFLAGS) $@.ud.cc -o $@.ud.o
	@$(CC) -o $(OUTDIR)/$@.ud.so $@.ud.o $(CLFLAGS)
	@$(RM) $@.ud.o

clean:
	@$(RM) $(OUTDIR)/*.vg.o
