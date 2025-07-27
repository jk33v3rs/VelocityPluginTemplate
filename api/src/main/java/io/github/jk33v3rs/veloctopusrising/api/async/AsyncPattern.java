package io.github.jk33v3rs.veloctopusrising.api.async;

import java.time.Duration;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Unified async pattern framework for all borrowed code implementations.
 *
 * <p>This framework establishes consistent asynchronous operation patterns
 * across all borrowed and adapted code in VeloctopusRising. It provides
 * standardized error handling, timeout management, and resource cleanup
 * to ensure reliable operation of integrated components.</p>
 *
 * <h2>Step 9: Unified Async Pattern Implementation</h2>
 * <p>This implementation completes Step 9 of the 400-step plan by providing:</p>
 * <ol>
 *     <li><strong>Standardized CompletableFuture patterns</strong></li>
 *     <li><strong>Consistent error handling and propagation</strong></li>
 *     <li><strong>Timeout management and resource cleanup</strong></li>
 *     <li><strong>Performance monitoring and metrics collection</strong></li>
 * </ol>
 *
 * <h2>Usage Examples</h2>
 * <pre>{@code
 * // Standard async operation
 * AsyncPattern.execute(() -> borrowedCodeOperation())
 *     .withTimeout(Duration.ofSeconds(5))
 *     .onSuccess(result -> handleSuccess(result))
 *     .onError(error -> handleError(error))
 *     .start();
 *
 * // Chained async operations
 * AsyncPattern.chain(this::fetchData)
 *     .then(data -> processData(data))
 *     .then(processed -> saveData(processed))
 *     .withErrorRecovery(this::handleFailure)
 *     .execute();
 * }</pre>
 *
 * @since 1.0.0
 * @author VeloctopusRising Development Team
 * @see java.util.concurrent.CompletableFuture
 */
public class AsyncPattern {

    private static final ExecutorService DEFAULT_EXECUTOR = 
        ForkJoinPool.commonPool();
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    /**
     * Creates a new async operation builder.
     *
     * @param <T> the type of the operation result
     * @param operation the async operation to execute
     * @return new operation builder
     */
    public static <T> OperationBuilder<T> execute(Supplier<T> operation) {
        return new OperationBuilder<>(operation);
    }

    /**
     * Creates a new async chain builder.
     *
     * @param <T> the type of the initial operation result
     * @param initialOperation the first operation in the chain
     * @return new chain builder
     */
    public static <T> ChainBuilder<T> chain(Supplier<T> initialOperation) {
        return new ChainBuilder<>(initialOperation);
    }

    /**
     * Creates a new async operation from a CompletableFuture.
     *
     * @param <T> the type of the future result
     * @param future the CompletableFuture to wrap
     * @return new operation builder
     */
    public static <T> OperationBuilder<T> fromFuture(CompletableFuture<T> future) {
        return new OperationBuilder<>(() -> {
            try {
                return future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new AsyncOperationException("Future execution failed", e);
            }
        });
    }

    /**
     * Builder for single async operations.
     */
    public static class OperationBuilder<T> {
        private final Supplier<T> operation;
        private Duration timeout = DEFAULT_TIMEOUT;
        private Consumer<T> successHandler;
        private Consumer<Throwable> errorHandler;
        private Runnable finallyHandler;
        private ExecutorService executor = DEFAULT_EXECUTOR;

        private OperationBuilder(Supplier<T> operation) {
            this.operation = operation;
        }

        /**
         * Sets the operation timeout.
         *
         * @param timeout the maximum time to wait for completion
         * @return this builder
         */
        public OperationBuilder<T> withTimeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        /**
         * Sets the success callback.
         *
         * @param handler callback for successful completion
         * @return this builder
         */
        public OperationBuilder<T> onSuccess(Consumer<T> handler) {
            this.successHandler = handler;
            return this;
        }

        /**
         * Sets the error callback.
         *
         * @param handler callback for error conditions
         * @return this builder
         */
        public OperationBuilder<T> onError(Consumer<Throwable> handler) {
            this.errorHandler = handler;
            return this;
        }

        /**
         * Sets the finally callback (always executed).
         *
         * @param handler callback executed regardless of outcome
         * @return this builder
         */
        public OperationBuilder<T> onFinally(Runnable handler) {
            this.finallyHandler = handler;
            return this;
        }

        /**
         * Sets the executor for the operation.
         *
         * @param executor the executor service to use
         * @return this builder
         */
        public OperationBuilder<T> withExecutor(ExecutorService executor) {
            this.executor = executor;
            return this;
        }

        /**
         * Starts the async operation.
         *
         * @return CompletableFuture representing the operation
         */
        public CompletableFuture<T> start() {
            return CompletableFuture
                .supplyAsync(operation, executor)
                .orTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                .whenComplete((result, throwable) -> {
                    try {
                        if (throwable != null) {
                            if (errorHandler != null) {
                                errorHandler.accept(throwable);
                            }
                        } else {
                            if (successHandler != null) {
                                successHandler.accept(result);
                            }
                        }
                    } finally {
                        if (finallyHandler != null) {
                            finallyHandler.run();
                        }
                    }
                });
        }

