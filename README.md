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

Kotlin:

```kotlin
fun main() {
    val policy = RetryPolicy.Builder(retryRule = MaxAttempts(10), backoffPolicy = FixedDelay(Duration.ofSeconds(1)))
        .addFailureListener(MyFailureListener())
        .build()
    policy.call {
        throw IOException("error")
    }
}
```
Or java:

```java
import retry.RetryPolicy;

public class Test {
    public static void main(String[] args) {
        RetryPolicy policy = new RetryPolicy.Builder(new MaxAttempts(10), new FixedDelay(Duration.ofSeconds(1)))
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
- Retry rule: Defines when to retry.
- Termination rule: Defines when to terminate retries. When the retry rule is not met or the termination rule is met, retries will stop.
- Backoff strategy: Defines the waiting time interval between retries.
- Failure listener: Defines the processing logic when retries fail. Includes logging output, custom recovery logic, etc.

Unlike some other retry frameworks, **Retry** does not provide retry policies based on return values, but **only through exceptions**. You can check unexpected return values and throw exceptions by yourself.

# Retry Rules
There are some built-in retry rules under `Rules`:
- `Rules.MaxAttempts(amount)`: Maximum number of attempts (including the first execution).
- `Rules.MaxTimeElapsed(duration)`: Maximum time elapsed for attempts.
- `Rules.ExceptionIn(types)`: Specified exception types.
- `Rules.TRUE`: Always returns true.
- `Rules.FALSE`: Always returns false.
- `Rules.UNRECOVERABLE_EXCEPTIONS`: Unrecoverable exception types (like `InterruptedException`, `RuntimeException`, and `Error`), which are also the default termination rule for `RetryPolicy`.

You can combine multiple rules to create more complex retry rules:
Kotlin:
```kotlin
val rule = MaxAttempts(10) and MaxTimeElapsed(Duration.ofSeconds(10))
```
Java:
```java
RetryRule rule = new MaxAttempts(10).and(new MaxTimeElapsed(Duration.ofSeconds(10)));
```

# Backoff Strategies
There are some built-in backoff strategies under `BackoffPolicies`:
- `BackoffPolicies.FixedDelay(duration)`: Fixed delay backoff.
- `BackoffPolicies.ExponentialDelay(initDuration, maxDuration)`: Exponential delay backoff.
- `BackoffPolicies.RandomDelay(minDuration, maxDuration)`: Random delay backoff.
- `BackoffPolicies.NONE`: No backoff, which is also the default backoff strategy for `RetryPolicy`.

You can combine multiple backoff strategies to create more complex backoff strategies:
Kotlin:
```kotlin
val backoffPolicy = FixedDelay(Duration.ofSeconds(10)) + RandomDelay(Duration.ofSeconds(0), Duration.ofSeconds(10))
```
Java:
```java
BackoffPolicy backoffPolicy = new FixedDelay(Duration.ofSeconds(10)).plus(new RandomDelay(Duration.ofSeconds(0), Duration.ofSeconds(10)));
```

# Built-in Failure Listeners
- `FailureListeners.SimpleLoggingFailureListener(log, stack)`: Simple logging output. `RetryPolicy` has built-in it for retry logging output.

## License
[Apache License 2.0](LICENSE)
