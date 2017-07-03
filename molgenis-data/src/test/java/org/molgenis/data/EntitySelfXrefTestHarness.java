package org.molgenis.data;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.*;
import org.molgenis.data.support.DynamicEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.AttributeType.XREF;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_LABEL;

@Component
public class EntitySelfXrefTestHarness
{
	public static final String ATTR_ID = "id_attr";
	public static final String ATTR_XREF = "xref_attr";
	public static final String ATTR_STRING = "string_attr";

	@Autowired
	private PackageFactory packageFactory;

	@Autowired
	private EntityTypeFactory entityTypeFactory;

	@Autowired
	private AttributeFactory attributeFactory;

	@Autowired
	private TestPackage testPackage;

	@PostConstruct
	public void postConstruct()
	{
	}

	public EntityType createDynamicEntityType()
	{
		return entityTypeFactory.create("SelfRef")
								.setLabel("SelfRef")
								.setBackend("PostgreSQL")
								.addAttribute(createAttribute(ATTR_ID, STRING), ROLE_ID)
								.addAttribute(createAttribute(ATTR_STRING, STRING).setNillable(false), ROLE_LABEL);
	}

	public void addSelfReference(EntityType entityType)
	{
		entityType.addAttribute(createAttribute(ATTR_XREF, XREF).setRefEntity(entityType));
	}

	private Attribute createAttribute(String name, AttributeType dataType)
	{
		return attributeFactory.create().setName(name).setDataType(dataType);
	}

	public Stream<Entity> createTestEntities(EntityType entityType, int numberOfEntities)
	{
		return IntStream.range(0, numberOfEntities).mapToObj(i -> createEntity(entityType, i));
	}

	private Entity createEntity(EntityType entityType, int id)
	{
		Entity entity1 = new DynamicEntity(entityType);
		entity1.set(ATTR_ID, "" + id);
		entity1.set(ATTR_XREF, entity1);
		entity1.set(ATTR_STRING, "attr_string_old");
		return entity1;
	}
}
