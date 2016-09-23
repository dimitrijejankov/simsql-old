#!/usr/bin/env python

from stub_generator import generate_stub
from cmake_generator import generate_cmake
from test_generator import generate_test

vg_function = 'NNNN'


# dataID, layerID, neuronID, o
vg_input = [('x', 'vector[a]'),
            ('j', 'integer'),
            ('w', 'matrix[a][a]'),
            ('e', 'integer')]


vg_output = [
    ('r', 'vector[a]'),
    ('row_id', 'integer'),
    ('column_id', 'integer')]


outfile_path = '%s.vg.cc' % vg_function
cmake_of_path = 'CMakeLists.txt'

test_of_path = 'Test%s.cpp' % vg_function

generate_stub(outfile_path, vg_function, vg_input, vg_output)
generate_cmake(cmake_of_path, vg_function)
generate_test(test_of_path, vg_function, vg_input, vg_output)

