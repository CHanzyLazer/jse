package jse.code;

import groovy.json.JsonBuilder;
import groovy.json.JsonSlurper;
import groovy.yaml.YamlBuilder;
import groovy.yaml.YamlSlurper;
import jse.cache.ByteArrayCache;
import jse.code.collection.AbstractCollections;
import jse.code.collection.NewCollections;
import jse.code.functional.IUnaryFullOperator;
import jse.code.io.CharScanner;
import jse.code.io.UnicodeReader;
import jse.math.function.IFunc1;
import jse.math.matrix.IMatrix;
import jse.math.matrix.RowMatrix;
import jse.math.table.ITable;
import jse.math.table.Table;
import jse.math.table.Tables;
import jse.math.vector.IVector;
import jse.math.vector.Vector;
import jse.math.vector.Vectors;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.codehaus.groovy.runtime.IOGroovyMethods;
import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.codehaus.groovy.util.CharSequenceReader;
import org.jetbrains.annotations.*;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static jse.code.CS.*;
import static jse.code.OS.USER_HOME;
import static jse.code.OS.WORKING_DIR_PATH;


public class IO {
    static {jse.code.OS.InitHelper.init();}
    private final static int BUFFER_SIZE = 8192;
    
    public static class Text {
        public static Reader toReader_(CharSequence aStr) {return new CharSequenceReader(aStr);}
        public static BufferedReader toReader(CharSequence aStr) {return new BufferedReader(toReader_(aStr));}
        public static void eachLine(final CharSequence aStr, final Consumer<String> aCon) throws IOException {
            try (BufferedReader tReader = toReader(aStr)) {
                String tLine;
                while ((tLine = tReader.readLine()) != null) aCon.accept(tLine);
            }
        }
        public static void eachLine(final CharSequence aStr, final BiConsumer<String, Integer> aCon) throws IOException {
            try (BufferedReader tReader = toReader(aStr)) {
                int tLineNum = 0;
                String tLine;
                while ((tLine = tReader.readLine()) != null) {
                    aCon.accept(tLine, tLineNum);
                    ++tLineNum;
                }
            }
        }
        
        public static String[] toArray(Collection<? extends CharSequence> aLines) {
            String[] rArray = new String[aLines.size()];
            int i = 0;
            for (CharSequence tStr : aLines) {
                rArray[i] = UT.Code.toString(tStr);
                ++i;
            }
            return rArray;
        }
        
        /**
         * Convert a prob value to percent String
         * @author liqa
         */
        public static String percent(double aProb) {
            return String.format("%.2f", aProb*100) + "%";
        }
        
        /**
         * 重复给定 char，照搬 {@code me.tongfei.progressbar.Util} 中的方法
         * @author Tongfei Chen, liqa
         */
        public static String repeat(char aChar, int aNum) {
            if (aNum <= 0) return "";
            char[] tChars = new char[aNum];
            Arrays.fill(tChars, aChar);
            return new String(tChars);
        }
        
        /**
         * 重复给定 String，使用类似 Groovy 中对于 String 的乘法的方法
         * @author liqa
         */
        public static String repeat(CharSequence aStr, int aNum) {
            if (aNum <= 0) return "";
            StringBuilder rStr = new StringBuilder(aStr);
            for (int i = 1; i < aNum; ++i) {
                rStr.append(aStr);
            }
            return rStr.toString();
        }
        
        
        private static boolean charIsDigitDecimal_(int aChar) {
            switch(aChar) {
            case '-': case '+': case '.': {
                return true;
            }
            default: {
                return CharScanner.isDigit(aChar);
            }}
        }
        /**
         * 将单个字符串转为 Number 值，要求前后不能含有任何空格，如果转换失败则返回 {@code null}；
         * 自动检测整数类型和小数类型，现在对于小数会返回 {@link Double} 而不是
         * {@link java.math.BigDecimal}
         * @author liqa
         */
        public static @Nullable Number str2number(String aStr) {
            // 先直接转 char[]，适配 groovy-json 的 CharScanner
            char[] tChar = aStr.toCharArray();
            // 先判断开头，这样可以避免抛出错误带来的性能损失
            if (!charIsDigitDecimal_(tChar[0])) return null;
            // 但一般还是使用 try，这样避免意外的情况
            try {return CharScanner.parseNumber(tChar, 0, tChar.length);}
            catch (Exception ignored) {}
            return null;
        }
        
        public static int findNoBlankIndex(String aStr, int aStart) {
            final int tLen = aStr.length();
            int c;
            for (; aStart < tLen; ++aStart) {
                c = aStr.charAt(aStart);
                if (c > 32) {
                    return aStart;
                }
            }
            return -1;
        }
        public static int findBlankIndex(String aStr, int aStart) {
            final int tLen = aStr.length();
            int c;
            for (; aStart < tLen; ++aStart) {
                c = aStr.charAt(aStart);
                if (c <= 32) {
                    return aStart;
                }
            }
            return -1;
        }
        
        /**
         * 针对 lammps 等软件输出文件中存在的使用空格分割的数据专门优化的读取操作，
         * 使用 groovy-json 中现成的 parseDouble 等方法，总体比直接 split 并用 java 的 parseDouble 快一倍以上；
         * <p>
         * 现在兼容考虑逗号分割的情况，并同时运用在 csv 的读取上
         * <p>
         * 现在读取失败会存为 {@link Double#NaN} 而不是抛出错误
         * @author liqa
         */
        public static Vector str2data(String aStr, int aLength) {
            // 不足的数据现在默认为 NaN
            Vector rData = Vectors.NaN(aLength);
            // 先直接转 char[]，适配 groovy-json 的 CharScanner
            char[] tChar = aStr.toCharArray();
            // 直接遍历忽略空格，获取开始和末尾，然后 parseDouble
            int tFrom = CharScanner.skipWhiteSpace(tChar, 0, tChar.length);
            int tIdx = 0;
            boolean tHasComma = false;
            for (int i = tFrom; i < tChar.length; ++i) {
                int tCharCode = tChar[i];
                if (tFrom < 0) {
                    if (tCharCode > 32) {
                        if (tCharCode == 44) {
                            if (tHasComma) {
                                ++tIdx;
                                if (tIdx == aLength) return rData;
                            } else {
                                tHasComma = true;
                            }
                        } else {
                            tHasComma = false;
                            tFrom = i;
                        }
                    }
                } else {
                    if (tCharCode<=32 || tCharCode==44) {
                        if (tCharCode == 44) tHasComma = true;
                        try {rData.set(tIdx, CharScanner.parseDouble(tChar, tFrom, i));}
                        catch (Exception ignored) {}
                        tFrom = -1;
                        ++tIdx;
                        if (tIdx == aLength) return rData;
                    }
                }
            }
            // 最后一个数据
            if (tFrom >= 0 && tFrom < tChar.length) {
                try {rData.set(tIdx, CharScanner.parseDouble(tChar, tFrom, tChar.length));}
                catch (Exception ignored) {}
            }
            return rData;
        }
        
