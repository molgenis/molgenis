package org.molgenis.data.populate;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.molgenis.data.populate.IdGenerator.Strategy.LONG_SECURE_RANDOM;
import static org.molgenis.data.populate.IdGenerator.Strategy.SECURE_RANDOM;
import static org.molgenis.data.populate.IdGenerator.Strategy.SEQUENTIAL_UUID;
import static org.molgenis.data.populate.IdGenerator.Strategy.SHORT_RANDOM;
import static org.molgenis.data.populate.IdGenerator.Strategy.SHORT_SECURE_RANDOM;

import com.google.common.base.Stopwatch;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class IdGeneratorImplTest {
  private final IdGeneratorImpl idGeneratorImpl = new IdGeneratorImpl();
  private static final Logger LOG = LoggerFactory.getLogger(IdGeneratorImplTest.class);

  static Object[][] generateIdDataProvider() {
    return new Object[][] {
      {SEQUENTIAL_UUID, 1000000, 26},
      {SHORT_RANDOM, 1000, 8},
      {SECURE_RANDOM, 1000, 26},
      {LONG_SECURE_RANDOM, 1000, 32},
      {SHORT_SECURE_RANDOM, 1000, 8}
    };
  }

  @ParameterizedTest
  @MethodSource("generateIdDataProvider")
  void testGenerateIds(IdGenerator.Strategy strategy, int numIds, int expectedLength) {
    // initialize
    idGeneratorImpl.generateId(strategy);

    Stopwatch sw = Stopwatch.createStarted();
    Set<String> ids =
        IntStream.range(0, numIds)
            .mapToObj((x) -> idGeneratorImpl.generateId(strategy))
            .collect(Collectors.toSet());
    LOG.info("Generated {} identifiers using strategy {} in {}", numIds, strategy, sw);
    assertTrue(ids.size() == numIds);
    assertTrue(ids.stream().allMatch(id -> id.length() == expectedLength));
  }
}
