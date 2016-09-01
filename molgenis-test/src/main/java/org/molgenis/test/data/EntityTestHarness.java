package org.molgenis.test.data;

import org.molgenis.MolgenisFieldTypes.AttributeType;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.*;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.support.EntityWithComputedAttributes;
import org.molgenis.test.data.staticentity.TestEntityStaticMetaData;
import org.molgenis.test.data.staticentity.TestRefEntityStaticMetaData;
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

	@Autowired
	private PackageFactory packageFactory;

	@Autowired
	private EntityMetaDataFactory entityMetaDataFactory;

	@Autowired
	private AttributeMetaDataFactory attributeMetaDataFactory;

	@Autowired
	TestEntityStaticMetaData staticTestEntityStaticMetaData;

	@Autowired
	TestRefEntityStaticMetaData staticTestRefEntityStaticMetaData;

	@Autowired
	private TestPackage testPackage;

	private Date date;
	private Date dateTime;

	@PostConstruct
	public void postConstruct()
	{
	}

	public EntityMetaData createStaticRefTestEntityMetaData()
	{
		return staticTestRefEntityStaticMetaData;
	}

	public EntityMetaData createStaticTestEntityMetaData()
	{
		return staticTestEntityStaticMetaData;
	}

	public EntityMetaData createDynamicRefEntityMetaData()
	{
		return entityMetaDataFactory.create().setPackage(testPackage).setSimpleName("TypeTestRefDynamic").setBackend("PostgreSQL")
				.addAttribute(createAttribute(ATTR_REF_ID, STRING), ROLE_ID)
				.addAttribute(createAttribute(ATTR_REF_STRING, STRING), ROLE_LABEL);
	}

	public EntityMetaData createDynamicTestEntityMetaData()
	{
		EntityMetaData refEntityMetaData = createDynamicRefEntityMetaData();
		return entityMetaDataFactory.create().setPackage(testPackage).setSimpleName("TypeTestDynamic").setBackend("PostgreSQL")
				.addAttribute(createAttribute(ATTR_ID, STRING).setAuto(true), ROLE_ID)
				.addAttribute(createAttribute(ATTR_STRING, STRING), ROLE_LABEL)
				.addAttribute(createAttribute(ATTR_BOOL, BOOL))
				.addAttribute(createAttribute(ATTR_CATEGORICAL, CATEGORICAL).setRefEntity(refEntityMetaData))
				.addAttribute(createAttribute(ATTR_CATEGORICAL_MREF, CATEGORICAL_MREF).setRefEntity(refEntityMetaData))
				.addAttribute(createAttribute(ATTR_DATE, DATE)).addAttribute(createAttribute(ATTR_DATETIME, DATE_TIME))
				.addAttribute(createAttribute(ATTR_EMAIL, EMAIL)).addAttribute(createAttribute(ATTR_DECIMAL, DECIMAL))
				.addAttribute(createAttribute(ATTR_HTML, HTML)).addAttribute(createAttribute(ATTR_HYPERLINK, HYPERLINK))
				.addAttribute(createAttribute(ATTR_LONG, LONG)).addAttribute(createAttribute(ATTR_INT, INT))
				.addAttribute(createAttribute(ATTR_SCRIPT, SCRIPT))
				.addAttribute(createAttribute(ATTR_XREF, XREF).setRefEntity(refEntityMetaData))
				.addAttribute(createAttribute(ATTR_MREF, MREF).setRefEntity(refEntityMetaData))
				.addAttribute(createAttribute(ATTR_COMPUTED_INT, INT).setExpression(ATTR_INT));
	}

	private AttributeMetaData createAttribute(String name, AttributeType dataType)
	{
		return attributeMetaDataFactory.create().setName(name).setDataType(dataType);
	}

	public List<Entity> createTestRefEntities(EntityMetaData refEntityMetaData, int numberOfEntities)
	{
		return IntStream.range(0, numberOfEntities).mapToObj(i -> createRefEntity(refEntityMetaData, i))
				.collect(toList());
	}

	public Stream<Entity> createTestEntities(EntityMetaData entityMetaData, int numberOfEntities,
			List<Entity> refEntities)
	{
		return IntStream.range(0, numberOfEntities)
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
		if (date == null || dateTime == null) generateDateAndDateTime();

		Entity entity = new DynamicEntity(entityMetaData);
		entity.set(ATTR_ID, "" + id);
		entity.set(ATTR_STRING, "string1");
		entity.set(ATTR_BOOL, id % 2 == 0);
		entity.set(ATTR_CATEGORICAL, refEntity);
		entity.set(ATTR_CATEGORICAL_MREF, Collections.singletonList(refEntity));
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
		entity.set(ATTR_MREF, Collections.singletonList(refEntity));

		return new EntityWithComputedAttributes(entity);
	}

	private void generateDateAndDateTime()
	{
		DateFormat dateFormat = MolgenisDateFormat.getDateFormat();
		DateFormat dateTimeFormat = MolgenisDateFormat.getDateTimeFormat();
		try
		{
			date = dateFormat.parse("2012-12-21");
			dateTime = dateTimeFormat.parse("1985-08-12T11:12:13+0500");
		}
		catch (ParseException e)
		{
			e.printStackTrace();
		}
	}
}
