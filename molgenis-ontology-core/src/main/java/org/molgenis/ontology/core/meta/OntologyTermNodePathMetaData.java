package org.molgenis.ontology.core.meta;

import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_LABEL;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.meta.AttributeMetaData;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.ontology.core.model.OntologyPackage;
import org.springframework.stereotype.Component;

@Component
public class OntologyTermNodePathMetaData extends EntityMetaData
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
		addAttribute(new AttributeMetaData(ID).setVisible(false), ROLE_ID);
		addAttribute(
				new AttributeMetaData(ONTOLOGY_TERM_NODE_PATH, FieldTypeEnum.TEXT).setNillable(false),
				ROLE_LABEL);
		addAttribute(new AttributeMetaData(ROOT, FieldTypeEnum.BOOL).setNillable(false));
	}
}