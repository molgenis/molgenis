package org.molgenis.data.annotation.core.entity.impl;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.annotation.core.entity.AnnotatorConfig;
import org.molgenis.data.annotation.core.entity.AnnotatorInfo;
import org.molgenis.data.annotation.core.entity.AnnotatorInfo.Status;
import org.molgenis.data.annotation.core.entity.EntityAnnotator;
import org.molgenis.data.annotation.core.entity.impl.framework.AnnotatorImpl;
import org.molgenis.data.annotation.core.entity.impl.framework.RepositoryAnnotatorImpl;
import org.molgenis.data.annotation.core.filter.MultiAllelicResultFilter;
import org.molgenis.data.annotation.core.query.LocusQueryCreator;
import org.molgenis.data.annotation.core.resources.Resource;
import org.molgenis.data.annotation.core.resources.Resources;
import org.molgenis.data.annotation.core.resources.impl.RepositoryFactory;
import org.molgenis.data.annotation.core.resources.impl.ResourceImpl;
import org.molgenis.data.annotation.core.resources.impl.SingleResourceConfig;
import org.molgenis.data.annotation.core.resources.impl.tabix.TabixVcfRepositoryFactory;
import org.molgenis.data.annotation.web.settings.SingleFileLocationCmdLineAnnotatorSettingsConfigurer;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.molgenis.MolgenisFieldTypes.AttributeType.STRING;
import static org.molgenis.data.annotation.web.settings.ExacAnnotatorSettings.Meta.EXAC_LOCATION;

@Configuration
public class ExacAnnotator implements AnnotatorConfig
{
	public static final String NAME = "exac";

	public static final String EXAC_AF = "EXAC_AF";
	public static final String EXAC_AC_HOM = "EXAC_AC_HOM";
	public static final String EXAC_AC_HET = "EXAC_AC_HET";
	public static final String EXAC_AF_LABEL = "ExAC allele frequency";
	public static final String EXAC_AC_HOM_LABEL = "ExAC homozygous alternative genotype count";
	public static final String EXAC_AC_HET_LABEL = "ExAC heterozygous genotype count";
	public static final String EXAC_AF_ResourceAttributeName = "AF";
	public static final String EXAC_AC_HOM_ResourceAttributeName = "AC_Hom";
	public static final String EXAC_AC_HET_ResourceAttributeName = "AC_Het";

	public static final String EXAC_TABIX_RESOURCE = "EXACTabixResource";

	@Autowired
	private Entity exacAnnotatorSettings;

	@Autowired
	private DataService dataService;

	@Autowired
	private Resources resources;

	@Autowired
	private VcfAttributes vcfAttributes;

	@Autowired
	private EntityMetaDataFactory entityMetaDataFactory;

	@Autowired
	private AttributeMetaDataFactory attributeMetaDataFactory;
	private RepositoryAnnotatorImpl annotator;

	@Bean
	public RepositoryAnnotator exac()
	{
		annotator = new RepositoryAnnotatorImpl(NAME);
		return annotator;
	}

	@Override
	public void init()
	{

		Attribute outputAttribute_AF = getExacAFAttr(attributeMetaDataFactory);
		Attribute outputAttribute_AC_HOM = getExacAcHomAttr(attributeMetaDataFactory);
		Attribute outputAttribute_AC_HET = getExacAcHetAttr(attributeMetaDataFactory);

		List<Attribute> outputMetaData = new ArrayList<Attribute>(Arrays.asList(
				new Attribute[] { outputAttribute_AF, outputAttribute_AC_HOM, outputAttribute_AC_HET }));

		List<Attribute> resourceMetaData = new ArrayList<Attribute>(Arrays.asList(
				new Attribute[] {
						attributeMetaDataFactory.create().setName(EXAC_AF_ResourceAttributeName).setDataType(STRING),
						attributeMetaDataFactory.create().setName(EXAC_AC_HOM_ResourceAttributeName).setDataType(
								STRING),
						attributeMetaDataFactory.create().setName(EXAC_AC_HET_ResourceAttributeName).setDataType(
								STRING) }));

		AnnotatorInfo exacInfo = AnnotatorInfo.create(Status.READY, AnnotatorInfo.Type.POPULATION_REFERENCE, "exac",
				" The Exome Aggregation Consortium (ExAC) is a coalition of investigators seeking to aggregate"
						+ " and harmonize exome sequencing data from a wide variety of large-scale sequencing projects"
						+ ", and to make summary data available for the wider scientific community.The data set provided"
						+ " on this website spans 60,706 unrelated individuals sequenced as part of various "
						+ "disease-specific and population genetic studies. ", outputMetaData);

		// TODO: properly test multiAllelicFresultFilter
		LocusQueryCreator locusQueryCreator = new LocusQueryCreator(vcfAttributes);
		MultiAllelicResultFilter multiAllelicResultFilter = new MultiAllelicResultFilter(resourceMetaData,
				vcfAttributes);
		EntityAnnotator entityAnnotator = new AnnotatorImpl(EXAC_TABIX_RESOURCE, exacInfo, locusQueryCreator,
				multiAllelicResultFilter, dataService, resources,
				new SingleFileLocationCmdLineAnnotatorSettingsConfigurer(EXAC_LOCATION, exacAnnotatorSettings))
		{
			@Override
			protected Object getResourceAttributeValue(Attribute attr, Entity sourceEntity)
			{
				String attrName = EXAC_AF.equals(attr.getName()) ? EXAC_AF_ResourceAttributeName : EXAC_AC_HOM
						.equals(attr.getName()) ? EXAC_AC_HOM_ResourceAttributeName : EXAC_AC_HET
						.equals(attr.getName()) ? EXAC_AC_HET_ResourceAttributeName : attr.getName();
				return sourceEntity.get(attrName);
			}
		};

		annotator.init(entityAnnotator);
	}

	public static Attribute getExacAcHetAttr(AttributeMetaDataFactory attributeMetaDataFactory)
	{
		return attributeMetaDataFactory.create().setName(EXAC_AC_HET).setDataType(STRING)
				.setDescription("The ExAC heterozygous genotype count").setLabel(EXAC_AC_HET_LABEL);
	}

	public static Attribute getExacAcHomAttr(AttributeMetaDataFactory attributeMetaDataFactory)
	{
		return attributeMetaDataFactory.create().setName(EXAC_AC_HOM).setDataType(STRING)
				.setDescription("The ExAC homozygous alternative genotype count").setLabel(EXAC_AC_HOM_LABEL);
	}

	public static Attribute getExacAFAttr(AttributeMetaDataFactory attributeMetaDataFactory)
	{
		return attributeMetaDataFactory.create().setName(EXAC_AF).setDataType(STRING)
				.setDescription("The ExAC allele frequency").setLabel(EXAC_AF_LABEL);
	}

	@Bean
	Resource exacResource()
	{
		Resource exacTabixResource = new ResourceImpl(EXAC_TABIX_RESOURCE,
				new SingleResourceConfig(EXAC_LOCATION, exacAnnotatorSettings))
		{
			@Override
			public RepositoryFactory getRepositoryFactory()
			{
				return new TabixVcfRepositoryFactory(EXAC_TABIX_RESOURCE, vcfAttributes, entityMetaDataFactory,
						attributeMetaDataFactory);
			}
		};

		return exacTabixResource;
	}
}
