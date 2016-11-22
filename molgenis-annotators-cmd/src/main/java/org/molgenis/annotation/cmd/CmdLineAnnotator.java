package org.molgenis.annotation.cmd;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.molgenis.annotation.cmd.conversion.EffectStructureConverter;
import org.molgenis.annotation.cmd.utils.CmdLineAnnotatorUtils;
import org.molgenis.annotation.cmd.utils.VcfValidator;
import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.annotation.core.entity.AnnotatorConfig;
import org.molgenis.data.annotation.core.entity.AnnotatorInfo;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.JOptCommandLinePropertySource;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

/**
 * Build JAR file...............: mvn clean install -pl molgenis-annotators-cmd/ -am -DskipTests -P create-delivery
 * Run..........................: java -jar molgenis-annotators-cmd/target/CmdLineAnnotator.jar
 */
public class CmdLineAnnotator
{
	private static final String VALIDATE = "validate";
	private static final String OUTPUT = "output";
	private static final String VCF_VALIDATOR_LOCATION = "vcf-validator-location";
	private static final String SOURCE = "source";
	private static final String ANNOTATOR = "annotator";
	private static final String INPUT = "input";
	private static final String HELP = "help";
	private static final String REPLACE = "replace";
	private static final String UPDATE_ANNOTATIONS = "update-annotations";
	private static final String USER_HOME = "user.home";

	@Autowired
	CommandLineAnnotatorConfig commandLineAnnotatorConfig;

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	VcfValidator vcfValidator;

	@Autowired
	VcfAttributes vcfAttributes;

	@Autowired
	EffectStructureConverter effectStructureConverter;

	@Autowired
	EntityTypeFactory entityTypeFactory;

	@Autowired
	AttributeFactory attributeFactory;

	// Default settings for running vcf-validator
	private void run(OptionSet options, OptionParser parser) throws Exception
	{
		Map<String, AnnotatorConfig> annotatorMap = applicationContext.getBeansOfType(AnnotatorConfig.class);
		annotatorMap.values().forEach(AnnotatorConfig::init);

		Map<String, RepositoryAnnotator> configuredAnnotators = applicationContext
				.getBeansOfType(RepositoryAnnotator.class);

		// for now, only get the annotators that have received a recent brush up for the new way of configuring
		Map<String, RepositoryAnnotator> configuredFreshAnnotators = getFreshAnnotators(configuredAnnotators);

		Set<String> annotatorNames = configuredFreshAnnotators.keySet();

		if (!options.has(ANNOTATOR) || options.has(HELP))
		{

			String implementationVersion = getClass().getPackage().getImplementationVersion();
			if (implementationVersion == null)
			{
				implementationVersion = "";
			}

			System.out.println("\n" + "****************************************************\n"
					+ "* MOLGENIS Annotator, commandline interface " + implementationVersion + " *\n"
					+ "****************************************************\n"
					+ "Typical usage to annotate a VCF file:\n\n"
					+ "java -jar CmdLineAnnotator.jar [options] [attribute names]\n"
					+ "Example: java -Xmx4g -jar CmdLineAnnotator.jar -v -a gonl -s GoNL/release5_noContam_noChildren_with_AN_AC_GTC_stripped/ -i Cardio.vcf -o Cardio_gonl.vcf GoNL_GTC GoNL_AF\n"
					+ "\n" + "----------------------------------------------------\n\n" + "Available options:\n");

			parser.printHelpOn(System.out);

			System.out.println("\n" + "----------------------------------------------------\n\n"
					+ "To get detailed description for a specific annotator:\n"
					+ "java -jar CmdLineAnnotator.jar -a [Annotator]\n\n"
					+ "To select only a few columns from an annotation source instead of everything, use:\n"
					+ "java -jar CmdLineAnnotator.jar -a [Annotator] -s [Annotation source file] <column1> <column2>\n\n"
					+ "----------------------------------------------------\n");

			System.out.println("List of available annotators per category:\n\n" + printAnnotatorsPerType(
					configuredFreshAnnotators));

			return;
		}

		String annotatorName = (String) options.valueOf("annotator");
		if (!annotatorNames.contains(annotatorName))
		{
			System.out.println("Annotator must be one of the following: " + annotatorNames.toString());
			return;
		}

		Map<String, RepositoryAnnotator> annotators = applicationContext.getBeansOfType(RepositoryAnnotator.class);
		RepositoryAnnotator annotator = annotators.get(annotatorName);
		if (annotator == null) throw new Exception("Annotator unknown: " + annotatorName);

		if (!options.has(INPUT))
		{
			printInfo(annotator.getInfo());
			return;
		}

		File annotationSourceFile = (File) options.valueOf(SOURCE);
		if (!annotationSourceFile.exists())
		{
			System.out.println("Annotation source file or directory not found at " + annotationSourceFile);
			return;
		}

		File inputVcfFile = (File) options.valueOf("input");
		if (!inputVcfFile.exists())
		{
			System.out.println("Input VCF file not found at " + inputVcfFile);
			return;
		}
		else if (inputVcfFile.isDirectory())
		{
			System.out.println("Input VCF file is a directory, not a file!");
			return;
		}

		File outputVCFFile = (File) options.valueOf(OUTPUT);
		if (outputVCFFile.exists())
		{
			if (options.has(REPLACE))
			{
				System.out.println("Override enabled, replacing existing vcf with specified output: " + outputVCFFile
						.getAbsolutePath());
			}
			else
			{
				System.out.println(
						"Output file already exists, please either enter a different output name or use the '-r' option to overwrite the output file.");
				return;
			}
		}

		annotator.getCmdLineAnnotatorSettingsConfigurer().addSettings(annotationSourceFile.getAbsolutePath());
		annotate(annotator, vcfAttributes, entityTypeFactory, attributeFactory, effectStructureConverter, inputVcfFile,
				outputVCFFile, options);
	}

