package org.molgenis.data.meta;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.AttributeMetadata;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.populate.IdGenerator;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.*;
import static org.testng.Assert.assertEquals;

public class SystemEntityTypeTest
{
	AttributeFactory attributeFactory;
	EntityTypeMetadata entityTypeMetaData;

	@BeforeMethod
	public void beforeMethod()
	{
		attributeFactory = mock(AttributeFactory.class);
		entityTypeMetaData = mock(EntityTypeMetadata.class);

		when(attributeFactory.getAttributeMetadata()).thenReturn(mock(AttributeMetadata.class));
	}

	@Test
	public void testAssignRolesToAttributeParts()
	{
		TestCompoundEMD testEMD = new TestCompoundEMD("Test");
		testEMD.setAttributeFactory(attributeFactory);
		testEMD.bootstrap(entityTypeMetaData);

		assertEquals(testEMD.getIdAttribute().getName(), "idAttr");
		assertEquals(testEMD.getLabelAttribute().getName(), "labelAttr");

		Set<String> lookupAttributes = newHashSet();
		testEMD.getLookupAttributes().forEach(e -> lookupAttributes.add(e.getName()));
		assertEquals(lookupAttributes, newHashSet("lookupAttr1", "lookupAttr2"));
	}

	@Test
	public void testAssignRolesToAttributePartsNested()
	{
		TestNestedCompoundEMD testEMD = new TestNestedCompoundEMD("Test");
		testEMD.setAttributeFactory(attributeFactory);
		testEMD.bootstrap(entityTypeMetaData);

		assertEquals(testEMD.getIdAttribute().getName(), "idAttr");
		assertEquals(testEMD.getLabelAttribute().getName(), "labelAttr");

		Set<String> lookupAttributes = newHashSet();
		testEMD.getLookupAttributes().forEach(e -> lookupAttributes.add(e.getName()));
		assertEquals(lookupAttributes, newHashSet("lookupAttr1", "lookupAttr2"));
	}

	private class TestNestedCompoundEMD extends SystemEntityType
	{

		TestNestedCompoundEMD(String entityName)
		{
			super(entityName);
			setIdGenerator(mock(IdGenerator.class));
		}

		@Override
		protected void init()
		{
			Attribute compoundAttr1 = mock(Attribute.class);
			addAttribute(compoundAttr1);
			addAttribute("compoundAttr2", compoundAttr1);
			addAttribute("idAttr", compoundAttr1, ROLE_ID);
			addAttribute("labelAttr", compoundAttr1, ROLE_LABEL);
			addAttribute("lookupAttr1", compoundAttr1, ROLE_LOOKUP);
			addAttribute("lookupAttr2", compoundAttr1, ROLE_LOOKUP);
		}
	}

	private class TestCompoundEMD extends SystemEntityType
	{

		TestCompoundEMD(String entityName)
		{
			super(entityName);
			setIdGenerator(mock(IdGenerator.class));
		}

		@Override
		protected void init()
		{
			Attribute compoundAttr = mock(Attribute.class);
			addAttribute(compoundAttr);
			addAttribute("idAttr", compoundAttr, ROLE_ID);
			addAttribute("labelAttr", compoundAttr, ROLE_LABEL);
			addAttribute("lookupAttr1", compoundAttr, ROLE_LOOKUP);
			addAttribute("lookupAttr2", compoundAttr, ROLE_LOOKUP);
		}
	}
}