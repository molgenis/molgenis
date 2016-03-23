package org.molgenis.data.annotation.cmd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.molgenis.CommandLineOnlyConfiguration;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.entity.AnnotatorInfo;
import org.molgenis.data.convert.DateToStringConverter;
import org.molgenis.data.convert.StringToDateConverter;
import org.molgenis.data.support.AnnotationServiceImpl;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.support.MapEntity;
import org.molgenis.util.ApplicationContextProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;

/**
 * Commandline-specific annotator configuration.
 */
@Configuration
@CommandLineOnlyConfiguration
public class CommandLineAnnotatorConfig
{

	@Value("${vcf-validator-location:@null}")
	private String vcfValidatorLocation;

	/**
	 * Needed to make @Value annotations with property placeholders work!
	 * 
	 * @see https
	 *      ://stackoverflow.com/questions/17097521/spring-3-2-value-annotation-with-pure-java-configuration-does-not
	 *      -work-but-env
	 */
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer()
	{
		PropertySourcesPlaceholderConfigurer result = new PropertySourcesPlaceholderConfigurer();
		result.setNullValue("@null");
		return result;
	}

	@Bean
	public CmdLineAnnotator cmdLineAnnotator()
	{
		return new CmdLineAnnotator();
	}

	@Bean
	public VcfValidator vcfValidator()
	{
		return new VcfValidator(vcfValidatorLocation);
	}

	/**
	 * Beans that allows referencing Spring managed beans from Java code which is not managed by Spring
	 * 
	 * @return
	 */
	@Bean
	public ApplicationContextProvider applicationContextProvider()
	{
		return new ApplicationContextProvider();
	}

	@Bean
	ConversionService conversionService()
	{
		DefaultConversionService registry = new DefaultConversionService();
		registry.addConverter(new DateToStringConverter());
		registry.addConverter(new StringToDateConverter());
		return registry;
	}

	@Bean
	public Entity caddAnnotatorSettings()
	{
		return new MapEntity();
	}

	@Bean
	public Entity snpEffAnnotatorSettings()
	{
		return new MapEntity();
	}

	@Bean
	public Entity goNLAnnotatorSettings()
	{
		return new MapEntity();
	}

	@Bean
	public Entity thousendGenomesAnnotatorSettings()
	{
		return new MapEntity();
	}

	@Bean
	public Entity CGDAnnotatorSettings()
	{
		return new MapEntity();
	}

	@Bean
	public Entity clinvarAnnotatorSettings()
	{
		return new MapEntity();
	}

	@Bean
	public Entity dannAnnotatorSettings()
	{
		return new MapEntity();
	}

	@Bean
	public Entity exacAnnotatorSettings()
	{
		return new MapEntity();
	}

	@Bean
	public Entity fitConAnnotatorSettings()
	{
		return new MapEntity();
	}

	@Bean
	public Entity HPOAnnotatorSettings()
	{
		return new MapEntity();
	}
	
	@Bean
	public Entity omimAnnotatorSettings()
	{
		return new MapEntity();
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
			sb.append("### " + type + " ###\n");
			for (String annotatorName : annotatorsPerType.get(type))
			{
				sb.append("* " + annotatorName + "\n");
			}

			sb.append("\n");
		}

		return sb.toString();
	}
}