        /** useful methods, wrapper of {@link StringGroovyMethods} stuffs */
        @Contract("null -> true")
        public static boolean isBlank(final CharSequence self) {
            if (self == null) return true;
            return BLANKS_OR_EMPTY.matcher(self).matches();
        }
        public static boolean containsIgnoreCase(final CharSequence self, final CharSequence searchString) {return StringGroovyMethods.containsIgnoreCase(self, searchString);}
        
        /**
         * Start from aStartIdx to find the first index containing aContainStr
         * @author liqa
         * @param aLines where to find the aContainStr
         * @param aStartIdx the start position, include
         * @param aContainStr a string to find in aLines
         * @param aIgnoreCase if true, ignore case when comparing characters
         * @return the idx of aLines which contains aContainStr, or aLines.length if not find
         */
        public static int findLineContaining(List<String> aLines, int aStartIdx, String aContainStr, boolean aIgnoreCase) {
            int tIdx = aStartIdx;
            while (tIdx < aLines.size()) {
                if (aIgnoreCase) {
                    if (containsIgnoreCase(aLines.get(tIdx), aContainStr)) break;
                } else {
                    if (aLines.get(tIdx).contains(aContainStr)) break;
                }
                ++tIdx;
            }
            return tIdx;
        }
        public static int findLineContaining(List<String> aLines, int aStartIdx, String aContainStr) {return findLineContaining(aLines, aStartIdx, aContainStr, false);}
        
        public static @Nullable String findLineContaining(BufferedReader aReader, String aContainStr, boolean aIgnoreCase) throws IOException {
            String tLine;
            while ((tLine = aReader.readLine()) != null) {
                if (aIgnoreCase) {
                    if (containsIgnoreCase(tLine, aContainStr)) return tLine;
                } else {
                    if (tLine.contains(aContainStr)) return tLine;
                }
            }
            return null;
        }
        public static @Nullable String findLineContaining(BufferedReader aReader, String aContainStr) throws IOException {return findLineContaining(aReader, aContainStr, false);}
        
        
        /**
         * Splits a string separated by blank characters into multiple strings
         * <p> will automatically ignores multiple spaces and the beginning and end spaces </p>
         * @author liqa
         * @param aStr input string
         * @return the split sting in array
         */
        public static String[] splitBlank(String aStr) {
            return BLANKS.split(aStr.trim(), -1);
        }
        
        
        /**
         * Splits a string separated by comma(",") characters into multiple strings
         * <p> will automatically ignores multiple spaces </p>
         * @author liqa
         * @param aStr input string
         * @return the split sting in array
         */
        public static String[] splitComma(String aStr) {
            return COMMA.split(aStr.trim(), -1);
        }
        
        /**
         * 匹配使用空格分割或者逗号（{@code ,}）分割的字符串，可以出现混合
         * <p> 会自动忽略多余的空格 </p>
         * @author liqa
         * @param aStr input string
         * @return the split sting in array
         */
        @ApiStatus.Experimental
        public static String[] splitStr(String aStr) {
            return COMMA_OR_BLANKS.split(aStr.trim(), -1);
        }
        
        /**
         * Java version of splitNodeList
         * @author liqa
         * @param aRawNodeList input raw node list from $SLURM_JOB_NODELIST
         * @return split node list
         */
        public static List<String> splitNodeList(String aRawNodeList) {
            List<String> rOutput = new ArrayList<>();
            
            int tLen = aRawNodeList.length();
            boolean tInBlock = false;
            int tStart = 0;
            for (int i = 0; i < tLen; ++i) {
                if (tInBlock) {
                    if (aRawNodeList.charAt(i) == ']') tInBlock = false;
                    continue;
                }
                if (aRawNodeList.charAt(i) == '[') {
                    tInBlock = true;
                    continue;
                }
                if (aRawNodeList.charAt(i) == ',') {
                    splitNodeList_(aRawNodeList.substring(tStart, i), rOutput);
                    tStart = i+1;
                }
            }
            splitNodeList_(aRawNodeList.substring(tStart, tLen), rOutput);
            
            return rOutput;
        }
        private static void splitNodeList_(String aSubRawNodeList, List<String> rNodeList) {
            // check for "[", "]"
            int tListStart = aSubRawNodeList.indexOf("[");
            if (tListStart < 0) {
                rNodeList.add(aSubRawNodeList);
                return;
            }
            String tHeadStr = aSubRawNodeList.substring(0, tListStart);
            String tListStr = aSubRawNodeList.substring(tListStart+1, aSubRawNodeList.length()-1);
            
            // Split the string by comma
            String[] tArray = tListStr.split(",");
            
            // Range of numbers
            Pattern tPattern = Pattern.compile("([0-9]+)-([0-9]+)");
            // Loop through each range and generate the numbers
            for (String tRange : tArray) {
                Matcher tMatcher = tPattern.matcher(tRange);
                if (tMatcher.find()) {
                    String tStartStr = tMatcher.group(1);
                    int tMinLen = tStartStr.length();
                    int tStart = Integer.parseInt(tStartStr);
                    int tEnd = Integer.parseInt(tMatcher.group(2));
                    for (int i = tStart; i <= tEnd; ++i) {
                        rNodeList.add(String.format("%s%0"+tMinLen+"d", tHeadStr, i));
                    }
                } else {
                    // Single number
                    rNodeList.add(tHeadStr + tRange);
                }
            }
        }
        
        
        /**
         * convert between json and map
         * @author liqa
         */
        public static Map<?, ?> json2map(String aText) {
            return (Map<?, ?>) (new JsonSlurper()).parseText(aText);
        }
        public static String map2json(Map<?, ?> aMap) {
            return map2json(aMap, false);
        }
        public static String map2json(Map<?, ?> aMap, boolean aPretty) {
            JsonBuilder tBuilder = new JsonBuilder();
            tBuilder.call(aMap);
            return aPretty ? tBuilder.toPrettyString() : tBuilder.toString();
        }
        /**
         * convert between yaml and map
         * @author liqa
         */
        public static Map<?, ?> yaml2map(String aText) {
            return (Map<?, ?>) (new YamlSlurper()).parseText(aText);
        }
        public static String map2yaml(Map<?, ?> aMap) {
            YamlBuilder tBuilder = new YamlBuilder();
            tBuilder.call(aMap);
            return tBuilder.toString();
        }
    }
    
