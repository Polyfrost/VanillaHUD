package at.hannibal2.skyhanni.deps.moulconfig.observer;

public abstract class Property<T>
        implements GetSetter<T> {
    public static <T> Property<T> of(T value) {
        return new Property<T>() {
            private T value;

            @Override
            public T get() {
                return this.value;
            }

            @Override
            public void set(T value) {
                this.value = value;
            }
        };
    }
}
