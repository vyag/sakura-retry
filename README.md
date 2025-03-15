![Maven Central Version](https://img.shields.io/maven-central/v/com.github.marks-yag/retry)
[![Java CI with Maven](https://github.com/marks-yag/retry/actions/workflows/maven.yml/badge.svg)](https://github.com/marks-yag/retry/actions/workflows/maven.yml)
# Retry
An elegant JVM library for transient failure handling, with customizable retries, backoff strategies, error handling, async support and proxy feature.

## Usage
Retry is available on Maven Central.

```kotlin
import retry.*

val retry = Retry(
    retryCondition = MaxRetries(10),
    backOff = BackOff.NONE
)
retry.call {
    throw Exception("error")
}
```

```java
import retry.BackOff;
import retry.Retry;

Retry retry = new RetryBuilder().maxRetries(10).backOff(BackOff.seconds(1)).build();
retry.call(() -> {
    throw new Exception("error");
});
```

##Maven
```xml
<dependency>
    <groupId>io.github.marks-yag</groupId>
    <artifactId>retry</artifactId>
    <version>1.0.0</version>
</dependency>
```
## Gradle
```groovy
implementation 'io.github.marks-yag:retry:1.0.0'
```
## License
[Apache License 2.0](LICENSE)
