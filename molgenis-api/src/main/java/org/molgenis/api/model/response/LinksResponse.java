package org.molgenis.api.model.response;

import com.google.auto.value.AutoValue;
import java.net.URI;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_LinksResponse.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class LinksResponse {
  @Nullable
  @CheckForNull
  public abstract URI getPrevious();

  public abstract URI getSelf();

  @Nullable
  @CheckForNull
  public abstract URI getNext();

  public static LinksResponse create(URI newPrevious, URI newSelf, URI newNext) {
    return builder().setPrevious(newPrevious).setSelf(newSelf).setNext(newNext).build();
  }

  public static Builder builder() {
    return new AutoValue_LinksResponse.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setPrevious(URI newPrevious);

    public abstract Builder setSelf(URI newSelf);

    public abstract Builder setNext(URI newNext);

    public abstract LinksResponse build();
  }
}
