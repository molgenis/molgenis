package org.molgenis.integrationtest.data;

import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.util.Arrays;
import java.util.Iterator;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.meta.AttributeMetaData;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.data.meta.Package;

import com.google.common.collect.Iterables;

public abstract class AbstractMetaDataIT extends AbstractDataIntegrationIT
{
	public void testIt()
	{
		SecuritySupport.login();

		// Create test_test1 package

		Package testPackage = new Package("test");
		metaDataService.addPackage(testPackage);
		Package retrievedPackage = metaDataService.getPackage("test");
		assertNotNull(retrievedPackage);
		assertNotNull(retrievedPackage.getRootPackage());
		assertEquals(retrievedPackage.getRootPackage().getName(), "test");

		Package testPackage1 = new Package("test1", "description", testPackage);
		metaDataService.addPackage(testPackage1);
		retrievedPackage = metaDataService.getPackage("test_test1");
		assertNotNull(retrievedPackage);
		assertEquals(retrievedPackage.getDescription(), "description");
		assertNotNull(retrievedPackage.getParent());
		assertEquals(retrievedPackage.getParent().getName(), "test");
		// assertNotNull(retrieved.getRootPackage());
		// assertEquals(retrieved.getRootPackage().getName(), "test");

		// Create EntityMetaData
		EntityMetaData entityMetaData = new EntityMetaData("TestEntity", testPackage1);
		entityMetaData.addAttribute("identifier", ROLE_ID).setNillable(false);
		AttributeMetaData compound1 = entityMetaData.addAttribute("compoundAttr1").setDataType(
				MolgenisFieldTypes.COMPOUND);
		AttributeMetaData compound2 = new AttributeMetaData("compoundAttr2",
				MolgenisFieldTypes.FieldTypeEnum.COMPOUND);
		AttributeMetaData intAttr = new AttributeMetaData("intAttr", MolgenisFieldTypes.FieldTypeEnum.INT);
		entityMetaData.setLabelAttribute(intAttr);
		compound2.setAttributesMetaData(Arrays.asList(intAttr));
		compound1.setAttributesMetaData(Arrays.asList(compound2));
		metaDataService.addEntityMeta(entityMetaData);

		EntityMetaData retrievedEntityMetaData = metaDataService.getEntityMetaData("test_test1_TestEntity");
		assertNotNull(retrievedEntityMetaData);
		assertEquals(retrievedEntityMetaData.getName(), "test_test1_TestEntity");
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
		metaDataService.addAttribute("test_test1_TestEntity", new AttributeMetaData("strAttr"));
		retrievedEntityMetaData = metaDataService.getEntityMetaData("test_test1_TestEntity");
		assertNotNull(retrievedEntityMetaData.getAttribute("strAttr"));
		assertEquals(Iterables.size(retrievedEntityMetaData.getAtomicAttributes()), 3);

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
