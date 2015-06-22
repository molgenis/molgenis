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
import org.molgenis.data.annotation.impl.DannAnnotator;
import org.molgenis.data.annotation.impl.DeNovoAnnotator;
import org.molgenis.data.annotation.impl.ExACServiceAnnotator;
import org.molgenis.data.annotation.impl.FitconAnnotator;
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
			throw new Exception("Annotation source file or directory not found at " + annotationSourceFile);
		}

		File inputVcfFile = new File(args[2]);
		if (!inputVcfFile.exists())
		{
			throw new Exception("Input VCF file not found at " + inputVcfFile);
		}
		if (inputVcfFile.isDirectory())
		{
			throw new Exception("Input VCF file is a directory, not a file!");
		}

		File outputVCFFile = new File(args[3]);
		if (outputVCFFile.exists())
		{
			// TODO: do we make this an input options? or always throw? what is best practice?
			// throw new Exception("Output VCF file already exists at " + outputVCFFile.getAbsolutePath());
		}

		// TODO: What to put here?
		// molgenisSettings.setProperty(CADD_FILE_LOCATION_PROPERTY, annotationSourceFile.getAbsolutePath());

		PrintWriter outputVCFWriter = new PrintWriter(outputVCFFile, "UTF-8");

		VcfRepository vcfRepo = new VcfRepository(inputVcfFile, this.getClass().getName());
		Iterator<Entity> vcfIter = vcfRepo.iterator();

		// VcfUtils.checkPreviouslyAnnotatedAndAddMetadata(inputVcfFile, outputVCFWriter, infoFields, CADD_SCALED);

		System.out.println("Now starting to process the data.");

		while (vcfIter.hasNext())
		{
			Entity record = vcfIter.next();

			// TODO: this is not part of the interface, a RepositoryAnnotator will annotate entire repositories!
			List<Entity> annotatedRecord = null;// annotator.annotateEntity(record);

			if (annotatedRecord.size() > 1)
			{
				outputVCFWriter.close();
				vcfRepo.close();
				throw new Exception("Multiple outputs for " + record.toString());
			}
			else if (annotatedRecord.size() == 0)
			{
				outputVCFWriter.println(VcfUtils.convertToVCF(record));
			}
			else
			{
				outputVCFWriter.println(VcfUtils.convertToVCF(annotatedRecord.get(0)));
			}
		}
		outputVCFWriter.close();
		vcfRepo.close();
		System.out.println("All done!");

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
		else if (annotator.equals("dann"))
		{
			new DannAnnotator(annotationSourceFile, inputVcfFile, outputVCFFile);
		}
		else if (annotator.equals("fitcon"))
		{
			new FitconAnnotator(annotationSourceFile, inputVcfFile, outputVCFFile);
		}
		else if (annotatorName.equals("clinvar"))
		{
			new ClinVarVCFServiceAnnotator(annotationSourceFile, inputVcfFile, outputVCFFile);
		}
		else if (annotatorName.equals("hpo"))
		{
			new HpoServiceAnnotator(annotationSourceFile, inputVcfFile, outputVCFFile);
		}
		else if (annotatorName.equals("ase"))
		{
			// TODO
		}
		else if (annotatorName.equals("monogenic"))
		{
			new MonogenicDiseaseCandidatesServiceAnnotator(annotationSourceFile, inputVcfFile, outputVCFFile);
		}
		else if (annotatorName.equals("phenomizer"))
		{
			new PhenomizerServiceAnnotator(annotationSourceFile, inputVcfFile, outputVCFFile);
		}
		else if (annotatorName.equals("ccgg"))
		{
			// TODO
		}
		else if (annotatorName.equals("denovo"))
		{
			new DeNovoAnnotator(annotationSourceFile, inputVcfFile, outputVCFFile);
		}
		else if (annotatorName.equals("exac"))
		{
			new ExACServiceAnnotator(annotationSourceFile, inputVcfFile, outputVCFFile);
		}
		else if (annotatorName.equals("1kg"))
		{
			new ThousandGenomesServiceAnnotator(annotationSourceFile, inputVcfFile, outputVCFFile);
		}
		else if (annotatorName.equals("gonl"))
		{
			new GoNLServiceAnnotator(annotationSourceFile, inputVcfFile, outputVCFFile);
		}
		else if (annotatorName.equals("gwascatalog"))
		{
			// TODO
		}
		else if (annotatorName.equals("vkgl"))
		{
			// TODO
		}
		else if (annotatorName.equals("cgd"))
		{
			new ClinicalGenomicsDatabaseServiceAnnotator(annotationSourceFile, inputVcfFile, outputVCFFile);
		}
		else if (annotatorName.equals("enhancers"))
		{
			// TODO
		}
		else if (annotatorName.equals("proteinatlas"))
		{
			// TODO
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
