package org.perfectable.introspection;

import java.lang.reflect.Array;
import java.util.Collection;

import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableList;

final class MethodSignature {
	static final CharMatcher IDENTIFIER_BREAKING = CharMatcher.anyOf(";.[:><").or(CharMatcher.whitespace());

	private final ImmutableList<TypeParameter> formalTypeParameters;
	private final String name;
	private final ImmutableList<FieldType> formalParameters;
	private final ReturnType returnType;
	private final ImmutableList<FieldType> thrownTypes;

	static MethodSignature read(String signatureString) {
		CharacterReader reader = new CharacterReader(signatureString);
		ImmutableList<TypeParameter> typeParameters =
			TypeParameter.readFormalTypeParametersFrom(reader);
		String name = parseName(reader);
		ImmutableList<FieldType> formalParameters = parseFormalParameters(reader);
		ReturnType returnType = ReturnType.readReturnTypeFrom(reader);
		ImmutableList<FieldType> thrownTypes = parseThrownTypes(reader);
		return new MethodSignature(typeParameters, name, formalParameters, returnType, thrownTypes);
	}

	private MethodSignature(ImmutableList<TypeParameter> typeParameters,
							String name,
							ImmutableList<FieldType> formalParameters,
							ReturnType returnType,
							ImmutableList<FieldType> thrownTypes) {
		this.formalTypeParameters = typeParameters;
		this.name = name;
		this.formalParameters = formalParameters;
		this.returnType = returnType;
		this.thrownTypes = thrownTypes;
	}

	public Class<?>[] runtimeParameterTypes(ClassLoaderIntrospection loader) {
		return formalParameters.stream()
			.map(typeSignature -> typeSignature.asRuntimeClass(loader, formalTypeParameters))
			.toArray(Class<?>[]::new);
	}

	public String name() {
		return name;
	}

	public Class<?> runtimeResultType(ClassLoaderIntrospection loader) {
		return returnType.asRuntimeClass(loader, formalTypeParameters);
	}

	public Class<?>[] runtimeDeclaredExceptionTypes(ClassLoaderIntrospection loader) {
		return thrownTypes.stream()
			.map(fieldType -> fieldType.asRuntimeClass(loader, formalTypeParameters))
			.toArray(Class<?>[]::new);
	}

	private static String parseName(CharacterReader reader) {
		return reader.readUntil(CharMatcher.is('('));
	}

	private static ImmutableList<FieldType> parseFormalParameters(CharacterReader reader) {
		reader.advanceAssuming('(');
		ImmutableList.Builder<FieldType> typeSignatures = ImmutableList.builder();
		while (!reader.currentIs(')')) {
			FieldType typeSignature = FieldType.readFieldTypeFrom(reader);
			typeSignatures.add(typeSignature);
		}
		ImmutableList<FieldType> signatures = typeSignatures.build();
		reader.advanceAssuming(')');
		return signatures;
	}

	private static ImmutableList<FieldType> parseThrownTypes(CharacterReader reader) {
		ImmutableList.Builder<FieldType> resultBuilder = ImmutableList.builder();
		while (reader.currentIsThenSkip('^')) {
			FieldType throwsSignature = FieldType.readFieldTypeFrom(reader);
			if (throwsSignature instanceof ArrayType) {
				throw new IllegalArgumentException("Throws cannot be array");
			}
			resultBuilder.add(throwsSignature);
		}
		return resultBuilder.build();
	}

	private static final class CharacterReader {
		private final char[] input;
		private int position;

		CharacterReader(String input) {
			this.input = input.toCharArray();
		}

		void advanceAssuming(char expected) {
			if (!currentIs(expected)) {
				throw new IllegalArgumentException("Expected " + expected);
			}
			position++;
		}

		String readUntil(CharMatcher breaking) {
			StringBuilder result = new StringBuilder();
			while (position < input.length) {
				char current = input[position];
				if (breaking.matches(current)) {
					break;
				}
				result.append(current);
				position++;
			}
			return result.toString();
		}

		private boolean currentIs(char expected) {
			return currentIn(CharMatcher.is(expected));
		}

		boolean currentIn(CharMatcher characters) {
			if (position >= input.length) {
				return false;
			}
			char current = input[position];
			return characters.matches(current);
		}

		boolean currentIsThenSkip(char expected) {
			if (!currentIs(expected)) {
				return false;
			}
			position++;
			return true;
		}
	}

	@SuppressWarnings("InterfaceWithOnlyStatics")
	private interface TypeArgument {
		static TypeArgument readTypeArgumentFrom(CharacterReader reader) {
			if (reader.currentIsThenSkip('*')) {
				return Wildcard.createWild();
			}
			if (reader.currentIsThenSkip('+')) {
				FieldType upper = FieldType.readFieldTypeFrom(reader);
				return Wildcard.createWithUpperBound(upper);
			}
			if (reader.currentIsThenSkip('-')) {
				FieldType lower = FieldType.readFieldTypeFrom(reader);
				return Wildcard.createWithLowerBound(lower);
			}
			return FieldType.readFieldTypeFrom(reader);
		}

