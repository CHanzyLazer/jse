package jse.io;

import groovy.yaml.YamlBuilder;
import groovy.yaml.YamlSlurper;
import jse.code.UT;
import org.codehaus.groovy.runtime.IOGroovyMethods;
import org.codehaus.groovy.util.CharSequenceReader;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.Map;

import static jse.code.CS.KEEP;
import static jse.code.CS.REMOVE;


/**
 * @author liqa
 * <p> Yaml 格式的输入文件 </p>
 */
public abstract class AbstractInFileYaml extends AbstractInFile {
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override public final void writeTo_(UT.IO.IWriteln aWriteln) throws IOException {
        Map tYaml;
        try (Reader tInFile = getInFileReader()) {
            tYaml = (Map) (new YamlSlurper()).parse(tInFile);
        }
        // 直接遍历修改
        for (Entry<String, Object> subSetting : entrySet()) if (subSetting.getValue()!=KEEP && tYaml.containsKey(subSetting.getKey())) {
            if (subSetting.getValue() == REMOVE) tYaml.remove(subSetting.getKey());
            else tYaml.put(subSetting.getKey(), subSetting.getValue());
        }
        YamlBuilder tBuilder = new YamlBuilder(); tBuilder.call(tYaml);
        String tYamlStr = tBuilder.toString();
        for (Iterator<String> it = IOGroovyMethods.iterator(new CharSequenceReader(tYamlStr)); it.hasNext(); ) {
            aWriteln.writeln(it.next());
        }
    }
    
    /** stuff to override，提供一个获取 inFile 的方法即可 */
    protected abstract Reader getInFileReader() throws IOException;
}
