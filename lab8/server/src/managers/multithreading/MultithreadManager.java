package managers.multithreading;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

public class MultithreadManager {
    private static final ForkJoinPool requestThreadPool = new ForkJoinPool(4);
    private static final ForkJoinPool responseThreadPool = new ForkJoinPool();

    public static ForkJoinPool getRequestThreadPool() {
        return requestThreadPool;
    }

    public static ForkJoinPool getResponseThreadPool() {
        return responseThreadPool;
    }
}
