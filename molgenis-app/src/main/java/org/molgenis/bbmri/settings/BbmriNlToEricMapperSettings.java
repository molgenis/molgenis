package org.molgenis.bbmri.settings;

public interface BbmriNlToEricMapperSettings
{
	boolean getScheduledMappingEnabled();

	void setScheduledMappingEnabled(boolean scheduledMappingEnabled);

	String getMapperDefaultEmailAddress();

	void setMapperDefaultEmailAddress(String mapperDefaultEmailAddress);
}