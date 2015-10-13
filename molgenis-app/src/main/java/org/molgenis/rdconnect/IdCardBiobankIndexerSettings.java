package org.molgenis.rdconnect;

import org.molgenis.data.settings.SettingsEntityListener;

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

	boolean getBiobankIndexingEnabled();

	void setBiobankIndexingEnabled(boolean biobankIndexing);

	String getBiobankIndexingFrequency();

	void setBiobankIndexingFrequency(String cronExpression);

	void addListener(SettingsEntityListener settingsEntityListener);

	void removeListener(SettingsEntityListener settingsEntityListener);
}