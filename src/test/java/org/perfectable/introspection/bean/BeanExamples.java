package org.perfectable.introspection.bean;

public final class BeanExamples {
    private static final String MESSAGE_METHOD_CALLED = "Test method should not be called";

    private BeanExamples() {
        // utility class
    }

    public static final class EmptySubject {
        public Object nonPropertyMethod() {
            throw new AssertionError(MESSAGE_METHOD_CALLED);
        }
    }

    public static final class SingleFieldSubject {
        public static final String PROPERTY_ONE_NAME = "one";
        private static String staticField;
        private String one;

        public Object nonPropertyMethod() {
            throw new AssertionError(MESSAGE_METHOD_CALLED);
        }

        public String getOne() {
            return one;
        }

        public void setOne(String one) {
            this.one = one;
        }
    }

    public static final class SingleGetterSubject {
        public static final String PROPERTY_ONE_NAME = "one";
        public static final String VALUE_ONE = "oneTest";

        public String getOne() {
            return VALUE_ONE;
        }

        public Object nonPropertyMethod() {
            throw new AssertionError(MESSAGE_METHOD_CALLED);
        }
    }

    public static final class SingleSetterSubject {
        public static final String PROPERTY_ONE_NAME = "one";

        public void setOne(String value) {
            throw new AssertionError(MESSAGE_METHOD_CALLED);
        }

        public void setNotASetter() {
            throw new AssertionError(MESSAGE_METHOD_CALLED);
        }

        public Object nonPropertyMethod() {
            throw new AssertionError(MESSAGE_METHOD_CALLED);
        }
    }
}
