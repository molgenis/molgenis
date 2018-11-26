package org.molgenis.data.meta;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static org.mockito.Mockito.mock;

import java.lang.reflect.ParameterizedType;
import java.nio.charset.Charset;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.commons.lang3.ArrayUtils;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.util.UnexpectedEnumException;

public class SystemEntityTestUtils {
  protected static final List<String> LANGUAGES =
      newArrayList("En", "Nl", "De", "Es", "It", "Pt", "Fr", "Xx");

  private static final Class[] LONG_CLASS = {Long.class, long.class};
  private static final Class[] INT_CLASS = {Integer.class, int.class};
  private static final Class[] BOOLEAN_CLASS = {Boolean.class, boolean.class};
  private static final Class[] DOUBLE_CLASS = {Double.class, double.class};

  protected static Class[] getReturnTypes(Attribute attr) {
    Class[] result;
    AttributeType dataType = attr.getDataType();
    switch (dataType) {
      case BOOL:
        result = BOOLEAN_CLASS;
        break;
      case CATEGORICAL:
      case FILE:
      case XREF:
        result = new Class[] {Entity.class};
        break;
      case CATEGORICAL_MREF:
      case MREF:
      case ONE_TO_MANY:
        result = new Class[] {Iterable.class};
        break;
      case COMPOUND:
        throw new IllegalArgumentException(
            format(
                "Unexpected data type [%s] for attribute: [%s]",
                dataType.toString(), attr.getName()));
      case DATE:
        result = new Class[] {LocalDate.class};
        break;
      case DATE_TIME:
        result = new Class[] {Instant.class};
        break;
      case DECIMAL:
        result = DOUBLE_CLASS;
        break;
      case EMAIL:
      case ENUM:
      case HTML:
      case HYPERLINK:
      case SCRIPT:
      case STRING:
      case TEXT:
        result = new Class[] {String.class};
        break;
      case INT:
        result = INT_CLASS;
        break;
      case LONG:
        result = LONG_CLASS;
        break;
      default:
        throw new UnexpectedEnumException(dataType);
    }
    return result;
  }

  protected static Object getTestValue(Class returnType, ParameterizedType parameterizedType) {
    if (ArrayUtils.contains(BOOLEAN_CLASS, returnType)) {
      return new Random().nextBoolean();
    }
    if (Entity.class.isAssignableFrom(returnType)) {
      return getTestEntity(returnType);
    }
    if (returnType.equals(Iterable.class)) {
      Object type = getTypedTestEntity(parameterizedType);
      return newArrayList(type);
    }
    if (returnType.equals(LocalDate.class)) {
      return getRandomDate();
    }
    if (returnType.equals(Instant.class)) {
      return Instant.ofEpochSecond(new Random().nextInt());
    }
    if (ArrayUtils.contains(DOUBLE_CLASS, returnType)) {
      return new Random().nextDouble();
    }
    if (returnType.equals(String.class)) {
      return getRandomString();
    }
    if (ArrayUtils.contains(INT_CLASS, returnType)) {
      return new Random().nextInt();
    }
    if (ArrayUtils.contains(LONG_CLASS, returnType)) {
      return new Random().nextLong();
    }
    throw new RuntimeException("Unknown returntype: " + returnType.getSimpleName());
  }

  private static Object getTestEntity(Class returnType) {
    Entity entity = mock(Entity.class);
    Object testValue = null;
    try {
      testValue = returnType.getDeclaredConstructor(Entity.class).newInstance(entity);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return testValue;
  }

  public static String getRandomString() {
    byte[] array = new byte[7];
    new Random().nextBytes(array);
    return new String(array, Charset.forName("UTF-8"));
  }

  private static LocalDate getRandomDate() {
    long minDay = LocalDate.of(1970, 1, 1).toEpochDay();
    long maxDay = LocalDate.of(2015, 12, 31).toEpochDay();
    long randomDay = ThreadLocalRandom.current().nextLong(minDay, maxDay);
    return LocalDate.ofEpochDay(randomDay);
  }

  private static Object getTypedTestEntity(ParameterizedType parameterizedType) {
    Entity entity = mock(Entity.class);
    Object type;

    try {
      Class clazz = (Class) parameterizedType.getActualTypeArguments()[0];
      type = clazz.getDeclaredConstructor(Entity.class).newInstance(entity);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return type;
  }
}
