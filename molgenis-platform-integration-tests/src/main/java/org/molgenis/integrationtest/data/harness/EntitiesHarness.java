package org.molgenis.integrationtest.data.harness;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Creates {@link EntityMetaData} and {@link org.molgenis.data.Entity}s to test with.
 */
@Component
public class EntitiesHarness
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
	private DataService dataService;

	public EntityMetaData createRefEntityMetaData(String name, Package p)
	{
		//		EntityMetaData refEntityMetaData = new EntityMetaData(name, p);
		//		refEntityMetaData.addAttribute(ATTR_REF_ID, ROLE_ID).setNillable(false);
		//		refEntityMetaData.addAttribute(ATTR_REF_STRING).setNillable(true).setDataType(MolgenisFieldTypes.STRING);
		//		return refEntityMetaData;
		return null; // FIXME
	}

	public EntityMetaData createEntityMetaData(String name, Package p, EntityMetaData refEntityMetaData)
	{
		//		EntityMetaData entityMetaData = new EntityMetaData(name, p);
		//		entityMetaData.addAttribute(ATTR_ID, ROLE_ID).setNillable(false).setAuto(true);
		//		entityMetaData.addAttribute(ATTR_STRING).setNillable(true).setDataType(MolgenisFieldTypes.STRING);
		//		entityMetaData.addAttribute(ATTR_BOOL).setNillable(true).setDataType(MolgenisFieldTypes.BOOL);
		//		entityMetaData.addAttribute(ATTR_CATEGORICAL).setNillable(true).setDataType(MolgenisFieldTypes.CATEGORICAL)
		//				.setRefEntity(refEntityMetaData);
		//		entityMetaData.addAttribute(ATTR_CATEGORICAL_MREF).setNillable(true)
		//				.setDataType(MolgenisFieldTypes.CATEGORICAL_MREF).setRefEntity(refEntityMetaData);
		//		entityMetaData.addAttribute(ATTR_DATE).setNillable(true).setDataType(MolgenisFieldTypes.DATE);
		//		entityMetaData.addAttribute(ATTR_DATETIME).setNillable(true).setDataType(MolgenisFieldTypes.DATETIME);
		//		entityMetaData.addAttribute(ATTR_EMAIL).setNillable(true).setDataType(MolgenisFieldTypes.EMAIL);
		//		entityMetaData.addAttribute(ATTR_DECIMAL).setNillable(true).setDataType(MolgenisFieldTypes.DECIMAL);
		//		entityMetaData.addAttribute(ATTR_HTML).setNillable(true).setDataType(MolgenisFieldTypes.HTML);
		//		entityMetaData.addAttribute(ATTR_HYPERLINK).setNillable(true).setDataType(MolgenisFieldTypes.HYPERLINK);
		//		entityMetaData.addAttribute(ATTR_LONG).setNillable(true).setDataType(MolgenisFieldTypes.LONG);
		//		entityMetaData.addAttribute(ATTR_INT).setNillable(true).setDataType(MolgenisFieldTypes.INT);
		//		entityMetaData.addAttribute(ATTR_SCRIPT).setNillable(true).setDataType(MolgenisFieldTypes.SCRIPT);
		//		entityMetaData.addAttribute(ATTR_XREF).setNillable(true).setDataType(MolgenisFieldTypes.XREF)
		//				.setRefEntity(refEntityMetaData);
		//		entityMetaData.addAttribute(ATTR_MREF).setNillable(true).setDataType(MolgenisFieldTypes.MREF)
		//				.setRefEntity(refEntityMetaData);
		//
		//		return entityMetaData;
		return null; // FIXME
	}

	public List<Entity> createTestRefEntities(EntityMetaData refEntityMetaData, int numberOfEntities)
	{
		return IntStream.range(0, numberOfEntities).mapToObj(i -> createRefEntity(refEntityMetaData, i))
				.collect(Collectors.toList());
	}

	public List<Entity> createTestEntities(EntityMetaData entityMetaData, int numberOfEntities, int numberOfRefEntities)
	{
		return IntStream.range(0, numberOfEntities)
				.mapToObj(i -> createEntity(entityMetaData, i, i % numberOfRefEntities)).collect(Collectors.toList());
	}

	private Entity createRefEntity(EntityMetaData refEntityMetaData, int id)
	{
		//		DefaultEntity refEntity = new DefaultEntity(refEntityMetaData, dataService);
		//		refEntity.set(ATTR_REF_ID, "" + id);
		//		refEntity.set(ATTR_REF_STRING, "refstring" + id);
		//		return refEntity;
		return null;
	}

	private Entity createEntity(EntityMetaData entityMetaData, int id, int refId)
	{
		//		DefaultEntity entity1 = new DefaultEntity(entityMetaData, dataService);
		//		entity1.set(ATTR_ID, id);
		//		entity1.set(ATTR_STRING, "string1");
		//		entity1.set(ATTR_BOOL, true);
		//		entity1.set(ATTR_CATEGORICAL, "" + refId);
		//		entity1.set(ATTR_CATEGORICAL_MREF, "" + refId);
		//		entity1.set(ATTR_DATE, "21-12-2012");
		//		entity1.set(ATTR_DATETIME, "1985-08-12T11:12:13+0500");
		//		entity1.set(ATTR_EMAIL, "this.is@mail.address");
		//		entity1.set(ATTR_DECIMAL, 1.123);
		//		entity1.set(ATTR_HTML, "<html>where is my head and where is my body</html>");
		//		entity1.set(ATTR_HYPERLINK, "http://www.molgenis.org");
		//		entity1.set(ATTR_LONG, 1000000);
		//		entity1.set(ATTR_INT, 18);
		//		entity1.set(ATTR_SCRIPT, "/bin/blaat/script.sh");
		//		entity1.set(ATTR_XREF, refId);
		//		entity1.set(ATTR_MREF, Collections.singletonList(refId));
		//		return entity1;
		return null;
	}
}
