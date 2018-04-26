package org.molgenis.data.annotation.web.settings;

import org.molgenis.data.annotation.core.entity.impl.gavin.GavinAnnotator;
import org.molgenis.data.annotation.core.utils.AnnotatorUtils;
import org.molgenis.settings.DefaultSettingsEntity;
import org.molgenis.settings.DefaultSettingsEntityType;
import org.springframework.stereotype.Component;

@Component
public class GavinAnnotatorSettings extends DefaultSettingsEntity
{
	private static final long serialVersionUID = 1L;
	private static final String ID = GavinAnnotator.NAME;

	public GavinAnnotatorSettings()
	{
		super(ID);
	}

	@Component
	public static class Meta extends DefaultSettingsEntityType
	{
		public static final String VARIANT_FILE_LOCATION = "variantFileLocation";

		public Meta()
		{
			super(ID);
		}

		@Override
		public void init()
		{
			super.init();
			setLabel(GavinAnnotator.NAME + " annotator settings");

			String defaultLocation =
					AnnotatorUtils.getAnnotatorResourceDir() + "/variant/variantinterpretation_emx.xlsx";
			addAttribute(VARIANT_FILE_LOCATION).setLabel("Variant classification file location")
											   .setDefaultValue(defaultLocation);
		}
	}
}
