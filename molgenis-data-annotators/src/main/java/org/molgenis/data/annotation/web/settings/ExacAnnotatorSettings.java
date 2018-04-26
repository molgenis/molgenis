package org.molgenis.data.annotation.web.settings;

import org.molgenis.data.annotation.core.entity.impl.ExacAnnotator;
import org.molgenis.settings.DefaultSettingsEntity;
import org.molgenis.settings.DefaultSettingsEntityType;
import org.springframework.stereotype.Component;

@Component
public class ExacAnnotatorSettings extends DefaultSettingsEntity
{
	private static final long serialVersionUID = 1L;
	private static final String ID = ExacAnnotator.NAME;

	public ExacAnnotatorSettings()
	{
		super(ID);
	}

	@Component
	public static class Meta extends DefaultSettingsEntityType
	{
		public static final String EXAC_LOCATION = "exacLocation";

		public Meta()
		{
			super(ID);
		}

		@Override
		public void init()
		{
			super.init();
			setLabel("Exac annotator settings");
			addAttribute(EXAC_LOCATION).setLabel("Exac file location");
		}
	}
}
