package com.guan.io;

import java.util.List;

public interface IHasIOFiles {
    /** 获取输入输出的文件路径 */
    default String getIFile(String aIFileKey) {return getIFiles(aIFileKey).get(0);}
    default String getOFile(String aOFileKey) {return getOFiles(aOFileKey).get(0);}
    default String getIFile(String aIFileKey, int aIndex) {return getIFiles(aIFileKey).get(aIndex);}
    default String getOFile(String aOFileKey, int aIndex) {return getOFiles(aOFileKey).get(aIndex);}
    List<String> getIFiles(String aIFileKey);
    List<String> getOFiles(String aOFileKey);
    Iterable<String> getIFiles();
    Iterable<String> getOFiles();
    
    
    /**
     * 用于设置已有的 IO 文件的接口，和 put 不同的是：
     * <p> 1. 如果 key 没有则不会添加 </p>
     * <p> 2. 如果设置只指定了路径而没有指定多少个，则会修改路径名称而不会覆盖个数的设定 </p>
     * <p> 3. 支持只给定数量的设定，从而只会调整数量 </p>
     * <p> 4. 因此，如果什么都不指定，则会设置对应的路径变成单一路径 </p>
     * @author liqa
     */
    IHasIOFiles setIFile(String aIFileKey, String aIFilePath                      );
    IHasIOFiles setIFile(String aIFileKey, String aIFilePath, int aStart, int aEnd);
    IHasIOFiles setIFile(String aIFileKey,                    int aStart, int aEnd);
    IHasIOFiles setIFile(String aIFileKeySetToSinglePath                          );
    IHasIOFiles setOFile(String aOFileKey, String aOFilePath                      );
    IHasIOFiles setOFile(String aOFileKey, String aOFilePath, int aStart, int aEnd);
    IHasIOFiles setOFile(String aOFileKey,                    int aStart, int aEnd);
    IHasIOFiles setOFile(String aOFileKeySetToSinglePath                          );
    default IHasIOFiles setIFile(String aIFileKey, String aIFilePath, int aMultiple) {return setIFile(aIFileKey, aIFilePath, 0, aMultiple);}
    default IHasIOFiles setIFile(String aIFileKey,                    int aMultiple) {return setIFile(aIFileKey, 0, aMultiple);}
    default IHasIOFiles setOFile(String aOFileKey, String aOFilePath, int aMultiple) {return setOFile(aOFileKey, aOFilePath, 0, aMultiple);}
    default IHasIOFiles setOFile(String aOFileKey,                    int aMultiple) {return setOFile(aOFileKey, 0, aMultiple);}
    
    
    /**
     * 添加输入输出的文件路径，返回自身方便链式调用，
     * 输入参数是序列化的，方便使用，格式为：
     * <p>
     * FileKey1, FilePath1, [start1], [end1], FileKey2, FilePath2, [start2], [end2], ...
     * <p>
     * 提供 [start], [end] 则认为 FilePath 有多个，名称为 ${InFilePath}-${i}, i 会从 start 依次增加到 end。
     * 注意这里由于是 java，约定默认 start 为 0，且 end 是不包含的，和其他的使用到 start 和 end 的操作保持一致
     * @author liqa
     */
            IHasIOFiles putIFiles(String aIFileKey1, String aIFilePath1, Object... aElse       );
    default IHasIOFiles putIFiles(String aIFileKey1, String aIFilePath1                        ) {return putIFiles(aIFileKey1, aIFilePath1, new Object[0]                );}
    default IHasIOFiles putIFiles(String aIFileKey1, String aIFilePath1, int aMultiple1        ) {return putIFiles(aIFileKey1, aIFilePath1, new Object[] {aMultiple1    });}
    default IHasIOFiles putIFiles(String aIFileKey1, String aIFilePath1, int aStart1, int aEnd1) {return putIFiles(aIFileKey1, aIFilePath1, new Object[] {aStart1, aEnd1});}
            IHasIOFiles putOFiles(String aOFileKey1, String aOFilePath1, Object... aElse       );
    default IHasIOFiles putOFiles(String aOFileKey1, String aOFilePath1                        ) {return putOFiles(aOFileKey1, aOFilePath1, new Object[0]                );}
    default IHasIOFiles putOFiles(String aOFileKey1, String aOFilePath1, int aMultiple1        ) {return putOFiles(aOFileKey1, aOFilePath1, new Object[] {aMultiple1    });}
    default IHasIOFiles putOFiles(String aOFileKey1, String aOFilePath1, int aStart1, int aEnd1) {return putOFiles(aOFileKey1, aOFilePath1, new Object[] {aStart1, aEnd1});}
    
    
    @Deprecated default String i(String aIFileKey, int aIndex) {return getIFile(aIFileKey, aIndex);}
    @Deprecated default String o(String aOFileKey, int aIndex) {return getOFile(aOFileKey, aIndex);}
    @Deprecated default String i(String aIFileKey) {return getIFile(aIFileKey);}
    @Deprecated default String o(String aOFileKey) {return getOFile(aOFileKey);}
    @Deprecated default Iterable<String> i() {return getIFiles();}
    @Deprecated default Iterable<String> o() {return getOFiles();}
    
    @Deprecated default IHasIOFiles i(String aIFileKey1, String aIFilePath1, Object... aElse       ) {return putIFiles(aIFileKey1, aIFilePath1, aElse);}
    @Deprecated default IHasIOFiles i(String aIFileKey1, String aIFilePath1                        ) {return putIFiles(aIFileKey1, aIFilePath1                );}
    @Deprecated default IHasIOFiles i(String aIFileKey1, String aIFilePath1, int aMultiple1        ) {return putIFiles(aIFileKey1, aIFilePath1, aMultiple1    );}
    @Deprecated default IHasIOFiles i(String aIFileKey1, String aIFilePath1, int aStart1, int aEnd1) {return putIFiles(aIFileKey1, aIFilePath1, aStart1, aEnd1);}
    @Deprecated default IHasIOFiles o(String aOFileKey1, String aOFilePath1, Object... aElse       ) {return putOFiles(aOFileKey1, aOFilePath1, aElse);}
    @Deprecated default IHasIOFiles o(String aOFileKey1, String aOFilePath1                        ) {return putOFiles(aOFileKey1, aOFilePath1                );}
    @Deprecated default IHasIOFiles o(String aOFileKey1, String aOFilePath1, int aMultiple1        ) {return putOFiles(aOFileKey1, aOFilePath1, aMultiple1    );}
    @Deprecated default IHasIOFiles o(String aOFileKey1, String aOFilePath1, int aStart1, int aEnd1) {return putOFiles(aOFileKey1, aOFilePath1, aStart1, aEnd1);}
}
