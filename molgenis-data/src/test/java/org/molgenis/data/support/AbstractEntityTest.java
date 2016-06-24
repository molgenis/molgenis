package org.molgenis.data.support;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.COMPOUND;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;
import static org.testng.Assert.assertEquals;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.UnknownAttributeException;
import org.testng.annotations.Test;

public class AbstractEntityTest
{
	// Regression test for #3301
	@Test
	public void getLabelValue()
	{
		String labelAttrName = "label";
		DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData("entity");
		entityMetaData.addAttribute(labelAttrName, ROLE_ID).setDataType(MolgenisFieldTypes.SCRIPT);
		AbstractEntity entity = mock(AbstractEntity.class);
		when(entity.getLabelValue()).thenCallRealMethod();
		when(entity.getEntityMetaData()).thenReturn(entityMetaData);
		when(entity.get(labelAttrName)).thenReturn("label value");
		assertEquals(entity.getLabelValue(), "label value");
	}

	// Regression test for #3536
	@Test
	public void toString_()
	{
		final DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData("entity");
		entityMetaData.addAttribute("attr");
		DefaultAttributeMetaData compoundAttr = entityMetaData.addAttribute("compound").setDataType(COMPOUND);
		DefaultAttributeMetaData compoundPart1Attr = new DefaultAttributeMetaData("part1");
		DefaultAttributeMetaData compoundPart2Attr = new DefaultAttributeMetaData("part2");
		compoundAttr.addAttributePart(compoundPart1Attr);
		compoundAttr.addAttributePart(compoundPart2Attr);

		// mock(AbstractEntity.class) replaces toString method
		AbstractEntity entity = new AbstractEntity()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public EntityMetaData getEntityMetaData()
			{
				return entityMetaData;
			}

			@Override
			public Iterable<String> getAttributeNames()
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public Object getIdValue()
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public Object get(String attributeName)
			{
				switch (attributeName)
				{
					case "attr":
						return "attr_value";
					case "part1":
						return "part1_value";
					case "part2":
						return "part2_value";
					default:
						throw new UnknownAttributeException();
				}
			}

			@Override
			public void set(String attributeName, Object value)
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public void set(Entity values)
			{
				throw new UnsupportedOperationException();
			}
		};
		assertEquals(entity.toString(), "entity=[attr=attr_value,compound={part1=part1_value,part2=part2_value}]");
	}
}
