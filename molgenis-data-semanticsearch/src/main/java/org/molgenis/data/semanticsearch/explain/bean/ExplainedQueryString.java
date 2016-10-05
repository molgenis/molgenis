package org.molgenis.data.semanticsearch.explain.bean;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_ExplainedQueryString.class)
public abstract class ExplainedQueryString
{
	public abstract String getMatchedWords();

	public abstract String getQueryString();

	public abstract String getTagName();

	public abstract float getScore();

	public static ExplainedQueryString create(String matchedWords, String queryString, String tagName, float score)
	{
		return new AutoValue_ExplainedQueryString(matchedWords, queryString, tagName, score);
	}
}