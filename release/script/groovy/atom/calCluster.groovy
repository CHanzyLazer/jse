package atom

import com.jtool.atom.Atom
import com.jtool.atom.IAtom
import com.jtool.lmp.Dump
import com.jtool.rareevent.atom.ABOOPSolidChecker

import java.util.stream.Collectors



//final def dataDir       = 'lmp/.stableglass-in/';
//final def filterDir     = 'lmp/.stableglass-out/';
//final def checker = new ABOOPSolidChecker().setRNearestQMul(4.0).setConnectThreshold(0.950).setSolidThreshold(7);
//
///** 读取 data 并过滤 */
//for (fileName in UT.IO.list(dataDir)) {
//    def data = Lmpdat.read(dataDir+fileName);
//    def isSolid = data.getMPC().withCloseable {checker.checkSolid(it)}
//
//    println("solid atom number of ${fileName}: ${isSolid.count()}");
//    int i = 0;
//    Lmpdat.fromAtomData(data.opt().collect {IAtom atom -> isSolid[i++] ? new Atom(atom).setType(atom.type()+ 3) : atom}).write(filterDir+ 'filter-'+ fileName);
//}
final def filterDir = 'lmp/.stableglass-out/';

final def dump = Dump.read('lmp/.stableglass-in/dump');
final def checker = new ABOOPSolidChecker().setRNearestMul(2.2).setConnectThreshold(0.88).setSolidThreshold(13);

def filterDump = dump.parallelStream().map {subDump ->
    def isSolid = subDump.getMPC().withCloseable {checker.checkSolid(it)}
    int j = 0;
    subDump.opt().collect {IAtom atom -> isSolid[j++] ? new Atom(atom).setType(atom.type()+2) : atom};
}.collect(Collectors.toList());

Dump.fromAtomDataList(filterDump).write(filterDir+'filter-dump');

