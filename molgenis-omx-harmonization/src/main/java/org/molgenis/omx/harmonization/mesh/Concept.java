package org.molgenis.omx.harmonization.mesh;

import javax.xml.bind.annotation.XmlElement;

public class Concept
{
	String conceptUI;

	String scopeNote;

	ConceptName conceptName;

	TermList termList;

	public TermList getTermList()
	{
		return termList;
	}

	@XmlElement(name = "TermList")
	public void setTermList(TermList termList)
	{
		this.termList = termList;
	}

	public String getConceptUI()
	{
		return conceptUI;
	}

	@XmlElement(name = "ConceptUI")
	public void setConceptUI(String conceptUI)
	{
		this.conceptUI = conceptUI;
	}

	public String getScopeNote()
	{
		return scopeNote;
	}

	@XmlElement(name = "ScopeNote")
	public void setScopeNote(String scopeNote)
	{
		this.scopeNote = scopeNote;
	}

	public ConceptName getConceptName()
	{
		return conceptName;
	}

	@XmlElement(name = "ConceptName")
	public void setConceptName(ConceptName conceptName)
	{
		this.conceptName = conceptName;
	}
}