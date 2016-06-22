package org.molgenis.test.data;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.*;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.support.DynamicEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;

@Component
public class DynamicEntityTestHarness
{
	public static final String ATTR_ID = "id_attr";
	public static final String ATTR_STRING = "string_attr";
	public static final String ATTR_BOOL = "bool_attr";
	public static final String ATTR_CATEGORICAL = "categorical_attr";
	public static final String ATTR_CATEGORICAL_MREF = "categorical_mref_attr";
	public static final String ATTR_DATE = "date_attr";
	public static final String ATTR_DATETIME = "datetime_attr";
	public static final String ATTR_DECIMAL = "decimal_attr";
	public static final String ATTR_HTML = "html_attr";
	public static final String ATTR_HYPERLINK = "hyperlink_attr";
	public static final String ATTR_LONG = "long_attr";
	public static final String ATTR_INT = "int_attr";
	public static final String ATTR_SCRIPT = "script_attr";
	public static final String ATTR_EMAIL = "email_attr";
	public static final String ATTR_XREF = "xref_attr";
	public static final String ATTR_MREF = "mref_attr";
	public static final String ATTR_REF_ID = "ref_id_attr";
	public static final String ATTR_REF_STRING = "ref_string_attr";

	@Autowired
	private PackageFactory packageFactory;

	@Autowired
	private AttributeMetaDataFactory attributeMetaDataFactory;

	@Autowired
	private EntityMetaDataFactory entityMetaDataFactory;
	private Package testPackage;

	public EntityMetaData createRefEntityMetaData()
	{
		testPackage = packageFactory.create("test");
		EntityMetaData result = entityMetaDataFactory.create();
		result.setPackage(testPackage);
		result.setSimpleName("TypeTestRef");
		AttributeMetaData idAttribute = attributeMetaDataFactory.create();
		idAttribute.setName(ATTR_REF_ID);
		AttributeMetaData labelAttribute = attributeMetaDataFactory.create();
		labelAttribute.setName(ATTR_STRING);

		result.setIdAttribute(idAttribute);
		result.setLabelAttribute(labelAttribute);
		result.addAttributes(newArrayList(idAttribute, labelAttribute));
		return result;
	}

	public List<Entity> createTestRefEntities(EntityMetaData refEntityMetaData, int numberOfEntities)
	{
		return IntStream.range(0, numberOfEntities).mapToObj(i -> createRefEntity(refEntityMetaData, i))
				.collect(Collectors.toList());
	}

	public Stream<Entity> createTestEntities(EntityMetaData entityMetaData, int numberOfEntities,
			List<Entity> refEntities)
	{
		return IntStream.range(0, numberOfEntities+1)
				.mapToObj(i -> createEntity(entityMetaData, i, refEntities.get(i % refEntities.size())));
	}

	private Entity createRefEntity(EntityMetaData refEntityMetaData, int id)
	{
		Entity refEntity = new DynamicEntity(refEntityMetaData);
		refEntity.set(ATTR_REF_ID, "" + id);
		refEntity.set(ATTR_REF_STRING, "refstring" + id);
		return refEntity;
	}

	private Entity createEntity(EntityMetaData entityMetaData, int id, Entity refEntity)
	{
		Entity entity1 = new DynamicEntity(entityMetaData);
		entity1.set(ATTR_ID, id);
		entity1.set(ATTR_STRING, "string1");
		entity1.set(ATTR_BOOL, true);
		entity1.set(ATTR_CATEGORICAL, refEntity);
		entity1.set(ATTR_CATEGORICAL_MREF, Collections.singletonList(refEntity));
		entity1.set(ATTR_DATE, "21-12-2012");
		entity1.set(ATTR_DATETIME, "1985-08-12T11:12:13+0500");
		entity1.set(ATTR_EMAIL, "this.is@mail.address");
		entity1.set(ATTR_DECIMAL, 1.123);
		entity1.set(ATTR_HTML, "<html>where is my head and where is my body</html>");
		entity1.set(ATTR_HYPERLINK, "http://www.molgenis.org");
		entity1.set(ATTR_LONG, 1000000);
		entity1.set(ATTR_INT, 18);
		entity1.set(ATTR_SCRIPT, "/bin/blaat/script.sh");
		entity1.set(ATTR_XREF, refEntity);
		entity1.set(ATTR_MREF, Collections.singletonList(refEntity));
		return entity1;
	}
}
