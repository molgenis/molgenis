package org.molgenis.oneclickimporter.model;

import com.google.auto.value.AutoValue;
import org.molgenis.util.AutoGson;

import java.util.List;

@AutoValue
@AutoGson(autoValueClass = AutoValue_Column.class)
public abstract class Column
{
	public abstract String getName();

	public abstract int getPosition();

	public abstract List<Object> getDataValues();

	public static Column create(String name, int position, List<Object> dataValues)
	{
		return new AutoValue_Column(name, position, dataValues);
	}
}
