package org.molgenis.data.annotation.core.entity.impl;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.annotation.core.entity.AnnotatorConfig;
import org.molgenis.data.annotation.core.entity.AnnotatorInfo;
import org.molgenis.data.annotation.core.entity.EntityAnnotator;
import org.molgenis.data.annotation.core.entity.impl.framework.AbstractAnnotator;
import org.molgenis.data.annotation.core.entity.impl.framework.RepositoryAnnotatorImpl;
import org.molgenis.data.annotation.core.filter.MultiAllelicResultFilter;
import org.molgenis.data.annotation.core.query.LocusQueryCreator;
import org.molgenis.data.annotation.core.resources.Resource;
import org.molgenis.data.annotation.core.resources.Resources;
import org.molgenis.data.annotation.core.resources.impl.RepositoryFactory;
import org.molgenis.data.annotation.core.resources.impl.ResourceImpl;
import org.molgenis.data.annotation.core.resources.impl.SingleResourceConfig;
import org.molgenis.data.annotation.core.resources.impl.tabix.TabixRepositoryFactory;
import org.molgenis.data.annotation.web.settings.SingleFileLocationCmdLineAnnotatorSettingsConfigurer;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.molgenis.data.annotation.web.settings.FitConAnnotatorSettings.Meta.FITCON_LOCATION;
import static org.molgenis.data.meta.AttributeType.STRING;

@Configuration
public class FitConAnnotator implements AnnotatorConfig
{
	public static final String NAME = "fitcon";

	public static final String FITCON_SCORE = "FITCON_SCORE";
	public static final String FITCON_SCORE_LABEL = "FITCON_SCORE";
	public static final String FITCON_TABIX_RESOURCE = "FitConTabixResource";

	@Autowired
	private Entity fitConAnnotatorSettings;

	@Autowired
	private DataService dataService;

	@Autowired
	private Resources resources;

	@Autowired
	private VcfAttributes vcfAttributes;

	@Autowired
	private EntityTypeFactory entityTypeFactory;

	@Autowired
	private AttributeFactory attributeFactory;

	private RepositoryAnnotatorImpl annotator;

	@Bean
	public RepositoryAnnotator fitcon()
	{
		annotator = new RepositoryAnnotatorImpl(NAME);
		return annotator;
	}

	@Override
	public void init()
	{
		List<Attribute> attributes = createFitconOutputAttributes();

		AnnotatorInfo fitconInfo = AnnotatorInfo.create(AnnotatorInfo.Status.READY,
				AnnotatorInfo.Type.EFFECT_PREDICTION, NAME,
				"Summary: Annotating genetic variants, especially non-coding variants, "
						+ "for the purpose of identifying pathogenic variants remains a challenge. "
						+ "Combined annotation-dependent depletion (CADD) is an al- gorithm designed "
						+ "to annotate both coding and non-coding variants, and has been shown to "
						+ "outper- form other annotation algorithms. CADD trains a linear kernel support"
						+ " vector machine (SVM) to dif- ferentiate evolutionarily derived, likely benign,"
						+ " alleles from simulated, likely deleterious, variants. However, SVMs cannot "
						+ "capture non-linear relationships among the features, which can limit per- formance. "
						+ "To address this issue, we have developed FITCON. FITCON uses the same feature set and "
						+ "training data as CADD to train a deep neural network (DNN). DNNs can capture non-linear"
						+ " relation- ships among features and are better suited than SVMs for problems with a "
						+ "large number of samples and features. We exploit Compute Unified Device Architecture-compatible"
						+ " graphics processing units and deep learning techniques such as dropout and momentum training to"
						+ " accelerate the DNN train- ing. FITCON achieves about a 19%relative reduction in the error rate and"
						+ " about a 14%relative increase in the area under the curve (AUC) metric over CADD’s SVMmethodology."
						+ " All data and source code are available at https://cbcl.ics.uci.edu/ public_data/FITCON/. Contact:",
				attributes);
		EntityAnnotator entityAnnotator = new AbstractAnnotator(FITCON_TABIX_RESOURCE, fitconInfo,
				new LocusQueryCreator(vcfAttributes), new MultiAllelicResultFilter(attributes, vcfAttributes),
				dataService, resources,
				new SingleFileLocationCmdLineAnnotatorSettingsConfigurer(FITCON_LOCATION, fitConAnnotatorSettings))
		{
			@Override
			public List<Attribute> createAnnotatorAttributes(AttributeFactory attributeFactory)
			{
				return createFitconOutputAttributes();
			}
		};

		annotator.init(entityAnnotator);
	}