		static ImmutableList<TypeArgument> readTypeArgumentsFrom(CharacterReader reader) {
			reader.advanceAssuming('<');
			ImmutableList.Builder<TypeArgument> typeArgumentBuilder = ImmutableList.builder();
			while (!reader.currentIs('>')) {
				TypeArgument typeArgument = readTypeArgumentFrom(reader);
				typeArgumentBuilder.add(typeArgument);
			}
			reader.advanceAssuming('>');
			return typeArgumentBuilder.build();
		}
	}

	private abstract static class Wildcard implements TypeArgument {
		Wildcard() {
			// no fields yet
		}

		static Wildcard createWild() {
			return new Wildcard() {
			};
		}

		static Wildcard createWithUpperBound(FieldType upper) {
			return new Wildcard() {
			};
		}

		static Wildcard createWithLowerBound(FieldType lower) {
			return new Wildcard() {
			};
		}
	}

	private interface ReturnType {
		ReturnType VOID = new ReturnType() {
			@Override
			public Class<?> asRuntimeClass(ClassLoaderIntrospection loader, Collection<TypeParameter> formals) {
				return void.class;
			}
		};

		Class<?> asRuntimeClass(ClassLoaderIntrospection loader, Collection<TypeParameter> formals);

		static ReturnType readReturnTypeFrom(CharacterReader reader) {
			if (reader.currentIsThenSkip('V')) {
				return ReturnType.VOID;
			}
			return FieldType.readFieldTypeFrom(reader);
		}
	}

	private interface FieldType extends ReturnType, TypeArgument {
		static FieldType readFieldTypeFrom(CharacterReader reader) {
			if (reader.currentIs('L')) {
				return ObjectType.readSimpleClassTypeSignature(reader);
			}
			if (reader.currentIs('T')) {
				return TypeVariable.readTypeVariableSignature(reader);
			}
			if (reader.currentIs('[')) {
				return ArrayType.readArrayTypeSignatureFrom(reader);
			}
			if (reader.currentIn(PrimitiveType.CHARACTERS)) {
				return PrimitiveType.readPrimitiveSignature(reader);
			}
			throw new IllegalArgumentException("Expected Field Type Signature");
		}
	}

	private static final class ArrayType implements FieldType {
		private final FieldType nested;

		private ArrayType(FieldType nested) {
			this.nested = nested;
		}

		@Override
		public Class<?> asRuntimeClass(ClassLoaderIntrospection loader, Collection<TypeParameter> formals) {
			Class<?> componentType = nested.asRuntimeClass(loader, formals);
			return Array.newInstance(componentType, 0).getClass();
		}

		static ArrayType readArrayTypeSignatureFrom(CharacterReader reader) {
			reader.advanceAssuming('[');
			FieldType nested = FieldType.readFieldTypeFrom(reader);
			return new ArrayType(nested);
		}
	}

	private static final class TypeParameter implements TypeArgument {
		private final String identifier;
		private final ImmutableList<FieldType> bounds;

		private TypeParameter(String identifier, ImmutableList<FieldType> bounds) {
			this.identifier = identifier;
			this.bounds = bounds;
		}

		static ImmutableList<TypeParameter> readFormalTypeParametersFrom(CharacterReader reader) {
			if (!reader.currentIsThenSkip('<')) {
				return ImmutableList.of();
			}
			ImmutableList.Builder<TypeParameter> resultBuilder = ImmutableList.builder();
			while (!reader.currentIs('>')) {
				TypeParameter typeParameter = TypeParameter.readFormalTypeParameterFrom(reader);
				resultBuilder.add(typeParameter);
			}
			reader.advanceAssuming('>');
			return resultBuilder.build();
		}

		static TypeParameter readFormalTypeParameterFrom(CharacterReader reader) {
			final String identifier = reader.readUntil(IDENTIFIER_BREAKING);
			ImmutableList.Builder<FieldType> boundsBuilder = ImmutableList.builder();
			reader.advanceAssuming(':');
			if (!reader.currentIs(':')) {
				FieldType bound = FieldType.readFieldTypeFrom(reader);
				boundsBuilder.add(bound);
			}
			while (reader.currentIsThenSkip(':')) {
				FieldType bound = FieldType.readFieldTypeFrom(reader);
				boundsBuilder.add(bound);
			}
			ImmutableList<FieldType> bounds = boundsBuilder.build();
			return new TypeParameter(identifier, bounds);
		}

