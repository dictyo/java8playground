package de.allmaennitta.java8playground.streams;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.stream.Collectors.summingDouble;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public class StreamProcessingTest {

    private static final List<Integer> waitTimes =
            Arrays.asList(1, 2, 3, 4, 5, 1, 2, 3, 4, 5, 1, 2, 3, 4, 5, 1, 2, 3, 4, 5)
                    .stream()
                    .map(x -> x*10)
                    .collect(toList());

    private final ExecutorService executorService =
            Executors.newFixedThreadPool(100);

    @BeforeAll
    public static void setUp(){
        final long totalWaitTime = waitTimes.stream().mapToLong(x->(long)x).sum();
        System.out.printf("Total Wait-Time: %d.\n",totalWaitTime);
    }

    @Test
    public void testSequentialStreamProcessing() {
        long startTime = System.nanoTime();
        double totalPurchases = waitTimes.stream()
                .map(id -> new FakeClient(id, 10d))
                .collect(summingDouble(FakeClient::getPurchases));

        long endTime = (System.nanoTime() - startTime) / 1_000_000;
        System.out.println("Sequential | Total time: " + endTime + " ms");
        assertThat(totalPurchases).isEqualTo(200);
    }

    @Test
    public void testParallelStreamProcessing() {
        long startTime = System.nanoTime();
        double totalPurchases = waitTimes.parallelStream()
                .map(id -> new FakeClient(id, 10d))
                .collect(summingDouble(FakeClient::getPurchases));

        long endTime = (System.nanoTime() - startTime) / 1_000_000;
        System.out.println("Parallel | Total time: " + endTime + " ms");
        assertThat(totalPurchases).isEqualTo(200);
    }

    @Test
    public void testAsyncStream(){
        long startTime = System.nanoTime();
        List<CompletableFuture<FakeClient>> futureRequests = waitTimes.stream()
                .map(id -> CompletableFuture.supplyAsync(() -> new FakeClient(id, 10d)))
                .collect(toList());

        double totalPurchases = futureRequests.stream()
                .map(CompletableFuture::join)
                .collect(summingDouble(FakeClient::getPurchases));

        long endTime = (System.nanoTime() - startTime) / 1_000_000;
        System.out.println("Async | Total time: " + endTime + " ms");
        assertThat(totalPurchases).isEqualTo(200);
    }

    @Test
    public void testAsyncStreamExecutor() {
        long startTime = System.nanoTime();
        List<CompletableFuture<FakeClient>> futureRequests = waitTimes.stream()
                .map(id -> CompletableFuture.supplyAsync(() -> new FakeClient(id, 10d), executorService))
                .collect(toList());

        double totalPurchases = futureRequests.stream()
                .map(CompletableFuture::join)
                .collect(summingDouble(FakeClient::getPurchases));

        long endTime = (System.nanoTime() - startTime) / 1_000_000;
        System.out.println("Async with executor | Total time: " + endTime + " ms");
        assertThat(totalPurchases).isEqualTo(200);
        executorService.shutdown();
    }
}