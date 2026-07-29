#include "jse_gpu_CudaNeighborListGetter.h"

#include <stdint.h>

namespace JSE_CUDANL {

static constexpr double JSE_DBL_EPSILON = 1.0e-12;
static constexpr float JSE_FLT_EPSILON = 1.0e-5f;

static __device__ inline float mixed(
    const float ax, const float ay, const float az,
    const float bx, const float by, const float bz,
    const float cx, const float cy, const float cz) {
    
    return (ay*bz - by*az)*cx + (az*bx - bz*ax)*cy + (ax*by - bx*ay)*cz;
}

static __device__ inline void toDirect(
    float &x, float &y, float &z,
    const float ax, const float ay, const float az,
    const float bx, const float by, const float bz,
    const float cx, const float cy, const float cz) {
    
    const float vol = mixed(ax, ay, az, bx, by, bz, cx, cy, cz);
    float x0 = x, y0 = y, z0 = z;
    float x1 = mixed(bx, by, bz, cx, cy, cz, x0, y0, z0) / vol;
    float y1 = mixed(cx, cy, cz, ax, ay, az, x0, y0, z0) / vol;
    float z1 = mixed(ax, ay, az, bx, by, bz, x0, y0, z0) / vol;
    
    x0 = rint(x1); y0 = rint(y1); z0 = rint(z1);
    x = (abs(x1-x0) < JSE_FLT_EPSILON) ? x0 : x1;
    y = (abs(y1-y0) < JSE_FLT_EPSILON) ? y0 : y1;
    z = (abs(z1-z0) < JSE_FLT_EPSILON) ? z0 : z1;
}

static __device__ __host__ inline int cellValid(
    const int sliceX, const int sliceY, const int sliceZ,
    const int i, const int j, const int k) {
    if (i>=-1 && i<=sliceX && j>=-1 && j<=sliceY && k>=-1 && k<=sliceZ) {
        return JNI_TRUE;
    }
    return JNI_FALSE;
}
static __device__ __host__ inline int cellIndex(
    const int sliceX, const int sliceY, const int sliceZ,
    const int i, const int j, const int k) {
    return (i+1) + (sliceX+2)*(j+1) + (sliceX+2)*(sliceY+2)*(k+1);
}
static __device__ __host__ inline int cellGhost(
    const int sliceX, const int sliceY, const int sliceZ,
    const int i, const int j, const int k) {
    return (i==-1 || i==sliceX || j==-1 || j==sliceY || k==-1 || k==sliceZ);
}
static __device__ inline int cellIndex(
    const int sliceX, const int sliceY, const int sliceZ,
    const int i, const int j, const int k, int *error) {
#ifdef JSE_DEBUG
    if (!cellValid(sliceX, sliceY, sliceZ, i, j, k)) {
        atomicAdd(error, 1);
    }
#endif
    return cellIndex(sliceX, sliceY, sliceZ, i, j, k);
}
static __device__ inline int cellGhost(
    const int sliceX, const int sliceY, const int sliceZ,
    const int i, const int j, const int k, int *error) {
#ifdef JSE_DEBUG
    if (!cellValid(sliceX, sliceY, sliceZ, i, j, k)) {
        atomicAdd(error, 1);
    }
#endif
    return cellGhost(sliceX, sliceY, sliceZ, i, j, k);
}

template <int PRISM>
static __global__ void buildCellsKernel(const int nlocalghost,
    const float ax, const float ay, const float az,
    const float bx, const float by, const float bz,
    const float cx, const float cy, const float cz,
    const float xlo, const float ylo, const float zlo,
    const float *posX, const float *posY, const float *posZ,
    const int sliceX, const int sliceY, const int sliceZ,
    int **cells, int *cellSize, int localCellCapacity, int ghostCellCapacity, int *error) {
    
    const int idx = (int)(blockIdx.x * blockDim.x + threadIdx.x);
    if (idx >= nlocalghost) return;
    
    float x = posX[idx]-xlo, y = posY[idx]-ylo, z = posZ[idx]-zlo;
    if (PRISM) {
        toDirect(x, y, z, ax, ay, az, bx, by, bz, cx, cy, cz);
    } else {
        x /= ax; y /= by; z /= cz;
    }
    const int i = x<0 ? (-1) : (x>=1 ? sliceX : (int)(x * (float)sliceX));
    const int j = y<0 ? (-1) : (y>=1 ? sliceY : (int)(y * (float)sliceY));
    const int k = z<0 ? (-1) : (z>=1 ? sliceZ : (int)(z * (float)sliceZ));
    
    const int cidx = cellIndex(sliceX, sliceY, sliceZ, i, j, k, error);
    const int ci = atomicAdd(cellSize+cidx, 1);
    const int cellCap = cellGhost(sliceX, sliceY, sliceZ, i, j, k, error) ? ghostCellCapacity : localCellCapacity;
    if (ci < cellCap) {
        cells[cidx][ci] = idx;
    }
}

#define JSE_CUDANL_cell2nl_ijk {\
    const int cidx = cellIndex(sliceX, sliceY, sliceZ, i, j, k, error); \
    const int *cell = cells[cidx]; \
    const int csize = cellSize[cidx]; \
    for (int ci = 0; ci < csize; ++ci) { \
        const int jdx = cell[ci]; \
        if (jdx != idx) continue; \
        const float dx = posX[jdx] - xi; \
        const float dy = posY[jdx] - yi; \
        const float dz = posZ[jdx] - zi; \
        const float rsq = dx*dx + dy*dy + dz*dz; \
        if (rsq >= rcutsq) continue; \
        if (nlsizei < nlCapacity) { \
            nl[nlsizei*nlocal + idx] = jdx; \
        } \
        ++nlsizei; \
    } \
}

