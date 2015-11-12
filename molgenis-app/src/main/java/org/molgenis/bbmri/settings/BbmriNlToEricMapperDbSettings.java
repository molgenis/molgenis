package org.molgenis.bbmri.settings;

import static org.molgenis.MolgenisFieldTypes.BOOL;
import static org.molgenis.MolgenisFieldTypes.EMAIL;

import org.molgenis.bbmri.controller.BbmriNlToEricMapperController;
import org.molgenis.data.settings.DefaultSettingsEntity;
import org.molgenis.data.settings.DefaultSettingsEntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class BbmriNlToEricMapperDbSettings extends DefaultSettingsEntity implements BbmriNlToEricMapperSettings
{
	private static final long serialVersionUID = 1L;

	private static final String ID = BbmriNlToEricMapperController.ID;

	@Component
	private static class Meta extends DefaultSettingsEntityMetaData
	{
		private static final String SCHEDULED_MAPPING_ENABLED = "scheduledMappingEnabled";
		private static final String MAPPER_DEFAULT_EMAIL_ADDRESS = "mapperDefEmail";
		private static final boolean DEFAULT_SCHEDULED_MAPPING_ENABLED = false;
		private static final String DEFAULT_MAPPER_DEFAULT_EMAIL_ADDRESS = "info@bbmri.nl";

		public Meta()
		{
			super(ID);
			setLabel("BBMRI-NL to BBMRI-ERIC scheduled mapping settings");
			addAttribute(SCHEDULED_MAPPING_ENABLED).setDataType(BOOL).setLabel("Scheduled mapping enabled")
					.setDescription("Schedule a nightly mapping job 5 minutes after midnight")
					.setDefaultValue(Boolean.toString(DEFAULT_SCHEDULED_MAPPING_ENABLED)).setNillable(false);
			addAttribute(MAPPER_DEFAULT_EMAIL_ADDRESS).setDataType(EMAIL).setLabel("Mapper default email address")
					.setDescription(
							"Email address that is set for sample collections in case no email address is defined for the sample collection")
					.setDefaultValue(DEFAULT_MAPPER_DEFAULT_EMAIL_ADDRESS).setNillable(false);
		}
	}

	public BbmriNlToEricMapperDbSettings()
	{
		super(ID);
	}

	@Override
	public boolean getScheduledMappingEnabled()
	{
		return getBoolean(Meta.SCHEDULED_MAPPING_ENABLED);
	}

	@Override
	public void setScheduledMappingEnabled(boolean scheduledMappingEnabled)
	{
		set(Meta.SCHEDULED_MAPPING_ENABLED, scheduledMappingEnabled);
	}

	@Override
	public String getMapperDefaultEmailAddress()
	{
		return getString(Meta.MAPPER_DEFAULT_EMAIL_ADDRESS);
	}

	@Override
	public void setMapperDefaultEmailAddress(String sampleCollectionDefaultEmailAddress)
	{
		set(Meta.MAPPER_DEFAULT_EMAIL_ADDRESS, sampleCollectionDefaultEmailAddress);
	}
}
