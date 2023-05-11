package com.guan.code;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 抽象的包含输入输出文件的类，目前用于 in 文件类型的创建。
 * 构造函数的输入是序列化的，方便使用，格式为：
 * <p>
 * InFileKey1, InFilePath1, [start1], [end1], InFileKey2, InFilePath2, ..., SEPARATOR, OutFileKey1, OutFilePath1, [start1], [end1], OutFileKey2, OutFilePath2, ...
 * <p>
 * 提供 [start], [end] 则认为 InFilePath 有多个，名称为 ${InFilePath}-${i}, i 会从 start 依次增加到 end。
 * 注意这里由于是 java 书写的，约定默认 start 为 0，且 end 是不包含的，和其他的使用到 start 和 end 的操作保持一致
 * @author liqa
 */
public class IOFiles implements IHasIOFiles {
    private final Map<String, List<String>> mInputFiles;
    private final Map<String, List<String>> mOutputFiles; // <FileKey, List<FilePath>>
    
    public List<String> inputFiles(String aInFileKey) {return mInputFiles.get(aInFileKey);}
    public List<String> outputFiles(String aOutFileKey) {return mOutputFiles.get(aOutFileKey);}
    public Iterable<String> inputFiles() {return UT.Code.toIterable(mInputFiles.values());}
    public Iterable<String> outputFiles() {return UT.Code.toIterable(mOutputFiles.values());}
    
    public static IOFiles get() {return new IOFiles();}
    public static IOFiles get(Object... aFiles) {return new IOFiles(aFiles);}
    
