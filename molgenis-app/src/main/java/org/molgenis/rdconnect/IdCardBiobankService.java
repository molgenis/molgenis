package org.molgenis.rdconnect;

import org.molgenis.data.Entity;

/**
 * ID-Cards: http://rd-connect.eu/platform/biobanks/id-cards-linking-up-rare-disease-research-across-the-world/
 */
public interface IdCardBiobankService
{
	/**
	 * Return all biobanks from ID-Cards as entities
	 * 
	 * @return
	 */
	public Iterable<Entity> getIdCardBiobanks();

	/**
	 * Return a biobank by id from ID-Cards as entity
	 * 
	 * @param id
	 * @return
	 */
	public Entity getIdCardBiobank(String id);

	/**
	 * Return biobanks by ids from ID-Cards as entities
	 * 
	 * @param ids
	 * @return
	 */
	public Iterable<Entity> getIdCardBiobanks(Iterable<String> ids);
}