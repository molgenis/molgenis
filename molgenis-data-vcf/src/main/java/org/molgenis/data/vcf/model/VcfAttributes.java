package org.molgenis.data.vcf.model;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.AttributeType.*;
import static org.molgenis.data.vcf.VcfRepository.DEFAULT_ATTRIBUTE_DESCRIPTION;

@Component
public class VcfAttributes
{
	public static final String CHROM = "#CHROM";
	public static final String ALT = "ALT";
	public static final String POS = "POS";
	public static final String REF = "REF";
	public static final String FILTER = "FILTER";
	public static final String QUAL = "QUAL";
	public static final String ID = "ID";
	public static final String INTERNAL_ID = "INTERNAL_ID";
	public static final String INFO = "INFO";
	public static final String FORMAT_GT = "GT";
	public static final String SAMPLES = "SAMPLES_ENTITIES";

	private final AttributeMetaDataFactory attributeMetaDataFactory;

	@Autowired
	public VcfAttributes(AttributeMetaDataFactory attributeMetaDataFactory)
	{
		this.attributeMetaDataFactory = requireNonNull(attributeMetaDataFactory);
	}

	public Attribute getChromAttribute()
	{
		return attributeMetaDataFactory.create().setName(CHROM).setDataType(STRING).setAggregatable(true)
				.setNillable(false).setDescription("The chromosome on which the variant is observed");
	}

	public Attribute getAltAttribute()
	{
		return attributeMetaDataFactory.create().setName(ALT).setDataType(TEXT).setAggregatable(true).setNillable(false)
				.setDescription("The alternative allele observed");
	}

	public Attribute getPosAttribute()
	{
		return attributeMetaDataFactory.create().setName(POS).setDataType(INT).setAggregatable(true).setNillable(false)
				.setDescription("The position on the chromosome which the variant is observed");
	}

	public Attribute getRefAttribute()
	{
		return attributeMetaDataFactory.create().setName(REF).setDataType(TEXT).setAggregatable(true).setNillable(false)
				.setDescription("The reference allele");
	}

	public Attribute getFilterAttribute()
	{
		return attributeMetaDataFactory.create().setName(FILTER).setDataType(STRING).setAggregatable(true)
				.setNillable(true).setDescription(DEFAULT_ATTRIBUTE_DESCRIPTION);
	}

	public Attribute getQualAttribute()
	{
		return attributeMetaDataFactory.create().setName(QUAL).setDataType(STRING).setAggregatable(true)
				.setNillable(true).setDescription(DEFAULT_ATTRIBUTE_DESCRIPTION);
	}

	public Attribute getIdAttribute()
	{
		return attributeMetaDataFactory.create().setName(ID).setDataType(STRING).setNillable(true)
				.setDescription(DEFAULT_ATTRIBUTE_DESCRIPTION);
	}
}