    /**
     * 内部使用，判断一个文件夹路径 aDir 是否符合内部使用的格式，
     * 内部使用时需要 aDir 的结尾为 / 或者为空，保证可以直接统一通过 + 文件名
     * 的方式来获取路径
     * @author liqa
     */
    @ApiStatus.Internal @Contract(pure = true) public static boolean isInternalValidDir(@NotNull String aDir) {
        return aDir.isEmpty() || aDir.endsWith("/") || aDir.endsWith("\\");
    }
    /**
     * 内部使用，将一个文件夹路径 aDir 转换为符合内部使用的格式
     * @author liqa
     */
    @ApiStatus.Internal @CheckReturnValue @Contract(pure = true) public static String toInternalValidDir(@NotNull String aDir) {
        return isInternalValidDir(aDir) ? aDir : (aDir+"/");
    }
    
    /**
     * Wrapper of {@link Files#write}
     * @author liqa
     * @param aFilePath File to write
     * @param aLines Iterable String or String[] to be written
     * @throws IOException when fail
     */
    public static void write(String aFilePath, String...                        aLines)                         throws IOException {write(aFilePath, aLines, ZL_OO);}
    public static void write(String aFilePath, Iterable<? extends CharSequence> aLines)                         throws IOException {write(aFilePath, aLines, ZL_OO);}
    public static void write(String aFilePath, String                            aLine)                         throws IOException {write(aFilePath, aLine, ZL_OO);}
    public static void write(String aFilePath, byte[]                            aData)                         throws IOException {write(aFilePath, aData, ZL_OO);}
    public static void write(String aFilePath, String[]                         aLines, OpenOption... aOptions) throws IOException {write(aFilePath, AbstractCollections.from(aLines), aOptions);}
    public static void write(String aFilePath, Iterable<? extends CharSequence> aLines, OpenOption... aOptions) throws IOException {write(toAbsolutePath_(aFilePath), aLines, aOptions);}
    public static void write(String aFilePath, String                            aLine, OpenOption... aOptions) throws IOException {write(aFilePath, Collections.singletonList(aLine), aOptions);}
    public static void write(String aFilePath, byte[]                            aData, OpenOption... aOptions) throws IOException {write(toAbsolutePath_(aFilePath), aData, aOptions);}
    public static void write(Path       aPath, byte[]                            aData, OpenOption... aOptions) throws IOException {validPath(aPath); Files.write(aPath, aData, aOptions);}
    public static void write(Path       aPath, String                            aLine, OpenOption... aOptions) throws IOException {write(aPath, Collections.singletonList(aLine), aOptions);}
    public static void write(Path       aPath, Iterable<? extends CharSequence> aLines, OpenOption... aOptions) throws IOException {
        validPath(aPath);
        // 使用 UT.IO 中的 stream 统一使用 LF 换行符
        try (IWriteln tWriteln = toWriteln(aPath, aOptions)) {
            for (CharSequence tLine: aLines) {tWriteln.writeln(tLine);}
        }
    }
    /**
     * Wrapper of {@link Files#writeString}
     * @author liqa
     * @param aFilePath File to write
     * @param aText String to be written
     * @throws IOException when fail
     */
    public static void writeText(String aFilePath, String aText) throws IOException {writeText(aFilePath, aText, ZL_OO);}
    public static void writeText(String aFilePath, String aText, OpenOption... aOptions) throws IOException {writeText(toAbsolutePath_(aFilePath), aText, aOptions);}
    public static void writeText(Path aPath, String aText, OpenOption... aOptions) throws IOException {
        validPath(aPath);
        // 现在改为直接使用 BufferedWriter 写入整个 String，保证编码格式为 UTF-8，并且内部也会自动分 buffer 处理字符串
        try (BufferedWriter tWriter = toWriter(aPath, aOptions)) {
            tWriter.write(aText);
        }
    }
    
    /**
     * Wrapper of {@link Files#readAllBytes}
     * @author liqa
     * @param aFilePath File to read
     * @return array of byte
     * @throws IOException when fail
     */
    public static byte[] readAllBytes(String aFilePath) throws IOException {return Files.readAllBytes(toAbsolutePath_(aFilePath));}
    /**
     * read all lines from the File
     * @author liqa
     * @param aFilePath File to read
     * @return lines of String
     * @throws IOException when fail
     */
    public static List<String> readAllLines(String aFilePath) throws IOException {
        try (BufferedReader tReader = toReader(aFilePath)) {
            return readAllLines(tReader);
        }
    }
    /**
     * read the specified number of lines from the File
     * @author liqa
     */
    public static List<String> readLines(String aFilePath, int aNumber) throws IOException {
        try (BufferedReader tReader = toReader(aFilePath)) {
            return readLines(tReader, aNumber);
        }
    }
    /**
     * read all lines from the BufferedReader
     * @author liqa
     */
    public static List<String> readAllLines(BufferedReader aReader) throws IOException {
        List<String> rLines = new ArrayList<>();
        String tLine;
        while ((tLine = aReader.readLine()) != null) rLines.add(tLine);
        return rLines;
    }
    /**
     * read the specified number of lines from the BufferedReader
     * @author liqa
     */
    public static List<String> readLines(BufferedReader aReader, int aNumber) throws IOException {
        List<String> rLines = new ArrayList<>();
        for (int i = 0; i < aNumber; ++i) {
            String tLine = aReader.readLine();
            if (tLine == null) break;
            rLines.add(tLine);
        }
        return rLines;
    }
    /**
     * jdk8 version of {@link Files#readString}
     * @author liqa
     * @param aFilePath File to read
     * @return lines of String
     * @throws IOException when fail
     */
    public static String readAllText(String aFilePath) throws IOException {
        try (BufferedReader tReader = toReader(aFilePath)) {
            return IOGroovyMethods.getText(tReader);
        }
    }
    
    
    /**
     * remove the Directory, will remove the subdirectories recursively
     * @author liqa
     * @param aDir the Directory will be removed
     */
    @VisibleForTesting public static void rmdir(String aDir) throws IOException {removeDir(aDir);}
    public static void removeDir(String aDir) throws IOException {
        aDir = toInternalValidDir(aDir);
        if (!isDir(aDir)) return;
        removeDir_(aDir);
    }
    private static void removeDir_(String aDir) throws IOException {
        for (String tName : list(aDir)) {
            if (tName==null || tName.isEmpty() || tName.equals(".") || tName.equals("..")) continue;
            String tFileOrDir = aDir+tName;
            if (isDir(tFileOrDir)) {removeDir_(tFileOrDir+"/");}
            else if (isFile(tFileOrDir)) {delete(tFileOrDir);}
        }
        delete(aDir);
    }
    
