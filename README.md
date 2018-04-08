# Introspectable

Easier Java reflections.

## Introduction

Reflections, an access to program elements structure from themselves, although not recommended in applications are essential 
when creating frameworks. Java has somewhat complicated and verbose ways to do reflections, sometimes to the extremes.

Introspectable comes to rescue. This library has few methods that eases common reflection tasks.

### Queries

Queries allow simpler access to reflections objects from their source: a class.  

#### Example: Test framework 

To inject created mocks, it needs to find all non-static fields in test object, that have annotation <code>@Mock</code> 
present.

```java
Stream<Field> injectedMocks = 
    introspect(testObject.getClass())
        .fields()
        .excludingModifier(Modifier.STATIC)
        .annotatedWith(Mock.class)
        .stream();
```

#### Example: Remoting library 

To create remote proxy, it needs to know all the interfaces implemented by provided class, that extend 
<code>Remote</code> interface, but excluding remote, and their name ends with <code>Rpc</code>.

```java
Iterable<? extends Class<?>> interfaces = 
    introspect(proxiedClass)
        .interfaces()
        .filter(candidate -> candidate.getSimpleName().endsWith("Rpc"))
        .upToExcluding(Remote.class)
        .filter(Remote.class::isAssignableFrom);
```

#### Example: Form binding

To clear form, binding library needs to know all single-parameter void non-static method that are named 
<code>apply</code>: 

```java
Stream<Method> appliers = 
    introspect(target.getClass())
        .methods()
        .named("apply")
        .parameterCount(1)
        .returningVoid()
        .excludingModifier(Modifier.STATIC)
        .stream();
```

#### Example: Messaging

To convert an incoming message to an appropriate object, messaging needs to know actual type parameter for consumer:  

```java
Class<?> targetClass = 
    introspect(Consumer.class)
        .generics()
        .parameter(0)
        .resolve(messageConsumer);
```

#### Example: Dependency injection

To inject new instances with required dependencies, IoC container needs to know which constructor is injected:

```java
Constructor<C> injectedConstructor =
    introspect(instanceClass)
        .constructors()
        .annotatedWith(Inject.class)
        .requiringModifier(Modifier.PUBLIC)
        .option()
        .orElseThrow(MissingInjectableConstructor::new);
```

#### Example: Servlet Discovery

To discover and register all servlets, web application needs to scan classpath for `Servlet` classes to instantiate:

```java
introspect(Application.class.getClassLoader())
    .classes()
    .inPackage(Application.class.getPackage())
    .subtypeOf(Servlet.class)
    .stream()
    .map(Introspections::introspect)
    .map(ClassIntrospection::instantiate)
    .forEach(this::registerServlet);
```

### Proxies

Introspectable adds simple facade for creating proxies. Natively, it supports standard JDK proxies and 
javassist + objenesis.

Proxies are built from <code>ProxyBuilder</code> which in turn is created from <code>ProxyBuilderFactory</code>.
Then, proxies for specific objects can be created by providing <code>InvocationHandler</code>:

#### Example: Remoting

Remoting library needs to replace method call on proxy with message transmission:

```java
Object proxy = 
    ProxyBuilderFactory.withFeature(Feature.SUPERCLASS)
        .ofType(stubClass, Remote.class)
        .instantiate(invocation -> {
                Transmission transmission =
                    invocation.decompose((method, target, parameters) ->
                        channel.transmit(method.getName(), proxyName, serialize(parameters)));
                return transmission.readReplay();
            });
```

#### Example: Call logging

`UserService` service calls needs to be logged:

```java
UserService proxy =
    ProxyBuilderFactory.any()
        .ofType(UserService.class)
        .instantiate(invocation -> {
                String source = invocation.decompose((method, target, parameters) ->
                    method.getDeclaringClass() + "#" + method.getName());
                LOGGER.info("Before call on {}", source);
                try {
                    return invocation.withReceiver(realService).invoke();
                }
                finally {
                    LOGGER.info("After call on {}", source);
                }
            });
```

## How to use

Add as dependency:

```xml
<dependency>
    <groupId>org.perfectable</groupId>
    <artifactId>introspectable</artifactId>
    <version>2.0.1-SNAPSHOT</version>
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
