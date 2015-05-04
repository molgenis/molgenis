package org.molgenis.data.annotation.impl.report;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class Reporter
{

	/**
	 * Create report based on input file
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception
	{
		List<String> outputformats = Arrays.asList(new String[]
		{ "txt" });

		File inputVcfFile = new File(args[0]);
		if (!inputVcfFile.exists())
		{
			throw new Exception("Input VCF file not found at " + inputVcfFile);
		}
		if (inputVcfFile.isDirectory())
		{
			throw new Exception("Input VCF file is a directory, not a file!");
		}

		File outputFile = new File(args[2]);
		if (outputFile.exists())
		{
			// TODO: do we make this an input options? or always throw? what is best practice?
			throw new Exception("Output file already exists at " + outputFile.getAbsolutePath());
		}

		String outputFormat = args[1];
		if (outputformats.contains(outputFormat))
		{
			System.out.println("Output format must be one of the following: ");
			for (String format : outputformats)
			{
				System.out.print(format + " ");
			}
			throw new Exception("\nInvalid format.\n" + "Possible formats are: " + outputformats.toString() + ".");
		}

		// create report
		ReportFactory cr = new ReportFactory(inputVcfFile);
		Report report = cr.createReport();

		// write report
		if (outputFormat.equals("txt"))
		{
			ReportTextWriter r = new ReportTextWriter(report, outputFile);
			r.write();

		}
		else
		{
			// should not be reachable
			throw new Exception("Unsupported output format: " + outputFormat);
		}
	}
}
