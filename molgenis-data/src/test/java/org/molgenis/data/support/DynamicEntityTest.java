package org.molgenis.data.support;

import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Iterator;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.*;

public class DynamicEntityTest
{
	@DataProvider(name = "setNoExceptionProvider")
	public static Iterator<Object[]> setNoExceptionProvider()
	{
		return newArrayList(new Object[] { ONE_TO_MANY, mock(Iterable.class) },
				new Object[] { XREF, mock(Entity.class) }).iterator();
	}

	@Test(dataProvider = "setNoExceptionProvider")
	public void setNoException(AttributeType attrType, Object value)
	{
		set(attrType, value); // test if no exception occurs
	}

	@DataProvider(name = "setExceptionProvider")
	public static Iterator<Object[]> setExceptionProvider()
	{
		return newArrayList(new Object[] { ONE_TO_MANY, mock(Entity.class) },
				new Object[] { XREF, mock(Iterable.class) }, new Object[] { DECIMAL, Double.NaN }).iterator();
	}

	@Test(dataProvider = "setExceptionProvider", expectedExceptions = MolgenisDataException.class)
	public void setException(AttributeType attrType, Object value) throws Exception
	{
		set(attrType, value);
	}

	private static void set(AttributeType attrType, Object value)
	{
		EntityType entityType = mock(EntityType.class);
		Attribute attr = mock(Attribute.class);
		String attrName = "attr";
		when(attr.getName()).thenReturn(attrName);
		when(attr.getDataType()).thenReturn(attrType);
		when(entityType.getAttribute(attrName)).thenReturn(attr);
		DynamicEntity dynamicEntity = new DynamicEntity(entityType);
		dynamicEntity.set(attrName, value);
	}
}