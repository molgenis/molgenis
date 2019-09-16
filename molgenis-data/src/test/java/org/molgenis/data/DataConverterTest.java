package org.molgenis.data;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Boolean.TRUE;
import static java.lang.Integer.valueOf;
import static java.time.LocalDate.parse;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.DataConverter.convert;
import static org.molgenis.data.DataConverter.toBoolean;
import static org.molgenis.data.DataConverter.toInstant;
import static org.molgenis.data.DataConverter.toInt;
import static org.molgenis.data.DataConverter.toList;
import static org.molgenis.data.DataConverter.toLong;
import static org.molgenis.data.meta.AttributeType.DATE;
import static org.molgenis.data.meta.AttributeType.DATE_TIME;
import static org.molgenis.data.meta.AttributeType.ONE_TO_MANY;
import static org.molgenis.data.meta.AttributeType.XREF;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Iterator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.data.convert.StringToDateConverter;
import org.molgenis.data.convert.StringToDateTimeConverter;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.springframework.format.support.DefaultFormattingConversionService;

class DataConverterTest {
  @BeforeAll
  static void setUpBeforeClass() {
    DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();
    conversionService.addConverter(new StringToDateConverter());
    conversionService.addConverter(new StringToDateTimeConverter());
    DataConverter.setConversionService(conversionService);
  }

  @Test
  void testToIntNull() {
    assertNull(DataConverter.toInt(null));
  }

  @Test
  void testToIntInteger() {
    assertEquals(valueOf(123), toInt(123));
  }

  @Test
  void testToIntString() {
    assertEquals(valueOf(123), toInt("123"));
  }

  @Test
  void testToLongNull() {
    assertNull(DataConverter.toLong(null));
  }

  @Test
  void testToLongLong() {
    assertEquals(Long.valueOf(123L), toLong(123L));
  }

  @Test
  void testToLongString() {
    assertEquals(Long.valueOf(123L), toLong("123"));
  }

  @Test
  void testToBooleanNull() {
    assertNull(DataConverter.toBoolean(null));
  }

  @Test
  void testToBooleanBoolean() {
    assertEquals(TRUE, toBoolean(TRUE));
  }

  @Test
  void testToBooleanString() {
    assertEquals(TRUE, toBoolean("true"));
  }

  @Test
  void testToDoubleNull() {
    assertNull(DataConverter.toDouble(null));
  }

  @SuppressWarnings("ConstantConditions")
  @Test
  void testToDoubleBoolean() {
    assertEquals(DataConverter.toDouble(1.23), 1.23, 0.01);
  }

  @SuppressWarnings("ConstantConditions")
  @Test
  void testToDoubleString() {
    assertEquals(DataConverter.toDouble("1.23"), 1.23, 0.01);
  }

  @Test
  void testToLocalDateNull() {
    assertNull(DataConverter.toLocalDate(null));
  }

  @Test
  void testToLocalDateLocalDate() {
    LocalDate localDate = LocalDate.now();
    assertEquals(localDate, DataConverter.toLocalDate(localDate));
  }

  @Test
  void testToLocalDateString() {
    assertEquals(parse("2015-06-04"), DataConverter.toLocalDate("2015-06-04"));
  }

  @Test
  void testToInstantNull() {
    assertNull(DataConverter.toInstant(null));
  }

  @Test
  void testToInstantInstant() {
    Instant instant = Instant.now();
    assertEquals(instant, toInstant(instant));
  }

  @Test
  void testToInstantString() {
    assertEquals(Instant.parse("1986-08-12T06:12:13Z"), toInstant("1986-08-12T06:12:13Z"));
  }

  @Test
  void testToStringNull() {
    assertNull(DataConverter.toString(null));
  }

  @Test
  void testToStringString() {
    assertEquals("abc", DataConverter.toString("abc"));
  }

  @Test
  void testToStringIterable() {
    assertEquals("a,b,c", DataConverter.toString(asList("a", "b", "c")));
  }

  @Test
  void testToStringInt() {
    assertEquals("123", DataConverter.toString(123));
  }

  @Test
  void convertLocalDate() {
    Attribute attr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
    when(attr.getDataType()).thenReturn(DATE);
    assertEquals(parse("2015-06-04"), convert("2015-06-04", attr));
  }

  static Iterator<Object[]> convertObjectAttributeProvider() {
    Object object = mock(Object.class);
    return newArrayList(
            new Object[] {object, ONE_TO_MANY, object}, new Object[] {object, XREF, object})
        .iterator();
  }

  @ParameterizedTest
  @MethodSource("convertObjectAttributeProvider")
  void convertObjectAttribute(Object source, AttributeType attrType, Object convertedValue) {
    Attribute attr = mock(Attribute.class);
    when(attr.getDataType()).thenReturn(attrType);
    assertEquals(convertedValue, convert(source, attr));
  }

  @Test
  void toLocalDate() {
    assertEquals(parse("2015-06-04"), DataConverter.toLocalDate("2015-06-04"));
  }

  @Test
  void convertDateTime() {
    Attribute attr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
    when(attr.getDataType()).thenReturn(DATE_TIME);
    assertEquals(Instant.parse("2015-05-22T06:12:13Z"), convert("2015-05-22T11:12:13+0500", attr));
  }

  @Test
  void testToListNull() {
    assertEquals(emptyList(), toList(null));
  }

  @Test
  void testToListIterableString() {
    String value0 = "0";
    String value1 = "1";
    assertEquals(asList(value0, value1), toList(asList(value0, value1)));
  }

  @Test
  void testToListString() {
    String value = "a,b,c";
    assertEquals(asList("a", "b", "c"), toList(value));
  }

  @Test
  void testToListOther() {
    assertEquals(singletonList("0"), toList(0L));
  }

  @Test
  void testWrapExceptionOnInvalidConversion() {
    EntityType entityType = mock(EntityType.class);
    when(entityType.getId()).thenReturn("test");
    Attribute attr = mock(Attribute.class);
    when(attr.getDataType()).thenReturn(AttributeType.INT);
    when(attr.getName()).thenReturn("id");
    when(attr.getEntity()).thenReturn(entityType);

    Exception exception =
        assertThrows(
            AttributeValueConversionException.class, () -> DataConverter.convert("test", attr));
    assertThat(exception.getMessage())
        .containsPattern("Conversion failure in entity type \\[test\\] attribute \\[id\\]; .*");
  }

  // regression test for https://github.com/molgenis/molgenis/issues/7752
  @Test
  void toStringIterable() {
    Iterable<String> iterable = () -> asList("str1", "str2").iterator();
    assertEquals("str1,str2", DataConverter.toString(iterable));
  }
}
