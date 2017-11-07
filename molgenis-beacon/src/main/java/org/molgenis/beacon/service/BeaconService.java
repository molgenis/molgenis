package org.molgenis.beacon.service;

import java.util.List;

public interface BeaconService
{
	/**
	 * List available datasets
	 */
	List<String> listDatasets();

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