	/**
	 * Annotate VCF file
	 *
	 * @param annotator
	 * @param inputVcfFile
	 * @param outputVCFFile
	 * @param options       , the attributes of the annotator to include in the output vcf, if empty outputs all
	 * @throws Exception
	 */
	private void annotate(RepositoryAnnotator annotator, VcfAttributes vcfAttributes,
			EntityTypeFactory entityTypeFactory, AttributeFactory attributeFactory,
			EffectStructureConverter effectStructureConverter, File inputVcfFile, File outputVCFFile, OptionSet options)
			throws Exception
	{
		List<String> attributesToInclude = options.nonOptionArguments().stream().map(Object::toString)
				.collect(Collectors.toList());
		CmdLineAnnotatorUtils
				.annotate(annotator, vcfAttributes, entityTypeFactory, attributeFactory, effectStructureConverter,
						inputVcfFile, outputVCFFile, attributesToInclude, options.has("u"));
		if (options.has(VALIDATE))
		{
			System.out.println("Validating produced VCF file...");
			System.out.println(vcfValidator.validateVCF(outputVCFFile));
		}
		System.out.println("All done!");
	}

	public static void main(String[] args) throws Exception
	{
		configureLogging();

		OptionParser parser = createOptionParser();
		try
		{
			OptionSet options = parser.parse(args);

			// See http://stackoverflow.com/questions/4787719/spring-console-application-configured-using-annotations
			AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
			JOptCommandLinePropertySource propertySource = new JOptCommandLinePropertySource(options);

			ctx.getEnvironment().getPropertySources().addFirst(propertySource);
			ctx.register(CommandLineAnnotatorConfig.class);
			ctx.scan("org.molgenis.data.annotation.core", "org.molgenis.annotation.cmd");

			ctx.refresh();

			CmdLineAnnotator main = ctx.getBean(CmdLineAnnotator.class);

			main.run(options, parser);
			ctx.close();

		}
		catch (OptionException ex)
		{
			System.out.println(ex.getMessage());
		}

	}

