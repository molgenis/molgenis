package org.molgenis.util.tuple;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

import org.molgenis.util.ListEscapeUtils;

/**
 * Tuple that delegates all calls to Tuple.get
 */
@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "NP_BOOLEAN_RETURN_NULL", justification = "return true/false/null on purpose")
public abstract class AbstractTuple implements Tuple
{
	private static final long serialVersionUID = 1L;

	@Override
	public boolean hasColNames()
	{
		Iterable<String> it = getColNames();
		return it != null ? it.iterator().hasNext() : false;
	}

	@Override
	public boolean isNull(String colName)
	{
		return get(colName) == null;
	}

	@Override
	public boolean isNull(int col)
	{
		return get(col) == null;
	}

	@Override
	public String getString(String colName)
	{
		Object obj = get(colName);
		if (obj == null) return null;
		else if (obj instanceof String) return (String) obj;
		else
			return obj.toString();
	}

	@Override
	public String getString(int col)
	{
		Object obj = get(col);
		if (obj == null) return null;
		else if (obj instanceof String) return (String) obj;
		else
			return obj.toString();
	}

	@Override
	public Integer getInt(String colName)
	{
		Object obj = get(colName);
		if (obj == null) return null;
		else if (obj instanceof Integer) return (Integer) obj;
		else
			return Integer.parseInt(obj.toString());
	}

	@Override
	public Integer getInt(int col)
	{
		Object obj = get(col);
		if (obj == null) return null;
		else if (obj instanceof Integer) return (Integer) obj;
		else
			return Integer.parseInt(obj.toString());
	}

	@Override
	public Long getLong(String colName)
	{
		Object obj = get(colName);
		if (obj == null) return null;
		else if (obj instanceof Long) return (Long) obj;
		else
			return Long.parseLong(obj.toString());
	}

	@Override
	public Long getLong(int col)
	{
		Object obj = get(col);
		if (obj == null) return null;
		else if (obj instanceof Long) return (Long) obj;
		else
			return Long.parseLong(obj.toString());
	}

	@Override
	public Boolean getBoolean(String colName)
	{
		Object obj = get(colName);
		if (obj == null) return null;
		else if (obj instanceof Boolean) return (Boolean) obj;
		else
		{
			String str = obj.toString();
			return str.equalsIgnoreCase("true") || str.equalsIgnoreCase("1");
		}
	}

	@Override
	public Boolean getBoolean(int col)
	{
		Object obj = get(col);
		if (obj == null) return null;
		else if (obj instanceof Boolean) return (Boolean) obj;
		else
		{
			String str = obj.toString();
			return str.equalsIgnoreCase("true") || str.equalsIgnoreCase("1");
		}
	}

	@Override
	public Double getDouble(String colName)
	{
		Object obj = get(colName);
		if (obj == null) return null;
		else if (obj instanceof Double) return (Double) obj; // FIXME
		else
			return Double.parseDouble(obj.toString());
	}

	@Override
	public Double getDouble(int col)
	{
		Object obj = get(col);
		if (obj == null) return null;
		else if (obj instanceof Double) return (Double) obj;
		else
			return Double.parseDouble(obj.toString());
	}

	@Override
	public Date getDate(String colName)
	{
		Object obj = get(colName);
		if (obj == null) return null;
		else if (obj instanceof Date) return (Date) obj;
		else
			return Date.valueOf(obj.toString());
	}

	@Override
	public Date getDate(int col)
	{
		Object obj = get(col);
		if (obj == null) return null;
		else if (obj instanceof Date) return (Date) obj;
		else
			return Date.valueOf(obj.toString());
	}

	@Override
	public Timestamp getTimestamp(String colName)
	{
		Object obj = get(colName);
		if (obj == null) return null;
		else if (obj instanceof Timestamp) return (Timestamp) obj;
		else
			return Timestamp.valueOf(obj.toString());
	}

	@Override
	public Timestamp getTimestamp(int col)
	{
		Object obj = get(col);
		if (obj == null) return null;
		else if (obj instanceof Timestamp) return (Timestamp) obj;
		else
			return Timestamp.valueOf(obj.toString());
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getList(String colName)
	{

		Object obj = get(colName);
		if (obj == null) return null;
		else if (obj instanceof List<?>) return (List<String>) obj;
		else if (obj instanceof String) return ListEscapeUtils.toList((String) obj);
		else
			return ListEscapeUtils.toList(obj.toString());
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getList(int col)
	{
		Object obj = get(col);
		if (obj == null) return null;
		else if (obj instanceof List<?>) return (List<String>) obj;
		else if (obj instanceof String) return ListEscapeUtils.toList((String) obj);
		else
			return ListEscapeUtils.toList(obj.toString());
	}

	@Override
	public String toString()
	{
		StringBuilder strBuilder = new StringBuilder();
		for (String colName : getColNames())
			strBuilder.append(colName).append('=').append(get(colName)).append(',');
		if (strBuilder.length() > 0) strBuilder.deleteCharAt(strBuilder.length() - 1);

		return "Tuple[" + strBuilder.toString() + ']';
	}
}