		FieldType firstBound() {
			if (bounds.isEmpty()) {
				return ObjectType.OBJECT;
			}
			return bounds.get(0);
		}

		boolean hasIdentifier(String candidate) {
			return this.identifier.equals(candidate);
		}
	}

	private static final class ObjectType implements FieldType {
		static final ObjectType OBJECT =
			ObjectType.createWithoutTypeArguments("java.lang.Object");

		private final String className;
		@SuppressWarnings("unused")
		private final ImmutableList<TypeArgument> typeArguments;

		private ObjectType(String className, ImmutableList<TypeArgument> typeArguments) {
			this.className = className;
			this.typeArguments = typeArguments;
		}

		static ObjectType readSimpleClassTypeSignature(CharacterReader reader) {
			reader.advanceAssuming('L');
			String identifier = reader.readUntil(IDENTIFIER_BREAKING);
			String qualified = identifier.replaceAll("/", ".");
			if (reader.currentIsThenSkip(';')) {
				return ObjectType.createWithoutTypeArguments(qualified);
			}
			if (reader.currentIs('<')) {
				ImmutableList<TypeArgument> typeArguments = TypeArgument.readTypeArgumentsFrom(reader);
				reader.advanceAssuming(';');
				return ObjectType.create(qualified, typeArguments);
			}
			throw new IllegalArgumentException("expected '<' or ';' or '.'");
		}


		static ObjectType create(String identifier, ImmutableList<TypeArgument> typeArguments) {
			return new ObjectType(identifier, typeArguments);
		}

		static ObjectType createWithoutTypeArguments(String identifier) {
			return new ObjectType(identifier, ImmutableList.of());
		}

		@Override
		public Class<?> asRuntimeClass(ClassLoaderIntrospection loader, Collection<TypeParameter> formals) {
			return loader.loadSafe(className);
		}
	}

	private static final class TypeVariable implements FieldType {
		private final String identifier;

		private TypeVariable(String identifier) {
			this.identifier = identifier;
		}

		private static TypeVariable readTypeVariableSignature(CharacterReader reader) {
			reader.advanceAssuming('T');
			String identifier = reader.readUntil(IDENTIFIER_BREAKING);
			TypeVariable typeVariableSignature = new TypeVariable(identifier);
			reader.advanceAssuming(';');
			return typeVariableSignature;
		}

		@Override
		public Class<?> asRuntimeClass(ClassLoaderIntrospection loader, Collection<TypeParameter> formals) {
			TypeParameter typeParameter = formals.stream()
				.filter(formal -> formal.hasIdentifier(identifier))
				.findAny()
				.orElseThrow(() -> new IllegalArgumentException("Cannot resolve parameter " + identifier));
			return typeParameter.firstBound().asRuntimeClass(loader, formals);
		}
	}

	private static final class PrimitiveType implements FieldType {
		static final CharMatcher CHARACTERS = CharMatcher.anyOf("BCDFIJSZ");

		@SuppressWarnings("NPathComplexity")
		static PrimitiveType readPrimitiveSignature(CharacterReader reader) {
			if (reader.currentIsThenSkip('B')) {
				return PrimitiveType.BYTE;
			}
			if (reader.currentIsThenSkip('C')) {
				return PrimitiveType.CHAR;
			}
			if (reader.currentIsThenSkip('D')) {
				return PrimitiveType.DOUBLE;
			}
			if (reader.currentIsThenSkip('F')) {
				return PrimitiveType.FLOAT;
			}
			if (reader.currentIsThenSkip('I')) {
				return PrimitiveType.INT;
			}
			if (reader.currentIsThenSkip('J')) {
				return PrimitiveType.LONG;
			}
			if (reader.currentIsThenSkip('S')) {
				return PrimitiveType.SHORT;
			}
			if (reader.currentIsThenSkip('Z')) {
				return PrimitiveType.BOOLEAN;
			}
			throw new IllegalArgumentException("expected primitive type");
		}

		static final PrimitiveType BYTE = new PrimitiveType(byte.class);
		static final PrimitiveType CHAR = new PrimitiveType(char.class);
		static final PrimitiveType DOUBLE = new PrimitiveType(double.class);
		static final PrimitiveType FLOAT = new PrimitiveType(float.class);
		static final PrimitiveType INT = new PrimitiveType(int.class);
		static final PrimitiveType LONG = new PrimitiveType(long.class);
		static final PrimitiveType SHORT = new PrimitiveType(short.class);
		static final PrimitiveType BOOLEAN = new PrimitiveType(boolean.class);

		private final Class<?> type;

		private PrimitiveType(Class<?> type) {
			this.type = type;
		}

		@Override
		public Class<?> asRuntimeClass(ClassLoaderIntrospection loader, Collection<TypeParameter> formals) {
			return type;
		}
	}
}
