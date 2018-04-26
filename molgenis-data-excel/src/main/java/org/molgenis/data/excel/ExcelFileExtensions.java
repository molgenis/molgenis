package org.molgenis.data.excel;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

public enum ExcelFileExtensions
{
	XLS("xls"), XLSX("xlsx");

	private String name;

	ExcelFileExtensions(String name)
	{
		this.name = name;
	}

	public static Set<String> getExcel()
	{
		return ImmutableSet.of(XLS.toString(), XLSX.toString());
	}

	@Override
	public String toString()
	{
		return this.name;
	}
}
