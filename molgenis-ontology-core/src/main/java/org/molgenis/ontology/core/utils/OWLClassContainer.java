package org.molgenis.ontology.core.utils;

import org.semanticweb.owlapi.model.OWLClass;

public class OWLClassContainer
{
	private final OWLClass owlClass;
	private final String nodePath;
	private final boolean root;

	public OWLClassContainer(OWLClass cls, String nodePath, boolean root)
	{
		this.owlClass = cls;
		this.nodePath = nodePath;
		this.root = root;
	}

	public OWLClass getOwlClass()
	{
		return owlClass;
	}

	public boolean isRoot()
	{
		return root;
	}

	public String getNodePath()
	{
		return nodePath;
	}
}
