package org.molgenis.data.annotation.entity.impl;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.entity.AnnotatorInfo;
import org.molgenis.data.annotation.entity.EntityAnnotator;
import org.molgenis.data.annotation.entity.AnnotatorInfo.Status;
import org.molgenis.data.annotation.resources.Resource;
import org.molgenis.data.annotation.resources.Resources;
import org.molgenis.data.annotation.resources.impl.ResourceImpl;
import org.molgenis.data.annotation.resources.impl.SingleResourceConfig;
import org.molgenis.data.annotation.resources.impl.TabixVcfRepositoryFactory;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.framework.server.MolgenisSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExacAnnotator
{

	public static final String EXAC_AF = "INFO_AF";
	public static final String EXAC_AF_LABEL = "EXAC_AF";

	public static final String EXAC_FILE_LOCATION_PROPERTY = "exac_location";
	public static final String EXAC_TABIX_RESOURCE = "EXACTabixResource";

	@Autowired
	private MolgenisSettings molgenisSettings;
	@Autowired
	private DataService dataService;
	@Autowired
	private Resources resources;

	@Bean
	public RepositoryAnnotator exac()
	{
		List<AttributeMetaData> attributes = new ArrayList<>();

		//TODO: description
		DefaultAttributeMetaData exac_af = new DefaultAttributeMetaData(EXAC_AF, FieldTypeEnum.STRING).setDescription(
				"TODO").setLabel(EXAC_AF_LABEL);


		attributes.add(exac_af);

		AnnotatorInfo exacInfo = AnnotatorInfo
				.create(Status.BETA,
						AnnotatorInfo.Type.POPULATION_REFERENCE,
						"exac",
						" The Exome Aggregation Consortium (ExAC) is a coalition of investigators seeking to aggregate"
								+ " and harmonize exome sequencing data from a wide variety of large-scale sequencing projects"
								+ ", and to make summary data available for the wider scientific community.The data set provided"
								+ " on this website spans 60,706 unrelated individuals sequenced as part of various "
								+ "disease-specific and population genetic studies. ", attributes);

		//TODO: properly test multiAllelicFresultFilter
		EntityAnnotator entityAnnotator = new AnnotatorImpl(EXAC_TABIX_RESOURCE, exacInfo, new LocusQueryCreator(),
				new MultiAllelicResultFilter(attributes), dataService, resources);

		return new RepositoryAnnotatorImpl(entityAnnotator);
	}

	@Bean
	Resource exacResource()
	{
		Resource exacTabixResource = new ResourceImpl(EXAC_TABIX_RESOURCE, new SingleResourceConfig(EXAC_FILE_LOCATION_PROPERTY,
				molgenisSettings), new TabixVcfRepositoryFactory(EXAC_TABIX_RESOURCE));

		return exacTabixResource;
	}
}
