package org.molgenis.data.annotation.core.settings;

import org.molgenis.data.annotation.core.entity.impl.CGDAnnotator;
import org.molgenis.data.annotation.core.utils.AnnotatorUtils;
import org.molgenis.data.settings.DefaultSettingsEntity;
import org.molgenis.data.settings.DefaultSettingsEntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class CGDAnnotatorSettings extends DefaultSettingsEntity
{
	private static final long serialVersionUID = 1L;
	private static final String ID = CGDAnnotator.NAME;

	public CGDAnnotatorSettings()
	{
		super(ID);
	}

	@Component
	public static class Meta extends DefaultSettingsEntityMetaData
	{
		public static final String CGD_LOCATION = "cgdLocation";

		public Meta()
		{
			super(ID);
		}

		@Override
		public void init()
		{
			super.init();
			setLabel("CGD annotator settings");

			String defaultLocation = AnnotatorUtils.getAnnotatorResourceDir() + "/CGD/CGD.txt";
			addAttribute(CGD_LOCATION).setLabel("CGD file location").setDefaultValue(defaultLocation);
		}
	}
}