	private List<Attribute> createFitconOutputAttributes()
	{
		List<Attribute> attributes = newArrayList();
		Attribute fitcon_score = attributeFactory.create()
												 .setName(FITCON_SCORE)
												 .setDataType(STRING)
												 .setDescription(
														 "fitness consequence score annotation of genetic variants using Fitcon scoring.")
												 .setLabel(FITCON_SCORE_LABEL);

		attributes.add(fitcon_score);
		return attributes;
	}

	@Bean
	Resource fitconResource()
	{
		Resource fitConTabixResource;
		fitConTabixResource = new ResourceImpl(FITCON_TABIX_RESOURCE,
				new SingleResourceConfig(FITCON_LOCATION, fitConAnnotatorSettings))
		{

			@Override
			public RepositoryFactory getRepositoryFactory()
			{
				EntityType repoMetaData = entityTypeFactory.create(FITCON_TABIX_RESOURCE);
				repoMetaData.addAttribute(vcfAttributes.getChromAttribute());
				repoMetaData.addAttribute(vcfAttributes.getPosAttribute());
				repoMetaData.addAttribute(vcfAttributes.getRefAttribute());
				repoMetaData.addAttribute(attributeFactory.create().setName("Anc"));
				repoMetaData.addAttribute(vcfAttributes.getAltAttribute());
				repoMetaData.addAttribute(attributeFactory.create().setName("Type"));
				repoMetaData.addAttribute(attributeFactory.create().setName("Length"));
				repoMetaData.addAttribute(attributeFactory.create().setName("isTv"));
				repoMetaData.addAttribute(attributeFactory.create().setName("isDerived"));
				repoMetaData.addAttribute(attributeFactory.create().setName("AnnoType"));
				repoMetaData.addAttribute(attributeFactory.create().setName("Consequence"));
				repoMetaData.addAttribute(attributeFactory.create().setName("ConsScore"));
				repoMetaData.addAttribute(attributeFactory.create().setName("ConsDetail"));
				repoMetaData.addAttribute(attributeFactory.create().setName("GC"));
				repoMetaData.addAttribute(attributeFactory.create().setName("CpG"));
				repoMetaData.addAttribute(attributeFactory.create().setName("mapAbility20bp"));
				repoMetaData.addAttribute(attributeFactory.create().setName("mapAbility35bp"));
				repoMetaData.addAttribute(attributeFactory.create().setName("scoreSegDup"));
				repoMetaData.addAttribute(attributeFactory.create().setName("priPhCons"));
				repoMetaData.addAttribute(attributeFactory.create().setName("mamPhCons"));
				repoMetaData.addAttribute(attributeFactory.create().setName("verPhCons"));
				repoMetaData.addAttribute(attributeFactory.create().setName("priPhyloP"));
				repoMetaData.addAttribute(attributeFactory.create().setName("mamPhyloP"));
				repoMetaData.addAttribute(attributeFactory.create().setName("verPhyloP"));
				repoMetaData.addAttribute(attributeFactory.create().setName("GerpN"));
				repoMetaData.addAttribute(attributeFactory.create().setName("GerpS"));
				repoMetaData.addAttribute(attributeFactory.create().setName("GerpRS"));
				repoMetaData.addAttribute(attributeFactory.create().setName("GerpRSpval"));
				repoMetaData.addAttribute(attributeFactory.create().setName("bStatistic"));
				repoMetaData.addAttribute(attributeFactory.create().setName("mutIndex"));
				repoMetaData.addAttribute(attributeFactory.create().setName("dnaHelT"));
				repoMetaData.addAttribute(attributeFactory.create().setName("dnaMGW"));
				repoMetaData.addAttribute(attributeFactory.create().setName("dnaProT"));
				repoMetaData.addAttribute(attributeFactory.create().setName("dnaRoll"));
				repoMetaData.addAttribute(attributeFactory.create().setName("mirSVR-Score"));
				repoMetaData.addAttribute(attributeFactory.create().setName("mirSVR-E"));
				repoMetaData.addAttribute(attributeFactory.create().setName("mirSVR-Aln"));
				repoMetaData.addAttribute(attributeFactory.create().setName("targetScan"));
				// fitcons can be NA so we need to catch the string value
				repoMetaData.addAttribute(attributeFactory.create().setName("FITCON_SCORE"));
				repoMetaData.addAttribute(attributeFactory.create().setName("cHmmTssA"));
				repoMetaData.addAttribute(attributeFactory.create().setName("cHmmTssAFlnk"));
				repoMetaData.addAttribute(attributeFactory.create().setName("cHmmTxFlnk"));
				repoMetaData.addAttribute(attributeFactory.create().setName("cHmmTx"));
				repoMetaData.addAttribute(attributeFactory.create().setName("cHmmTxWk"));
				repoMetaData.addAttribute(attributeFactory.create().setName("cHmmEnhG"));
				repoMetaData.addAttribute(attributeFactory.create().setName("cHmmEnh"));
				repoMetaData.addAttribute(attributeFactory.create().setName("cHmmZnfRpts"));
				repoMetaData.addAttribute(attributeFactory.create().setName("cHmmHet"));
				repoMetaData.addAttribute(attributeFactory.create().setName("cHmmTssBiv"));
				repoMetaData.addAttribute(attributeFactory.create().setName("cHmmBivFlnk"));
				repoMetaData.addAttribute(attributeFactory.create().setName("cHmmEnhBiv"));
				repoMetaData.addAttribute(attributeFactory.create().setName("cHmmReprPC"));
				repoMetaData.addAttribute(attributeFactory.create().setName("cHmmReprPCWk"));
				repoMetaData.addAttribute(attributeFactory.create().setName("cHmmQuies"));
				repoMetaData.addAttribute(attributeFactory.create().setName("EncExp"));
				repoMetaData.addAttribute(attributeFactory.create().setName("EncH3K27Ac"));
				repoMetaData.addAttribute(attributeFactory.create().setName("EncH3K4Me1"));
				repoMetaData.addAttribute(attributeFactory.create().setName("EncH3K4Me3"));
				repoMetaData.addAttribute(attributeFactory.create().setName("EncNucleo"));
				repoMetaData.addAttribute(attributeFactory.create().setName("EncOCC"));
				repoMetaData.addAttribute(attributeFactory.create().setName("EncOCCombPVal"));
				repoMetaData.addAttribute(attributeFactory.create().setName("EncOCDNasePVal"));
				repoMetaData.addAttribute(attributeFactory.create().setName("EncOCFairePVal"));
				repoMetaData.addAttribute(attributeFactory.create().setName("EncOCpolIIPVal"));
				repoMetaData.addAttribute(attributeFactory.create().setName("EncOCctcfPVal"));
				repoMetaData.addAttribute(attributeFactory.create().setName("EncOCmycPVal"));
				repoMetaData.addAttribute(attributeFactory.create().setName("EncOCDNaseSig"));
				repoMetaData.addAttribute(attributeFactory.create().setName("EncOCFaireSig"));
				repoMetaData.addAttribute(attributeFactory.create().setName("EncOCpolIISig"));
				repoMetaData.addAttribute(attributeFactory.create().setName("EncOCctcfSig"));
				repoMetaData.addAttribute(attributeFactory.create().setName("EncOCmycSig"));
				repoMetaData.addAttribute(attributeFactory.create().setName("Segway"));
				repoMetaData.addAttribute(attributeFactory.create().setName("tOverlapMotifs"));
				repoMetaData.addAttribute(attributeFactory.create().setName("motifDist"));
				repoMetaData.addAttribute(attributeFactory.create().setName("motifECount"));
				repoMetaData.addAttribute(attributeFactory.create().setName("motifEName"));
				repoMetaData.addAttribute(attributeFactory.create().setName("motifEHIPos"));
				repoMetaData.addAttribute(attributeFactory.create().setName("motifEScoreChng"));
				repoMetaData.addAttribute(attributeFactory.create().setName("TFBS"));
				repoMetaData.addAttribute(attributeFactory.create().setName("TFBSPeaks"));
				repoMetaData.addAttribute(attributeFactory.create().setName("TFBSPeaksMax"));
				repoMetaData.addAttribute(attributeFactory.create().setName("isKnownVariant"));
				repoMetaData.addAttribute(attributeFactory.create().setName("ESP_AF"));
				repoMetaData.addAttribute(attributeFactory.create().setName("ESP_AFR"));
				repoMetaData.addAttribute(attributeFactory.create().setName("ESP_EUR"));
				repoMetaData.addAttribute(attributeFactory.create().setName("TG_AF"));
				repoMetaData.addAttribute(attributeFactory.create().setName("TG_ASN"));
				repoMetaData.addAttribute(attributeFactory.create().setName("TG_AMR"));
				repoMetaData.addAttribute(attributeFactory.create().setName("TG_AFR"));
				repoMetaData.addAttribute(attributeFactory.create().setName("TG_EUR"));
				repoMetaData.addAttribute(attributeFactory.create().setName("minDistTSS"));
				repoMetaData.addAttribute(attributeFactory.create().setName("minDistTSE"));
				repoMetaData.addAttribute(attributeFactory.create().setName("GeneID"));
				repoMetaData.addAttribute(attributeFactory.create().setName("FeatureID"));
				repoMetaData.addAttribute(attributeFactory.create().setName("CCDS"));
				repoMetaData.addAttribute(attributeFactory.create().setName("GeneName"));
				repoMetaData.addAttribute(attributeFactory.create().setName("cDNApos"));
				repoMetaData.addAttribute(attributeFactory.create().setName("relcDNApos"));
				repoMetaData.addAttribute(attributeFactory.create().setName("CDSpos"));
				repoMetaData.addAttribute(attributeFactory.create().setName("relCDSpos"));
				repoMetaData.addAttribute(attributeFactory.create().setName("protPos"));
				repoMetaData.addAttribute(attributeFactory.create().setName("relProtPos"));
				repoMetaData.addAttribute(attributeFactory.create().setName("Domain"));
				repoMetaData.addAttribute(attributeFactory.create().setName("Dst2Splice"));
				repoMetaData.addAttribute(attributeFactory.create().setName("Dst2SplType"));
				repoMetaData.addAttribute(attributeFactory.create().setName("Exon"));
				repoMetaData.addAttribute(attributeFactory.create().setName("Intron"));
				repoMetaData.addAttribute(attributeFactory.create().setName("oAA"));
				repoMetaData.addAttribute(attributeFactory.create().setName("nAA"));
				repoMetaData.addAttribute(attributeFactory.create().setName("Grantham"));
				repoMetaData.addAttribute(attributeFactory.create().setName("PolyPhenCat"));
				repoMetaData.addAttribute(attributeFactory.create().setName("PolyPhenVal"));
				repoMetaData.addAttribute(attributeFactory.create().setName("SIFTcat"));
				repoMetaData.addAttribute(attributeFactory.create().setName("SIFTval"));
				repoMetaData.addAttribute(attributeFactory.create().setName("RawScore"));
				repoMetaData.addAttribute(attributeFactory.create().setName("PHRED"));

				Attribute idAttribute = attributeFactory.create().setName("id").setVisible(false).setIdAttribute(true);
				repoMetaData.addAttribute(idAttribute);

				return new TabixRepositoryFactory(repoMetaData);
			}
		};

		return fitConTabixResource;
	}
}
