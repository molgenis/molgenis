package org.molgenis.data.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.molgenis.data.Entity;
import org.molgenis.data.file.processor.CellProcessor;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.DynamicEntity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Entity implementation for Excel.
 * <p>
 * All attributes are of type String, values are processed using the given CellProcessors
 */
public class ExcelEntity extends DynamicEntity
{
	private static final long serialVersionUID = 8928375571009145452L;
	private final transient Row row;
	private final Map<String, Integer> colNamesMap;
	private final List<CellProcessor> cellProcessors;

	private transient Map<String, Object> cachedValueMap;

	public ExcelEntity(Row row, Map<String, Integer> colNamesMap, List<CellProcessor> cellProcessors,
			EntityType entityType)
	{
		super(entityType);

		if (row == null) throw new IllegalArgumentException("row is null");
		if (colNamesMap == null) throw new IllegalArgumentException("column names map is null");

		this.row = row;
		this.colNamesMap = colNamesMap;
		this.cellProcessors = cellProcessors;
	}

	/**
	 * Gets an Attribute (Cell value).
	 * <p>
	 * All values are retrieved as String, returns null if the attributeName is unknown
	 */
	@Override
	public Object get(String attributeName)
	{
		if (cachedValueMap == null)
		{
			cachedValueMap = new LinkedHashMap<>();
		}

		Object value;
		if (cachedValueMap.containsKey(attributeName))
		{
			value = cachedValueMap.get(attributeName);
		}
		else
		{
			Integer col = colNamesMap.get(attributeName);
			if (col != null)
			{
				Cell cell = row.getCell(col);
				if (cell != null)
				{
					value = ExcelUtils.toValue(cell, cellProcessors);
				}
				else
				{
					value = null;
				}
			}
			else
			{
				value = null;
			}

			cachedValueMap.put(attributeName, value);
		}
		return value;
	}

	@Override
	public void set(String attributeName, Object value)
	{
		if (cachedValueMap == null)
		{
			cachedValueMap = new LinkedHashMap<>();
		}
		cachedValueMap.put(attributeName, value);
	}

	@Override
	public void set(Entity values)
	{
		colNamesMap.keySet().forEach(attr -> set(attr, values.get(attr)));
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		sb.append("[");
		boolean first = true;
		for (String attr : colNamesMap.keySet())
		{
			if (!first)
			{
				sb.append(",");
			}
			sb.append(attr).append("=").append(get(attr));
			first = false;
		}
		sb.append("]");

		return sb.toString();
	}

	@Override
	public Iterable<String> getAttributeNames()
	{
		return colNamesMap.keySet();
	}
}
