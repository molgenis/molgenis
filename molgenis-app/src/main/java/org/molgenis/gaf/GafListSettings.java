package org.molgenis.gaf;

public interface GafListSettings
{
	boolean isImporterEnabled();

	void setImportedEnabled(boolean importerEnabled);

	String getInternalSampleIdRegExp();

	void setInternalSampleIdRegExp(String regExp);

	String getExternalSampleIdRegExp();

	void setExternalSampleIdRegExp(String regExp);

	String getProjectRegExp();

	void setProjectRegExp(String rexExp);

	String getCapturingKitRegExp();

	void setCapturingKitRegExp(String regExp);

	String getSequencerRegExp();

	void setSequencerRegExp(String regExp);

	String getContactRegExp();

	void setContactRegExp(String regExp);

	String getSequencingStartDateRegExp();

	void setSequencingStartDateRegExp(String regExp);

	String getRunRegExp();

	void setRunRegExp(String regexp);

	String getFlowCellRegExp();

	void setFlowCellRegExp(String regExp);

	String getLaneRegExp();

	void setLaneRegExp(String regExp);

	String getBarcode1RegExp();

	void setBarcode1RegExp(String regExp);

	String getArrayFileRegExp();

	void setArrayFileRegExp(String regExp);

	String getArrayIdRegExp();

	void setArrayIdRegExp(String regExp);

	String getName();

	void setName(String name);

	String getInternalSampleIdExample();

	void setInternalSamplIdExample(String example);

	String getExternalSampleIdExample();

	void setExternalSampleIdExample(String example);

	String getProjectExample();

	void setProjectExample(String example);

	String getSequencerExample();

	void setSequencerExample(String example);

	String getContactExample();

	void setContactExample(String example);

	String getSequencingStartDateExample();

	void setSequencingStartDateExample(String example);

	String getRunExample();

	void setRunExample(String example);

	String getFlowCellExample();

	void setFlowCellExample(String example);

	String getLaneExample();

	void setLaneExample(String example);

	String getBarcode1Example();

	void setBarcode1Example(String example);

	String getArrayFileExample();

	void setArrayFileExample(String example);

	String getArrayIdExample();

	void setArrayIdExample(String example);

	String getCapturingKitExample();

	void setCapturingKitExample(String example);

	String getSeqTypeExample();

	void setSeqTypeExample(String example);

	String getSampleTypeExample();

	void setSampleTypeExample(String example);

	String getPrepKitExample();

	void setPrepKitExample(String example);

	String getGafQcNameExample();

	void setGafQcNameExample(String example);

	String getGafQcDateExample();

	void setGafQcDateExample(String example);

	String getGafQcStatusExample();

	void setGafQcStatusExampke(String example);

	String getGccAnalysisExample();

	void setGccAnalysisExample(String example);

	String getBarcode2Example();

	void setBarcode2Example(String example);

	String getBarcodeExample();

	void setBarcodeExample(String example);

	String getBarcodeTypeExample();

	void setBarcodeTypeExample(String example);
}
