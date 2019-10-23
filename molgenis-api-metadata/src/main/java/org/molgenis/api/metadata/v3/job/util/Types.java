package org.molgenis.api.metadata.v3.job.util;

import com.google.common.base.Optional;
import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO use dependency or move to molgenis-utils
 *
 * <p>copied from
 * https://github.com/acebaggins/gson-serializers/blob/master/src/main/java/com/tyler/gson/immutable/
 */
@SuppressWarnings({"unchecked", "serial"})
public class Types {

  public static <K, V> TypeToken<Map<K, V>> mapOf(final Type key, final Type value) {
    TypeParameter<K> newKeyTypeParameter = new TypeParameter<K>() {};
    TypeParameter<V> newValueTypeParameter = new TypeParameter<V>() {};
    return new TypeToken<Map<K, V>>() {}.where(newKeyTypeParameter, Types.<K>typeTokenOf(key))
        .where(newValueTypeParameter, Types.<V>typeTokenOf(value));
  }

  public static <K, V> TypeToken<HashMap<K, V>> hashmapOf(final Type key, final Type value) {
    TypeParameter<K> newKeyTypeParameter = new TypeParameter<K>() {};
    TypeParameter<V> newValueTypeParameter = new TypeParameter<V>() {};
    return new TypeToken<HashMap<K, V>>() {}.where(newKeyTypeParameter, Types.<K>typeTokenOf(key))
        .where(newValueTypeParameter, Types.<V>typeTokenOf(value));
  }

  public static <E> TypeToken<Collection<E>> collectionOf(final Type type) {
    TypeParameter<E> newTypeParameter = new TypeParameter<E>() {};
    return new TypeToken<Collection<E>>() {}.where(newTypeParameter, Types.<E>typeTokenOf(type));
  }

  public static <E> TypeToken<Optional<E>> optionalOf(final Type type) {
    TypeParameter<E> newTypeParameter = new TypeParameter<E>() {};
    return new TypeToken<Optional<E>>() {}.where(newTypeParameter, Types.<E>typeTokenOf(type));
  }

  public static <E> TypeToken<E> parametrizedOf(final Class<E> clazz, final Type type) {
    TypeParameter<E> newTypeParameter = new TypeParameter<E>() {};
    return new TypeToken<E>(clazz) {}.where(newTypeParameter, Types.<E>typeTokenOf(type));
  }

  private static <E> TypeToken<E> typeTokenOf(final Type type) {
    return (TypeToken<E>) TypeToken.of(type);
  }
}
