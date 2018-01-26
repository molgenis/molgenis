package org.molgenis.ontology.core.importer.repository;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

public enum OntologyFileExtensions
{
	OBO_ZIP("obo.zip"), OWL_ZIP("owl.zip");

	private String name;

	OntologyFileExtensions(String name)
	{
		this.name = name;
	}

	public static Set<String> getOntology()
	{
		return ImmutableSet.of(OBO_ZIP.toString(), OWL_ZIP.toString());
	}

	@Override
	public String toString()
	{
		return this.name;
	}
}
