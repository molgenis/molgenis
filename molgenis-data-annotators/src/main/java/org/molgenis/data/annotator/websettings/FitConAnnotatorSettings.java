package org.molgenis.data.annotator.websettings;

import org.molgenis.data.annotation.entity.impl.FitConAnnotator;
import org.molgenis.data.settings.DefaultSettingsEntity;
import org.molgenis.data.settings.DefaultSettingsEntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class FitConAnnotatorSettings extends DefaultSettingsEntity
{
	private static final long serialVersionUID = 1L;
	public static final String ID = FitConAnnotator.NAME;

	public FitConAnnotatorSettings()
	{
		super(ID);
	}

	@Component
	public static class Meta extends DefaultSettingsEntityMetaData
	{
		public static final String FITCON_LOCATION = "fitconLocation";

		public Meta()
		{
			super(ID);
			setLabel("Fitcon annotator settings");
			addAttribute(FITCON_LOCATION).setLabel("Fitcon file location");
		}
	}
}
