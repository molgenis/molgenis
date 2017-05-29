package org.molgenis.data.importer.emx;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

public enum EmxFileExtensions
{
	XLS("xls"), XLSX("xlsx"), CSV("csv"), TSV("tsv"), ZIP("zip");

	private String name;

	EmxFileExtensions(String name)
	{
		this.name = name;
	}

	public static Set<String> getEmx()
	{
		return ImmutableSet.of(XLS.toString(), XLSX.toString(), CSV.toString(), TSV.toString(), ZIP.toString());
	}

	@Override
	public String toString()
	{
		return this.name;
	}
}