    /**
     * 递归的复制一个目录，这里使用字符串拼接的方法实现，而不是
     * {@link Files#walkFileTree}
     * @author liqa
     */
    @VisibleForTesting public static void cpdir(String aSourceDir, String aTargetDir) throws IOException {copyDir(aSourceDir, aTargetDir);}
    public static void copyDir(String aSourceDir, String aTargetDir) throws IOException {
        aSourceDir = toInternalValidDir(aSourceDir);
        aTargetDir = toInternalValidDir(aTargetDir);
        if (!exists(aSourceDir)) throw new NoSuchFileException(toAbsolutePath(aSourceDir));
        if (!isDir(aSourceDir)) throw new NotDirectoryException(toAbsolutePath(aSourceDir));
        copyDir_(aSourceDir, aTargetDir);
    }
    private static void copyDir_(String aSourceDir, String aTargetDir) throws IOException {
        makeDir(aTargetDir);
        for (String tName : list(aSourceDir)) {
            if (tName==null || tName.isEmpty() || tName.equals(".") || tName.equals("..")) continue;
            String tSourceFileOrDir = aSourceDir+tName;
            String tTargetFileOrDir = aTargetDir+tName;
            if (isDir(tSourceFileOrDir)) {copyDir_(tSourceFileOrDir+"/", tTargetFileOrDir+"/");}
            else if (isFile(tSourceFileOrDir)) {copy(tSourceFileOrDir, tTargetFileOrDir);}
        }
    }
    
    
    /** useful methods, wrapper of {@link Files} stuffs */
    @VisibleForTesting
    public static void      mkdir   (String aDir)                                   throws IOException {makeDir(aDir);} // can mkdir nested
    public static void      makeDir (String aDir)                                   throws IOException {makeDir(toAbsolutePath_(aDir));} // can mkdir nested
    @VisibleForTesting
    public static boolean   isdir   (String aDir)                                                      {return isDir(aDir);}
    public static boolean   isDir   (String aDir)                                                      {return Files.isDirectory(toAbsolutePath_(aDir));}
    @VisibleForTesting
    public static boolean   isfile  (String aFilePath)                                                 {return isFile(aFilePath);}
    public static boolean   isFile  (String aFilePath)                                                 {return Files.isRegularFile(toAbsolutePath_(aFilePath));}
    
    public static boolean   exists  (String aPath)                                                     {return Files.exists(toAbsolutePath_(aPath));}
    public static void      delete  (String aPath)                                  throws IOException {Files.deleteIfExists(toAbsolutePath_(aPath));} // can delete not exist path
    public static void      copy    (String aSourcePath, String aTargetPath)        throws IOException {copy(toAbsolutePath_(aSourcePath), toAbsolutePath_(aTargetPath));}
    public static void      copy    (InputStream aSourceStream, String aTargetPath) throws IOException {copy(aSourceStream, toAbsolutePath_(aTargetPath));}
    public static void      copy    (URL aSourceURL, String aTargetPath)            throws IOException {copy(aSourceURL, toAbsolutePath_(aTargetPath));}
    public static void      move    (String aSourcePath, String aTargetPath)        throws IOException {move(toAbsolutePath_(aSourcePath), toAbsolutePath_(aTargetPath));}
    public static void      makeDir (Path aDir)                                     throws IOException {Files.createDirectories(aDir);} // can mkdir nested
    public static void      copy    (Path aSourcePath, Path aTargetPath)            throws IOException {validPath(aTargetPath); Files.copy(aSourcePath, aTargetPath, REPLACE_EXISTING);}
    public static void      copy    (InputStream aSourceStream, Path aTargetPath)   throws IOException {validPath(aTargetPath); Files.copy(aSourceStream, aTargetPath, REPLACE_EXISTING);}
    public static void      copy    (URL aSourceURL, Path aTargetPath)              throws IOException {try (InputStream tURLStream = aSourceURL.openStream()) {copy(tURLStream, aTargetPath);}}
    public static void      move    (Path aSourcePath, Path aTargetPath)            throws IOException {validPath(aTargetPath); Files.move(aSourcePath, aTargetPath, REPLACE_EXISTING);}
    /** use the {@link File#list} not {@link Files#list} to get the simple result */
    public static String @NotNull[] list(String aDir) throws IOException {
        String[] tList = toFile(aDir).list();
        if (tList==null) throw new IOException("Fail to det list of \""+aDir+"\"");
        return tList;
    }
    /** map (filterLines) */
    public static void      map     (String        aSourcePath, String aTargetPath, IUnaryFullOperator<? extends CharSequence, ? super String> aOpt) throws IOException  {try (BufferedReader tReader = toReader(aSourcePath); IWriteln tWriter = toWriteln(aTargetPath)) {map(tReader, tWriter, aOpt);}}
    public static void      map     (InputStream aSourceStream, String aTargetPath, IUnaryFullOperator<? extends CharSequence, ? super String> aOpt) throws IOException  {try (BufferedReader tReader = toReader(aSourceStream); IWriteln tWriter = toWriteln(aTargetPath)) {map(tReader, tWriter, aOpt);}}
    public static void      map     (URL            aSourceURL, String aTargetPath, IUnaryFullOperator<? extends CharSequence, ? super String> aOpt) throws IOException  {try (BufferedReader tReader = toReader(aSourceURL); IWriteln tWriter = toWriteln(aTargetPath)) {map(tReader, tWriter, aOpt);}}
    public static void      map     (BufferedReader    aReader, IWriteln   aWriter, IUnaryFullOperator<? extends CharSequence, ? super String> aOpt) throws IOException  {String tLine; while ((tLine = aReader.readLine()) != null) {aWriter.writeln(aOpt.apply(tLine));}}
    
    
    /** only support writeln */
    @FunctionalInterface public interface IWriteln extends AutoCloseable {
        void writeln(CharSequence aLine) throws IOException;
        default void close() throws IOException {/**/}
    }
    
