[![Maven Central Version](https://img.shields.io/maven-central/v/com.github.marks-yag/retry)](https://maven-badges.herokuapp.com/maven-central/com.github.marks-yag/retry)
![Maven Central Last Update](https://img.shields.io/maven-central/last-update/com.github.marks-yag/retry)
[![Java CI with Maven](https://github.com/marks-yag/retry/actions/workflows/maven.yml/badge.svg)](https://github.com/marks-yag/retry/actions/workflows/maven.yml)

English | [中文](README_cn.md)
# Retry
An elegant(🌝) lightweight retry framework for JVM languages, supporting:
- Highly customizable retry policies, backoff strategies, and failure listeners.
- Both synchronous and asynchronous calls
- AOP retry policy enhancement.

**Retry** is written in Kotlin and provides Java-friendly APIs. 

# Getting Started
Retry is available on [Maven Central](https://mvnrepository.com/artifact/com.github.marks-yag/retry).

We can use constructor with default parameters to create `RetryPolicy` in Kotlin:
```kotlin
fun main() {
    val policy = RetryPolicy(
        retryCondition = MaxRetries(10),
        backoffPolicy = FixedDelay(Duration.ofSeconds(1))
    )
    policy.call {
        throw IOException("error")
    }
}
```
Alternatively, you can also use `RetryBuilder` to create `RetryPolicy`:
```kotlin
fun main() {
    val policy = RetryBuilder(retryCondition = MaxRetries(10), backoffPolicy = FixedDelay(Duration.ofSeconds(1)))
        .addFailureListener(MyFailureListener())
        .build()
    policy.call {
        throw IOException("error")
    }
}
```
Java user was suggested to use `RetryBuilder` to create `RetryPolicy`:
```java
public class Test {
    public static void main(String[] args) {
        RetryPolicy policy = new RetryBuilder(new MaxRetries(10), new FixedDelay(Duration.ofSeconds(1)))
            .addFailureListener(new MyFailureListener())
            .build();
        try {
            policy.call(() -> {
                throw new IOException("error");
            });
        } catch (IOException e) {
            // handle IOException
        } catch (Exception e) {
            // handle other exceptions
        }
    }
}
```

# Design Principles
Unlike SpringRetry, Retry does not provide annotation-based retry policies, but recommends users to define reusable retry policies through programming and apply them to the business logic that needs retries. This approach decouples the specific business logic implementation from the retry policy, allowing users to dynamically select different retry policies for the same business logic without limitations imposed by annotation declarations at compile time.
Retry policies are defined by combining the following elements, and provide built-in implementations:
- Retry condition: Defines when to retry.
- Termination condition: Defines when to terminate retries. When the retry condition is not met or the termination condition is met, retries will stop.
- Backoff strategy: Defines the waiting time interval between retries.
- Failure listener: Defines the processing logic when retries fail. Includes logging output, custom recovery logic, etc.

Unlike some other retry frameworks, **Retry** does not provide retry policies based on return values, but **only through exceptions**. You can check unexpected return values and throw exceptions by yourself.

# Built-in Retry Conditions
- `Conditions.MaxRetries(amount)`: Maximum number of retries (not including the first execution).
- `Conditions.MaxAttempts(amount)`: Maximum number of attempts (including the first execution).
- `Conditions.MaxTimeElapsed(duration)`: Maximum time elapsed for attempts.
- `Conditions.ExceptionOf(types)`: Specified exception types.
- `Conditions.TRUE`: Always returns true.
- `Conditions.FALSE`: Always returns false.
- `Conditions.UNRECOVERABLE_EXCEPTIONS`: Unrecoverable exception types (like `InterruptedException`, `RuntimeException`, and `Error`), which are also the default termination condition for `RetryPolicy`.
# Built-in Backoff Strategies
- `BackoffPolicies.FixedDelay(duration)`: Fixed backoff time.
- `BackoffPolicies.FixedInterval(duration)`: Fixed interval between attempts. If the execution time of the attempt exceeds the interval, the next attempt will start immediately.
- `BackoffPolicies.Exponential(initDuration, maxDuration)`: Exponential backoff.
- `BackoffPolicies.NONE`: No backoff, which is also the default backoff strategy for `RetryPolicy`.
# Built-in Failure Listeners
- `FailureListeners.SimpleLoggingFailureListener(log, stack)`: Simple logging output. Also the default failure listener for `RetryPolicy`.

## License
[Apache License 2.0](LICENSE)
