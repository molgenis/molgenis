package org.molgenis.data.support;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Package;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

public class AbstractEntityMetaDataTest
{
	@Test
	public void getAtomicAttributeNames()
	{
		assertEquals(Lists.newArrayList(new EntityMetaDataImpl().getAtomicAttributeNames()),
				Arrays.asList("attr0", "attr1", "attr2"));
	}

	private static class EntityMetaDataImpl extends AbstractEntityMetaData
	{

		@Override
		public Package getPackage()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public String getSimpleName()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public String getBackend()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isAbstract()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public String getLabel()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public String getDescription()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public Iterable<AttributeMetaData> getAttributes()
		{
			String attrName0 = "attr0";
			AttributeMetaData attr0 = mock(AttributeMetaData.class);
			when(attr0.getName()).thenReturn(attrName0);
			when(attr0.getDataType()).thenReturn(MolgenisFieldTypes.STRING);

			String attrName1 = "attr1";
			AttributeMetaData attr1 = mock(AttributeMetaData.class);
			when(attr1.getName()).thenReturn(attrName1);
			when(attr1.getDataType()).thenReturn(MolgenisFieldTypes.STRING);

			String attrName2 = "attr2";
			AttributeMetaData attr2 = mock(AttributeMetaData.class);
			when(attr2.getName()).thenReturn(attrName2);
			when(attr2.getDataType()).thenReturn(MolgenisFieldTypes.STRING);

			AttributeMetaData attrCompound = mock(AttributeMetaData.class);
			when(attrCompound.getName()).thenReturn("attrCompound");
			when(attrCompound.getDataType()).thenReturn(MolgenisFieldTypes.COMPOUND);
			when(attrCompound.getAttributeParts()).thenReturn(Arrays.asList(attr1, attr2));

			return Arrays.asList(attr0, attrCompound);
		}

		@Override
		public EntityMetaData getExtends()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public Class<? extends Entity> getEntityClass()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean hasAttributeWithExpression()
		{
			throw new UnsupportedOperationException();
		}
	}
}
