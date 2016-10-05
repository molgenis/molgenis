package org.molgenis.data.idcard.client;

import org.molgenis.data.Entity;

/**
 * ID-Cards: http://rd-connect.eu/platform/biobanks/id-cards-linking-up-rare-disease-research-across-the-world/
 */
public interface IdCardClient
{
	/**
	 * Return all biobanks from ID-Cards as entities
	 *
	 * @return
	 */
	Iterable<Entity> getIdCardBiobanks();

	/**
	 * Return all biobanks from ID-Cards as entities, throws an exception if the request took longer than the given
	 * timeout.
	 *
	 * @param timeout request timeout in ms
	 * @return
	 */
	Iterable<Entity> getIdCardBiobanks(long timeout);

	/**
	 * Return a biobank by id from ID-Cards as entity
	 *
	 * @param id
	 * @return
	 */
	Entity getIdCardBiobank(String id);

	/**
	 * Return a biobank by id from ID-Cards as entity, throws an exception if the request took longer than the given
	 * timeout.
	 *
	 * @param id
	 * @param timeout request timeout in ms
	 * @return
	 */
	Entity getIdCardBiobank(String id, long timeout);

	/**
	 * Return biobanks by ids from ID-Cards as entities
	 *
	 * @param ids
	 * @return
	 */
	Iterable<Entity> getIdCardBiobanks(Iterable<String> ids);

	/**
	 * Return biobanks by ids from ID-Cards as entities, throws an exception if the request took longer than the given
	 * timeout.
	 *
	 * @param ids
	 * @return
	 */
	Iterable<Entity> getIdCardBiobanks(Iterable<String> ids, long timeout);
}