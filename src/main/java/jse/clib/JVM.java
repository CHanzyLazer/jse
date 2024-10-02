package jse.clib;

import static jse.code.Conf.LIB_NAME_IN;
import static jse.code.Conf.LLIB_NAME_IN;
import static jse.code.OS.IS_WINDOWS;
import static jse.code.OS.JAVA_HOME_DIR;

public class JVM {
    public final static String INCLUDE_DIR;
    public final static String LIB_DIR;
    public final static String LIB_PATH;
    public final static String LLIB_DIR;
    public final static String LLIB_PATH;
    
    static {
        INCLUDE_DIR = JAVA_HOME_DIR + "include/";
        String tLibDir = JAVA_HOME_DIR + "bin/server/";
        String tLibName = LIB_NAME_IN(tLibDir, "jvm");
        if (tLibName == null) {
            tLibDir = JAVA_HOME_DIR + "bin/client/";
            tLibName = LIB_NAME_IN(tLibDir, "jvm");
        }
        if (tLibName == null) {
            tLibDir = JAVA_HOME_DIR + "lib/server/";
            tLibName = LIB_NAME_IN(tLibDir, "jvm");
        }
        if (tLibName == null) {
            tLibDir = JAVA_HOME_DIR + "lib/client/";
            tLibName = LIB_NAME_IN(tLibDir, "jvm");
        }
        if (tLibName == null) throw new RuntimeException("No jvm lib in '$JAVA_HOME/bin/server/', '$JAVA_HOME/bin/client/', '$JAVA_HOME/lib/server/' or '$JAVA_HOME/lib/client/'");
        LIB_DIR = tLibDir;
        LIB_PATH = tLibDir + tLibName;
        if (!IS_WINDOWS) {
            LLIB_DIR = LIB_DIR;
            LLIB_PATH = LIB_PATH;
        } else {
            LLIB_DIR = JAVA_HOME_DIR + "lib/";
            String tLLibName = LLIB_NAME_IN(LLIB_DIR, "jvm");
            if (tLLibName == null) throw new RuntimeException("No jvm llib in '$JAVA_HOME/lib/'");
            LLIB_PATH = LLIB_DIR + tLLibName;
        }
    }
}
