package org.molgenis.fieldtypes;

import com.google.common.collect.Lists;
import org.molgenis.model.MolgenisModelException;
import org.molgenis.model.elements.Field;

import java.text.ParseException;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.molgenis.MolgenisFieldTypes.AttributeType;
import static org.molgenis.MolgenisFieldTypes.AttributeType.ONE_TO_MANY;

/**
 * Field type that models the relationship between two entities A and B in which an element of A may be linked
 * to many elements of B, but a member of B is linked to only one element of A.
 */
public class OneToManyField extends FieldType
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
	public String getPostgreSqlType()
	{
		throw new UnsupportedOperationException(
				format("No PostgreSQL data type exists for [%s] field type", getEnumType().toString()));
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
	public AttributeType getEnumType()
	{
		return ONE_TO_MANY;
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