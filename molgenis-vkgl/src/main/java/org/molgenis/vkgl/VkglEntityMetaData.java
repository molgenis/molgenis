package org.molgenis.vkgl;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.meta.PackageImpl;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;

public class VkglEntityMetaData extends DefaultEntityMetaData
{
	public static final VkglEntityMetaData INSTANCE = new VkglEntityMetaData();

	public static final String ENTITY_NAME = "Vkgl";
	public static final String PACKAGE_NAME = "vkgl";

	public static final String INTERNAL_ID = "INTERNAL_ID";
	public static final String CHROM = "#CHROM";
	public static final String ALLELE1 = "ALLELE1";
	public static final String ALLELE2 = "ALLELE2";
	public static final String POS = "POS";
	public static final String REF = "REF";
	public static final String FILTER = "FILTER";
	public static final String QUAL = "QUAL";
	public static final String ID = "ID";
	public static final String GT = "GT";

	public VkglEntityMetaData()
	{
		super(ENTITY_NAME, new PackageImpl(PACKAGE_NAME, "VKGL package"));

		addAttributeMetaData(new DefaultAttributeMetaData(INTERNAL_ID, MolgenisFieldTypes.FieldTypeEnum.STRING)
				.setIdAttribute(true).setNillable(false).setVisible(false));
		addAttributeMetaData(new DefaultAttributeMetaData(CHROM, MolgenisFieldTypes.FieldTypeEnum.STRING)
				.setAggregateable(true));
		addAttributeMetaData(new DefaultAttributeMetaData(ALLELE1, MolgenisFieldTypes.FieldTypeEnum.STRING)
				.setAggregateable(true));
		addAttributeMetaData(new DefaultAttributeMetaData(ALLELE2, MolgenisFieldTypes.FieldTypeEnum.STRING)
				.setAggregateable(true));
		addAttributeMetaData(new DefaultAttributeMetaData(POS, MolgenisFieldTypes.FieldTypeEnum.LONG)
				.setAggregateable(true));
		addAttributeMetaData(new DefaultAttributeMetaData(REF, MolgenisFieldTypes.FieldTypeEnum.STRING)
				.setAggregateable(true));
		addAttributeMetaData(new DefaultAttributeMetaData(FILTER, MolgenisFieldTypes.FieldTypeEnum.STRING)
				.setAggregateable(true));
		addAttributeMetaData(new DefaultAttributeMetaData(QUAL, MolgenisFieldTypes.FieldTypeEnum.STRING)
				.setAggregateable(true));
		addAttributeMetaData(new DefaultAttributeMetaData(ID, MolgenisFieldTypes.FieldTypeEnum.STRING)
				.setAggregateable(true));
		addAttributeMetaData(new DefaultAttributeMetaData(GT, MolgenisFieldTypes.FieldTypeEnum.STRING)
				.setAggregateable(true));
	}

}
