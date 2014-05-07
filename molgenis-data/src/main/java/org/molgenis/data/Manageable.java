package org.molgenis.data;

//TODO: discuss name of this interface
public interface Manageable extends Repository
{
	/**
	 * Create a new repository backend (e.g., create a table in a database; add a sheet to Excel)
	 */
	public void create();

	/**
	 * Drop a repository backend (e.g. drop a table in a database; remove a sheet from Excel)
	 */
	public void drop();
}
