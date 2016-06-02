package org.molgenis.ontology.core.model;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.RootSystemPackage.PACKAGE_SYSTEM;

import org.molgenis.data.meta.PackageMetaData;
import org.molgenis.data.meta.RootSystemPackage;
import org.molgenis.data.meta.SystemPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OntologyPackage extends SystemPackage
{
	public static final String SIMPLE_NAME = "ont";
	public static final String PACKAGE_ONTOLOGY = PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

	private final RootSystemPackage rootSystemPackage;

	@Autowired
	public OntologyPackage(PackageMetaData packageMetaData, RootSystemPackage rootSystemPackage)
	{
		super(SIMPLE_NAME, packageMetaData);
		this.rootSystemPackage = requireNonNull(rootSystemPackage);
	}

	@Override
	protected void init()
	{
		setLabel("Ontology");
		setParent(rootSystemPackage);
	}
}
