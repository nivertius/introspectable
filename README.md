# Introspectable

Easier Java reflections.

## Introduction

Reflections, a access to program elements structure from themselves, although not recommended in applications are essential 
when creating frameworks. Java has somewhat complicated and verbose ways to do reflections, sometimes to the extremes.

Introspectable comes to rescue. This library has few methods that eases common reflection tasks.

## How to use

Add as dependency:

```xml
<dependency>
    <groupId>org.perfectable</groupId>
    <artifactId>introspectable</artifactId>
    <version>1.0.0</version>
</dependency>
```

Currently, <code>introspectable</code> artifacts is stored on 
[perfectable.org maven repository](https://maven.perfectable.org/) only, so you need to add following entry to your 
repositories:

```xml
<repository>
    <id>perfectable-all</id>
    <name>Perfectable</name>
    <url>https://maven.perfectable.org/repo</url>
</repository>
```
