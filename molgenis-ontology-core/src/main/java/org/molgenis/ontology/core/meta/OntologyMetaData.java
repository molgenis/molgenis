package org.molgenis.ontology.core.meta;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.ontology.core.model.OntologyPackage.PACKAGE_ONTOLOGY;

import org.molgenis.data.meta.SystemEntityMetaData;
import org.molgenis.ontology.core.model.OntologyPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OntologyMetaData extends SystemEntityMetaData
{
	public static final String SIMPLE_NAME = "Ontology";
	public final static String ONTOLOGY = PACKAGE_ONTOLOGY + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public final static String ID = "id";
	public final static String ONTOLOGY_IRI = "ontologyIRI";
	public final static String ONTOLOGY_NAME = "ontologyName";

	private final OntologyPackage ontologyPackage;

	@Autowired
	public OntologyMetaData(OntologyPackage ontologyPackage)
	{
		super(SIMPLE_NAME, PACKAGE_ONTOLOGY);
		this.ontologyPackage = requireNonNull(ontologyPackage);
	}

	@Override
	public void init()
	{
		setPackage(ontologyPackage);

		addAttribute(ID, ROLE_ID).setVisible(false);
		addAttribute(ONTOLOGY_IRI).setNillable(false);
		addAttribute(ONTOLOGY_NAME, ROLE_LABEL).setNillable(false);
	}
}