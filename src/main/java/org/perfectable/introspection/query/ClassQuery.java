package org.perfectable.introspection.query; // SUPPRESS FILE FileLength

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import javax.annotation.Nullable;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

import static java.util.Objects.requireNonNull;

/**
 * Iterable-like container that searches for classes.
 *
 * <p>This is straightforward method to search for classes that have specified characteristics when one doesn't have
 * their name.
 *
 * <p>Instances of this class are immutable, each filtering produces new, modified instance. To obtain unrestricted
 * query, use {@link #system()} or {@link #of(ClassLoader)}.
 *
 * <p>To obtain results either iterate this class with {@link #iterator} (or in enhanced-for loop) or use one of
 * {@link #stream()}, {@link #unique()}, {@link #option()} or {@link #isPresent()}.
 *
 * <p>This query works in three stages, from fastest to slowest. First it reads classpath entries looking for class
 * files. At this point it filters for package and class names. These are the fastest filters that can immeasurably
 * speed up querying, and it is suggested that all queries should have at least one of these filters.
 *
 * <p>Then, it uses Javassist classfile reading, which only parses the classfile, without initializing it. At this
 * point, the query can filter for things like checking if annotation is present or if interface is implemented.
 * These filters are much faster that conventional classloading (because class dependencies don't have to be loaded and
 * no class initialization actually occurs).
 *
 * <p>Lastly, class is loaded and arbitrary filtering on it is applied.
 *
 * <p>If unrestricted, this query is very slow, as it will load all classes either in classloader or on classpath!
 *
 * <p>Example usage, which registers all classes in package "org.perfectable" that implements
 * {@link java.io.Serializable} and are annotated by javax.annotation.Generated:
 * <pre>
 *     ClassQuery.of(Application.class.getClassloader())
 *         .inPackage("org.perfectable")
 *         .subtypeOf(java.io.Serializable.class)
 *         .annotatedBy(javax.annotation.Generated.class)
 *         .stream()
 *         .forEach(this::register);
 * </pre>
 *
 * @param <C>
 *     Base type for returned classes.
 */
public final class ClassQuery<C> extends AbstractQuery<Class<? extends C>, ClassQuery<C>> {
	private static final Predicate<? super String> DEFAULT_CLASSNAME_FILTER = className -> true;
	private static final Predicate<? super CtClass> DEFAULT_PRE_LOAD_FILTER = ctClass -> true;
	private static final Predicate<? super Class<?>> DEFAULT_POST_LOAD_FILTER = type -> true;

	private static final String CLASS_FILE_SUFFIX = ".class";

	private static final ClassQuery<Object> SYSTEM =  // SUPPRESS AvoidFieldNameMatchingMethodName
		new ClassQuery<>(Object.class, ClassPathResourceSource.INSTANCE, ClassPool.getDefault(),
			name -> ClassLoader.getSystemClassLoader().loadClass(name),
			DEFAULT_CLASSNAME_FILTER, DEFAULT_PRE_LOAD_FILTER, DEFAULT_POST_LOAD_FILTER);

	private final ResourceSource resources;
	private final ClassPool classPool;
	private final TypeLoader loader;
	private final Class<? extends C> castedType;
	private final Predicate<? super String> classNameFilter;
	private final Predicate<? super CtClass> preLoadFilter;
	private final Predicate<? super Class<? extends C>> postLoadFilter;

	/**
	 * Queries for all classes reachable from declared classpath.
	 *
	 * @return query for system classloader classes.
	 */
	public static ClassQuery<Object> system() {
		return ClassQuery.SYSTEM;
	}

	/**
	 * Queries for classes reachable by specified classloader.
	 *
	 * @param loader classloader to introspect
	 * @return query for classes in classloader
	 */
	public static ClassQuery<Object> of(ClassLoader loader) {
		requireNonNull(loader);
		ClassPool classPool1 = new ClassPool();
		classPool1.appendClassPath(new LoaderClassPath(loader));
		return new ClassQuery<>(Object.class, ClassLoaderResourceSource.of(loader), classPool1,
			loader::loadClass, DEFAULT_CLASSNAME_FILTER, DEFAULT_PRE_LOAD_FILTER, DEFAULT_POST_LOAD_FILTER);
	}

