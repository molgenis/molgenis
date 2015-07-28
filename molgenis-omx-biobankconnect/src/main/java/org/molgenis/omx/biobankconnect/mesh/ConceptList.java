package org.molgenis.omx.biobankconnect.mesh;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class ConceptList
{
	List<Concept> concepts;

	public List<Concept> getConcepts()
	{
		return concepts;
	}

	@XmlElement(name = "Concept")
	public void setConcepts(List<Concept> concepts)
	{
		this.concepts = concepts;
	}
}
