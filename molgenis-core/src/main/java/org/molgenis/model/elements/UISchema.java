/**
 * File: invengine_generate/meta/UISchema.java <br>
 * Copyright: Inventory 2000-2006, GBIC 2005, all rights reserved <br>
 * Changelog:
 * <ul>
 * <li> 2005-12-15; 1.0.0; RA Scheltema; Creation.
 * </ul>
 */

package org.molgenis.model.elements;

// jdk
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.molgenis.util.SimpleTree;

/**
 * Definition of the base-class for objects in the user interface schema. This
 * class inherits from the tree, so it can hold multiple children and have
 * convenient search-methods. Objects that need to be placed in this container
 * need to inherit from it.
 * 
 * @author RA Scheltema
 * @version 1.0.0
 */
public class UISchema extends SimpleTree<UISchema>
{
	// member variables
	/** Used for serialization purposes */
	private static final long serialVersionUID = 1816308705758091632L;

	private String label;

	private String namespace;

	private String group;

	private String groupRead;

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
	public UISchema(String name, UISchema parent)
	{
		// super(buildName(name, parent), parent);
		super(name, parent);
	}

	// naming methods
	/**
	 * 
	 */
	public String getPathName()
	{
		String path = getPackageName() + "/";
		return path.replace('.', '/');
	}

	/**
	 * 
	 */
	public String getPackageName()
	{
		return "";
		// String name = getName();
		//
		// // hack to get rid of the tree-root
		// int start = name.indexOf('.');
		// int finish = name.lastIndexOf('.');
		//
		// if (start != -1 && finish != -1)
		// {
		// if (start == finish)
		// return "";// name.substring(start+1);
		// else
		// return name.substring(start + 1, finish);
		// }
		// else if (start != -1)
		// {
		// return name.substring(start + 1);
		// }
		// else
		// {
		// return name.substring(0, finish);
		// }
	}

	/**
	 * 
	 */
	public String getClassName()
	{
		String name = getName();

		int index = name.lastIndexOf('.');
		if (index == -1)
		{
			return name;
		}
		else
		{
			return name.substring(name.lastIndexOf('.') + 1);
		}
	}

	/**
	 * 
	 */
	public String getCanonicalClassName()
	{
		return this.getClassName().substring(0, 1).toUpperCase();
		// + this.getClassName().substring(1);
		// String class_name = this.getClassName().substring(0,1).toUpperCase()
		// + this.getClassName().substring(1);
		// String package_name = this.getPackageName();
		//
		// if (package_name.equals(""))
		// {
		// return class_name;
		// }
		// else
		// {
		// return package_name + "." + class_name;
		// }
	}

	/**
	 * 
	 */
	public String getVelocityName()
	{
		return getName().replace('.', '_');
	}

	// access methods
	/**
	 * 
	 */
	public List<UISchema> getCompleteSchema()
	{
		Vector<UISchema> results = new Vector<UISchema>();
		Vector<UISchema> children = getChildren();

		for (UISchema child : children)
		{
			results.add(child);
			results.addAll(child.getCompleteSchema());
		}

		return results;
	}

	/**
	 * 
	 */
	public List<Menu> getMenus()
	{
		Vector<Menu> menus = new Vector<Menu>();

		for (UISchema element : getChildren())
		{
			if (element.getClass().equals(Menu.class))
			{
				menus.add((Menu) element);
			}
		}
		return menus;
	}

	public ArrayList<String> getAllUniqueGroups()
	{
		ArrayList<String> res = new ArrayList<String>();

		// first add all unique read/write groups
		// FIXME: are these hardcoded excludes OK ?
		for (UISchema schema : getAllChildren())
		{
			if (schema.getGroup() != null && !res.contains(schema.getGroup()) && !schema.getGroup().equals("admin")
					&& !schema.getGroup().equals("anonymous") && !schema.getGroup().equals("AllUsers")
					&& !schema.getGroup().equals("system"))
			{
				res.add(schema.getGroup());
			}
		}

		// now add all unique read groups that were NOT part of the regular
		// read/write groups
		// FIXME: are these hardcoded excludes OK ?
		for (UISchema schema : getAllChildren())
		{
			if (schema.getGroupRead() != null && !res.contains(schema.getGroupRead())
					&& !schema.getGroupRead().equals("admin") && !schema.getGroupRead().equals("anonymous")
					&& !schema.getGroupRead().equals("AllUsers") && !schema.getGroupRead().equals("system"))
			{
				res.add(schema.getGroupRead());
			}
		}

		return res;
	}

	public List<Form> getAllForms()
	{
		List<Form> forms = new ArrayList<Form>();

		for (UISchema element : getAllChildren())
		{
			if (element.getClass().equals(Form.class))
			{
				forms.add((Form) element);
			}
		}

		return forms;
	}

	/**
	 * 
	 */
	public List<Form> getForms()
	{
		Vector<Form> forms = new Vector<Form>();

		for (UISchema element : getChildren())
		{
			if (element.getClass().equals(Form.class))
			{
				forms.add((Form) element);
			}
		}
		return forms;
	}

	/**
	 * 
	 */
	public Vector<Tree> getTrees()
	{
		Vector<Tree> trees = new Vector<Tree>();

		for (UISchema child : getChildren())
		{
			if (child.getClass().equals(Tree.class))
			{
				trees.add((Tree) child);
			}
		}

		return trees;
	}

	/**
	 * 
	 */
	public Vector<Plugin> getPlugins()
	{
		Vector<Plugin> plugins = new Vector<Plugin>();

		for (UISchema child : getChildren())
		{
			if (child.getClass().equals(Plugin.class))
			{
				plugins.add(((Plugin) child));
			}
		}

		return plugins;
	}

	//
	/** */
	enum Type
	{
		UNKNOWN, FORM, TREE, MENU, PLUGIN
	};

	/**
	 * 
	 */
	public Type getType()
	{
		return Type.UNKNOWN;
	}

	public String getLabel()
	{
		return label;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}

	// private methode

	public String getNamespace()
	{
		return namespace;
	}

	public void setNamespace(String namespace)
	{
		this.namespace = namespace;
	}

	public String getGroup()
	{
		return group;
	}

	public void setGroup(String group)
	{
		this.group = group;
	}

	public String getGroupRead()
	{
		return groupRead;
	}

	public void setGroupRead(String groupRead)
	{
		this.groupRead = groupRead;
	}

	// public String getNearestParentRole(){
	// TODO role inheritance?
	// }

}
