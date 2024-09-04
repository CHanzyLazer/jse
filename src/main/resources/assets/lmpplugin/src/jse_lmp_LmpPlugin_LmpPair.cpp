#include "jse_lmp_LmpPlugin_LmpPair.h"
#include "pair_jse.h"

extern "C" {

using namespace LAMMPS_NS;

JNIEXPORT void JNICALL Java_jse_lmp_LmpPlugin_00024LmpPair_neighborRequestDefault_1(JNIEnv *aEnv, jclass aClazz, jlong aPairPtr) {
    ((PairJSE *)(intptr_t)aPairPtr)->neighborRequestDefault();
}
JNIEXPORT void JNICALL Java_jse_lmp_LmpPlugin_00024LmpPair_neighborRequestFull_1(JNIEnv *aEnv, jclass aClazz, jlong aPairPtr) {
    ((PairJSE *)(intptr_t)aPairPtr)->neighborRequestFull();
}
JNIEXPORT void JNICALL Java_jse_lmp_LmpPlugin_00024LmpPair_evInit_1(JNIEnv *aEnv, jclass aClazz, jlong aPairPtr, jboolean eflag, jboolean vflag) {
    ((PairJSE *)(intptr_t)aPairPtr)->evInit(eflag, vflag);
}
JNIEXPORT jlong JNICALL Java_jse_lmp_LmpPlugin_00024LmpPair_atomX_1(JNIEnv *aEnv, jclass aClazz, jlong aPairPtr) {
    return ((PairJSE *)(intptr_t)aPairPtr)->atomX();
}
JNIEXPORT jlong JNICALL Java_jse_lmp_LmpPlugin_00024LmpPair_atomF_1(JNIEnv *aEnv, jclass aClazz, jlong aPairPtr) {
    return ((PairJSE *)(intptr_t)aPairPtr)->atomF();
}
JNIEXPORT jlong JNICALL Java_jse_lmp_LmpPlugin_00024LmpPair_atomType_1(JNIEnv *aEnv, jclass aClazz, jlong aPairPtr) {
    return ((PairJSE *)(intptr_t)aPairPtr)->atomType();
}
JNIEXPORT jint JNICALL Java_jse_lmp_LmpPlugin_00024LmpPair_atomNlocal_1(JNIEnv *aEnv, jclass aClazz, jlong aPairPtr) {
    return ((PairJSE *)(intptr_t)aPairPtr)->atomNlocal();
}
JNIEXPORT jlong JNICALL Java_jse_lmp_LmpPlugin_00024LmpPair_forceSpecialLj_1(JNIEnv *aEnv, jclass aClazz, jlong aPairPtr) {
    return ((PairJSE *)(intptr_t)aPairPtr)->forceSpecialLj();
}
JNIEXPORT jboolean JNICALL Java_jse_lmp_LmpPlugin_00024LmpPair_forceNewtonPair_1(JNIEnv *aEnv, jclass aClazz, jlong aPairPtr) {
    return ((PairJSE *)(intptr_t)aPairPtr)->forceNewtonPair();
}
JNIEXPORT jint JNICALL Java_jse_lmp_LmpPlugin_00024LmpPair_listInum_1(JNIEnv *aEnv, jclass aClazz, jlong aPairPtr) {
    return ((PairJSE *)(intptr_t)aPairPtr)->listInum();
}
JNIEXPORT jlong JNICALL Java_jse_lmp_LmpPlugin_00024LmpPair_listIlist_1(JNIEnv *aEnv, jclass aClazz, jlong aPairPtr) {
    return ((PairJSE *)(intptr_t)aPairPtr)->listIlist();
}
JNIEXPORT jlong JNICALL Java_jse_lmp_LmpPlugin_00024LmpPair_listNumneigh_1(JNIEnv *aEnv, jclass aClazz, jlong aPairPtr) {
    return ((PairJSE *)(intptr_t)aPairPtr)->listNumneigh();
}
JNIEXPORT jlong JNICALL Java_jse_lmp_LmpPlugin_00024LmpPair_listFirstneigh_1(JNIEnv *aEnv, jclass aClazz, jlong aPairPtr) {
    return ((PairJSE *)(intptr_t)aPairPtr)->listFirstneigh();
}
JNIEXPORT jdouble JNICALL Java_jse_lmp_LmpPlugin_00024LmpPair_cutsq_1(JNIEnv *aEnv, jclass aClazz, jlong aPairPtr, jint i, jint j) {
    return ((PairJSE *)(intptr_t)aPairPtr)->cutsq_(i, j);
}
JNIEXPORT void JNICALL Java_jse_lmp_LmpPlugin_00024LmpPair_evTally_1(JNIEnv *aEnv, jclass aClazz, jlong aPairPtr, jint i, jint j, jint nlocal, jboolean newtonPair, jdouble evdwl, jdouble ecoul, jdouble fpair, jdouble delx, jdouble dely, jdouble delz) {
    ((PairJSE *)(intptr_t)aPairPtr)->evTally(i, j, nlocal, newtonPair, evdwl, ecoul, fpair, delx, dely, delz);
}
JNIEXPORT jboolean JNICALL Java_jse_lmp_LmpPlugin_00024LmpPair_vflagFdotr_1(JNIEnv *aEnv, jclass aClazz, jlong aPairPtr) {
    return ((PairJSE *)(intptr_t)aPairPtr)->vflagFdotr();
}
JNIEXPORT void JNICALL Java_jse_lmp_LmpPlugin_00024LmpPair_virialFdotrCompute_1(JNIEnv *aEnv, jclass aClazz, jlong aPairPtr) {
    ((PairJSE *)(intptr_t)aPairPtr)->virialFdotrCompute();
}

}
