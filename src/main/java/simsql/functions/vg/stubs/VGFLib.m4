divert(-1)

# foreach(x, (item_1, item_2, ..., item_n), stmt)
define(`foreach', `pushdef(`$1', `')_foreach(`$1', `$2', `$3')popdef(`$1')')
define(`_arg1', `$1')
define(`_foreach', 
		   `ifelse(`$2', `()', ,
		   		 `define(`$1', _arg1$2)$3`'_foreach(`$1', (shift$2), `$3')')')

# _arg1 for two
define(`_arg2', `$1$2')

# just arg2
define(`_arg2x', `$2')

# returns length of list [$1: (list, of, elements)]
define(`_length', `pushdef(`_count', 0)dnl
foreach(`__x', $1, `define(`_count', incr(_count))')dnl
_count`'popdef(`_count')')

# returns the corresponding C type [$1: type string]
define(`_M4_VG_TYPESTRDEC', `ifelse($1, `string', `char', $1, `double', `double', $1, `integer', `long', `')')

# returns the corresponding MC VGType [$1: type string]
define(`_M4_VG_TYPESTR', `ifelse($1, `string', `STRING', $1, `double', `DOUBLE', $1, `integer', `INT', `')')

# returns an individual declaration [$1: type string, $2: var name]
define(`_M4_VG_DECLAREA', `dnl
   _M4_VG_TYPESTRDEC($2) *$1;
')

# returns declarations from a list [$1: list ((name1,type1),(nameN,typeN))]
define(`_M4_VG_DECLARE', `foreach(`__x', $1, `_arg2(`_M4_VG_DECLAREA', __x)')')

# returns an individual comparison [$1: input array name, $2: input array index, $3: attribute type str]
define(`_M4_VG_COMPAREA', `$1[$2] == _M4_VG_TYPESTR($3)')

# returns comparisons for types [$1: input array name, $2: size var name, $3: list of (type,name)]
define(`_M4_VG_COMPARE', `
		return($2 == _length($3) &&
pushdef(`__i', 0)dnl
foreach(`__x', $3, `dnl
		  _M4_VG_COMPAREA($1, __i, _arg2(`_arg2x', __x)) && 
define(`__i', incr(__i))')dnl
		  true
		);')

# returns a return statement with the list of types
define(`_M4_VG_RETURN', `
			return (VGSchema){_length($1), {dnl
pushdef(`__i', 1)dnl
foreach(`__x', $1, `dnl
_M4_VG_TYPESTR(_arg2(`_arg2x', __x))`'dnl
ifelse(__i, _length($1), `}', `, ')dnl
define(`__i', incr(__i))'), dnl
dnl
{dnl
pushdef(`__i', 1)dnl
foreach(`__x', $1, `"dnl
_arg2(`_arg1', __x)"`'dnl
ifelse(__i, _length($1), `}', `, ')dnl
define(`__i', incr(__i))'), dnl
dnl
{dnl
pushdef(`__i', 1)dnl
foreach(`__x', $1, `dnl
0dnl
ifelse(__i, _length($1), `}', `, ')dnl
define(`__i', incr(__i))')dnl
};')

divert`'dnl