	private static OptionParser createOptionParser()
	{
		OptionParser parser = new OptionParser();
		parser.acceptsAll(asList("i", INPUT), "Input VCF file").withRequiredArg().ofType(File.class);
		parser.acceptsAll(asList("a", ANNOTATOR), "Annotator name").requiredIf("input").withRequiredArg();
		parser.acceptsAll(asList("s", SOURCE), "Source file for the annotator").requiredIf("input").withRequiredArg()
				.ofType(File.class);
		parser.acceptsAll(asList("o", OUTPUT), "Output VCF file").requiredIf("input").withRequiredArg()
				.ofType(File.class);
		parser.acceptsAll(asList("v", VALIDATE), "Use VCF validator on the output file");
		parser.acceptsAll(asList("t", VCF_VALIDATOR_LOCATION),
				"Location of the vcf-validator executable from the vcf-tools suite").withRequiredArg()
				.ofType(String.class).defaultsTo(
				System.getProperty(USER_HOME) + File.separator + ".molgenis" + File.separator + "vcf-tools"
						+ File.separator + "bin" + File.separator + "vcf-validator");
		parser.acceptsAll(asList("h", HELP), "Prints this help text");
		parser.acceptsAll(asList("r", REPLACE),
				"Enables output file override, replacing a file with the same name as the argument for the -o option");
		parser.acceptsAll(asList("u", UPDATE_ANNOTATIONS),
				"Enables add/updating of annotations, i.e. CADD scores from a different source, by reusing existing annotations when no match was found.");

		return parser;
	}

	private void printInfo(AnnotatorInfo info)
	{
		System.out.println("*********************************************");
		System.out.println("  " + info.getCode());
		System.out.println("*********************************************");
		System.out.println("Description: " + info.getDescription());
		System.out.println("Type:        " + info.getType());
		System.out.println("Status:      " + info.getStatus());
		System.out.print("Attributes:  ");

		List<Attribute> attributes = info.getOutputAttributes();
		if (attributes.isEmpty())
		{
			System.out.println();
		}
		else
		{
			System.out.println(attributes.get(0).getName());
			for (int i = 1; i < attributes.size(); i++)
			{
				System.out.print("             ");
				System.out.println(attributes.get(i).getName());
			}
		}
	}

	private static void configureLogging()
	{
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

		PatternLayoutEncoder patternLayoutEncoder = new PatternLayoutEncoder();
		patternLayoutEncoder.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
		patternLayoutEncoder.setContext(loggerContext);
		patternLayoutEncoder.start();

		ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
		consoleAppender.setContext(loggerContext);
		consoleAppender.setEncoder(patternLayoutEncoder);
		consoleAppender.setName("STDOUT");
		consoleAppender.start();

		Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		rootLogger.addAppender(consoleAppender);
		rootLogger.setLevel(Level.WARN);

		Logger molgenisLogger = (Logger) LoggerFactory.getLogger("org.molgenis");
		molgenisLogger.addAppender(consoleAppender);
		molgenisLogger.setLevel(Level.INFO);
		molgenisLogger.setAdditive(false);
	}

	/**
	 * Helper function to print annotators per type
	 *
	 * @param annotators
	 * @return
	 */
	static String printAnnotatorsPerType(Map<String, RepositoryAnnotator> annotators)
	{
		Map<AnnotatorInfo.Type, List<String>> annotatorsPerType = new HashMap<>();
		for (String annotator : annotators.keySet())
		{
			AnnotatorInfo.Type type = annotators.get(annotator).getInfo().getType();
			if (annotatorsPerType.containsKey(type))
			{
				annotatorsPerType.get(type).add(annotator);
			}
			else
			{
				annotatorsPerType.put(type, new ArrayList<>(Arrays.asList(new String[] { annotator })));
			}

		}
		StringBuilder sb = new StringBuilder();
		for (AnnotatorInfo.Type type : annotatorsPerType.keySet())
		{
			sb.append("### ").append(type).append(" ###\n");
			for (String annotatorName : annotatorsPerType.get(type))
			{
				sb.append("* ").append(annotatorName).append("\n");
			}

			sb.append("\n");
		}

		return sb.toString();
	}

	/**
	 * Helper function to select the annotators that have received a recent brush up for the new way of configuring
	 *
	 * @param configuredAnnotators
	 * @return
	 */
	static HashMap<String, RepositoryAnnotator> getFreshAnnotators(
			Map<String, RepositoryAnnotator> configuredAnnotators)
	{
		HashMap<String, RepositoryAnnotator> configuredFreshAnnotators = new HashMap<>();
		configuredAnnotators.keySet().stream()
				.filter(annotator -> configuredAnnotators.get(annotator).getInfo() != null && configuredAnnotators
						.get(annotator).getInfo().getStatus().equals(AnnotatorInfo.Status.READY))
				.forEachOrdered(annotator ->
				{
					configuredFreshAnnotators.put(annotator, configuredAnnotators.get(annotator));
				});
		return configuredFreshAnnotators;
	}

}
