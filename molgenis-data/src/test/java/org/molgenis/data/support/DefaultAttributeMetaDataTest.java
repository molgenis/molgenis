package org.molgenis.data.support;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.AGGREGATEABLE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.AUTO;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.DATA_TYPE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.DEFAULT_VALUE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.DESCRIPTION;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.ENUM_OPTIONS;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.EXPRESSION;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.ID_ATTRIBUTE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.LABEL;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.LABEL_ATTRIBUTE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.LOOKUP_ATTRIBUTE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.NILLABLE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.RANGE_MAX;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.RANGE_MIN;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.READ_ONLY;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.REF_ENTITY;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.UNIQUE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.VALIDATION_EXPRESSION;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.VISIBLE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.VISIBLE_EXPRESSION;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.Arrays;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeChangeListener;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Range;
import org.molgenis.fieldtypes.FieldType;
import org.testng.annotations.Test;

public class DefaultAttributeMetaDataTest
{
	private FieldType STRING;

	@Test
	public void DefaultAttributeMetaDataAttributeMetaData()
	{
		DefaultAttributeMetaData attr = new DefaultAttributeMetaData("attribute");
		attr.setAggregateable(true);
		attr.setAuto(true);
		attr.setDataType(MolgenisFieldTypes.INT);
		attr.setDefaultValue(null);
		attr.setDescription("description");
		attr.setIdAttribute(true);
		attr.setLabel("label");
		attr.setLabelAttribute(true);
		attr.setLookupAttribute(true);
		attr.setNillable(true);
		attr.setRange(new Range(-1l, 1l));
		attr.setReadOnly(true);
		attr.setUnique(true);
		attr.setVisible(true);
		assertEquals(new DefaultAttributeMetaData(attr), attr);
	}

	@Test
	public void DefaultAttributeMetaDataUnknownLabelUseName()
	{
		assertEquals(new DefaultAttributeMetaData("attribute").getLabel(), "attribute");
	}

	@Test
	public void addChangeListener()
	{
		AttributeChangeListener changeListener = mock(AttributeChangeListener.class);
		DefaultAttributeMetaData attr = new DefaultAttributeMetaData("attribute");
		attr.addChangeListener(changeListener);

		attr.setAggregateable(true);
		verify(changeListener, times(1)).onChange(AGGREGATEABLE, attr);
		attr.setAuto(true);
		verify(changeListener, times(1)).onChange(AUTO, attr);
		attr.setDataType(STRING);
		verify(changeListener, times(1)).onChange(DATA_TYPE, attr);
		attr.setDefaultValue("default");
		verify(changeListener, times(1)).onChange(DEFAULT_VALUE, attr);
		attr.setDescription("description");
		verify(changeListener, times(1)).onChange(DESCRIPTION, attr);
		attr.setEnumOptions(Arrays.asList("enum0", "enum1"));
		verify(changeListener, times(1)).onChange(ENUM_OPTIONS, attr);
		attr.setExpression("expr");
		verify(changeListener, times(1)).onChange(EXPRESSION, attr);
		attr.setIdAttribute(true);
		verify(changeListener, times(1)).onChange(ID_ATTRIBUTE, attr);
		attr.setLabel("lbl");
		verify(changeListener, times(1)).onChange(LABEL, attr);
		attr.setLabelAttribute(true);
		verify(changeListener, times(1)).onChange(LABEL_ATTRIBUTE, attr);
		attr.setLookupAttribute(true);
		verify(changeListener, times(1)).onChange(LOOKUP_ATTRIBUTE, attr);
		attr.setNillable(true);
		verify(changeListener, times(1)).onChange(NILLABLE, attr);
		attr.setRange(mock(Range.class));
		verify(changeListener, times(1)).onChange(RANGE_MIN, attr);
		verify(changeListener, times(1)).onChange(RANGE_MAX, attr);
		attr.setReadOnly(true);
		verify(changeListener, times(1)).onChange(READ_ONLY, attr);
		attr.setRefEntity(mock(EntityMetaData.class));
		verify(changeListener, times(1)).onChange(REF_ENTITY, attr);
		attr.setUnique(true);
		verify(changeListener, times(1)).onChange(UNIQUE, attr);
		attr.setValidationExpression("expr");
		verify(changeListener, times(1)).onChange(VALIDATION_EXPRESSION, attr);
		attr.setVisible(true);
		verify(changeListener, times(1)).onChange(VISIBLE, attr);
		attr.setVisibleExpression("expr");
		verify(changeListener, times(1)).onChange(VISIBLE_EXPRESSION, attr);
	}

	@Test
	public void removeChangeListener()
	{
		AttributeChangeListener changeListener = when(mock(AttributeChangeListener.class).getId()).thenReturn("id0")
				.getMock();
		DefaultAttributeMetaData attr = new DefaultAttributeMetaData("attribute");
		attr.addChangeListener(changeListener);
		attr.setAggregateable(true);
		verify(changeListener, times(1)).onChange(AGGREGATEABLE, attr);
		attr.removeChangeListener(changeListener.getId());
		attr.setAuto(true);
		verify(changeListener, times(0)).onChange(AUTO, attr);
	}

	@Test
	public void getAttributePart()
	{
		DefaultAttributeMetaData attr = new DefaultAttributeMetaData("attribute");
		String attrName = "Attr";
		assertNull(attr.getAttributePart(attrName));
		AttributeMetaData attrPart = when(mock(AttributeMetaData.class).getName()).thenReturn(attrName).getMock();
		attr.addAttributePart(attrPart);
		assertEquals(attrPart, attr.getAttributePart(attrName));
		assertEquals(attrPart, attr.getAttributePart(attrName.toLowerCase())); // case insensitive
	}
}
