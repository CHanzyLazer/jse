package jtool.code.script;

import groovy.lang.MetaClass;
import jep.JepException;
import jep.python.PyCallable;
import jep.python.PyObject;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.util.Map;

/**
 * @author liqa
 * <p> Python Script Object </p>
 */
public class ScriptObjectPython implements IScriptObject {
    private final PyObject mPyObj;
    ScriptObjectPython(PyObject aPyObj) {mPyObj = aPyObj;}
    @Override public Object unwrap() {return mPyObj;}
    
    @SuppressWarnings("unchecked")
    @Override public Object invokeMethod(String name, Object args) throws JepException {
        Object[] aArgs = (Object[])args;
        if (aArgs == null || aArgs.length == 0) return of(mPyObj.getAttr(name, PyCallable.class).call());
        if (aArgs.length == 1 && (aArgs[0] instanceof Map)) return of(mPyObj.getAttr(name, PyCallable.class).call((Map<String, Object>)aArgs[0]));
        if (aArgs.length > 1 && (aArgs[aArgs.length-1] instanceof Map)) {
            Object[] tArgs = new Object[aArgs.length-1];
            System.arraycopy(aArgs, 0, tArgs, 0, aArgs.length-1);
            return of(mPyObj.getAttr(name, PyCallable.class).call(tArgs, (Map<String, Object>)aArgs[aArgs.length-1]));
        }
        return of(mPyObj.getAttr(name, PyCallable.class).call(aArgs));
    }
    @Override public Object getProperty(String propertyName) throws JepException {return of(mPyObj.getAttr(propertyName));}
    @Override public void setProperty(String propertyName, Object newValue) throws JepException {mPyObj.setAttr(propertyName, newValue);}
    
    
    private MetaClass mDelegate = InvokerHelper.getMetaClass(getClass());
    @Override public MetaClass getMetaClass() {return mDelegate;}
    @Override public void setMetaClass(MetaClass metaClass) {mDelegate = metaClass;}
    
    
    /** python 重载运算符匹配 */
    public Object call(Object... aArgs) throws JepException {return invokeMethod("__call__", aArgs);}
    public Object getAt(int aIdx) {return of(mPyObj.getAttr("__getitem__", PyCallable.class).call(aIdx));}
    public void putAt(int aIdx, Object aValue) {mPyObj.getAttr("__setitem__", PyCallable.class).call(aIdx, aValue);}
    
    @Override public String toString() {return String.valueOf(mPyObj.getAttr("__str__", PyCallable.class).call());}
    
    /** 主要用来判断是否需要外包这一层 */
    public static Object of(Object aObj) {return (aObj instanceof PyObject) ? (new ScriptObjectPython((PyObject)aObj)) : aObj;}
}
