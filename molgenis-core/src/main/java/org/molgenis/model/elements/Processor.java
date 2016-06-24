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

import java.util.Vector;

import org.molgenis.model.MolgenisModelException;

// jdk

/**
 * Describes a database-entity (or table).
 * 
 * @author MA Swertz
 * @version 1.0.0
 */
@Deprecated
public class Processor extends PRSchema
{
	// constructor(s)
	/**
	 */
	public Processor(String name, PRSchema parent)
	{
		super(name, parent);
	}

	public void addDataset(Dataset dataset) throws MolgenisModelException
	{
		if (datasets.contains(dataset))
		{
			throw new MolgenisModelException("Dataset with name " + dataset.getName() + " already in processor.");
		}

		datasets.add(dataset);
	}

	public Vector<Dataset> getDatasets()
	{
		return datasets;
	}

	@Override
	public String toString()
	{
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("Processor(").append(getName()).append(")\n(\n");
		for (Dataset dataset : datasets)
			strBuilder.append(' ').append(dataset.toString()).append('\n');
		strBuilder.append(");");
		return strBuilder.toString();
	}

	private Vector<Dataset> datasets = new Vector<Dataset>();

	private static final long serialVersionUID = 2296459638604325393L;
}
