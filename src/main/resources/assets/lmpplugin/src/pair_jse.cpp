#include "jniutil.h"
#include "pair_jse.h"

#include "LmpPair.h"
#include "lammps/atom.h"
#include "lammps/error.h"
#include "lammps/force.h"
#include "lammps/memory.h"
#include "lammps/neigh_list.h"
#include "lammps/neighbor.h"

using namespace LAMMPS_NS;

/* ---------------------------------------------------------------------- */

PairJSE::PairJSE(LAMMPS *lmp) : Pair(lmp) {
    single_enable = 0;
    restartinfo = 0;
    one_coeff = 1;
    manybody_flag = 1;
}

/* ---------------------------------------------------------------------- */

PairJSE::~PairJSE() {
    if (allocated) {
        memory->destroy(setflag);
        memory->destroy(cutsq);
    }
    if (core!=NULL && env!=NULL) {
        env->DeleteGlobalRef(core);
    }
}

/* ---------------------------------------------------------------------- */

void PairJSE::compute(int eflag, int vflag) {
    int i, j, ii, jj, inum, jnum, itype, jtype;
    double xtmp, ytmp, ztmp, delx, dely, delz, evdwl, fpair;
    double rsq, r2inv, r6inv, forcelj, factor_lj;
    int *ilist, *jlist, *numneigh, **firstneigh;

    evdwl = 0.0;
    ev_init(eflag, vflag);

    double **x = atom->x;
    double **f = atom->f;
    int *type = atom->type;
    int nlocal = atom->nlocal;
    double *special_lj = force->special_lj;
    int newton_pair = force->newton_pair;

    inum = list->inum;
    ilist = list->ilist;
    numneigh = list->numneigh;
    firstneigh = list->firstneigh;

    // loop over neighbors of my atoms

    for (ii = 0; ii < inum; ii++) {
        i = ilist[ii];
        xtmp = x[i][0];
        ytmp = x[i][1];
        ztmp = x[i][2];
        itype = type[i];
        jlist = firstneigh[i];
        jnum = numneigh[i];

        for (jj = 0; jj < jnum; jj++) {
            j = jlist[jj];
            factor_lj = special_lj[sbmask(j)];
            j &= NEIGHMASK;

            delx = xtmp - x[j][0];
            dely = ytmp - x[j][1];
            delz = ztmp - x[j][2];
            rsq = delx * delx + dely * dely + delz * delz;
            jtype = type[j];

            if (rsq < cutsq[itype][jtype]) {
                r2inv = 1.0 / rsq;
                r6inv = r2inv * r2inv * r2inv;
                forcelj = r6inv * (lj1[itype][jtype] * r6inv - lj2[itype][jtype]);
                fpair = factor_lj * forcelj * r2inv;

                f[i][0] += delx * fpair;
                f[i][1] += dely * fpair;
                f[i][2] += delz * fpair;
                if (newton_pair || j < nlocal) {
                    f[j][0] -= delx * fpair;
                    f[j][1] -= dely * fpair;
                    f[j][2] -= delz * fpair;
                }

                if (eflag) {
                    evdwl = r6inv * (lj3[itype][jtype] * r6inv - lj4[itype][jtype]) - offset[itype][jtype];
                    evdwl *= factor_lj;
                }

                if (evflag) ev_tally(i, j, nlocal, newton_pair, evdwl, 0.0, fpair, delx, dely, delz);
            }
        }
    }

    if (vflag_fdotr) virial_fdotr_compute();
}


/* ----------------------------------------------------------------------
   allocate all arrays
------------------------------------------------------------------------- */

void PairJSE::allocate() {
    allocated = 1;
    int n = atom->ntypes + 1;
    
    memory->create(setflag, n, n, "pair:setflag");
    for (int i = 1; i < n; i++) for (int j = i; j < n; j++) setflag[i][j] = 0;
    
    memory->create(cutsq, n, n, "pair:cutsq");
}

/* ----------------------------------------------------------------------
   global settings
------------------------------------------------------------------------- */

void PairJSE::settings(int narg, char **arg) {
    if (narg != 1) error->all(FLERR, "Illegal pair_style command");
    
    // init jni env
    if (env == NULL) {
        JavaVM *jvm;
        jsize nVMs;
        JNI_GetCreatedJavaVMs(&jvm, 1, &nVMs);
        if (jvm == NULL) error->all(FLERR, "pair_style jse can not run without jse yet");
        jvm->AttachCurrentThreadAsDaemon((void**)&env, NULL);
        if (env == NULL) error->all(FLERR, "Fail to get jni env");
    }
    // init java LmpPair object
    if (core != NULL) env->DeleteGlobalRef(core);
    jobject obj = JSE_LMPPAIR::newJObject(env, arg[0], this);
    if (obj == NULL) error->all(FLERR, "Fail to create java LmpPair object");
    core = env->NewGlobalRef(obj);
    env->DeleteLocalRef(obj);
}

/* ----------------------------------------------------------------------
   set coeffs for one or more type pairs
------------------------------------------------------------------------- */

void PairJSE::coeff(int narg, char **arg) {
    JSE_LMPPAIR::coeff(env, core, narg, arg);
    if (env->ExceptionCheck()) error->all(FLERR, "Fail to set coeff");
    if (!allocated) allocate();
}

/* ----------------------------------------------------------------------
   init specific to this pair style
------------------------------------------------------------------------- */

void PairJSE::init_style() {
//    neighbor->add_request(this, NeighConst::REQ_DEFAULT);
    JSE_LMPPAIR::initStyle(env, core);
    if (env->ExceptionCheck()) error->all(FLERR, "Fail to init_style");
}

/* ----------------------------------------------------------------------
   init for one type pair i,j and corresponding j,i
------------------------------------------------------------------------- */

double PairJSE::init_one(int i, int j) {
    double cutij = JSE_LMPPAIR::initOne(env, core, i, j);
    if (cutij<=0.0 || env->ExceptionCheck()) error->all(FLERR, "Fail to init_one");
    return cutij;
}
