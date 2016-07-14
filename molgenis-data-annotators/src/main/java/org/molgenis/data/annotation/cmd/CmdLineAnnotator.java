package org.molgenis.data.annotation.cmd;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisInvalidFormatException;
import org.molgenis.data.annotation.core.EffectsAnnotator;
import org.molgenis.data.annotation.core.RefEntityAnnotator;
import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.annotation.core.entity.AnnotatorConfig;
import org.molgenis.data.annotation.core.entity.AnnotatorInfo;
import org.molgenis.data.annotation.core.utils.AnnotatorUtils;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.molgenis.data.vcf.utils.VcfUtils;
import org.molgenis.data.vcf.utils.VcfWriterUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.JOptCommandLinePropertySource;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.molgenis.MolgenisFieldTypes.AttributeType.MREF;

/**
 * Build JAR file...............: mvn clean install -pl molgenis-data-annotators/ -am -DskipTests -P create-delivery
 * Run..........................: java -jar molgenis-data-annotators/target/CmdLineAnnotator.jar
 */
public class CmdLineAnnotator
{
	private static final String EFFECT = "EFFECT";

	@Autowired
	CommandLineAnnotatorConfig commandLineAnnotatorConfig;

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	VcfValidator vcfValidator;

	@Autowired
	VcfAttributes vcfAttributes;

	@Autowired
	VcfUtils vcfUtils;

	@Autowired
	EntityMetaDataFactory entityMetaDataFactory;

	@Autowired
	AttributeMetaDataFactory attributeMetaDataFactory;

