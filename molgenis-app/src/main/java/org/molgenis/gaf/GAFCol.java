package org.molgenis.gaf;

import java.util.ArrayList;
import java.util.List;

public enum GAFCol
{
	INTERNAL_SAMPLE_ID("internalSampleID"), LANE("lane"), SEQUENCER("sequencer"), SEQUENCING_START_DATE(
			"sequencingStartDate"), RUN("run"), FLOWCELL("flowcell"), SEQ_TYPE("seqType"), BARCODE_1("Barcode_1"), EXTERNAL_SAMPLE_ID(
			"externalSampleID"), PROJECT("project"), CONTACT("contact"), SAMPLE_TYPE("Sample_Type"), ARRAY_FILE(
			"arrayFile"), ARRAY_ID("arrayID"), CAPTURING_KIT("capturingKit"), PREP_KIT("prepKit"), GAF_QC_NAME(
			"GAF_QC_Name"), GAF_QC_DATE("GAF_QC_Date"), GAF_QC_STATUS("GAF_QC_Status"), GCC_ANALYSIS("GCC_Analysis"), BARCODE_2(
			"Barcode_2"), BARCODE("barcode"), BARCODE_TYPE("barcodeType");

	private final String columnName;

	GAFCol(String columnName)
	{
		this.columnName = columnName;
	}

	public String toString()
	{
		return this.columnName;
	}

	public static List<String> getAllColumnsNames()
	{
		List<String> list = new ArrayList<String>();
		for (GAFCol gafCol : GAFCol.values())
		{
			list.add(gafCol.toString());
		}
		return list;
	}
}
