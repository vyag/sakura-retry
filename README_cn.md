[![Maven Central Version](https://img.shields.io/maven-central/v/com.github.marks-yag/sakura-retry)](https://maven-badges.herokuapp.com/maven-central/com.github.marks-yag/sakura-retry)
![Maven Central Last Update](https://img.shields.io/maven-central/last-update/com.github.marks-yag/sakura-retry)
[![Java CI with Maven](https://github.com/marks-yag/sakura-retry/actions/workflows/maven.yml/badge.svg)](https://github.com/vyag/sakura-retry/actions/workflows/maven.yml)

[English](README.md) | 中文

# Sakura Retry

一个JVM上追求优雅（无用🌝）的轻量级重试框架，支持：

- 高度可定制的的重试策略、退避策略和失败监听能力。
- 支持同步调用和异步提交。
- 将定义好的重试策略以AOP的方式增强于代理对象。

Sakura Retry并不试图解决其它老牌重试框架不能解决的问题，也不会提供比它们更好的性能。作者开发Sakura Retry是由于早年间信息闭塞，而继续维护这个项目的原因是Sakura Retry的设计越来越符合作者自己的审美。

# 开始使用

*Sakura Retry*可以通过[Maven 中心仓](https://mvnrepository.com/artifact/com.github.marks-yag/sakura-retry)获取。

**例子：**

```kotlin
fun main() {
    val retryPolicy = maxAttempts(10) and !runtimeException()
    val backoffPolicy = fixedDelayInSeconds(10) + randomDelayInSeconds(0, 1)
    val template = RetryTemplate.Builder(retryPolicy, backoffPolicy)
        .addFailureListener(logging())
        .build()
    template.call {
        println("maybe fail")
    }
}
```

**基本概念：**

- RetryPolicy：是否允许或终止重试。
- BackoffPolicy：重试之间的等待间隔。
- FailureListener：自定义的重试失败处理，比如日志输出、故障告警等。

**Policy组合：**

- `RetryPolicy`设计为可以通过逻辑运算进行自由组合，比如：`maxAttempts(10) and !runtimeException()`，这样就可以通过`RetryPolicy`一个概念来表达复杂的重试策略。
- `BackoffPolicy`也可以进行自由叠加，比如：`fixedDelayInSeconds(10) + randomDelayInSeconds(0, 1)`表示一个10秒的固定延迟，加上一个0到1秒的随机扰动。

更多的例子请查看 [这里](demo/src/main)

# License

[Apache License 2.0](LICENSE)
