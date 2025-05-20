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

- [Kotlin Simple Demo](demo/src/main/kotlin/retry/demo/kotlin/Simple.kt)
- [Java Simple Demo](demo/src/main/java/retry/demo/java/Simple.java)
- [Kotlin Async Retry Demo](demo/src/main/kotlin/retry/demo/kotlin/Async.kt)
- [Java Async Retry Demo](demo/src/main/java/retry/demo/java/Async.java)
- [Kotlin Proxy Retry Demo](demo/src/main/kotlin/retry/demo/kotlin/Proxy.kt)
- [Java Proxy Retry Demo](demo/src/main/java/retry/demo/java/Proxy.java)

# Design Principles
Unlike SpringRetry, Retry does not provide annotation-based retry policies, but recommends users to define reusable retry policies through programming and apply them to the business logic that needs retries. This approach decouples the specific business logic implementation from the retry policy, allowing users to dynamically select different retry policies for the same business logic without limitations imposed by annotation declarations at compile time.
Retry policies are defined by combining the following elements, and provide built-in implementations:
- Retry policy: Defines when to retry.
- Termination policy: Defines when to terminate retries. When the retry policy is not met or the termination policy is met, retries will stop.
- Backoff policy: Defines the waiting time interval between retries.
- Failure listener: Defines the processing logic when retries fail. Includes logging output, custom recovery logic, etc.

Unlike some other retry frameworks, **Retry** does not provide retry policies based on return values, but **only through exceptions**. You can check unexpected return values and throw exceptions by yourself.

# Retry RetryPolicies
There are some built-in retry policies under `RetryPolicies`. You can combine multiple policies to create more complex retry policies:
Kotlin:
```kotlin
val policy = MaxAttempts(10) and MaxTimeElapsed(Duration.ofSeconds(10))
```
Java:
```java
RetryRetryPolicy policy = new MaxAttempts(10).and(new MaxTimeElapsed(Duration.ofSeconds(10)));
```

# Backoff Strategies
There are some built-in backoff strategies under `BackoffPolicies`.

You can combine multiple backoff strategies to create more complex backoff strategies:
Kotlin:
```kotlin
val backoffPolicy = FixedDelay(Duration.ofSeconds(10)) + RandomDelay(Duration.ofSeconds(0), Duration.ofSeconds(10))
```
Java:
```java
BackoffPolicy backoffPolicy = new FixedDelay(Duration.ofSeconds(10)).plus(new RandomDelay(Duration.ofSeconds(0), Duration.ofSeconds(10)));
```

## License
[Apache License 2.0](LICENSE)
