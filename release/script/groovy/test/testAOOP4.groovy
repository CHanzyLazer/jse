package test

import com.jtool.jobs.StepJobManager
import com.jtool.lmp.Lmpdat
import com.jtool.plot.Plotters


/** 测试计算 AOOP，使用 AOOP 判断结晶并绘制结果 */


// 首先导入 Lmpdat
def data = Lmpdat.read('lmp/data/data-crystal');


new StepJobManager('testAOOP4')
.init {println("0. 使用 q6 判断");}
.doJob {
    // 计算 q6
    def q6;
    try (def mpc = data.getMPC()) {
        q6 = mpc.calAOOP(6);
    }
    
    // 绘制晶体结构
    def q6C = 0.30;
    // 直接获取 xyz 数据
    def dataSTD = data.dataSTD();
    def type = dataSTD['type'];
    // 绘制
    def plt = Plotters.get();
    plt.plot(dataSTD['x'][{int i -> q6[i]<=q6C && type[i]==1}], dataSTD['y'][{int i -> q6[i]<=q6C && type[i]==1}], 'glass-Cu'  ).color(0.8, 0.6, 0.0).lineType('none').markerType('o').markerSize(3);
    plt.plot(dataSTD['x'][{int i -> q6[i]> q6C && type[i]==1}], dataSTD['y'][{int i -> q6[i]> q6C && type[i]==1}], 'crystal-Cu').color(0.5, 0.3, 0.0).lineType('none').markerType('o').markerSize(10);
    plt.plot(dataSTD['x'][{int i -> q6[i]<=q6C && type[i]==2}], dataSTD['y'][{int i -> q6[i]<=q6C && type[i]==2}], 'glass-Zr'  ).color(0.2, 0.6, 0.0).lineType('none').markerType('o').markerSize(4);
    plt.plot(dataSTD['x'][{int i -> q6[i]> q6C && type[i]==2}], dataSTD['y'][{int i -> q6[i]> q6C && type[i]==2}], 'crystal-Zr').color(0.1, 0.3, 0.0).lineType('none').markerType('o').markerSize(12);
    plt.show();
}
.then {println("1. 使用专门的内置方法判断");}
.doJob {
    // 计算固体判断
    def isSolid;
    try (def mpc = data.getMPC()) {
        isSolid = mpc.checkSolidQ6();
    }
    
    // 绘制晶体结构
    // 直接获取 xyz 数据
    def dataSTD = data.dataSTD();
    def type = dataSTD['type'];
    // 绘制
    def plt = Plotters.get();
    plt.plot(dataSTD['x'][{int i -> isSolid[i]==0 && type[i]==1}], dataSTD['y'][{int i -> isSolid[i]==0 && type[i]==1}], 'glass-Cu'  ).color(0.8, 0.6, 0.0).lineType('none').markerType('o').markerSize(3);
    plt.plot(dataSTD['x'][{int i -> isSolid[i]==1 && type[i]==1}], dataSTD['y'][{int i -> isSolid[i]==1 && type[i]==1}], 'crystal-Cu').color(0.5, 0.3, 0.0).lineType('none').markerType('o').markerSize(10);
    plt.plot(dataSTD['x'][{int i -> isSolid[i]==0 && type[i]==2}], dataSTD['y'][{int i -> isSolid[i]==0 && type[i]==2}], 'glass-Zr'  ).color(0.2, 0.6, 0.0).lineType('none').markerType('o').markerSize(4);
    plt.plot(dataSTD['x'][{int i -> isSolid[i]==1 && type[i]==2}], dataSTD['y'][{int i -> isSolid[i]==1 && type[i]==2}], 'crystal-Zr').color(0.1, 0.3, 0.0).lineType('none').markerType('o').markerSize(12);
    plt.show();
}
.finish {println("Finished");}
;
