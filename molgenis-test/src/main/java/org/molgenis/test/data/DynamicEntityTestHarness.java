package org.molgenis.test.data;

import com.google.common.collect.Lists;
import org.molgenis.data.meta.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.google.common.collect.Lists.newArrayList;

@Component
public class DynamicEntityTestHarness
{
	public static final String ID = "ID";
	public static final String LABEL = "label";

	@Autowired
	private PackageFactory packageFactory;

	@Autowired
	private AttributeMetaDataFactory attributeMetaDataFactory;

	@Autowired
	private EntityMetaDataFactory entityMetaDataFactory;

	public EntityMetaData createRefEntityMetaData()
	{
		org.molgenis.data.meta.Package testPackage = packageFactory.create("test");
		EntityMetaData result = entityMetaDataFactory.create();
		result.setPackage(testPackage);
		result.setSimpleName("TypeTestRef");
		AttributeMetaData idAttribute = attributeMetaDataFactory.create();
		idAttribute.setName(ID);
		AttributeMetaData labelAttribute = attributeMetaDataFactory.create();
		labelAttribute.setName(LABEL);

		result.setIdAttribute(idAttribute);
		result.setLabelAttribute(labelAttribute);
		result.addAttributes(newArrayList(idAttribute, labelAttribute));
		return result;
	}
}
