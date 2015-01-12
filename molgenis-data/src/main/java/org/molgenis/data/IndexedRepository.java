package org.molgenis.data;

public interface IndexedRepository
{
	/**
	 * Rebuild current index
	 */
	public void rebuildIndex();

	public void drop();
}
