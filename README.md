[![Maven Central Version](https://img.shields.io/maven-central/v/com.github.marks-yag/retry)](https://maven-badges.herokuapp.com/maven-central/com.github.marks-yag/retry)
![Maven Central Last Update](https://img.shields.io/maven-central/last-update/com.github.marks-yag/retry)
[![Java CI with Maven](https://github.com/marks-yag/retry/actions/workflows/maven.yml/badge.svg)](https://github.com/marks-yag/retry/actions/workflows/maven.yml)
# Retry
An elegant JVM library for transient failure handling, with customizable retries, backoff strategies, error handling, async support and proxy feature.

## Usage
Retry is available on Maven Central.

```kotlin
import retry.*

val retryPolicy = RetryPolicy(
    retryCondition = MaxRetries(10),
    backOff = BackoffPolicies.seconds(1)
)
retryPolicy.call {
    throw Exception("error")
}
```

```java
import retry.BackoffPolicy;
import retry.Conditions;
import retry.RetryPolicy;

RetryPolicy retryPolicy = new RetryBuilder()
    .retryCondition(Conditions.TRUE)
    .backoffPolicy(BackoffPolicies.seconds(1))
    .build();
retryPolicy.call(() ->{
    throw new Exception("error");
});
```
## License
[Apache License 2.0](LICENSE)
