package org.molgenis.api.metadata.v3.job.util;

import com.google.common.collect.ImmutableMap;
import java.util.Map;

/**
 * TODO use dependency or move to molgenis-utils
 *
 * <p>copied from
 * https://github.com/acebaggins/gson-serializers/blob/master/src/main/java/com/tyler/gson/immutable/
 */
public class ImmutableMapDeserializer extends BaseMapDeserializer<ImmutableMap<?, ?>> {

  @Override
  protected ImmutableMap<?, ?> buildFrom(final Map<?, ?> map) {
    return ImmutableMap.copyOf(map);
  }
}
