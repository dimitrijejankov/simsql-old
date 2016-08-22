def get_c_type(input_type):
    if input_type == "integer":
        return "long"
    if input_type == "vector[a]":
        return "Vector"
    if input_type == "matrix[a][a]":
        return "Matrix"
    else:
        return input_type


def get_type_token(input_type):
    if input_type == "double":
        return "%lf"
    elif input_type == "integer":
        return "%ld"
    else:
        ""


def generate_license(of):
    of.write("""/*****************************************************************************
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

""")


def generate_include(of):
    of.write("""#include <stdio.h>
#include "VGFunction.h\"\n\n""")


def generate_input_record(of, vg_input):
    of.write("""struct RecordIn {\n""")

    for value in vg_input:
        of.write("\t" + get_c_type(value[1]) + " *" + value[0] + ";\n")

    of.write("""};\n\n""")


def generate_output_record(of, vg_output):
    of.write("""struct RecordOut {\n""")

    for value in vg_output:
        of.write("\t" + get_c_type(value[1]) + " *" + value[0] + ";\n")

    of.write("""};\n\n""")


def generate_class_header(of, vg_function):
    of.write("/** VG Function class */\n")
    of.write("class %s : public VGFunction { \npublic: \n" % vg_function)


def generate_constructor(of, vg_function):
    of.write("""/** Constructor. Use this to declare your RNG and other
  * important structures .
  */\n""")
    of.write("\t%s() {}\n\n" % vg_function)


def generate_destructor(of, vg_function):
    of.write("/** Destructor. Deallocate everything from the constructor. */\n")
    of.write("\t~%s() {}\n\n" % vg_function)


def generate_initialize_seed(of):
    of.write("/** Initializes the RNG seed for a given call. */\n")
    of.write("\tvoid initializeSeed(long seedValue) {}\n\n")


def generate_clear_params(of):
    of.write("/**\n")
    of.write("  * Clears the set of parameters for the first call to takeParams.\n")
    of.write("  * If possible, uses the default parameter set.\n")
    of.write("  */\n\n")

    of.write("\tvoid clearParams() {\n")
    of.write("\t\tprintf(\"---DATA START---\\n\");\n")
    of.write("\t\tfflush(stdout);\n")
    of.write("\t}\n\n")


def generate_finalize_trial(of):
    of.write("/** Finalizes the current trial and prepares the structures for\n")
    of.write("  * another fresh call to outputVals(). */\n")
    of.write("\tvoid finalizeTrial() {\n")
    of.write("\t\tprintf(\"---DATA END---\\n\");\n")
    of.write("\t\tfflush(stdout);\n")
    of.write("\t}\n\n")


def generate_print_methods(of):
    of.write("""    void printVector(Vector* v) {

        if(v->value == NULL)
            return;

        printf("[");
        for(int i = 0; i < (size_t)v->length; i++){
            if (i != ((size_t)v->length) - 1) {
                printf("%lf ", v->value[i]);
            }
            else {
                printf("%lf", v->value[i]);
            }
        }
        printf("] ");
    }


    void printMatrix(Matrix* m) {

        if(m->value == NULL)
            return;

        printf("[");
        for(int i = 0; i < (size_t)m->numRow; i++){
            printf("[");
            for(int j = 0; j < (size_t)m->numCol; j++) {
                if (j != ((size_t)m->numCol) - 1) {
                    printf("%lf ", m->value[i * (size_t)m->numCol + j]);
                }
                else {
                    printf("%lf", m->value[i * (size_t)m->numCol + j]);
                }
            }
            printf("]");
        }
        printf("] ");
    }\n\n""")


