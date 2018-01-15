package org.molgenis.data.annotation.web.settings;

import org.molgenis.data.annotation.core.entity.impl.omim.OmimAnnotator;
import org.molgenis.data.annotation.core.utils.AnnotatorUtils;
import org.molgenis.settings.DefaultSettingsEntity;
import org.molgenis.settings.DefaultSettingsEntityType;
import org.springframework.stereotype.Component;

@Component
public class OmimAnnotatorSettings extends DefaultSettingsEntity
{
	private static final long serialVersionUID = 1L;
	private static final String ID = OmimAnnotator.NAME;

	public OmimAnnotatorSettings()
	{
		super(ID);
	}

	@Component
	public static class Meta extends DefaultSettingsEntityType
	{
		public static final String OMIM_LOCATION = "omimLocation";

		public Meta()
		{
			super(ID);
		}

		@Override
		public void init()
		{
			super.init();
			setLabel("OMIM annotator settings");
			String defaultLocation = AnnotatorUtils.getAnnotatorResourceDir() + "/omim/omim.txt";
			addAttribute(OMIM_LOCATION).setLabel("OMIM morbid map File location").setDefaultValue(defaultLocation);
		}
	}

}
