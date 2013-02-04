package org.molgenis.framework.tupletable.view.JQGridJSObjects;

public class JQGridSettings
{
	private boolean del;
	private boolean add;
	private boolean edit;
	private boolean search;

	public JQGridSettings()
	{
		this(false, false, false, false);
	}

	public JQGridSettings(boolean del, boolean add, boolean edit, boolean search)
	{
		this.del = del;
		this.add = add;
		this.edit = edit;
		this.search = search;
	}

	public boolean isDel()
	{
		return del;
	}

	public void setDel(boolean del)
	{
		this.del = del;
	}

	public boolean isAdd()
	{
		return add;
	}

	public void setAdd(boolean add)
	{
		this.add = add;
	}

	public boolean isEdit()
	{
		return edit;
	}

	public void setEdit(boolean edit)
	{
		this.edit = edit;
	}

	public boolean isSearch()
	{
		return search;
	}

	public void setSearch(boolean search)
	{
		this.search = search;
	}
}