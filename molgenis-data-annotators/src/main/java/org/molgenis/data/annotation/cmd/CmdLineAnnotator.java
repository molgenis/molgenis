package org.molgenis.data.annotation.cmd;

import java.io.File;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.data.vcf.utils.VcfUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

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
@Component
public class CmdLineAnnotator
{
	@Autowired
	private ApplicationContext applicationContext;

	public void run(String[] args) throws Exception
	{
		Map<String, RepositoryAnnotator> configuredAnnotators = applicationContext
				.getBeansOfType(RepositoryAnnotator.class);

		// for now, only get the annotators that have recieved a recent brush up for the new way of configuring
		Map<String, RepositoryAnnotator> configuredFreshAnnotators = CommandLineAnnotatorConfig
				.getFreshAnnotators(configuredAnnotators);

		Set<String> annotatorNames = configuredFreshAnnotators.keySet();

		if (args.length != 4)
		{
			System.out
					.println("\n"
							+ "*********************************************\n"
							+ "* MOLGENIS Annotator, commandline interface *\n"
							+ "*********************************************\n"
							+ "\n"
							+ "Typical usage to annotate a VCF file:\n"
							+ "\tjava -jar CmdLineAnnotator.jar [Annotator] [Annotation source file] [input VCF] [output VCF].\n"
							+ "\tExample: java -Xmx4g -jar CmdLineAnnotator.jar gonl GoNL/release5_noContam_noChildren_with_AN_AC_GTC_stripped/ Cardio.vcf Cardio_gonl.vcf\n"
							+ "\n"
							+ "Help:\n"
							+ "\tTo get a detailed description and installation instructions for a specific annotator:\n"
							+ "\t\tjava -jar CmdLineAnnotator.jar [Annotator]\n"
							+ "\tTo check if an annotator is ready for use:\n"
							+ "\t\tjava -jar CmdLineAnnotator.jar [Annotator] [Annotation source file]\n" + "\n"
							+ "Currently available annotators are:\n" + "\t" + annotatorNames.toString() + "\n"
							+ "Breakdown per category:\n"
							+ CommandLineAnnotatorConfig.printAnnotatorsPerType(configuredFreshAnnotators));
			return;
		}

		String annotatorName = args[0];
		if (!annotatorNames.contains(annotatorName))
		{
			System.out.println("Annotator must be one of the following: " + annotatorNames.toString());
			return;
		}

		File annotationSourceFile = new File(args[1]);
		if (!annotationSourceFile.exists())
		{
			System.out.println("Annotation source file or directory not found at " + annotationSourceFile);
			return;
		}

		File inputVcfFile = new File(args[2]);
		if (!inputVcfFile.exists())
		{
			System.out.println("Input VCF file not found at " + inputVcfFile);
			return;
		}
		if (inputVcfFile.isDirectory())
		{
			System.out.println("Input VCF file is a directory, not a file!");
			return;
		}

		File outputVCFFile = new File(args[3]);
		if (outputVCFFile.exists())
		{
			System.out.println("WARNING: Output VCF file already exists at " + outputVCFFile.getAbsolutePath());
		}

		// engage!
		Map<String, RepositoryAnnotator> annotators = applicationContext.getBeansOfType(RepositoryAnnotator.class);
		RepositoryAnnotator annotator = annotators.get(annotatorName);
		if (annotator == null) throw new Exception("Annotator unknown: " + annotatorName);

		annotator.getCmdLineAnnotatorSettingsConfigurer().addSettings(annotationSourceFile.getAbsolutePath());
		annotate(annotator, inputVcfFile, outputVCFFile);
	}

	public static void main(String[] args) throws Exception
	{
		configureLogging();

		// See http://stackoverflow.com/questions/4787719/spring-console-application-configured-using-annotations
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext("org.molgenis.data.annotation");
		CmdLineAnnotator main = ctx.getBean(CmdLineAnnotator.class);
		main.run(args);
		ctx.close();
	}

	public void annotate(RepositoryAnnotator annotator, File inputVcfFile, File outputVCFFile) throws Exception
	{
		PrintWriter outputVCFWriter = new PrintWriter(outputVCFFile, "UTF-8");
		VcfRepository vcfRepo = new VcfRepository(inputVcfFile, this.getClass().getName());
		VcfUtils.checkPreviouslyAnnotatedAndAddMetadata(inputVcfFile, outputVCFWriter, annotator.getOutputMetaData(),
				annotator.getOutputMetaData().get(0).getName());
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
			outputVCFWriter.println(VcfUtils.convertToVCF(annotatedRecord));
		}
		outputVCFWriter.close();
		vcfRepo.close();
		System.out.println("All done!");
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
