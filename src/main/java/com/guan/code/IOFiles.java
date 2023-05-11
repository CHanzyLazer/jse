package com.guan.code;


import com.google.common.collect.ImmutableList;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author liqa
 * <p> IHasIOFiles 的默认实现 </p>
 */
public class IOFiles implements IHasIOFiles {
    private final Map<String, List<String>> mIFiles;
    private final Map<String, List<String>> mOFiles; // <FileKey, List<FilePath>>
    
    public IOFiles() {
        mIFiles = new HashMap<>();
        mOFiles = new HashMap<>();
    }
    
    @Override public List<String> getIFiles(String aIFileKey) {return mIFiles.get(aIFileKey);}
    @Override public List<String> getOFiles(String aOFileKey) {return mOFiles.get(aOFileKey);}
    @Override public Iterable<String> getIFiles() {return UT.Code.toIterable(mIFiles.values());}
    @Override public Iterable<String> getOFiles() {return UT.Code.toIterable(mOFiles.values());}
    
    
    @Override public IHasIOFiles setIFiles(String aIFileKey1, String aIFilePath1, Object... aElse) {scanAndAddFiles2Dest(mIFiles, UT.Code.merge(aIFileKey1, aIFilePath1, aElse)); return this;}
    @Override public IHasIOFiles setOFiles(String aOFileKey1, String aOFilePath1, Object... aElse) {scanAndAddFiles2Dest(mOFiles, UT.Code.merge(aOFileKey1, aOFilePath1, aElse)); return this;}
    
    
    
    private static void scanAndAddFiles2Dest(Map<String, List<String>> rDest, List<Object> aFiles) {
        int idx = 0;
        int tSize = aFiles.size();
        while (idx < tSize) {
            // 获取 key
            Object
            tNext = aFiles.get(idx); ++idx;
            if (!(tNext instanceof String)) continue;
            String tKey = (String)tNext;
            // 获取 path
            tNext = idx<tSize ? aFiles.get(idx) : null; ++idx;
            if (!(tNext instanceof String)) continue;
            String tPath = (String)tNext;
            // 通过检测后两个来获取可选的 start 和 end
            int tStart = 0, tEnd = -1;
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
            // 根据 end 来决定文件路径名称格式
            List<String> tPaths;
            if (tEnd <= 0) tPaths = Collections.singletonList(tPath);
            else {
                ImmutableList.Builder<String> tBuilder = new ImmutableList.Builder<>();
                for (int i = tStart; i < tEnd; ++i) tBuilder.add(tPath+"-"+i);
                tPaths = tBuilder.build();
            }
            rDest.put(tKey, tPaths);
        }
    }
}
