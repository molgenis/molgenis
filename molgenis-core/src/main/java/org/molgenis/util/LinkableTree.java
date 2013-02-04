/**
 * File: invengine.tdg.TDG <br>
 * Copyright: Inventory 2000-2006, GBIC 2005, all rights reserved <br>
 * Changelog:
 * <ul>
 * <li>2005-05-01; 1.0.0; MA Swertz; Creation.
 * <li>2005-12-01; 1.0.0; RA Scheltema; Did some updating to the new
 * style-guide, however there still remain quite a few warnings to solve.
 * </ul>
 */

package org.molgenis.util;

/**
 * Implementation of a simple tree
 */
public class LinkableTree<T extends Tree<T>> extends SimpleTree<T>
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -351346449721432255L;

	/**
	 * Construct a new Tree
	 * 
	 * @param name
	 *            unique name
	 * @param parent
	 *            the parent of this Tree. If null, then this is the root.
	 */
	public LinkableTree(String name, T parent, String url)
	{
		super(name, parent);
		System.out.println("name:" + name + "parent" + parent);
		this.setName(name, url);
	}

}
