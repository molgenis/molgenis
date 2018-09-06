package org.molgenis.dataexplorer.negotiator;

import com.google.auto.value.AutoValue;
import java.util.List;
import javax.annotation.Nullable;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_NegotiatorQuery.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class NegotiatorQuery {
  public abstract String getURL();

  public abstract List<Collection> getCollections();

  public abstract String getHumanReadable();

  @Nullable
  public abstract String getnToken();

  public static NegotiatorQuery create(
      String url, List<Collection> collections, String humanReadable, String nToken) {
    return new AutoValue_NegotiatorQuery(url, collections, humanReadable, nToken);
  }
}
