package org.molgenis.data.annotation.cmd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.molgenis.CommandLineOnlyConfiguration;
import org.molgenis.data.DataService;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.entity.AnnotatorInfo;
import org.molgenis.data.support.AnnotationServiceImpl;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.framework.server.MolgenisSimpleSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Commandline-specific annotator configuration.
 */
@Configuration
@CommandLineOnlyConfiguration
public class CommandLineAnnotatorConfig
{
	@Bean
	MolgenisSettings settings()
	{
		return new MolgenisSimpleSettings();
	}

	@Bean
	DataService dataService()
	{
		return new DataServiceImpl();
	}

	@Bean
	AnnotationService annotationService()
	{
		return new AnnotationServiceImpl();
	}

	/**
	 * Helper function to select the annotators that have received a recent brush up for the new way of configuring
	 * 
	 * @param configuredAnnotators
	 * @return
	 */
	static HashMap<String, RepositoryAnnotator> getFreshAnnotators(Map<String, RepositoryAnnotator> configuredAnnotators)
	{
		HashMap<String, RepositoryAnnotator> configuredFreshAnnotators = new HashMap<String, RepositoryAnnotator>();
		for (String annotator : configuredAnnotators.keySet())
		{
			if (configuredAnnotators.get(annotator).getInfo() != null
					&& configuredAnnotators.get(annotator).getInfo().getStatus().equals(AnnotatorInfo.Status.READY))
			{
				configuredFreshAnnotators.put(annotator, configuredAnnotators.get(annotator));
			}
		}
		return configuredFreshAnnotators;
	}

	/**
	 * Helper function to print annotators per type
	 * 
	 * @param annotators
	 * @return
	 */
	static String printAnnotatorsPerType(Map<String, RepositoryAnnotator> annotators)
	{
		Map<AnnotatorInfo.Type, List<String>> annotatorsPerType = new HashMap<AnnotatorInfo.Type, List<String>>();
		for (String annotator : annotators.keySet())
		{
			AnnotatorInfo.Type type = annotators.get(annotator).getInfo().getType();
			if (annotatorsPerType.containsKey(type))
			{
				annotatorsPerType.get(type).add(annotator);
			}
			else
			{
				annotatorsPerType.put(type, new ArrayList<String>(Arrays.asList(new String[]
				{ annotator })));
			}

		}
		StringBuilder sb = new StringBuilder();
		for (AnnotatorInfo.Type type : annotatorsPerType.keySet())
		{
			sb.append("\t" + type + "\n");
			for (String s : annotatorsPerType.get(type))
			{
				sb.append("\t\t" + s + "\n");
			}
		}

		return sb.toString();
	}
}
