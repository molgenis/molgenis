package org.molgenis.api.metadata.v3.job.util;

import com.google.common.collect.ImmutableList;
import java.util.Collection;

/**
 * TODO use dependency or move to molgenis-utils
 *
 * <p>copied from
 * https://github.com/acebaggins/gson-serializers/blob/master/src/main/java/com/tyler/gson/immutable/
 */
public class ImmutableListDeserializer extends BaseCollectionDeserializer<ImmutableList<?>> {

  @Override
  protected ImmutableList<?> buildFrom(Collection<?> collection) {
    return ImmutableList.copyOf(collection);
  }
}
