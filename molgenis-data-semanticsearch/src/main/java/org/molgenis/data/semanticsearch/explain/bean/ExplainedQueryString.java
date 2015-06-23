package org.molgenis.data.semanticsearch.explain.bean;

public class ExplainedQueryString
{
	private final String matchedTerms;
	private final String queryString;
	private final String tagName;
	private final double score;

	public ExplainedQueryString(String matchedTerms, String queryString, String tagName, double score)
	{
		this.matchedTerms = matchedTerms;
		this.queryString = queryString;
		this.tagName = tagName;
		this.score = score;
	}

	public String getMatchedTerms()
	{
		return matchedTerms;
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
		result = prime * result + ((matchedTerms == null) ? 0 : matchedTerms.hashCode());
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
		if (matchedTerms == null)
		{
			if (other.matchedTerms != null) return false;
		}
		else if (!matchedTerms.equals(other.matchedTerms)) return false;
		if (queryString == null)
		{
			if (other.queryString != null) return false;
		}
		else if (!queryString.equals(other.queryString)) return false;
		return true;
	}
}
