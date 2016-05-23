package org.molgenis.data.idcard.settings;

import org.molgenis.data.settings.SettingsEntityListener;

public interface IdCardIndexerSettings
{
	String getApiBaseUri();

	void setApiBaseUri(String idCardApiBaseUri);

	long getApiTimeout();

	void setApiTimeout(long timeout);

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

	String getNotificationEmail();

	void setNotificationEmail(String notificationEmail);

	long getIndexRebuildTimeout();

	void setIndexRebuildTimeout(long timeout);
}