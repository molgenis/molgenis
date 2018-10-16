package org.molgenis.data.cache.utils;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import javax.annotation.Nullable;

public class CacheHit<T> {

  private static final CacheHit<?> EMPTY = new CacheHit<>(null);

  @Nullable private T value;

  private CacheHit(@Nullable T value) {
    this.value = value;
  }

  public static <T> CacheHit<T> empty() {
    @SuppressWarnings("unchecked")
    CacheHit<T> hit = (CacheHit<T>) EMPTY;
    return hit;
  }

  public static <T> CacheHit<T> of(T value) {
    @SuppressWarnings("unchecked")
    CacheHit<T> hit = new CacheHit(requireNonNull(value));
    return hit;
  }

  @Nullable
  public T getValue() {
    return value;
  }

  public boolean isEmpty() {
    return value == null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CacheHit)) {
      return false;
    }
    CacheHit<?> cacheHit = (CacheHit<?>) o;
    return Objects.equals(getValue(), cacheHit.getValue());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getValue());
  }

  @Override
  public String toString() {
    return "CacheHit{" + "value=" + value + '}';
  }
}
