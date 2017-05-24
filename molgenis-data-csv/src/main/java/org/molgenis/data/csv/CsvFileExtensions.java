package org.molgenis.data.csv;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

public enum CsvFileExtensions
{
	CSV("csv"), TXT("txt"), TSV("tsv"), ZIP("zip");

	private String name;

	CsvFileExtensions(String name)
	{
		this.name = name;
	}

	public static Set<String> getCSV()
	{
		return ImmutableSet.of(CSV.toString(), TXT.toString(), TSV.toString(), ZIP.toString());
	}

	@Override
	public String toString()
	{
		return this.name;
	}
}