def generate_take_parameters(of, vg_input):
    of.write("/**\n")
    of.write("  * Passes the parameter values. Might be called several times\n")
    of.write("  * for each group.\n")
    of.write("  */\n")
    of.write("\tvoid takeParams(RecordIn &input) {\n")

    for value in vg_input:
        of.write("\t\tif (input.%s != NULL) {\n" % value[0])

        if value[1] == "vector[a]":
            of.write("""\t\t\tprintVector(input.%s);\n""" % value[0])
        elif value[1] == "matrix[a][a]":
            of.write("""\t\t\tprintMatrix(input.%s);\n""" % value[0])
        else:
            of.write("\t\t\tprintf(\"%s \", *input.%s);\n" % (get_type_token(value[1]), value[0]))
        of.write("\t\t}\n")
        of.write("\t\telse\n")
        of.write("\t\t\tprintf(\"null \");\n")
        of.write("\n")

    of.write("\t\tprintf(\"\\n\");\n\t}\n\n")


def generate_output_values(of):
    of.write("/**\n")
    of.write("  * Produces the sample values. Returns 1 if there are more\n")
    of.write("  * records to be produced for the current sample, 0 otherwise.\n")
    of.write("  */\n")
    of.write("\tint outputVals(RecordOut &output) {\n")
    of.write("\t\treturn 0;\n")
    of.write("\t}\n\n")


def generate_input_schema(of, vg_input):
    of.write("/** Schema information methods -- DO NOT MODIFY */\n")
    of.write("\tVGSchema inputSchema() {\n")
    of.write("\t\treturn (VGSchema){%s, {" % len(vg_input))

    for i in xrange(len(vg_input)):
        if i == len(vg_input) - 1:
            of.write("\"%s\"" % vg_input[i][1])
        else:
            of.write("\"%s\", " % vg_input[i][1])

    of.write("}, {")

    for i in xrange(len(vg_input)):
        if i == len(vg_input) - 1:
            of.write("\"%s\"" % vg_input[i][0])
        else:
            of.write("\"%s\", " % vg_input[i][0])

    of.write("}};\n\t}\n\n")


def generate_output_schema(of, vg_output):
    of.write("\tVGSchema outputSchema() {\n")
    of.write("\t\treturn (VGSchema){%s, {" % len(vg_output))

    for i in xrange(len(vg_output)):
        if i == len(vg_output) - 1:
            of.write("\"%s\"" % vg_output[i][1])
        else:
            of.write("\"%s\", " % vg_output[i][1])

    of.write("}, {")

    for i in xrange(len(vg_output)):
        if i == len(vg_output) - 1:
            of.write("\"%s\"" % vg_output[i][0])
        else:
            of.write("\"%s\", " % vg_output[i][0])

    of.write("}};\n\t}\n\n")


def generate_get_name(of, vg_function):
    of.write("\tconst char *getName() {\n")
    of.write("\t\treturn \"%s\";\n" % vg_function)
    of.write("\t}\n\n")


def generate_class_end(of):
    of.write("};\n")


def generate_create_and_destroy(of, vg_function):
    of.write("/** External creation/destruction methods -- DO NOT MODIFY */\n")
    of.write("VGFunction *create() {\n")
    of.write("\treturn(new %s());\n" % vg_function)
    of.write("}\n\n")

    of.write("void destroy(VGFunction *vgFunction) {\n")
    of.write("\tdelete (%s *)vgFunction;\n" % vg_function)
    of.write("}")


def generate_stub(outfile_path, vg_function, vg_input, vg_output):

    outfile = open(outfile_path, 'w')

    generate_license(outfile)
    generate_include(outfile)
    generate_input_record(outfile, vg_input)
    generate_output_record(outfile, vg_output)

    generate_class_header(outfile, vg_function)

    generate_constructor(outfile, vg_function)
    generate_destructor(outfile, vg_function)

    generate_initialize_seed(outfile)

    generate_clear_params(outfile)
    generate_finalize_trial(outfile)
    generate_print_methods(outfile)

    generate_take_parameters(outfile, vg_input)
    generate_output_values(outfile)

    generate_input_schema(outfile, vg_input)
    generate_output_schema(outfile, vg_output)

    generate_get_name(outfile, vg_function)

    generate_class_end(outfile)

    generate_create_and_destroy(outfile, vg_function)
