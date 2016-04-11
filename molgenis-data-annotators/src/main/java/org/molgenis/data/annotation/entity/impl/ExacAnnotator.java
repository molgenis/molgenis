package org.molgenis.data.annotation.entity.impl;

import static org.molgenis.data.annotator.websettings.ExacAnnotatorSettings.Meta.EXAC_LOCATION;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.entity.AnnotatorInfo;
import org.molgenis.data.annotation.entity.AnnotatorInfo.Status;
import org.molgenis.data.annotation.entity.EntityAnnotator;
import org.molgenis.data.annotation.filter.MultiAllelicResultFilter;
import org.molgenis.data.annotation.cmd.cmdlineannotatorsettingsconfigurer.SingleFileLocationCmdLineAnnotatorSettingsConfigurer;
import org.molgenis.data.annotation.query.LocusQueryCreator;
import org.molgenis.data.annotation.resources.Resource;
import org.molgenis.data.annotation.resources.Resources;
import org.molgenis.data.annotation.resources.impl.ResourceImpl;
import org.molgenis.data.annotation.resources.impl.SingleResourceConfig;
import org.molgenis.data.annotation.resources.impl.TabixVcfRepositoryFactory;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExacAnnotator
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
	public static final DefaultAttributeMetaData EXAC_AF_ATTR = new DefaultAttributeMetaData(EXAC_AF,
			FieldTypeEnum.STRING).setDescription("The ExAC allele frequency").setLabel(EXAC_AF_LABEL);

	@Autowired
	private Entity exacAnnotatorSettings;

	@Autowired
	private DataService dataService;

	@Autowired
	private Resources resources;

	@Bean
	public RepositoryAnnotator exac()
	{

		DefaultAttributeMetaData outputAttribute_AF = EXAC_AF_ATTR;
		DefaultAttributeMetaData outputAttribute_AC_HOM = new DefaultAttributeMetaData(EXAC_AC_HOM,
				FieldTypeEnum.STRING).setDescription("The ExAC homozygous alternative genotype count")
						.setLabel(EXAC_AC_HOM_LABEL);
		DefaultAttributeMetaData outputAttribute_AC_HET = new DefaultAttributeMetaData(EXAC_AC_HET,
				FieldTypeEnum.STRING).setDescription("The ExAC heterozygous genotype count")
						.setLabel(EXAC_AC_HET_LABEL);

		List<AttributeMetaData> outputMetaData = new ArrayList<AttributeMetaData>(
				Arrays.asList(new DefaultAttributeMetaData[]
		{ outputAttribute_AF, outputAttribute_AC_HOM, outputAttribute_AC_HET }));

		List<AttributeMetaData> resourceMetaData = new ArrayList<AttributeMetaData>(
				Arrays.asList(new DefaultAttributeMetaData[]
		{ new DefaultAttributeMetaData(EXAC_AF_ResourceAttributeName, FieldTypeEnum.DECIMAL),
				new DefaultAttributeMetaData(EXAC_AC_HOM_ResourceAttributeName, FieldTypeEnum.INT),
				new DefaultAttributeMetaData(EXAC_AC_HET_ResourceAttributeName, FieldTypeEnum.INT) }));

		AnnotatorInfo exacInfo = AnnotatorInfo.create(Status.READY, AnnotatorInfo.Type.POPULATION_REFERENCE, "exac",
				" The Exome Aggregation Consortium (ExAC) is a coalition of investigators seeking to aggregate"
						+ " and harmonize exome sequencing data from a wide variety of large-scale sequencing projects"
						+ ", and to make summary data available for the wider scientific community.The data set provided"
						+ " on this website spans 60,706 unrelated individuals sequenced as part of various "
						+ "disease-specific and population genetic studies. ",
				outputMetaData);

		LocusQueryCreator locusQueryCreator = new LocusQueryCreator();
		MultiAllelicResultFilter multiAllelicResultFilter = new MultiAllelicResultFilter(resourceMetaData);
		EntityAnnotator entityAnnotator = new AnnotatorImpl(EXAC_TABIX_RESOURCE, exacInfo, locusQueryCreator,
				multiAllelicResultFilter, dataService, resources,
				new SingleFileLocationCmdLineAnnotatorSettingsConfigurer(EXAC_LOCATION, exacAnnotatorSettings))
		{
			@Override
			protected Object getResourceAttributeValue(AttributeMetaData attr, Entity sourceEntity)
			{
				String attrName = EXAC_AF.equals(attr.getName()) ? EXAC_AF_ResourceAttributeName
						: EXAC_AC_HOM.equals(attr.getName()) ? EXAC_AC_HOM_ResourceAttributeName
								: EXAC_AC_HET.equals(attr.getName()) ? EXAC_AC_HET_ResourceAttributeName
										: attr.getName();
				return sourceEntity.get(attrName);
			}
		};

		return new RepositoryAnnotatorImpl(entityAnnotator);
	}

	@Bean
	Resource exacResource()
	{
		Resource exacTabixResource = new ResourceImpl(EXAC_TABIX_RESOURCE,
				new SingleResourceConfig(EXAC_LOCATION, exacAnnotatorSettings),
				new TabixVcfRepositoryFactory(EXAC_TABIX_RESOURCE));

		return exacTabixResource;
	}
}
