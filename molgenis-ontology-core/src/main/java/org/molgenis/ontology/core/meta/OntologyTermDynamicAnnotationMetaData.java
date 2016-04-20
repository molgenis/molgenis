package org.molgenis.ontology.core.meta;

import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_LABEL;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.meta.AttributeMetaData;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.ontology.core.model.OntologyPackage;
import org.springframework.stereotype.Component;

@Component
public class OntologyTermDynamicAnnotationMetaData extends EntityMetaData
{
	public final static String ID = "id";
	public final static String NAME = "name";
	public final static String VALUE = "value";
	public final static String LABEL = "label";
	public final static String SIMPLE_NAME = "OntologyTermDynamicAnnotation";
	public final static String ENTITY_NAME = OntologyPackage.PACKAGE_NAME + "_" + SIMPLE_NAME;

	public final static OntologyTermDynamicAnnotationMetaData INSTANCE = new OntologyTermDynamicAnnotationMetaData();

	private OntologyTermDynamicAnnotationMetaData()
	{
		super(SIMPLE_NAME, OntologyPackage.getPackageInstance());

		addAttribute(new AttributeMetaData(ID).setVisible(false), ROLE_ID);
		addAttribute(new AttributeMetaData(NAME, FieldTypeEnum.STRING).setNillable(false));
		addAttribute(new AttributeMetaData(VALUE, FieldTypeEnum.STRING).setNillable(false));
		addAttribute(new AttributeMetaData(LABEL, FieldTypeEnum.STRING).setNillable(false), ROLE_LABEL);
	}
}