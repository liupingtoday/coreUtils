package com.migu;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;


public class FileSize {
    private static Logger logger = LogManager.getLogger(FileSize.class);

    private final static ForkJoinPool forkjoinPool = new ForkJoinPool();
    private static class FileSizeFinder extends RecursiveTask<Long>{
        final File file;

        public FileSizeFinder(final File theFile) {
            file = theFile;
        }

        @Override
        protected Long compute() {
            long size = 0;
            if (file.isFile()) {
                size = file.length();
                logger.info("the file name is " + file.getName() + ",and the size is " + size + " byte" );
            }else{
                File[] children = file.listFiles();
                if (children != null) {
                    List<ForkJoinTask<Long>> tasks = new ArrayList<ForkJoinTask<Long>>();
                    for (File child : children) {
                        if (child.isFile()) {
                            size += child.length();
                            logger.info("the file name is " + child.getName() + ",and the size is " + size + " byte" );
                        }else{
                            tasks.add(new FileSizeFinder(child));
                            logger.info("----tasks.add " + child.getName());
                        }
                    }
                    for (ForkJoinTask<Long> forkJoinTask : invokeAll(tasks)) {
                        size +=  forkJoinTask.join();
                    }
                }
            }
            return size;
        }

    }

    public static void main(String[] args) throws InterruptedException {
        System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
        final long start = System.nanoTime();
        long startTime=System.currentTimeMillis();
//        final long total = forkjoinPool.invoke(new FileSizeFinder(new File("/code")));
        final long total = forkjoinPool.invoke(new FileSizeFinder(new File("/nas/cmam/cmam_reading/prod/anchor")));
//        final long total = forkjoinPool.invoke(new FileSizeFinder(new File("/nas/cmam/cmam_reading/prod/audio")));
//        final long total = forkjoinPool.invoke(new FileSizeFinder(new File("/nas/cmam/cmam_reading/prod/author")));
//        final long total = forkjoinPool.invoke(new FileSizeFinder(new File("/nas/cmam/cmam_reading/prod/book")));
//        final long total = forkjoinPool.invoke(new FileSizeFinder(new File("/nas/cmam/cmam_reading/prod/chapter")));
//        final long total = forkjoinPool.invoke(new FileSizeFinder(new File("/nas/cmam/cmam_reading/prod/copyright")));
//        final long total = forkjoinPool.invoke(new FileSizeFinder(new File("/nas/cmam/cmam_reading/prod/image")));
//        final long total = forkjoinPool.invoke(new FileSizeFinder(new File("/nas/cmam/cmam_reading/prod")));
        final long end = System.nanoTime();
        logger.info("Total size :"+total*1.0/1024/1024/1024 + "GB");
        logger.info("Time taken:"+ (end-start)/1.0e9 + " s");
        logger.info("Time cost  " + (System.currentTimeMillis()-startTime)*1.0/1000 + " s");
    }
}
