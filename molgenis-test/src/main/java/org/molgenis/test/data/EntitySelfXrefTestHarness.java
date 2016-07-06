package org.molgenis.test.data;

import org.molgenis.MolgenisFieldTypes.AttributeType;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.*;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.util.MolgenisDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.molgenis.MolgenisFieldTypes.AttributeType.*;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_LABEL;

@Component
public class EntitySelfXrefTestHarness
{
	public static final String ATTR_ID = "id_attr";
	public static final String ATTR_XREF = "xref_attr";

	@Autowired
	private PackageFactory packageFactory;

	@Autowired
	private EntityMetaDataFactory entityMetaDataFactory;

	@Autowired
	private AttributeMetaDataFactory attributeMetaDataFactory;

	private Package testPackage;

	@PostConstruct
	public void postConstruct()
	{
		testPackage = packageFactory.create("test");
	}

	public EntityMetaData createDynamicEntityMetaData()
	{
		return entityMetaDataFactory.create().setPackage(testPackage).setSimpleName("SelfRef")
				.addAttribute(createAttribute(ATTR_ID, STRING), ROLE_ID)
				.addAttribute(createAttribute(ATTR_XREF, STRING), ROLE_LABEL);
	}

	private AttributeMetaData createAttribute(String name, AttributeType dataType)
	{
		return attributeMetaDataFactory.create().setName(name).setDataType(dataType);
	}

	public Stream<Entity> createTestEntities(EntityMetaData entityMetaData, int numberOfEntities)
	{
		return IntStream.range(0, numberOfEntities)
				.mapToObj(i -> createEntity(entityMetaData, i));
	}

	private Entity createEntity(EntityMetaData entityMetaData, int id)
	{
		Entity entity1 = new DynamicEntity(entityMetaData);
		entity1.set(ATTR_ID, "" + id);
		entity1.set(ATTR_XREF, entity1);
		return entity1;
	}
}
