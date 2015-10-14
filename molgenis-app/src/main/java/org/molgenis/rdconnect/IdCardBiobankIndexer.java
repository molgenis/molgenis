package org.molgenis.rdconnect;

public interface IdCardBiobankIndexer
{
	public enum IndexAction
	{
		MANUAL, SCHEDULED
	}

	void rebuildIndex(IndexAction indexAction);

	void onIndexConfigurationUpdate(String updateMessage);
}
