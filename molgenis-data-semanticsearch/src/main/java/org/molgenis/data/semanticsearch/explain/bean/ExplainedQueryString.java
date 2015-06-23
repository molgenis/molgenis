package org.molgenis.data.semanticsearch.explain.bean;

public class ExplainedQueryString
{
	private final String matchedWords;
	private final String queryString;
	private final String tagName;
	private final double score;

	public ExplainedQueryString(String matchedWords, String queryString, String tagName, double score)
	{
		this.matchedWords = matchedWords;
		this.queryString = queryString;
		this.tagName = tagName;
		this.score = score;
	}

	public String getMatchedWords()
	{
		return matchedWords;
	}

	public String getQueryString()
	{
		return queryString;
	}

	public String getTagName()
	{
		return tagName;
	}

	public double getScore()
	{
		return score;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((matchedWords == null) ? 0 : matchedWords.hashCode());
		result = prime * result + ((queryString == null) ? 0 : queryString.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		ExplainedQueryString other = (ExplainedQueryString) obj;
		if (matchedWords == null)
		{
			if (other.matchedWords != null) return false;
		}
		else if (!matchedWords.equals(other.matchedWords)) return false;
		if (queryString == null)
		{
			if (other.queryString != null) return false;
		}
		else if (!queryString.equals(other.queryString)) return false;
		return true;
	}
}
