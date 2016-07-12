package org.molgenis.data.annotation.core.meta.effects;

import org.molgenis.data.annotation.core.meta.AnnotatorEntityMetaData;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedList;

import static org.molgenis.MolgenisFieldTypes.AttributeType.STRING;
import static org.molgenis.MolgenisFieldTypes.AttributeType.TEXT;

@Component
public class EffectsMetaData implements AnnotatorEntityMetaData
{
	@Autowired
	private AttributeMetaDataFactory attributeMetaDataFactory;

	public static String ID = "id";
	public static String VARIANT = "Variant";

	public static String ALT = "Alt_Allele";
	public static String ANNOTATION = "Annotation";
	public static String PUTATIVE_IMPACT = "Putative_impact";
	public static String GENE_NAME = "Gene_Name";
	public static String GENE_ID = "Gene_ID";
	public static String FEATURE_TYPE = "Feature_type";
	public static String FEATURE_ID = "Feature_ID";
	public static String TRANSCRIPT_BIOTYPE = "Transcript_biotype";
	public static String RANK_TOTAL = "Rank_total";
	public static String HGVS_C = "HGVS_c";
	public static String HGVS_P = "HGVS_p";
	public static String C_DNA_POSITION = "cDNA_position";
	public static String CDS_POSITION = "CDS_position";
	public static String PROTEIN_POSITION = "Protein_position";
	public static String DISTANCE_TO_FEATURE = "Distance_to_feature";
	public static String ERRORS = "Errors";

	public LinkedList<AttributeMetaData> getOrderedAttributes()
	{
		AttributeMetaData ALT_ATTR = getAltAttr();
		AttributeMetaData PUTATIVE_IMPACT_ATTR = getPutativeImpactAttr();
		AttributeMetaData GENE_NAME_ATTR = getGeneNameAttr();
		AttributeMetaData GENE_ID_ATTR = getGeneIdAttr();
		AttributeMetaData FEATURE_TYPE_ATTR = getFeatureTypeAttr();
		AttributeMetaData FEATURE_ID_ATTR = getFeatureIdAttr();
		AttributeMetaData TRANSCRIPT_BIOTYPE_ATTR = getTranscriptBiotypeAttr();
		AttributeMetaData RANK_TOTAL_ATTR = getRankTotalAttr();
		AttributeMetaData HGVS_C_ATTR = getHgvsCAttr();
		AttributeMetaData HGVS_P_ATTR = getHgvsPAttr();
		AttributeMetaData C_DNA_POSITION_ATTR = getCdnaPositionAttr();
		AttributeMetaData CDS_POSITION_ATTR = getCdsPositionAttr();
		AttributeMetaData PROTEIN_POSITION_ATTR = getProteinPositionAttr();
		AttributeMetaData DISTANCE_TO_FEATURE_ATTR = getDistanceToFeatureAttr();
		AttributeMetaData ERRORS_ATTR = getErrorsAttr();
		AttributeMetaData ANNOTATION_ATTR = getAnnotationAttr();

		LinkedList<AttributeMetaData> attributes = new LinkedList<>();
		attributes.add(ALT_ATTR);
		attributes.add(GENE_NAME_ATTR);
		attributes.add(ANNOTATION_ATTR);
		attributes.add(PUTATIVE_IMPACT_ATTR);
		attributes.add(GENE_ID_ATTR);
		attributes.add(FEATURE_TYPE_ATTR);
		attributes.add(FEATURE_ID_ATTR);
		attributes.add(TRANSCRIPT_BIOTYPE_ATTR);
		attributes.add(RANK_TOTAL_ATTR);
		attributes.add(HGVS_C_ATTR);
		attributes.add(HGVS_P_ATTR);
		attributes.add(C_DNA_POSITION_ATTR);
		attributes.add(CDS_POSITION_ATTR);
		attributes.add(PROTEIN_POSITION_ATTR);
		attributes.add(DISTANCE_TO_FEATURE_ATTR);
		attributes.add(ERRORS_ATTR);

		return attributes;
	}

	public AttributeMetaData getTranscriptBiotypeAttr()
	{
		return attributeMetaDataFactory.create().setName(
					TRANSCRIPT_BIOTYPE).setDataType(STRING).setDescription(
					"The bare minimum is at least a description on whether the transcript is {“Coding”, “Noncoding”}. Whenever possible, use ENSEMBL biotypes.(source:http://snpeff.sourceforge.net)").setAggregatable(true);
	}

	public AttributeMetaData getRankTotalAttr()
	{
		return attributeMetaDataFactory.create().setName(RANK_TOTAL).setDataType(STRING)
					.setDescription(
							"Exon or Intron rank / total number of exons or introns(source:http://snpeff.sourceforge.net)");
	}

	public AttributeMetaData getHgvsCAttr()
	{
		return attributeMetaDataFactory.create().setName(HGVS_C).setDataType(TEXT)
					.setDescription("Variant using HGVS notation (DNA level)(source:http://snpeff.sourceforge.net)");
	}

	public AttributeMetaData getHgvsPAttr()
	{
		return attributeMetaDataFactory.create().setName(HGVS_P).setDataType(STRING)
					.setDescription(
							"If variant is coding, this field describes the variant using HGVS notation (Protein level). Since transcript ID is already mentioned in ‘feature ID’, it may be omitted here.(source:http://snpeff.sourceforge.net)");
	}

