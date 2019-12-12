package org.molgenis.api.model.response;

import static com.google.common.base.Preconditions.checkState;

import com.google.auto.value.AutoValue;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_PageResponse.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class PageResponse {
  public abstract int getSize();

  public abstract int getTotalElements();

  public abstract int getTotalPages();

  public abstract int getNumber();

  public static PageResponse create(int newSize, int newTotalElements, int newNumber) {
    return builder()
        .setSize(newSize)
        .setTotalElements(newTotalElements)
        .setTotalPages(getTotalPages(newSize, newTotalElements))
        .setNumber(newNumber)
        .build();
  }

  public static Builder builder() {
    return new AutoValue_PageResponse.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setSize(int newSize);

    public abstract Builder setTotalElements(int newTotalElements);

    public abstract Builder setTotalPages(int newTotalPages);

    public abstract Builder setNumber(int newNumber);

    abstract PageResponse autoBuild();

    public PageResponse build() {
      PageResponse pageResponse = autoBuild();
      checkState(pageResponse.getSize() >= 0, "Negative number of results on the page");
      checkState(pageResponse.getTotalElements() >= 0, "Negative number of results in the system");
      checkState(pageResponse.getTotalPages() >= 0, "Negative number of pages");
      checkState(pageResponse.getNumber() >= 0, "Negative number of items");
      return pageResponse;
    }
  }

  private static int getTotalPages(int pageSize, int totalElements) {
    return (int) Math.ceil(totalElements / (double) pageSize);
  }
}
