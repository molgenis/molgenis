package org.molgenis.data.annotation.cmd;

import static java.util.Arrays.asList;

import java.io.File;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.entity.AnnotatorInfo;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.data.vcf.utils.VcfUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.JOptCommandLinePropertySource;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;

/**
 * 
 * Build JAR file...............: mvn clean install -pl molgenis-data-annotators/ -am -DskipTests -P create-delivery
 * Run..........................: java -jar molgenis-data-annotators/target/CmdLineAnnotator.jar
 * 
 */
public class CmdLineAnnotator
{
	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	VcfValidator vcfValidator;

	// Default settings for running vcf-validator
	public void run(OptionSet options, OptionParser parser) throws Exception
	{
		Map<String, RepositoryAnnotator> configuredAnnotators = applicationContext
				.getBeansOfType(RepositoryAnnotator.class);

		// for now, only get the annotators that have recieved a recent brush up for the new way of configuring
		Map<String, RepositoryAnnotator> configuredFreshAnnotators = CommandLineAnnotatorConfig
				.getFreshAnnotators(configuredAnnotators);

		Set<String> annotatorNames = configuredFreshAnnotators.keySet();

		if (!options.has("annotator") || options.has("help"))
		{

			String implementationVersion = getClass().getPackage().getImplementationVersion();
			if (implementationVersion == null)
			{
				implementationVersion = "";
			}

			System.out
					.println("\n"
							+ "****************************************************\n"
							+ "* MOLGENIS Annotator, commandline interface "
							+ implementationVersion
							+ " *\n"
							+ "****************************************************\n"
							+ "Typical usage to annotate a VCF file:\n\n"
							+ "java -jar CmdLineAnnotator.jar [options] [attribute names]\n"
							+ "Example: java -Xmx4g -jar CmdLineAnnotator.jar -v -a gonl -s GoNL/release5_noContam_noChildren_with_AN_AC_GTC_stripped/ -i Cardio.vcf -o Cardio_gonl.vcf GoNL_GTC GoNL_AF\n"
							+ "\n" + "----------------------------------------------------\n\n"
							+ "Available options:\n");

			parser.printHelpOn(System.out);

			System.out
					.println("\n"
							+ "----------------------------------------------------\n\n"
							+ "To get detailed description for a specific annotator:\n"
							+ "java -jar CmdLineAnnotator.jar -a [Annotator]\n\n"
							+ "To select only a few columns from an annotation source instead of everything, use:\n"
							+ "java -jar CmdLineAnnotator.jar -a [Annotator] -s [Annotation source file] <column1> <column2>\n\n"
							+ "----------------------------------------------------\n");

			System.out.println("List of available annotators per category:\n\n"
					+ CommandLineAnnotatorConfig.printAnnotatorsPerType(configuredFreshAnnotators));

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

		if (!options.has("input"))
		{
			printInfo(annotator.getInfo());
			return;
		}

		File annotationSourceFile = (File) options.valueOf("source");
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

		File outputVCFFile = (File) options.valueOf("output");
		if (outputVCFFile.exists())
		{
			if (options.has("replace"))
			{
				System.out.println("Override enabled, replacing existing vcf with specified output: "
						+ outputVCFFile.getAbsolutePath());
			}
			else
			{
				System.out.println("Output file already exists, please either enter a different output name or use the '-r' option to overwrite the output file.");
				return;
			}
		}

		annotator.getCmdLineAnnotatorSettingsConfigurer().addSettings(annotationSourceFile.getAbsolutePath());
		annotate(annotator, inputVcfFile, outputVCFFile, options);
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
			ctx.scan("org.molgenis.data.annotation", "org.molgenis.data.annotation.cmd");
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

	protected static OptionParser createOptionParser()
	{
		OptionParser parser = new OptionParser();
		parser.acceptsAll(asList("i", "input"), "Input VCF file").withRequiredArg().ofType(File.class);
		parser.acceptsAll(asList("a", "annotator"), "Annotator name").requiredIf("input").withRequiredArg();
		parser.acceptsAll(asList("s", "source"), "Source file for the annotator").requiredIf("input").withRequiredArg()
				.ofType(File.class);
		parser.acceptsAll(asList("o", "output"), "Output VCF file").requiredIf("input").withRequiredArg()
				.ofType(File.class);
		parser.acceptsAll(asList("v", "validate"), "Use VCF validator on the output file");
		parser.acceptsAll(asList("t", "vcf-validator-location"),
				"Location of the vcf-validator executable from the vcf-tools suite")
				.withRequiredArg()
				.ofType(String.class)
				.defaultsTo(
						System.getProperty("user.home") + File.separator + ".molgenis" + File.separator + "vcf-tools"
								+ File.separator + "bin" + File.separator + "vcf-validator");
		parser.acceptsAll(asList("h", "help"), "Prints this help text");
		parser.acceptsAll(asList("r", "replace"),
				"Enables output file override, replacing a file with the same name as the argument for the -o option");

		return parser;
	}

	/**
	 * Annotate VCF file
	 * 
	 * 
	 * @param annotator
	 * @param inputVcfFile
	 * @param outputVCFFile
	 * @param options
	 *            , the attributes of the annotator to include in the output vcf, if empty outputs all
	 * @throws Exception
	 */
	public void annotate(RepositoryAnnotator annotator, File inputVcfFile, File outputVCFFile, OptionSet options)
			throws Exception
	{
		List<String> attributesToInclude = options.nonOptionArguments().stream().map(Object::toString)
				.collect(Collectors.toList());
		PrintWriter outputVCFWriter = new PrintWriter(outputVCFFile, "UTF-8");
		VcfRepository vcfRepo = new VcfRepository(inputVcfFile, this.getClass().getName());

		try
		{
			if (!attributesToInclude.isEmpty())
			{
				// Check attribute names
				List<String> outputAttributeNames = VcfUtils.getAtomicAttributesFromList(annotator.getOutputMetaData())
						.stream().map((attr) -> attr.getName()).collect(Collectors.toList());

				boolean stop = false;
				for (Object attrName : attributesToInclude)
				{
					if (!outputAttributeNames.contains(attrName))
					{
						System.out.println("Unknown output attribute '" + attrName + "'");
						stop = true;
					}
				}
				if (stop) return;

				// Include the original attributes
				vcfRepo.getEntityMetaData().getAtomicAttributes()
						.forEach((attr) -> attributesToInclude.add(attr.getName()));
			}

			VcfUtils.checkPreviouslyAnnotatedAndAddMetadata(inputVcfFile, outputVCFWriter,
					annotator.getOutputMetaData(), attributesToInclude);
			System.out.println("Now starting to process the data.");

			DefaultEntityMetaData emd = (DefaultEntityMetaData) vcfRepo.getEntityMetaData();
			DefaultAttributeMetaData infoAttribute = (DefaultAttributeMetaData) emd.getAttribute(VcfRepository.INFO);
			for (AttributeMetaData attribute : annotator.getOutputMetaData())
			{
				for (AttributeMetaData atomicAttribute : attribute.getAttributeParts())
				{
					infoAttribute.addAttributePart(atomicAttribute);
				}
			}

			Iterator<Entity> annotatedRecords = annotator.annotate(vcfRepo);
			while (annotatedRecords.hasNext())
			{
				Entity annotatedRecord = annotatedRecords.next();
				outputVCFWriter.println(VcfUtils.convertToVCF(annotatedRecord, attributesToInclude));
			}
		}
		finally
		{
			outputVCFWriter.close();

			vcfRepo.close();
		}
		if (options.has("validate"))
		{
			System.out.println("Validating produced VCF file...");
			System.out.println(vcfValidator.validateVCF(outputVCFFile));
		}
		System.out.println("All done!");
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

		List<AttributeMetaData> attributes = info.getOutputAttributes();
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
}
