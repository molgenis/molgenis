/**
 * File: invengine_generate/meta/Entity.java <br>
 * Copyright: Inventory 2000-2006, GBIC 2005, all rights reserved <br>
 * Changelog:
 * <ul>
 * <li>2005-12-06; 1.0.0; RA Scheltema; Creation.
 * <li>2006-01-11; 1.0.0; RA Scheltema; Added documentation.
 * <li>2006-01-16; 1.0.0; RA Scheltema; Added a system-identifier indicating
 * whether the entity is a system-specific table or user-defined.
 * <li>2006-01-25; 1.0.0; RA Scheltema Added the indices.
 * </ul>
 */

package org.molgenis.model.elements;

// imports
import java.util.Vector;

import org.molgenis.model.MolgenisModelException;

/**
 * Describes an exposable method in the molgenis framework. A method can for
 * example be exposed through a SOAP interface or in the user interface.
 */
public class Method extends MethodSchema
{
	// constructor(s)
	public Method(String name, MethodSchema parent)
	{
		super(name, parent);
	}

	// access
	public void setReturnType(Entity returntype) throws Exception
	{
		this.returntype = returntype;
	}

	public Entity getReturnType()
	{
		return returntype;
	}

	public void addParameter(Parameter parameter) throws MolgenisModelException
	{
		if (parameters.contains(parameter))
		{
			throw new MolgenisModelException("Parameter with name " + parameter.getName() + " already in method.");
		}

		parameters.add(parameter);
	}

	public Vector<Parameter> getParameters()
	{
		return parameters;
	}

	public void setQuery(MethodQuery query)
	{
		this.query = query;
	}

	public MethodQuery getQuery()
	{
		return this.query;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	@Override
	public String toString()
	{
		StringBuilder strBuilder = new StringBuilder();

		if (description != null && !description.equals("")) strBuilder.append(description).append('\n');
		if (returntype != null) strBuilder.append(returntype.getName()).append(' ');
		strBuilder.append("Method(").append(getName()).append(")\n(\n");
		for (Parameter parameter : parameters)
			strBuilder.append("  ").append(parameter.toString()).append('\n');
		strBuilder.append(");");
		if (query != null)
		{
			strBuilder.append("\n-> ").append(query.getEntity()).append(":\n");
			for (MethodQuery.Rule rule : query.getRules())
				strBuilder.append("   ").append(rule.getField()).append(' ').append(rule.getOperator()).append(' ').append(rule.getParameter()).append('\n');
		}
		return strBuilder.toString();
	}

	// data
	private String description = "";
	private Entity returntype = null;
	private Vector<Parameter> parameters = new Vector<Parameter>();

	private MethodQuery query = null;

	private static final long serialVersionUID = 2296459638604325393L;
}
