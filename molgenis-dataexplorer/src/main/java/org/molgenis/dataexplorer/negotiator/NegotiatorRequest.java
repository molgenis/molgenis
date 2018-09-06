package org.molgenis.dataexplorer.negotiator;

import com.google.auto.value.AutoValue;
import javax.annotation.Nullable;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_NegotiatorRequest.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class NegotiatorRequest {
  public abstract String getURL();

  public abstract String getEntityId();

  public abstract String getRsql();

  public abstract String getHumanReadable();

  @Nullable
  public abstract String getnToken();

  public static NegotiatorRequest create(
      String url, String entityId, String rsql, String humanReadable, String nToken) {
    return new AutoValue_NegotiatorRequest(url, entityId, rsql, humanReadable, nToken);
  }
}
