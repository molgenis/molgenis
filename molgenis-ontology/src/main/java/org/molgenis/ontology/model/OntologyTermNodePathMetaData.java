package org.molgenis.ontology.model;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;

public class OntologyTermNodePathMetaData extends DefaultEntityMetaData
{
	public final static String ID = "id";
	public final static String ONTOLOGY_TERM_NODE_PATH = "nodePath";
	public final static String ROOT = "root";
	public final static String SIMPLE_NAME = "OntologyTermNodePath";
	public final static String ENTITY_NAME = OntologyPackage.PACKAGE_NAME + "_" + SIMPLE_NAME;
	public final static OntologyTermNodePathMetaData INSTANCE = new OntologyTermNodePathMetaData();

	private OntologyTermNodePathMetaData()
	{
		super(SIMPLE_NAME, OntologyPackage.getPackageInstance());
		addAttributeMetaData(new DefaultAttributeMetaData(ID).setIdAttribute(true).setNillable(false).setVisible(false));
		addAttributeMetaData(new DefaultAttributeMetaData(ONTOLOGY_TERM_NODE_PATH, FieldTypeEnum.STRING).setNillable(
				false).setLabelAttribute(true));
		addAttributeMetaData(new DefaultAttributeMetaData(ROOT, FieldTypeEnum.BOOL).setNillable(false));
	}
}