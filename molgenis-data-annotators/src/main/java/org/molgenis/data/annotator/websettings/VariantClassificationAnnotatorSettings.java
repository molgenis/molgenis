package org.molgenis.data.annotator.websettings;

import org.molgenis.data.annotation.entity.impl.CaddAnnotator;
import org.molgenis.data.annotation.entity.impl.VariantClassificationAnnotator;
import org.molgenis.data.annotation.utils.AnnotatorUtils;
import org.molgenis.data.settings.DefaultSettingsEntity;
import org.molgenis.data.settings.DefaultSettingsEntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class VariantClassificationAnnotatorSettings extends DefaultSettingsEntity
{
	private static final long serialVersionUID = 1L;
	private static final String ID = VariantClassificationAnnotator.NAME;

	public VariantClassificationAnnotatorSettings()
	{
		super(ID);
	}

	@Component
	public static class Meta extends DefaultSettingsEntityMetaData
	{
		public static final String VARIANT_FILE_LOCATION = "variantFileLocation";

		public Meta()
		{
			super(ID);
			setLabel(VariantClassificationAnnotator.NAME + " annotator settings");

			String defaultLocation = AnnotatorUtils.getAnnotatorResourceDir() + "/variant/variantinterpretation_emx.xlsx";
			addAttribute(VARIANT_FILE_LOCATION).setLabel("Variant classification file location").setDefaultValue(defaultLocation);
		}
	}
}
