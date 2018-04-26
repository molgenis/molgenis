package org.molgenis.data.annotation.web.settings;

import org.molgenis.data.annotation.core.entity.impl.snpeff.SnpEffAnnotator;
import org.molgenis.data.annotation.core.utils.AnnotatorUtils;
import org.molgenis.settings.DefaultSettingsEntity;
import org.molgenis.settings.DefaultSettingsEntityType;
import org.springframework.stereotype.Component;

@Component
public class SnpEffAnnotatorSettings extends DefaultSettingsEntity
{
	private static final long serialVersionUID = 1L;
	public static final String ID = SnpEffAnnotator.NAME;

	public SnpEffAnnotatorSettings()
	{
		super(ID);
	}

	@Component
	public static class Meta extends DefaultSettingsEntityType
	{
		public static final String SNPEFF_JAR_LOCATION = "snpEffJarLocation";

		public Meta()
		{
			super(ID);
		}

		@Override
		public void init()
		{
			super.init();
			setLabel("SnpEff annotator settings");
			String defaultLocation = AnnotatorUtils.getAnnotatorResourceDir() + "/Applications/snpEff/snpEff.jar";
			addAttribute(SNPEFF_JAR_LOCATION).setLabel("SnpEff jar location").setDefaultValue(defaultLocation);
		}
	}

}
