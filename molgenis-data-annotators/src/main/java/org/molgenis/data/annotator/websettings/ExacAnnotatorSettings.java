package org.molgenis.data.annotator.websettings;

import org.molgenis.data.annotation.entity.impl.ExacAnnotator;
import org.molgenis.data.settings.DefaultSettingsEntity;
import org.molgenis.data.settings.DefaultSettingsEntityMetaData;
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
	public static class Meta extends DefaultSettingsEntityMetaData
	{
		public static final String EXAC_LOCATION = "exacLocation";

		public Meta()
		{
			super(ID);
			setLabel("Exac annotator settings");
			addAttribute(EXAC_LOCATION).setLabel("Exac file location");
		}
	}
}
