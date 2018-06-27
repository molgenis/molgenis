package org.molgenis.semanticsearch.explain.bean;

import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
@SuppressWarnings("squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class Hits<ExplainedAttribute>
{
	public abstract List<ExplainedAttribute> getHits();

	public static <ExplainedAttribute> Hits<ExplainedAttribute> create(List<ExplainedAttribute> attributeSearchHits)
	{
		return new AutoValue_Hits<>(attributeSearchHits);
	}
}
