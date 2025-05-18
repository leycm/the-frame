## Introduction

`leyCM/the-frame` is a utility plugin for Java that provides enhanced table/matrix functionality and multi-language content management. The library source is located in `src/main/org/leycm/lang/`.

## Features

- **Translation**: Manage translations with built-in formatting, ideal for internationalized applications.
- **Storage**: Easily handle YAML and JSON files for configuration or data storage.
- **Table**: A flexible Java-style table structure with unlimited rows and columns.

## Requirements

- **Java 17+**
- **Maven 3.6+** (or Gradle for Kotlin projects)

## Installation

### Maven
Add the repository and dependency to your `pom.xml`:
```xml
<repository>
    <id>leyCM.org</id>
    <url>https://leycm.github.io/repository</url>
</repository>

<dependency>
    <groupId>org.leycm.frames</groupId>
    <artifactId>the-frame</artifactId>
    <version>1.2.0</version>
</dependency>
```

### Gradle (Kotlin DSL)
```kotlin
repositories {
    maven("https://leycm.github.io/repository")
}

dependencies {
    implementation("org.leycm.frames:the-frame:1.2.0")
}
```

## Quick Example 
```java
Table myTable = new HashTable(Integer.class, String.class, UUID.class, String.class ... ); // 1 or more columns
myTable.addEntry(15, "Hello", UUID.randomUUID(), "test"); // an index is given by default
String name = myTable.getEntry(0).getValue(1);
System.out.println(name); // Output: "Hello"
```

```java
Storage storage = Storage.of("test/path/file", Storage.Type.JSON, JavaStorage.class);
storage.reload();
storage.set("tes.name", "Hans");
storage.save();

String s = storage.get("test.name", String.class);
System.out.println(s);
```

## Contributing
Contributions are welcome! Fork the repository and submit a pull request for improvements or bug fixes.

## License
MIT License. See [LICENSE](LICENSE) for details. Free to use with attribution.
