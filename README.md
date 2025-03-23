[![Maven Central Version](https://img.shields.io/maven-central/v/com.github.marks-yag/retry)](https://maven-badges.herokuapp.com/maven-central/com.github.marks-yag/retry)
![Maven Central Last Update](https://img.shields.io/maven-central/last-update/com.github.marks-yag/retry)
[![Java CI with Maven](https://github.com/marks-yag/retry/actions/workflows/maven.yml/badge.svg)](https://github.com/marks-yag/retry/actions/workflows/maven.yml)
# Retry
An elegant JVM library for transient failure handling, with customizable retries, backoff strategies, error handling, async support and proxy feature.

## Usage
Retry is available on Maven Central.

Kotlin:
```kotlin
import retry.*

fun main() {
    val policy = RetryPolicy(
        retryCondition = MaxRetries(10),
        backOff = BackoffPolicies.seconds(1)
    )
    policy.call {
        throw Exception("error")
    }
}
```

Java:
```java
import retry.BackoffPolicy;
import retry.Conditions;
import retry.RetryPolicy;

public class Test {
    public static void main(String[] args) {
        RetryPolicy policy = new RetryBuilder()
            .retryCondition(new MaxRetries(10))
            .backoffPolicy(BackoffPolicies.seconds(1))
            .build();
        policy.call(() -> {
            throw new Exception("error");
        });
    }
}
```
## License
[Apache License 2.0](LICENSE)
