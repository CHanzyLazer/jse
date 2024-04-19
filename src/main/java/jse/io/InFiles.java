package jse.io;

import com.google.common.collect.ImmutableMap;
import jse.code.UT;
import jse.lmp.LmpIn;
import org.jetbrains.annotations.VisibleForTesting;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * @author liqa
 * <p> 提供一些常用的输入文件的获取 </p>
 */
public class InFiles {
    public static IInFile lmp(String aLmpInFilePath) {return LmpIn.custom(aLmpInFilePath);}
    public static IInFile json(final String aJsonFilePath) {return new AbstractInFileJson() {@Override protected Map<?, ?> getJsonMap() throws IOException {return UT.IO.json2map(aJsonFilePath);}};}
    public static IInFile yaml(final String aYamlFilePath) {return new AbstractInFileYaml() {@Override protected Map<?, ?> getYamlMap() throws IOException {return UT.IO.yaml2map(aYamlFilePath);}};}
    public static IInFile json(final Map<?, ?>   aJsonMap) {return new AbstractInFileJson() {@Override protected Map<?, ?> getJsonMap() {return new LinkedHashMap<>(aJsonMap);}};}
    public static IInFile yaml(final Map<?, ?>   aYamlMap) {return new AbstractInFileYaml() {@Override protected Map<?, ?> getYamlMap() {return new LinkedHashMap<>(aYamlMap);}};}
    public static IInFile immutable(final String aInFilePath) {return new AbstractInFile(ImmutableMap.of()) {
        @Override public void writeTo_(UT.IO.IWriteln aWriteln) throws IOException {
            try (BufferedReader tReader = UT.IO.toReader(aInFilePath)) {
                String tLine;
                while ((tLine = tReader.readLine()) != null) aWriteln.writeln(tLine);
            }
        }
    };}
    
    /**
     * 默认行为，这里约定：
     * 对于实例类内部的静态方法来构造自身的，如果有输入参数，默认行为使用 of；
     * 对于实例类内部的静态方法来构造自身的，如果没有输入参数，默认行为使用 create；
     * 对于 xxxs 之类的工具类中的静态方法统一构造的，如果有输入参数，对于默认行为使用 from；
     * 对于 xxxs 之类的工具类中的静态方法统一构造的，如果没有输入参数，对于默认行为使用 get
     */
    @VisibleForTesting public static IInFile from(String aInFilePath) {return immutable(aInFilePath);}
}
