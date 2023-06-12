package test

import com.jtool.math.matrix.Matrices

def mat = Matrices.from(5, {col, row -> (col*10 + row) as double;});

println(mat);
println(mat.row(1));
println(mat.row(1).opt().refReverse());
println(mat.opt().refT().row(1));
println(mat.opt().refT().row(1).opt().refReverse());

