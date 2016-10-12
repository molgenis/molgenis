package org.molgenis.annotation.cmd;

import org.molgenis.CommandLineOnlyConfiguration;
import org.molgenis.data.*;
import org.molgenis.data.annotation.core.utils.JarRunnerImpl;
import org.molgenis.data.convert.DateToStringConverter;
import org.molgenis.data.convert.StringToDateConverter;
import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.meta.model.AttributeMetadata;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.populate.EntityPopulator;
import org.molgenis.data.populate.UuidGenerator;
import org.molgenis.data.vcf.utils.VcfUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;

import javax.annotation.PostConstruct;
import java.util.Map;

import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;
import static org.molgenis.data.meta.model.TagMetaData.TAG;

/**
 * Commandline-specific annotator configuration.
 */
@Configuration
@CommandLineOnlyConfiguration
@ComponentScan({ "org.molgenis.data.meta.model", "org.molgenis.data.system.model", "org.molgenis.data.vcf.model",
		"org.molgenis.data.annotation.core.effects" })
public class CommandLineAnnotatorConfig
{
	@Autowired
	ApplicationContext applicationContext;

	@PostConstruct
	public void bootstrap()
	{
		EntityTypeMetadata entityTypeMeta = applicationContext.getBean(EntityTypeMetadata.class);
		applicationContext.getBean(AttributeMetadata.class).bootstrap(entityTypeMeta);
		Map<String, SystemEntityType> systemEntityMetaMap = applicationContext.getBeansOfType(SystemEntityType.class);

		systemEntityMetaMap.values().stream()
				.filter(systemEntityMeta -> systemEntityMeta.getName().equals(ENTITY_TYPE_META_DATA)
						|| systemEntityMeta.getName().equals(ATTRIBUTE_META_DATA)
						|| systemEntityMeta.getName().equals(PACKAGE) || systemEntityMeta.getName()
						.equals(TAG))
				.forEach(systemEntityMetaData -> systemEntityMetaData.bootstrap(entityTypeMeta));
	}

	@Value("${vcf-validator-location:@null}")
	private String vcfValidatorLocation;

	/**
	 * Needed to make @Value annotations with property placeholders work!
	 * <p>
	 * https://stackoverflow.com/questions/17097521/spring-3-2-value-annotation-with-pure-java-configuration-does-not
	 * -work-but-env
	 */
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer()
	{
		PropertySourcesPlaceholderConfigurer result = new PropertySourcesPlaceholderConfigurer();
		result.setNullValue("@null");
		return result;
	}

	@Bean
	VcfUtils vcfUtils()
	{
		return new VcfUtils();
	}

	@Bean
	public DataService dataService()
	{
		return new CmdLineDataService();
	}

	@Bean
	public VcfValidator vcfValidator()
	{
		return new VcfValidator(vcfValidatorLocation);
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
	EntityManager entityManager()
	{
		return new EntityManagerImpl(dataService(), new EntityFactoryRegistry(), entityPopulator());
	}

	@Bean
	public CmdLineAnnotator cmdLineAnnotator()
	{
		return new CmdLineAnnotator();
	}

	@Bean
	public EntityPopulator entityPopulator()
	{
		return new EntityPopulator(uuidGenerator());
	}

	@Bean
	public UuidGenerator uuidGenerator()
	{
		return new UuidGenerator();
	}

	@Bean
	public JarRunnerImpl jarRunner()
	{
		return new JarRunnerImpl();
	}

	@Bean
	public Entity snpEffAnnotatorSettings()
	{
		return new CmdLineSettingsEntity();
	}

	@Bean
	public Entity goNLAnnotatorSettings()
	{
		return new CmdLineSettingsEntity();
	}

	@Bean
	public Entity thousendGenomesAnnotatorSettings()
	{
		return new CmdLineSettingsEntity();
	}

	@Bean
	public Entity CGDAnnotatorSettings()
	{
		return new CmdLineSettingsEntity();
	}

	@Bean
	public Entity clinvarAnnotatorSettings()
	{
		return new CmdLineSettingsEntity();
	}

	@Bean
	public Entity dannAnnotatorSettings()
	{
		return new CmdLineSettingsEntity();
	}

	@Bean
	public Entity exacAnnotatorSettings()
	{
		return new CmdLineSettingsEntity();
	}

	@Bean
	public Entity caddAnnotatorSettings()
	{
		return new CmdLineSettingsEntity();
	}

	@Bean
	public Entity fitConAnnotatorSettings()
	{
		return new CmdLineSettingsEntity();
	}

	@Bean
	public Entity HPOAnnotatorSettings()
	{
		return new CmdLineSettingsEntity();
	}

	@Bean
	public Entity gavinAnnotatorSettings()
	{
		return new CmdLineSettingsEntity();
	}

	@Bean
	public Entity omimAnnotatorSettings()
	{
		return new CmdLineSettingsEntity();
	}
}
