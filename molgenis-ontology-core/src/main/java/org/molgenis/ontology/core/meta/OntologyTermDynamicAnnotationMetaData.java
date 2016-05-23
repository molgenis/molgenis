package org.molgenis.ontology.core.meta;

import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_LABEL;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.ontology.core.model.OntologyPackage;
import org.springframework.stereotype.Component;

@Component
public class OntologyTermDynamicAnnotationMetaData extends DefaultEntityMetaData
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

		addAttributeMetaData(new DefaultAttributeMetaData(ID).setVisible(false), ROLE_ID);
		addAttributeMetaData(new DefaultAttributeMetaData(NAME, FieldTypeEnum.STRING).setNillable(false));
		addAttributeMetaData(new DefaultAttributeMetaData(VALUE, FieldTypeEnum.STRING).setNillable(false));
		addAttributeMetaData(new DefaultAttributeMetaData(LABEL, FieldTypeEnum.STRING).setNillable(false), ROLE_LABEL);
	}
}