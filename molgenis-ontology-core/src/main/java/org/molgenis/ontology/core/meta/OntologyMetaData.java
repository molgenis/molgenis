package org.molgenis.ontology.core.meta;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.Package.PACKAGE_SEPARATOR;
import static org.molgenis.ontology.core.model.OntologyPackage.PACKAGE_ONTOLOGY;

import org.molgenis.data.meta.SystemEntityMetaDataImpl;
import org.molgenis.ontology.core.model.OntologyPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OntologyMetaData extends SystemEntityMetaDataImpl
{
	public final static String SIMPLE_NAME = "Ontology";
	public final static String ONTOLOGY = PACKAGE_ONTOLOGY + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public final static String ID = "id";
	public final static String ONTOLOGY_IRI = "ontologyIRI";
	public final static String ONTOLOGY_NAME = "ontologyName";

	private OntologyPackage ontologyPackage;

	public OntologyMetaData()
	{
		super(SIMPLE_NAME, PACKAGE_ONTOLOGY);
	}

	@Override
	public void init()
	{
		setPackage(ontologyPackage);
		addAttribute(ID, ROLE_ID).setVisible(false);
		addAttribute(ONTOLOGY_IRI).setNillable(false);
		addAttribute(ONTOLOGY_NAME, ROLE_LABEL).setNillable(false);
	}

	// setter injection instead of constructor injection to avoid unresolvable circular dependencies
	@Autowired
	public void setOntologyPackage(OntologyPackage ontologyPackage)
	{
		this.ontologyPackage = requireNonNull(ontologyPackage);
	}
}