    public static IOFiles get(String aInFileKey1, String aInFilePath1                                                                                                                         ) {return new IOFiles(aInFileKey1, aInFilePath1                                                                          );}
    public static IOFiles get(String aInFileKey1, String aInFilePath1, int aMultiple1                                                                                                         ) {return new IOFiles(aInFileKey1, aInFilePath1, aMultiple1                                                              );}
    public static IOFiles get(String aInFileKey1, String aInFilePath1, int aStart1, int aEnd1                                                                                                 ) {return new IOFiles(aInFileKey1, aInFilePath1, aStart1, aEnd1                                                          );}
    public static IOFiles get(                                                                 @Nullable Object aSeparator2, String aOutFileKey1, String aOutFilePath1                        ) {return new IOFiles(                                           aSeparator2, aOutFileKey1, aOutFilePath1                );}
    public static IOFiles get(                                                                 @Nullable Object aSeparator2, String aOutFileKey1, String aOutFilePath1, int aMultiple2        ) {return new IOFiles(                                           aSeparator2, aOutFileKey1, aOutFilePath1, aMultiple2    );}
    public static IOFiles get(                                                                 @Nullable Object aSeparator2, String aOutFileKey1, String aOutFilePath1, int aStart2, int aEnd2) {return new IOFiles(                                           aSeparator2, aOutFileKey1, aOutFilePath1, aStart2, aEnd2);}
    public static IOFiles get(String aInFileKey1, String aInFilePath1,                         @Nullable Object aSeparator2, String aOutFileKey1, String aOutFilePath1                        ) {return new IOFiles(aInFileKey1, aInFilePath1,                 aSeparator2, aOutFileKey1, aOutFilePath1                );}
    public static IOFiles get(String aInFileKey1, String aInFilePath1,                         @Nullable Object aSeparator2, String aOutFileKey1, String aOutFilePath1, int aMultiple2        ) {return new IOFiles(aInFileKey1, aInFilePath1,                 aSeparator2, aOutFileKey1, aOutFilePath1, aMultiple2    );}
    public static IOFiles get(String aInFileKey1, String aInFilePath1,                         @Nullable Object aSeparator2, String aOutFileKey1, String aOutFilePath1, int aStart2, int aEnd2) {return new IOFiles(aInFileKey1, aInFilePath1,                 aSeparator2, aOutFileKey1, aOutFilePath1, aStart2, aEnd2);}
    public static IOFiles get(String aInFileKey1, String aInFilePath1, int aMultiple1,         @Nullable Object aSeparator2, String aOutFileKey1, String aOutFilePath1                        ) {return new IOFiles(aInFileKey1, aInFilePath1, aMultiple1,     aSeparator2, aOutFileKey1, aOutFilePath1                );}
    public static IOFiles get(String aInFileKey1, String aInFilePath1, int aMultiple1,         @Nullable Object aSeparator2, String aOutFileKey1, String aOutFilePath1, int aMultiple2        ) {return new IOFiles(aInFileKey1, aInFilePath1, aMultiple1,     aSeparator2, aOutFileKey1, aOutFilePath1, aMultiple2    );}
    public static IOFiles get(String aInFileKey1, String aInFilePath1, int aMultiple1,         @Nullable Object aSeparator2, String aOutFileKey1, String aOutFilePath1, int aStart2, int aEnd2) {return new IOFiles(aInFileKey1, aInFilePath1, aMultiple1,     aSeparator2, aOutFileKey1, aOutFilePath1, aStart2, aEnd2);}
    public static IOFiles get(String aInFileKey1, String aInFilePath1, int aStart1, int aEnd1, @Nullable Object aSeparator2, String aOutFileKey1, String aOutFilePath1                        ) {return new IOFiles(aInFileKey1, aInFilePath1, aStart1, aEnd1, aSeparator2, aOutFileKey1, aOutFilePath1                );}
    public static IOFiles get(String aInFileKey1, String aInFilePath1, int aStart1, int aEnd1, @Nullable Object aSeparator2, String aOutFileKey1, String aOutFilePath1, int aMultiple2        ) {return new IOFiles(aInFileKey1, aInFilePath1, aStart1, aEnd1, aSeparator2, aOutFileKey1, aOutFilePath1, aMultiple2    );}
    public static IOFiles get(String aInFileKey1, String aInFilePath1, int aStart1, int aEnd1, @Nullable Object aSeparator2, String aOutFileKey1, String aOutFilePath1, int aStart2, int aEnd2) {return new IOFiles(aInFileKey1, aInFilePath1, aStart1, aEnd1, aSeparator2, aOutFileKey1, aOutFilePath1, aStart2, aEnd2);}
    
    
    private IOFiles() {
        mInputFiles = ImmutableMap.of();
        mOutputFiles = ImmutableMap.of();
    }
    private IOFiles(Object... aFiles) {
        // 组装输入部分
        Pair<Map<String, List<String>>, Integer>
        tPair = scanAndBuildFiles_(0, aFiles);
        mInputFiles = tPair.first;
        // 组装输出部分
        tPair = scanAndBuildFiles_(tPair.second, aFiles);
        mOutputFiles = tPair.first;
    }
    private static Pair<Map<String, List<String>>, Integer> scanAndBuildFiles_(int aIdx, Object... aFiles) {
        ImmutableMap.Builder<String, List<String>> fileBuilder = new ImmutableMap.Builder<>();
        while (aIdx < aFiles.length) {
            Object tKey = aFiles[aIdx]; ++aIdx;
            if (!(tKey instanceof String)) break;
            String tPath = (String)aFiles[aIdx]; ++aIdx;
            // 通过检测后两个来获取可选的 start 和 end
            int tStart = 0, tEnd = -1;
            Object tNext = aFiles[aIdx];
            if (tNext instanceof Number) {
                tEnd = ((Number)tNext).intValue();
                ++aIdx;
                tNext = aFiles[aIdx];
                if (tNext instanceof Number) {
                    tStart = tEnd;
                    tEnd = ((Number)tNext).intValue();
                    ++aIdx;
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
            fileBuilder.put((String)tKey, tPaths);
        }
        // 输出
        return new Pair<>(fileBuilder.build(), aIdx);
    }
}
