package org.molgenis.data.annotation.entity.impl;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.entity.AnnotatorConfig;
import org.molgenis.data.annotation.entity.AnnotatorInfo;
import org.molgenis.data.annotation.entity.EntityAnnotator;
import org.molgenis.data.annotation.filter.MultiAllelicResultFilter;
import org.molgenis.data.annotation.cmd.cmdlineannotatorsettingsconfigurer.SingleFileLocationCmdLineAnnotatorSettingsConfigurer;
import org.molgenis.data.annotation.query.LocusQueryCreator;
import org.molgenis.data.annotation.resources.Resource;
import org.molgenis.data.annotation.resources.Resources;
import org.molgenis.data.annotation.resources.impl.RepositoryFactory;
import org.molgenis.data.annotation.resources.impl.ResourceImpl;
import org.molgenis.data.annotation.resources.impl.SingleResourceConfig;
import org.molgenis.data.annotation.resources.impl.TabixRepositoryFactory;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

import static org.molgenis.MolgenisFieldTypes.AttributeType.DECIMAL;
import static org.molgenis.data.annotator.websettings.DannAnnotatorSettings.Meta.DANN_LOCATION;

@Configuration
public class DannAnnotator implements AnnotatorConfig
{
	public static final String NAME = "dann";

	public static final String DANN_SCORE = "DANN_SCORE";
	public static final String DANN_SCORE_LABEL = "DANNSCORE";
	public static final String DANN_TABIX_RESOURCE = "DANNTabixResource";

	@Autowired
	private Entity dannAnnotatorSettings;

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
	public RepositoryAnnotator dann()
	{
		annotator = new RepositoryAnnotatorImpl(NAME);
		return annotator;
	}

	@Override
	public void init()
	{
		List<AttributeMetaData> attributes = new ArrayList<>();
		AttributeMetaData dann_score = attributeMetaDataFactory.create().setName(DANN_SCORE).setDataType(DECIMAL)
				.setDescription("deleterious score of genetic variants using neural networks.")
				.setLabel(DANN_SCORE_LABEL);

		attributes.add(dann_score);

		AnnotatorInfo dannInfo = AnnotatorInfo
				.create(AnnotatorInfo.Status.READY, AnnotatorInfo.Type.PATHOGENICITY_ESTIMATE, NAME,
						"Annotating genetic variants, especially non-coding variants, "
								+ "for the purpose of identifying pathogenic variants remains a challenge."
								+ " Combined annotation-dependent depletion (CADD) is an al- gorithm designed "
								+ "to annotate both coding and non-coding variants, and has been shown to outper- form "
								+ "other annotation algorithms. CADD trains a linear kernel support vector machine (SVM) "
								+ "to dif- ferentiate evolutionarily derived, likely benign, alleles from simulated, "
								+ "likely deleterious, variants. However, SVMs cannot capture non-linear relationships"
								+ " among the features, which can limit performance. To address this issue, we have"
								+ " developed DANN. DANN uses the same feature set and training data as CADD to train"
								+ " a deep neural network (DNN). DNNs can capture non-linear relation- ships among "
								+ "features and are better suited than SVMs for problems with a large number of samples "
								+ "and features. We exploit Compute Unified Device Architecture-compatible "
								+ "graphics processing units and deep learning techniques such as dropout and momentum "
								+ "training to accelerate the DNN training. DANN achieves about a 19%relative reduction "
								+ "in the error rate and about a 14%relative increase in the area under the curve (AUC) metric "
								+ "over CADD’s SVM methodology. "
								+ "All data and source code are available at https://cbcl.ics.uci.edu/ public_data/DANN/.",
						attributes);

		EntityAnnotator entityAnnotator = new AnnotatorImpl(DANN_TABIX_RESOURCE, dannInfo,
				new LocusQueryCreator(vcfAttributes), new MultiAllelicResultFilter(attributes), dataService, resources,
				new SingleFileLocationCmdLineAnnotatorSettingsConfigurer(DANN_LOCATION, dannAnnotatorSettings));

		annotator.init(entityAnnotator);
	}

	@Bean
	Resource dannResource()
	{
		Resource dannTabixResource = null;

		dannTabixResource = new ResourceImpl(DANN_TABIX_RESOURCE,
				new SingleResourceConfig(DANN_LOCATION, dannAnnotatorSettings))
		{
			@Override
			public RepositoryFactory getRepositoryFactory()
			{

				String idAttrName = "id";
				EntityMetaData repoMetaData = entityMetaDataFactory.create().setName(DANN_TABIX_RESOURCE);
				repoMetaData.addAttribute(vcfAttributes.getChromAttribute());
				repoMetaData.addAttribute(vcfAttributes.getPosAttribute());
				repoMetaData.addAttribute(vcfAttributes.getRefAttribute());
				repoMetaData.addAttribute(vcfAttributes.getAltAttribute());
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("DANN_SCORE").setDataType(DECIMAL));
				AttributeMetaData idAttributeMetaData = attributeMetaDataFactory.create().setName(idAttrName)
						.setVisible(false);
				repoMetaData.addAttribute(idAttributeMetaData);
				repoMetaData.setIdAttribute(idAttributeMetaData);
				return new TabixRepositoryFactory(repoMetaData);
			}
		};

		return dannTabixResource;
	}
}
