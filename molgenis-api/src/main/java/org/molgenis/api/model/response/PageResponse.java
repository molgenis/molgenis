package org.molgenis.api.model.response;

import static com.google.common.base.Preconditions.checkState;

import com.google.auto.value.AutoValue;
import org.molgenis.api.support.PageUtils;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_PageResponse.class)
@SuppressWarnings("java:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class PageResponse {
  /** Returns the requested size of the page. */
  public abstract int getSize();
  /** Returns the total number of elements available. */
  public abstract int getTotalElements();
  /** Returns how many pages are available in total. */
  public abstract int getTotalPages();
  /** Returns the number of the current page. */
  public abstract int getNumber();

  public static PageResponse create(int newSize, int newTotalElements, int newNumber) {
    int totalPages = PageUtils.getTotalPages(newSize, newTotalElements);
    return builder()
        .setSize(newSize)
        .setTotalElements(newTotalElements)
        .setTotalPages(totalPages)
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
}
