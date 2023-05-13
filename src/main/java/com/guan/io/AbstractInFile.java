package com.guan.io;


import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;


/**
 * @author liqa
 * <p> 输入文件 IInFile 的默认实现，自身文件的 key 为 {@code "<self>"} </p>
 * <p> 由于 Map 需要的接口更多，因此继承 AbstractMap 来减少重复代码的数量 </p>
 * <p> 注意调用 write 后会永久修改内部的属性，因此不允许重复修改，并且调用后会锁死属性的修改 </p>
 */
public abstract class AbstractInFile extends AbstractMap<String, Object> implements IInFile {
    /** Wrapper of IOFile and Map */
    private final IHasIOFiles mIOFiles;
    private final Map<String, Object> mSettings;
    private volatile boolean mIsWritten = false;
    public AbstractInFile() {
        mIOFiles = new IOFiles();
        mSettings = new HashMap<>();
    }
    
    
    /** IOFile stuffs */
    @Override public final List<String> getIFiles(String aIFileKey) {return mIOFiles.getIFiles(aIFileKey);}
    @Override public final List<String> getOFiles(String aOFileKey) {return mIOFiles.getOFiles(aOFileKey);}
    @Override public final Iterable<String> getIFiles() {return mIOFiles.getIFiles();}
    @Override public final Iterable<String> getOFiles() {return mIOFiles.getOFiles();}
    @Override public final IHasIOFiles putIFiles(String aIFileKey1, String aIFilePath1, Object... aElse) {if (mIsWritten) throw new RuntimeException("Can NOT change an written InFile"); mIOFiles.putIFiles(aIFileKey1, aIFilePath1, aElse); return this;}
    @Override public final IHasIOFiles putOFiles(String aOFileKey1, String aOFilePath1, Object... aElse) {if (mIsWritten) throw new RuntimeException("Can NOT change an written InFile"); mIOFiles.putOFiles(aOFileKey1, aOFilePath1, aElse); return this;}
    
    @Override public IHasIOFiles setIFile(String aIFileKey, String aIFilePath                      ) {if (mIsWritten) throw new RuntimeException("Can NOT change an written InFile"); mIOFiles.setIFile(aIFileKey, aIFilePath); return this;}
    @Override public IHasIOFiles setIFile(String aIFileKey, String aIFilePath, int aStart, int aEnd) {if (mIsWritten) throw new RuntimeException("Can NOT change an written InFile"); mIOFiles.setIFile(aIFileKey, aIFilePath, aStart, aEnd); return this;}
    @Override public IHasIOFiles setIFile(String aIFileKey,                    int aStart, int aEnd) {if (mIsWritten) throw new RuntimeException("Can NOT change an written InFile"); mIOFiles.setIFile(aIFileKey, aStart, aEnd); return this;}
    @Override public IHasIOFiles setIFile(String aIFileKeySetToSinglePath                          ) {if (mIsWritten) throw new RuntimeException("Can NOT change an written InFile"); mIOFiles.setIFile(aIFileKeySetToSinglePath); return this;}
    @Override public IHasIOFiles setOFile(String aOFileKey, String aOFilePath                      ) {if (mIsWritten) throw new RuntimeException("Can NOT change an written InFile"); mIOFiles.setOFile(aOFileKey, aOFilePath); return this;}
    @Override public IHasIOFiles setOFile(String aOFileKey, String aOFilePath, int aStart, int aEnd) {if (mIsWritten) throw new RuntimeException("Can NOT change an written InFile"); mIOFiles.setOFile(aOFileKey, aOFilePath, aStart, aEnd); return this;}
    @Override public IHasIOFiles setOFile(String aOFileKey,                    int aStart, int aEnd) {if (mIsWritten) throw new RuntimeException("Can NOT change an written InFile"); mIOFiles.setOFile(aOFileKey, aStart, aEnd); return this;}
    @Override public IHasIOFiles setOFile(String aOFileKeySetToSinglePath                          ) {if (mIsWritten) throw new RuntimeException("Can NOT change an written InFile"); mIOFiles.setOFile(aOFileKeySetToSinglePath); return this;}
    
    
    /** Map stuffs */
    @NotNull @Override public final Set<Entry<String, Object>> entrySet() {return mSettings.entrySet();}
    @Override public final Object put(String key, Object value) {if (mIsWritten) throw new RuntimeException("Can NOT change an written InFile"); return mSettings.put(key, value);}
    @Override public final Object get(Object key) {return mSettings.get(key);}
    @Override public final Object remove(Object key) {if (mIsWritten) throw new RuntimeException("Can NOT change an written InFile"); return mSettings.remove(key);}
    @Override public final int size() {return mSettings.size();}
    @Override public final void clear() {if (mIsWritten) throw new RuntimeException("Can NOT change an written InFile"); mSettings.clear();}
    
    /** IInFile stuffs */
    @Override public void hookIOFilesMultiple(String aHookKey, String aIOFilesKey);
    @Override public void hookIOFilesStart(String aHookKey, String aIOFilesKey);
    @Override public void hookIOFileEnd(String aHookKey, String aIOFilesKey);
    
    
    @Override public final void write(String aPath) throws IOException {
        if (mIsWritten) throw new RuntimeException("This InFile has been Written, it cannot be written twice");
        mIsWritten = true;
        write_(aPath);
        // 在写入时根据 setting 来顺便修改相同 key 的 IOFiles，约定写入后不允许再次修改让实现起来更加方便
        // 首先设置自身的文件作为输入文件
        putIFiles("<self>", aPath);
        // 遍历设置
        for (Map.Entry<String, Object> tEntry : entrySet()) if (tEntry.getValue() instanceof String) {
            setIFile(tEntry.getKey(), (String) tEntry.getValue());
            setOFile(tEntry.getKey(), (String) tEntry.getValue());
        }
    }
    
    /** stuff to override */
    protected abstract void write_(String aPath) throws IOException;
}
