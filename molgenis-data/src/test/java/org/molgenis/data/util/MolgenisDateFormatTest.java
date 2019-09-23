package org.molgenis.data.util;

import static java.time.Instant.parse;
import static java.time.ZoneId.systemDefault;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.molgenis.data.util.MolgenisDateFormat.parseInstant;
import static org.molgenis.data.util.MolgenisDateFormat.parseLocalDate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class MolgenisDateFormatTest {
  static Object[][] parseInstantDataProvider() {
    return new Object[][] {
      {"2000-12-31T12:34:56.789+0200", "2000-12-31T10:34:56.789Z"},
      {"2000-12-31T12:34:56.789+02:00", "2000-12-31T10:34:56.789Z"},
      {
        "2000-12-31T12:34:56.789",
        LocalDateTime.parse("2000-12-31T12:34:56.789")
            .atZone(systemDefault())
            .toInstant()
            .toString()
      },
      {
        "2000-12-31T12:34",
        LocalDateTime.parse("2000-12-31T12:34").atZone(systemDefault()).toInstant().toString()
      },
      {"2000-12-31T12:34Z", "2000-12-31T12:34:00Z"},
      {
        "2000-12-31",
        LocalDate.parse("2000-12-31").atStartOfDay(systemDefault()).toInstant().toString()
      },
    };
  }

  @ParameterizedTest
  @MethodSource("parseInstantDataProvider")
  void testParseInstant(String text, String expected) {
    assertEquals(parse(expected), parseInstant(text));
  }

  static Object[][] parseLocalDateDataProvider() {
    return new Object[][] {
      {"2000-12-31T00:34:56.789+0200", "2000-12-31"},
      {"2000-12-31T00:34:56.789+02:00", "2000-12-31"},
      {"2000-12-31T00:00:00+0200", "2000-12-31"},
      {"2000-12-31T23:00:00Z", "2000-12-31"},
      {"2000-12-31T00:34", "2000-12-31"},
      {"2000-12-31Z", "2000-12-31"}
    };
  }

  @ParameterizedTest
  @MethodSource("parseLocalDateDataProvider")
  void testParseLocalDate(String text, String expected) {
    assertEquals(LocalDate.parse(expected), parseLocalDate(text));
  }
}