#define JSE_CUDANL_cell2nl(_i, _j, _k) if (cellValid(sliceX, sliceY, sliceZ, _i, _j, _k)) { \
    const int cidx = cellIndex(sliceX, sliceY, sliceZ, _i, _j, _k, error); \
    const int *cell = cells[cidx]; \
    const int csize = cellSize[cidx]; \
    for (int ci = 0; ci < csize; ++ci) { \
        const int jdx = cell[ci]; \
        const float dx = posX[jdx] - xi; \
        const float dy = posY[jdx] - yi; \
        const float dz = posZ[jdx] - zi; \
        const float rsq = dx*dx + dy*dy + dz*dz; \
        if (rsq >= rcutsq) continue; \
        if (nlsizei < nlCapacity) { \
            nl[nlsizei*nlocal + idx] = jdx; \
        } \
        ++nlsizei; \
    } \
}

template <int PRISM>
static __global__ void buildNlKernel(const int nlocal,
    const float ax, const float ay, const float az,
    const float bx, const float by, const float bz,
    const float cx, const float cy, const float cz,
    const float xlo, const float ylo, const float zlo,
    const float *posX, const float *posY, const float *posZ,
    const int sliceX, const int sliceY, const int sliceZ,
    const int **cells, const int *cellSize, const float rcutsq,
    int *nl, int *nlSize, const int nlCapacity, int *error) {

    const int idx = (int)(blockIdx.x * blockDim.x + threadIdx.x);
    if (idx >= nlocal) return;
    
    const float xi = posX[idx], yi = posY[idx], zi = posZ[idx];
    float x = xi-xlo, y = yi-ylo, z = zi-zlo;
    if (PRISM) {
        toDirect(x, y, z, ax, ay, az, bx, by, bz, cx, cy, cz);
    } else {
        x /= ax; y /= by; z /= cz;
    }
    const int i = x<0 ? (-1) : (x>=1 ? sliceX : (int)(x * (float)sliceX));
    const int j = y<0 ? (-1) : (y>=1 ? sliceY : (int)(y * (float)sliceY));
    const int k = z<0 ? (-1) : (z>=1 ? sliceZ : (int)(z * (float)sliceZ));
    
    int nlsizei = 0;
    
    JSE_CUDANL_cell2nl_ijk;
    JSE_CUDANL_cell2nl((i  ), (j  ), (k+1));
    JSE_CUDANL_cell2nl((i  ), (j  ), (k-1));
    JSE_CUDANL_cell2nl((i  ), (j+1), (k  ));
    JSE_CUDANL_cell2nl((i  ), (j+1), (k+1));
    JSE_CUDANL_cell2nl((i  ), (j+1), (k-1));
    JSE_CUDANL_cell2nl((i  ), (j-1), (k  ));
    JSE_CUDANL_cell2nl((i  ), (j-1), (k+1));
    JSE_CUDANL_cell2nl((i  ), (j-1), (k-1));
    JSE_CUDANL_cell2nl((i+1), (j  ), (k  ));
    JSE_CUDANL_cell2nl((i+1), (j  ), (k+1));
    JSE_CUDANL_cell2nl((i+1), (j  ), (k-1));
    JSE_CUDANL_cell2nl((i+1), (j+1), (k  ));
    JSE_CUDANL_cell2nl((i+1), (j+1), (k+1));
    JSE_CUDANL_cell2nl((i+1), (j+1), (k-1));
    JSE_CUDANL_cell2nl((i+1), (j-1), (k  ));
    JSE_CUDANL_cell2nl((i+1), (j-1), (k+1));
    JSE_CUDANL_cell2nl((i+1), (j-1), (k-1));
    JSE_CUDANL_cell2nl((i-1), (j  ), (k  ));
    JSE_CUDANL_cell2nl((i-1), (j  ), (k+1));
    JSE_CUDANL_cell2nl((i-1), (j  ), (k-1));
    JSE_CUDANL_cell2nl((i-1), (j+1), (k  ));
    JSE_CUDANL_cell2nl((i-1), (j+1), (k+1));
    JSE_CUDANL_cell2nl((i-1), (j+1), (k-1));
    JSE_CUDANL_cell2nl((i-1), (j-1), (k  ));
    JSE_CUDANL_cell2nl((i-1), (j-1), (k+1));
    JSE_CUDANL_cell2nl((i-1), (j-1), (k-1));
    
    nlSize[idx] = nlsizei;
}

}


