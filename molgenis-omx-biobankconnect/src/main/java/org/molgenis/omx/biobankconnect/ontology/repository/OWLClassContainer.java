package org.molgenis.omx.biobankconnect.ontology.repository;

import org.apache.commons.lang3.StringUtils;
import org.semanticweb.owlapi.model.OWLClass;

public class OWLClassContainer
{
	final OWLClass owlClass;
	final String classLabel;
	final String classDefinition;
	final String nodePath;
	final String parentNodePath;
	final String parentOntologyTermIRI;
	final boolean root;
	final boolean last;
	final boolean original;
	final String alternativeDefinitions;

	public OWLClassContainer(OWLClass cls, String synonym, String classDefinition, String nodePath,
			String parentNodePath, String parentOntologyTermIRI, boolean root, boolean last, boolean original,
			String alternativeDefinitions)
	{
		this.owlClass = cls;
		this.classLabel = synonym;
		this.classDefinition = classDefinition;
		this.nodePath = nodePath;
		this.parentNodePath = parentNodePath;
		this.parentOntologyTermIRI = parentOntologyTermIRI;
		this.root = root;
		this.last = last;
		this.original = original;
		this.alternativeDefinitions = alternativeDefinitions;
	}

	public OWLClass getOWLClass()
	{
		return owlClass;
	}

	public String getClassLabel()
	{
		return classLabel;
	}

	public String getClassDefinition()
	{
		return classDefinition == null ? StringUtils.EMPTY : classDefinition;
	}

	public String getParentNodePath()
	{
		return parentNodePath == null ? StringUtils.EMPTY : parentNodePath;
	}

	public String getParentOntologyTermIRI()
	{
		return parentOntologyTermIRI;
	}

	public boolean isRoot()
	{
		return root;
	}

	public boolean isLast()
	{
		return last;
	}

	public String getNodePath()
	{
		return nodePath;
	}

	public String getAssociatedClasses()
	{
		return alternativeDefinitions == null ? StringUtils.EMPTY : alternativeDefinitions;
	}

	public boolean isOriginal()
	{
		return original;
	}
}