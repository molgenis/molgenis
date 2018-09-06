package org.molgenis.oneclickimporter.model;

import com.google.auto.value.AutoValue;
import java.util.List;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_Column.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class Column {
  public abstract String getName();

  public abstract int getPosition();

  public abstract List<Object> getDataValues();

  public static Column create(String name, int position, List<Object> dataValues) {
    return new AutoValue_Column(name, position, dataValues);
  }
}
