package org.molgenis.data;

import static com.google.common.collect.Streams.stream;
import static java.lang.String.format;
import static java.util.Collections.emptyList;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.util.ApplicationContextProvider;
import org.molgenis.util.ListEscapeUtils;
import org.molgenis.util.UnexpectedEnumException;
import org.springframework.core.convert.ConversionException;
import org.springframework.core.convert.ConversionService;

public class DataConverter {
  private static ConversionService conversionService;

  private DataConverter() {}

  /** @throws ConversionException if convertion failed or no converter was found */
  @SuppressWarnings("unchecked")
  @Nullable
  @CheckForNull
  private static <T> T convert(@Nullable @CheckForNull Object source, Class<T> targetType) {
    if (source == null) {
      return null;
    }

    if (targetType.isAssignableFrom(source.getClass())) {
      return (T) source;
    }

    try {
      return getConversionService().convert(source, targetType);
    } catch (ConversionException e) {
      throw new DataConversionException(e);
    }
  }

  /**
   * Convert value to the type based on the given attribute.
   *
   * @param source value to convert
   * @param attr attribute that defines the type of the converted value
   * @return converted value or the input value if the attribute type is a reference type
   * @throws AttributeValueConversionException if conversion failed
   */
  public static Object convert(Object source, Attribute attr) {
    try {
      return convert(source, attr.getDataType());
    } catch (DataConversionException e) {
      throw new AttributeValueConversionException(
          format(
              "Conversion failure in entity type [%s] attribute [%s]; %s",
              attr.getEntity().getId(), attr.getName(), e.getMessage()),
          e);
    }
  }

  private static Object convert(Object source, AttributeType attributeType) {
    Object value;
    switch (attributeType) {
      case BOOL:
        value = toBoolean(source);
        break;
      case XREF:
      case CATEGORICAL:
      case CATEGORICAL_MREF:
      case MREF:
      case FILE:
      case ONE_TO_MANY:
        value = source;
        break;
      case COMPOUND:
        throw new UnsupportedOperationException();
      case DATE:
        value = toLocalDate(source);
        break;
      case DATE_TIME:
        value = toInstant(source);
        break;
      case DECIMAL:
        value = toDouble(source);
        break;
      case INT:
        value = toInt(source);
        break;
      case LONG:
        value = toLong(source);
        break;
      case EMAIL:
      case ENUM:
      case HTML:
      case HYPERLINK:
      case SCRIPT:
      case STRING:
      case TEXT:
        value = toString(source);
        break;
      default:
        throw new UnexpectedEnumException(attributeType);
    }
    return value;
  }

  /** @throws DataConversionException if conversion failed */
  @Nullable
  @CheckForNull
  public static String toString(@Nullable @CheckForNull Object source) {
    if (source == null) {
      return null;
    }
    if (source instanceof String) {
      return (String) source;
    }
    if (source instanceof Iterable<?>) {
      StringBuilder sb = new StringBuilder();
      for (Object obj : (Iterable<?>) source) {
        if (sb.length() > 0) sb.append(",");
        sb.append(toString(obj));
      }

      return sb.toString();
    }
    return convert(source, String.class);
  }

  /** @throws DataConversionException if conversion failed */
  @Nullable
  @CheckForNull
  public static Integer toInt(@Nullable @CheckForNull Object source) {
    if (source == null) {
      return null;
    }
    if (source instanceof Integer) {
      return (Integer) source;
    }
    return convert(source, Integer.class);
  }

  /** @throws DataConversionException if conversion failed */
  @Nullable
  @CheckForNull
  public static Long toLong(@Nullable @CheckForNull Object source) {
    if (source == null) {
      return null;
    }
    if (source instanceof Long) {
      return (Long) source;
    }
    return convert(source, Long.class);
  }

  /**
   * @return true, false or null
   * @throws DataConversionException if conversion failed
   */
  @SuppressWarnings("squid:S2447") // null is a valid return value
  @Nullable
  @CheckForNull
  public static Boolean toBoolean(@Nullable @CheckForNull Object source) {
    if (source == null) {
      return null;
    }
    if (source instanceof Boolean) {
      return (Boolean) source;
    }
    return convert(source, Boolean.class);
  }

  /** @throws DataConversionException if conversion failed */
  @Nullable
  @CheckForNull
  public static Double toDouble(@Nullable @CheckForNull Object source) {
    if (source == null) {
      return null;
    }
    if (source instanceof Double) {
      return (Double) source;
    }
    return convert(source, Double.class);
  }

  /** @throws DataConversionException if conversion failed */
  @Nullable
  @CheckForNull
  public static LocalDate toLocalDate(@Nullable @CheckForNull Object source) {
    if (source == null) {
      return null;
    }
    if (source instanceof LocalDate) {
      return (LocalDate) source;
    }
    return convert(source, LocalDate.class);
  }

  /** @throws DataConversionException if conversion failed */
  @Nullable
  @CheckForNull
  public static Instant toInstant(@Nullable @CheckForNull Object source) {
    if (source == null) {
      return null;
    }
    if (source instanceof Instant) {
      return (Instant) source;
    }
    return convert(source, Instant.class);
  }

  /** @throws DataConversionException if conversion failed */
  public static List<String> toList(@Nullable @CheckForNull Object source) {
    List<String> stringList;
    if (source == null) {
      stringList = emptyList();
    } else if (source instanceof Iterable<?>) {
      Iterable<?> iterable = (Iterable<?>) source;
      stringList = stream(iterable).map(DataConverter::toString).collect(Collectors.toList());
    } else if (source instanceof String) {
      String string = (String) source;
      stringList = ListEscapeUtils.toList(string);
    } else {
      stringList = ListEscapeUtils.toList(source.toString());
    }
    return stringList;
  }

  /** testability */
  public static void setConversionService(ConversionService conversionService) {
    DataConverter.conversionService = conversionService;
  }

  private static ConversionService getConversionService() {
    if (conversionService == null) {
      conversionService =
          ApplicationContextProvider.getApplicationContext().getBean(ConversionService.class);
    }
    return conversionService;
  }
}
