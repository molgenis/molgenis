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
public class Plugin extends UISchema
{
	public enum Flavor
	{
		FREEMARKER("freemarker"), EASY("easy"), UNKNOWN("unknown");

		private String tag;

		Flavor(String tag)
		{
			this.tag = tag;
		}

		@Override
		public String toString()
		{
			return this.tag;
		}

		public static Flavor getPluginMethod(String method) throws MolgenisModelException
		{
			StringBuilder optionsBuilder = new StringBuilder();
			for (Flavor p : Flavor.values())
			{
				if (p.toString().equalsIgnoreCase(method)) return p;
				optionsBuilder.append(p.toString()).append(", ");
			}
			throw new MolgenisModelException(
					"method='" + method + "' is UNKNOWN for plugin. Valid options: " + optionsBuilder.toString());
		}
	}

	// constructor(s)

	/**
	 *
	 */
	public Plugin(String name, UISchema parent, String pluginType)
	{
		super(name, parent);
		this.entity = null;
		this.pluginType = pluginType;
	}

	/**
	 *
	 */
	@Override
	public Type getType()
	{
		return Type.PLUGIN;
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

	@Override
	public String toString()
	{
		if (getRecord() != null)
		{
			return String.format("Plugin(name=%s, entity=%s, group=%s)", getName(), getRecord().getName(), getGroup());
		}
		else
		{
			return String.format("Plugin(name=%s, group=%s)", getName(), getGroup());
		}
	}

	/**
	 *
	 */
	public boolean getReadOnly()
	{
		return this.readonly;
	}

	public String getPluginType()
	{
		return pluginType;
	}

	public void setPluginType(String pluginType)
	{
		this.pluginType = pluginType;
	}

	public Flavor getFlavor()
	{
		return flavor;
	}

	public void setPluginMethod(Flavor pluginMethod)
	{
		this.flavor = pluginMethod;
	}

	/** */
	private Record record;
	/** */
	private Entity entity;
	/** */
	private String pluginType;
	/** */
	private boolean readonly;
	/** */
	private Flavor flavor = Flavor.FREEMARKER;

	/** */
	private static final long serialVersionUID = -2642011592737487306L;
}
