package at.hannibal2.skyhanni.deps.moulconfig.observer;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface GetSetter<T>
        extends Supplier<T>,
        Consumer<T> {
    @Override
    public T get();

    public void set(T var1);

    @Override
    default public void accept(T t) {
        this.set(t);
    }
}
