package atom

import com.jtool.code.UT
import com.jtool.lmp.Lmpdat
import com.jtool.rareevent.atom.MultiTypeClusterSizeCalculator

final def dataDir       = 'lmp/.lhr-in/';
final def filterDir     = 'lmp/.lhr-out/';
final def cal = new MultiTypeClusterSizeCalculator().setQ6CutoffMul(2.20).setConnectThreshold(0.88).setSolidThreshold(13);

/** 读取 data 并过滤 */
for (fileName in UT.IO.list(dataDir)) {
    def data = Lmpdat.read(dataDir+fileName);
    def isSolid = data.getMPC().withCloseable {cal.getIsSolid_(it, data)}
    Lmpdat.fromAtomData(data.opt().filterIndices(isSolid)).write(filterDir+'filter-'+fileName);
    println("solid atom number of ${fileName}: ${isSolid.count()}");
}

