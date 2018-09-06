package org.molgenis.navigator;

import com.google.auto.value.AutoValue;
import java.util.List;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_DeleteItemsRequest.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class DeleteItemsRequest {

  public abstract List<String> getPackageIds();

  public abstract List<String> getEntityTypeIds();

  public static DeleteItemsRequest create(List<String> packageIds, List<String> entityTypeIds) {
    return new AutoValue_DeleteItemsRequest(packageIds, entityTypeIds);
  }
}
