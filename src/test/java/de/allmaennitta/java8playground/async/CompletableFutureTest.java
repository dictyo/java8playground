package de.allmaennitta.java8playground.async;

import org.assertj.core.api.CompletableFutureAssert;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CompletableFutureTest {
    private static final int sleepTime = 4_000;
    private DummyReceiver receiver = DummyReceiver.get();
    private List<String> messages = new ArrayList<>(3);

    @Test
    public void testSupplyAsync() {
        //Default Executor is ForkJoinPool.commonPool()
        CompletableFuture<Void> completed =
                CompletableFuture
                        .supplyAsync(() -> this.sendMsg("SupplyAsync"))
                        .thenApply(msg -> this.sendMsg("SupplyAsync"))//pick up, transform and pass result like map
                        .thenAccept(msg -> this.notifyListeners(msg))// use result, return void CFuture
                        .thenRun(() -> System.out.println("I neither get nor give.")); //neither use result nor return anything other than void


        completed.thenRun(() -> {
            assertThat(receiver.getNotifications().size()).isEqualTo(1);
            assertThat(receiver.getNotifications().contains("SupplyAsync"));
            receiver.reset();
        });
        assertThat(completed)
                .isCompleted()
                .isDone();
    }


    private String sendMsg(String msg) {
        return msg;
    }

    private CompletableFuture<String> sendMsgFakeAsync(String msg) {
        return CompletableFuture.completedFuture(msg);
    }

    private void notifyListeners(String msg) {
        receiver.notify(msg);
    }

    @Test
    public void testCompose() {
        // apply leads in the sendMsgFakeAsync case to nesting of CompletableFutures
        CompletableFuture<CompletableFuture<String>> completedNested =
                CompletableFuture
                        .supplyAsync(() -> this.sendMsg("SupplyAsync"))
                        .thenApply(m -> this.sendMsgFakeAsync("Apply"));

        assertThat(completedNested)
                .isCompleted()
                .isCompletedWithValueMatching(p -> {
                    try {
                        return p.get().equals("Apply"); //nested CFuture<CFuture<String>>
                    } catch (InterruptedException | ExecutionException e) {
                        throw new IllegalStateException("CF-get failed.", e);
                    }
                })
                .isDone();

        //-------------------------

        // thenCompose avoids in the sendMsgFakeAsync case nesting, comparable to flatMap
        CompletableFuture<String> completedFlat =
                CompletableFuture
                        .supplyAsync(() -> this.sendMsg("SupplyAsync"))
                        .thenCompose(m -> this.sendMsgFakeAsync("Compose"));

        assertThat(completedFlat)
                .isCompleted()
                .isCompletedWithValue("Compose")
                .isDone();
    }

    @Test
    public void testApply_WITHOUT_Async() throws InterruptedException, ExecutionException, TimeoutException {
        // apply leads in the sendMsgFakeAsync case to nesting of CompletableFutures
        final long startTimeApply = System.currentTimeMillis();
        final int singleWaitTime = 2;
        final int sequentialWaitTime = singleWaitTime * 2;
        final int timeout = sequentialWaitTime + singleWaitTime; //singleWaittime as additional buffer

        CompletableFuture completedApply = CompletableFuture
                .supplyAsync(() -> true)
                .thenApply(b -> this.sendMsgWithWait("SupplyAsync", singleWaitTime))
                .thenApply(m -> this.sendMsgWithWait("Apply", singleWaitTime))
                .thenApply(m -> {
                    long delta = System.currentTimeMillis() - startTimeApply;
                    System.out.println("totalTime: " + delta);
                    assertThat(delta).isGreaterThan(sequentialWaitTime);
                    return m;
                });

        System.out.println("Timeout: " + timeout);
        String resultApply = (String) completedApply.get(timeout, TimeUnit.SECONDS);
        assertThat(resultApply).isEqualTo("Apply");
    }

    @Disabled("Don't know why async doesnt lead to a total time of less than sequentialWaitTime.")
    @Test
    public void testApply_WITH_Async() throws InterruptedException, ExecutionException, TimeoutException {
        // apply leads in the sendMsgFakeAsync case to nesting of CompletableFutures
        final long startTimeApply = System.currentTimeMillis();
        final int singleWaitTime = 2;
        final int sequentialWaitTime = singleWaitTime * 2;
        final int timeout = sequentialWaitTime * 2; //singleWaittime as additional buffer


        final long startTimeApplyAsync = System.currentTimeMillis();
        CompletableFuture completedApplyAsync = CompletableFuture
                .supplyAsync(() -> true)
                .thenApplyAsync(b -> this.sendMsgWithWait("SupplyAsync", singleWaitTime))
                .thenApplyAsync(m -> this.sendMsgWithWait("ApplyAsync", singleWaitTime))
                .thenApply(m -> {
                    long delta = System.currentTimeMillis() - startTimeApplyAsync;
                    System.out.println("totalTime: " + delta);
                    assertThat(delta).isLessThan(sequentialWaitTime);
                    return m;
                });
        System.out.println("Timeout: " + timeout);
        String resultApplyAsync = (String) completedApplyAsync.get(timeout, TimeUnit.SECONDS);
        assertThat(resultApplyAsync).isEqualTo("ApplyAsync");
    }

    private String sendMsgWithWait(String msg, int soManySeconds) {
        System.out.println("Thread: " + Thread.currentThread().getName());
        System.out.printf("Sleeping %d seconds.\n", soManySeconds);
        try {
            TimeUnit.SECONDS.sleep(soManySeconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return msg;
    }

    @Test
    public void testException() throws InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture completedException = CompletableFuture
                .supplyAsync(() -> true)
                .thenApply(b -> {
                    throw new IllegalStateException("FakeError!");
                    //  return this.sendMsg("SupplyAsync");
                })
                .exceptionally(ex -> ex.getMessage())//for more options: whenComplete or handle
                .thenApply(msg -> msg + "!!");

        String resultException = (String) completedException.get(2, TimeUnit.SECONDS);
        assertThat(resultException).contains("FakeError!!!");
    }

    @Disabled
    @Test
    public void testTimeout() throws ExecutionException, InterruptedException {

        AtomicLong startTime = new AtomicLong(0L);
        CompletableFuture completedException = CompletableFuture
                .supplyAsync(() -> true)
                .thenApply(b -> {
                    startTime.addAndGet(System.currentTimeMillis());
                    return this.sendMsgWithWait("Too long", 5);
                })
                .thenApply(s -> {
                    System.out.println("DeltaTime Millis: " + (System.currentTimeMillis()-startTime.get()));
                    return s;
                })
//              .orTimeout(2, TimeUnit.SECONDS); //Seems not to work properly (Completed with value)
                .completeOnTimeout("Timeout!", 1, TimeUnit.SECONDS); //Seems not to work, either.


          assertThat((String) completedException.get()).isEqualTo("Too long"); // WHY?
//        assertThatThrownBy(() -> {}).hasMessageContaining("Too long"); //WHY NOT?
    }

    //TODO thenCombine
    //TODO acceptEither
}
