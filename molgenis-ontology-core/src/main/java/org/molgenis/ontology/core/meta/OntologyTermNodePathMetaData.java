package org.molgenis.ontology.core.meta;

import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.ontology.core.model.OntologyPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.BOOL;
import static org.molgenis.data.meta.AttributeType.TEXT;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.ontology.core.model.OntologyPackage.PACKAGE_ONTOLOGY;

@Component
public class OntologyTermNodePathMetaData extends SystemEntityType
{
	public static final String SIMPLE_NAME = "OntologyTermNodePath";
	public final static String ONTOLOGY_TERM_NODE_PATH = PACKAGE_ONTOLOGY + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public final static String ID = "id";
	public final static String NODE_PATH = "nodePath";
	public final static String ROOT = "root";

	private OntologyPackage ontologyPackage;

	public OntologyTermNodePathMetaData()
	{
		super(SIMPLE_NAME, PACKAGE_ONTOLOGY);
	}

	@Override
	public void init()
	{
		setLabel("Ontology term node path");
		setPackage(ontologyPackage);
		addAttribute(ID, ROLE_ID).setVisible(false);
		addAttribute(NODE_PATH, ROLE_LABEL).setDataType(TEXT).setNillable(false);
		addAttribute(ROOT).setDataType(BOOL).setNillable(false);
	}

	@Autowired
	public void setOntologyPackage(OntologyPackage ontologyPackage)
	{
		this.ontologyPackage = requireNonNull(ontologyPackage);
	}
}