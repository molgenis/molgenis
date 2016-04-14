package org.molgenis.data.support;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.annotation.meta.AnnotatorEntityMetaData;

import java.util.LinkedList;

import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.STRING;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.TEXT;

public class EffectsMetaData implements AnnotatorEntityMetaData
{
	public static final String ID = "id";
	public static final String VARIANT = "VARIANT";

	public static final String ALT = "Alt_Allele";
	public static final String ANNOTATION = "Annotation";
	public static final String PUTATIVE_IMPACT = "Putative_impact";
	public static final String GENE_NAME = "Gene_Name";
	public static final String GENE_ID = "Gene_ID";
	public static final String FEATURE_TYPE = "Feature_type";
	public static final String FEATURE_ID = "Feature_ID";
	public static final String TRANSCRIPT_BIOTYPE = "Transcript_biotype";
	public static final String RANK_TOTAL = "Rank_total";
	public static final String HGVS_C = "HGVS_c";
	public static final String HGVS_P = "HGVS_p";
	public static final String C_DNA_POSITION = "cDNA_position";
	public static final String CDS_POSITION = "CDS_position";
	public static final String PROTEIN_POSITION = "Protein_position";
	public static final String DISTANCE_TO_FEATURE = "Distance_to_feature";
	public static final String ERRORS = "Errors";

	public static final DefaultAttributeMetaData ALT_ATTR = new DefaultAttributeMetaData(ALT, TEXT)
			.setDescription("The alternative allele on which this EFFECT applies");
	public static final DefaultAttributeMetaData PUTATIVE_IMPACT_ATTR = new DefaultAttributeMetaData(PUTATIVE_IMPACT,
			STRING).setDescription(
					"A simple estimation of putative impact / deleteriousness : {HIGH, MODERATE, LOW, MODIFIER}(source:http://snpeff.sourceforge.net)");
	public static final DefaultAttributeMetaData GENE_NAME_ATTR = new DefaultAttributeMetaData(GENE_NAME, STRING)
			.setDescription(
					"Common gene name (HGNC). Optional: use closest gene when the variant is “intergenic”(source:http://snpeff.sourceforge.net)");
	public static final DefaultAttributeMetaData GENE_ID_ATTR = new DefaultAttributeMetaData(GENE_ID, STRING)
			.setDescription("Gene ID");
	public static final DefaultAttributeMetaData FEATURE_TYPE_ATTR = new DefaultAttributeMetaData(FEATURE_TYPE, STRING)
			.setDescription(
					"Which type of feature is in the next field (e.g. transcript, motif, miRNA, etc.). It is preferred to use Sequence Ontology (SO) terms, but ‘custom’ (user defined) are allowed. ANN=A|stop_gained|HIGH|||transcript|... Tissue specific features may include cell type / tissue information separated by semicolon e.g.: ANN=A|histone_binding_site|LOW|||H3K4me3:HeLa-S3|...\n"
							+ "Feature ID: Depending on the annotation, this may be: Transcript ID (preferably using version number), Motif ID, miRNA, ChipSeq peak, Histone mark, etc. Note: Some features may not have ID (e.g. histone marks from custom Chip-Seq experiments may not have a unique ID). (source:http://snpeff.sourceforge.net)");
	public static final DefaultAttributeMetaData FEATURE_ID_ATTR = new DefaultAttributeMetaData(FEATURE_ID, STRING)
			.setDescription(
					"Depending on the annotation, this may be: Transcript ID (preferably using version number), Motif ID, miRNA, ChipSeq peak, Histone mark, etc. Note: Some features may not have ID (e.g. histone marks from custom Chip-Seq experiments may not have a unique ID).(source:http://snpeff.sourceforge.net)");
	public static final DefaultAttributeMetaData TRANSCRIPT_BIOTYPE_ATTR = new DefaultAttributeMetaData(
			TRANSCRIPT_BIOTYPE, STRING).setDescription(
					"The bare minimum is at least a description on whether the transcript is {“Coding”, “Noncoding”}. Whenever possible, use ENSEMBL biotypes.(source:http://snpeff.sourceforge.net)");
	public static final DefaultAttributeMetaData RANK_TOTAL_ATTR = new DefaultAttributeMetaData(RANK_TOTAL, STRING)
			.setDescription(
					"Exon or Intron rank / total number of exons or introns(source:http://snpeff.sourceforge.net)");
	public static final DefaultAttributeMetaData HGVS_C_ATTR = new DefaultAttributeMetaData(HGVS_C, TEXT)
			.setDescription("Variant using HGVS notation (DNA level)(source:http://snpeff.sourceforge.net)");
	public static final DefaultAttributeMetaData HGVS_P_ATTR = new DefaultAttributeMetaData(HGVS_P, STRING)
			.setDescription(
					"If variant is coding, this field describes the variant using HGVS notation (Protein level). Since transcript ID is already mentioned in ‘feature ID’, it may be omitted here.(source:http://snpeff.sourceforge.net)");
	public static final DefaultAttributeMetaData C_DNA_POSITION_ATTR = new DefaultAttributeMetaData(C_DNA_POSITION,
			STRING).setDescription(
					"Position in cDNA and trancript’s cDNA length (one based)(source:http://snpeff.sourceforge.net)");
	public static final DefaultAttributeMetaData CDS_POSITION_ATTR = new DefaultAttributeMetaData(CDS_POSITION, STRING)
			.setDescription(
					"Position and number of coding bases (one based includes START and STOP codons)(source:http://snpeff.sourceforge.net)");
	public static final DefaultAttributeMetaData PROTEIN_POSITION_ATTR = new DefaultAttributeMetaData(PROTEIN_POSITION,
			STRING).setDescription("Position and number of AA (one based, including START, but not STOP)");
	public static final DefaultAttributeMetaData DISTANCE_TO_FEATURE_ATTR = new DefaultAttributeMetaData(
			DISTANCE_TO_FEATURE, STRING).setDescription(
					"All items in this field are options, so the field could be empty. Up/Downstream: Distance to first / last codon Intergenic: Distance to closest gene Distance to closest Intron boundary in exon (+/- up/downstream). If same, use positive number. Distance to closest exon boundary in Intron (+/- up/downstream) Distance to first base in MOTIF Distance to first base in miRNA Distance to exon-intron boundary in splice_site or splice _region ChipSeq peak: Distance to summit (or peak center) Histone mark / Histone state: Distance to summit (or peak center)(source:http://snpeff.sourceforge.net)");
	public static final DefaultAttributeMetaData ERRORS_ATTR = new DefaultAttributeMetaData(ERRORS, STRING)
			.setDescription(
					"Add errors, warnings oErrors, Warnings or Information messages: Add errors, warnings or r informative message that can affect annotation accuracy. It can be added using either ‘codes’ (as shown in column 1, e.g. W1) or ‘message types’ (as shown in column 2, e.g. WARNING_REF_DOES_NOT_MATCH_GENOME). All these errors, warnings or information messages messages are optional.(source:http://snpeff.sourceforge.net)");
	public static final DefaultAttributeMetaData ANNOTATION_ATTR = new DefaultAttributeMetaData(ANNOTATION, STRING)
			.setDescription(
					"Annotated using Sequence Ontology terms. Multiple effects can be concatenated using ‘&’ (source:http://snpeff.sourceforge.net)");

	public LinkedList<AttributeMetaData> getOrderedAttributes()
	{
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
}
