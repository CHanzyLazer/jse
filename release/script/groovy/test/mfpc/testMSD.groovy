package test.mfpc

import com.jtool.atom.Structures
import com.jtool.code.UT
import com.jtool.lmp.LPC
import com.jtool.math.MathEX
import com.jtool.math.table.Tables
import com.jtool.plot.Plotters
import com.jtool.system.SSH
import com.jtool.system.WSL

import static com.jtool.code.CS.*

/** 测试计算 MSD */

// 需要先运行一下 lammps 来创建 dump
final int Cu = 60, Zr = 40;
final int meltTemp          = Math.round(1600 + MathEX.Code.units(800, Cu+Zr, Zr, false));
final double cellSize       = 3.971 + Zr/(Cu+Zr) * 1.006;

final String FS1dataPath    = 'lmp/.temp/data-fs1';
final String FS2dataPath    = 'lmp/.temp/data-fs2';

final String FS1MSDPath     = 'lmp/.temp/msd-fs1.csv';
final String FS2MSDPath     = 'lmp/.temp/msd-fs2.csv';

final boolean genData       = false;
final boolean calMSD        = true;


if (genData) {
    String lmpExe   = '~/.local/bin/lmp';
    int lmpCores    = 12;
    
    UT.Timer.tic();
    try (def lpc = new LPC(new WSL(), "mpiexec -np ${lmpCores} ${lmpExe}", 'eam/fs', '* * lmp/.potential/Cu-Zr_2.eam.fs Cu Zr')) {
        lpc.runMelt(Structures.FCC(cellSize, 10).opt().mapTypeRandom(Cu, Zr), [MASS.Cu, MASS.Zr], FS1dataPath, meltTemp, 0.002, 500000);
        lpc.runMelt(FS1dataPath, FS1dataPath, 1000, 0.002, 500000 );
        lpc.runMelt(FS1dataPath, FS1dataPath, 800 , 0.002, 5000000);
    }
    try (def lpc = new LPC(new WSL(), "mpiexec -np ${lmpCores} ${lmpExe}", 'eam/fs', '* * lmp/.potential/Cu-Zr_4.eam.fs Cu Zr')) {
        lpc.runMelt(Structures.FCC(cellSize, 10).opt().mapTypeRandom(Cu, Zr), [MASS.Cu, MASS.Zr], FS2dataPath, meltTemp, 0.002, 500000);
        lpc.runMelt(FS2dataPath, FS2dataPath, 1000, 0.002, 500000 );
        lpc.runMelt(FS2dataPath, FS2dataPath, 850 , 0.002, 5000000);
    }
    UT.Timer.toc('genData');
}


if (calMSD) {
    String lmpExe   = '~/.local/bin/lmp';
    int lmpCores    = 12;
    
    UT.Timer.tic();
    try (def lpc = new LPC(new WSL(), "mpiexec -np ${lmpCores} ${lmpExe}", 'eam/fs', '* * lmp/.potential/Cu-Zr_2.eam.fs Cu Zr')) {
        def msd800 = lpc.calMSD(FS1dataPath, 800, true);
        
        def table = Tables.zeros(msd800.first().size());
        table['t'] = msd800.second();
        table['800'] = msd800.first();
        
        UT.IO.table2csv(table, FS1MSDPath);
    }
    
    try (def lpc = new LPC(new WSL(), "mpiexec -np ${lmpCores} ${lmpExe}", 'eam/fs', '* * lmp/.potential/Cu-Zr_4.eam.fs Cu Zr')) {
        def msd800 = lpc.calMSD(FS2dataPath, 800, true);
        def msd850 = lpc.calMSD(FS2dataPath, 850, true);
        def msd900 = lpc.calMSD(FS2dataPath, 900, true);
        
        def table = Tables.zeros(msd800.first().size());
        table['t'] = msd800.second();
        table['800'] = msd800.first();
        table['850'] = msd850.first();
        table['900'] = msd900.first();
        
        UT.IO.table2csv(table, FS2MSDPath);
    }
    UT.Timer.toc('calMSD');
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
