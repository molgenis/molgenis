package org.molgenis.integrationtest.data;

import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.util.Arrays;
import java.util.Iterator;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EditableEntityMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Package;
import org.molgenis.data.meta.PackageImpl;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.QueryImpl;

import com.google.common.collect.Iterables;

public abstract class AbstractMetaDataIT extends AbstractDataIntegrationIT
{
	private static final String ENTITY_FULL_NAME = "test_test1_TestEntity";

	public void testIt()
	{
		SecuritySupport.login();

		// Create test_test1 package

		PackageImpl testPackage = new PackageImpl("test");
		metaDataService.addPackage(testPackage);
		Package retrievedPackage = metaDataService.getPackage("test");
		assertNotNull(retrievedPackage);
		assertNotNull(retrievedPackage.getRootPackage());
		assertEquals(retrievedPackage.getRootPackage().getName(), "test");

		Package testPackage1 = new PackageImpl("test1", "description", testPackage);
		metaDataService.addPackage(testPackage1);
		retrievedPackage = metaDataService.getPackage("test_test1");
		assertNotNull(retrievedPackage);
		assertEquals(retrievedPackage.getDescription(), "description");
		assertNotNull(retrievedPackage.getParent());
		assertEquals(retrievedPackage.getParent().getName(), "test");
		// assertNotNull(retrieved.getRootPackage());
		// assertEquals(retrieved.getRootPackage().getName(), "test");

		// Create EntityMetaData
		EditableEntityMetaData entityMetaData = new DefaultEntityMetaData("TestEntity", testPackage1);
		entityMetaData.addAttribute("identifier", ROLE_ID).setNillable(false);
		DefaultAttributeMetaData compound1 = entityMetaData.addAttribute("compoundAttr1")
				.setDataType(MolgenisFieldTypes.COMPOUND);
		DefaultAttributeMetaData compound2 = new DefaultAttributeMetaData("compoundAttr2",
				MolgenisFieldTypes.FieldTypeEnum.COMPOUND);
		DefaultAttributeMetaData intAttr = new DefaultAttributeMetaData("intAttr",
				MolgenisFieldTypes.FieldTypeEnum.INT);
		entityMetaData.setLabelAttribute(intAttr);
		compound2.setAttributesMetaData(Arrays.asList(intAttr));
		compound1.setAttributesMetaData(Arrays.asList(compound2));
		metaDataService.addEntityMeta(entityMetaData);

		EntityMetaData retrievedEntityMetaData = metaDataService.getEntityMetaData(ENTITY_FULL_NAME);
		assertNotNull(retrievedEntityMetaData);
		assertEquals(retrievedEntityMetaData.getName(), ENTITY_FULL_NAME);
		assertNotNull(retrievedEntityMetaData.getIdAttribute());
		assertEquals(retrievedEntityMetaData.getIdAttribute().getName(), "identifier");
		assertNotNull(retrievedEntityMetaData.getLabelAttribute());
		assertEquals(retrievedEntityMetaData.getLabelAttribute().getName(), "intAttr");

		assertNotNull(retrievedEntityMetaData.getAttributes());
		assertEquals(Iterables.size(retrievedEntityMetaData.getAttributes()), 2);
		Iterator<AttributeMetaData> attrs = retrievedEntityMetaData.getAttributes().iterator();
		assertEquals(attrs.next().getName(), "identifier");
		AttributeMetaData comp1 = attrs.next();
		assertEquals(comp1.getName(), "compoundAttr1");
		assertNotNull(comp1.getAttributeParts());
		assertEquals(Iterables.size(comp1.getAttributeParts()), 1);
		AttributeMetaData comp2 = comp1.getAttributePart("compoundAttr2");
		assertNotNull(comp2);
		assertNotNull(comp2.getAttributeParts());
		assertEquals(Iterables.size(comp2.getAttributeParts()), 1);
		assertNotNull(comp2.getAttributePart("intAttr"));

		assertNotNull(entityMetaData.getAtomicAttributes());
		assertEquals(Iterables.size(entityMetaData.getAtomicAttributes()), 2);
		attrs = entityMetaData.getAtomicAttributes().iterator();
		assertEquals(attrs.next().getName(), "identifier");
		assertEquals(attrs.next().getName(), "intAttr");

		// Add attribute
		metaDataService.addAttribute(ENTITY_FULL_NAME, new DefaultAttributeMetaData("strAttr"));
		retrievedEntityMetaData = metaDataService.getEntityMetaData(ENTITY_FULL_NAME);
		assertNotNull(retrievedEntityMetaData.getAttribute("strAttr"));
		assertEquals(Iterables.size(retrievedEntityMetaData.getAtomicAttributes()), 3);

		// Add attribute test default values
		DefaultEntity testEntity = new DefaultEntity(retrievedEntityMetaData, dataService);
		testEntity.set("identifier", "0");
		testEntity.set("intAttr", 0);
		testEntity.set("strAttr", "test");
		dataService.add(ENTITY_FULL_NAME, testEntity);

		DefaultAttributeMetaData attrStrDefault = new DefaultAttributeMetaData("strAttr_default");
		attrStrDefault.setDefaultValue("DEFAULT VALUE");
		metaDataService.addAttribute(ENTITY_FULL_NAME, attrStrDefault);
		retrievedEntityMetaData = metaDataService.getEntityMetaData(ENTITY_FULL_NAME);
		assertEquals(retrievedEntityMetaData.getAttribute("strAttr_default").getDefaultValue(), "DEFAULT VALUE");
		assertEquals(Iterables.size(retrievedEntityMetaData.getAtomicAttributes()), 4);

		Entity retreivedTestEntity = dataService.findOne(ENTITY_FULL_NAME, new QueryImpl());
		assertEquals(retreivedTestEntity.get("strAttr_default"), "DEFAULT VALUE");

		// TODO, fails -> fix deleteAttribute
		// Delete attribute
		// metaDataService.deleteAttribute("test_test1_TestEntity", "strAttr");
		// retrievedEntityMetaData = metaDataService.getEntityMetaData("test_test1_TestEntity");
		// assertNull(retrievedEntityMetaData.getAttribute("strAttr"));
		// assertEquals(Iterables.size(retrievedEntityMetaData.getAtomicAttributes()), 2);

		// Delete EntityMetaData
		metaDataService.deleteEntityMeta("test_test1_TestEntity");
		assertNull(metaDataService.getEntityMetaData("test_test1_TestEntity"));
	}
}
