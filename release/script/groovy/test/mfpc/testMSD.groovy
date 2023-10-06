package test.mfpc

import com.jtool.atom.Structures
import com.jtool.code.UT
import com.jtool.lmp.LPC
import com.jtool.math.MathEX
import com.jtool.math.table.Tables
import com.jtool.plot.Plotters
import com.jtool.system.WSL

import static com.jtool.code.CS.*

/** 测试计算 MSD */

// 需要先运行一下 lammps 来创建 dump
final String lmpExe         = '~/.local/bin/lmp';
final int lmpCores          = 12;

final int Cu = 60, Zr = 40;
final int meltTemp          = Math.round(1600 + MathEX.Code.units(800, Cu+Zr, Zr, false));
final double cellSize       = 3.971 + Zr/(Cu+Zr) * 1.006;

final String FS1MSDPath     = 'lmp/.temp/msd-fs1.csv';
final String FS2MSDPath     = 'lmp/.temp/msd-fs2.csv';
final boolean genMSD        = true;

if (genMSD) {
    UT.Timer.tic();
    try (def lpc = new LPC(new WSL(), "mpiexec -np ${lmpCores} ${lmpExe}", 'eam/fs', '* * lmp/.potential/Cu-Zr_2.eam.fs Cu Zr')) {
        def inDataPath = 'lmp/.temp/data-fs1';
        lpc.runMelt(Structures.FCC(cellSize, 10).opt().mapTypeRandom(Cu, Zr), [MASS.Cu, MASS.Zr], inDataPath, meltTemp);
        lpc.runMelt(inDataPath, inDataPath, 800);
        def msd800 = lpc.calMSD(inDataPath, 800, true);
        
        def table = Tables.from(msd800.first().size(), 2, {row, col ->
            if (col == 0) return msd800.second()[row];
            if (col == 1) return msd800.first()[row];
        }, 't', '800');
        
        UT.IO.table2csv(table, FS1MSDPath);
    }
    
    try (def lpc = new LPC(new WSL(), "mpiexec -np ${lmpCores} ${lmpExe}", 'eam/fs', '* * lmp/.potential/Cu-Zr_4.eam.fs Cu Zr')) {
        def inDataPath = 'lmp/.temp/data-fs2';
        lpc.runMelt(Structures.FCC(cellSize, 10).opt().mapTypeRandom(Cu, Zr), [MASS.Cu, MASS.Zr], inDataPath, meltTemp);
        lpc.runMelt(inDataPath, inDataPath, 850);
        def msd800 = lpc.calMSD(inDataPath, 800, true);
        def msd850 = lpc.calMSD(inDataPath, 850, true);
        def msd900 = lpc.calMSD(inDataPath, 900, true);
        
        def table = Tables.from(msd800.first().size(), 4, {row, col ->
            if (col == 0) return msd800.second()[row];
            if (col == 1) return msd800.first()[row];
            if (col == 2) return msd850.first()[row];
            if (col == 3) return msd900.first()[row];
        }, 't', '800', '850', '900');
        
        UT.IO.table2csv(table, FS2MSDPath);
    }
    UT.Timer.toc();
}


// 读取数据
def msdFS1 = UT.IO.csv2table(FS1MSDPath);
def msdFS2 = UT.IO.csv2table(FS2MSDPath);

// 绘制
def plt = Plotters.get();
plt.loglog(msdFS1['t'], msdFS1['800'], 'FS1, 800K').marker('s');
plt.loglog(msdFS2['t'], msdFS2['800'], 'FS2, 800K').marker('o');
plt.loglog(msdFS2['t'], msdFS2['850'], 'FS2, 850K').marker('^');
plt.loglog(msdFS2['t'], msdFS2['900'], 'FS2, 900K').marker('d');
plt.xLabel('time[ps]').yLabel('msd');
plt.show();
