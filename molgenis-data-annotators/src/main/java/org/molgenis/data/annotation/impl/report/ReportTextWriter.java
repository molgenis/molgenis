package org.molgenis.data.annotation.impl.report;

import java.io.File;
import java.io.PrintWriter;

import org.molgenis.data.Entity;
import org.molgenis.data.annotation.entity.impl.CGDAnnotator;
import org.molgenis.data.vcf.VcfRepository;

public class ReportTextWriter implements ReportWriter
{
	private final Report report;
	private final File out;

	public ReportTextWriter(Report report, File out)
	{
		this.report = report;
		this.out = out;
	}

	@Override
	public void write() throws Exception
	{
		PrintWriter writer = new PrintWriter(out, "UTF-8");
		writer.println("Genome report");
		writer.println("");
		writer.println("Patient details");
		writer.println("_______________");
		writer.println("Name: " + report.getPatientName());
		writer.println("DOB: " + report.getPatientDOB());
		writer.println("Sex: " + report.getPatientSex());
		writer.println("Race: " + report.getPatientRace());
		writer.println("Indication of testing: " + report.getPatientIndication());
		writer.println("Test: " + report.getPatientTest());
		writer.println("MRN: " + report.getPatientMRN());
		writer.println("Specimen type: " + report.getPatientSpecimenType());
		writer.println("Specimen received: " + report.getPatientSpecimenRecieved());
		writer.println("AccessionID: " + report.getPatientAccessionID());
		writer.println("Family #: " + report.getPatientFamilyNr());
		writer.println("Referring physician: " + report.getPatientReferringPhysician());
		writer.println("Referring facility: " + report.getPatientReferringFacility());
		writer.println("");
		writer.println("Variant details");
		writer.println("_______________");
		for (String gene : report.getMonogenicDiseaseRiskGeneRanking().keySet())
		{
			writer.println(gene + ": " + report.getMonogenicDiseaseRiskGeneRanking().get(gene) + " stars");

			Entity variant = report.getMonogenicDiseaseRiskVariants().get(gene).get(0);

			writer.println(variant.getString("INFO_CGDCOND"));
			writer.println(variant
					.getString(CGDAnnotator.CGDAttributeName.GENERALIZED_INHERITANCE
							.getAttributeName()));

			for (Entity variantt : report.getMonogenicDiseaseRiskVariants().get(gene))
			{
				writer.print(variantt.get(VcfRepository.CHROM));
				writer.print(", ");
				writer.print(variantt.get(VcfRepository.POS));
				writer.print(", ");
				writer.print(variantt.get(VcfRepository.REF));
				writer.print(", ");
				writer.print(variantt.get(VcfRepository.ALT));
				writer.print(", ");
				writer.print(variantt.get(VcfRepository.FILTER));
				writer.print(", ");
				writer.print(variantt.get(VcfRepository.QUAL));
				writer.print(", ");
				writer.print(variantt.get("INFO_ANN").toString().split("\\|")[1]);
				writer.print(", ");
				writer.print(variantt.get("INFO_ANN").toString().split("\\|")[2]);
				writer.println();
			}
			writer.println();
		}

		writer.close();
	}
}