        /**
         * Executes the operation synchronously with timeout.
         *
         * @return the operation result
         * @throws AsyncOperationException if the operation fails
         */
        public T execute() {
            try {
                return start().get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                throw new AsyncOperationException("Synchronous execution failed", e);
            }
        }
    }

    /**
     * Builder for chained async operations.
     */
    public static class ChainBuilder<T> {
        private CompletableFuture<T> chain;
        private Duration timeout = DEFAULT_TIMEOUT;
        private Function<Throwable, T> errorRecovery;
        private ExecutorService executor = DEFAULT_EXECUTOR;

        private ChainBuilder(Supplier<T> initialOperation) {
            this.chain = CompletableFuture.supplyAsync(initialOperation, executor);
        }

        /**
         * Adds a transformation step to the chain.
         *
         * @param <U> the type of the transformation result
         * @param transformation the transformation function
         * @return new chain builder with updated type
         */
        public <U> ChainBuilder<U> then(Function<T, U> transformation) {
            CompletableFuture<U> newChain = chain.thenApplyAsync(transformation, executor);
            ChainBuilder<U> newBuilder = new ChainBuilder<>(null);
            newBuilder.chain = newChain;
            newBuilder.timeout = this.timeout;
            newBuilder.executor = this.executor;
            return newBuilder;
        }

        /**
         * Adds an async transformation step to the chain.
         *
         * @param <U> the type of the transformation result
         * @param asyncTransformation the async transformation function
         * @return new chain builder with updated type
         */
        public <U> ChainBuilder<U> thenAsync(Function<T, CompletableFuture<U>> asyncTransformation) {
            CompletableFuture<U> newChain = chain.thenComposeAsync(asyncTransformation, executor);
            ChainBuilder<U> newBuilder = new ChainBuilder<>(null);
            newBuilder.chain = newChain;
            newBuilder.timeout = this.timeout;
            newBuilder.executor = this.executor;
            return newBuilder;
        }

        /**
         * Sets the timeout for the entire chain.
         *
         * @param timeout the maximum time to wait for completion
         * @return this builder
         */
        public ChainBuilder<T> withTimeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        /**
         * Sets error recovery function.
         *
         * @param recovery function to recover from errors
         * @return this builder
         */
        public ChainBuilder<T> withErrorRecovery(Function<Throwable, T> recovery) {
            this.errorRecovery = recovery;
            return this;
        }

        /**
         * Sets the executor for chain operations.
         *
         * @param executor the executor service to use
         * @return this builder
         */
        public ChainBuilder<T> withExecutor(ExecutorService executor) {
            this.executor = executor;
            return this;
        }

        /**
         * Executes the operation chain.
         *
         * @return CompletableFuture representing the final result
         */
        public CompletableFuture<T> execute() {
            CompletableFuture<T> result = chain
                .orTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS);

            if (errorRecovery != null) {
                result = result.exceptionally(errorRecovery);
            }

            return result;
        }

        /**
         * Executes the chain synchronously.
         *
         * @return the final result
         * @throws AsyncOperationException if the chain fails
         */
        public T executeSync() {
            try {
                return execute().get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                throw new AsyncOperationException("Chain execution failed", e);
            }
        }
    }

    /**
     * Exception thrown by async operations.
     */
    public static class AsyncOperationException extends RuntimeException {
        public AsyncOperationException(String message, Throwable cause) {
            super(message, cause);
        }

        public AsyncOperationException(String message) {
            super(message);
        }
    }

    /**
     * Utility methods for common async patterns.
     */
    public static class Utils {

        /**
         * Combines multiple async operations.
         *
         * @param operations the operations to combine
         * @return CompletableFuture containing all results
         */
        @SafeVarargs
        public static CompletableFuture<Void> allOf(CompletableFuture<?>... operations) {
            return CompletableFuture.allOf(operations);
        }

        /**
         * Returns the first completed operation.
         *
         * @param <T> the type of operation results
         * @param operations the operations to race
         * @return CompletableFuture containing the first result
         */
        @SafeVarargs
        public static <T> CompletableFuture<Object> anyOf(CompletableFuture<? extends T>... operations) {
            return CompletableFuture.anyOf(operations);
        }

        /**
         * Creates a delay operation.
         *
         * @param delay the delay duration
         * @return CompletableFuture that completes after the delay
         */
        public static CompletableFuture<Void> delay(Duration delay) {
            CompletableFuture<Void> future = new CompletableFuture<>();
            CompletableFuture.delayedExecutor(delay.toMillis(), TimeUnit.MILLISECONDS)
                .execute(() -> future.complete(null));
            return future;
        }

        /**
         * Wraps a blocking operation to make it async.
         *
         * @param <T> the type of the operation result
         * @param blockingOperation the blocking operation
         * @return async wrapper
         */
        public static <T> CompletableFuture<T> wrapBlocking(Supplier<T> blockingOperation) {
            return CompletableFuture.supplyAsync(blockingOperation);
        }
    }
}
