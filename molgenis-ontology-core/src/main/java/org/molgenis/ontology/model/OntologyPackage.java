package org.molgenis.ontology.model;

import org.molgenis.data.Package;
import org.molgenis.data.meta.PackageImpl;

public class OntologyPackage
{
	private static Package ontologyPackage = null;
	public final static String PACKAGE_NAME = "Ontology";

	public static Package getPackageInstance()
	{
		if (ontologyPackage == null) ontologyPackage = new PackageImpl(PACKAGE_NAME,
				"This is a pacakge for storing ontology related model", null);
		return ontologyPackage;
	}
}
