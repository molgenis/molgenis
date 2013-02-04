package org.molgenis.util;

import java.io.Serializable;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.molgenis.model.elements.Field;
import org.molgenis.util.tuple.EntityTuple;
import org.molgenis.util.tuple.SingletonTuple;
import org.molgenis.util.tuple.Tuple;

/**
 * Abstract Entity class that implements common parts for each Entity.
 */
@XmlRootElement(name = "entity")
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractEntity implements Entity, Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@XmlTransient
	private boolean readonly;

	public static boolean isObjectRepresentation(String objStr)
	{
		int left = objStr.indexOf('(');
		int right = objStr.lastIndexOf(')');
		return (left == -1 || right == -1) ? false : true;
	}

	public static <T extends Entity> T setValuesFromString(String objStr, Class<T> klass) throws Exception
	{
		T result = klass.newInstance();

		int left = objStr.indexOf('(');
		int right = objStr.lastIndexOf(')');

		String content = objStr.substring(left + 1, right);

		String[] attrValues = content.split(" ");
		for (String attrValue : attrValues)
		{
			String[] av = attrValue.split("=");
			String attr = av[0];
			String value = av[1];
			if (value.charAt(0) == '\'' && value.charAt(value.length() - 1) == '\'')
			{
				value = value.substring(1, value.length() - 1);
			}
			result.set(attr, value);
		}
		return result;
	}

	@Override
	public void set(String name, Object value) throws Exception
	{
		this.set(new SingletonTuple<Object>(name, value), false);
	}

	@Override
	public void set(Tuple values) throws Exception
	{
		this.set(values, true);
	}

	@Override
	public Tuple getValues()
	{
		return new EntityTuple(this);
	}

	@Override
	public String getValues(String sep)
	{
		StringWriter out = new StringWriter();
		for (String field : this.getFields())
		{
			{
				Object valueO = get(field);
				String valueS;
				if (valueO != null) valueS = valueO.toString();
				else
					valueS = "";
				valueS = valueS.replaceAll("\r\n", " ").replaceAll("\n", " ").replaceAll("\r", " ");
				valueS = valueS.replaceAll("\t", " ").replaceAll(sep, " ");
				out.write(valueS);
			}
		}
		return out.toString();
	}

	@Override
	public void setReadonly(boolean readonly)
	{
		this.readonly = readonly;
	}

	@Override
	public boolean isReadonly()
	{
		return readonly;
	}

	public static java.sql.Date string2date(String str) throws ParseException
	{
		String dateFormat = "MMMM d, yyyy";
		String dateFormat2 = "dd-MM-yyyy";
		try
		{
			DateFormat formatter = new SimpleDateFormat(dateFormat, Locale.US);
			return new java.sql.Date(formatter.parse(str).getTime());
		}
		catch (ParseException pe)
		{
			try
			{
				DateFormat formatter = new SimpleDateFormat(dateFormat2, Locale.US);
				return new java.sql.Date(formatter.parse(str).getTime());
			}
			catch (ParseException pe2)
			{
				throw new ParseException("parsing failed: expected date value formatted '" + dateFormat + " or "
						+ dateFormat2, 0);
			}
		}
	}

	/**
	 * Default implementation. Will be overriden if your entity model contains
	 * subclasses
	 */
	public String get__Type()
	{
		if (this.get(Field.TYPE_FIELD) != null) return this.get(Field.TYPE_FIELD).toString();
		return null;
	}

	/**
	 * Default implementation. Will be overriden if your entity model contains
	 * subclasses
	 */
	public String get__TypeLabel()
	{
		if (this.get(Field.TYPE_FIELD + "_Label") != null) return this.get(Field.TYPE_FIELD + "_Label").toString();
		return null;
	}

	/**
	 * Default implementation. Will be overriden if your entity model contains
	 * subclasses
	 */
	public List<ValueLabel> get__TypeOptions()
	{
		if (this.get(Field.TYPE_FIELD + "_options") != null) return (List<ValueLabel>) this.get(Field.TYPE_FIELD
				+ "_options");
		return null;
	}

	public void set__Type(String type)
	{
		// throwing would be better but requires more refactoring
		try
		{
			this.set(Field.TYPE_FIELD, type);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public String getLabelValue()
	{
		StringBuilder resultBuilder = new StringBuilder();
		for (String label : this.getLabelFields())
		{
			if (resultBuilder.length() > 0) resultBuilder.append(':');
			if (this.get(label) != null) resultBuilder.append(this.get(label));
		}

		return resultBuilder.toString();
	}

}
