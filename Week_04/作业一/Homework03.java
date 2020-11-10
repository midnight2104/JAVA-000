package java0.conc0303;

import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 本周作业：（必做）思考有多少种方式，在main函数启动一个新线程或线程池，
 * 异步运行一个方法，拿到这个方法的返回值后，退出主线程？
 * 写出你的方法，越多越好，提交到github。
 * <p>
 * 一个简单的代码参考：
 */
public class Homework03 {

    public static void main(String[] args) {

        long start = System.currentTimeMillis();
        // 在这里创建一个线程或线程池，
        // 异步执行 下面方法

        //int result = sum(); //这是得到的返回值

        //方式一：使用线程池
        //int result = method1();

        //方式二：：使用线 FutureTask
        //int result = method2();

        //方式三：使用 CompletableFuture
        //int result = method3();

        //方式四：使用 join
        //int result = method4();

        //方式五：使用 CountDownLatch
        //int result = method5();

        //方式六：使用 CyclicBarrier
        //int result = method6();

        //方式七：使用 while等待
        int result = method7();

        // 确保  拿到result 并输出
        System.out.println("异步计算结果为：" + result);

        System.out.println("使用时间：" + (System.currentTimeMillis() - start) + " ms");

        // 然后退出main线程
    }

    private static int sum() {
        return fibo(36);
    }

    private static int fibo(int a) {
        if (a < 2)
            return 1;
        return fibo(a - 1) + fibo(a - 2);
    }


    /**
     * 方式一：使用线程池
     *
     * @return 结果值
     */
    private static int method1() {
        ExecutorService executorService = Executors.newFixedThreadPool(8);
        FutureTask<Integer> task = new FutureTask<>(Homework03::sum);
        executorService.submit(task);

        executorService.shutdown();
        try {
            return task.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return -1;
    }


    /**
     * 方式二：使用线 FutureTask
     *
     * @return 结果值
     */
    private static int method2() {
        FutureTask<Integer> task = new FutureTask<>(Homework03::sum);

        new Thread(task).start();

        try {
            return task.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * 方式三：使用 CompletableFuture
     *
     * @return 结果值
     */
    private static int method3() {
        CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(Homework03::sum);

        try {
            return completableFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * 方式四：使用 join
     *
     * @return 结果值
     */
    private static int method4() {
        class CalcTask implements Runnable {
            private volatile int value;

            @Override
            public void run() {
                value = sum();
            }

            public int getValue() {
                return value;
            }
        }

        CalcTask task = new CalcTask();
        Thread thread = new Thread(task);
        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return task.getValue();
    }

    /**
     * 方式五：使用 CountDownLatch
     *
     * @return 结果值
     */
    private static int method5() {

        CountDownLatch countDownLatch = new CountDownLatch(1);

        class CalcTask implements Runnable {
            private volatile int value;

            @Override
            public void run() {
                value = sum();
                //执行完一个任务
                countDownLatch.countDown();
            }

            public int getValue() {
                return value;
            }
        }

        CalcTask task = new CalcTask();
        new Thread(task).start();

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return task.getValue();
    }

    /**
     * 方式六：使用 CyclicBarrier
     *
     * @return 结果值
     */
    private static int method6() {

        CyclicBarrier cyclicBarrier = new CyclicBarrier(2, new Runnable() {
            @Override
            public void run() {

            }
        });

        class CalcTask implements Runnable {
            private volatile int value;

            @Override
            public void run() {
                value = sum();

                //执行完一个任务，就等待
                try {
                    cyclicBarrier.await();
                } catch (BrokenBarrierException | InterruptedException e) {
                    e.printStackTrace();
                }
            }

            public int getValue() {
                return value;
            }
        }

        CalcTask task = new CalcTask();
        new Thread(task).start();

        //执行完一个任务，就等待
        try {
            cyclicBarrier.await();
        } catch (BrokenBarrierException | InterruptedException e) {
            e.printStackTrace();
        }

        return task.getValue();
    }

    /**
     * 方式七：使用 while等待
     *
     * @return 结果值
     */
    private static int method7() {
        final boolean[] flag = {false};

        class CalcTask implements Runnable {
            private volatile int value;

            @Override
            public void run() {
                value = sum();

                flag[0] = true;
            }

            public int getValue() {
                return value;
            }
        }

        CalcTask task = new CalcTask();
        new Thread(task).start();

        //等待
        while (!flag[0]) {
            //让出CPU
            Thread.yield();
        }

        return task.getValue();
    }
}
