package org.molgenis.api.metadata.v3.job.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

/**
 * TODO use dependency or move to molgenis-utils
 *
 * <p>copied from
 * https://github.com/acebaggins/gson-serializers/blob/master/src/main/java/com/tyler/gson/immutable/
 */
abstract class BaseCollectionDeserializer<E> implements JsonDeserializer<E> {

  protected abstract E buildFrom(final Collection<?> collection);

  public E deserialize(
      final JsonElement json, final Type type, final JsonDeserializationContext context)
      throws JsonParseException {
    final Type[] typeArguments = ((ParameterizedType) type).getActualTypeArguments();

    final Type parameterizedType = Types.collectionOf(typeArguments[0]).getType();
    final Collection<?> collection = context.deserialize(json, parameterizedType);

    return buildFrom(collection);
  }
}
