package org.molgenis.data;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.*;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.staticentity.TestEntityStaticMetaData;
import org.molgenis.data.staticentity.TestRefEntityStaticMetaData;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.support.EntityWithComputedAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.String.valueOf;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_LABEL;

@Component
public class EntityTestHarness
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
	public static final String ATTR_COMPUTED_INT = "computed_int_attr";
	public static final String ATTR_COMPOUND = "compound_attr";
	public static final String ATTR_COMPOUND_CHILD_INT = "compound_child_int_attr";
	public static final String ATTR_ENUM = "enum_attr";

	@Autowired
	private PackageFactory packageFactory;

	@Autowired
	private EntityTypeFactory entityTypeFactory;

	@Autowired
	private AttributeFactory attributeFactory;

	@Autowired
	TestEntityStaticMetaData staticTestEntityStaticMetaData;

	@Autowired
	TestRefEntityStaticMetaData staticTestRefEntityStaticMetaData;

	@Autowired
	private TestPackage testPackage;

	private final LocalDate date = LocalDate.parse("2012-12-21");
	private final Instant dateTime = Instant.parse("1985-08-12T06:12:13Z");

	@PostConstruct
	public void postConstruct()
	{
	}

	public EntityType createStaticRefTestEntityType()
	{
		return staticTestRefEntityStaticMetaData;
	}

	public EntityType createStaticTestEntityType()
	{
		return staticTestEntityStaticMetaData;
	}

	public EntityType createDynamicRefEntityType(String id, Package package_)
	{
		EntityType refEntityType = entityTypeFactory.create(id)
													.setLabel(id)
													.setBackend("PostgreSQL")
													.setPackage(package_);
		return createDynamicRefEntityType(refEntityType);
	}

	public EntityType createDynamicRefEntityType()
	{
		return createDynamicRefEntityType("TypeTestRefDynamic");
	}

	public EntityType createDynamicRefEntityType(String id)
	{
		EntityType refEntityType = entityTypeFactory.create(id).setLabel(id)
													.setBackend("PostgreSQL");
		return createDynamicRefEntityType(refEntityType);
	}

	private EntityType createDynamicRefEntityType(EntityType refEntityType)
	{
		refEntityType.addAttribute(createAttribute(ATTR_REF_ID, STRING), ROLE_ID)
					 .addAttribute(createAttribute(ATTR_REF_STRING, STRING).setNillable(false), ROLE_LABEL);
		return refEntityType;
	}

	public EntityType createDynamicTestEntityType(String id, Package package_, EntityType refEntityType)
	{
		EntityType entityType = entityTypeFactory.create(id).setLabel(id).setBackend("PostgreSQL").setPackage(package_);
		return createDynamicTestEntityType(entityType, refEntityType);
	}

	public EntityType createDynamicTestEntityType(EntityType refEntityType)
	{
		return createDynamicTestEntityType(refEntityType, "TypeTestDynamic");
	}

	public EntityType createDynamicTestEntityType(EntityType refEntityType, String id)
	{
		EntityType entityType = entityTypeFactory.create(id).setLabel(id).setBackend("PostgreSQL");
		return createDynamicTestEntityType(entityType, refEntityType);
	}

	public EntityType createDynamicSelfReferencingTestEntityType()
	{
		EntityType entityType = entityTypeFactory.create("TypeTestDynamicSelfReference")
												 .setLabel("TypeTestDynamicSelfReference")
												 .setBackend("PostgreSQL");
		return createDynamicTestEntityType(entityType, entityType);
	}

	private EntityType createDynamicTestEntityType(EntityType entityType, EntityType refEntityType)
	{
		entityType.addAttribute(createAttribute(ATTR_ID, STRING).setAuto(true), ROLE_ID)
				  .addAttribute(createAttribute(ATTR_STRING, STRING).setNillable(false), ROLE_LABEL)
				  .addAttribute(createAttribute(ATTR_BOOL, BOOL).setAggregatable(true)
																.setNillable(false)
																.setDefaultValue(valueOf(true)))
				  .addAttribute(createAttribute(ATTR_CATEGORICAL, CATEGORICAL).setRefEntity(refEntityType))
				  .addAttribute(createAttribute(ATTR_CATEGORICAL_MREF, CATEGORICAL_MREF).setRefEntity(refEntityType))
				  .addAttribute(createAttribute(ATTR_DATE, DATE))
				  .addAttribute(createAttribute(ATTR_ENUM, ENUM).setEnumOptions(asList("option1", "option2"))
																.setNillable(false)
																.setAggregatable(true)
																.setDefaultValue("option1"))
				  .addAttribute(createAttribute(ATTR_DATETIME, DATE_TIME))
				  .addAttribute(createAttribute(ATTR_EMAIL, EMAIL))
				  .addAttribute(createAttribute(ATTR_DECIMAL, DECIMAL))
				  .addAttribute(createAttribute(ATTR_HTML, HTML))
				  .addAttribute(createAttribute(ATTR_HYPERLINK, HYPERLINK))
				  .addAttribute(createAttribute(ATTR_LONG, LONG))
				  .addAttribute(createAttribute(ATTR_INT, INT).setAggregatable(true)
															  .setNillable(false)
															  .setDefaultValue(valueOf(1)))
				  .addAttribute(createAttribute(ATTR_SCRIPT, SCRIPT))
				  .addAttribute(createAttribute(ATTR_XREF, XREF).setRefEntity(refEntityType))
				  .addAttribute(createAttribute(ATTR_MREF, MREF).setRefEntity(refEntityType))
				  .addAttribute(createAttribute(ATTR_COMPUTED_INT, INT).setExpression(ATTR_INT));

		// Add a compound attribute
		Attribute compound = createAttribute(ATTR_COMPOUND, COMPOUND);
		Attribute child = createAttribute(ATTR_COMPOUND_CHILD_INT, INT).setParent(compound);

		return entityType.addAttribute(compound).addAttribute(child);
	}

	private Attribute createAttribute(String name, AttributeType dataType)
	{
		return attributeFactory.create().setName(name).setDataType(dataType);
	}

	public List<Entity> createTestRefEntities(EntityType refEntityType, int numberOfEntities)
	{
		return IntStream.range(0, numberOfEntities).mapToObj(i -> createRefEntity(refEntityType, i)).collect(toList());
	}

	public Stream<Entity> createTestEntities(EntityType entityType, int numberOfEntities, List<Entity> refEntities)
	{
		return IntStream.range(0, numberOfEntities)
						.mapToObj(i -> createEntity(entityType, i, refEntities.get(i % refEntities.size())));
	}

	public Stream<Entity> createSelfRefEntitiesWithEmptyReferences(EntityType entityType, int numberOfEntities)
	{
		return IntStream.range(0, numberOfEntities).mapToObj(i -> createEntity(entityType, i, null));
	}

	private Entity createRefEntity(EntityType refEntityType, int id)
	{
		Entity refEntity = new DynamicEntity(refEntityType);
		refEntity.set(ATTR_REF_ID, "" + id);
		refEntity.set(ATTR_REF_STRING, "refstring" + id);
		return refEntity;
	}

	public Entity createEntity(EntityType entityType, int id, Entity refEntity)
	{
		Entity entity = new DynamicEntity(entityType);
		entity.set(ATTR_ID, "" + id);
		entity.set(ATTR_STRING, "string1");
		entity.set(ATTR_BOOL, id % 2 == 0);
		entity.set(ATTR_CATEGORICAL, refEntity);
		entity.set(ATTR_CATEGORICAL_MREF, refEntity == null ? emptyList() : singletonList(refEntity));
		entity.set(ATTR_DATE, date);
		entity.set(ATTR_DATETIME, dateTime);
		entity.set(ATTR_EMAIL, "this.is@mail.address");
		entity.set(ATTR_DECIMAL, id + 0.123);
		entity.set(ATTR_HTML, id % 2 == 1 ? "<html>where is my head and where is my body</html>" : null);
		entity.set(ATTR_HYPERLINK, "http://www.molgenis.org");
		entity.set(ATTR_LONG, id * 1000000L);
		entity.set(ATTR_INT, 10 + id);
		entity.set(ATTR_SCRIPT, "/bin/blaat/script.sh");
		entity.set(ATTR_XREF, refEntity);
		entity.set(ATTR_MREF, refEntity == null ? emptyList() : singletonList(refEntity));
		entity.set(ATTR_COMPOUND_CHILD_INT, 10 + id);
		entity.set(ATTR_ENUM, id % 2 == 0 ? "option1" : "option2");

		return new EntityWithComputedAttributes(entity);
	}
}
