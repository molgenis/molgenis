package org.molgenis.data.support;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

public enum GenericImporterExtensions
{
	// CSV
	CSV("csv"), TXT("txt"), TSV("tsv"), ZIP("zip"),

	// Excel
	XLS("xls"), XLSX("xlsx"),

	// Ontology
	OBO_ZIP("obo.zip"), OWL_ZIP("owl.zip"),

	// VCF
	VCF("vcf"), VCF_GZ("vcf.gz"), VCF_ZIP("vcf.zip");

	private String name;

	GenericImporterExtensions(String name)
	{
		this.name = name;
	}

	@Override
	public String toString()
	{
		return this.name;
	}

	public static Set<String> getOntology()
	{
		return ImmutableSet.of(OBO_ZIP.toString(), OWL_ZIP.toString());
	}

	public static Set<String> getCSV()
	{
		return ImmutableSet.of(CSV.toString(), TXT.toString(), TSV.toString(), ZIP.toString());
	}

	public static Set<String> getExcel()
	{
		return ImmutableSet.of(XLS.toString(), XLSX.toString());
	}

	public static Set<String> getVCF()
	{
		return ImmutableSet.of(VCF.toString(), VCF_GZ.toString(), VCF_ZIP.toString());
	}

	public static Set<String> getJPA()
	{
		return ImmutableSet.of(XLS.toString(), XLSX.toString(), CSV.toString(), ZIP.toString());
	}

	public static Set<String> getEMX()
	{
		return ImmutableSet.of(XLS.toString(), XLSX.toString(), CSV.toString(), TSV.toString(), ZIP.toString());
	}

	public static Set<String> getAll()
	{
		return ImmutableSet
				.of(CSV.toString(), TXT.toString(), TSV.toString(), ZIP.toString(), XLS.toString(), XLSX.toString(),
						OBO_ZIP.toString(), OWL_ZIP.toString(), VCF.toString(), VCF_GZ.toString(), VCF_ZIP.toString());
	}
}