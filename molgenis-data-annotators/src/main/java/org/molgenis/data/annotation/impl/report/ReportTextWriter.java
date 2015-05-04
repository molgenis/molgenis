package org.molgenis.data.annotation.impl.report;

import java.io.File;
import java.io.PrintWriter;

public class ReportTextWriter implements ReportWriter
{
	private Report report;
	private File out;

	public ReportTextWriter(Report report, File out)
	{
		this.report = report;
		this.out = out;
	}

	public void write() throws Exception
	{
		PrintWriter writer = new PrintWriter(out, "UTF-8");
		writer.println("Genome report");

		for (String gene : report.getMonogenicDiseaseRiskGeneRanking().keySet())
		{
			writer.print(gene + "\t" + report.getMonogenicDiseaseRiskGeneRanking().get(gene));
		}

		writer.close();
	}
}
