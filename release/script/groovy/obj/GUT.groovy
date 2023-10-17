package obj

import jtool.iofile.IOFiles
import jtool.system.ISystemExecutor


/** Groovy UT */
class GUT {
    /** 在给定的 ISystemExecutor 位置初始化 jtool 的环境，使得在对应环境能够使用 ./jtool 指令。一般需要对方是远程的环境 */
    def static initJToolEnv(ISystemExecutor exe) {
        // 构造输入输出文件，涉及到的输入文件为 jtool 本身的库，jtool 执行脚本，key 是随便起的
        var ioFiles = (new IOFiles())
                .i('lib', 'lib/jtool-all.jar')
                .i('shell', 'jtool')
                .i('bat', 'jtool.bat');
        // 发送指令设置脚本的运行权限，执行此指令前会自动上传附加文件
        exe.system('chmod 777 jtool', ioFiles);
    }
}
