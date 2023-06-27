package de.mischmaschine.database.redis

import java.util.concurrent.CancellationException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

class FutureAction<T>(future: CompletableFuture<T>.() -> Unit) : CompletableFuture<T>() {

    /**
     * Creates an uncompleted empty future.
     */
    constructor() : this({})

    /**
     * This constructor completes the future with the given value.
     * @param objects The value to complete the future with.
     */
    constructor(objects: T) : this() {
        this.complete(objects)
    }

    init {
        this.apply(future)
    }

    /**
     * Is only called if the future is completed exceptionally.
     * @param action The throwable that caused the future to be completed exceptionally.
     */
    fun onFailure(action: (Throwable) -> Unit): FutureAction<T> {
        whenComplete { _, throwable ->
            throwable?.let { action(it) }
        }
        return this
    }

    fun onFailureAsync(action: (Throwable) -> Unit): FutureAction<T> {
        whenCompleteAsync { _, throwable ->
            throwable?.let { action(it) }
        }
        return this
    }

    /**
     * Is only called if the future is completed normally.
     * @param action The value that caused the future to be completed normally.
     */
    fun onSuccess(action: (T) -> Unit): FutureAction<T> {
        whenComplete { result, throwable ->
            throwable ?: action(result)
        }
        return this
    }

    fun onSuccessAsync(action: (T) -> Unit): FutureAction<T> {
        whenCompleteAsync { result, throwable ->
            throwable ?: action(result)
        }
        return this
    }

    /**
     * Gets the result of the future. If the future is not completed yet, this method will block until the future is completed.
     * @return The result of the future or null.
     */
    fun getBlockingOrNull(): T? {
        return try {
            this.get()
        } catch (_: CancellationException) {
            null
        } catch (_: InterruptedException) {
            null
        } catch (_: ExecutionException) {
            null
        }
    }
}