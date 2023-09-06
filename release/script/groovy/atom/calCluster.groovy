package atom

import com.jtool.atom.IAtomData
import com.jtool.code.collection.ArrayLists
import com.jtool.lmp.Dump
import com.jtool.rareevent.atom.ABOOPSolidChecker
import com.jtool.rareevent.atom.BOOPSolidChecker
import com.jtool.rareevent.atom.MainTypeClusterSizeCalculator

import static com.jtool.code.UT.Par.*



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
final def calculator = new MainTypeClusterSizeCalculator(
    new ABOOPSolidChecker().setRNearestMul(1.4).setConnectThreshold(0.90).setSolidThreshold(7),
    new ABOOPSolidChecker().setRNearestMul(1.8).setConnectThreshold(0.86).setSolidThreshold(13),
    1
);

List<IAtomData> filterDump = ArrayLists.nulls(dump.size());
parfor(dump.size()) {int i ->
    def subDump = dump[i];
    def isSolid = subDump.getMPC().withCloseable {calculator.getIsSolid_(it, subDump)}
    int j = 0;
    filterDump[i] = subDump.opt().mapType {def atom -> isSolid[j++] ? atom.type()+2 : atom.type()};
}

Dump.fromAtomDataList(filterDump).write(filterDir+'filter-aboop-dump');

