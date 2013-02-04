/**
 * File: invengine_generate/meta/DBSchema.java <br>
 * Copyright: Inventory 2000-2006, GBIC 2005, all rights reserved <br>
 * Changelog:
 * <ul>
 * <li>2005-12-15; 1.0.0; RA Scheltema; Creation.
 * </ul>
 */

package org.molgenis.model.elements;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.util.SimpleTree;

// jdk

// invengine

/**
 * Definition of the base-class for objects in the database schema. This class
 * inherits from the tree, so it can hold multiple children and have convenient
 * search-methods. Objects that need to be placed in this container need to
 * inherit from it.
 * 
 * @author RA Scheltema
 * @version 1.0.0
 */
public class DBSchema extends SimpleTree<DBSchema>
{
	// variables
	/** Used for serialization purposes */
	private static final long serialVersionUID = -6239251669294203517L;

	private List<Module> modules = new ArrayList<Module>();

	private Model model;

	public Model getModel()
	{
		return model;
	}

	public void setModel(Model model)
	{
		this.model = model;
	}

	// constructor(s)
	/**
	 * The standard constructor, which links the object in the tree (with the
	 * parent parameter).
	 * 
	 * @param name
	 *            The name of the element.
	 * @param parent
	 *            The parent which will be used to link this object in the tree.
	 */
	public DBSchema(String name, DBSchema parent, Model model)
	{
		super(name, parent);
		this.model = model;
	}

	public List<Module> getModules()
	{
		return modules;
	}

	public void setModules(List<Module> modules)
	{
		this.modules = modules;
	}

	public static long getSerialVersionUID()
	{
		return serialVersionUID;
	}
}
