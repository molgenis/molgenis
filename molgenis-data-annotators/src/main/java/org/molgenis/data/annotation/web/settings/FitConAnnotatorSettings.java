package org.molgenis.data.annotation.web.settings;

import org.molgenis.data.annotation.core.entity.impl.FitConAnnotator;
import org.molgenis.settings.DefaultSettingsEntity;
import org.molgenis.settings.DefaultSettingsEntityType;
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
	public static class Meta extends DefaultSettingsEntityType
	{
		public static final String FITCON_LOCATION = "fitconLocation";

		public Meta()
		{
			super(ID);
		}

		@Override
		public void init()
		{
			super.init();
			setLabel("Fitcon annotator settings");
			addAttribute(FITCON_LOCATION).setLabel("Fitcon file location");
		}
	}
}
