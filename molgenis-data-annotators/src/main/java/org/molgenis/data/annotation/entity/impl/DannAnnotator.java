package org.molgenis.data.annotation.entity.impl;

import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.DECIMAL;
import static org.molgenis.data.annotator.websettings.DannAnnotatorSettings.Meta.DANN_LOCATION;
import static org.molgenis.data.vcf.VcfRepository.ALT_META;
import static org.molgenis.data.vcf.VcfRepository.CHROM_META;
import static org.molgenis.data.vcf.VcfRepository.POS_META;
import static org.molgenis.data.vcf.VcfRepository.REF_META;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.entity.AnnotatorInfo;
import org.molgenis.data.annotation.entity.AnnotatorInfo.Status;
import org.molgenis.data.annotation.entity.EntityAnnotator;
import org.molgenis.data.annotation.filter.VariantResultFilter;
import org.molgenis.data.annotation.impl.cmdlineannotatorsettingsconfigurer.SingleFileLocationCmdLineAnnotatorSettingsConfigurer;
import org.molgenis.data.annotation.query.LocusQueryCreator;
import org.molgenis.data.annotation.resources.Resource;
import org.molgenis.data.annotation.resources.Resources;
import org.molgenis.data.annotation.resources.impl.ResourceImpl;
import org.molgenis.data.annotation.resources.impl.SingleResourceConfig;
import org.molgenis.data.annotation.resources.impl.TabixRepositoryFactory;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DannAnnotator
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

	@Bean
	public RepositoryAnnotator dann()
	{
		List<AttributeMetaData> attributes = new ArrayList<>();
		DefaultAttributeMetaData dann_score = new DefaultAttributeMetaData(DANN_SCORE, FieldTypeEnum.DECIMAL)
				.setDescription("deleterious score of genetic variants using neural networks.").setLabel(
						DANN_SCORE_LABEL);

		attributes.add(dann_score);

		AnnotatorInfo dannInfo = AnnotatorInfo
				.create(Status.READY,
						AnnotatorInfo.Type.PATHOGENICITY_ESTIMATE,
						NAME,
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

		EntityAnnotator entityAnnotator = new AnnotatorImpl(DANN_TABIX_RESOURCE, dannInfo, new LocusQueryCreator(),
				new VariantResultFilter(), dataService, resources,
				new SingleFileLocationCmdLineAnnotatorSettingsConfigurer(DANN_LOCATION, dannAnnotatorSettings));

		return new RepositoryAnnotatorImpl(entityAnnotator);
	}

	@Bean
	Resource dannResource()
	{
		Resource dannTabixResource = null;

		DefaultEntityMetaData repoMetaData = new DefaultEntityMetaData(DANN_TABIX_RESOURCE);
		repoMetaData.addAttributeMetaData(CHROM_META);
		repoMetaData.addAttributeMetaData(POS_META);
		repoMetaData.addAttributeMetaData(REF_META);
		repoMetaData.addAttributeMetaData(ALT_META);
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("DANN_SCORE", DECIMAL));
		repoMetaData.addAttribute("id").setIdAttribute(true).setVisible(false);

		dannTabixResource = new ResourceImpl(DANN_TABIX_RESOURCE, new SingleResourceConfig(DANN_LOCATION,
				dannAnnotatorSettings), new TabixRepositoryFactory(repoMetaData));

		return dannTabixResource;
	}
}
