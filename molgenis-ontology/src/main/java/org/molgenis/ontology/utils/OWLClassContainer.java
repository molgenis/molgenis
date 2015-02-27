package org.molgenis.ontology.utils;

import org.semanticweb.owlapi.model.OWLClass;

public class OWLClassContainer
{
	private final OWLClass owlClass;
	private final String nodePath;

	public OWLClassContainer(OWLClass cls, String nodePath)
	{
		this.owlClass = cls;
		this.nodePath = nodePath;
	}

	public OWLClass getOwlClass()
	{
		return owlClass;
	}

	public String getNodePath()
	{
		return nodePath;
	}
}
