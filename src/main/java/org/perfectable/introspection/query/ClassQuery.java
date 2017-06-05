package org.perfectable.introspection.query; // SUPPRESS FileLength

import java.io.File;
import java.io.IOException;
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
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;

import static java.util.Objects.requireNonNull;

public abstract class ClassQuery<C> extends AbstractQuery<Class<? extends C>, ClassQuery<C>> {

	private static final ClassPath CLASS_PATH = new ClassPath();

	public static ClassQuery<Object> of(ClassLoader loader) {
		requireNonNull(loader);
		return ClassQuery.OfClassLoader.create(loader);
	}

	public <X extends C> ClassQuery<X> subtypeOf(Class<? extends X> subtype) {
		return new Subtyped<>(this, subtype);
	}

	public abstract ClassQuery<C> inPackage(String filteredPackageName);

	public final ClassQuery<C> inPackage(Package filteredPackage) {
		return inPackage(filteredPackage.getName());
	}

	@Override
	public ClassQuery<C> filter(Predicate<? super Class<? extends C>> filter) {
		return new Predicated<>(this, filter);
	}

	private static final class OfClassLoader extends ClassQuery<Object> {
		private static final String CLASS_FILE_SUFFIX = ".class";
		private final ClassLoader loader;
		private final String filteredPackageName;

		public static ClassQuery<Object> create(ClassLoader loader) {
			return new OfClassLoader(loader, "");
		}

		private OfClassLoader(ClassLoader loader, String filteredPackageName) {
			this.loader = loader;
			this.filteredPackageName = filteredPackageName;
		}


		@Override
		public ClassQuery<Object> inPackage(String newFilteredPackageName) {
			requireNonNull(newFilteredPackageName);
			if (!newFilteredPackageName.startsWith(this.filteredPackageName)) {
				return EmptyClassQuery.INSTANCE;
			}
			return new OfClassLoader(loader, newFilteredPackageName);
		}

		@Override
		public Stream<Class<?>> stream() {
			return CLASS_PATH.entries(loader).stream()
				.filter(OfClassLoader::isClass)
				.map(OfClassLoader::getClassName)
				.filter(this::isInFilteredPackage)
				.map(this::load)
				.flatMap(Streams::presentInstances);
		}

		private boolean isInFilteredPackage(String className) {
			return className.startsWith(filteredPackageName);
		}

		private static boolean isClass(String path) {
			return path.endsWith(CLASS_FILE_SUFFIX)
				&& !path.endsWith("package-info.class")
				&& !path.endsWith("module-info.class");
		}

		private Optional<Class<?>> load(String className) {
			try {
				Class<?> loadedClass = loader.loadClass(className);
				return Optional.of(loadedClass);
			}
			catch (ClassNotFoundException | NoClassDefFoundError exception) {
				return Optional.empty();
			}
		}

		private static String getClassName(String path) {
			int classNameEnd = path.length() - CLASS_FILE_SUFFIX.length();
			return path.substring(0, classNameEnd).replace('/', '.');
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
	}

	private static class Subtyped<X extends C, C> extends ClassQuery<X> {
		// generic types changes, cannot inherit from Filtered

		private final ClassQuery<? extends C> parent;
		private final Class<? extends X> subtype;

		Subtyped(ClassQuery<? extends C> parent, Class<? extends X> subtype) {
			this.parent = parent;
			this.subtype = subtype;
		}

		@Override
		public Stream<Class<? extends X>> stream() {
			return parent.stream()
				.filter(subtype::isAssignableFrom)
				.map(current -> current.asSubclass(subtype));
		}

		@Override
		public ClassQuery<X> inPackage(String filteredPackageName) {
			return new Subtyped<>(parent.inPackage(filteredPackageName), subtype);
		}
	}

	private static final class EmptyClassQuery extends ClassQuery<Object> {
		static final EmptyClassQuery INSTANCE = new EmptyClassQuery();

		private EmptyClassQuery() {
			// singleton
		}

		@Override
		public Stream<Class<?>> stream() {
			return Stream.of();
		}

		@Override
		public EmptyClassQuery inPackage(String filteredPackageName) {
			return INSTANCE;
		}

		@Override
		public EmptyClassQuery filter(Predicate<? super Class<?>> filter) {
			return INSTANCE;
		}
	}

	private static class ClassPath {
		private final Map<ClassLoader, ImmutableSet<String>> entriesCache = new ConcurrentHashMap<>();

		ImmutableSet<String> entries(ClassLoader loader) {
			return entriesCache.computeIfAbsent(loader, ClassPath::generateHierarchyEntries);
		}

		private static ImmutableSet<String> generateHierarchyEntries(ClassLoader classLoader) {
			Set<File> visited = new HashSet<>();
			ClassLoader currentClassLoader = classLoader;
			ImmutableSet.Builder<String> resultBuilder = ImmutableSet.builder();
			while (currentClassLoader != null) {
				generateClassloaderEntries(currentClassLoader, resultBuilder, visited);
				currentClassLoader = currentClassLoader.getParent();
			}
			return resultBuilder.build();
		}

		private static void generateClassloaderEntries(ClassLoader classLoader,
												ImmutableSet.Builder<String> resultBuilder,
												Set<File> visited) {
			if (!(classLoader instanceof URLClassLoader)) {
				return;
			}
			URLClassLoader urlClassLoader = (URLClassLoader) classLoader;
			for (URL url : urlClassLoader.getURLs()) {
				generateUrlEntry(url, resultBuilder, visited);
			}
		}

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

		private static URL safeToUrl(String entry) {
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

}
