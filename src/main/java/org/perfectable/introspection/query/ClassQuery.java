package org.perfectable.introspection.query; // SUPPRESS FileLength

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

public abstract class ClassQuery<C> extends AbstractQuery<Class<? extends C>, ClassQuery<C>> {

	public static ClassQuery<Object> all() {
		return StandardClassQuery.GLOBAL;
	}

	public static ClassQuery<Object> of(ClassLoader loader) {
		requireNonNull(loader);
		return StandardClassQuery.ofClassLoader(loader);
	}

	public abstract <X extends C> ClassQuery<X> subtypeOf(Class<? extends X> supertype);

	public abstract ClassQuery<C> inPackage(String filteredPackageName);

	public final ClassQuery<C> inPackage(Package filteredPackage) {
		return inPackage(filteredPackage.getName());
	}

	@Override
	public ClassQuery<C> filter(Predicate<? super Class<? extends C>> filter) {
		return new Predicated<>(this, filter);
	}

	public final ClassQuery<C> annotatedWith(Class<? extends Annotation> annotation) {
		return annotatedWith(AnnotationFilter.of(annotation));
	}

	public abstract ClassQuery<C> annotatedWith(AnnotationFilter annotationFilter);

	private static final class StandardClassQuery<C> extends ClassQuery<C> {
		private static final Predicate<? super String> DEFAULT_CLASSNAME_FILTER = className -> true;
		private static final Predicate<? super CtClass> DEFAULT_PRE_LOAD_FILTER = ctClass -> true;

		private static final StandardClassQuery<Object> GLOBAL =
			new StandardClassQuery<>(Object.class, GlobalResourceSource.INSTANCE, ClassPool.getDefault(),
				Class::forName, DEFAULT_CLASSNAME_FILTER, DEFAULT_PRE_LOAD_FILTER);

		private static final String CLASS_FILE_SUFFIX = ".class";
		private final ResourceSource resources;
		private final ClassPool classPool;
		private final TypeLoader loader;
		private final Class<? extends C> castedType;
		private final Predicate<? super String> classNameFilter;
		private final Predicate<? super CtClass> preLoadFilter;

		static ClassQuery<Object> ofClassLoader(ClassLoader loader) {
			ClassPool classPool = new ClassPool();
			classPool.appendClassPath(new LoaderClassPath(loader));
			return new StandardClassQuery<>(Object.class, ClassLoaderResourceSource.of(loader), classPool,
				loader::loadClass, DEFAULT_CLASSNAME_FILTER, DEFAULT_PRE_LOAD_FILTER);
		}

		private StandardClassQuery(Class<? extends C> castedType, ResourceSource resources, ClassPool classPool,
								   TypeLoader loader, Predicate<? super String> classNameFilter,
								   Predicate<? super CtClass> preLoadFilter) {
			this.castedType = castedType;
			this.resources = resources;
			this.classPool = classPool;
			this.loader = loader;
			this.classNameFilter = classNameFilter;
			this.preLoadFilter = preLoadFilter;
		}

		@Override
		public ClassQuery<C> inPackage(String newFilteredPackageName) {
			return withClassNameFilter(PackageNamePredicate.of(newFilteredPackageName));
		}

		@Override
		public ClassQuery<C> annotatedWith(AnnotationFilter annotationFilter) {
			return withPreLoadFilter(AnnotationPredicate.of(annotationFilter));
		}

		@Override
		public <X extends C> ClassQuery<X> subtypeOf(Class<? extends X> supertype) {
			@SuppressWarnings("unchecked")
			Predicate<? super CtClass> newPreLoadFilter =
				((Predicate<CtClass>) preLoadFilter).and(SubtypePredicate.of(supertype));
			return new StandardClassQuery<X>(supertype, resources, classPool, loader, classNameFilter, newPreLoadFilter);
		}

		private ClassQuery<C> withClassNameFilter(Predicate<? super String> additionalClassNameFilter) {
			@SuppressWarnings("unchecked")
			Predicate<? super String> newClassNameFilter =
				((Predicate<String>) classNameFilter).and(additionalClassNameFilter);
			return new StandardClassQuery<>(castedType, resources, classPool, loader, newClassNameFilter, preLoadFilter);
		}

		private ClassQuery<C> withPreLoadFilter(Predicate<? super CtClass> additionalPreLoadFilter) {
			@SuppressWarnings("unchecked")
			Predicate<? super CtClass> newPreLoadFilter =
				((Predicate<CtClass>) preLoadFilter).and(additionalPreLoadFilter);
			return new StandardClassQuery<>(castedType, resources, classPool, loader, classNameFilter, newPreLoadFilter);
		}

