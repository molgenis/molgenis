package org.molgenis.beacon.service;

public interface BeaconService
{
	/**
	 * Query a table based on the given EntityTypeID and answer the query with "yes" or "no"
	 *
	 * @param chromosome
	 * @param position
	 * @param reference
	 * @param allele
	 * @param entityTypeID
	 * @return Whether the variant exists in the given dataset or not
	 */
	boolean query(String chromosome, Long position, String reference, String allele, String entityTypeID);
}
