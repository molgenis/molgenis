package org.molgenis.data.elasticsearch.generator.model;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Index
{
	public abstract String getName();

	public static Index create(String name)
	{
		return new AutoValue_Index(name);
	}
}
