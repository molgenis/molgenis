package org.molgenis.framework.tupletable.view.JQGridJSObjects;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.util.tuple.Tuple;

import com.google.gson.Gson;

/**
 * This is the request that we receive from JQGrid via JSON. Also, this can be
 * used to preload a 'saved' filters/sort/paging setting.
 */
public class JQGridPostData
{
	private JQGridFilter filters;
	/** sort order */
	private String sord;
	/** sort index field name */
	private String sidx;
	/** number of rows to show */
	private int rows;
	/** page offset, each page being rows long */
	private int page;
	private int colPage;

	public JQGridPostData()
	{
		this.sord = null;
		this.sidx = null;
		this.rows = 0;
		this.page = 0;
		this.colPage = 1;
		this.filters = new JQGridFilter();
	}

	public JQGridPostData(Tuple request)
	{
		this.rows = request.getInt("rows");
		this.page = request.getInt("page");
		this.sidx = request.getString("sidx");
		this.sord = request.getString("sord");
		this.colPage = request.getInt("colPage");

		JQGridFilter gridFilter = new Gson().fromJson(request.getString("filters"), JQGridFilter.class);

		if (gridFilter == null)
		{
			// Check simple, single search
			String searchString = request.getString("searchString");
			String searchField = request.getString("searchField");
			String searchOper = request.getString("searchOper");

			if (StringUtils.isNotBlank(searchString) && StringUtils.isNotBlank(searchField)
					&& StringUtils.isNotBlank(searchOper))
			{
				JQGridRule.JQGridOp op = JQGridRule.JQGridOp.valueOf(searchOper);
				gridFilter = new JQGridFilter();
				gridFilter.addRule(new JQGridRule(searchField, op, searchString));
			}
		}
		this.filters = gridFilter;
	}

	public JQGridFilter getFilters()
	{
		return filters;
	}

	public void setFilters(JQGridFilter filters)
	{
		this.filters = filters;
	}

	public String getSord()
	{
		return sord;
	}

	public void setSord(String sord)
	{
		this.sord = sord;
	}

	public String getSidx()
	{
		return sidx;
	}

	public void setSidx(String sidx)
	{
		this.sidx = sidx;
	}

	public int getRows()
	{
		return rows;
	}

	public void setRows(int rows)
	{
		this.rows = rows;
	}

	public int getPage()
	{
		return page;
	}

	public void setPage(int page)
	{
		this.page = page;
	}

	public int getColPage()
	{
		return colPage;
	}

	public void setColPage(int colPage)
	{
		this.colPage = colPage;
	}
}
