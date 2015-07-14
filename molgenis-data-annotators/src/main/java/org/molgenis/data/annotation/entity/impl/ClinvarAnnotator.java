package org.molgenis.data.annotation.entity.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.entity.AnnotatorInfo;
import org.molgenis.data.annotation.entity.AnnotatorInfo.Status;
import org.molgenis.data.annotation.entity.EntityAnnotator;
import org.molgenis.data.annotation.resources.Resource;
import org.molgenis.data.annotation.resources.Resources;
import org.molgenis.data.annotation.resources.impl.ResourceImpl;
import org.molgenis.data.annotation.resources.impl.SingleResourceConfig;
import org.molgenis.data.annotation.resources.impl.TabixVcfRepositoryFactory;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.framework.server.MolgenisSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClinvarAnnotator
{
	// TODO Write test
	public static final String CLINVAR_CLNSIG = "CLINVAR_CLNSIG";
	public static final String CLINVAR_CLNSIG_LABEL = "ClinVar clinical significance";
	public static final String CLINVAR_CLNSIG_ResourceAttributeName = VcfRepository.getInfoPrefix() + "CLNSIG";
	public static final String CLINVAR_CLNALLE = "CLINVAR_CLNALLE";
	public static final String CLINVAR_CLNALLE_LABEL = "ClinVar clinical significant allele";
	public static final String CLINVAR_CLINALL_ResourceAttributeName = VcfRepository.getInfoPrefix() + "CLNALLE";
	public static final String CLINVAR_FILE_LOCATION_PROPERTY = "clinvar_location";
	public static final String CLINVAR_TABIX_RESOURCE = "clinVarTabixResource";

	@Autowired
	private MolgenisSettings molgenisSettings;
	@Autowired
	private DataService dataService;
	@Autowired
	private Resources resources;

	@Bean
	public RepositoryAnnotator clinvar()
	{
		// TODO: description
		List<AttributeMetaData> attributes = new ArrayList<>();
		
		DefaultAttributeMetaData clinvar_clnsig = new DefaultAttributeMetaData(CLINVAR_CLNSIG, FieldTypeEnum.STRING)
				.setDescription("Value representing clinical significant allele 0 means ref 1 means first alt allele etc.").setLabel(CLINVAR_CLNSIG_LABEL);
		
		DefaultAttributeMetaData clinvar_clnalle = new DefaultAttributeMetaData(CLINVAR_CLNALLE, FieldTypeEnum.STRING)
		.setDescription("Value representing the clinical significanct according to ClinVar").setLabel(CLINVAR_CLNALLE_LABEL);
		
		attributes.add(clinvar_clnsig);
		attributes.add(clinvar_clnalle);
		
		AnnotatorInfo clinvarInfo = AnnotatorInfo
				.create(Status.READY,
						AnnotatorInfo.Type.PATHOGENICITY_ESTIMATE,
						"clinvar",
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

		// TODO: properly test multiAllelicFresultFilter
		LocusQueryCreator locusQueryCreator = new LocusQueryCreator();
		MultiAllelicResultFilter multiAllelicResultFilter = new MultiAllelicResultFilter(
				Collections.singletonList(new DefaultAttributeMetaData(CLINVAR_CLNSIG_ResourceAttributeName,
						FieldTypeEnum.STRING)));
		EntityAnnotator entityAnnotator = new AnnotatorImpl(CLINVAR_TABIX_RESOURCE, clinvarInfo, locusQueryCreator,
				multiAllelicResultFilter, dataService, resources)
		{
			@Override
			protected Object getResourceAttributeValue(AttributeMetaData attr, Entity sourceEntity)
			{
				String attrName = CLINVAR_CLNSIG.equals(attr.getName()) ? CLINVAR_CLNSIG_ResourceAttributeName : attr.getName();
				return sourceEntity.get(attrName);
			}
		};

		return new RepositoryAnnotatorImpl(entityAnnotator);
	}

	@Bean
	Resource clinVarTabixResource()
	{
		Resource clinVarTabixResource = new ResourceImpl(CLINVAR_TABIX_RESOURCE, new SingleResourceConfig(
				CLINVAR_FILE_LOCATION_PROPERTY, molgenisSettings), new TabixVcfRepositoryFactory(CLINVAR_TABIX_RESOURCE));

		return clinVarTabixResource;
	}
}
