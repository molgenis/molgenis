package org.molgenis.framework.tupletable.view.JQGridJSObjects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Class wrapping the results of a jqGrid query. To be serialized by Gson, hence
 * no accessors necessary for private datamembers.
 */
public class JQGridResult
{
	private final int page;
	private final int total;
	private final int records;
	private final List<LinkedHashMap<String, String>> rows;

	public JQGridResult(int page, int total, int records)
	{
		this.page = page;
		this.total = total;
		this.records = records;
		this.rows = new ArrayList<LinkedHashMap<String, String>>();
	}

	public int getPage()
	{
		return page;
	}

	public int getTotal()
	{
		return total;
	}

	public int getRecords()
	{
		return records;
	}

	public List<LinkedHashMap<String, String>> getRows()
	{
		return Collections.unmodifiableList(rows);
	}

	public boolean addRow(LinkedHashMap<String, String> row)
	{
		return rows.add(row);
	}
}