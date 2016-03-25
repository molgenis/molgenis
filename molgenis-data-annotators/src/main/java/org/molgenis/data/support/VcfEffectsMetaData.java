package org.molgenis.data.support;

import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.STRING;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.TEXT;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;

public class VcfEffectsMetaData extends DefaultEntityMetaData
{
	public static final String ENTITY_NAME_SUFFIX = "_EFFECTS";

	public static final String ID = "id";
	public static final String ALT = "ALT";
	public static final String GENE = "Gene_Name";
	public static final String VARIANT = "VARIANT";

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

	public VcfEffectsMetaData(EntityMetaData sourceEMD)
	{
		super(sourceEMD.getSimpleName() + ENTITY_NAME_SUFFIX, sourceEMD.getPackage());

		setBackend(sourceEMD.getBackend());

		addAttribute(ID, ROLE_ID).setAuto(true).setVisible(false);

		addAttribute(ALT);
		addAttribute(GENE);
		addAttribute(VARIANT).setNillable(false).setDataType(MolgenisFieldTypes.XREF).setRefEntity(sourceEMD);

		for (AttributeMetaData attr : createAttributes())
		{
			addAttributeMetaData(attr);
		}
	}

	public static List<AttributeMetaData> createAttributes()
	{
		List<AttributeMetaData> attributes = new ArrayList<>();

		DefaultAttributeMetaData annotation = new DefaultAttributeMetaData(ANNOTATION, STRING);
		annotation.setDescription(
				"Annotated using Sequence Ontology terms. Multiple effects can be concatenated using ‘&’ (source:http://snpeff.sourceforge.net)");
		attributes.add(annotation);

		DefaultAttributeMetaData putative_impact = new DefaultAttributeMetaData(PUTATIVE_IMPACT, STRING);
		putative_impact.setDescription(
				" A simple estimation of putative impact / deleteriousness : {HIGH, MODERATE, LOW, MODIFIER}(source:http://snpeff.sourceforge.net)");
		attributes.add(putative_impact);

		DefaultAttributeMetaData gene_name = new DefaultAttributeMetaData(GENE_NAME, STRING);
		gene_name.setDescription(
				"Common gene name (HGNC). Optional: use closest gene when the variant is “intergenic”(source:http://snpeff.sourceforge.net)");
		attributes.add(gene_name);

		DefaultAttributeMetaData gene_id = new DefaultAttributeMetaData(GENE_ID, STRING);
		gene_id.setDescription("Gene ID");
		attributes.add(gene_id);

		DefaultAttributeMetaData feature_type = new DefaultAttributeMetaData(FEATURE_TYPE, STRING);
		feature_type.setDescription(
				"Which type of feature is in the next field (e.g. transcript, motif, miRNA, etc.). It is preferred to use Sequence Ontology (SO) terms, but ‘custom’ (user defined) are allowed. ANN=A|stop_gained|HIGH|||transcript|... Tissue specific features may include cell type / tissue information separated by semicolon e.g.: ANN=A|histone_binding_site|LOW|||H3K4me3:HeLa-S3|...\n"
						+ "Feature ID: Depending on the annotation, this may be: Transcript ID (preferably using version number), Motif ID, miRNA, ChipSeq peak, Histone mark, etc. Note: Some features may not have ID (e.g. histone marks from custom Chip-Seq experiments may not have a unique ID). (source:http://snpeff.sourceforge.net)");
		attributes.add(feature_type);

		DefaultAttributeMetaData feature_id = new DefaultAttributeMetaData(FEATURE_ID, STRING);
		feature_id.setDescription(
				"Depending on the annotation, this may be: Transcript ID (preferably using version number), Motif ID, miRNA, ChipSeq peak, Histone mark, etc. Note: Some features may not have ID (e.g. histone marks from custom Chip-Seq experiments may not have a unique ID).(source:http://snpeff.sourceforge.net)");
		attributes.add(feature_id);

		DefaultAttributeMetaData transcript_biotype = new DefaultAttributeMetaData(TRANSCRIPT_BIOTYPE, STRING);
		transcript_biotype.setDescription(
				"The bare minimum is at least a description on whether the transcript is {“Coding”, “Noncoding”}. Whenever possible, use ENSEMBL biotypes.(source:http://snpeff.sourceforge.net)");
		attributes.add(transcript_biotype);

		DefaultAttributeMetaData rank_total = new DefaultAttributeMetaData(RANK_TOTAL, STRING);
		rank_total.setDescription(
				"Exon or Intron rank / total number of exons or introns(source:http://snpeff.sourceforge.net)");
		attributes.add(rank_total);

		DefaultAttributeMetaData HGVS_c = new DefaultAttributeMetaData(HGVS_C, TEXT);
		HGVS_c.setDescription("Variant using HGVS notation (DNA level)(source:http://snpeff.sourceforge.net)");
		attributes.add(HGVS_c);

		DefaultAttributeMetaData HGVS_p = new DefaultAttributeMetaData(HGVS_P, STRING);
		HGVS_p.setDescription(
				"If variant is coding, this field describes the variant using HGVS notation (Protein level). Since transcript ID is already mentioned in ‘feature ID’, it may be omitted here.(source:http://snpeff.sourceforge.net)");
		attributes.add(HGVS_p);

		DefaultAttributeMetaData cDNA_position = new DefaultAttributeMetaData(C_DNA_POSITION, STRING);
		cDNA_position.setDescription(
				"Position in cDNA and trancript’s cDNA length (one based)(source:http://snpeff.sourceforge.net)");
		attributes.add(cDNA_position);

		DefaultAttributeMetaData CDS_position = new DefaultAttributeMetaData(CDS_POSITION, STRING);
		CDS_position.setDescription(
				"Position and number of coding bases (one based includes START and STOP codons)(source:http://snpeff.sourceforge.net)");
		attributes.add(CDS_position);

		DefaultAttributeMetaData Protein_position = new DefaultAttributeMetaData(PROTEIN_POSITION, STRING);
		Protein_position.setDescription("Position and number of AA (one based, including START, but not STOP)");
		attributes.add(Protein_position);

		DefaultAttributeMetaData Distance_to_feature = new DefaultAttributeMetaData(DISTANCE_TO_FEATURE, STRING);
		Distance_to_feature.setDescription(
				"All items in this field are options, so the field could be empty. Up/Downstream: Distance to first / last codon Intergenic: Distance to closest gene Distance to closest Intron boundary in exon (+/- up/downstream). If same, use positive number. Distance to closest exon boundary in Intron (+/- up/downstream) Distance to first base in MOTIF Distance to first base in miRNA Distance to exon-intron boundary in splice_site or splice _region ChipSeq peak: Distance to summit (or peak center) Histone mark / Histone state: Distance to summit (or peak center)(source:http://snpeff.sourceforge.net)");
		attributes.add(Distance_to_feature);

		DefaultAttributeMetaData Errors = new DefaultAttributeMetaData(ERRORS, STRING);
		Errors.setDescription(
				"Add errors, warnings oErrors, Warnings or Information messages: Add errors, warnings or r informative message that can affect annotation accuracy. It can be added using either ‘codes’ (as shown in column 1, e.g. W1) or ‘message types’ (as shown in column 2, e.g. WARNING_REF_DOES_NOT_MATCH_GENOME). All these errors, warnings or information messages messages are optional.(source:http://snpeff.sourceforge.net)");
		attributes.add(Errors);

		return attributes;
	}
}
