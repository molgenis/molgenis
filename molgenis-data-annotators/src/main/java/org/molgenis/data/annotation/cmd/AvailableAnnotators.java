package org.molgenis.data.annotation.cmd;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.VariantAnnotator;
import org.molgenis.data.annotation.impl.GoNLServiceAnnotator;
import org.molgenis.data.support.AnnotationServiceImpl;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.framework.server.MolgenisSimpleSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

public class AvailableAnnotators
{
	@Configuration
	public static class Config
	{
		@Bean
		MolgenisSettings settings()
		{
			return new MolgenisSimpleSettings();
		}

		@Bean
		AnnotationService annotationService()
		{
			return new AnnotationServiceImpl();
		}
	}

	@Autowired
	private GoNLServiceAnnotator gonlAnnotator;

	public List<VariantAnnotator> getAnnotators()
	{
		List<VariantAnnotator> res = new ArrayList<VariantAnnotator>();
		res.add(gonlAnnotator);
		return res;
	}

}
