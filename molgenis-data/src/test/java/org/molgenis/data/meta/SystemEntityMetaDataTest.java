package org.molgenis.data.meta;

import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.AttributeMetaDataMetaData;
import org.molgenis.data.meta.model.EntityMetaDataMetaData;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.*;
import static org.testng.Assert.assertEquals;

public class SystemEntityMetaDataTest
{
	AttributeMetaDataFactory metaDataFactory;
	EntityMetaDataMetaData metaDataMetaData;

	@BeforeMethod
	public void beforeMethod()
	{
		metaDataFactory = mock(AttributeMetaDataFactory.class);
		metaDataMetaData = mock(EntityMetaDataMetaData.class);

		when(metaDataFactory.getAttributeMetaDataMetaData()).thenReturn(mock(AttributeMetaDataMetaData.class));
	}

	@Test
	public void testAssignRolesToAttributeParts()
	{
		TestCompoundEMD testEMD = new TestCompoundEMD("Test");
		testEMD.setAttributeMetaDataFactory(metaDataFactory);
		testEMD.bootstrap(metaDataMetaData);

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
		testEMD.setAttributeMetaDataFactory(metaDataFactory);
		testEMD.bootstrap(metaDataMetaData);

		assertEquals(testEMD.getIdAttribute().getName(), "idAttr");
		assertEquals(testEMD.getLabelAttribute().getName(), "labelAttr");

		Set<String> lookupAttributes = newHashSet();
		testEMD.getLookupAttributes().forEach(e -> lookupAttributes.add(e.getName()));
		assertEquals(lookupAttributes, newHashSet("lookupAttr1", "lookupAttr2"));
	}

	private class TestNestedCompoundEMD extends SystemEntityMetaData
	{

		TestNestedCompoundEMD(String entityName)
		{
			super(entityName);
		}

		@Override
		protected void init()
		{
			AttributeMetaData compoundAttr1 = mock(AttributeMetaData.class);
			addAttribute(compoundAttr1);
			addAttribute("compoundAttr2", compoundAttr1);
			addAttribute("idAttr", compoundAttr1, ROLE_ID);
			addAttribute("labelAttr", compoundAttr1, ROLE_LABEL);
			addAttribute("lookupAttr1", compoundAttr1, ROLE_LOOKUP);
			addAttribute("lookupAttr2", compoundAttr1, ROLE_LOOKUP);
		}
	}

	private class TestCompoundEMD extends SystemEntityMetaData
	{

		TestCompoundEMD(String entityName)
		{
			super(entityName);
		}

		@Override
		protected void init()
		{
			AttributeMetaData compoundAttr = mock(AttributeMetaData.class);
			addAttribute(compoundAttr);
			addAttribute("idAttr", compoundAttr, ROLE_ID);
			addAttribute("labelAttr", compoundAttr, ROLE_LABEL);
			addAttribute("lookupAttr1", compoundAttr, ROLE_LOOKUP);
			addAttribute("lookupAttr2", compoundAttr, ROLE_LOOKUP);
		}
	}
}