    /** output stuffs */
    public static OutputStream   toOutputStream(String aFilePath)                           throws IOException {return toOutputStream(aFilePath, ZL_OO);}
    public static OutputStream   toOutputStream(String aFilePath, OpenOption... aOptions)   throws IOException {return toOutputStream(toAbsolutePath_(aFilePath), aOptions);}
    public static OutputStream   toOutputStream(Path aPath, OpenOption... aOptions)         throws IOException {validPath(aPath); return Files.newOutputStream(aPath, aOptions);}
    public static BufferedWriter toWriter      (String aFilePath)                           throws IOException {return toWriter(aFilePath, ZL_OO);}
    public static BufferedWriter toWriter      (String aFilePath, OpenOption... aOptions)   throws IOException {return toWriter(toAbsolutePath_(aFilePath), aOptions);}
    public static BufferedWriter toWriter      (Path aPath, OpenOption... aOptions)         throws IOException {validPath(aPath); return new BufferedWriter(new OutputStreamWriter(toOutputStream(aPath, aOptions), StandardCharsets.UTF_8)) {@Override public void newLine() throws IOException {write("\n");}};}
    public static BufferedWriter toWriter      (OutputStream aOutputStream)                                    {return toWriter(aOutputStream, StandardCharsets.UTF_8);}
    public static BufferedWriter toWriter      (OutputStream aOutputStream, Charset aCS)                       {return new BufferedWriter(new OutputStreamWriter(aOutputStream, aCS)) {@Override public void newLine() throws IOException {write("\n");}};}
    public static IWriteln       toWriteln     (String aFilePath)                           throws IOException {return toWriteln(aFilePath, ZL_OO);}
    public static IWriteln       toWriteln     (String aFilePath, OpenOption... aOptions)   throws IOException {return toWriteln(toWriter(aFilePath, aOptions));}
    public static IWriteln       toWriteln     (Path aPath, OpenOption... aOptions)         throws IOException {return toWriteln(toWriter(aPath, aOptions));}
    public static IWriteln       toWriteln     (OutputStream aOutputStream)                                    {return toWriteln(toWriter(aOutputStream));}
    public static IWriteln       toWriteln     (OutputStream aOutputStream, Charset aCS)                       {return toWriteln(toWriter(aOutputStream, aCS));}
    public static IWriteln       toWriteln     (BufferedWriter aWriter)                                        {
        return new IWriteln() {
            @Override public void writeln(CharSequence aLine) throws IOException {aWriter.append(aLine); aWriter.newLine();}
            @Override public void close() throws IOException {aWriter.close();}
        };
    }
    
    /** input stuffs */
    public static InputStream    toInputStream(String aFilePath)         throws IOException {return toInputStream(toAbsolutePath_(aFilePath));}
    public static InputStream    toInputStream(Path aPath)               throws IOException {return Files.newInputStream(aPath);}
    public static BufferedReader toReader     (String aFilePath)         throws IOException {return toReader(toAbsolutePath_(aFilePath));}
    public static BufferedReader toReader     (Path aPath)               throws IOException {return toReader(toInputStream(aPath));}
    public static BufferedReader toReader     (URL aFileURL)             throws IOException {return toReader(aFileURL.openStream());}
    public static BufferedReader toReader     (InputStream aInputStream) throws IOException {return toReader(aInputStream, StandardCharsets.UTF_8, true);}
    public static BufferedReader toReader     (InputStream aInputStream, Charset aCS) throws IOException {return toReader(aInputStream, aCS, false);}
    public static BufferedReader toReader     (InputStream aInputStream, Charset aCS, boolean aUseUnicodeReader) throws IOException {
        // 现在改为 UnicodeReader 实现，可以自动检测 UTF 的 BOM
        return new BufferedReader(aUseUnicodeReader ? new UnicodeReader(aInputStream, aCS.name()) : new InputStreamReader(aInputStream, aCS));
    }
    
    /** misc stuffs */
    public static File toFile(String aFilePath)                    {return toAbsolutePath_(aFilePath).toFile();}
    public static void validPath(String aPath)  throws IOException {if (aPath.endsWith("/") || aPath.endsWith("\\")) makeDir(aPath); else validPath(toAbsolutePath_(aPath));}
    public static void validPath(Path aPath)    throws IOException {Path tParent = aPath.getParent(); if (tParent != null) makeDir(tParent);}
    
    /**
     * extract zip file to directory
     * @author liqa
     */
    public static void zip2dir(String aZipFilePath, String aDir) throws IOException {
        aDir = toInternalValidDir(aDir);
        makeDir(aDir);
        byte[] tBuffer = ByteArrayCache.getArray(BUFFER_SIZE);
        try (ZipInputStream tZipInputStream = new ZipInputStream(toInputStream(aZipFilePath))) {
            ZipEntry tZipEntry = tZipInputStream.getNextEntry();
            while (tZipEntry != null) {
                String tEntryPath = aDir + tZipEntry.getName();
                if (tZipEntry.isDirectory()) {
                    makeDir(tEntryPath);
                } else {
                    try (OutputStream tOutputStream = toOutputStream(tEntryPath)) {
                        int length;
                        while ((length = tZipInputStream.read(tBuffer, 0, BUFFER_SIZE)) > 0) {
                            tOutputStream.write(tBuffer, 0, length);
                        }
                    }
                }
                tZipEntry = tZipInputStream.getNextEntry();
            }
        } finally {
            ByteArrayCache.returnArray(tBuffer);
        }
    }
    
