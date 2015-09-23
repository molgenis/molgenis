package org.molgenis.rdconnect;

public interface IdCardBiobankIndexerSettings
{
	String getApiBaseUri();

	void setApiBaseUri(String idCardApiBaseUri);

	String getBiobankResource();

	void setBiobankResource(String idCardBiobankResource);

	String getBiobankCollectionResource();

	void setBiobankCollectionResource(String biobankCollectionResource);

	String getBiobankCollectionSelectionResource();

	void setBiobankCollectionSelectionResource(String biobankCollectionSelectionResource);
}