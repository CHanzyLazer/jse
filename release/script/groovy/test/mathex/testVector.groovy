package test.mathex

import jtool.math.vector.ComplexVector

import static jtool.code.CS.*


def vec = ComplexVector.zeros(5);
println(vec);

vec.fill {int i -> I*i + 10*i}
println(vec);

println(-vec);