    /**
     * compress directory to zip file
     * @author liqa
     */
    public static void dir2zip(String aDir, String aZipFilePath, int aCompressLevel) throws IOException {
        aDir = toInternalValidDir(aDir);
        byte[] tBuffer =  ByteArrayCache.getArray(BUFFER_SIZE);
        try (ZipOutputStream tZipOutputStream = new ZipOutputStream(toOutputStream(aZipFilePath))) {
            tZipOutputStream.setLevel(aCompressLevel);
            for (String tName : list(aDir)) {
                if (tName==null || tName.isEmpty() || tName.equals(".") || tName.equals("..")) continue;
                String tPath = aDir+tName;
                if (isDir(tPath)) addDirToZip_("", tPath+"/", tName, tZipOutputStream, tBuffer);
                else addFileToZip_("", tPath, tName, tZipOutputStream, tBuffer);
            }
        } finally {
            ByteArrayCache.returnArray(tBuffer);
        }
    }
    public static void dir2zip(String aDir, String aZipFilePath) throws IOException {dir2zip(aDir, aZipFilePath, Deflater.DEFAULT_COMPRESSION);}
    /**
     * compress files/directories to zip file
     * @author liqa
     */
    public static void files2zip(String[] aPaths, String aZipFilePath, int aCompressLevel) throws IOException {files2zip(AbstractCollections.from(aPaths), aZipFilePath, aCompressLevel);}
    public static void files2zip(String[] aPaths, String aZipFilePath) throws IOException {files2zip(AbstractCollections.from(aPaths), aZipFilePath);}
    /** Groovy stuff */
    public static void files2zip(Iterable<? extends CharSequence> aPaths, String aZipFilePath, int aCompressLevel) throws IOException {
        byte[] tBuffer = ByteArrayCache.getArray(BUFFER_SIZE);
        try (ZipOutputStream tZipOutputStream = new ZipOutputStream(toOutputStream(aZipFilePath))) {
            tZipOutputStream.setLevel(aCompressLevel);
            for (CharSequence tCS : aPaths) {
                String tPath = tCS.toString();
                File tFile = toFile(tPath);
                if (tFile.isDirectory()) {
                    tPath = toInternalValidDir(tPath);
                    addDirToZip_("", tPath, tFile.getName(), tZipOutputStream, tBuffer);
                } else {
                    addFileToZip_("", tPath, tFile.getName(), tZipOutputStream, tBuffer);
                }
            }
        } finally {
            ByteArrayCache.returnArray(tBuffer);
        }
    }
    public static void files2zip(Iterable<? extends CharSequence> aPaths, String aZipFilePath) throws IOException {files2zip(aPaths, aZipFilePath, Deflater.DEFAULT_COMPRESSION);}
    
    private static void addFileToZip_(String aZipDir, String aFilePath, String aFileName, ZipOutputStream aZipOutputStream, byte[] rBuffer) throws IOException {
        try (InputStream tInputStream = toInputStream(aFilePath)) {
            aZipOutputStream.putNextEntry(new ZipEntry(aZipDir+aFileName));
            int length;
            while ((length = tInputStream.read(rBuffer, 0, BUFFER_SIZE)) > 0) {
                aZipOutputStream.write(rBuffer, 0, length);
            }
            aZipOutputStream.closeEntry();
        }
    }
    private static void addDirToZip_(String aZipDir, String aDir, String aDirName, ZipOutputStream aZipOutputStream, byte[] rBuffer) throws IOException {
        String tZipDir = aZipDir+aDirName+"/";
        for (String tName : list(aDir)) {
            if (tName==null || tName.isEmpty() || tName.equals(".") || tName.equals("..")) continue;
            String tPath = aDir+tName;
            if (isDir(tPath)) addDirToZip_(tZipDir, tPath+"/", tName, aZipOutputStream, rBuffer);
            else addFileToZip_(tZipDir, tPath, tName, aZipOutputStream, rBuffer);
        }
    }
    
    
    /**
     * convert between json and map
     * @author liqa
     */
    public static Map<?, ?> json2map(String aFilePath) throws IOException {
        try (Reader tReader = toReader(aFilePath)) {return json2map(tReader);}
    }
    public static Map<?, ?> json2map(Reader aReader) {
        return (Map<?, ?>) (new JsonSlurper()).parse(aReader);
    }
    public static void map2json(Map<?, ?> aMap, String aFilePath) throws IOException {
        map2json(aMap, aFilePath, false);
    }
    public static void map2json(Map<?, ?> aMap, String aFilePath, boolean aPretty) throws IOException {
        try (Writer tWriter = toWriter(aFilePath)) {
            JsonBuilder tBuilder = new JsonBuilder();
            tBuilder.call(aMap);
            if (aPretty) {
                tWriter.append(tBuilder.toPrettyString());
            } else {
                tBuilder.writeTo(tWriter);
            }
        }
    }
    /**
     * convert between yaml and map
     * @author liqa
     */
    public static Map<?, ?> yaml2map(String aFilePath) throws IOException {
        try (Reader tReader = toReader(aFilePath)) {return yaml2map(tReader);}
    }
    public static Map<?, ?> yaml2map(Reader aReader) {
        return (Map<?, ?>) (new YamlSlurper()).parse(aReader);
    }
    public static void map2yaml(Map<?, ?> aMap, String aFilePath) throws IOException {
        try (Writer tWriter = toWriter(aFilePath)) {
            YamlBuilder tBuilder = new YamlBuilder();
            tBuilder.call(aMap);
            tBuilder.writeTo(tWriter);
        }
    }
    
    
    /**
     * save matrix data to csv file
     * @author liqa
     * @param aData the matrix form data to save
     * @param aFilePath csv file path to save
     */
    public static void data2csv(double[][] aData, String aFilePath)                   throws IOException {data2csv(aData, aFilePath, ZL_STR);}
    public static void data2csv(double[][] aData, String aFilePath, String... aHeads) throws IOException {rows2csv(aData, aFilePath, aHeads);}
    public static void rows2csv(double[][] aData, String aFilePath)                   throws IOException {rows2csv(aData, aFilePath, ZL_STR);}
    public static void rows2csv(double[][] aRows, String aFilePath, String... aHeads) throws IOException {
        List<String> rLines = AbstractCollections.map(AbstractCollections.from(aRows), row -> String.join(",", AbstractCollections.map(row, Object::toString)));
        if (aHeads!=null && aHeads.length>0) rLines = AbstractCollections.merge(String.join(",", aHeads), rLines);
        write(aFilePath, rLines);
    }
    public static void cols2csv(double[][] aData, String aFilePath)                   throws IOException {cols2csv(aData, aFilePath, ZL_STR);}
    public static void cols2csv(double[][] aCols, String aFilePath, String... aHeads) throws IOException {
        List<String> rLines = AbstractCollections.from(aCols[0].length, i ->  String.join(",", AbstractCollections.from(aCols.length, j -> String.valueOf(aCols[j][i]))));
        if (aHeads!=null && aHeads.length>0) rLines = AbstractCollections.merge(String.join(",", aHeads), rLines);
        write(aFilePath, rLines);
    }
    public static void data2csv(double[] aData, String aFilePath) throws IOException {
        write(aFilePath, AbstractCollections.map(aData, Object::toString));
    }
    public static void data2csv(double[] aData, String aFilePath, String aHead) throws IOException {
        write(aFilePath, AbstractCollections.merge(aHead, AbstractCollections.map(aData, Object::toString)));
    }
    public static void data2csv(Iterable<?> aData, String aFilePath)                   throws IOException {data2csv(aData, aFilePath, ZL_STR);}
    public static void data2csv(Iterable<?> aData, String aFilePath, String... aHeads) throws IOException {rows2csv(aData, aFilePath, aHeads);}
    public static void rows2csv(Iterable<?> aData, String aFilePath)                   throws IOException {rows2csv(aData, aFilePath, ZL_STR);}
    public static void rows2csv(Iterable<?> aRows, String aFilePath, String... aHeads) throws IOException {
        Iterable<String> rLines = AbstractCollections.map(aRows, row -> {
            if (row instanceof IVector) {
                return String.join(",", AbstractCollections.map(((IVector)row), Object::toString));
            } else
            if (row instanceof double[]) {
                return String.join(",", AbstractCollections.map((double[])row, Object::toString));
            } else
            if (row instanceof Iterable) {
                return String.join(",", AbstractCollections.map((Iterable<?>)row, String::valueOf));
            } else
            if (row instanceof Object[]) {
                return String.join(",", AbstractCollections.map((Object[])row, String::valueOf));
            } else {
                return String.valueOf(row);
            }
        });
        if (aHeads!=null && aHeads.length>0) rLines = AbstractCollections.merge(String.join(",", aHeads), rLines);
        write(aFilePath, rLines);
    }
    public static void cols2csv(Iterable<?> aData, String aFilePath)                   throws IOException {cols2csv(aData, aFilePath, ZL_STR);}
    public static void cols2csv(Iterable<?> aCols, String aFilePath, String... aHeads) throws IOException {
        List<Iterator<String>> its = NewCollections.map(aCols, col -> {
            if (col instanceof IVector) {
                return AbstractCollections.map((IVector)col, Object::toString).iterator();
            } else
            if (col instanceof double[]) {
                return AbstractCollections.map((double[])col, Object::toString).iterator();
            } else
            if (col instanceof Iterable) {
                return AbstractCollections.map((Iterable<?>)col, String::valueOf).iterator();
            } else
            if (col instanceof Object[]) {
                return AbstractCollections.map((Object[])col, String::valueOf).iterator();
            } else {
                return Collections.singletonList(String.valueOf(col)).iterator();
            }
        });
        validPath(aFilePath);
        try (IWriteln tWriteln = toWriteln(aFilePath)) {
            if (aHeads!=null && aHeads.length>0) tWriteln.writeln(String.join(",", aHeads));
            List<String> tTokens = new ArrayList<>(its.size());
            boolean tHasNext = true;
            while (true) {
                for (Iterator<String> it : its) {
                    if (!it.hasNext()) {tHasNext = false; break;}
                    tTokens.add(it.next());
                }
                if (!tHasNext) break;
                tWriteln.writeln(String.join(",", tTokens));
                tTokens.clear();
            }
        }
    }
    public static void data2csv(IMatrix aData, String aFilePath)                   throws IOException {data2csv(aData, aFilePath, ZL_STR);}
    public static void data2csv(IMatrix aData, String aFilePath, String... aHeads) throws IOException {
        List<String> rLines = AbstractCollections.map(aData.rows(), subData -> String.join(",", AbstractCollections.map(subData, Object::toString)));
        if (aHeads!=null && aHeads.length>0) rLines = AbstractCollections.merge(String.join(",", aHeads), rLines);
        write(aFilePath, rLines);
    }
    public static void data2csv(IVector aData, String aFilePath) throws IOException {
        write(aFilePath, AbstractCollections.map(aData, Object::toString));
    }
    public static void data2csv(IVector aData, String aFilePath, String aHead) throws IOException {
        write(aFilePath, AbstractCollections.merge(aHead, AbstractCollections.map(aData, Object::toString)));
    }
    public static void data2csv(IFunc1 aFunc, String aFilePath)                   throws IOException {data2csv(aFunc, aFilePath, ZL_STR);}
    public static void data2csv(IFunc1 aFunc, String aFilePath, String... aHeads) throws IOException {
        List<String> rLines = AbstractCollections.map(AbstractCollections.range(aFunc.Nx()), i -> aFunc.get(i)+","+aFunc.getX(i));
        rLines = AbstractCollections.merge((aHeads!=null && aHeads.length>0) ? String.join(",", aHeads) : "f,x", rLines);
        write(aFilePath, rLines);
    }
    