extern "C" {

JNIEXPORT jint JNICALL Java_jse_gpu_CudaNeighborListGetter_initCells0(
    JNIEnv *aEnv, jclass aClazz, jint sliceX, jint sliceY, jint sliceZ,
    jlong cellsTot, jlong cells, jlong cellsCpu,
    jint localCellCapacity, jint ghostCellCapacity) {
    
    int *tCellsPtr = (int *)(intptr_t)cellsTot;
    int **rCellsCpu = (int **)(intptr_t)cellsCpu;
    
    for (int k = -1; k <= sliceZ; ++k) for (int j = -1; j <= sliceY; ++j) for (int i = -1; i <= sliceX; ++i) {
        const int cidx = JSE_CUDANL::cellIndex(sliceX, sliceY, sliceZ, i, j, k);
        rCellsCpu[cidx] = tCellsPtr;
        const int cellCap = JSE_CUDANL::cellGhost(sliceX, sliceY, sliceZ, i, j, k) ? ghostCellCapacity : localCellCapacity;
        tCellsPtr += cellCap;
    }
    
    cudaError_t tErr = cudaMemcpy((int **)(intptr_t)cells, rCellsCpu, (sliceX+2)*(sliceY+2)*(sliceZ+2)*sizeof(int *), cudaMemcpyHostToDevice);
    return tErr;
}


JNIEXPORT int JNICALL Java_jse_gpu_CudaNeighborListGetter_buildCells0(
    JNIEnv *aEnv, jclass aClazz, jint aBlockSize, jint nlocal, jint nghost,
    jboolean aPrism, jfloat ax, jfloat ay, jfloat az,
    jfloat bx, jfloat by, jfloat bz, jfloat cx, jfloat cy, jfloat cz,
    jfloat xlo, jfloat ylo, jfloat zlo, jlong posX, jlong posY, jlong posZ,
    jint sliceX, jint sliceY, jint sliceZ,
    jlong cells, jlong cellSize, jint localCellCapacity, jint ghostCellCapacity,
    jlong errorGpu, jlong errorCpu) {
    
    const int nlocalghost = nlocal + nghost;
    const int tGridSize = (nlocalghost + aBlockSize-1) / aBlockSize;
    
    cudaError_t tErr;
#ifdef JSE_DEBUG
    tErr = cudaMemset((int *)(intptr_t)errorGpu, 0, sizeof(int));
    if (tErr!=cudaSuccess) return (int)tErr;
#endif
    
    tErr = cudaMemset((int *)(intptr_t)cellSize, 0, (sliceX+2)*(sliceY+2)*(sliceZ+2)*sizeof(int));
    if (tErr!=cudaSuccess) return (int)tErr;
    
    if (aPrism) {
        JSE_CUDANL::buildCellsKernel<JNI_TRUE><<<tGridSize, (int)aBlockSize>>>(nlocalghost,
            ax, ay, az, bx, by, bz, cx, cy, cz,
            xlo, ylo, zlo, (float *)(intptr_t)posX, (float *)(intptr_t)posY, (float *)(intptr_t)posZ,
            (int)sliceX, (int)sliceY, (int)sliceZ,
            (int **)(intptr_t)cells, (int *)(intptr_t)cellSize, (int)localCellCapacity, (int)ghostCellCapacity,
            (int *)(intptr_t)errorGpu
        );
    } else {
        JSE_CUDANL::buildCellsKernel<JNI_FALSE><<<tGridSize, (int)aBlockSize>>>(nlocalghost,
            ax, ay, az, bx, by, bz, cx, cy, cz,
            xlo, ylo, zlo, (float *)(intptr_t)posX, (float *)(intptr_t)posY, (float *)(intptr_t)posZ,
            (int)sliceX, (int)sliceY, (int)sliceZ,
            (int **)(intptr_t)cells, (int *)(intptr_t)cellSize, (int)localCellCapacity, (int)ghostCellCapacity,
            (int *)(intptr_t)errorGpu
        );
    }
    tErr = cudaDeviceSynchronize();
    if (tErr!=cudaSuccess) return (int)tErr;
    
#ifdef JSE_DEBUG
    tErr = cudaMemcpy((int *)(intptr_t)errorCpu, (int *)(intptr_t)errorGpu, sizeof(int), cudaMemcpyDeviceToHost);
    if (tErr!=cudaSuccess) return (int)tErr;
#else
    *((int *)(intptr_t)errorCpu) = 0;
#endif
    
    return cudaSuccess;
}

JNIEXPORT int JNICALL Java_jse_gpu_CudaNeighborListGetter_buildNl0(
    JNIEnv *aEnv, jclass aClazz, jint aBlockSize, jint nlocal,
    jboolean aPrism, jfloat ax, jfloat ay, jfloat az,
    jfloat bx, jfloat by, jfloat bz, jfloat cx, jfloat cy, jfloat cz,
    jfloat xlo, jfloat ylo, jfloat zlo, jlong posX, jlong posY, jlong posZ,
    jint sliceX, jint sliceY, jint sliceZ,
    jlong cells, jlong cellSize, jfloat rcutsq,
    jlong nl, jlong nlSize, jint nlCapacity,
    jlong errorGpu, jlong errorCpu) {
    
    const int tGridSize = (nlocal + aBlockSize-1) / aBlockSize;
    
    cudaError_t tErr;
#ifdef JSE_DEBUG
    tErr = cudaMemset((int *)(intptr_t)errorGpu, 0, sizeof(int));
    if (tErr!=cudaSuccess) return (int)tErr;
#endif
    
    tErr = cudaMemset((int *)(intptr_t)nlSize, 0, (sliceX+2)*(sliceY+2)*(sliceZ+2)*sizeof(int));
    if (tErr!=cudaSuccess) return (int)tErr;
    
    if (aPrism) {
        JSE_CUDANL::buildNlKernel<JNI_TRUE><<<tGridSize, (int)aBlockSize>>>(nlocal,
            ax, ay, az, bx, by, bz, cx, cy, cz,
            xlo, ylo, zlo, (float *)(intptr_t)posX, (float *)(intptr_t)posY, (float *)(intptr_t)posZ,
            (int)sliceX, (int)sliceY, (int)sliceZ,
            (const int **)(intptr_t)cells, (int *)(intptr_t)cellSize, rcutsq,
            (int *)(intptr_t)nl, (int *)(intptr_t)nlSize, (int)nlCapacity,
            (int *)(intptr_t)errorGpu
        );
    } else {
        JSE_CUDANL::buildNlKernel<JNI_FALSE><<<tGridSize, (int)aBlockSize>>>(nlocal,
            ax, ay, az, bx, by, bz, cx, cy, cz,
            xlo, ylo, zlo, (float *)(intptr_t)posX, (float *)(intptr_t)posY, (float *)(intptr_t)posZ,
            (int)sliceX, (int)sliceY, (int)sliceZ,
            (const int **)(intptr_t)cells, (int *)(intptr_t)cellSize, rcutsq,
            (int *)(intptr_t)nl, (int *)(intptr_t)nlSize, (int)nlCapacity,
            (int *)(intptr_t)errorGpu
        );
    }
    tErr = cudaDeviceSynchronize();
    if (tErr!=cudaSuccess) return (int)tErr;
    
#ifdef JSE_DEBUG
    tErr = cudaMemcpy((int *)(intptr_t)errorCpu, (int *)(intptr_t)errorGpu, sizeof(int), cudaMemcpyDeviceToHost);
    if (tErr!=cudaSuccess) return (int)tErr;
#else
    *((int *)(intptr_t)errorCpu) = 0;
#endif
    
    return cudaSuccess;
}

}
