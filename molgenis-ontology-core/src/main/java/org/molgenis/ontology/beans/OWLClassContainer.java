package org.molgenis.ontology.beans;

import java.util.Map;
import java.util.Set;

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
	final Map<String, Set<String>> allDatabaseIds;

	public OWLClassContainer(OWLClass cls, String synonym, String classDefinition, String nodePath,
			String parentNodePath, String parentOntologyTermIRI, boolean root, boolean last, boolean original,
			String alternativeDefinitions, Map<String, Set<String>> allDatabaseIds)
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
		this.allDatabaseIds = allDatabaseIds;
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

	public boolean isLabel()
	{
		return original;
	}

	public Map<String, Set<String>> getAllDatabaseIds()
	{
		return allDatabaseIds;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OWLClassContainer)) return false;

        OWLClassContainer that = (OWLClassContainer) o;

        if (last != that.last) return false;
        if (original != that.original) return false;
        if (root != that.root) return false;
        if (allDatabaseIds != null ? !allDatabaseIds.equals(that.allDatabaseIds) : that.allDatabaseIds != null)
            return false;
        if (alternativeDefinitions != null ? !alternativeDefinitions.equals(that.alternativeDefinitions) : that.alternativeDefinitions != null)
            return false;
        if (classDefinition != null ? !classDefinition.equals(that.classDefinition) : that.classDefinition != null)
            return false;
        if (classLabel != null ? !classLabel.equals(that.classLabel) : that.classLabel != null) return false;
        if (nodePath != null ? !nodePath.equals(that.nodePath) : that.nodePath != null) return false;
        if (owlClass != null ? !owlClass.equals(that.owlClass) : that.owlClass != null) return false;
        if (parentNodePath != null ? !parentNodePath.equals(that.parentNodePath) : that.parentNodePath != null)
            return false;
        if (parentOntologyTermIRI != null ? !parentOntologyTermIRI.equals(that.parentOntologyTermIRI) : that.parentOntologyTermIRI != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = owlClass != null ? owlClass.hashCode() : 0;
        result = 31 * result + (classLabel != null ? classLabel.hashCode() : 0);
        result = 31 * result + (classDefinition != null ? classDefinition.hashCode() : 0);
        result = 31 * result + (nodePath != null ? nodePath.hashCode() : 0);
        result = 31 * result + (parentNodePath != null ? parentNodePath.hashCode() : 0);
        result = 31 * result + (parentOntologyTermIRI != null ? parentOntologyTermIRI.hashCode() : 0);
        result = 31 * result + (root ? 1 : 0);
        result = 31 * result + (last ? 1 : 0);
        result = 31 * result + (original ? 1 : 0);
        result = 31 * result + (alternativeDefinitions != null ? alternativeDefinitions.hashCode() : 0);
        result = 31 * result + (allDatabaseIds != null ? allDatabaseIds.hashCode() : 0);
        return result;
    }
}