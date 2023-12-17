package test.system

import jtool.code.UT

UT.Timer.pbar(10);
for (i in 0..<10) {
    sleep(500);
    UT.Timer.pbar();
}

