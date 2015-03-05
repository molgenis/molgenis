package org.molgenis.ontology.model;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;

public class OntologyTermSynonymMetaData
{
	public final static String ID = "id";
	public final static String ONTOLOGY_TERM_SYNONYM = "ontologyTermSynonym";
	public final static String SIMPLE_NAME = "OntologyTermSynonym";
	public final static String ENTITY_NAME = OntologyPackage.PACKAGE_NAME + "_" + SIMPLE_NAME;

	public static EntityMetaData getEntityMetaData()
	{
		DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData(SIMPLE_NAME,
				OntologyPackage.getPackageInstance());

		DefaultAttributeMetaData idAttr = new DefaultAttributeMetaData(ID);
		idAttr.setIdAttribute(true);
		idAttr.setNillable(false);
		idAttr.setVisible(false);
		entityMetaData.addAttributeMetaData(idAttr);

		DefaultAttributeMetaData ontologyTermSynonymAttr = new DefaultAttributeMetaData(ONTOLOGY_TERM_SYNONYM,
				FieldTypeEnum.STRING);
		ontologyTermSynonymAttr.setNillable(false);
		ontologyTermSynonymAttr.setLabelAttribute(true);
		entityMetaData.addAttributeMetaData(ontologyTermSynonymAttr);

		return entityMetaData;
	}
}