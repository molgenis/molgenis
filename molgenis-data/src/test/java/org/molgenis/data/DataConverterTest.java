package org.molgenis.data;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.DATE;
import static org.molgenis.data.meta.AttributeType.DATE_TIME;
import static org.molgenis.data.meta.AttributeType.ONE_TO_MANY;
import static org.molgenis.data.meta.AttributeType.XREF;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Iterator;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DataConverterTest {
  @Test
  public void testToIntNull() {
    assertNull(DataConverter.toInt(null));
  }

  @Test
  public void testToIntInteger() {
    assertEquals(DataConverter.toInt(123), Integer.valueOf(123));
  }

  @Test
  public void testToIntString() {
    assertEquals(DataConverter.toInt("123"), Integer.valueOf(123));
  }

  @Test
  public void testToLongNull() {
    assertNull(DataConverter.toLong(null));
  }

  @Test
  public void testToLongLong() {
    assertEquals(DataConverter.toLong(123L), Long.valueOf(123L));
  }

  @Test
  public void testToLongString() {
    assertEquals(DataConverter.toLong("123"), Long.valueOf(123L));
  }

  @Test
  public void testToBooleanNull() {
    assertNull(DataConverter.toBoolean(null));
  }

  @Test
  public void testToBooleanBoolean() {
    assertEquals(DataConverter.toBoolean(Boolean.TRUE), Boolean.TRUE);
  }

  @Test
  public void testToBooleanString() {
    assertEquals(DataConverter.toBoolean("true"), Boolean.TRUE);
  }

  @Test
  public void testToDoubleNull() {
    assertNull(DataConverter.toDouble(null));
  }

  @SuppressWarnings("ConstantConditions")
  @Test
  public void testToDoubleBoolean() {
    assertEquals(DataConverter.toDouble(1.23), 1.23, 0.01);
  }

  @SuppressWarnings("ConstantConditions")
  @Test
  public void testToDoubleString() {
    assertEquals(DataConverter.toDouble("1.23"), 1.23, 0.01);
  }

  @Test
  public void testToLocalDateNull() {
    assertNull(DataConverter.toLocalDate(null));
  }

  @Test
  public void testToLocalDateLocalDate() {
    LocalDate localDate = LocalDate.now();
    assertEquals(DataConverter.toLocalDate(localDate), localDate);
  }

  @Test
  public void testToLocalDateString() {
    assertEquals(DataConverter.toLocalDate("2015-06-04"), LocalDate.parse("2015-06-04"));
  }

  @Test
  public void testToInstantNull() {
    assertNull(DataConverter.toInstant(null));
  }

  @Test
  public void testToInstantInstant() {
    Instant instant = Instant.now();
    assertEquals(DataConverter.toInstant(instant), instant);
  }

  @Test
  public void testToInstantString() {
    assertEquals(
        DataConverter.toInstant("1986-08-12T06:12:13Z"), Instant.parse("1986-08-12T06:12:13Z"));
  }

  @Test
  public void testToStringNull() {
    assertNull(DataConverter.toString(null));
  }

  @Test
  public void testToStringString() {
    assertEquals(DataConverter.toString("abc"), "abc");
  }

  @Test
  public void testToStringIterable() {
    assertEquals(DataConverter.toString(asList("a", "b", "c")), "a,b,c");
  }

  @Test
  public void testToStringInt() {
    assertEquals(DataConverter.toString(123), "123");
  }

  @Test
  public void convertLocalDate() {
    Attribute attr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
    when(attr.getDataType()).thenReturn(DATE);
    assertEquals(DataConverter.convert("2015-06-04", attr), LocalDate.parse("2015-06-04"));
  }

  @DataProvider(name = "convertObjectAttributeProvider")
  public static Iterator<Object[]> convertObjectAttributeProvider() {
    Object object = mock(Object.class);
    return newArrayList(
            new Object[] {object, ONE_TO_MANY, object}, new Object[] {object, XREF, object})
        .iterator();
  }

  @Test(dataProvider = "convertObjectAttributeProvider")
  public void convertObjectAttribute(Object source, AttributeType attrType, Object convertedValue) {
    Attribute attr = mock(Attribute.class);
    when(attr.getDataType()).thenReturn(attrType);
    assertEquals(DataConverter.convert(source, attr), convertedValue);
  }

  @Test
  public void toLocalDate() {
    assertEquals(DataConverter.toLocalDate("2015-06-04"), LocalDate.parse("2015-06-04"));
  }

  @Test
  public void convertDateTime() {
    Attribute attr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
    when(attr.getDataType()).thenReturn(DATE_TIME);
    assertEquals(
        DataConverter.convert("2015-05-22T11:12:13+0500", attr),
        Instant.parse("2015-05-22T06:12:13Z"));
  }

  @Test
  public void testToListNull() {
    assertEquals(DataConverter.toList(null), emptyList());
  }

  @Test
  public void testToListIterableString() {
    String value0 = "0";
    String value1 = "1";
    assertEquals(DataConverter.toList(asList(value0, value1)), asList(value0, value1));
  }

  @Test
  public void testToListString() {
    String value = "a,b,c";
    assertEquals(DataConverter.toList(value), asList("a", "b", "c"));
  }

  @Test
  public void testToListOther() {
    assertEquals(DataConverter.toList(0L), singletonList("0"));
  }

  @Test(
      expectedExceptions = AttributeValueConversionException.class,
      expectedExceptionsMessageRegExp =
          "Conversion failure in entity type \\[test\\] attribute \\[id\\]; .*")
  public void testWrapExceptionOnInvalidConversion() {
    EntityType entityType = mock(EntityType.class);
    when(entityType.getId()).thenReturn("test");
    Attribute attr = mock(Attribute.class);
    when(attr.getDataType()).thenReturn(AttributeType.INT);
    when(attr.getName()).thenReturn("id");
    when(attr.getEntity()).thenReturn(entityType);

    DataConverter.convert("test", attr);
  }

  // regression test for https://github.com/molgenis/molgenis/issues/7752
  @Test
  public void toStringIterable() {
    Iterable<String> iterable = () -> asList("str1", "str2").iterator();
    assertEquals(DataConverter.toString(iterable), "str1,str2");
  }
}
