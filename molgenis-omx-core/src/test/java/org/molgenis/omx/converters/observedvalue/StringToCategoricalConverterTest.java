package org.molgenis.omx.converters.observedvalue;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

import java.util.Arrays;
import java.util.Collections;

import org.molgenis.mock.MockDatabase;
import org.molgenis.omx.observ.Category;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.util.Entity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class StringToCategoricalConverterTest
{
	private MockDatabase mockDatabase;
	private ObservableFeature feature;
	private StringToCategoricalConverter converter;

	@BeforeMethod
	public void beforeMethod()
	{
		mockDatabase = new MockDatabase();
		feature = new ObservableFeature();
		converter = new StringToCategoricalConverter();
	}

	@Test
	public void fromString()
	{
		Category cat = new Category();
		cat.setName("cat");
		mockDatabase.setEntities(Arrays.asList(cat));
		String value = converter.fromString("0", mockDatabase, feature);
		assertEquals(value, cat.getName());
	}

	@Test
	public void fromStringNullValue()
	{
		assertNull(converter.fromString(null, mockDatabase, feature));
	}

	@Test
	public void fromStringWithInvalidValueCode()
	{
		mockDatabase.setEntities(Collections.<Entity> emptyList());
		assertEquals(converter.fromString("0", mockDatabase, feature), "0");
	}
}
