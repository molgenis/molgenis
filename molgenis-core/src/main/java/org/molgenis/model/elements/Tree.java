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

// jdk

/**
 *
 */
public class Tree extends UISchema
{
	/**
	 *
	 */
	public enum ViewType
	{
		VIEWTYPE_LIST("list"), VIEWTYPE_RECORD("record"), VIEWTYPE_UNKNOWN("unknown");

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

			if (str.equals(ViewType.VIEWTYPE_LIST.val)) return ViewType.VIEWTYPE_LIST;
			else if (str.equals(ViewType.VIEWTYPE_RECORD.val)) return ViewType.VIEWTYPE_RECORD;
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

	// constructor(s)

	/**
	 *
	 */
	public Tree(String name, UISchema parent, String parentField, String idField, String labelField)
	{
		super(name, parent);

		this.parentField = parentField;
		this.idField = idField;
		this.labelField = labelField;
		this.entity = null;
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

	public String getIdField()
	{
		return idField;
	}

	public void setIdField(String idField)
	{
		this.idField = idField;
	}

	public String getLabelField()
	{
		return labelField;
	}

	public void setLabelField(String labelField)
	{
		this.labelField = labelField;
	}

	public String getParentField()
	{
		return parentField;
	}

	public void setParentField(String parentField)
	{
		this.parentField = parentField;
	}

	//

	/**
	 *
	 */
	@Override
	public Type getType()
	{
		return Type.TREE;
	}

	// member variables
	/** */
	private Record record;
	/** */
	private Entity entity;
	/** */
	private boolean readonly;

	private String parentField;

	private String idField;

	private String labelField;

	/** */
	private static final long serialVersionUID = -2642011592737487306L;

}
