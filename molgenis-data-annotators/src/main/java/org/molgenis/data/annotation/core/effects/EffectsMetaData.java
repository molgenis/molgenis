package org.molgenis.data.annotation.core.effects;

import org.molgenis.data.annotation.web.meta.AnnotatorEntityType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.vcf.utils.VcfWriterUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedList;

import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.AttributeType.TEXT;

@Component
public class EffectsMetaData implements AnnotatorEntityType
{
	@Autowired
	AttributeFactory attributeFactory;

	public static final String ID = "id";
	public static final String VARIANT = VcfWriterUtils.VARIANT;
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

	public LinkedList<Attribute> getOrderedAttributes()
	{
		Attribute ALT_ATTR = getAltAttr();
		Attribute PUTATIVE_IMPACT_ATTR = getPutativeImpactAttr();
		Attribute GENE_NAME_ATTR = getGeneNameAttr();
		Attribute GENE_ID_ATTR = getGeneIdAttr();
		Attribute FEATURE_TYPE_ATTR = getFeatureTypeAttr();
		Attribute FEATURE_ID_ATTR = getFeatureIdAttr();
		Attribute TRANSCRIPT_BIOTYPE_ATTR = getTranscriptBiotypeAttr();
		Attribute RANK_TOTAL_ATTR = getRankTotalAttr();
		Attribute HGVS_C_ATTR = getHgvsCAttr();
		Attribute HGVS_P_ATTR = getHgvsPAttr();
		Attribute C_DNA_POSITION_ATTR = getCdnaPositionAttr();
		Attribute CDS_POSITION_ATTR = getCdsPositionAttr();
		Attribute PROTEIN_POSITION_ATTR = getProteinPositionAttr();
		Attribute DISTANCE_TO_FEATURE_ATTR = getDistanceToFeatureAttr();
		Attribute ERRORS_ATTR = getErrorsAttr();
		Attribute ANNOTATION_ATTR = getAnnotationAttr();

		LinkedList<Attribute> attributes = new LinkedList<>();
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

	public Attribute getTranscriptBiotypeAttr()
	{
		return attributeFactory.create()
							   .setName(TRANSCRIPT_BIOTYPE)
							   .setDataType(STRING)
							   .setDescription(
									   "The bare minimum is at least a description on whether the transcript is {“Coding”, “Noncoding”}. Whenever possible, use ENSEMBL biotypes.(source:http://snpeff.sourceforge.net)")
							   .setAggregatable(true);
	}

	public Attribute getRankTotalAttr()
	{
		return attributeFactory.create()
							   .setName(RANK_TOTAL)
							   .setDataType(STRING)
							   .setDescription(
									   "Exon or Intron rank / total number of exons or introns(source:http://snpeff.sourceforge.net)");
	}

	public Attribute getHgvsCAttr()
	{
		return attributeFactory.create()
							   .setName(HGVS_C)
							   .setDataType(TEXT)
							   .setDescription(
									   "Variant using HGVS notation (DNA level)(source:http://snpeff.sourceforge.net)");
	}

	public Attribute getHgvsPAttr()
	{
		return attributeFactory.create()
							   .setName(HGVS_P)
							   .setDataType(STRING)
							   .setDescription(
									   "If variant is coding, this field describes the variant using HGVS notation (Protein level). Since transcript ID is already mentioned in ‘feature ID’, it may be omitted here.(source:http://snpeff.sourceforge.net)");
	}

	public Attribute getCdnaPositionAttr()
	{
		return attributeFactory.create()
							   .setName(C_DNA_POSITION)
							   .setDataType(STRING)
							   .setDescription(
									   "Position in cDNA and trancript’s cDNA length (one based)(source:http://snpeff.sourceforge.net)");
	}

	public Attribute getCdsPositionAttr()
	{
		return attributeFactory.create()
							   .setName(CDS_POSITION)
							   .setDataType(STRING)
							   .setDescription(
									   "Position and number of coding bases (one based includes START and STOP codons)(source:http://snpeff.sourceforge.net)");
	}

	public Attribute getProteinPositionAttr()
	{
		return attributeFactory.create()
							   .setName(PROTEIN_POSITION)
							   .setDataType(STRING)
							   .setDescription("Position and number of AA (one based, including START, but not STOP)");
	}

	public Attribute getDistanceToFeatureAttr()
	{
		return attributeFactory.create()
							   .setName(DISTANCE_TO_FEATURE)
							   .setDataType(STRING)
							   .setDescription(
									   "All items in this field are options, so the field could be empty. Up/Downstream: Distance to first / last codon Intergenic: Distance to closest gene Distance to closest Intron boundary in exon (+/- up/downstream). If same, use positive number. Distance to closest exon boundary in Intron (+/- up/downstream) Distance to first base in MOTIF Distance to first base in miRNA Distance to exon-intron boundary in splice_site or splice _region ChipSeq peak: Distance to summit (or peak center) Histone mark / Histone state: Distance to summit (or peak center)(source:http://snpeff.sourceforge.net)");
	}

	public Attribute getErrorsAttr()
	{
		return attributeFactory.create()
							   .setName(ERRORS)
							   .setDataType(STRING)
							   .setDescription(
									   "Add errors, warnings oErrors, Warnings or Information messages: Add errors, warnings or r informative message that can affect annotation accuracy. It can be added using either ‘codes’ (as shown in column 1, e.g. W1) or ‘message types’ (as shown in column 2, e.g. WARNING_REF_DOES_NOT_MATCH_GENOME). All these errors, warnings or information messages messages are optional.(source:http://snpeff.sourceforge.net)");
	}

	public Attribute getAnnotationAttr()
	{
		return attributeFactory.create()
							   .setName(ANNOTATION)
							   .setDataType(STRING)
							   .setDescription(
									   "Annotated using Sequence Ontology terms. Multiple effects can be concatenated using ‘&’ (source:http://snpeff.sourceforge.net)")
							   .setAggregatable(true);
	}

	public Attribute getFeatureIdAttr()
	{
		return attributeFactory.create()
							   .setName(FEATURE_ID)
							   .setDataType(STRING)
							   .setDescription(
									   "Depending on the annotation, this may be: Transcript ID (preferably using version number), Motif ID, miRNA, ChipSeq peak, Histone mark, etc. Note: Some features may not have ID (e.g. histone marks from custom Chip-Seq experiments may not have a unique ID).(source:http://snpeff.sourceforge.net)");
	}

	public Attribute getFeatureTypeAttr()
	{
		return attributeFactory.create()
							   .setName(FEATURE_TYPE)
							   .setDataType(STRING)
							   .setDescription(
									   "Which type of feature is in the next field (e.g. transcript, motif, miRNA, etc.). It is preferred to use Sequence Ontology (SO) terms, but ‘custom’ (user defined) are allowed. ANN=A|stop_gained|HIGH|||transcript|... Tissue specific features may include cell type / tissue information separated by semicolon e.g.: ANN=A|histone_binding_site|LOW|||H3K4me3:HeLa-S3|...\n"
											   + "Feature ID: Depending on the annotation, this may be: Transcript ID (preferably using version number), Motif ID, miRNA, ChipSeq peak, Histone mark, etc. Note: Some features may not have ID (e.g. histone marks from custom Chip-Seq experiments may not have a unique ID). (source:http://snpeff.sourceforge.net)")
							   .setAggregatable(true);
	}

	public Attribute getGeneIdAttr()
	{
		return attributeFactory.create()
							   .setName(GENE_ID)
							   .setDataType(STRING)
							   .setDescription("Gene ID")
							   .setAggregatable(true);
	}

	public Attribute getGeneNameAttr()
	{
		return attributeFactory.create()
							   .setName(GENE_NAME)
							   .setDataType(STRING)
							   .setDescription(
									   "Common gene name (HGNC). Optional: use closest gene when the variant is “intergenic”(source:http://snpeff.sourceforge.net)")
							   .setAggregatable(true);
	}

	public Attribute getPutativeImpactAttr()
	{
		return attributeFactory.create()
							   .setName(PUTATIVE_IMPACT)
							   .setDataType(STRING)
							   .setDescription(
									   "A simple estimation of putative impact / deleteriousness : {HIGH, MODERATE, LOW, MODIFIER}(source:http://snpeff.sourceforge.net)")
							   .setAggregatable(true);
	}

	public Attribute getAltAttr()
	{
		return attributeFactory.create()
							   .setName(ALT)
							   .setDataType(TEXT)
							   .setDescription("The alternative allele on which this EFFECT applies");
	}
}
