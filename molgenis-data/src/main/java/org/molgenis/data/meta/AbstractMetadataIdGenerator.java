package org.molgenis.data.meta;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.guava.CaffeinatedGuava;
import com.google.common.cache.LoadingCache;
import com.google.common.hash.Hashing;

public abstract class AbstractMetadataIdGenerator implements MetadataIdGenerator {

  private static final int MAXIMUM_CACHE_SIZE = 10000;
  private static final LoadingCache<String, String> CACHE =
      CaffeinatedGuava.build(
          Caffeine.newBuilder().maximumSize(MAXIMUM_CACHE_SIZE),
          AbstractMetadataIdGenerator::generateHashcodeInternal);

  private static String generateHashcodeInternal(String id) {
    return Hashing.crc32().hashString(id, UTF_8).toString();
  }

  /**
   * Generates a eight character [a-z0-9] system unique identifier.
   *
   * @param id identifier of variable length
   * @return hashcode
   */
  protected String generateHashcode(Object id) {
    return CACHE.getUnchecked(id.toString());
  }
}
