package com.guan.code;

public interface IHasIOFiles {
    Iterable<String> inputFiles(String aInFileKey);
    Iterable<String> outputFiles(String aOutFileKey);
    Iterable<String> inputFiles();
    Iterable<String> outputFiles();
}
