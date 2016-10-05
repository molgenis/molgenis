package org.molgenis.data.semanticsearch.explain.bean;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

import java.util.Collections;
import java.util.List;

@AutoValue
@AutoGson(autoValueClass = AutoValue_ExplainedMatchCandidate.class)
public abstract class ExplainedMatchCandidate<T>
{
	public static <T> ExplainedMatchCandidate<T> create(T match)
	{
		return new AutoValue_ExplainedMatchCandidate<T>(match, Collections.emptyList(), false);
	}

	public static <T> ExplainedMatchCandidate<T> create(T match, List<ExplainedQueryString> explainedQueryStrings,
			boolean highQuality)
	{
		return new AutoValue_ExplainedMatchCandidate<T>(match, explainedQueryStrings, highQuality);
	}

	public abstract T getMatch();

	public abstract List<ExplainedQueryString> getExplainedQueryStrings();

	public abstract boolean isHighQuality();
}