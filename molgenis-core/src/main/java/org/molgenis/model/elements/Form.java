/**
 * File: invengine_generate/meta/Form.java <br>
 * Copyright: Inventory 2000-2006, GBIC 2005, all rights reserved <br>
 * Changelog:
 * <ul>
 * <li>2005-12-06; 1.0.0; RA Scheltema Creation.
 * </ul>
 */

package org.molgenis.model.elements;

import org.molgenis.model.MolgenisModelException;

import java.util.ArrayList;
import java.util.List;

// jdk

/**
 *
 */
public class Form extends UISchema
{
	// constructor(s)

	/**
	 *
	 */
	public Form(String name, UISchema parent)
	{
		super(name, parent);
		this.limit = 0;
		this.entity = null;
		this.viewtype = ViewType.LIST_VIEW;
	}

	/**
	 *
	 */
	@Override
	public Type getType()
	{
		return Type.FORM;
	}

	// access methods

	/**
	 *
	 */
	public void setEntity(Entity entity)
	{
		this.entity = entity;
	}

	/**
	 *
	 */
	public Entity getEntity()
	{
		if (this.entity == null)
		{
			return (Entity) this.getRecord();
		}
		return this.entity;
	}

	/**
	 *
	 */
	public void setRecord(Record record)
	{
		this.record = record;
	}

	/**
	 * Getter/setter for optional custom header for the selected form screen
	 */
	public String getHeader()
	{
		return this.header;
	}

	public void setHeader(String header)
	{
		this.header = header;
	}

