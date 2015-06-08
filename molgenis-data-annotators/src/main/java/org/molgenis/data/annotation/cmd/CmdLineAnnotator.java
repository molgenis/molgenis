package org.molgenis.data.annotation.cmd;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.molgenis.data.annotation.impl.CaddServiceAnnotator;
import org.molgenis.data.annotation.impl.ClinVarVCFServiceAnnotator;
import org.molgenis.data.annotation.impl.ClinicalGenomicsDatabaseServiceAnnotator;
import org.molgenis.data.annotation.impl.DeNovoAnnotator;
import org.molgenis.data.annotation.impl.ExACServiceAnnotator;
import org.molgenis.data.annotation.impl.GoNLServiceAnnotator;
import org.molgenis.data.annotation.impl.HpoServiceAnnotator;
import org.molgenis.data.annotation.impl.MonogenicDiseaseCandidatesServiceAnnotator;
import org.molgenis.data.annotation.impl.PhenomizerServiceAnnotator;
import org.molgenis.data.annotation.impl.SnpEffServiceAnnotator;
import org.molgenis.data.annotation.impl.ThousandGenomesServiceAnnotator;

public class CmdLineAnnotator
{

	public static void main(String[] args) throws Exception
	{
		List<String> annotators = Arrays.asList(new String[]
		{ "cadd", "snpeff", "clinvar", "hpo", "ase", "monogenic", "phenomizer", "ccgg", "denovo", "exac", "1kg",
				"gonl", "gwascatalog", "vkgl", "cgd", "enhancers", "proteinatlas" });

		if (args.length != 4)
		{
			throw new Exception(
					"Usage: java -Xmx4g -jar CmdLineAnnotator.jar [Annotator] [Annotation source file] [input VCF] [output VCF].\n"
							+ "Possible annotators are: "
							+ annotators.toString()
							+ ".\n"
							+ "Example: java -Xmx4g -jar CmdLineAnnotator.jar gonl GoNL/release5_noContam_noChildren_with_AN_AC_GTC_stripped/ Cardio.vcf Cardio_gonl.vcf\n");
		}

		String annotator = args[0];
		if (!annotators.contains(annotator))
		{
			System.out.println("Annotator must be one of the following: ");
			for (String ann : annotators)
			{
				System.out.print(ann + " ");
			}
			throw new Exception("\nInvalid annotator.\n" + "Possible annotators are: " + annotators.toString() + ".");
		}

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

		// engage!
		if (annotator.equals("cadd"))
		{
			new CaddServiceAnnotator(annotationSourceFile, inputVcfFile, outputVCFFile);
		}
		else if (annotator.equals("snpeff"))
		{
			new SnpEffServiceAnnotator(annotationSourceFile, inputVcfFile, outputVCFFile);
		}
		else if (annotator.equals("clinvar"))
		{
			new ClinVarVCFServiceAnnotator(annotationSourceFile, inputVcfFile, outputVCFFile);
		}
		else if (annotator.equals("hpo"))
		{
			new HpoServiceAnnotator(annotationSourceFile, inputVcfFile, outputVCFFile);
		}
		else if (annotator.equals("ase"))
		{
			// TODO
		}
		else if (annotator.equals("monogenic"))
		{
			new MonogenicDiseaseCandidatesServiceAnnotator(annotationSourceFile, inputVcfFile, outputVCFFile);
		}
		else if (annotator.equals("phenomizer"))
		{
			new PhenomizerServiceAnnotator(annotationSourceFile, inputVcfFile, outputVCFFile);
		}
		else if (annotator.equals("ccgg"))
		{
			// TODO
		}
		else if (annotator.equals("denovo"))
		{
			new DeNovoAnnotator(annotationSourceFile, inputVcfFile, outputVCFFile);
		}
		else if (annotator.equals("exac"))
		{
			new ExACServiceAnnotator(annotationSourceFile, inputVcfFile, outputVCFFile);
		}
		else if (annotator.equals("1kg"))
		{
			new ThousandGenomesServiceAnnotator(annotationSourceFile, inputVcfFile, outputVCFFile);
		}
		else if (annotator.equals("gonl"))
		{
			new GoNLServiceAnnotator(annotationSourceFile, inputVcfFile, outputVCFFile);
		}
		else if (annotator.equals("gwascatalog"))
		{
			// TODO
		}
		else if (annotator.equals("vkgl"))
		{
			// TODO
		}
		else if (annotator.equals("cgd"))
		{
			new ClinicalGenomicsDatabaseServiceAnnotator(annotationSourceFile, inputVcfFile, outputVCFFile);
		}
		else if (annotator.equals("enhancers"))
		{
			// TODO
		}
		else if (annotator.equals("proteinatlas"))
		{
			// TODO
		}
		else
		{
			throw new Exception("Annotor unknown: " + annotator);
		}

	}

}
