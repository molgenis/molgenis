package org.molgenis.oneclickimporter.model;

import com.google.auto.value.AutoValue;
import java.util.List;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_DataCollection.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class DataCollection {
  public abstract String getName();

  public abstract List<Column> getColumns();

  public static DataCollection create(String name, List<Column> columns) {
    return new AutoValue_DataCollection(name, columns);
  }
}
