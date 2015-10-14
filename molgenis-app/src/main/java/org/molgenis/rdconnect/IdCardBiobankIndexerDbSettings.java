package org.molgenis.rdconnect;

import static org.molgenis.MolgenisFieldTypes.BOOL;
import static org.molgenis.MolgenisFieldTypes.EMAIL;
import static org.molgenis.MolgenisFieldTypes.STRING;

import org.molgenis.data.settings.DefaultSettingsEntity;
import org.molgenis.data.settings.DefaultSettingsEntityMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class IdCardBiobankIndexerDbSettings extends DefaultSettingsEntity implements IdCardBiobankIndexerSettings
{
	private static final long serialVersionUID = 1L;

	private static final String ID = IdCardBiobankIndexerController.ID;

	private final ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	public IdCardBiobankIndexerDbSettings(ApplicationEventPublisher applicationEventPublisher)
	{
		super(ID);
		this.applicationEventPublisher = applicationEventPublisher;
	}

	@Component
	private static class Meta extends DefaultSettingsEntityMetaData
	{
		private static final String API_BASE_URI = "apiBaseUri";
		private static final String BIOBANK_RESOURCE = "biobankResource";
		private static final String BIOBANK_COLLECTIONS_RESOURCE = "biobankCollResource";
		private static final String BIOBANK_COLLECTIONS_SELECTION_RESOURCE = "biobankCollSelResource";
		private static final String BIOBANK_INDEXING_ENABLED = "biobankIndexingEnabled";
		private static final String NOTIFICATION_EMAIL = "notificationEmail";

		private static final String BIOBANK_INDEXING_FREQUENCY = "biobankIndexingFrequency";
		private static final String DEFAULT_API_BASE_URI = "http://catalogue.rd-connect.eu/api/jsonws/BiBBoxCommonServices-portlet.logapi";
		private static final String DEFAULT_BIOBANK_RESOURCE = "regbb/organization-id";
		private static final String DEFAULT_BIOBANK_COLLECTIONS_RESOURCE = "regbbs";
		private static final String DEFAULT_BIOBANK_COLLECTIONS_SELECTION_RESOURCE = DEFAULT_BIOBANK_COLLECTIONS_RESOURCE
				+ "/data";
		private static final boolean DEFAULT_BIOBANK_INDEXING_ENABLED = false;
		private static final String DEFAULT_BIOBANK_INDEXING_FREQUENCY = "0 4 * * * ?";
		private static final String DEFAULT_NOTIFICATION_EMAIL = "molgenis+idcard@gmail.com";

		public Meta()
		{
			super(ID);
			setLabel("ID-Card biobank indexer settings");
			addAttribute(API_BASE_URI).setDataType(STRING).setLabel("API base URI")
					.setDefaultValue(DEFAULT_API_BASE_URI);
			addAttribute(BIOBANK_RESOURCE).setDataType(STRING).setLabel("Biobank resource")
					.setDefaultValue(DEFAULT_BIOBANK_RESOURCE);
			addAttribute(BIOBANK_COLLECTIONS_RESOURCE).setDataType(STRING).setLabel("Biobank collection resource")
					.setDefaultValue(DEFAULT_BIOBANK_COLLECTIONS_RESOURCE);
			addAttribute(BIOBANK_COLLECTIONS_SELECTION_RESOURCE).setDataType(STRING)
					.setLabel("Biobank collection filtered resource")
					.setDefaultValue(DEFAULT_BIOBANK_COLLECTIONS_SELECTION_RESOURCE);
			addAttribute(BIOBANK_INDEXING_ENABLED).setDataType(BOOL).setLabel("Biobank indexing enabled")
					.setDefaultValue(Boolean.toString(DEFAULT_BIOBANK_INDEXING_ENABLED)).setNillable(false);
			addAttribute(BIOBANK_INDEXING_FREQUENCY).setDataType(STRING).setLabel("Biobank indexing frequency")
					.setDescription("Cron expression (e.g. 0 4 * * * ?)")
					.setDefaultValue(DEFAULT_BIOBANK_INDEXING_FREQUENCY).setNillable(false)
					.setVisibleExpression("$('" + BIOBANK_INDEXING_ENABLED + "').eq(true).value()");
			addAttribute(NOTIFICATION_EMAIL).setDataType(EMAIL).setLabel("Notification email")
					.setDescription("email address used for index failure notifications")
					.setDefaultValue(DEFAULT_NOTIFICATION_EMAIL);
		}
	}

	@Override
	public String getApiBaseUri()
	{
		return getString(Meta.API_BASE_URI);
	}

	@Override
	public void setApiBaseUri(String apiBaseUri)
	{
		set(Meta.API_BASE_URI, apiBaseUri);
	}

	@Override
	public String getBiobankResource()
	{
		return getString(Meta.BIOBANK_RESOURCE);
	}

	@Override
	public void setBiobankResource(String biobankResource)
	{
		set(Meta.BIOBANK_RESOURCE, biobankResource);
	}

	@Override
	public String getBiobankCollectionResource()
	{
		return getString(Meta.BIOBANK_COLLECTIONS_RESOURCE);
	}

	@Override
	public void setBiobankCollectionResource(String biobankCollectionResource)
	{
		set(Meta.BIOBANK_COLLECTIONS_RESOURCE, biobankCollectionResource);
	}

	@Override
	public String getBiobankCollectionSelectionResource()
	{
		return getString(Meta.BIOBANK_COLLECTIONS_SELECTION_RESOURCE);
	}

	@Override
	public void setBiobankCollectionSelectionResource(String biobankCollectionSelectionResource)
	{
		set(Meta.BIOBANK_COLLECTIONS_SELECTION_RESOURCE, biobankCollectionSelectionResource);
	}

	@Override
	public boolean getBiobankIndexingEnabled()
	{
		Boolean enableBiobankIndexing = getBoolean(Meta.BIOBANK_INDEXING_ENABLED);
		return enableBiobankIndexing != null ? enableBiobankIndexing : false;
	}

	@Override
	public void setBiobankIndexingEnabled(boolean biobankIndexing)
	{
		set(Meta.BIOBANK_INDEXING_ENABLED, biobankIndexing);
		applicationEventPublisher.publishEvent(new IdCardBiobankIndexingEnabledEvent(this, biobankIndexing));
	}

	@Override
	public String getBiobankIndexingFrequency()
	{
		return getString(Meta.BIOBANK_INDEXING_FREQUENCY);
	}

	@Override
	public void setBiobankIndexingFrequency(String cronExpression)
	{
		// TODO validate if cronExpession is valid, not here but in decorator
		set(Meta.BIOBANK_INDEXING_FREQUENCY, cronExpression);
		applicationEventPublisher.publishEvent(new IdCardBiobankIndexingFrequencyEvent(this, cronExpression));
	}

	@Override
	public String getNotificationEmail()
	{
		return getString(Meta.NOTIFICATION_EMAIL);
	}

	@Override
	public void setNotificationEmail(String notificationEmail)
	{
		set(Meta.NOTIFICATION_EMAIL, notificationEmail);
	}
}
