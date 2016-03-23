package org.molgenis.data.support;

import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.STRING;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.BOOL;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Package;

public class VcfEffectsMetaData extends DefaultEntityMetaData
{
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

	public static final DefaultAttributeMetaData GENE_NAME_ATTR = new DefaultAttributeMetaData(GENE_NAME, BOOL).setDescription(
			"Common gene name (HGNC). Optional: use closest gene when the variant is “intergenic”(source:http://snpeff.sourceforge.net)");
	public static final DefaultAttributeMetaData IMPACT_ATTR = new DefaultAttributeMetaData(PUTATIVE_IMPACT, STRING).setDescription(
			" A simple estimation of putative impact / deleteriousness : {HIGH, MODERATE, LOW, MODIFIER}(source:http://snpeff.sourceforge.net)");


	public VcfEffectsMetaData(String entityName, Package package_, EntityMetaData sourceEMD)
	{
		super(entityName, package_);

		addAttribute(ID, ROLE_ID).setAuto(true).setVisible(false);
		addAttribute(ALT).setNillable(false);
		addAttribute(GENE).setNillable(false);

		addAttribute(VARIANT).setNillable(false).setDataType(MolgenisFieldTypes.XREF).setRefEntity(sourceEMD);

		addAttribute(ANNOTATION);
		addAttribute(PUTATIVE_IMPACT);
		addAttribute(GENE_NAME);
		addAttribute(GENE_ID);
		addAttribute(FEATURE_TYPE);
		addAttribute(FEATURE_ID);
		addAttribute(TRANSCRIPT_BIOTYPE);
		addAttribute(RANK_TOTAL);
		addAttribute(HGVS_C);
		addAttribute(HGVS_P);
		addAttribute(C_DNA_POSITION);
		addAttribute(CDS_POSITION);
		addAttribute(PROTEIN_POSITION);
		addAttribute(DISTANCE_TO_FEATURE);
		addAttribute(ERRORS);
	}
}
