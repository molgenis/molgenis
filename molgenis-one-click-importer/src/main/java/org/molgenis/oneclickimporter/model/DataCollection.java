package org.molgenis.oneclickimporter.model;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

import java.util.List;

@AutoValue
@AutoGson(autoValueClass = AutoValue_DataCollection.class)
public abstract class DataCollection
{
	public abstract String getName();

	public abstract List<Column> getColumns();

	public abstract int getNumberOfRows();

	public static DataCollection create(String name, List<Column> columns, int numberOfRows)
	{
		return new AutoValue_DataCollection(name, columns, numberOfRows);
	}
}
