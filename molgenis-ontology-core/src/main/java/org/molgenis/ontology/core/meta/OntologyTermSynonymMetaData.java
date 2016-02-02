package org.molgenis.ontology.core.meta;

import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_LABEL;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.ontology.core.model.OntologyPackage;

public class OntologyTermSynonymMetaData extends DefaultEntityMetaData
{
	public final static String ID = "id";
	public final static String ONTOLOGY_TERM_SYNONYM = "ontologyTermSynonym";
	public final static String SIMPLE_NAME = "OntologyTermSynonym";
	public final static String ENTITY_NAME = OntologyPackage.PACKAGE_NAME + "_" + SIMPLE_NAME;
	public final static OntologyTermSynonymMetaData INSTANCE = new OntologyTermSynonymMetaData();

	private OntologyTermSynonymMetaData()
	{
		super(SIMPLE_NAME, OntologyPackage.getPackageInstance());
		addAttributeMetaData(new DefaultAttributeMetaData(ID).setVisible(false), ROLE_ID);

		DefaultAttributeMetaData ontologyTermSynonymAttr = new DefaultAttributeMetaData(ONTOLOGY_TERM_SYNONYM,
				FieldTypeEnum.STRING).setNillable(false);
		addAttributeMetaData(ontologyTermSynonymAttr, ROLE_LABEL);
	}
}