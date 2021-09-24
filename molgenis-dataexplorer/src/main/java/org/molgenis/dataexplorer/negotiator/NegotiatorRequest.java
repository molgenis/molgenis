package org.molgenis.dataexplorer.negotiator;

import com.google.auto.value.AutoValue;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_NegotiatorRequest.class)
@SuppressWarnings("java:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class NegotiatorRequest {
  public abstract String getURL();

  /** Entity type ID for the collections */
  public abstract String getEntityId();

  /** Entity type ID for the biobanks */
  @Nullable
  @CheckForNull
  public abstract String getBiobankId();

  /** RSQL for the collection entity */
  @Nullable
  @CheckForNull
  public abstract String getRsql();

  /** RSQL for the biobank entity */
  @Nullable
  @CheckForNull
  public abstract String getBiobankRsql();

  public abstract String getHumanReadable();

  @Nullable
  @CheckForNull
  public abstract String getnToken();

  public static NegotiatorRequest create(
      String url, String entityId, String rsql, String humanReadable, String nToken) {
    return new AutoValue_NegotiatorRequest(url, entityId, null, rsql, null, humanReadable, nToken);
  }

  public static NegotiatorRequest create(
      String url,
      String entityId,
      String biobankId,
      String rsql,
      String biobankRsql,
      String humanReadable,
      String nToken) {
    return new AutoValue_NegotiatorRequest(
        url, entityId, biobankId, rsql, biobankRsql, humanReadable, nToken);
  }
}
