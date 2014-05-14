package org.molgenis.omx.biobankconnect.utils;

import java.util.Map;

import org.molgenis.search.Hit;

public class TermComparison implements Comparable<TermComparison>
{
	private final Hit hit;
	private final Integer synonymLength;
	private final Integer termLength;

	public TermComparison(Hit hit)
	{
		Map<String, Object> data = hit.getColumnValueMap();
		String ontologyTermSynonym = data.get("ontologyTermSynonym").toString().toLowerCase();
		String ontologyTerm = data.get("ontologyTerm").toString().toLowerCase();
		this.hit = hit;
		this.synonymLength = ontologyTermSynonym.split(" +").length;
		this.termLength = ontologyTerm.split(" +").length;
	}

	private Integer getSynonymLength()
	{
		return synonymLength;
	}

	private Integer getTermLength()
	{
		return termLength;
	}

	public Hit getHit()
	{
		return hit;
	}

	@Override
	public int compareTo(TermComparison other)
	{
		if (synonymLength.compareTo(other.getSynonymLength()) == 0)
		{
			return this.termLength.compareTo(other.getTermLength());
		}
		else return this.synonymLength.compareTo(other.getSynonymLength()) * (-1);
	}
}