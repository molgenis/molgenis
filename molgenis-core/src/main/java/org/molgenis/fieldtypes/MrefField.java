package org.molgenis.fieldtypes;

import static java.util.stream.Collectors.toList;

import java.text.ParseException;
import java.util.stream.Stream;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.model.MolgenisModelException;
import org.molgenis.model.elements.Field;

import com.google.common.collect.Lists;

/**
 * Many to many reference.
 * 
 * Example MOLGENIS DSL,
 * 
 * <pre>
 * <field name="myfield" type="mref" xref_entity="OtherEntity" xref_field="id" xref_label="name"/>
 * </pre>
 * 
 * This example would in the UI show a seletion box with 'name' elements.
 */
public class MrefField extends FieldType
{
	private static final long serialVersionUID = 1L;

	@Override
	public String getJavaAssignment(String value)
	{
		return "NOT IMPLEMENTED";
	}

	@Override
	public String getJavaPropertyType() throws MolgenisModelException
	{
		// Entity e_ref = f.getXrefEntity();
		Field f_ref = f.getXrefField();
		return "java.util.List<" + getFieldType(f_ref).getJavaPropertyType() + ">";
	}

	@Override
	public String getJavaPropertyDefault() throws MolgenisModelException
	{
		// Entity e_ref = f.getXrefEntity();
		Field f_ref = f.getXrefField();
		// if(f.getDefaultValue() == null || f.getDefaultValue() == "")
		// "new java.util.ArrayList<"+getFieldType(f_ref).getJavaPropertyType(f_ref)+">()";
		// FIXME can there be defaults here?
		return "new java.util.ArrayList<" + getFieldType(f_ref).getJavaPropertyType() + ">()";
	}

	@Override
	public String getJavaSetterType() throws MolgenisModelException
	{
		// Entity e_ref = f.getXrefEntity();
		Field f_ref = f.getXrefField();
		return "new java.util.ArrayList<" + getFieldType(f_ref).getJavaSetterType() + ">()";
	}

	@Override
	public String getMysqlType() throws MolgenisModelException
	{
		// FIXME this function should be never called???
		return getFieldType(f.getXrefField()).getMysqlType();
	}

	@Override
	public String getOracleType() throws MolgenisModelException
	{
		// FIXME this function should be never called???
		return getFieldType(f.getXrefField()).getOracleType();
	}

	@Override
	public String getHsqlType() throws MolgenisModelException
	{
		return getFieldType(f.getXrefField()).getHsqlType();
	}

	@Override
	public String getXsdType() throws MolgenisModelException
	{
		return getFieldType(f.getXrefField()).getXsdType();
	}

	@Override
	public String getFormatString()
	{
		return "";
	}

	@Override
	public String getCppPropertyType() throws MolgenisModelException
	{
		Field f_ref = f.getXrefField();
		return "vector<" + getFieldType(f_ref).getCppPropertyType() + ">";
	}

	@Override
	public String getCppJavaPropertyType() throws MolgenisModelException
	{
		return "Ljava/util/List;";
	}

	@Override
	public Class<?> getJavaType()
	{
		return java.util.List.class;
	}

	@Override
	public Object getTypedValue(String value) throws ParseException
	{
		throw new UnsupportedOperationException("Conversion of MRef not supported.");
	}

	@Override
	public FieldTypeEnum getEnumType()
	{
		return FieldTypeEnum.MREF;
	}

	@Override
	public Long getMaxLength()
	{
		return null;
	}

	@Override
	public Object convert(Object value)
	{
		if (value instanceof Iterable<?>)
		{
			value = Lists.newArrayList((Iterable<?>) value);
		}
		else if (value instanceof Stream<?>)
		{
			value = ((Stream<?>) value).collect(toList());
		}
		return value;
	}

}
