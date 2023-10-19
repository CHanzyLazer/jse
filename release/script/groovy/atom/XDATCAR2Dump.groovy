package atom

import jtool.lmp.Dump
import jtool.vasp.XDATCAR


def data = XDATCAR.read('lmp/.lll-in/XDATCAR');

println(data.atomNum('Ce'));

data.write('lmp/.lll-out/XDATCAR');
Dump.fromAtomDataList(data).write('lmp/.lll-out/dump');

