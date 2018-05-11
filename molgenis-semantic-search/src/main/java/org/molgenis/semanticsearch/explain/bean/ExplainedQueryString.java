package org.molgenis.semanticsearch.explain.bean;

import com.google.auto.value.AutoValue;
import org.molgenis.util.AutoGson;

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