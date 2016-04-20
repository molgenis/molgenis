package org.molgenis.ontology.core.meta;

import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_LABEL;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.meta.AttributeMetaData;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.ontology.core.model.OntologyPackage;
import org.springframework.stereotype.Component;

@Component
public class OntologyTermSynonymMetaData extends EntityMetaData
{
	public final static String ID = "id";
	public final static String ONTOLOGY_TERM_SYNONYM = "ontologyTermSynonym";
	public final static String SIMPLE_NAME = "OntologyTermSynonym";
	public final static String ENTITY_NAME = OntologyPackage.PACKAGE_NAME + "_" + SIMPLE_NAME;
	public final static OntologyTermSynonymMetaData INSTANCE = new OntologyTermSynonymMetaData();

	private OntologyTermSynonymMetaData()
	{
		super(SIMPLE_NAME, OntologyPackage.getPackageInstance());
		addAttribute(new AttributeMetaData(ID).setVisible(false), ROLE_ID);
		AttributeMetaData ontologyTermSynonymAttr = new AttributeMetaData(ONTOLOGY_TERM_SYNONYM,
				FieldTypeEnum.TEXT).setNillable(false);
		addAttribute(ontologyTermSynonymAttr, ROLE_LABEL);
	}
}