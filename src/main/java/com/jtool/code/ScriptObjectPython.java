package com.jtool.code;

import groovy.lang.MetaClass;
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
    public ScriptObjectPython(PyObject aPyObj) {mPyObj = aPyObj;}
    @Override public Object unwrap() {return mPyObj;}
    
    @SuppressWarnings("unchecked")
    @Override public Object invokeMethod(String name, Object args) {
        Object[] aArgs = (Object[])args;
        if (aArgs == null || aArgs.length == 0) return mPyObj.getAttr(name, PyCallable.class).call();
        if (aArgs.length == 1 && (aArgs[0] instanceof Map)) return mPyObj.getAttr(name, PyCallable.class).call((Map<String, Object>)aArgs[0]);
        if (aArgs.length > 1 && (aArgs[aArgs.length-1] instanceof Map)) {
            Object[] tArgs = new Object[aArgs.length-1];
            System.arraycopy(aArgs, 0, tArgs, 0, aArgs.length-1);
            return mPyObj.getAttr(name, PyCallable.class).call(tArgs, (Map<String, Object>)aArgs[aArgs.length-1]);
        }
        return mPyObj.getAttr(name, PyCallable.class).call(aArgs);
    }
    @Override public Object getProperty(String propertyName) {return mPyObj.getAttr(propertyName);}
    @Override public void setProperty(String propertyName, Object newValue) {mPyObj.setAttr(propertyName, newValue);}
    
    
    private MetaClass mDelegate = InvokerHelper.getMetaClass(getClass());
    @Override public MetaClass getMetaClass() {return mDelegate;}
    @Override public void setMetaClass(MetaClass metaClass) {mDelegate = metaClass;}
}
