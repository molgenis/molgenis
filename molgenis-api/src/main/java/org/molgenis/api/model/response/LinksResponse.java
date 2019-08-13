package org.molgenis.api.model.response;

import com.google.auto.value.AutoValue;
import java.net.URI;
import javax.annotation.Nullable;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_LinksResponse.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class LinksResponse {
  @Nullable
  public abstract URI getPrevious();

  public abstract URI getSelf();

  @Nullable
  public abstract URI getNext();

  public static LinksResponse create(URI previous, URI self, URI next) {
    return new AutoValue_LinksResponse(previous, self, next);
  }
}
