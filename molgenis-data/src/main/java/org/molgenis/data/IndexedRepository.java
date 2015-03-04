package org.molgenis.data;

public interface IndexedRepository extends Manageable
{
	/**
	 * Rebuild current index
	 */
	public void rebuildIndex();
}
