package org.molgenis.data.annotation.cmd;

import java.io.File;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.entity.impl.CGDAnnotator;
import org.molgenis.data.annotation.entity.impl.CaddAnnotator;
import org.molgenis.data.annotation.entity.impl.ClinvarAnnotator;
import org.molgenis.data.annotation.entity.impl.DannAnnotator;
import org.molgenis.data.annotation.entity.impl.ExacAnnotator;
import org.molgenis.data.annotation.entity.impl.FitConAnnotator;
import org.molgenis.data.annotation.entity.impl.GoNLAnnotator;
import org.molgenis.data.annotation.entity.impl.SnpEffAnnotator;
import org.molgenis.data.annotation.entity.impl.ThousandGenomesAnnotator;
import org.molgenis.data.annotation.impl.HpoServiceAnnotator;
import org.molgenis.data.annotation.impl.MonogenicDiseaseCandidatesServiceAnnotator;
import org.molgenis.data.annotation.impl.PhenomizerServiceAnnotator;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.data.vcf.utils.VcfUtils;
import org.molgenis.framework.server.MolgenisSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

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
	private MolgenisSettings molgenisSettings;

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
		if (annotatorName.equals("cadd"))
		{
			molgenisSettings.setProperty(CaddAnnotator.CADD_FILE_LOCATION_PROPERTY,
					annotationSourceFile.getAbsolutePath());
			Map<String, RepositoryAnnotator> annotators = applicationContext.getBeansOfType(RepositoryAnnotator.class);
			RepositoryAnnotator annotator = annotators.get("cadd");
			annotate(annotator, inputVcfFile, outputVCFFile);
		}
		else if (annotatorName.equals("dann"))
		{
			molgenisSettings.setProperty(DannAnnotator.DANN_FILE_LOCATION_PROPERTY,
					annotationSourceFile.getAbsolutePath());
			Map<String, RepositoryAnnotator> annotators = applicationContext.getBeansOfType(RepositoryAnnotator.class);
			RepositoryAnnotator annotator = annotators.get("dann");
			annotate(annotator, inputVcfFile, outputVCFFile);
		}
		else if (annotatorName.equals("fitcon"))
		{
			molgenisSettings.setProperty(FitConAnnotator.FITCON_FILE_LOCATION_PROPERTY,
					annotationSourceFile.getAbsolutePath());
			Map<String, RepositoryAnnotator> annotators = applicationContext.getBeansOfType(RepositoryAnnotator.class);
			RepositoryAnnotator annotator = annotators.get("fitcon");
			annotate(annotator, inputVcfFile, outputVCFFile);
		}
		else if (annotatorName.equals("snpeff"))
		{
			molgenisSettings.setProperty(SnpEffAnnotator.SNPEFF_JAR_LOCATION_PROPERTY,
					annotationSourceFile.getAbsolutePath());
			Map<String, RepositoryAnnotator> annotators = applicationContext.getBeansOfType(RepositoryAnnotator.class);
			RepositoryAnnotator annotator = annotators.get("snpEff");
			annotate(annotator, inputVcfFile, outputVCFFile);
		}
		else if (annotatorName.equals("clinvar"))
		{
			molgenisSettings.setProperty(ClinvarAnnotator.CLINVAR_FILE_LOCATION_PROPERTY,
					annotationSourceFile.getAbsolutePath());
			Map<String, RepositoryAnnotator> annotators = applicationContext.getBeansOfType(RepositoryAnnotator.class);
			RepositoryAnnotator annotator = annotators.get("clinvar");
			annotate(annotator, inputVcfFile, outputVCFFile);
		}
		else if (annotatorName.equals("hpo"))
		{
			new HpoServiceAnnotator(annotationSourceFile, inputVcfFile, outputVCFFile);
		}
		else if (annotatorName.equals("monogenic"))
		{
			new MonogenicDiseaseCandidatesServiceAnnotator(annotationSourceFile, inputVcfFile, outputVCFFile);
		}
		else if (annotatorName.equals("phenomizer"))
		{
			new PhenomizerServiceAnnotator(annotationSourceFile, inputVcfFile, outputVCFFile);
		}
		else if (annotatorName.equals("exac"))
		{
			molgenisSettings.setProperty(ExacAnnotator.EXAC_FILE_LOCATION_PROPERTY,
					annotationSourceFile.getAbsolutePath());
			Map<String, RepositoryAnnotator> annotators = applicationContext.getBeansOfType(RepositoryAnnotator.class);
			RepositoryAnnotator annotator = annotators.get("exac");
			annotate(annotator, inputVcfFile, outputVCFFile);
		}
		else if (annotatorName.equals("gonl"))
		{
			molgenisSettings.setProperty(GoNLAnnotator.GONL_ROOT_DIRECTORY_PROPERTY, annotationSourceFile.getAbsolutePath());
			molgenisSettings.setProperty(GoNLAnnotator.GONL_FILE_PATTERN_PROPERTY, "gonl.chr%s.snps_indels.r5.vcf.gz");
			molgenisSettings.setProperty(GoNLAnnotator.GONL_OVERRIDE_CHROMOSOME_FILES_PROPERTY,
					"X:gonl.chrX.release4.gtc.vcf.gz");
			molgenisSettings.setProperty(GoNLAnnotator.GONL_CHROMOSOME_PROPERTY,
					"1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,X");

			Map<String, RepositoryAnnotator> annotators = applicationContext.getBeansOfType(RepositoryAnnotator.class);
			RepositoryAnnotator annotator = annotators.get("gonl");
			annotate(annotator, inputVcfFile, outputVCFFile);
		}
		else if (annotatorName.equals("cgd"))
		{
			molgenisSettings.setProperty(CGDAnnotator.CGD_FILE_LOCATION_PROPERTY,
					annotationSourceFile.getAbsolutePath());
			Map<String, RepositoryAnnotator> annotators = applicationContext.getBeansOfType(RepositoryAnnotator.class);
			RepositoryAnnotator annotator = annotators.get("cgd");
			annotate(annotator, inputVcfFile, outputVCFFile);
		}
		else if (annotatorName.equals("thousandGenomes"))
		{
			molgenisSettings.setProperty(ThousandGenomesAnnotator.THOUSAND_GENOME_FOLDER_PROPERTY,
					annotationSourceFile.getAbsolutePath());
			molgenisSettings.setProperty(ThousandGenomesAnnotator.THOUSAND_GENOME_FILE_PATTERN_PROPERTY,
					"ALL.chr%s.phase3_shapeit2_mvncall_integrated_v5.20130502.genotypes.vcf.gz");
			molgenisSettings.setProperty(ThousandGenomesAnnotator.THOUSAND_GENOME_CHROMOSOME_PROPERTY,
					"1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22");

			Map<String, RepositoryAnnotator> annotators = applicationContext.getBeansOfType(RepositoryAnnotator.class);
			RepositoryAnnotator annotator = annotators.get("thousandGenomes");
			annotate(annotator, inputVcfFile, outputVCFFile);
		}
		else
		{
			throw new Exception("Annotor unknown: " + annotatorName);
		}
	}

	public static void main(String[] args) throws Exception
	{
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
}