	private ClassQuery(Class<? extends C> castedType, // SUPPRESS ParameterNumber
					   ResourceSource resources, ClassPool classPool,
					   TypeLoader loader, Predicate<? super String> classNameFilter,
					   Predicate<? super CtClass> preLoadFilter,
					   Predicate<? super Class<? extends C>> postLoadFilter) {
		this.castedType = castedType;
		this.resources = resources;
		this.classPool = classPool;
		this.loader = loader;
		this.classNameFilter = classNameFilter;
		this.preLoadFilter = preLoadFilter;
		this.postLoadFilter = postLoadFilter;
	}

	/**
	 * Restricts query to classes that are subtype of specified type.
	 *
	 * <p>This restriction works on unloaded classes and gives some speedup when obtaining results of query.
	 *
	 * @param supertype class which will be supertype of any result returned by this query
	 * @param <X> new supertype bound
	 * @return query for classes with specific supertype
	 */
	public <X extends C> ClassQuery<X> subtypeOf(Class<? extends X> supertype) {
		@SuppressWarnings("unchecked")
		Predicate<? super CtClass> newPreLoadFilter =
			((Predicate<CtClass>) preLoadFilter).and(SubtypePredicate.of(supertype));
		return new ClassQuery<X>(supertype, resources, classPool, loader,
			classNameFilter, newPreLoadFilter, DEFAULT_POST_LOAD_FILTER);
	}

	/**
	 * Restricts query to classes that in specified package or its descendants.
	 *
	 * <p>This restriction works on class names and gives generous speedup when obtaining results of query.
	 *
	 * @param filteredPackageName name of the package which will restrict results
	 * @return query that returns classes in specified package
	 */
	public ClassQuery<C> inPackage(String filteredPackageName) {
		return withClassNameFilter(PackageNamePredicate.of(filteredPackageName));
	}

	/**
	 * Restricts query to classes that in specified package or its descendants.
	 *
	 * <p>This restriction works on class names and gives generous speedup when obtaining results of query.
	 *
	 * @param filteredPackage package which will restrict results
	 * @return query that returns classes in specified package
	 */
	public ClassQuery<C> inPackage(Package filteredPackage) {
		return inPackage(filteredPackage.getName());
	}

	/**
	 * Restricts query to classes that matches specified predicate.
	 *
	 * <p>This restriction works on loaded classes and gives no speedup when obtaining results of query.
	 */
	@Override
	public ClassQuery<C> filter(Predicate<? super Class<? extends C>> filter) {
		@SuppressWarnings("unchecked")
		Predicate<? super Class<? extends C>> newPostLoadFilter =
			((Predicate<Class<? extends C>>) postLoadFilter).and(filter);
		return new ClassQuery<C>(castedType, resources, classPool, loader,
			classNameFilter, preLoadFilter, newPostLoadFilter);
	}

	/**
	 * Restricts query to classes that are annotated by annotation with specified class.
	 *
	 * <p>This restriction works on unloaded classes and gives some speedup when obtaining results of query.
	 *
	 * @param annotation annotation that must be present on class to be returned
	 * @return query that returns only classes that have specific annotation
	 */
	public ClassQuery<C> annotatedWith(Class<? extends Annotation> annotation) {
		return annotatedWith(AnnotationFilter.single(annotation));
	}

	/**
	 * Restricts query to classes that matches specified annotation filter.
	 *
	 * <p>This restriction works on unloaded classes and gives some speedup when obtaining results of query.
	 *
	 * @param annotationFilter filter for annotation on class
	 * @return query that returns classes matching specified filter
	 */
	public ClassQuery<C> annotatedWith(AnnotationFilter annotationFilter) {
		return withPreLoadFilter(AnnotationPredicate.of(annotationFilter));
	}

