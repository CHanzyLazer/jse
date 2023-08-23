package test

import com.jtool.atom.Structures
import com.jtool.lmp.Lmpdat
import com.jtool.vasp.POSCAR


/** 测试创建的结构是否正确 */

Lmpdat.fromAtomData(Structures.FCC(3.61, 4, 5, 6)).write('.temp/fcc');
Lmpdat.fromAtomData(Structures.BCC(3.61, 4, 5, 6)).write('.temp/bcc');

Lmpdat.fromAtomData(Structures.FCC(3.61, 6).opt().randomUpdateTypeByWeight(3, 7)).write('.temp/alloy');

Lmpdat.fromAtomData(Structures.FCC(3.61, 6).opt().perturbG(0.2)).write('.temp/fccP');
Lmpdat.fromAtomData(Structures.BCC(3.61, 6).opt().perturbG(0.3)).write('.temp/bccP');

Lmpdat.fromAtomData(Structures.from(Lmpdat.read('lmp/data/data-glass'    ), 1, 2, 3)).write('.temp/glass-rep');
Lmpdat.fromAtomData(Structures.from(POSCAR.read('lmp/data/Zr7Cu10.poscar'), 5)).write('.temp/Zr7Cu10');

