package org.molgenis.gaf;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.settings.DefaultSettingsEntity;
import org.molgenis.data.settings.DefaultSettingsEntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class GafListDbSettings extends DefaultSettingsEntity implements GafListSettings
{
	private static final String ID = GafListImporterController.ID;

	public GafListDbSettings()
	{
		super(ID);
	}

	@Component
	private static class Meta extends DefaultSettingsEntityMetaData
	{
		static final String IMPORTER_ENABLED = "importerEnabled";
		static final String ARRAY_FILE_EXAMPLE = "arrayFileExample";
		static final String ARRAY_FILE_REGEXP = "arrayFileRegExp";
		static final String ARRAY_ID_EXAMPLE = "arrayIdExample";
		static final String ARRAY_ID_REGEXP = "arrayIdRegExp";
		static final String BARCODE1_EXAMPLE = "barcode1Example";
		static final String BARCODE1_REGEXP = "barcode1RegExp";
		static final String BARCODE2_EXAMPLE = "barcode2Example";
		static final String BARCODE_EXAMPLE = "barcodeExample";
		static final String BARCODE_TYPE_EXAMPLE = "barcodeTypeExample";
		static final String CAPTURING_KIT_EXAMPLE = "capturingKitExample";
		static final String CAPTURING_KIT_REGEXP = "capturingKitRegExp";
		static final String CONTACT_EXAMPLE = "contactExample";
		static final String CONTACT_REGEXP = "contactRegExp";
		static final String EXTERNAL_SAMPLE_ID_EXAMPLE = "externalSampleIdExample";
		static final String EXTERNAL_SAMPLE_ID_REGEXP = "externalSampleIdRegExp";
		static final String FLOW_CELL_EXAMPLE = "flowCellExample";
		static final String FLOW_CELL_REGEXP = "flowCellRegExp";
		static final String GAF_QC_DATE_EXAMPLE = "gafQcDateExample";
		static final String GAF_QC_NAME_EXAMPLE = "gafQcNameExample";
		static final String GAF_QC_STATUS_EXAMPLE = "gafQcStatusExample";
		static final String GCC_ANALYSIS_EXAMPLE = "gccAnalysisExample";
		static final String INTERNAL_SAMPLE_ID_EXAMPLE = "internalSampleIdExample";
		static final String INTERNAL_SAMPLE_ID_REGEXP = "internalSampleIdRegExp";
		static final String LANE_EXAMPLE = "laneExample";
		static final String LANE_REGEXP = "laneRegExp";
		static final String NAME = "name";
		static final String PREP_KIT_EXAMPLE = "prepKitExample";
		static final String PROJECT_EXAMPLE = "projectExample";
		static final String PROJECT_REGEXP = "projectRegExp";
		static final String RUN_EXAMPLE = "runExample";
		static final String RUN_REGEXP = "runRegExp";
		static final String SAMPLE_TYPE_EXAMPLE = "sampleTypeExample";
		static final String SEQ_TYPE_EXAMPLE = "seqTypeExample";
		static final String SEQUENCER_EXAMPLE = "sequencerExample";
		static final String SEQUENCER_REGEXP = "sequencerRegExp";
		static final String SEQUENCING_START_DATE_EXAMPLE = "sequencingStartDateExample";
		static final String SEQUENCING_START_DATE_REG_EXP = "sequencingStarteDateRegExp";

		public Meta()
		{
			super(ID);
			setLabel("Gaflist settings");
			addAttribute(IMPORTER_ENABLED).setDataType(MolgenisFieldTypes.BOOL).setNillable(false)
					.setDefaultValue("true");
			addAttribute(ARRAY_FILE_EXAMPLE).setNillable(false).setDefaultValue("/path/to/arrafile");
			addAttribute(ARRAY_FILE_REGEXP).setNillable(false).setDefaultValue("^.*$");
			addAttribute(ARRAY_ID_EXAMPLE).setNillable(false).setDefaultValue("A number");
			addAttribute(ARRAY_ID_REGEXP).setNillable(false).setDefaultValue("^.*$");
			addAttribute(BARCODE1_EXAMPLE)
					.setNillable(false)
					.setDefaultValue(
							"A text than include the seqType Number, and the barcode made from only the characters ATCG. Example: \"RPI 01 ATCACG\"");
			addAttribute(BARCODE1_REGEXP).setNillable(false).setDefaultValue(
					"^(None)|(((GAF)|(RPI)|(AGI)|(MON)|(RTP)|(NEX)||(HP8))\\s[N0-9]{1,4}\\s([ACGT]{6})([ATCG]{2})?)$");
			addAttribute(BARCODE2_EXAMPLE).setNillable(false).setDefaultValue(
					"Empty, or else a barcode that is made from only the characters ATCG");
			addAttribute(BARCODE_EXAMPLE).setNillable(false).setDefaultValue(
					"A barcode that is made from only the characters ATCG");
			addAttribute(BARCODE_TYPE_EXAMPLE).setNillable(false).setDefaultValue(
					"None or GAF, RPI, AGI, MON, RTP, HP8 enz");
			addAttribute(CAPTURING_KIT_EXAMPLE)
					.setNillable(false)
					.setDefaultValue(
							" \"None\", or name of caputingkit that is made from only  the characters a-z A-Z 0-9 and _, (Current options: None, SureSelect_All_Exon_G3362, SureSelect_All_Exon_50MB, SureSelect_All_Exon_50MB_v4, SureSelect_All_Exon_50MB_v5, Nimblegen_cardio_custom20110617, Sureselect_MP_Capture_Library_Design_0394321, Kinome, NugenOnco)");
			addAttribute(CAPTURING_KIT_REGEXP).setNillable(false).setDefaultValue("^[a-zA-Z0-9_]+$");
			addAttribute(CONTACT_EXAMPLE).setNillable(false).setDefaultValue(
					"Name of addressee &lt;email@address.nl&gt; or only email@address.nl");
			addAttribute(CONTACT_REGEXP).setNillable(false).setDefaultValue("^.*$");
			addAttribute(EXTERNAL_SAMPLE_ID_EXAMPLE).setNillable(false).setDefaultValue(
					"A text that is made from only  the characters a-z A-Z 0-9 and _");
			addAttribute(EXTERNAL_SAMPLE_ID_REGEXP).setNillable(false).setDefaultValue("^[a-zA-Z0-9_]+$");
			addAttribute(FLOW_CELL_EXAMPLE).setNillable(false).setDefaultValue(
					"A text of 10 charcters long starting with A or B");
			addAttribute(FLOW_CELL_REGEXP).setNillable(false).setDefaultValue("^(([AB][A-Z0-9]{7}XX)|(A[A-Z0-9]{4}))$");
			addAttribute(GAF_QC_DATE_EXAMPLE).setNillable(false).setDefaultValue("SKIP");
			addAttribute(GAF_QC_NAME_EXAMPLE).setNillable(false).setDefaultValue("SKIP");
			addAttribute(GAF_QC_STATUS_EXAMPLE).setNillable(false).setDefaultValue(
					"Current options: Determined, Passed, Failed");
			addAttribute(GCC_ANALYSIS_EXAMPLE).setNillable(false).setDefaultValue(
					"Current options: Yes, No, RNA_Pipeline, DNA_Pipeline");
			addAttribute(INTERNAL_SAMPLE_ID_EXAMPLE).setNillable(false).setDefaultValue("A number");
			addAttribute(INTERNAL_SAMPLE_ID_REGEXP).setNillable(false).setDefaultValue("^[0-9]+$");
			addAttribute(LANE_EXAMPLE).setNillable(false).setDefaultValue("A number from 1 to 8");
			addAttribute(LANE_REGEXP).setNillable(false).setDefaultValue("^[1-8](,[1-8])*$");
			addAttribute(NAME).setNillable(false).setDefaultValue("gaflist_gaflist_20150217");
			addAttribute(PREP_KIT_EXAMPLE).setNillable(false).setDefaultValue("SKIP");
			addAttribute(PROJECT_EXAMPLE).setNillable(false).setDefaultValue(
					"A text that is made from only  the characters a-z A-Z 0-9 and _");
			addAttribute(PROJECT_REGEXP).setNillable(false).setDefaultValue("^[a-zA-Z0-9_]+$");
			addAttribute(RUN_EXAMPLE).setNillable(false).setDefaultValue("A number from 0000 to 9999");
			addAttribute(RUN_REGEXP).setNillable(false).setDefaultValue("^[0-9]{4}$");
			addAttribute(SAMPLE_TYPE_EXAMPLE).setNillable(false).setDefaultValue("Current options: DNA, RNA");
			addAttribute(SEQ_TYPE_EXAMPLE).setNillable(false).setDefaultValue("Current options: SR, PE, MP");
			addAttribute(SEQUENCER_EXAMPLE)
					.setNillable(false)
					.setDefaultValue(
							"A text that is made from only  the characters a-z A-Z 0-9 and _  (Current options: HWUSI_EAS536, SN163, M01785)");
			addAttribute(SEQUENCER_REGEXP).setNillable(false).setDefaultValue("^[a-zA-Z0-9_]+$");
			addAttribute(SEQUENCING_START_DATE_EXAMPLE).setNillable(false).setDefaultValue("A date in pattern YYMMDD");
			addAttribute(SEQUENCING_START_DATE_REG_EXP).setNillable(false).setDefaultValue("^[0-9]{6}$");
		}
	}

	@Override
	public boolean isImporterEnabled()
	{
		return getBoolean(Meta.IMPORTER_ENABLED);
	}

	@Override
	public void setImportedEnabled(boolean importerEnabled)
	{
		set(Meta.IMPORTER_ENABLED, importerEnabled);
	}

	@Override
	public String getInternalSampleIdRegExp()
	{
		return getString(Meta.INTERNAL_SAMPLE_ID_REGEXP);
	}

	@Override
	public void setInternalSampleIdRegExp(String regExp)
	{
		set(Meta.INTERNAL_SAMPLE_ID_REGEXP, regExp);
	}

	@Override
	public String getExternalSampleIdRegExp()
	{
		return getString(Meta.EXTERNAL_SAMPLE_ID_REGEXP);
	}

	@Override
	public void setExternalSampleIdRegExp(String regExp)
	{
		set(Meta.EXTERNAL_SAMPLE_ID_REGEXP, regExp);
	}

	@Override
	public String getProjectRegExp()
	{
		return getString(Meta.PROJECT_REGEXP);
	}

	@Override
	public void setProjectRegExp(String regExp)
	{
		set(Meta.PROJECT_REGEXP, regExp);
	}

	@Override
	public String getCapturingKitRegExp()
	{
		return getString(Meta.CAPTURING_KIT_REGEXP);
	}

	@Override
	public void setCapturingKitRegExp(String regExp)
	{
		set(Meta.CAPTURING_KIT_REGEXP, regExp);
	}

	@Override
	public String getSequencerRegExp()
	{
		return getString(Meta.SEQUENCER_REGEXP);
	}

	@Override
	public void setSequencerRegExp(String regExp)
	{
		set(Meta.SEQUENCER_REGEXP, regExp);
	}

	@Override
	public String getContactRegExp()
	{
		return getString(Meta.CONTACT_REGEXP);
	}

	@Override
	public void setContactRegExp(String regExp)
	{
		set(Meta.CONTACT_REGEXP, regExp);
	}

	@Override
	public String getSequencingStartDateRegExp()
	{
		return getString(Meta.SEQUENCING_START_DATE_REG_EXP);
	}

	@Override
	public void setSequencingStartDateRegExp(String regExp)
	{
		set(Meta.SEQUENCING_START_DATE_REG_EXP, regExp);
	}

	@Override
	public String getRunRegExp()
	{
		return getString(Meta.RUN_REGEXP);
	}

	@Override
	public void setRunRegExp(String regExp)
	{
		set(Meta.RUN_REGEXP, regExp);
	}

	@Override
	public String getFlowCellRegExp()
	{
		return getString(Meta.FLOW_CELL_REGEXP);
	}

	@Override
	public void setFlowCellRegExp(String regExp)
	{
		set(Meta.FLOW_CELL_REGEXP, regExp);
	}

	@Override
	public String getLaneRegExp()
	{
		return getString(Meta.LANE_REGEXP);
	}

	@Override
	public void setLaneRegExp(String regExp)
	{
		set(Meta.LANE_REGEXP, regExp);
	}

	@Override
	public String getBarcode1RegExp()
	{
		return getString(Meta.BARCODE1_REGEXP);
	}

	@Override
	public void setBarcode1RegExp(String regExp)
	{
		set(Meta.BARCODE1_REGEXP, regExp);
	}

	@Override
	public String getArrayFileRegExp()
	{
		return getString(Meta.ARRAY_FILE_REGEXP);
	}

	@Override
	public void setArrayFileRegExp(String regExp)
	{
		set(Meta.ARRAY_FILE_REGEXP, regExp);
	}

	@Override
	public String getArrayIdRegExp()
	{
		return getString(Meta.ARRAY_ID_REGEXP);
	}

	@Override
	public void setArrayIdRegExp(String regExp)
	{
		set(Meta.ARRAY_ID_REGEXP, regExp);
	}

	@Override
	public String getName()
	{
		return getString(Meta.NAME);
	}

	@Override
	public void setName(String name)
	{
		set(Meta.NAME, name);
	}

	@Override
	public String getInternalSampleIdExample()
	{
		return getString(Meta.INTERNAL_SAMPLE_ID_EXAMPLE);
	}

	@Override
	public void setInternalSamplIdExample(String example)
	{
		set(Meta.INTERNAL_SAMPLE_ID_EXAMPLE, example);
	}

	@Override
	public String getExternalSampleIdExample()
	{
		return getString(Meta.EXTERNAL_SAMPLE_ID_EXAMPLE);
	}

	@Override
	public void setExternalSampleIdExample(String example)
	{
		set(Meta.EXTERNAL_SAMPLE_ID_EXAMPLE, example);
	}

	@Override
	public String getProjectExample()
	{
		return getString(Meta.PROJECT_EXAMPLE);
	}

	@Override
	public void setProjectExample(String example)
	{
		set(Meta.PROJECT_EXAMPLE, example);
	}

	@Override
	public String getSequencerExample()
	{
		return getString(Meta.SEQUENCER_EXAMPLE);
	}

	@Override
	public void setSequencerExample(String example)
	{
		set(Meta.SEQUENCER_EXAMPLE, example);
	}

	@Override
	public String getContactExample()
	{
		return getString(Meta.CONTACT_EXAMPLE);
	}

	@Override
	public void setContactExample(String example)
	{
		set(Meta.CONTACT_EXAMPLE, example);
	}

	@Override
	public String getSequencingStartDateExample()
	{
		return getString(Meta.SEQUENCING_START_DATE_EXAMPLE);
	}

	@Override
	public void setSequencingStartDateExample(String example)
	{
		set(Meta.SEQUENCING_START_DATE_EXAMPLE, example);
	}

	@Override
	public String getRunExample()
	{
		return getString(Meta.RUN_EXAMPLE);
	}

	@Override
	public void setRunExample(String example)
	{
		set(Meta.RUN_EXAMPLE, example);
	}

	@Override
	public String getFlowCellExample()
	{
		return getString(Meta.FLOW_CELL_EXAMPLE);
	}

	@Override
	public void setFlowCellExample(String example)
	{
		set(Meta.FLOW_CELL_EXAMPLE, example);
	}

	@Override
	public String getLaneExample()
	{
		return getString(Meta.LANE_EXAMPLE);
	}

	@Override
	public void setLaneExample(String example)
	{
		set(Meta.LANE_EXAMPLE, example);
	}

	@Override
	public String getBarcode1Example()
	{
		return getString(Meta.BARCODE1_EXAMPLE);
	}

	@Override
	public void setBarcode1Example(String example)
	{
		set(Meta.BARCODE1_EXAMPLE, example);
	}

	@Override
	public String getArrayFileExample()
	{
		return getString(Meta.ARRAY_FILE_EXAMPLE);
	}

	@Override
	public void setArrayFileExample(String example)
	{
		set(Meta.ARRAY_FILE_EXAMPLE, example);
	}

	@Override
	public String getArrayIdExample()
	{
		return getString(Meta.ARRAY_ID_EXAMPLE);
	}

	@Override
	public void setArrayIdExample(String example)
	{
		set(Meta.ARRAY_ID_EXAMPLE, example);
	}

	@Override
	public String getCapturingKitExample()
	{
		return getString(Meta.CAPTURING_KIT_EXAMPLE);
	}

	@Override
	public void setCapturingKitExample(String example)
	{
		set(Meta.CAPTURING_KIT_EXAMPLE, example);
	}

	@Override
	public String getSeqTypeExample()
	{
		return getString(Meta.SEQ_TYPE_EXAMPLE);
	}

	@Override
	public void setSeqTypeExample(String example)
	{
		set(Meta.SEQ_TYPE_EXAMPLE, example);
	}

	@Override
	public String getSampleTypeExample()
	{
		return getString(Meta.SAMPLE_TYPE_EXAMPLE);
	}

	@Override
	public void setSampleTypeExample(String example)
	{
		set(Meta.SAMPLE_TYPE_EXAMPLE, example);
	}

	@Override
	public String getPrepKitExample()
	{
		return getString(Meta.PREP_KIT_EXAMPLE);
	}

	@Override
	public void setPrepKitExample(String example)
	{
		set(Meta.PREP_KIT_EXAMPLE, example);
	}

	@Override
	public String getGafQcNameExample()
	{
		return getString(Meta.GAF_QC_NAME_EXAMPLE);
	}

	@Override
	public void setGafQcNameExample(String example)
	{
		set(Meta.GAF_QC_NAME_EXAMPLE, example);
	}

	@Override
	public String getGafQcDateExample()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setGafQcDateExample(String example)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public String getGafQcStatusExample()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setGafQcStatusExampke(String example)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public String getGccAnalysisExample()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setGccAnalysisExample(String example)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public String getBarcode2Example()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setBarcode2Example(String example)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public String getBarcodeExample()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setBarcodeExample(String example)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public String getBarcodeTypeExample()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setBarcodeTypeExample(String example)
	{
		// TODO Auto-generated method stub

	}

}
