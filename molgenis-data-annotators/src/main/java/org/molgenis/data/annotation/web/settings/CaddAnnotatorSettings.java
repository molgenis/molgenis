package org.molgenis.data.annotation.web.settings;

import org.molgenis.data.annotation.core.entity.impl.CaddAnnotator;
import org.molgenis.data.annotation.core.utils.AnnotatorUtils;
import org.molgenis.settings.DefaultSettingsEntity;
import org.molgenis.settings.DefaultSettingsEntityType;
import org.springframework.stereotype.Component;

@Component
public class CaddAnnotatorSettings extends DefaultSettingsEntity
{
	private static final long serialVersionUID = 1L;
	private static final String ID = CaddAnnotator.NAME;

	public CaddAnnotatorSettings()
	{
		super(ID);
	}

	@Component
	public static class Meta extends DefaultSettingsEntityType
	{
		public static final String CADD_LOCATION = "caddLocation";

		public Meta()
		{
			super(ID);
		}

		@Override
		public void init()
		{
			super.init();
			setLabel("Cadd annotator settings");

			String defaultLocation = AnnotatorUtils.getAnnotatorResourceDir() + "/CADD/1000G.vcf.gz";
			addAttribute(CADD_LOCATION).setLabel("Cadd file location").setDefaultValue(defaultLocation);
		}
	}
}
