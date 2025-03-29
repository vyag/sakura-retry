[![Maven Central Version](https://img.shields.io/maven-central/v/com.github.marks-yag/retry)](https://maven-badges.herokuapp.com/maven-central/com.github.marks-yag/retry)
![Maven Central Last Update](https://img.shields.io/maven-central/last-update/com.github.marks-yag/retry)
[![Java CI with Maven](https://github.com/marks-yag/retry/actions/workflows/maven.yml/badge.svg)](https://github.com/marks-yag/retry/actions/workflows/maven.yml)

[English](README.md) | 中文

# Retry
一个JVM上追求优雅（🌝）的轻量级重试框架，支持：
- 高度可定制的的重试策略、退避策略和失败监听能力。
- 支持同步调用和异步提交。
- 将定义好的重试策略以AOP的方式增强于代理对象。

# 开始使用
Retry可以通过[Maven 中心仓](https://mvnrepository.com/artifact/com.github.marks-yag/retry)获取。

## 示例
Kotlin用户可以使用带缺省参数的构造函数直接创建`RetryPolicy`:
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
当然也可以使用`RetryBuilder`来创建`RetryPolicy`:
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
Java用户建议通过`RetryBuilder`来创建`RetryPolicy`:
```java
public class Test {
    public static void main(String[] args) {
        RetryPolicy policy = new RetryBuilder(MaxRetries(10), FixedDelay(Duration.ofSeconds(1)))
            .addFailureListener(MyFailureListener())
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

# 设计理念
不同于SpringRetry，**Retry并未提供**注解式的重试策略声明，而是推荐用户通过编程式的方式来定义可复用的重试策略，并灵活地应用于需要重试的业务逻辑。这样的好处是：具体业务逻辑实现与重试策略解耦，我们可以根据需要为同样的业务逻辑动态选择不同的重试策略，不会受到注解声明编译期常量的限制。

**Retry**的重试策略定义包含以下要素的组合，并提供了常用的内置实现：
- 重试条件：定义了何时触发重试。
- 终止条件：定义了何时终止重试。当重试条件不满足或者终止条件满足时，重试将停止。
- 退避策略：定义了重试之间的等待时间间隔。
- 失败监听器：定义了在重试失败时的处理逻辑。包括日志输出、自定义的故障恢复逻辑等。

不同于另一些重试框架，**Retry**目前也**不提供**基于返回值的重试策略，而是**仅通过异常**来判断是否需要重试。调用者可以通过自己将不符合预期的返回值封装为异常抛出。

# 内置重试条件
- `Conditions.MaxRetries(amount)`：最大重试（不包含首次执行）次数。
- `Conditions.MaxAttempts(amount)`：最大尝试（包含首次执行）次数。
- `Conditions.MaxTimeElapsed(duration)`：最大尝试等待时间。
- `Conditions.ExceptionOf(types)`：指定的异常类型。
- `Conditions.TRUE`：总是返回true。
- `Conditions.FALSE`：总是返回false。
- `Conditions.UNRECOVERABLE_EXCEPTIONS`：按照通常认知，没有重试价值的异常类型（比如`InterruptedException`, `RuntimeException`和`Error`），也是`RetryPolicy`的默认终止条件。

# 内置退避策略
- `BackoffPolicies.FixedDelay(duration)`：固定的退避时间。
- `BackoffPolicies.FixedInterval(duration)`：固定的开始时间间隔，如果某次尝试执行的时间超过了间隔，那么下一次尝试将立即开始。
- `BackoffPolicies.Exponential(initDuration, maxDuration)`：指数退避。
- `BackoffPolicies.NONE`：不进行退避，它也是`RetryPolicy`的默认退避策略。
- 
# 内置失败监听器
- `FailureListeners.SimpleLoggingFailureListener(log, stack)`：简单日志输出。同时也是`RetryPolicy`默认添加的失败监听器。

## License
[Apache License 2.0](LICENSE)
