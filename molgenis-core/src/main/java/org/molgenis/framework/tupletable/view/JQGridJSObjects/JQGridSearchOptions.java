package org.molgenis.framework.tupletable.view.JQGridJSObjects;

import java.util.Arrays;
import java.util.List;

public class JQGridSearchOptions
{
	private boolean multipleSearch;
	private boolean multipleGroup;
	private boolean showQuery;
	private int width = 650;// Width of the search dialog
	private boolean closeAfterSearch = true;
	private boolean closeAfterReset = true;
	private List<JQGridRule.JQGridOp> sopt;

	public JQGridSearchOptions()
	{
		this(true, true, true, Arrays.asList(JQGridRule.JQGridOp.values()));
	}

	public JQGridSearchOptions(boolean multipleSearch, boolean multipleGroup, boolean showQuery,
			List<JQGridRule.JQGridOp> sopt)
	{
		this.multipleSearch = multipleSearch;
		this.multipleGroup = multipleGroup;
		this.showQuery = showQuery;
		this.sopt = sopt;
	}

	public boolean isMultipleSearch()
	{
		return multipleSearch;
	}

	public void setMultipleSearch(boolean multipleSearch)
	{
		this.multipleSearch = multipleSearch;
	}

	public boolean isMultipleGroup()
	{
		return multipleGroup;
	}

	public void setMultipleGroup(boolean multipleGroup)
	{
		this.multipleGroup = multipleGroup;
	}

	public boolean isShowQuery()
	{
		return showQuery;
	}

	public void setShowQuery(boolean showQuery)
	{
		this.showQuery = showQuery;
	}

	public List<JQGridRule.JQGridOp> getSopt()
	{
		return sopt;
	}

	public void setSopt(List<JQGridRule.JQGridOp> sopt)
	{
		this.sopt = sopt;
	}

	public int getWidth()
	{
		return width;
	}

	public void setWidth(int width)
	{
		this.width = width;
	}

	public boolean isCloseAfterSearch()
	{
		return closeAfterSearch;
	}

	public void setCloseAfterSearch(boolean closeAfterSearch)
	{
		this.closeAfterSearch = closeAfterSearch;
	}

	public boolean isCloseAfterReset()
	{
		return closeAfterReset;
	}

	public void setCloseAfterReset(boolean closeAfterReset)
	{
		this.closeAfterReset = closeAfterReset;
	}

}