    /**
     * read matrix data from csv file
     * <p>
     * 现在也可以直接读取泛滥的空格分割的数据；
     * 现在也可以处理泛滥的 {@code #} 注释的情况，
     * 这里简单处理只考虑其在行首的情况
     * @author liqa
     * @param aFilePath csv file path to read
     * @return a matrix
     */
    public static RowMatrix csv2data(String aFilePath) throws IOException {
        try (BufferedReader tReader = toReader(aFilePath)) {
            // 需要的参数
            RowMatrix.Builder rBuilder;
            String tLine;
            int tColNum;
            // 跳过开头可能的注释行
            while ((tLine = tReader.readLine()) != null) {
                if (!tLine.startsWith("#")) break;
            }
            if (tLine == null) return null;
            // 读取第一行来判断列数
            String[] tTokens = Text.splitStr(tLine);
            tColNum = tTokens.length;
            rBuilder = RowMatrix.builder(tColNum);
            // 读取第一行检测是否有头，直接看能否成功粘贴
            IVector tRow = null;
            try {tRow = Vectors.from(AbstractCollections.map(tTokens, Double::parseDouble));} catch (Exception ignored) {} // 直接看能否成功粘贴
            if (tRow != null) {
                rBuilder.addRow(tRow);
            }
            // 遍历读取后续数据
            while ((tLine = tReader.readLine()) != null) {
                // 跳过注释行
                if (tLine.startsWith("#")) continue;
                rBuilder.addRow(Text.str2data(tLine, tColNum));
            }
            // 返回结果
            rBuilder.trimToSize();
            return rBuilder.build();
        }
    }
    
    
    /**
     * save table to csv file
     * @author liqa
     * @param aTable the Table to be saved
     * @param aFilePath csv file path to save
     */
    public static void table2csv(ITable aTable, String aFilePath) throws IOException {
        List<String> rLines = AbstractCollections.map(aTable.rows(), subData -> String.join(",", AbstractCollections.map(subData, Object::toString)));
        rLines = AbstractCollections.merge(String.join(",", aTable.heads()), rLines);
        write(aFilePath, rLines);
    }
    /**
     * read table from csv file
     * <p>
     * 现在也可以直接读取泛滥的空格分割的数据；
     * 现在也可以处理泛滥的 {@code #} 注释的情况，
     * 这里简单处理只考虑其在行首的情况
     * @author liqa
     * @param aFilePath csv file path to read
     * @return table with head
     */
    public static Table csv2table(String aFilePath) throws IOException {
        try (BufferedReader tReader = toReader(aFilePath)) {
            // 需要的参数
            List<IVector> rRows = new ArrayList<>();
            String tLine;
            int tColNum;
            String[] tHeads = ZL_STR;
            // 跳过开头可能的注释行
            while ((tLine = tReader.readLine()) != null) {
                if (!tLine.startsWith("#")) break;
            }
            if (tLine == null) return null;
            // 读取第一行来判断列数
            String[] tTokens = Text.splitStr(tLine);
            tColNum = tTokens.length;
            // 读取第一行检测是否有头，直接看能否成功粘贴
            IVector tRow = null;
            try {tRow = Vectors.from(AbstractCollections.map(tTokens, Double::parseDouble));} catch (Exception ignored) {} // 直接看能否成功粘贴
            if (tRow != null) {
                rRows.add(tRow);
            } else {
                tHeads = tTokens;
            }
            // 遍历读取后续数据
            while ((tLine = tReader.readLine()) != null) {
                // 跳过注释行
                if (tLine.startsWith("#")) continue;
                rRows.add(Text.str2data(tLine, tColNum));
            }
            // 返回结果
            return Tables.fromRows(rRows, tHeads);
        }
    }
    
