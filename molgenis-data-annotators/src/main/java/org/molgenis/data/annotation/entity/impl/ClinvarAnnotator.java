package org.molgenis.data.annotation.entity.impl;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.cmd.cmdlineannotatorsettingsconfigurer.SingleFileLocationCmdLineAnnotatorSettingsConfigurer;
import org.molgenis.data.annotation.entity.AnnotatorConfig;
import org.molgenis.data.annotation.entity.AnnotatorInfo;
import org.molgenis.data.annotation.entity.AnnotatorInfo.Status;
import org.molgenis.data.annotation.entity.EntityAnnotator;
import org.molgenis.data.annotation.filter.ClinvarMultiAllelicResultFilter;
import org.molgenis.data.annotation.query.LocusQueryCreator;
import org.molgenis.data.annotation.resources.Resource;
import org.molgenis.data.annotation.resources.Resources;
import org.molgenis.data.annotation.resources.impl.RepositoryFactory;
import org.molgenis.data.annotation.resources.impl.ResourceImpl;
import org.molgenis.data.annotation.resources.impl.SingleResourceConfig;
import org.molgenis.data.annotation.resources.impl.TabixVcfRepositoryFactory;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

import static org.molgenis.MolgenisFieldTypes.AttributeType.STRING;
import static org.molgenis.data.annotator.websettings.ClinvarAnnotatorSettings.Meta.CLINVAR_LOCATION;

@Configuration
public class ClinvarAnnotator implements AnnotatorConfig
{
	public static final String NAME = "clinvar";
	public static final String CLINVAR_CLNSIG = "CLINVAR_CLNSIG";
	public static final String CLINVAR_CLNSIG_LABEL = "ClinVar clinical significance";
	public static final String CLINVAR_CLNSIG_ResourceAttributeName = "CLNSIG";
	public static final String CLINVAR_CLNALLE = "CLINVAR_CLNALLE";
	public static final String CLINVAR_CLNALLE_LABEL = "ClinVar clinical significant allele";
	public static final String CLINVAR_CLINALL_ResourceAttributeName = "CLNALLE";
	public static final String CLINVAR_TABIX_RESOURCE = "clinVarTabixResource";

	@Autowired
	private Entity clinvarAnnotatorSettings;

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
	public RepositoryAnnotator clinvar()
	{
		annotator = new RepositoryAnnotatorImpl(NAME);
		return annotator;
	}

	@Override
	public void init()
	{
		List<AttributeMetaData> attributes = new ArrayList<>();

		AttributeMetaData clinvar_clnsig = attributeMetaDataFactory.create().setName(CLINVAR_CLNSIG).setDataType(STRING)
				.setDescription(
						"Value representing clinical significant allele 0 means ref 1 means first alt allele etc.")
				.setLabel(CLINVAR_CLNSIG_LABEL);

		AttributeMetaData clinvar_clnalle = attributeMetaDataFactory.create().setName(CLINVAR_CLNALLE)
				.setDataType(STRING).setDescription("Value representing the clinical significanct according to ClinVar")
				.setLabel(CLINVAR_CLNALLE_LABEL);

		attributes.add(clinvar_clnsig);
		attributes.add(clinvar_clnalle);

		AnnotatorInfo clinvarInfo = AnnotatorInfo.create(Status.READY, AnnotatorInfo.Type.PATHOGENICITY_ESTIMATE, NAME,
				" ClinVar is a freely accessible, public archive of reports of the relationships"
						+ " among human variations and phenotypes, with supporting evidence. ClinVar thus facilitates"
						+ " access to and communication about the relationships asserted between human variation and "
						+ "observed health status, and the history of that interpretation. ClinVar collects reports "
						+ "of variants found in patient samples, assertions made regarding their clinical significance, "
						+ "information about the submitter, and other supporting data. The alleles described in submissions "
						+ "are mapped to reference sequences, and reported according to the HGVS standard. ClinVar then "
						+ "presents the data for interactive users as well as those wishing to use ClinVar in daily "
						+ "workflows and other local applications. ClinVar works in collaboration with interested "
						+ "organizations to meet the needs of the medical genetics community as efficiently and effectively "
						+ "as possible. Information about using ClinVar is available at: http://www.ncbi.nlm.nih.gov/clinvar/docs/help/.",
				attributes);

		LocusQueryCreator locusQueryCreator = new LocusQueryCreator(vcfAttributes);
		ClinvarMultiAllelicResultFilter clinvarMultiAllelicResultFilter = new ClinvarMultiAllelicResultFilter(
				vcfAttributes);
		EntityAnnotator entityAnnotator = new AnnotatorImpl(CLINVAR_TABIX_RESOURCE, clinvarInfo, locusQueryCreator,
				clinvarMultiAllelicResultFilter, dataService, resources,
				new SingleFileLocationCmdLineAnnotatorSettingsConfigurer(CLINVAR_LOCATION, clinvarAnnotatorSettings))
		{
			@Override
			protected Object getResourceAttributeValue(AttributeMetaData attr, Entity sourceEntity)
			{
				String attrName = null;
				if (CLINVAR_CLNSIG.equals(attr.getName()))
				{
					attrName = CLINVAR_CLNSIG_ResourceAttributeName;
				}
				else if (CLINVAR_CLNALLE.equals(attr.getName()))
				{
					attrName = CLINVAR_CLINALL_ResourceAttributeName;
				}
				else
				{

					attrName = attr.getName();
				}
				return sourceEntity.get(attrName);
			}
		};

		annotator.init(entityAnnotator);
	}

	@Bean
	Resource clinVarTabixResource()
	{
		Resource clinVarTabixResource = new ResourceImpl(CLINVAR_TABIX_RESOURCE,
				new SingleResourceConfig(CLINVAR_LOCATION, clinvarAnnotatorSettings))
		{
			@Override
			public RepositoryFactory getRepositoryFactory()
			{
				return new TabixVcfRepositoryFactory(CLINVAR_TABIX_RESOURCE);
			}
		};

		return clinVarTabixResource;
	}
}