	public AttributeMetaData getCdnaPositionAttr()
	{
		return attributeMetaDataFactory.create().setName(C_DNA_POSITION).setDataType(
					STRING).setDescription(
					"Position in cDNA and trancript’s cDNA length (one based)(source:http://snpeff.sourceforge.net)");
	}

	public AttributeMetaData getCdsPositionAttr()
	{
		return attributeMetaDataFactory.create().setName(CDS_POSITION).setDataType(STRING)
					.setDescription(
							"Position and number of coding bases (one based includes START and STOP codons)(source:http://snpeff.sourceforge.net)");
	}

	public AttributeMetaData getProteinPositionAttr()
	{
		return attributeMetaDataFactory.create().setName(PROTEIN_POSITION).setDataType(
					STRING).setDescription("Position and number of AA (one based, including START, but not STOP)");
	}

	public AttributeMetaData getDistanceToFeatureAttr()
	{
		return attributeMetaDataFactory.create().setName(
					DISTANCE_TO_FEATURE).setDataType(STRING).setDescription(
					"All items in this field are options, so the field could be empty. Up/Downstream: Distance to first / last codon Intergenic: Distance to closest gene Distance to closest Intron boundary in exon (+/- up/downstream). If same, use positive number. Distance to closest exon boundary in Intron (+/- up/downstream) Distance to first base in MOTIF Distance to first base in miRNA Distance to exon-intron boundary in splice_site or splice _region ChipSeq peak: Distance to summit (or peak center) Histone mark / Histone state: Distance to summit (or peak center)(source:http://snpeff.sourceforge.net)");
	}

	public AttributeMetaData getErrorsAttr()
	{
		return attributeMetaDataFactory.create().setName(ERRORS).setDataType(STRING)
					.setDescription(
							"Add errors, warnings oErrors, Warnings or Information messages: Add errors, warnings or r informative message that can affect annotation accuracy. It can be added using either ‘codes’ (as shown in column 1, e.g. W1) or ‘message types’ (as shown in column 2, e.g. WARNING_REF_DOES_NOT_MATCH_GENOME). All these errors, warnings or information messages messages are optional.(source:http://snpeff.sourceforge.net)");
	}

	public AttributeMetaData getAnnotationAttr()
	{
		return attributeMetaDataFactory.create().setName(ANNOTATION).setDataType(STRING)
					.setDescription(
							"Annotated using Sequence Ontology terms. Multiple effects can be concatenated using ‘&’ (source:http://snpeff.sourceforge.net)").setAggregatable(true);
	}

	public AttributeMetaData getFeatureIdAttr()
	{
		return attributeMetaDataFactory.create().setName(FEATURE_ID).setDataType(STRING)
					.setDescription(
							"Depending on the annotation, this may be: Transcript ID (preferably using version number), Motif ID, miRNA, ChipSeq peak, Histone mark, etc. Note: Some features may not have ID (e.g. histone marks from custom Chip-Seq experiments may not have a unique ID).(source:http://snpeff.sourceforge.net)");
	}

	public AttributeMetaData getFeatureTypeAttr()
	{
		return attributeMetaDataFactory.create().setName(FEATURE_TYPE).setDataType( STRING)
					.setDescription(
							"Which type of feature is in the next field (e.g. transcript, motif, miRNA, etc.). It is preferred to use Sequence Ontology (SO) terms, but ‘custom’ (user defined) are allowed. ANN=A|stop_gained|HIGH|||transcript|... Tissue specific features may include cell type / tissue information separated by semicolon e.g.: ANN=A|histone_binding_site|LOW|||H3K4me3:HeLa-S3|...\n"
									+ "Feature ID: Depending on the annotation, this may be: Transcript ID (preferably using version number), Motif ID, miRNA, ChipSeq peak, Histone mark, etc. Note: Some features may not have ID (e.g. histone marks from custom Chip-Seq experiments may not have a unique ID). (source:http://snpeff.sourceforge.net)").setAggregatable(true);
	}

	public AttributeMetaData getGeneIdAttr()
	{
		return attributeMetaDataFactory.create().setName(GENE_ID).setDataType(STRING)
					.setDescription("Gene ID").setAggregatable(true);
	}

	public AttributeMetaData getGeneNameAttr()
	{
		return attributeMetaDataFactory.create().setName(GENE_NAME).setDataType(STRING)
					.setDescription(
							"Common gene name (HGNC). Optional: use closest gene when the variant is “intergenic”(source:http://snpeff.sourceforge.net)").setAggregatable(true);
	}

	public AttributeMetaData getPutativeImpactAttr()
	{
		return attributeMetaDataFactory.create().setName(PUTATIVE_IMPACT).setDataType(
					STRING).setDescription(
					"A simple estimation of putative impact / deleteriousness : {HIGH, MODERATE, LOW, MODIFIER}(source:http://snpeff.sourceforge.net)").setAggregatable(true);
	}

	public AttributeMetaData getAltAttr()
	{
		return attributeMetaDataFactory.create().setName(ALT).setDataType(TEXT)
					.setDescription("The alternative allele on which this EFFECT applies");
	}
}
