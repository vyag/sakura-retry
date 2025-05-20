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

- [Kotlin简单示例](demo/src/main/kotlin/retry/demo/kotlin/Simple.kt)
- [Java简单示例](demo/src/main/java/retry/demo/java/Simple.java)
- [Kotlin异步重试示例](demo/src/main/kotlin/retry/demo/kotlin/Async.kt)
- [Java异步重试示例](demo/src/main/java/retry/demo/java/Async.java)
- [Kotlin代理重试示例](demo/src/main/kotlin/retry/demo/kotlin/Proxy.kt)
- [Java代理重试示例](demo/src/main/java/retry/demo/java/Proxy.java)

# 设计理念
不同于SpringRetry，**Retry并未提供**注解式的重试策略声明，而是推荐用户通过编程式的方式来定义可复用的重试策略，并灵活地应用于需要重试的业务逻辑。这样的好处是：具体业务逻辑实现与重试策略解耦，我们可以根据需要为同样的业务逻辑动态选择不同的重试策略，不会受到注解声明编译期常量的限制。

**Retry**的重试策略定义包含以下要素的组合，并提供了常用的内置实现：
- 重试条件：定义了何时触发重试。
- 终止条件：定义了何时终止重试。当重试条件不满足或者终止条件满足时，重试将停止。
- 退避策略：定义了重试之间的等待时间间隔。
- 失败监听器：定义了在重试失败时的处理逻辑。包括日志输出、自定义的故障恢复逻辑等。

不同于另一些重试框架，**Retry**目前也**不提供**基于返回值的重试策略，而是**仅通过异常**来判断是否需要重试。调用者可以通过自己将不符合预期的返回值封装为异常抛出。

# 重试规则
`RetryPolicies`下面提供了一些内置的`RetryPolicy`。

`RetryPolicy`支持逻辑组合：
Kotlin:
```kotlin
val policy = MaxAttempts(10) and MaxTimeElapsed(Duration.ofSeconds(10))
```
Java:
```java
RetryPolicy policy = MaxAttempts(10).and(MaxTimeElapsed(Duration.ofSeconds(10)));
```

# 退避策略
`BackoffPolicies`下面提供了一些内置的`BackoffPolicy`，同时，`BackoffPolicy`支持叠加组合：
Kotlin:
```kotlin
val backoffPolicy = FixedDelay(Duration.ofSeconds(10)) + RandomDelay(Duration.ofSeconds(0), Duration.ofSeconds(10))
```
Java:
```java
BackoffPolicy backoffPolicy = new FixedDelay(Duration.ofSeconds(10)).plus(new RandomDelay(Duration.ofSeconds(0), Duration.ofSeconds(10)));
```

# License
[Apache License 2.0](LICENSE)
