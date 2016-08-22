from stub_generator import get_type_token, get_c_type


def generate_include(of, vg_function):
    of.write("""#include <iostream>
#include <cstring>
#include <list>
#include <vector>
#include "%s.vg.cc"\n\n""" % vg_function)


def generate_class_header(of, vg_function):
    of.write("class Test%s {\n" % vg_function)


def generate_private_attributes(of):
    of.write("private: \n\nVGFunction* tp;\n\n")


def generate_the_public_annotation(of):
    of.write("public:\n\n")


def generate_constructor(of, vg_function):
    of.write("""\tTest%s() {\n
\t\ttp = create();
    }\n\n""" % vg_function)


def generate_destructor(of, vg_function):
    of.write("""\t~Test%s() {
\t\tdestroy(tp);
    }\n\n""" % vg_function)


def generate_execute_test(of, vg_function):
    of.write("""\tvoid executeTest(){

\t\tchar buffer[512];

\t\tFILE *fp;
\t\tfp = fopen("input.in", "r");

\t\twhile (fgets (buffer , 512 , fp) != NULL) {

\t\t\tRecordOut ro;

\t\t\ttp->initializeSeed(rand());

\t\t\ttp->clearParams();
\t\t\tpassInParameters(fp, buffer);

\t\t\ttp->finalizeTrial();
\t\t}
\t}\n\n""")


def generate_trim_whitespace(of):
    of.write("""    char* trimWhitespace(char *str)
    {
        char *end;

        while(isspace(*str)) str++;

        if(*str == 0)
            return str;

        end = str + strlen(str) - 1;
        while(end > str && isspace(*end)) end--;

        *(end+1) = 0;

        return str;
    }\n\n""")


def generate_vector_load(of, key):

    of.write("""\t\t\tsscanf(temp, "%s%n", t, &step);""")

    of.write("""
\t\t\tif(strstr(t, "null") == NULL) {
\t\t\t\tri.%s = new Vector();
\t\t\t\ttemp = loadVector(temp, *ri.%s);
\t\t\t}
\t\t\telse {
\t\t\t\tri.%s = NULL;
\t\t\t\ttemp += step;
\t\t\t\ttemp = trimWhitespace(temp);
\t\t\t}\n\n""" % (key, key, key))


def generate_matrix_load(of, key):

    of.write("""\t\t\tsscanf(temp, "%s%n", t, &step);""")

    of.write("""
\t\t\tif(strstr(t, "null") == NULL) {
\t\t\t\tri.%s = new Matrix();
\t\t\t\ttemp = loadMatrix(temp, *ri.%s);
\t\t\t}
\t\t\telse {
\t\t\t\tri.%s = NULL;
\t\t\t\ttemp += step;
\t\t\t\ttemp = trimWhitespace(temp);
\t\t\t}\n\n""" % (key, key, key))


def generate_scan_scalar(of, key, token):

    of.write("""\t\t\tsscanf(temp, "%s%n", t, &step);""")

    of.write("""
\t\t\tif(strstr(t, "null") == NULL) {
\t\t\t\tri.%s = new long;
\t\t\t\tsscanf(temp, "%s%%n", ri.%s, &step);
\t\t\t}
\t\t\telse {
\t\t\t\tri.%s = NULL;
\t\t\t}

\t\t\ttemp += step;
\t\t\ttemp = trimWhitespace(temp);\n\n""" % (key, token, key, key))


def generate_load_vector_method(of):
    of.write("""    char* loadVector(char *buffer, Vector &vector) {

        char temp[512];
        double t;

        std::vector<double> vec;

        buffer = trimWhitespace(buffer);

        buffer++;

        while(*buffer != ']') {

            int i;
            for(i=0; i < 512; i++){

                if(*buffer == ' ' || *buffer == ']')
                    break;

                temp[i] = *buffer;
                buffer++;
            }

            temp[i] = 0;

            sscanf(temp, "%lf", &t);
            vec.push_back(t);

            buffer = trimWhitespace(buffer);
        }

        buffer += 1;

        vector.length = (double)vec.size();
        vector.value = new double[vec.size()];

        memcpy(vector.value, vec.data(), vec.size() * sizeof(double));

        return buffer;
    }\n\n""")


def generate_load_matrix_method(of):
    of.write("""    char* loadMatrix(char* buffer, Matrix &matrix) {
        char temp[512];
        double t;

        std::list<Vector> mat;

        buffer = trimWhitespace(buffer);
        buffer++;

        while (*buffer != ']') {

            buffer = trimWhitespace(buffer);

            Vector v;
            buffer = loadVector(buffer, v);
            mat.push_back(v);
        }

        buffer = trimWhitespace(buffer);
        buffer += 1;

        matrix.numRow = mat.size();
        matrix.numCol = mat.front().length;
        matrix.value = new double[mat.size() * (size_t)mat.front().length];

        int i = 0;
        for(std::list<Vector>::iterator it = mat.begin(); it != mat.end(); ++it){
            memcpy(matrix.value + i * (size_t)mat.front().length,
                   (*it).value,
                   sizeof(double) * (size_t)mat.front().length);
            i++;
        }

        return buffer;
    }\n\n""")


def generate_pass_in_parameters(of, vg_input):
    of.write("""\tvoid passInParameters(FILE *fp, char *buffer) {

\t\tRecordIn ri;\n""")

    of.write("\n")
    of.write("""\t\twhile(fgets (buffer , 512 , fp) != NULL && strcmp(buffer, "---DATA END---\\n") != 0) {\n""")

    of.write("""
\t\t\tint step;

\t\t\tchar* temp = buffer;
\t\t\tchar t[512];\n\n\n""")

    for value in vg_input:
        if value[1] == "vector[a]":
            generate_vector_load(of, value[0])
        elif value[1] == "matrix[a][a]":
            generate_matrix_load(of, value[0])
        else:
            generate_scan_scalar(of, value[0], get_type_token(value[1]))

    of.write("\t\t\ttp->takeParams(ri);\n\n")

    for value in vg_input:
        if value[1] == "vector[a]" or value[1] == "matrix[a][a]":
            of.write("""
\t\t\tif(ri.%s != NULL) {
\t\t\t\tif(ri.%s->value != NULL){
\t\t\t\t\tdelete ri.%s->value;
\t\t\t\t}

\t\t\t\tdelete ri.%s;
\t\t\t}\n""" % (value[0], value[0], value[0], value[0]))
        else:
            of.write("""
\t\t\tif(ri.%s != NULL) {
\t\t\t\tdelete ri.%s;
\t\t\t}\n""" % (value[0], value[0]))

    of.write("\t\t}\n")
    of.write("\t}\n")


def generate_class_end(of):
    of.write("};\n")


def generate_main(of, vg_function):
    of.write("""int main() {

\tTest%s* t = new Test%s();

\tt->executeTest();

\tdelete t;
}\n""" % (vg_function, vg_function))


def generate_test(outfile_path, vg_function, vg_input, vg_output):

    outfile = open(outfile_path, 'w')

    generate_include(outfile, vg_function)
    generate_class_header(outfile, vg_function)
    generate_private_attributes(outfile)
    generate_the_public_annotation(outfile)
    generate_constructor(outfile, vg_function)
    generate_destructor(outfile, vg_function)
    generate_execute_test(outfile, vg_function)

    generate_trim_whitespace(outfile)
    generate_load_vector_method(outfile)
    generate_load_matrix_method(outfile)

    generate_pass_in_parameters(outfile, vg_input)
    generate_class_end(outfile)
    generate_main(outfile, vg_function)
