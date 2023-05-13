package com.guan.io;


import com.google.common.collect.ImmutableMap;
import com.guan.code.UT;
import com.guan.lmp.LmpIn;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author liqa
 * <p> 提供一些常用的输入文件的获取 </p>
 */
public class InFiles {
    private static abstract class ImmutableInFile extends AbstractMap<String, Object> implements IInFile {
        private final static Map<String, Object> ZL_SETTING = ImmutableMap.of();
        private final IHasIOFiles mIOFiles;
        public ImmutableInFile() {mIOFiles = new IOFiles();}
        
        /** IOFile stuffs */
        @Override public final List<String> getIFiles(String aIFileKey) {return mIOFiles.getIFiles(aIFileKey);}
        @Override public final List<String> getOFiles(String aOFileKey) {return mIOFiles.getOFiles(aOFileKey);}
        @Override public final Iterable<String> getIFiles() {return mIOFiles.getIFiles();}
        @Override public final Iterable<String> getOFiles() {return mIOFiles.getOFiles();}
        @Override public final IHasIOFiles putIFiles(String aIFileKey1, String aIFilePath1, Object... aElse) {mIOFiles.putIFiles(aIFileKey1, aIFilePath1, aElse); return this;}
        @Override public final IHasIOFiles putOFiles(String aOFileKey1, String aOFilePath1, Object... aElse) {mIOFiles.putOFiles(aOFileKey1, aOFilePath1, aElse); return this;}
        
        @Override public IHasIOFiles setIFile(String aIFileKey, String aIFilePath                      ) {mIOFiles.setIFile(aIFileKey, aIFilePath); return this;}
        @Override public IHasIOFiles setIFile(String aIFileKey, String aIFilePath, int aStart, int aEnd) {mIOFiles.setIFile(aIFileKey, aIFilePath, aStart, aEnd); return this;}
        @Override public IHasIOFiles setIFile(String aIFileKey,                    int aStart, int aEnd) {mIOFiles.setIFile(aIFileKey, aStart, aEnd); return this;}
        @Override public IHasIOFiles setIFile(String aIFileKeySetToSinglePath                          ) {mIOFiles.setIFile(aIFileKeySetToSinglePath); return this;}
        @Override public IHasIOFiles setOFile(String aOFileKey, String aOFilePath                      ) {mIOFiles.setOFile(aOFileKey, aOFilePath); return this;}
        @Override public IHasIOFiles setOFile(String aOFileKey, String aOFilePath, int aStart, int aEnd) {mIOFiles.setOFile(aOFileKey, aOFilePath, aStart, aEnd); return this;}
        @Override public IHasIOFiles setOFile(String aOFileKey,                    int aStart, int aEnd) {mIOFiles.setOFile(aOFileKey, aStart, aEnd); return this;}
        @Override public IHasIOFiles setOFile(String aOFileKeySetToSinglePath                          ) {mIOFiles.setOFile(aOFileKeySetToSinglePath); return this;}
        
        /** Map stuffs */
        @NotNull @Override public final Set<Entry<String, Object>> entrySet() {return ZL_SETTING.entrySet();}
        
        /** IInFile stuffs */
        public final void write(String aPath) throws IOException {
            write_(aPath);
            putIFiles("<self>", aPath);
        }
        
        /** stuff to override */
        protected abstract void write_(String aPath) throws IOException;
    }
    
    
    public static IInFile lmp(String aLmpInFilePath) {return LmpIn.custom(aLmpInFilePath);}
    public static IInFile json(final String aJsonFilePath) {return new AbstractInFileJson() {@Override protected Reader getInFileReader() throws IOException {return UT.IO.toReader(aJsonFilePath);}};}
    public static IInFile immutable(final String aInFilePath) {return new ImmutableInFile() {@Override public void write_(String aPath) throws IOException {UT.IO.copy(aInFilePath, aPath);}};}
    
    /** 默认行为 */
    @Deprecated public static IInFile get(String aInFilePath) {return immutable(aInFilePath);}
}
