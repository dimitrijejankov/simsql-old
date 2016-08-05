This tool generates C++ code stubs for user-defined VG functions from
simple description files.

For example, given the following file myFunction.vg :

    vgfunction myFunction
    input [att1: integer, att2: double, att3: string]
    output [attx: integer]

We execute:
   ./vgstub myFunction.vg

This will create myFunction.cc with its corresponding Record classes
and empty methods for the user to define.

Supported attribute types are integer, double, and string.

Requires Perl and M4 (present on most systems).