	@Override
	public Stream<Class<? extends C>> stream() {
		return resources.entries()
			.filter(ClassQuery::isClass)
			.map(ClassQuery::getClassName)
			.filter(classNameFilter)
			.map(this::preload)
			.filter(preLoadFilter)
			.map(this::load)
			.flatMap(com.google.common.collect.Streams::stream)
			.filter(postLoadFilter);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean contains(Object candidate) {
		if (!(candidate instanceof Class<?>)) {
			return false;
		}
		Class<? extends C> candidateClass = (Class<? extends C>) candidate;
		if (!DEFAULT_CLASSNAME_FILTER.equals(classNameFilter) && !classNameFilter.test(candidateClass.getName())) {
			return false;
		}
		if (!DEFAULT_POST_LOAD_FILTER.equals(postLoadFilter) && !postLoadFilter.test(candidateClass)) {
			return false;
		}
		if (!DEFAULT_PRE_LOAD_FILTER.equals(preLoadFilter)) {
			CtClass preloaded = preload(candidateClass.getName());
			if (!preLoadFilter.test(preloaded)) {
				return false;
			}
		}
		return resources.contains(getClassPath(candidateClass));
	}



	private ClassQuery<C> withClassNameFilter(Predicate<? super String> additionalClassNameFilter) {
		@SuppressWarnings("unchecked")
		Predicate<? super String> newClassNameFilter =
			((Predicate<String>) classNameFilter).and(additionalClassNameFilter);
		return new ClassQuery<>(castedType, resources, classPool, loader,
			newClassNameFilter, preLoadFilter, DEFAULT_POST_LOAD_FILTER);
	}

	private ClassQuery<C> withPreLoadFilter(Predicate<? super CtClass> additionalPreLoadFilter) {
		@SuppressWarnings("unchecked")
		Predicate<? super CtClass> newPreLoadFilter =
			((Predicate<CtClass>) preLoadFilter).and(additionalPreLoadFilter);
		return new ClassQuery<>(castedType, resources, classPool, loader,
			classNameFilter, newPreLoadFilter, DEFAULT_POST_LOAD_FILTER);
	}

	private static boolean isClass(String path) {
		return path.endsWith(CLASS_FILE_SUFFIX)
			&& !path.endsWith("package-info.class")
			&& !path.endsWith("module-info.class");
	}

	private static String getClassName(String path) {
		int classNameEnd = path.length() - CLASS_FILE_SUFFIX.length();
		return path.substring(0, classNameEnd).replace('/', '.');
	}

	private static String getClassPath(Class<?> resolvedClass) {
		return resolvedClass.getName().replace('.', '/') + CLASS_FILE_SUFFIX;
	}

	private CtClass preload(String className) {
		try {
			return classPool.get(className);
		}
		catch (NotFoundException e) {
			throw new AssertionError(e);
		}
	}

	private Optional<Class<? extends C>> load(CtClass preloadedClass) {
		try {
			Class<? extends C> loaded = loader.load(preloadedClass.getName())
				.asSubclass(castedType);
			return Optional.of(loaded);
		}
		catch (Throwable e) { // SUPPRESS IllegalCatch
			// although this should only throw ClassNotFoundException or NoClassDefFoundError,
			// lot of different exception occurs while loading classes and if it happens, Class is not loadable anyway
			return Optional.empty();
		}
	}

	private static final class PackageNamePredicate implements Predicate<String> {
		private final String filteredPackageName;

		public static PackageNamePredicate of(String filteredPackageName) {
			return new PackageNamePredicate(filteredPackageName);
		}

		private PackageNamePredicate(String filteredPackageName) {
			this.filteredPackageName = filteredPackageName;
		}

		@Override
		public boolean test(String className) {
			return className.startsWith(filteredPackageName);
		}
	}

	private static final class AnnotationPredicate implements Predicate<CtClass> {
		private final AnnotationFilter annotationFilter;

		public static AnnotationPredicate of(AnnotationFilter annotationFilter) {
			return new AnnotationPredicate(annotationFilter);
		}

		private AnnotationPredicate(AnnotationFilter annotationFilter) {
			this.annotationFilter = annotationFilter;
		}

		@Override
		public boolean test(CtClass preloadedClass) {
			return annotationFilter.matches(CtClassAnnotatedElementAdapter.adapt(preloadedClass));
		}

	}

	private static final class SubtypePredicate implements Predicate<CtClass> {
		private final Class<?> supertype;

		public static SubtypePredicate of(Class<?> supertype) {
			return new SubtypePredicate(supertype);
		}

		private SubtypePredicate(Class<?> supertype) {
			this.supertype = supertype;
		}

		@Override
		public boolean test(CtClass ctClass) {
			return ctClass.getName().equals(supertype.getName())
				|| testSuperclass(ctClass)
				|| testInterfaces(ctClass);
		}

		private boolean testSuperclass(CtClass ctClass) {
			CtClass superclass;
			try {
				superclass = ctClass.getSuperclass();
			}
			catch (Exception ignored) { // SUPPRESS IllegalCatch
				return false;
			}
			return superclass != null && test(superclass);
		}

		private boolean testInterfaces(CtClass ctClass) {
			CtClass[] ctInterfaces;
			try {
				ctInterfaces = ctClass.getInterfaces();
			}
			catch (Exception ignored) { // SUPPRESS IllegalCatch
				return false;
			}
			for (CtClass ctInterface : ctInterfaces) {
				if (test(ctInterface)) {
					return true;
				}
			}
			return false;
		}
	}

	private interface TypeLoader {
		Class<?> load(String typeName) throws ClassNotFoundException;
	}

	private interface ResourceSource {
		Stream<String> entries();

		boolean contains(String candidate);
	}

	private abstract static class UrlResourceSource implements ResourceSource {
		public static final Splitter MANIFEST_CLASSPATH_ENTRY_SPLITTER = Splitter.on(" ");

		@Override
		public Stream<String> entries() {
			Set<File> visited = new HashSet<>();
			ImmutableSet.Builder<String> resultBuilder = ImmutableSet.builder();
			generateUrls(url -> generateUrlEntry(url, resultBuilder, visited));
			return resultBuilder.build().stream();
		}

		@Override
		public boolean contains(String candidate) {
			return entries().anyMatch(candidate::equals);
		}

		protected abstract void generateUrls(Consumer<URL> urlAction);

		private static void generateUrlEntry(URL url, ImmutableSet.Builder<String> resultBuilder, Set<File> visited) {
			File file = new File(url.getFile());
			if (visited.contains(file)) {
				return;
			}
			visited.add(file);
			if (file.isDirectory()) {
				generateDirectoryEntries(file, resultBuilder);
			}
			else {
				generateJarEntries(file, resultBuilder, visited);
			}
		}

		// SUPPRESS NEXT 1 MethodLength
		private static void generateJarEntries(File jarPath,
											   ImmutableSet.Builder<String> resultBuilder,
											   Set<File> visited) {
			@Nullable String manifestClassPath;
			try (JarFile jarFile = new JarFile(jarPath)) {
				manifestClassPath = getManifestClassPathString(jarFile);
				Streams.from(jarFile.entries())
					.filter(entry -> !entry.isDirectory())
					.map(ZipEntry::getName)
					.forEach(resultBuilder::add);
			}
			catch (IOException ignored) {
				// could not open jar file, probably not jar
				return;
			}
			if (manifestClassPath == null) {
				return;
			}
			for (String manifestEntry : MANIFEST_CLASSPATH_ENTRY_SPLITTER.split(manifestClassPath)) {
				URL manifestUrl = safeToUrl(manifestEntry);
				generateUrlEntry(manifestUrl, resultBuilder, visited);
			}
		}

		private static void generateDirectoryEntries(File directoryBase, ImmutableSet.Builder<String> resultBuilder) {
			Path basePath = Paths.get(directoryBase.getPath());
			try {
				Files.walkFileTree(basePath, new ResultAddingFileVisitor(resultBuilder, basePath));
			}
			catch (IOException e) {
				throw new AssertionError(e);
			}
		}

		@Nullable
		private static String getManifestClassPathString(JarFile jarFile) throws IOException {
			Manifest manifest = jarFile.getManifest();
			if (manifest == null) {
				return null;
			}
			return manifest.getMainAttributes().getValue(Attributes.Name.CLASS_PATH);
		}

		protected static URL safeToUrl(String entry) {
			try {
				return new URL(entry);
			}
			catch (MalformedURLException e) {
				throw new AssertionError(e);
			}
		}

		private static class ResultAddingFileVisitor extends SimpleFileVisitor<Path> {
			private final ImmutableSet.Builder<String> resultBuilder;
			private final Path basePath;

			ResultAddingFileVisitor(ImmutableSet.Builder<String> resultBuilder, Path basePath) {
				this.resultBuilder = resultBuilder;
				this.basePath = basePath;
			}

			@Override
			public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) {
				Path relativePath = basePath.relativize(path);
				resultBuilder.add(relativePath.toString());
				return FileVisitResult.CONTINUE;
			}
		}
	}

