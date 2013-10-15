package org.molgenis.omx.biobankconnect.mesh;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class TermList
{
	List<Term> terms;

	public List<Term> getTerms()
	{
		return terms;
	}

	@XmlElement(name = "Term")
	public void setTerms(List<Term> terms)
	{
		this.terms = terms;
	}
}
