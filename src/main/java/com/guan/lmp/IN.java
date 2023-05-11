package com.guan.lmp;

import com.guan.code.IHasIOFiles;
import com.guan.code.IOFiles;
import com.guan.code.UT;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;

import static com.guan.code.CS.SEP;

/**
 * @author liqa
 * <p> 一些预设的 lammps 输入文件格式，仅作为设置选项的作用来初始化 LmpIn，由于实例唯一不能包含任何可以修改的元素 </p>
 */
@SuppressWarnings("SameParameterValue")
public enum IN {
      INIT_MELT_NPT_Cu   ("init-melt-NPT-Cu"        , IOFiles.get("vInDataPath"    , "lmp/data/CuFCC108.lmpdat"            , SEP, "vOutRestartPath", "lmp/.temp/restart/melt-Cu108-init"))
    , RESTART_MELT_NPT_Cu("restart-melt-NPT-Cu"     , IOFiles.get("vInRestartPath" , "lmp/.temp/restart/melt-Cu108-init"   , SEP, "vOutRestartPath", "lmp/.temp/restart/melt-Cu108", 5))
    ;
    
    
    final @Nullable URL mLmpInFile;
    final IHasIOFiles mIOFiles;
    
    public @NotNull URL lmpInFile() {if (mLmpInFile == null) throw new RuntimeException(String.format("Lammps IN file of %s is missing", this.name())); return mLmpInFile;}
    
    IN (String aLmpInFileName) {
        mLmpInFile = UT.IO.getResource("lmp/in/"+aLmpInFileName);
        mIOFiles = IOFiles.get();
    }
    IN (String aLmpInFileName, IHasIOFiles aIOFiles) {
        mLmpInFile = UT.IO.getResource("lmp/in/"+aLmpInFileName);
        mIOFiles = aIOFiles;
    }
}
