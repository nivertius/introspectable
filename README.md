# Introspectable

Easier Java reflections.

## Introduction

Reflections, an access to program elements structure from themselves, although not recommended in applications are
essential when creating frameworks. Java has somewhat complicated and verbose ways to do reflections, sometimes to the
extremes.

Introspectable comes to rescue. This library has few methods that eases common reflection tasks.

### Queries

Queries allow simpler access to reflections objects. They are available under the
`org.perfectable.introspection.query` package. The examples should explain everything.

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

### Types

Native java `java.lang.reflect.Type` is annoyingly hard to interrogate and reason with. To check subtyping. 

#### Example: Parameterized type injection

Injection framework needs to check if concrete implementation of `Supplier` can be injected into field with
parameterized type:

```java
boolean canInject =
    introspect(targetField)
        .typeView()
        .isSuperTypeOf(injectedValue);
```

This check will fail if `targetField` is declared as `Supplier<Node<C> newNodeSupplier` and `injectedValue` is instance
of class `StringSupplier` which implements `Supplier<String>`, as it should. But the check will pass when doing
`targetField.getType().isInstance(injectedValue)`. Using `targetField.getGenericType()` would help, but all the logic
implemented here would need to be recreated.

#### Example: Messaging

To convert an incoming message to an appropriate object, messaging needs to know actual type parameter for consumer:

```java
Class<?> targetClass =
    introspect(MessageConsumer.class)
        .view()
        .resolve(messageConsumer.getClass())
        .parameter(0)
        .erasure()
```

Lets say we have following declarations:
```java
interface MessageConsumer<M extends Message, S extends State> {}
class LoginMessageConsumer<S extends LoginState> implements MessageConsumer<LoginMessage, S> {}
```

Lets say variable `messageConsumer` is of class with erasure `LoginMessageConsumer`. In this case `resolve` allows
creating new type that uses same type variable substitution. Expression
`TypeView.of(Consumer.class).resolve(messageConsumer.getClass())` will produce `TypeView` with synthetic
parameterized type `MessageConsumer<LoginMessage, S>` because parameter `M` in `MessageConsumer` was substituted
with `LoginMessage` in declaration of class `LoginMessageConsumer`.

This is a simplified case, because in this situation calling `Class#getGenericSuperclass`. But even then, this needs
to be casted onto `ParameterizedType`, first parameter needs to be extracted, casted to `Class` and so on.

### Proxies

Introspectable adds simple facade for creating proxies. Natively, it supports standard JDK proxies and 
javassist + objenesis.

Proxies are built by `org.perfectable.introspection.proxy.ProxyBuilder`. It allows creating proxy by chaining
configuration. After configuration is done, proxies for specific objects can be created by providing
`InvocationHandler`:

#### Example: Remoting

Remoting library needs to replace method call on proxy with message transmission:

```java
Object proxy = 
    ProxyBuilder.ofType(stubClass).withInterface(Remote.class)
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
    ProxyBuilder.ofInterface(UserService.class)
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

### Annotation building

Sometimes you just need to get instance of annotation type. Be it library interface which assumes that you will extract
the annotation from element, or some method call just requires annotation, assuming that you extract. In either case, you
can build it using `AnnotationBuilder`.

#### Example: Named injection

Injection framework requires you to provide `javax.inject.Named` annotation instance:

```java
Named instance =
    AnnotationBuilder.of(Named.class)
        .with(Named::value, "fantastic")
        .build()
```

### Functional references

Sometimes it would be nice to have properties or method compile-time checked to match requested signature. You can use
`FunctionalReference` superinterface to make functional interfaces introspectable.

#### Example: Annotation building

Introspection framework uses annotation methods to build annotations:

```java
@FunctionalInterface
public interface MemberExtractor<A, X> extends FunctionalReference {
    X extract(A annotation);
}

class AnnotationBuilder<A> {
    public <X> AnnotationBuilder<A> with(MemberExtractor<A, X> member, X value) {
        String name = member.introspect().referencedMethodName();
        return withMember(name, value); // private, safe
    }
    <...>
}
```

#### Example: Factory methods

Injection framework needs to extract factory class and product class from provided factory method:

```java
@FunctionalInterface
public interface Factory<F, P> extends FunctionalReference {
    P build(F factory);
}

class FactoryInjector<F, P> {
    public static <F, P> FactoryInjector<F, P> of(Factory<F, P> factory) {
        FunctionalReference.Introspection introspection = factory.introspect(); // this kinda slow
        Class<F> factoryClass = (Class<F>) introspection.resultType();
        Class<P> productClass = (Class<P>) introspection.parameterType(0); // safe
        return of(factoryClass, productClass, factory);
    }
    <...>
}
```

### Beans

A lot of code still uses Java Beans as data container. Package `org.perfectable.introspection.bean` helps managing those:

#### Example: Form binding

```java
Bean<User> userBean = BeanSchema.of(User.class).instantiate();
form.forEachField((fieldName, value) ->
    userBean.property(fieldName).set(value));
User boundUser = userBean.contents();
```

## How to use

Add as dependency:

```xml
<dependency>
    <groupId>org.perfectable</groupId>
    <artifactId>introspectable</artifactId>
    <version>4.0.0-SNAPSHOT</version>
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
