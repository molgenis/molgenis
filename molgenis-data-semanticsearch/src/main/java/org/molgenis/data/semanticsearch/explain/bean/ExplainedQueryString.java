package org.molgenis.data.semanticsearch.explain.bean;

public class ExplainedQueryString
{
	private final String matchedTerm;
	private final String queryValue;
	private final String relatedQuery;
	private final double score;

	public ExplainedQueryString(String matchedTerm, String queryValue, String relatedQuery, double score)
	{
		this.matchedTerm = matchedTerm;
		this.queryValue = queryValue;
		this.relatedQuery = relatedQuery;
		this.score = score;
	}

	public String getMatchedTerm()
	{
		return matchedTerm;
	}

	public String getQueryValue()
	{
		return queryValue;
	}

	public String getRelatedQuery()
	{
		return relatedQuery;
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
		result = prime * result + ((matchedTerm == null) ? 0 : matchedTerm.hashCode());
		result = prime * result + ((queryValue == null) ? 0 : queryValue.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		ExplainedQueryString other = (ExplainedQueryString) obj;
		if (matchedTerm == null)
		{
			if (other.matchedTerm != null) return false;
		}
		else if (!matchedTerm.equals(other.matchedTerm)) return false;
		if (queryValue == null)
		{
			if (other.queryValue != null) return false;
		}
		else if (!queryValue.equals(other.queryValue)) return false;
		return true;
	}
}
