package com.guan.code;

import java.util.Map;

/**
 * @author liqa
 * <p> 一般的输入文件接口，继承 IHasIOFiles 支持远程提交任务时自动上传下载文件，
 * 继承 Map 支持在 Groovy 中直接使用 . 索引来设置属性 </p>
 */
public interface IInFile extends IHasIOFiles, Map<String, String> {

}
