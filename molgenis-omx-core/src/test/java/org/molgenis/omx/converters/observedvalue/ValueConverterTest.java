package org.molgenis.omx.converters.observedvalue;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;

import org.molgenis.mock.MockDatabase;
import org.molgenis.omx.observ.Category;
import org.molgenis.omx.observ.ObservableFeature;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ValueConverterTest
{
	private MockDatabase mockDatabase;
	private ObservableFeature feature;

	@BeforeMethod
	public void beforeMethod()
	{
		mockDatabase = new MockDatabase();
		feature = new ObservableFeature();
	}

	@Test
	public void testConvertString()
	{
		feature.setDataType("string");
		assertEquals(ValueConverter.fromString("test", mockDatabase, feature), "test");
	}

	@Test
	public void testConvertCategorical()
	{
		feature.setDataType("categorical");
		Category cat = new Category();
		cat.setName("cat");
		mockDatabase.setEntities(Arrays.asList(cat));
		assertEquals(ValueConverter.fromString("test", mockDatabase, feature), "cat");
	}

	@Test
	public void testConvertInt()
	{
		feature.setDataType("int");
		assertEquals(ValueConverter.fromString("1", mockDatabase, feature), 1);
	}

	@Test
	public void testConvertDecimal()
	{
		feature.setDataType("decimal");
		assertEquals(ValueConverter.fromString("1.3", mockDatabase, feature), 1.3);
	}

	@Test
	public void testConvertBool()
	{
		feature.setDataType("bool");
		assertEquals(ValueConverter.fromString("true", mockDatabase, feature), true);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testConvertUnknown()
	{
		feature.setDataType("bogusl");
		ValueConverter.fromString("1.3", mockDatabase, feature);
	}
}
