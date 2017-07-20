package org.molgenis.data.annotation.core.entity.impl;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.annotation.core.entity.AnnotatorConfig;
import org.molgenis.data.annotation.core.entity.AnnotatorInfo;
import org.molgenis.data.annotation.core.entity.AnnotatorInfo.Status;
import org.molgenis.data.annotation.core.entity.EntityAnnotator;
import org.molgenis.data.annotation.core.entity.impl.framework.AbstractAnnotator;
import org.molgenis.data.annotation.core.entity.impl.framework.RepositoryAnnotatorImpl;
import org.molgenis.data.annotation.core.filter.ClinvarMultiAllelicResultFilter;
import org.molgenis.data.annotation.core.query.LocusQueryCreator;
import org.molgenis.data.annotation.core.resources.Resource;
import org.molgenis.data.annotation.core.resources.Resources;
import org.molgenis.data.annotation.core.resources.impl.RepositoryFactory;
import org.molgenis.data.annotation.core.resources.impl.ResourceImpl;
import org.molgenis.data.annotation.core.resources.impl.SingleResourceConfig;
import org.molgenis.data.annotation.core.resources.impl.tabix.TabixVcfRepositoryFactory;
import org.molgenis.data.annotation.web.settings.SingleFileLocationCmdLineAnnotatorSettingsConfigurer;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

import static org.molgenis.data.annotation.web.settings.ClinvarAnnotatorSettings.Meta.CLINVAR_LOCATION;
import static org.molgenis.data.meta.AttributeType.STRING;

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
	private AttributeFactory attributeFactory;

	@Autowired
	private EntityTypeFactory entityTypeFactory;

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
		List<Attribute> attributes = createClinvarOutputAttributes();

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
		EntityAnnotator entityAnnotator = new AbstractAnnotator(CLINVAR_TABIX_RESOURCE, clinvarInfo, locusQueryCreator,
				clinvarMultiAllelicResultFilter, dataService, resources,
				new SingleFileLocationCmdLineAnnotatorSettingsConfigurer(CLINVAR_LOCATION, clinvarAnnotatorSettings))
		{
			@Override
			public List<Attribute> createAnnotatorAttributes(AttributeFactory attributeFactory)
			{
				return createClinvarOutputAttributes();
			}

			@Override
			protected Object getResourceAttributeValue(Attribute attr, Entity sourceEntity)
			{
				String attrName;
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

	private List<Attribute> createClinvarOutputAttributes()
	{
		List<Attribute> attributes = new ArrayList<>();

		Attribute clinvar_clnsig = attributeFactory.create()
												   .setName(CLINVAR_CLNSIG)
												   .setDataType(STRING)
												   .setDescription(
														   "Value representing clinical significant allele 0 means ref 1 means first alt allele etc.")
												   .setLabel(CLINVAR_CLNSIG_LABEL);

		Attribute clinvar_clnalle = attributeFactory.create()
													.setName(CLINVAR_CLNALLE)
													.setDataType(STRING)
													.setDescription(
															"Value representing the clinical significanct according to ClinVar")
													.setLabel(CLINVAR_CLNALLE_LABEL);

		attributes.add(clinvar_clnsig);
		attributes.add(clinvar_clnalle);
		return attributes;
	}

	@Bean
	Resource clinVarTabixResource()
	{
		return new ResourceImpl(CLINVAR_TABIX_RESOURCE,
				new SingleResourceConfig(CLINVAR_LOCATION, clinvarAnnotatorSettings))
		{
			@Override
			public RepositoryFactory getRepositoryFactory()
			{
				return new TabixVcfRepositoryFactory(CLINVAR_TABIX_RESOURCE, vcfAttributes, entityTypeFactory,
						attributeFactory);
			}
		};
	}
}
