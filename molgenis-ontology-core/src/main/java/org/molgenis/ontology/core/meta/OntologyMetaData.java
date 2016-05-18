package org.molgenis.ontology.core.meta;

import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_LABEL;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.meta.AttributeMetaData;
import org.molgenis.data.meta.EntityMetaDataImpl;
import org.molgenis.data.meta.SystemEntityMetaDataImpl;
import org.molgenis.ontology.core.model.OntologyPackage;
import org.springframework.stereotype.Component;

@Component
public class OntologyMetaData extends SystemEntityMetaDataImpl
{
	public final static String ID = "id";
	public final static String ONTOLOGY_IRI = "ontologyIRI";
	public final static String ONTOLOGY_NAME = "ontologyName";
	public final static String SIMPLE_NAME = "Ontology";
	public final static String ENTITY_NAME = OntologyPackage.PACKAGE_NAME + "_" + SIMPLE_NAME;

	@Override
	public void init()
	{
		setName(SIMPLE_NAME);
		setPackage(OntologyPackage.getPackageInstance());
		addAttribute(ID, ROLE_ID).setVisible(false);
		addAttribute(ONTOLOGY_IRI).setNillable(false);
		addAttribute(ONTOLOGY_NAME, ROLE_LABEL).setNillable(false);
	}
}