package org.molgenis.data;

public interface IndexedRepository extends Aggregateable, Manageable
{
	/**
	 * Rebuild current index
	 */
	public void rebuildIndex();
}
