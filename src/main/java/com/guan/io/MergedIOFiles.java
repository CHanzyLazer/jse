package com.guan.io;


import com.guan.code.UT;

import java.util.*;

import static com.guan.code.CS.IFILE_KEY;
import static com.guan.code.CS.OFILE_KEY;

/**
 * @author liqa
 * <p> 已经合并的 IHasIOFiles 的实现，只有两个 key: {@code "<o>", "<i>"}，排除相同的文件 </p>
 */
public class MergedIOFiles implements IHasIOFiles {
    /** 提供额外的合并 IOFiles 的接口 */
    public void merge(IHasIOFiles aIOFiles) {
        for (String tIFile : aIOFiles.getIFiles()) mIFiles.add(tIFile);
        for (String tOFile : aIOFiles.getOFiles()) mOFiles.add(tOFile);
    }
    
    /** 全部遍历一次保证一定会值拷贝，String 也不会被修改因此不用考虑进一步值拷贝 */
    @Override public final MergedIOFiles copy() {
        MergedIOFiles rIOFiles = new MergedIOFiles();
        rIOFiles.mIFiles.addAll(mIFiles);
        rIOFiles.mOFiles.addAll(mOFiles);
        return rIOFiles;
    }
    
    
    private final Set<String> mIFiles;
    private final Set<String> mOFiles;
    
    public MergedIOFiles() {
        mIFiles = new LinkedHashSet<>();
        mOFiles = new LinkedHashSet<>();
    }
    
    
    @Override public Collection<String> getIFiles(String aIFileKey) {return aIFileKey.equals(IFILE_KEY) ? mIFiles : null;}
    @Override public Collection<String> getOFiles(String aOFileKey) {return aOFileKey.equals(OFILE_KEY) ? mOFiles : null;}
    @Override public Iterable<String> getIFiles() {return mIFiles;}
    @Override public Iterable<String> getOFiles() {return mOFiles;}
    @Override public Iterable<String> getIFileKeys() {return Collections.singletonList(IFILE_KEY);}
    @Override public Iterable<String> getOFileKeys() {return Collections.singletonList(OFILE_KEY);}
    
    
    
    @Override public final MergedIOFiles putIFiles(String aIFileKey1, String aIFilePath1                        ) {return putIFiles(aIFileKey1, aIFilePath1, new Object[0]                );}
    @Override public final MergedIOFiles putIFiles(String aIFileKey1, String aIFilePath1, int aMultiple1        ) {return putIFiles(aIFileKey1, aIFilePath1, new Object[] {aMultiple1    });}
    @Override public final MergedIOFiles putIFiles(String aIFileKey1, String aIFilePath1, int aStart1, int aEnd1) {return putIFiles(aIFileKey1, aIFilePath1, new Object[] {aStart1, aEnd1});}
    @Override public final MergedIOFiles putOFiles(String aOFileKey1, String aOFilePath1                        ) {return putOFiles(aOFileKey1, aOFilePath1, new Object[0]                );}
    @Override public final MergedIOFiles putOFiles(String aOFileKey1, String aOFilePath1, int aMultiple1        ) {return putOFiles(aOFileKey1, aOFilePath1, new Object[] {aMultiple1    });}
    @Override public final MergedIOFiles putOFiles(String aOFileKey1, String aOFilePath1, int aStart1, int aEnd1) {return putOFiles(aOFileKey1, aOFilePath1, new Object[] {aStart1, aEnd1});}
    
    
    @Override public MergedIOFiles putIFiles(String aIFileKey1, String aIFilePath1, Object... aElse) {scanAndAddFiles2Dest(mIFiles, UT.Code.merge(aIFileKey1, aIFilePath1, aElse)); return this;}
    @Override public MergedIOFiles putOFiles(String aOFileKey1, String aOFilePath1, Object... aElse) {scanAndAddFiles2Dest(mOFiles, UT.Code.merge(aOFileKey1, aOFilePath1, aElse)); return this;}
    
    
    private void scanAndAddFiles2Dest(Set<String> rDest, List<Object> aFiles) {
        int idx = 0;
        int tSize = aFiles.size();
        while (idx < tSize) {
            // 获取 key
            Object
            tNext = aFiles.get(idx); ++idx;
            if (!(tNext instanceof String)) continue;
            // 获取 path
            tNext = idx<tSize ? aFiles.get(idx) : null; ++idx;
            if (!(tNext instanceof String)) continue;
            String tPath = (String)tNext;
            // 通过检测后两个来获取可选的 start 和 end
            Integer tStart = null, tEnd = null;
            tNext = idx<tSize ? aFiles.get(idx) : null;
            if (tNext instanceof Number) {
                tEnd = ((Number)tNext).intValue();
                ++idx;
                tNext = idx<tSize ? aFiles.get(idx) : null;
                if (tNext instanceof Number) {
                    tStart = tEnd;
                    tEnd = ((Number)tNext).intValue();
                    ++idx;
                }
            }
            // 这里直接添加即可
            if (tEnd == null) {
                rDest.add(tPath);
            } else if (tStart == null) {
                for (int i = 0; i < tEnd; ++i) rDest.add(tPath+"-"+i);
            } else {
                for (int i = tStart; i < tEnd; ++i) rDest.add(tPath+"-"+i);
            }
        }
    }
    
    
    @Deprecated @Override public final MergedIOFiles i(String aIFileKey1, String aIFilePath1, Object... aElse       ) {return putIFiles(aIFileKey1, aIFilePath1, aElse);}
    @Deprecated @Override public final MergedIOFiles i(String aIFileKey1, String aIFilePath1                        ) {return putIFiles(aIFileKey1, aIFilePath1                );}
    @Deprecated @Override public final MergedIOFiles i(String aIFileKey1, String aIFilePath1, int aMultiple1        ) {return putIFiles(aIFileKey1, aIFilePath1, aMultiple1    );}
    @Deprecated @Override public final MergedIOFiles i(String aIFileKey1, String aIFilePath1, int aStart1, int aEnd1) {return putIFiles(aIFileKey1, aIFilePath1, aStart1, aEnd1);}
    @Deprecated @Override public final MergedIOFiles o(String aOFileKey1, String aOFilePath1, Object... aElse       ) {return putOFiles(aOFileKey1, aOFilePath1, aElse);}
    @Deprecated @Override public final MergedIOFiles o(String aOFileKey1, String aOFilePath1                        ) {return putOFiles(aOFileKey1, aOFilePath1                );}
    @Deprecated @Override public final MergedIOFiles o(String aOFileKey1, String aOFilePath1, int aMultiple1        ) {return putOFiles(aOFileKey1, aOFilePath1, aMultiple1    );}
    @Deprecated @Override public final MergedIOFiles o(String aOFileKey1, String aOFilePath1, int aStart1, int aEnd1) {return putOFiles(aOFileKey1, aOFilePath1, aStart1, aEnd1);}
}
