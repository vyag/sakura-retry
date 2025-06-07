[![Maven Central Version](https://img.shields.io/maven-central/v/com.github.marks-yag/sakura-retry)](https://maven-badges.herokuapp.com/maven-central/com.github.marks-yag/sakura-retry)
![Maven Central Last Update](https://img.shields.io/maven-central/last-update/com.github.marks-yag/sakura-retry)
[![Java CI with Maven](https://github.com/vyag/sakura-retry/actions/workflows/maven.yml/badge.svg)](https://github.com/vyag/sakura-retry/actions/workflows/maven.yml)

[中文](README_cn.md) | English

# Sakura Retry

An elegant(useless😉) and lightweight retry framework for JVM which supports:

- Highly customizable retry and backoff policies.
- Both synchronous and asynchronous execution.
- Apply pre-defined retry template on existing object using dynamic proxy.

*Sakura Retry* does **NOT** attempt to support more features than other mainstream retry frameworks, nor does it offer better performance. In the early years, I started writing *Sakura Retry* due to a lack of information, and I have kept using it because I like it more and more in terms of aesthetics.

# Getting Started

Available on [Maven Central](https://mvnrepository.com/artifact/com.github.marks-yag/sakura-retry).

**Example：**

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

**Basic Concepts:**

- **RetryPolicy**: Determines whether to trigger or terminate retries.
- **BackoffPolicy**: Defines the waiting interval between retries.
- **FailureListener**: Handles (e.g., logging, alerting) upon execution failures.

**Policy Composition:**

- **RetryPolicy** supports logical composition (e.g., `maxAttempts(10) and !runtimeException()`) to express complex retry strategies through a single concept.
- **BackoffPolicy** allows combinable configurations (e.g., `fixedDelayInSeconds(10) + randomDelayInSeconds(0, 1)`), which represents a 10-second fixed delay with an additional 0–1 second random jitter.

Find more examples [here](demo/src/main).

# License

[Apache License 2.0](LICENSE)