	// Default settings for running vcf-validator
	private void run(OptionSet options, OptionParser parser) throws Exception
	{
		Map<String, AnnotatorConfig> annotatorMap = applicationContext.getBeansOfType(AnnotatorConfig.class);
		annotatorMap.values().forEach(AnnotatorConfig::init);

		Map<String, RepositoryAnnotator> configuredAnnotators = applicationContext
				.getBeansOfType(RepositoryAnnotator.class);

		// for now, only get the annotators that have recieved a recent brush up for the new way of configuring
		Map<String, RepositoryAnnotator> configuredFreshAnnotators = getFreshAnnotators(configuredAnnotators);

		Set<String> annotatorNames = configuredFreshAnnotators.keySet();

		if (!options.has("annotator") || options.has("help"))
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
			ctx.scan("org.molgenis.data.annotation.core", "org.molgenis.data.annotation.cmd");

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
		parser.acceptsAll(asList("i", "input"), "Input VCF file").withRequiredArg().ofType(File.class);
		parser.acceptsAll(asList("a", "annotator"), "Annotator name").requiredIf("input").withRequiredArg();
		parser.acceptsAll(asList("s", "source"), "Source file for the annotator").requiredIf("input").withRequiredArg()
				.ofType(File.class);
		parser.acceptsAll(asList("o", "output"), "Output VCF file").requiredIf("input").withRequiredArg()
				.ofType(File.class);
		parser.acceptsAll(asList("v", "validate"), "Use VCF validator on the output file");
		parser.acceptsAll(asList("t", "vcf-validator-location"),
				"Location of the vcf-validator executable from the vcf-tools suite").withRequiredArg()
				.ofType(String.class).defaultsTo(
				System.getProperty("user.home") + File.separator + ".molgenis" + File.separator + "vcf-tools"
						+ File.separator + "bin" + File.separator + "vcf-validator");
		parser.acceptsAll(asList("h", "help"), "Prints this help text");
		parser.acceptsAll(asList("r", "replace"),
				"Enables output file override, replacing a file with the same name as the argument for the -o option");
		parser.acceptsAll(asList("u", "update-annotations"),
				"Enables add/updating of annotations, i.e. CADD scores from a different source, by reusing existing annotations when no match was found.");

		return parser;
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
	private void annotate(RepositoryAnnotator annotator, File inputVcfFile, File outputVCFFile, OptionSet options)
			throws Exception
	{
		List<String> attributesToInclude = options.nonOptionArguments().stream().map(Object::toString)
				.collect(Collectors.toList());
		annotate(annotator, inputVcfFile, outputVCFFile, attributesToInclude, options.has("validate"),
				options.has("u"));
	}

	public void annotate(RepositoryAnnotator annotator, File inputVcfFile, File outputVCFFile,
			List<String> attributesToInclude, boolean validate, boolean update)
			throws IOException, MolgenisInvalidFormatException
	{

		try (BufferedWriter outputVCFWriter = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(outputVCFFile), UTF_8));
				VcfRepository vcfRepo = new VcfRepository(inputVcfFile, inputVcfFile.getName(), vcfAttributes,
						entityMetaDataFactory, attributeMetaDataFactory))
		{
			if (!attributesToInclude.isEmpty())
			{
				// Check attribute names
				List<String> outputAttributeNames = VcfUtils
						.getAtomicAttributesFromList(annotator.getOutputAttributes()).stream()
						.map(AttributeMetaData::getName).collect(Collectors.toList());

				List<String> inputAttributeNames = VcfUtils
						.getAtomicAttributesFromList(vcfRepo.getEntityMetaData().getAtomicAttributes()).stream()
						.map(AttributeMetaData::getName).collect(Collectors.toList());

				boolean stop = false;
				for (Object attrName : attributesToInclude)
				{
					if (!outputAttributeNames.contains(attrName))
					{
						System.out.println("Unknown output attribute '" + attrName + "'");
						stop = true;
					}
					else if (inputAttributeNames.contains(attrName))
					{
						System.out.println("The output attribute '" + attrName
								+ "' is present in the inputfile, but is deselected in the current run, this is not supported");
						stop = true;
					}
				}
				if (stop) return;
			}

			// If the annotator e.g. SnpEff creates an external repository, collect the output metadata into an mref
			// entity
			// This allows for the header to be written as 'EFFECT annotations: <ouput_attributes> | <ouput_attributes>'
			List<AttributeMetaData> outputMetaData = newArrayList();
			if (annotator instanceof RefEntityAnnotator || annotator instanceof EffectsAnnotator)
			{
				EntityMetaData effectRefEntity = entityMetaDataFactory.create()
						.setName(annotator.getSimpleName() + "_EFFECTS");
				for (AttributeMetaData outputAttribute : annotator.getOutputAttributes())
				{
					effectRefEntity.addAttribute(outputAttribute);
				}
				AttributeMetaData effect = attributeMetaDataFactory.create().setName(EFFECT);
				effect.setDataType(MREF).setRefEntity(effectRefEntity);
				outputMetaData.add(effect);
			}
			else
			{
				outputMetaData = annotator.getOutputAttributes();
			}

			VcfWriterUtils
					.writeVcfHeader(inputVcfFile, outputVCFWriter, VcfUtils.getAtomicAttributesFromList(outputMetaData),
							attributesToInclude);
			System.out.println("Now starting to process the data.");

			EntityMetaData emd = vcfRepo.getEntityMetaData();
			AttributeMetaData infoAttribute = emd.getAttribute(VcfAttributes.INFO);
			for (AttributeMetaData attribute : annotator.getOutputAttributes())
			{
				for (AttributeMetaData atomicAttribute : attribute.getAttributeParts())
				{
					infoAttribute.addAttributePart(atomicAttribute);
				}
			}
			Iterable<Entity> entitiesToAnnotate;
			EntityMetaData newMetaData = AnnotatorUtils
					.addAnnotatorMetadataToRepositories(vcfRepo.getEntityMetaData(), annotator.getOutputAttributes());
			if (annotator instanceof EffectsAnnotator)
			{
				entitiesToAnnotate = vcfUtils.createEntityStructureForVcf(newMetaData, EFFECT,
						vcfRepo.findAll(new QueryImpl<>()));
			}
			else
			{
				entitiesToAnnotate = vcfRepo;
			}
			Iterator<Entity> annotatedRecords = annotator.annotate(entitiesToAnnotate, update);

			if (annotator instanceof RefEntityAnnotator || annotator instanceof EffectsAnnotator)
			{
				annotatedRecords = vcfUtils.reverseXrefMrefRelation(annotatedRecords);
			}

			while (annotatedRecords.hasNext())
			{
				Entity annotatedRecord = annotatedRecords.next();
				VcfWriterUtils.writeToVcf(annotatedRecord, VcfUtils.getAtomicAttributesFromList(outputMetaData),
						attributesToInclude, outputVCFWriter);
				outputVCFWriter.newLine();
			}

		}
		if (validate)
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
				annotatorsPerType.put(type, new ArrayList<String>(Arrays.asList(new String[] { annotator })));
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

	/**
	 * Helper function to select the annotators that have received a recent brush up for the new way of configuring
	 *
	 * @param configuredAnnotators
	 * @return
	 */
	static HashMap<String, RepositoryAnnotator> getFreshAnnotators(
			Map<String, RepositoryAnnotator> configuredAnnotators)
	{
		HashMap<String, RepositoryAnnotator> configuredFreshAnnotators = new HashMap<String, RepositoryAnnotator>();
		for (String annotator : configuredAnnotators.keySet())
		{
			if (configuredAnnotators.get(annotator).getInfo() != null && configuredAnnotators.get(annotator).getInfo()
					.getStatus().equals(AnnotatorInfo.Status.READY))
			{
				configuredFreshAnnotators.put(annotator, configuredAnnotators.get(annotator));
			}
		}
		return configuredFreshAnnotators;
	}

}
