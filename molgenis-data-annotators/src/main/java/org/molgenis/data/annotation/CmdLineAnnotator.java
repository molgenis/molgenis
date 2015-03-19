package org.molgenis.data.annotation;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.molgenis.data.annotation.impl.CaddServiceAnnotator;

public class CmdLineAnnotator
{

	public static void main(String[] args) throws Exception
	{
		List<String> annotators = Arrays.asList(new String[]
		{ "cadd", "clinvar", "ase", "ccgg", "exac", "1kg", "gonl", "gwascatalog", "vkgl", "cgd", "enhancers",
				"proteinatlas" });

		if (args.length != 4)
		{
			throw new Exception(
					"Usage: java -Xmx4g -jar CmdLineAnnotator.jar [Annotator] [Annotation source file] [input VCF] [output VCF].\n");
		}

		String annotator = args[0];
		if (!annotators.contains(annotator))
		{
			System.out.println("Annotator must be one of the following: ");
			for (String ann : annotators)
			{
				System.out.print(ann + " ");
			}
			throw new Exception("Invalid annotator.");
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
			// TODO terugzetten!! throw new Exception("Output VCF file already exists at " +
			// outputVCFFile.getAbsolutePath());
		}

		// engage!
		if (annotator.equals("cadd"))
		{
			new CaddServiceAnnotator(annotationSourceFile, inputVcfFile, outputVCFFile);
		}
		else if (annotator.equals("clinvar"))
		{
			// TODO
		}
		else if (annotator.equals("ase"))
		{
			// TODO
		}
		else if (annotator.equals("ccgg"))
		{
			// TODO
		}
		else if (annotator.equals("exac"))
		{
			// TODO
		}
		else if (annotator.equals("1kg"))
		{
			// TODO
		}
		else if (annotator.equals("gonl"))
		{
			// TODO
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
			// TODO
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
