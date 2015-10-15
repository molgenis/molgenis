package org.molgenis.rdconnect;

public interface IdCardBiobankIndexer
{
	/**
	 * @param username
	 *            user who requested the index rebuild or null if the rebuild was initiated by the system
	 */
	void rebuildIndex(String username);

	void onIndexConfigurationUpdate(String updateMessage);
}
