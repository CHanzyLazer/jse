#include "jniutil.h"
#include "LmpPair.h"


jclass JSE_LMPPAIR::LMPPAIR_CLAZZ = NULL;
jclass JSE_LMPPAIR::STRING_CLAZZ = NULL;

int JSE_LMPPAIR::cacheJClass(JNIEnv *env) {
    if (LMPPAIR_CLAZZ == NULL) {
        jclass clazz = env->FindClass("jse/lmp/LmpPlugin$Pair");
        if(env->ExceptionCheck()) return 0;
        LMPPAIR_CLAZZ = (jclass)env->NewGlobalRef(clazz);
        env->DeleteLocalRef(clazz);
    }
    if (STRING_CLAZZ == NULL) {
        jclass clazz = env->FindClass("java/lang/String");
        if(env->ExceptionCheck()) return 0;
        STRING_CLAZZ = (jclass)env->NewGlobalRef(clazz);
        env->DeleteLocalRef(clazz);
    }
    return 1;
}
void JSE_LMPPAIR::uncacheJClass(JNIEnv *env) {
    if (LMPPAIR_CLAZZ != NULL) {
        env->DeleteGlobalRef(LMPPAIR_CLAZZ);
        LMPPAIR_CLAZZ = NULL;
    }
    if (STRING_CLAZZ != NULL) {
        env->DeleteGlobalRef(STRING_CLAZZ);
        STRING_CLAZZ = NULL;
    }
}

static jmethodID _of = 0;
static jmethodID _compute = 0;
static jmethodID _coeff = 0;
static jmethodID _initStyle = 0;
static jmethodID _initOne = 0;

jobject JSE_LMPPAIR::newJObject(JNIEnv *env, char *arg, void *cPtr) {
    jobject result = NULL;

    jstring jarg = env->NewStringUTF(arg);
    if (_of || (_of = env->GetStaticMethodID(LMPPAIR_CLAZZ, "of", "(Ljava/lang/String;J)Ljse/lmp/LmpPlugin$Pair;"))) {
        result = env->CallStaticObjectMethod(LMPPAIR_CLAZZ, _of, jarg, (jlong)(intptr_t)cPtr);
    }
    env->DeleteLocalRef(jarg);
    
    return result;
}

void JSE_LMPPAIR::compute(JNIEnv *env, jobject self, int eflag, int vflag) {
    if (_compute || (_compute = env->GetMethodID(LMPPAIR_CLAZZ, "compute", "(ZZ)V"))) {
        env->CallVoidMethod(self, _compute, eflag ? JNI_TRUE : JNI_FALSE, vflag ? JNI_TRUE : JNI_FALSE);
    }
}
void JSE_LMPPAIR::coeff(JNIEnv *env, jobject self, int nargs, char **args) {
    jobjectArray jargs = env->NewObjectArray(nargs, STRING_CLAZZ, NULL);
    for (int i = 0; i < nargs; ++i) {
        jstring str = env->NewStringUTF(args[i]);
        env->SetObjectArrayElement(jargs, i, str);
        env->DeleteLocalRef(str);
    }
    if (_coeff || (_coeff = env->GetMethodID(LMPPAIR_CLAZZ, "coeff", "([Ljava/lang/String;)V"))) {
        env->CallVoidMethod(self, _coeff, jargs);
    }
    env->DeleteLocalRef(jargs);
}
void JSE_LMPPAIR::initStyle(JNIEnv *env, jobject self) {
    if (_initStyle || (_initStyle = env->GetMethodID(LMPPAIR_CLAZZ, "initStyle", "()V"))) {
        env->CallVoidMethod(self, _initStyle);
    }
}
double JSE_LMPPAIR::initOne(JNIEnv *env, jobject self, int i, int j) {
    if (_initOne || (_initOne = env->GetMethodID(LMPPAIR_CLAZZ, "initOne", "(II)D"))) {
        return env->CallDoubleMethod(self, _initOne, i, j);
    }
    return -1.0;
}
