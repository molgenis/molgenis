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

import org.molgenis.util.SimpleTree;

import java.util.*;

/**
 * Definition of the base-class for objects in the user interface schema. This class inherits from the tree, so it can
 * hold multiple children and have convenient search-methods. Objects that need to be placed in this container need to
 * inherit from it.
 *
 * @author RA Scheltema
 * @version 1.0.0
 */
public class UISchema extends SimpleTree<UISchema>
{
	// member variables
	/**
	 * Used for serialization purposes
	 */
	private static final long serialVersionUID = 1816308705758091632L;

	private String label;

	private String namespace;

	private String group;

	private String groupRead;

	// constructor(s)

	/**
	 * The standard constructor, which links the object in the tree (with the parent parameter).
	 *
	 * @param name   The name of the element.
	 * @param parent The parent which will be used to link this object in the tree.
	 */
	public UISchema(String name, UISchema parent)
	{
		super(name, parent);
	}

	public String getPathName()
	{
		String path = getPackageName() + "/";
		return path.replace('.', '/');
	}

	public String getPackageName()
	{
		return "";
	}

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

	public String getCanonicalClassName()
	{
		return this.getClassName().substring(0, 1).toUpperCase();
	}

	public String getVelocityName()
	{
		return getName().replace('.', '_');
	}

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
		Set<String> uniqueGroups = new LinkedHashSet<String>();
		for (UISchema schema : getAllChildren())
		{
			String groupStr = schema.getGroup();
			if (groupStr != null)
			{
				for (String group : groupStr.split(","))
				{
					group = group.trim();
					if (!group.isEmpty()) uniqueGroups.add(group);
				}
			}
			String groupReadStr = schema.getGroupRead();
			if (groupReadStr != null)
			{
				for (String group : groupReadStr.split(","))
				{
					group = group.trim();
					if (!group.isEmpty()) uniqueGroups.add(group);
				}
			}
		}

		return new ArrayList<String>(uniqueGroups);
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

	enum Type
	{
		UNKNOWN, FORM, TREE, MENU, PLUGIN
	}

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
}
