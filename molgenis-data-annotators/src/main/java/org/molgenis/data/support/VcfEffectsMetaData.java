package org.molgenis.data.support;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;

import java.util.LinkedList;

import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.BOOL;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.STRING;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;

public class VcfEffectsMetaData extends DefaultEntityMetaData {
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

    public static final DefaultAttributeMetaData GENE_NAME_ATTR = new DefaultAttributeMetaData(GENE_NAME, BOOL).setDescription(
            "Common gene name (HGNC). Optional: use closest gene when the variant is “intergenic”(source:http://snpeff.sourceforge.net)");
    public static final DefaultAttributeMetaData IMPACT_ATTR = new DefaultAttributeMetaData(PUTATIVE_IMPACT, STRING).setDescription(
            " A simple estimation of putative impact / deleteriousness : {HIGH, MODERATE, LOW, MODIFIER}(source:http://snpeff.sourceforge.net)");


    public VcfEffectsMetaData(EntityMetaData sourceEMD) {
        super(sourceEMD.getSimpleName() + ENTITY_NAME_SUFFIX, sourceEMD.getPackage());
        for (AttributeMetaData attr : getOrderedAttributeList(sourceEMD)) {
            if (attr.getName().equals(ID)) {
                addAttributeMetaData(attr, ROLE_ID);
            } else {
                addAttributeMetaData(attr);
            }
        }
        setBackend(sourceEMD.getBackend());


    }

    public static LinkedList<AttributeMetaData> getOrderedAttributeList(EntityMetaData sourceEMD) {
        LinkedList<AttributeMetaData> attributes = new LinkedList<>();
        attributes.add(new DefaultAttributeMetaData(ID).setAuto(true).setVisible(false));

        attributes.add(new DefaultAttributeMetaData(ALT));
        attributes.add(new DefaultAttributeMetaData(GENE));
        attributes.add(new DefaultAttributeMetaData(VARIANT).setNillable(false).setDataType(MolgenisFieldTypes.XREF).setRefEntity(sourceEMD));
        attributes.add(new DefaultAttributeMetaData(ANNOTATION));
        attributes.add(new DefaultAttributeMetaData(PUTATIVE_IMPACT));
        attributes.add(new DefaultAttributeMetaData(GENE_NAME));
        attributes.add(new DefaultAttributeMetaData(GENE_ID));
        attributes.add(new DefaultAttributeMetaData(FEATURE_TYPE));
        attributes.add(new DefaultAttributeMetaData(FEATURE_ID));
        attributes.add(new DefaultAttributeMetaData(TRANSCRIPT_BIOTYPE));
        attributes.add(new DefaultAttributeMetaData(RANK_TOTAL));
        attributes.add(new DefaultAttributeMetaData(HGVS_C));
        attributes.add(new DefaultAttributeMetaData(HGVS_P));
        attributes.add(new DefaultAttributeMetaData(C_DNA_POSITION));
        attributes.add(new DefaultAttributeMetaData(CDS_POSITION));
        attributes.add(new DefaultAttributeMetaData(PROTEIN_POSITION));
        attributes.add(new DefaultAttributeMetaData(DISTANCE_TO_FEATURE));
        attributes.add(new DefaultAttributeMetaData(ERRORS));
        return attributes;
    }
}
