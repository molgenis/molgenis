package org.molgenis.data.annotator.websettings;

import org.molgenis.data.annotation.entity.impl.SnpEffAnnotator;
import org.molgenis.data.annotation.utils.AnnotatorUtils;
import org.molgenis.data.settings.DefaultSettingsEntity;
import org.molgenis.data.settings.DefaultSettingsEntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class SnpEffAnnotatorSettings extends DefaultSettingsEntity
{
	private static final long serialVersionUID = 1L;
	private static final String ID = SnpEffAnnotator.NAME;

	public SnpEffAnnotatorSettings()
	{
		super(ID);
	}

	@Component
	public static class Meta extends DefaultSettingsEntityMetaData
	{
		public static final String SNPEFF_JAR_LOCATION = "snpEffJarLocation";

		public Meta()
		{
			super(ID);
			setLabel("SnpEff annotator settings");
			String defaultLocation = AnnotatorUtils.getAnnotatorResourceDir() + "/Applications/snpEff/snpEff.jar";
			addAttribute(SNPEFF_JAR_LOCATION).setLabel("SnpEff jar location").setDefaultValue(defaultLocation);
		}
	}

}
