package org.molgenis.data.annotation.cmd;

import java.io.File;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.molgenis.data.Entity;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.impl.ClinVarVCFServiceAnnotator;
import org.molgenis.data.annotation.impl.ClinicalGenomicsDatabaseServiceAnnotator;
import org.molgenis.data.annotation.impl.DeNovoAnnotator;
import org.molgenis.data.annotation.impl.GoNLServiceAnnotator;
import org.molgenis.data.annotation.impl.HpoServiceAnnotator;
import org.molgenis.data.annotation.impl.MonogenicDiseaseCandidatesServiceAnnotator;
import org.molgenis.data.annotation.impl.PhenomizerServiceAnnotator;
import org.molgenis.data.annotation.impl.SnpEffServiceAnnotator;
import org.molgenis.data.annotation.impl.ThousandGenomesServiceAnnotator;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.data.vcf.utils.VcfUtils;
import org.molgenis.framework.server.MolgenisSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class CmdLineAnnotator
{
	@Autowired
	private AnnotationService annotationService;

	@Autowired
	private MolgenisSettings molgenisSettings;

	@Autowired
	private ApplicationContext applicationContext;

	public void run(String[] args) throws Exception
	{
		List<String> annotatorNames = annotationService.getAllAnnotators().stream().map(a -> a.getSimpleName())
				.collect(Collectors.toList());

		if (args.length != 4)
		{
			System.out
					.println("Usage: java -Xmx4g -jar CmdLineAnnotator.jar [Annotator] [Annotation source file] [input VCF] [output VCF].\n"
							+ "Possible annotators are: "
							+ annotatorNames.toString()
							+ ".\n"
							+ "Example: java -Xmx4g -jar CmdLineAnnotator.jar gonl GoNL/release5_noContam_noChildren_with_AN_AC_GTC_stripped/ Cardio.vcf Cardio_gonl.vcf\n");
			return;
		}

		String annotatorName = args[0];
		if (!annotatorNames.contains(annotatorName))
		{
			System.out.println("Annotator must be one of the following: " + annotatorNames.toString());
			return;
		}

		// EntityAnnotator annotator = annotationService.getAnnotatorByName(annotatorName);

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
			System.out.println("Output VCF file already exists at " + outputVCFFile.getAbsolutePath());
			return;
		} 

		// engage!
		if (annotatorName.equals("cadd"))
		{
			Map<String, RepositoryAnnotator> annotators = applicationContext.getBeansOfType(RepositoryAnnotator.class);
			RepositoryAnnotator annotator = annotators.get("cadd");
			annotate(annotator, inputVcfFile, outputVCFFile);
		}
		else if (annotatorName.equals("snpeff"))
		{
			new SnpEffServiceAnnotator(annotationSourceFile, inputVcfFile, outputVCFFile);
		}
		else if (annotatorName.equals("clinvar"))
		{
			new ClinVarVCFServiceAnnotator(annotationSourceFile, inputVcfFile, outputVCFFile);
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
		else if (annotatorName.equals("denovo"))
		{
			new DeNovoAnnotator(annotationSourceFile, inputVcfFile, outputVCFFile);
		}
		else if (annotatorName.equals("exac"))
		{
			Map<String, RepositoryAnnotator> annotators = applicationContext.getBeansOfType(RepositoryAnnotator.class);
			RepositoryAnnotator annotator = annotators.get("exac");
			annotate(annotator, inputVcfFile, outputVCFFile);
		}
		else if (annotatorName.equals("1kg"))
		{
			new ThousandGenomesServiceAnnotator(annotationSourceFile, inputVcfFile, outputVCFFile);
		}
		else if (annotatorName.equals("gonl"))
		{
			new GoNLServiceAnnotator(annotationSourceFile, inputVcfFile, outputVCFFile);
		}
		else if (annotatorName.equals("cgd"))
		{
			new ClinicalGenomicsDatabaseServiceAnnotator(annotationSourceFile, inputVcfFile, outputVCFFile);
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
	}

	public void annotate(RepositoryAnnotator annotator, File inputVcfFile, File outputVCFFile) throws Exception
	{
		PrintWriter outputVCFWriter = new PrintWriter(outputVCFFile, "UTF-8");
		VcfRepository vcfRepo = new VcfRepository(inputVcfFile, this.getClass().getName());
		VcfUtils.checkPreviouslyAnnotatedAndAddMetadata(inputVcfFile, outputVCFWriter, annotator.getOutputMetaData(),
				annotator.getOutputMetaData().get(0).getName());
		System.out.println("Now starting to process the data.");
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
