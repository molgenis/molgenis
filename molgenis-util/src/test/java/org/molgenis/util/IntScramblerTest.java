package org.molgenis.util;

import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.DecimalFormat;
import java.util.stream.IntStream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class IntScramblerTest {

  @ParameterizedTest
  @ValueSource(ints = {5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19})
  void scramble(int m) {
    var scrambler = new IntScrambler(m);
    final var M = scrambler.getMaxValue() + 1;
    var values = IntStream.range(0, M).boxed().collect(toSet());
    var scrambled = values.stream().map(scrambler::scramble).collect(toSet());
    assertEquals(values, scrambled);
  }

  public static Object[][] forDecimalFormat() {
    return new Object[][] {
      {"GEN'-'0", 9},
      {"GEN'-'0000", 9999},
      {"GEN'-'0000000", 9999999}
    };
  }

  @ParameterizedTest
  @MethodSource
  void forDecimalFormat(String formatString, int maxmax) {
    var format = new DecimalFormat(formatString);
    var scrambler = IntScrambler.forDecimalFormat(format);
    var maxValue = scrambler.getMaxValue();
    assertTrue(maxValue < maxmax);
    assertTrue(maxValue > maxmax / 2);
  }
}
