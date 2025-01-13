#include "jse_lmp_LmpPlugin_Fix.h"
#include "fix_jse.h"

extern "C" {

using namespace LAMMPS_NS;

JNIEXPORT void JNICALL Java_jse_lmp_LmpPlugin_00024Fix_setForceReneighbor_1(JNIEnv *aEnv, jclass aClazz, jlong aFixPtr, jboolean aFlag) {
    ((FixJSE *)(intptr_t)aFixPtr)->setForceReneighbor(aFlag);
}
JNIEXPORT void JNICALL Java_jse_lmp_LmpPlugin_00024Fix_setNextReneighbor_1(JNIEnv *aEnv, jclass aClazz, jlong aFixPtr, jlong aTimestep) {
    ((FixJSE *)(intptr_t)aFixPtr)->setNextReneighbor(aTimestep);
}
JNIEXPORT jlong JNICALL Java_jse_lmp_LmpPlugin_00024Fix_nextReneighbor_1(JNIEnv *aEnv, jclass aClazz, jlong aFixPtr) {
    return ((FixJSE *)(intptr_t)aFixPtr)->nextReneighbor();
}
JNIEXPORT void JNICALL Java_jse_lmp_LmpPlugin_00024Fix_setNevery_1(JNIEnv *aEnv, jclass aClazz, jlong aFixPtr, jint aNevery) {
    ((FixJSE *)(intptr_t)aFixPtr)->setNevery(aNevery);
}
JNIEXPORT void JNICALL Java_jse_lmp_LmpPlugin_00024Fix_setEnergyGlobalFlag_1(JNIEnv *aEnv, jclass aClazz, jlong aFixPtr, jboolean aFlag) {
    ((FixJSE *)(intptr_t)aFixPtr)->setEnergyGlobalFlag(aFlag);
}
JNIEXPORT void JNICALL Java_jse_lmp_LmpPlugin_00024Fix_setEnergyPeratomFlag_1(JNIEnv *aEnv, jclass aClazz, jlong aFixPtr, jboolean aFlag) {
    ((FixJSE *)(intptr_t)aFixPtr)->setEnergyPeratomFlag(aFlag);
}
JNIEXPORT void JNICALL Java_jse_lmp_LmpPlugin_00024Fix_setVirialGlobalFlag_1(JNIEnv *aEnv, jclass aClazz, jlong aFixPtr, jboolean aFlag) {
    ((FixJSE *)(intptr_t)aFixPtr)->setVirialGlobalFlag(aFlag);
}
JNIEXPORT void JNICALL Java_jse_lmp_LmpPlugin_00024Fix_setVirialPeratomFlag_1(JNIEnv *aEnv, jclass aClazz, jlong aFixPtr, jboolean aFlag) {
    ((FixJSE *)(intptr_t)aFixPtr)->setVirialPeratomFlag(aFlag);
}
JNIEXPORT void JNICALL Java_jse_lmp_LmpPlugin_00024Fix_setTimeDepend_1(JNIEnv *aEnv, jclass aClazz, jlong aFixPtr, jboolean aFlag) {
    ((FixJSE *)(intptr_t)aFixPtr)->setTimeDepend(aFlag);
}
JNIEXPORT void JNICALL Java_jse_lmp_LmpPlugin_00024Fix_setDynamicGroupAllow_1(JNIEnv *aEnv, jclass aClazz, jlong aFixPtr, jboolean aFlag) {
    ((FixJSE *)(intptr_t)aFixPtr)->setDynamicGroupAllow(aFlag);
}
JNIEXPORT void JNICALL Java_jse_lmp_LmpPlugin_00024Fix_setScalarFlag_1(JNIEnv *aEnv, jclass aClazz, jlong aFixPtr, jboolean aFlag) {
    ((FixJSE *)(intptr_t)aFixPtr)->setScalarFlag(aFlag);
}
JNIEXPORT void JNICALL Java_jse_lmp_LmpPlugin_00024Fix_setVectorFlag_1(JNIEnv *aEnv, jclass aClazz, jlong aFixPtr, jboolean aFlag) {
    ((FixJSE *)(intptr_t)aFixPtr)->setVectorFlag(aFlag);
}
JNIEXPORT void JNICALL Java_jse_lmp_LmpPlugin_00024Fix_setArrayFlag_1(JNIEnv *aEnv, jclass aClazz, jlong aFixPtr, jboolean aFlag) {
    ((FixJSE *)(intptr_t)aFixPtr)->setArrayFlag(aFlag);
}
JNIEXPORT void JNICALL Java_jse_lmp_LmpPlugin_00024Fix_setSizeVector_1(JNIEnv *aEnv, jclass aClazz, jlong aFixPtr, jint aSize) {
    ((FixJSE *)(intptr_t)aFixPtr)->setSizeVector(aSize);
}
JNIEXPORT void JNICALL Java_jse_lmp_LmpPlugin_00024Fix_setSizeArrayRows_1(JNIEnv *aEnv, jclass aClazz, jlong aFixPtr, jint aRowNum) {
    ((FixJSE *)(intptr_t)aFixPtr)->setSizeArrayRows(aRowNum);
}
JNIEXPORT void JNICALL Java_jse_lmp_LmpPlugin_00024Fix_setSizeArrayCols_1(JNIEnv *aEnv, jclass aClazz, jlong aFixPtr, jint aColNum) {
    ((FixJSE *)(intptr_t)aFixPtr)->setSizeArrayCols(aColNum);
}
JNIEXPORT void JNICALL Java_jse_lmp_LmpPlugin_00024Fix_setGlobalFreq_1(JNIEnv *aEnv, jclass aClazz, jlong aFixPtr, jint aFreq) {
    ((FixJSE *)(intptr_t)aFixPtr)->setGlobalFreq(aFreq);
}
JNIEXPORT void JNICALL Java_jse_lmp_LmpPlugin_00024Fix_setExtscalar_1(JNIEnv *aEnv, jclass aClazz, jlong aFixPtr, jboolean aFlag) {
    ((FixJSE *)(intptr_t)aFixPtr)->setExtscalar(aFlag);
}
JNIEXPORT void JNICALL Java_jse_lmp_LmpPlugin_00024Fix_setExtvector_1(JNIEnv *aEnv, jclass aClazz, jlong aFixPtr, jboolean aFlag) {
    ((FixJSE *)(intptr_t)aFixPtr)->setExtvector(aFlag);
}
JNIEXPORT void JNICALL Java_jse_lmp_LmpPlugin_00024Fix_setExtarray_1(JNIEnv *aEnv, jclass aClazz, jlong aFixPtr, jboolean aFlag) {
    ((FixJSE *)(intptr_t)aFixPtr)->setExtarray(aFlag);
}
JNIEXPORT jlong JNICALL Java_jse_lmp_LmpPlugin_00024Fix_atomX_1(JNIEnv *aEnv, jclass aClazz, jlong aFixPtr) {
    return ((FixJSE *)(intptr_t)aFixPtr)->atomX();
}
JNIEXPORT jlong JNICALL Java_jse_lmp_LmpPlugin_00024Fix_atomF_1(JNIEnv *aEnv, jclass aClazz, jlong aFixPtr) {
    return ((FixJSE *)(intptr_t)aFixPtr)->atomF();
}
JNIEXPORT jlong JNICALL Java_jse_lmp_LmpPlugin_00024Fix_atomType_1(JNIEnv *aEnv, jclass aClazz, jlong aFixPtr) {
    return ((FixJSE *)(intptr_t)aFixPtr)->atomType();
}
JNIEXPORT jint JNICALL Java_jse_lmp_LmpPlugin_00024Fix_atomNtypes_1(JNIEnv *aEnv, jclass aClazz, jlong aFixPtr) {
    return ((FixJSE *)(intptr_t)aFixPtr)->atomNtypes();
}
JNIEXPORT jint JNICALL Java_jse_lmp_LmpPlugin_00024Fix_atomNlocal_1(JNIEnv *aEnv, jclass aClazz, jlong aFixPtr) {
    return ((FixJSE *)(intptr_t)aFixPtr)->atomNlocal();
}
JNIEXPORT jint JNICALL Java_jse_lmp_LmpPlugin_00024Fix_atomNghost_1(JNIEnv *aEnv, jclass aClazz, jlong aFixPtr) {
    return ((FixJSE *)(intptr_t)aFixPtr)->atomNghost();
}
JNIEXPORT jdouble JNICALL Java_jse_lmp_LmpPlugin_00024Fix_forceBoltz_1(JNIEnv *aEnv, jclass aClazz, jlong aFixPtr) {
    return ((FixJSE *)(intptr_t)aFixPtr)->forceBoltz();
}
JNIEXPORT jdouble JNICALL Java_jse_lmp_LmpPlugin_00024Fix_dt_1(JNIEnv *aEnv, jclass aClazz, jlong aFixPtr) {
    return ((FixJSE *)(intptr_t)aFixPtr)->dt();
}
JNIEXPORT jlong JNICALL Java_jse_lmp_LmpPlugin_00024Fix_ntimestep_1(JNIEnv *aEnv, jclass aClazz, jlong aFixPtr) {
    return ((FixJSE *)(intptr_t)aFixPtr)->ntimestep();
}
JNIEXPORT jlong JNICALL Java_jse_lmp_LmpPlugin_00024Fix_firststep_1(JNIEnv *aEnv, jclass aClazz, jlong aFixPtr) {
    return ((FixJSE *)(intptr_t)aFixPtr)->firststep();
}
JNIEXPORT jlong JNICALL Java_jse_lmp_LmpPlugin_00024Fix_laststep_1(JNIEnv *aEnv, jclass aClazz, jlong aFixPtr) {
    return ((FixJSE *)(intptr_t)aFixPtr)->laststep();
}
JNIEXPORT jlong JNICALL Java_jse_lmp_LmpPlugin_00024Fix_beginstep_1(JNIEnv *aEnv, jclass aClazz, jlong aFixPtr) {
    return ((FixJSE *)(intptr_t)aFixPtr)->beginstep();
}
JNIEXPORT jlong JNICALL Java_jse_lmp_LmpPlugin_00024Fix_endstep_1(JNIEnv *aEnv, jclass aClazz, jlong aFixPtr) {
    return ((FixJSE *)(intptr_t)aFixPtr)->endstep();
}
JNIEXPORT jint JNICALL Java_jse_lmp_LmpPlugin_00024Fix_commMe_1(JNIEnv *aEnv, jclass aClazz, jlong aFixPtr) {
    return ((FixJSE *)(intptr_t)aFixPtr)->commMe();
}
JNIEXPORT jint JNICALL Java_jse_lmp_LmpPlugin_00024Fix_commNprocs_1(JNIEnv *aEnv, jclass aClazz, jlong aFixPtr) {
    return ((FixJSE *)(intptr_t)aFixPtr)->commNprocs();
}
JNIEXPORT jstring JNICALL Java_jse_lmp_LmpPlugin_00024Fix_unitStyle_1(JNIEnv *aEnv, jclass aClazz, jlong aFixPtr) {
    return ((FixJSE *)(intptr_t)aFixPtr)->unitStyle();
}

}
