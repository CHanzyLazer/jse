package jtool.lmp;

import jtool.iofile.IIOFiles;
import jtool.iofile.IInFile;
import jtool.parallel.IHasAutoShutdown;
import jtool.system.ISystemExecutor;

/**
 * 通用的 lammps 运行器
 * @author liqa
 */
public interface ILmpExecutor extends IHasAutoShutdown {
    ISystemExecutor exec();
    int run(IInFile aInFile);
    int run(String aInFile, IIOFiles aIOFiles);
    
    /** IHasAutoShutdown stuffs */
    ILmpExecutor setDoNotShutdown(boolean aDoNotShutdown);
}
