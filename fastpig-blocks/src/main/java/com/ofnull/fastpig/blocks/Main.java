package com.ofnull.fastpig.blocks;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author ofnull
 * @date 2022/2/16 14:31
 */
public class Main {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(10);
        ExecutorService p1 = Executors.newFixedThreadPool(10);
        ExecutorService p2 = Executors.newFixedThreadPool(10);

//        CompletableFuture.supplyAsync(() -> {
//            return 10;
//        }, pool).thenApplyAsync(t -> {
//            return t + 100;
//        }).thenAcceptAsync(t -> System.out.println(t));
//
        CompletableFuture<Double> comsumer1 = CompletableFuture.supplyAsync(() -> {
            return 1.1;
        }, p1);


        CompletableFuture<Double> comsumer2 = CompletableFuture.supplyAsync(() -> {
            return 2.9;
        }, p2);

        CompletableFuture<Double> future = comsumer1.thenCombineAsync(comsumer2, (a, b) -> {
            return a + b;
        }, p1);

        System.out.println("结果："+ future.get());
        System.out.println("------");
    }
}
