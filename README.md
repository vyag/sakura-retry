[![Maven Central Version](https://img.shields.io/maven-central/v/com.github.marks-yag/retry)](https://maven-badges.herokuapp.com/maven-central/com.github.marks-yag/retry)
![Maven Central Last Update](https://img.shields.io/maven-central/last-update/com.github.marks-yag/retry)
[![Java CI with Maven](https://github.com/marks-yag/retry/actions/workflows/maven.yml/badge.svg)](https://github.com/marks-yag/retry/actions/workflows/maven.yml)

English | [中文](README_cn.md)
# Retry
An elegant lightweight retry framework for JVM, supporting highly customizable retry policies, backoff strategies, and failure listeners, supporting both synchronous and asynchronous calls, and supporting the application of retry policies as AOP proxies.

# Design Principles
Unlike SpringRetry, Retry does not provide annotation-based retry policies, but recommends users to define reusable retry policies through programming and apply them to the business logic that needs retries. This approach decouples the specific business logic implementation from the retry policy, allowing users to dynamically select different retry policies for the same business logic without limitations imposed by annotation declarations at compile time.
Retry policies are defined by combining the following elements, and provide built-in implementations:
- Retry condition: Defines when to retry.
- Termination condition: Defines when to terminate retries. When the retry condition is not met or the termination condition is met, retries will stop.
- Backoff strategy: Defines the waiting time interval between retries.
- Failure listener: Defines the processing logic when retries fail. Includes logging output, custom recovery logic, etc.
Unlike other retry frameworks, Retry does not provide retry policies based on return values, but only through exceptions. Callers can encapsulate unexpected return values as exceptions and throw them.
# Supported Languages
Retry is written in Kotlin and provides Java-friendly APIs. Although not tested, it should theoretically be compatible with other JVM languages like Scala.
# Built-in Retry Conditions
- `Conditions.MaxRetries(amount)`: Maximum number of retries (not including the first execution).
- `Conditions.MaxAttempts(amount)`: Maximum number of attempts (including the first execution).
- `Conditions.MaxTimeElapsed(duration)`: Maximum time elapsed for attempts.
- `Conditions.ExceptionOf(types)`: Specified exception types.
- `Conditions.TRUE`: Always returns true.
- `Conditions.FALSE`: Always returns false.
- `Conditions.UNRECOVERABLE_EXCEPTIONS`: Unrecoverable exception types (like `InterruptedException`, `RuntimeException`, and `Error`), which are also the default termination condition for `RetryPolicy`.
# Built-in Backoff Strategies
- `BackoffPolicies.FixedDelay`: Fixed backoff time.
- `BackoffPolicies.FixedInterval`: Fixed interval between attempts. If the execution time of the attempt exceeds the interval, the next attempt will start immediately.
- `BackoffPolicies.Exponential(initDuration, maxDuration)`: Exponential backoff.
- `BackoffPolicies.NONE`: No backoff, which is also the default backoff strategy for `RetryPolicy`.
# Built-in Failure Listeners
- `FailureListeners.SimpleLoggingFailureListener(log, stack)`: Simple logging output. Also the default failure listener for `RetryPolicy`.
# Getting Started
RetryX is available on Maven Central.

# Example
Kotlin user can use the default parameters of the constructor to create `RetryPolicy`:
```kotlin
import retry.*
fun main() {
    val policy = RetryPolicy(
        retryCondition = MaxRetries(10),
    )
    policy.call {
        throw Exception("error")
    }
}
```
Alternatively, you can use `RetryBuilder` to create `RetryPolicy`:
```kotlin
import retry.*
fun main() {
    val policy = RetryBuilder()
        .retryCondition(MaxRetries(10))
        .build()
    policy.call {
        throw Exception("error")
    }
}
```
Java user was suggested to use `RetryBuilder` to create `RetryPolicy`:
```java
import retry.*;
public class Test {
    public static void main(String[] args) {
        RetryPolicy policy = new RetryBuilder()
           .retryCondition(new MaxRetries(10))
           .build();
        policy.call(() -> {
            throw new Exception("error");
        });
    }
}
```

# Additional tips for Java users
There are different options for checked exception. For Kotlin users, `RetryPolicy.call(Callable)` will throw the exception thrown by the last execution of the Callable, even though its method signature does not declare any exceptions, which is recommended in Kotlin. Callers can ignore the method signature based on actual needs to catch exceptions.
For Java users, `RetryPolicy.call(Callable)` does not declare any exceptions, so callers cannot catch specific checked exceptions, such as the following code cannot compile:
```java
public class Test {
    public static void main(String[] args) {
        RetryPolicy policy = new RetryBuilder()
          .retryCondition(new MaxRetries(10))
          .build();
        try {
            policy.call(() -> {
                throw new IOException("error");
            });
        } catch (IOException e) {
            // handle IOException
        }
    }
}
```
You can solve this problem by catching `Exception` exceptions, and then handling exceptions based on their types:
```java
public class Test {
    public static void main(String[] args) {
        RetryPolicy policy = new RetryBuilder()
         .retryCondition(new MaxRetries(10))
         .build();
        try {
            policy.call(() -> {
                throw new IOException("error");
            });
        } catch (Exception e) {
            if (e instanceof IOException) {
                // handle IOException
            } else {
                // handle other exceptions
            }
        }
    }
}
```
Or you can use `RetryPolicy.callWithThrows`, which declares `Exception` exceptions in the method signature, so you can:
```java
public class Test {
    public static void main(String[] args) {
        RetryPolicy policy = new RetryBuilder()
        .retryCondition(new MaxRetries(10))
        .build();
        try {
            policy.callWithThrows(() -> {
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

## License
[Apache License 2.0](LICENSE)
