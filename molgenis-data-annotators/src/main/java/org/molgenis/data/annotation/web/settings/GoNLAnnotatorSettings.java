package org.molgenis.data.annotation.web.settings;

import org.molgenis.data.annotation.core.entity.impl.GoNLAnnotator;
import org.molgenis.settings.DefaultSettingsEntity;
import org.molgenis.settings.DefaultSettingsEntityType;
import org.springframework.stereotype.Component;

@Component
public class GoNLAnnotatorSettings extends DefaultSettingsEntity
{
	private static final long serialVersionUID = 1L;
	private static final String ID = GoNLAnnotator.NAME;

	public GoNLAnnotatorSettings()
	{
		super(ID);
	}

	@Component
	public static class Meta extends DefaultSettingsEntityType
	{
		public static final String CHROMOSOMES = "chromosomes";
		public static final String FILEPATTERN = "filepattern";
		public static final String OVERRIDE_CHROMOSOME_FILES = "overrideChromosomeFiles";
		public static final String ROOT_DIRECTORY = "rootDirectory";

		public Meta()
		{
			super(ID);
		}

		@Override
		public void init()
		{
			super.init();
			setLabel("GoNL annotator settings");
			addAttribute(ROOT_DIRECTORY).setLabel("Root directory");
			addAttribute(CHROMOSOMES).setDefaultValue("1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,X")
									 .setLabel("Chromosomes");
			addAttribute(FILEPATTERN).setDefaultValue("gonl.chr%s.snps_indels.r5.vcf.gz").setLabel("Filepattern");
			addAttribute(OVERRIDE_CHROMOSOME_FILES).setDefaultValue("X:gonl.chrX.release4.gtc.vcf.gz")
												   .setLabel("Override chromosomes file");
		}
	}
}
