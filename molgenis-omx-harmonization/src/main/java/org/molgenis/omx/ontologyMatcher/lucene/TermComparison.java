package org.molgenis.omx.ontologyMatcher.lucene;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class TermComparison implements Comparable<TermComparison>
{
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		List<TermComparison> lists = new ArrayList<TermComparison>();
		lists.add(new TermComparison("Total cholesterol", "Total Serum Cholesterol Measurement"));
		lists.add(new TermComparison("Total cholesterol", "Total cholesterol"));
		Collections.sort(lists);

		for (TermComparison term : lists)
		{
			System.out.println(term.getOntologyTermSynonym() + " : " + term.getOntologyTerm());
		}
	}

	private String ontologyTermSynonym;
	private String ontologyTerm;
	private Integer synonymLength;
	private Integer termLength;

	public TermComparison(String ontologyTermSynonym, String ontologyTerm)
	{
		this.ontologyTermSynonym = ontologyTermSynonym;
		this.ontologyTerm = ontologyTerm;
		this.synonymLength = this.ontologyTermSynonym.split(" +").length;
		this.termLength = this.ontologyTerm.split(" +").length;
	}

	public String getOntologyTermSynonym()
	{
		return ontologyTermSynonym;
	}

	public String getOntologyTerm()
	{
		return ontologyTerm;
	}

	public Integer getLength()
	{
		return synonymLength;
	}

	public Integer getTermLength()
	{
		return termLength;
	}

	@Override
	public int compareTo(TermComparison other)
	{
		if (this.synonymLength.compareTo(other.getLength()) == 0)
		{
			return this.termLength.compareTo(other.getTermLength()) * (-1);
		}
		return this.synonymLength.compareTo(other.getLength()) * (-1);
	}
}