		@Override
		public Stream<Class<? extends C>> stream() {
			return resources.entries()
				.filter(StandardClassQuery::isClass)
				.map(StandardClassQuery::getClassName)
				.filter(classNameFilter)
				.map(this::preload)
				.filter(preLoadFilter)
				.map(this::load);
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

		private CtClass preload(String className) {
			try {
				return classPool.get(className);
			}
			catch (NotFoundException e) {
				throw new AssertionError(e);
			}
		}

		private Class<? extends C> load(CtClass preloadedClass) {
			try {
				return loader.load(preloadedClass.getName())
					.asSubclass(castedType);
			}
			catch (ClassNotFoundException e) {
				throw new AssertionError(e);
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

			@Override // SUPPRESS Unit4TestShouldUseTestAnnotation
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

			@Override // SUPPRESS Unit4TestShouldUseTestAnnotation
			public boolean test(CtClass preloadedClass) {
				return annotationFilter.matches(CtClassAnnotatedElementAdapter.adapt(preloadedClass));
			}

		}

		private static final class SubtypePredicate implements Predicate<CtClass> {
			private final Class<?> supertype;

			public static Predicate<? super CtClass> of(Class<?> supertype) {
				return new SubtypePredicate(supertype);
			}

			private SubtypePredicate(Class<?> supertype) {
				this.supertype = supertype;
			}

			@Override // SUPPRESS Unit4TestShouldUseTestAnnotation
			public boolean test(CtClass ctClass) {
				if (ctClass.getName().equals(supertype.getName())) {
					return true;
				}
				try {
					CtClass superclass = ctClass.getSuperclass();
					if (superclass != null && test(superclass)) {
						return true;
					}
				}
				catch (NotFoundException ignored) { // SUPPRESS EmptyBlock
					// continue
				}
				try {
					CtClass[] ctInterfaces = ctClass.getInterfaces();
					for (CtClass ctInterface : ctInterfaces) {
						if (test(ctInterface)) {
							return true;
						}
					}
				}
				catch (NotFoundException ignored) { // SUPPRESS EmptyBlock
					// continue
				}
				return false;
			}
		}
	}

	private abstract static class Filtered<C> extends ClassQuery<C> {
		protected final ClassQuery<C> parent;

		Filtered(ClassQuery<C> parent) {
			this.parent = parent;
		}

		protected abstract boolean matches(Class<? extends C> candidate);

		@Override
		public Stream<Class<? extends C>> stream() {
			return this.parent.stream()
				.filter(this::matches);
		}
	}

	private static final class Predicated<C>
		extends Filtered<C> {
		private final Predicate<? super Class<? extends C>> filter;

		Predicated(ClassQuery<C> parent, Predicate<? super Class<? extends C>> filter) {
			super(parent);
			this.filter = filter;
		}

		@Override
		protected boolean matches(Class<? extends C> candidate) {
			return filter.test(candidate);
		}

		@Override
		public ClassQuery<C> inPackage(String filteredPackageName) {
			return new Predicated<>(parent.inPackage(filteredPackageName), filter);
		}

		@Override
		public ClassQuery<C> annotatedWith(AnnotationFilter annotationFilter) {
			return new Predicated<>(parent.annotatedWith(annotationFilter), filter);
		}

		@Override
		public <X extends C> ClassQuery<X> subtypeOf(Class<? extends X> subtype) {
			return new Predicated<>(parent.subtypeOf(subtype), filter);
		}
	}

	private interface TypeLoader {
		Class<?> load(String typeName) throws ClassNotFoundException, NoClassDefFoundError;
	}

	private interface ResourceSource {
		Stream<String> entries();
	}

	private abstract static class UrlResourceSource implements ResourceSource {
		@Override
		public Stream<String> entries() {
			Set<File> visited = new HashSet<>();
			ImmutableSet.Builder<String> resultBuilder = ImmutableSet.builder();
			generateUrls(url -> generateUrlEntry(url, resultBuilder, visited));
			return resultBuilder.build().stream();
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
			try (JarFile jarFile = new JarFile(jarPath);) {
				manifestClassPath = getManifestClassPathString(jarFile);
				Streams.from(jarFile.entries())
					.filter(entry -> !entry.isDirectory())
					.map(ZipEntry::getName)
					.forEach(resultBuilder::add);
			}
			catch (IOException e) {
				throw new AssertionError(e);
			}
			if (manifestClassPath == null) {
				return;
			}
			for (String manifestEntry : manifestClassPath.split(" ")) {
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

	private static final class GlobalResourceSource extends UrlResourceSource {
		static final GlobalResourceSource INSTANCE = new GlobalResourceSource();

		private static final Splitter CLASSPATH_SPLITTER = Splitter.on(':');
		private static final String ENTRY_URL_PREFIX = "file://";

		private GlobalResourceSource() {
			// singleton
		}

		@Override
		protected void generateUrls(Consumer<URL> urlAction) {
			String classPathString = System.getProperty("java.class.path");
			Iterable<String> classPathEntries = CLASSPATH_SPLITTER.split(classPathString);
			for (String entry : classPathEntries) {
				if (entry.endsWith("*")) {
					throw new AssertionError("Wildcarded classpath is unsupported");
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
				if (!(currentClassLoader instanceof URLClassLoader)) {
					continue;
				}
				URLClassLoader urlClassLoader = (URLClassLoader) currentClassLoader;
				for (URL url : urlClassLoader.getURLs()) {
					urlAction.accept(url);
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

		@SuppressWarnings("unchecked")
		@Override
		public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
			try {
				return (T) ctClass.getAnnotation(annotationClass);
			}
			catch (ClassNotFoundException e) {
				throw new AssertionError(e);
			}
		}

		@Override
		public Annotation[] getAnnotations() {
			try {
				return (Annotation[]) ctClass.getAnnotations();
			}
			catch (ClassNotFoundException e) {
				throw new AssertionError(e);
			}
		}

		@Override
		public Annotation[] getDeclaredAnnotations() {
			return (Annotation[]) ctClass.getAvailableAnnotations();
		}
	}

}