	/**
	 * Getter/setter for optional description for the selected form screen
	 */
	public String getDescription()
	{
		return this.description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	/**
	 *
	 */
	public Record getRecord()
	{
		return this.record;
	}

	/**
	 *
	 */
	public void setReadOnly(final boolean readonly)
	{
		this.readonly = readonly;
	}

	/**
	 *
	 */
	public boolean getReadOnly()
	{
		return this.readonly;
	}

	/**
	 *
	 */
	public void setLimit(final int limit)
	{
		this.limit = limit;
	}

	/**
	 *
	 */
	public int getLimit()
	{
		return this.limit;
	}

	/**
	 *
	 */
	public void setViewType(final ViewType viewtype)
	{
		this.viewtype = viewtype;
	}

	/**
	 *
	 */
	public ViewType getViewType()
	{
		return this.viewtype;
	}

	/**
	 *
	 */
	public enum ViewType
	{
		LIST_VIEW("list"), EDIT_VIEW("edit"), VIEWTYPE_UNKNOWN("unknown");

		//

		/**
		 *
		 */
		public String getValue()
		{
			return val;
		}

		/**
		 *
		 */
		public static ViewType parseViewType(String str) throws MolgenisModelException
		{
			if (str == null) throw new MolgenisModelException("view-type cannot be null");

			if (str.equals(ViewType.LIST_VIEW.val)) return ViewType.LIST_VIEW;
			else if (str.equals(ViewType.EDIT_VIEW.val)) return ViewType.EDIT_VIEW;
			else return ViewType.VIEWTYPE_UNKNOWN;
		}

		/** */
		ViewType(String val)
		{
			this.val = val;
		}

		/** */
		private String val;
	}

	// member variables
	/** */
	private int limit;

	/** */
	private List<String> commandPlugins;

	/** */
	private Record record;
	/** */
	private Entity entity;
	/** */
	private ViewType viewtype;
	/** */
	private List<String> hiddenFields;
	/** */
	private boolean readonly;

	/**
	 * Optional custom header for the selected form screen
	 */
	private String header;

	/**
	 * Optional description of this form, explaining it in the context of your
	 * application
	 */
	private String description;

	private boolean filter;
	private String filterfield;
	private String filtertype;
	private String filtervalue;

	private boolean embedparent;
	private String embedkeyfield = "";
	private String embedchildname;

	private boolean embedchild;
	private String embedparentname;
	private String embedparentfield;
	private String embedchildfield;
	private String embedfields;

	private String sortby;
	private SortOrder sortorder = SortOrder.ASC;

	public enum SortOrder
	{
		ASC, DESC;

		public static SortOrder parse(String str) throws MolgenisModelException
		{
			if (str == null) throw new MolgenisModelException("SortOrder cannot be null");
			if (str.equalsIgnoreCase(SortOrder.ASC.toString())) return SortOrder.ASC;
			if (str.equalsIgnoreCase(SortOrder.DESC.toString())) return SortOrder.DESC;
			throw new MolgenisModelException("SortOrder can only be 'asc' or 'desc'");
		}
	}

	/**
	 * Option to have a compact view only showing the assigned fields.. The
	 * other fields will be 'collapsed' with a '+' to uncollapse them.
	 */
	private List<String> compactView = new ArrayList<String>();

	/** */
	private static final long serialVersionUID = -2642011592737487306L;

	public boolean isFilter()
	{
		return filter;
	}

	public void setFilter(boolean filter)
	{
		this.filter = filter;
	}

	public String getFilterfield()
	{
		return filterfield;
	}

	public void setFilterfield(String filterfield)
	{
		this.filterfield = filterfield;
	}

	public String getFiltertype()
	{
		return filtertype;
	}

	public void setFiltertype(String filtertype)
	{
		this.filtertype = filtertype;
	}

	public String getFiltervalue()
	{
		return filtervalue;
	}

	public void setFiltervalue(String filtervalue)
	{
		this.filtervalue = filtervalue;
	}

	public boolean isEmbedchild()
	{
		return embedchild;
	}

	public void setEmbedchild(boolean embedchild)
	{
		this.embedchild = embedchild;
	}

	public String getEmbedfields()
	{
		return embedfields;
	}

	public void setEmbedfields(String embedfields)
	{
		this.embedfields = embedfields;
	}

	public String getEmbedkeyfield()
	{
		return embedkeyfield;
	}

	public void setEmbedkeyfield(String embedkeyfield)
	{
		this.embedkeyfield = embedkeyfield;
	}

	public String getEmbedchildname()
	{
		return embedchildname;
	}

	public void setEmbedchildname(String embedchildname)
	{
		this.embedchildname = embedchildname;
	}

	public boolean isEmbedparent()
	{
		return embedparent;
	}

	public void setEmbedparent(boolean embedparent)
	{
		this.embedparent = embedparent;
	}

	public String getEmbedchildfield()
	{
		return embedchildfield;
	}

	public void setEmbedchildfield(String embedchildfield)
	{
		this.embedchildfield = embedchildfield;
	}

	public String getEmbedparentfield()
	{
		return embedparentfield;
	}

	public void setEmbedparentfield(String embedparentfield)
	{
		this.embedparentfield = embedparentfield;
	}

	public String getEmbedparentname()
	{
		return embedparentname;
	}

	public void setEmbedparentname(String embedparentname)
	{
		this.embedparentname = embedparentname;
	}

	public List<String> getCommands()
	{
		return commandPlugins;
	}

	public void setCommands(List<String> actionPlugins)
	{
		this.commandPlugins = actionPlugins;
	}

	public List<String> getCompactView()
	{
		return compactView;
	}

	public void setCompactView(List<String> compactFields)
	{
		this.compactView = compactFields;
	}

	@Override
	public String toString()
	{
		StringBuilder commandsBuilder = new StringBuilder();
		for (String command : getCommands())
			commandsBuilder.append(command).append(',');
		String sortby = "";
		if (this.sortby != null) sortby = "sortby=" + this.sortby;
		return String.format("Form(name=%s, entity=%s, group=%s, groupRead=%s, commands=%s,%s)", getName(),
				getRecord().getName(), getGroup(), getGroupRead(), commandsBuilder.toString(), sortby);
	}

	public String getSortby()
	{
		return sortby;
	}

	public void setSortby(String sortby)
	{
		this.sortby = sortby;
	}

	public SortOrder getSortorder()
	{
		return sortorder;
	}

	public void setSortorder(SortOrder sortorder)
	{
		this.sortorder = sortorder;
	}

	public synchronized List<String> getHideFields()
	{
		return hiddenFields;
	}

	public synchronized void setHideFields(List<String> hiddenFields)
	{
		this.hiddenFields = hiddenFields;
	}

}