	private static final class ClassPathResourceSource extends UrlResourceSource {
		static final ClassPathResourceSource INSTANCE = new ClassPathResourceSource();

		private static final Splitter CLASSPATH_SPLITTER = Splitter.on(':');
		private static final String ENTRY_URL_PREFIX = "file://";

		@Override
		protected void generateUrls(Consumer<URL> urlAction) {
			String classPathString = System.getProperty("java.class.path");
			Iterable<String> classPathEntries = CLASSPATH_SPLITTER.split(classPathString);
			for (String entry : classPathEntries) {
				if (entry.endsWith("*")) {
					throw new AssertionError("Wild-carded classpath is unsupported");
				}
				URL url = safeToUrl(ENTRY_URL_PREFIX + entry);
				urlAction.accept(url);
			}
		}
	}

	private static final class ClassLoaderResourceSource extends UrlResourceSource {
		private final ClassLoader classLoader;

		public static ClassLoaderResourceSource of(ClassLoader classLoader) {
			return new ClassLoaderResourceSource(classLoader);
		}

		private ClassLoaderResourceSource(ClassLoader classLoader) {
			this.classLoader = classLoader;
		}

		@Override
		protected void generateUrls(Consumer<URL> urlAction) {
			ClassLoader currentClassLoader = classLoader;
			while (currentClassLoader != null) {
				if (currentClassLoader instanceof URLClassLoader) {
					URLClassLoader urlClassLoader = (URLClassLoader) currentClassLoader;
					for (URL url : urlClassLoader.getURLs()) {
						urlAction.accept(url);
					}
				}
				currentClassLoader = currentClassLoader.getParent();
			}
		}
	}

	private static final class CtClassAnnotatedElementAdapter implements AnnotatedElement {
		private final CtClass ctClass;

		static CtClassAnnotatedElementAdapter adapt(CtClass preloadedClass) {
			return new CtClassAnnotatedElementAdapter(preloadedClass);
		}

		private CtClassAnnotatedElementAdapter(CtClass ctClass) {
			this.ctClass = ctClass;
		}

		@Nullable
		@SuppressWarnings("unchecked")
		@Override
		public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
			try {
				return (T) ctClass.getAnnotation(annotationClass);
			}
			catch (Exception e) { // SUPPRESS IllegalCatch
				return null;
			}
		}

		@Override
		public Annotation[] getAnnotations() {
			throw new UnsupportedOperationException("Inherited annotation fetching is not currently supported");
		}

		@Override
		public Annotation[] getDeclaredAnnotations() {
			return (Annotation[]) ctClass.getAvailableAnnotations();
		}
	}

}
