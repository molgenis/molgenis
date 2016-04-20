package org.molgenis.ontology.core.model;

import org.molgenis.data.meta.Package;

public class OntologyPackage
{
	private static Package ontologyPackage = null;
	public final static String PACKAGE_NAME = "Ontology";

	public static Package getPackageInstance()
	{
		if (ontologyPackage == null) ontologyPackage = new Package(PACKAGE_NAME,
				"This is a pacakge for storing ontology related model", null);
		return ontologyPackage;
	}
}
