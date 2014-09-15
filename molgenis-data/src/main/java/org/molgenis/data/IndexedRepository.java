package org.molgenis.data;

public interface IndexedRepository extends Aggregateable
{
	/**
	 * Rebuild current index
	 */
	public void rebuildIndex();
}
