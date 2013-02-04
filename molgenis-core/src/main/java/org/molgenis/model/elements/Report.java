// File:		Report.java
// Copyright:	Inventory 2000-2006, GBIC 2005-2006, all rights reserved <br>

package org.molgenis.model.elements;

// jdk
import java.util.Vector;

// invengine

/**
 * <report name="week"> <entity name="project" fields="[name]" /> <entity
 * name="task" fields="[name, user, start, project]" />
 * 
 * <constraint type="hidden" entity="task" field="type" value="qtl" />
 * <constraint type="equals" entity="task" field="user" /> <constraint
 * type="range" entity="task" field="start" /> </report>
 */
public class Report
{
	class Entity
	{
		String name;
		Vector<String> fields;
	}

	class Constraint
	{
	}

	// constructor(s)
	/**
	 * 
	 */
	public Report(String name)
	{
		this.name = name;
	}

	// data
	/** */
	String name;
}
