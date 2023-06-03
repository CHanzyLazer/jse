package test

import com.jtool.parallel.ParforThreadPool


/** 测试并行 for 循环 */
pool = new ParforThreadPool(4);


/** 一般的 lambda 表达式的写法 */
pool.parfor(10, {i ->
    println(i);
});

/** groovy 支持这种更加类似 for 循环的写法 */
pool.parfor(10) {i ->
    println(i+10);
}

pool.shutdown();
