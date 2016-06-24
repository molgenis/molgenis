package org.molgenis.data.semanticsearch.explain.bean;

import org.molgenis.gson.AutoGson;

import com.google.auto.value.AutoValue;

@AutoValue
@AutoGson(autoValueClass = AutoValue_ExplainedQueryString.class)
public abstract class ExplainedQueryString
{
	public abstract String getMatchedWords();

	public abstract String getQueryString();

	public abstract String getTagName();

	public abstract double getScore();

	public static ExplainedQueryString create(String matchedWords, String queryString, String tagName, double score)
	{
		return new AutoValue_ExplainedQueryString(matchedWords, queryString, tagName, score);
	}
}