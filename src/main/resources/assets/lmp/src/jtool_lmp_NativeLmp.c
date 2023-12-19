#include <stdlib.h>
#include <string.h>

#include "lammps/library.h"
#include "jtool_lmp_NativeLmp.h"


/** utils */
char **parseArgs(JNIEnv *aEnv, jobjectArray aArgs, int *rLen) {
    jsize tLen = (*aEnv)->GetArrayLength(aEnv, aArgs);
    char **sArgs = (char**)calloc(tLen+1, sizeof(char*));
    
    for (jsize i = 0; i < tLen; i++) {
        jstring jc = (jstring)(*aEnv)->GetObjectArrayElement(aEnv, aArgs, i);
        const char *s = (*aEnv)->GetStringUTFChars(aEnv, jc, NULL);
#ifdef WIN32
        sArgs[i] = _strdup(s);
#elif _WIN64
        sArgs[i] = _strdup(s);
#elif _WIN32
        sArgs[i] = _strdup(s);
#elif __unix__
        sArgs[i] = strdup(s);
#elif __linux__
        sArgs[i] = strdup(s);
#endif
        (*aEnv)->ReleaseStringUTFChars(aEnv, jc, s);
        (*aEnv)->DeleteLocalRef(aEnv, jc);
    }
    
    *rLen = tLen;
    return sArgs;
}
void freeArgs(char **aArgs, int aLen) {
    for(int i = 0; i < aLen; i++) free(aArgs[i]);
    free(aArgs);
}


JNIEXPORT jlong JNICALL Java_jtool_lmp_NativeLmp_lammpsOpen_1___3Ljava_lang_String_2JJ(JNIEnv *aEnv, jclass aClazz, jobjectArray aArgs, jlong aComm, jlong aPtr) {
    int tLen;
    char **sArgs = parseArgs(aEnv, aArgs, &tLen);
#if defined(LAMMPS_LIB_MPI)
    MPI_Comm tComm = (MPI_Comm) (intptr_t) aComm;
    void *tLmpPtr = lammps_open(tLen, sArgs, tComm, (void *)aPtr);
#else
    void *tLmpPtr = lammps_open_no_mpi(tLen, sArgs, (void *)aPtr);
#endif
    freeArgs(sArgs, tLen);
    return (intptr_t)tLmpPtr;
}

JNIEXPORT jlong JNICALL Java_jtool_lmp_NativeLmp_lammpsOpen_1___3Ljava_lang_String_2J(JNIEnv *aEnv, jclass aClazz, jobjectArray aArgs, jlong aPtr) {
    int tLen;
    char **sArgs = parseArgs(aEnv, aArgs, &tLen);
#if defined(LAMMPS_LIB_MPI)
    void *tLmpPtr = lammps_open(tLen, sArgs, MPI_COMM_WORLD, (void *)aPtr);
#else
    void *tLmpPtr = lammps_open_no_mpi(tLen, sArgs, (void *)aPtr);
#endif
    freeArgs(sArgs, tLen);
    return (intptr_t)tLmpPtr;
}

JNIEXPORT jint JNICALL Java_jtool_lmp_NativeLmp_lammpsVersion_1(JNIEnv *aEnv, jclass aClazz, jlong aLmpPtr) {
    return lammps_version((void *)aLmpPtr);
}

JNIEXPORT void JNICALL Java_jtool_lmp_NativeLmp_lammpsClose_1(JNIEnv *aEnv, jclass aClazz, jlong aLmpPtr) {
    lammps_close((void *)aLmpPtr);
}

