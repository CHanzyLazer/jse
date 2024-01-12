package test.mpi

import jtool.code.CS


println("Mem of ${CS.Slurm.PROCID}: ${Runtime.runtime.maxMemory()}");

def list = new LinkedList();
while (true) list.add(new byte[512]);

