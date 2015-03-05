package org.molgenis.ontology.model;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;

public class OntologyTermNodePathMetaData
{
	public final static String ID = "id";
	public final static String ONTOLOGY_TERM_NODE_PATH = "nodePath";
	public final static String ROOT = "root";
	public final static String SIMPLE_NAME = "OntologyTermNodePath";
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

		DefaultAttributeMetaData ontologyTermNodePathAttr = new DefaultAttributeMetaData(ONTOLOGY_TERM_NODE_PATH,
				FieldTypeEnum.STRING);
		ontologyTermNodePathAttr.setNillable(false);
		ontologyTermNodePathAttr.setLabelAttribute(true);
		entityMetaData.addAttributeMetaData(ontologyTermNodePathAttr);

		DefaultAttributeMetaData ontologyTermRootAttr = new DefaultAttributeMetaData(ROOT, FieldTypeEnum.BOOL);
		ontologyTermRootAttr.setNillable(false);
		entityMetaData.addAttributeMetaData(ontologyTermRootAttr);

		return entityMetaData;
	}
}