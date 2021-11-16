package net.sourcewriters.spigot.rwg.legacy.api.data.property;

import java.util.function.Function;

@SuppressWarnings("unchecked")
class Property<E> implements IProperty<E> {

    private final String key;
    private final E value;

    public Property(final String key, final E value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public E getValue() {
        return value;
    }

    @Override
    public E getValueOr(final E fallback) {
        return value == null ? fallback : value;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public Class<?> getOwner() {
        return value.getClass();
    }

    @Override
    public boolean isInstance(final Class<?> sample) {
        return sample.isInstance(value);
    }

    @Override
    public <V> IProperty<V> cast(final Class<V> sample) {
        if (isInstance(sample)) {
            return (IProperty<V>) this;
        }
        return new VoidProperty(key);
    }

    @Override
    public <V> IProperty<V> map(final Function<E, V> mapper) {
        return IProperty.of(key, mapper.apply(value));
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

}