    /**
     * 保证兼容性的读取 csv 到 String，
     * 不假设 csv 是纯数字的，并且不识别 head
     * @author liqa
     * @param aFilePath csv file path to read
     * @param aFormat 自定义 {@link CSVFormat}
     * @return split 后的行组成的 List
     */
    public static List<String[]> csv2str(String aFilePath, CSVFormat aFormat) throws IOException {
        List<String[]> rLines = new ArrayList<>();
        try (CSVParser tParser = new CSVParser(toReader(aFilePath), aFormat)) {
            for (CSVRecord tRecord : tParser) rLines.add(tRecord.values());
        }
        return rLines;
    }
    /**
     * 保证兼容性的保存 String 到 csv，
     * 这里不提供额外 head 支持
     * @author liqa
     * @param aLines 需要保存的字符串数据，按行排列
     * @param aFilePath csv file path to save
     * @param aFormat 自定义 {@link CSVFormat}
     */
    public static void str2csv(Iterable<?> aLines, String aFilePath, CSVFormat aFormat) throws IOException {
        try (CSVPrinter tPrinter = new CSVPrinter(toWriter(aFilePath), aFormat)) {
            for (Object tLine : aLines) {
                if (tLine instanceof Iterable) {
                    tPrinter.printRecord((Iterable<?>)tLine);
                } else
                if (tLine instanceof Object[]) {
                    tPrinter.printRecord((Object[])tLine);
                } else {
                    tPrinter.printRecord(tLine);
                }
            }
        }
    }
    private final static CSVFormat DEFAULT_CSV_FORMAT = CSVFormat.DEFAULT.builder().setRecordSeparator('\n').setCommentMarker('#').setTrim(true).build();
    public static List<String[]> csv2str(String aFilePath) throws IOException {return csv2str(aFilePath, DEFAULT_CSV_FORMAT);}
    public static void str2csv(Iterable<?> aLines, String aFilePath) throws IOException {str2csv(aLines, aFilePath, DEFAULT_CSV_FORMAT);}
    
    /**
     * get URL of the resource
     * @author liqa
     */
    public static URL getResource(String aPath) {
        return IO.class.getClassLoader().getResource("assets/" + aPath);
    }
    
    /**
     * check whether the two paths are actually same
     * @author liqa
     */
    public static boolean samePath(String aPath1, String aPath2) {return WORKING_DIR_PATH.resolve(aPath1).normalize().equals(WORKING_DIR_PATH.resolve(aPath2).normalize());}
    
    
    /**
     * @author liqa
     * @param aPath 字符串路径
     * @return 给定路径的父路径
     */
    public static @Nullable String toParentPath(String aPath) {
        Path tPath = toParentPath_(aPath);
        return tPath==null ? null : tPath.toString();
    }
    public static @Nullable Path toParentPath_(String aPath) {return Paths.get(aPath).getParent();}
    
    /**
     * @author liqa
     * @param aPath 字符串路径
     * @return 给定路径的最后一项文件名（或目录名）
     */
    public static String toFileName(String aPath) {return toFileName_(aPath).toString();}
    public static Path toFileName_(String aPath) {return Paths.get(aPath).getFileName();}
    
    /**
     * 将输入路径转为相对路径，顺便会将 {@code `\`}
     * 转换为 {@code `/`}，保证可以用于 ssh 的文件路径；
     * 此项目依旧认为所有路径风格符都为 {@code `/`} 并要求至少兼容此种路径分隔符
     * @author liqa
     * @param aPath 字符串路径
     * @return 合理的相对路径
     */
    public static String toRelativePath(String aPath) {return toRelativePath_(aPath).toString().replace("\\", "/");}
    public static Path toRelativePath_(String aPath) {return WORKING_DIR_PATH.relativize(Paths.get(aPath));}
    
    /**
     * Right `toAbsolutePath` method,
     * because `toAbsolutePath` in `Paths` will still not work even used `setProperty`
     * @author liqa
     * @param aPath string path, can be relative or absolute
     * @return the Right absolute path
     */
    public static String toAbsolutePath(String aPath) {
        if (aPath.startsWith("~")) {
            // 默认不支持 ~
            return USER_HOME + aPath.substring(1); // user.home 这里统一认为 user.home 就是绝对路径
        }
        return WORKING_DIR_PATH.resolve(aPath).toString();
    }
    public static Path toAbsolutePath_(String aPath) {
        if (aPath.startsWith("~")) {
            // 默认不支持 ~
            return Paths.get(USER_HOME + aPath.substring(1)); // user.home 这里统一认为 user.home 就是绝对路径
        }
        return WORKING_DIR_PATH.resolve(aPath);
    }
    public static boolean isAbsolutePath(String aPath) {return isAbsolutePath(Paths.get(aPath));}
    public static boolean isAbsolutePath(Path aPath) {return aPath.isAbsolute();}
}
