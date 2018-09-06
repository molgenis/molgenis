package org.molgenis.data.rest.client.bean;

import com.google.auto.value.AutoValue;
import java.util.Map;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_MetaDataResponse.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class MetaDataResponse {
  public static MetaDataResponse create(
      String href,
      String name,
      String label,
      Map<String, ? extends Attribute> attributes,
      String labelAttribute) {
    return new AutoValue_MetaDataResponse(href, name, label, attributes, labelAttribute);
  }

  public abstract String getHref();

  public abstract String getName();

  public abstract String getLabel();

  public abstract Map<String, ? extends Attribute> getAttributes();

  public abstract String getLabelAttribute();

  @AutoValue
  @AutoGson(autoValueClass = AutoValue_MetaDataResponse_Attribute.class)
  @SuppressWarnings(
      "squid:S1610") // Abstract classes without fields should be converted to interfaces
  public abstract static class Attribute {
    public static Attribute create(String href) {
      return new AutoValue_MetaDataResponse_Attribute(href);
    }

    public abstract String getHref();
  }
}
