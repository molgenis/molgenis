package org.molgenis.data.annotation.core.entity.impl;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.annotation.core.entity.AnnotatorConfig;
import org.molgenis.data.annotation.core.entity.AnnotatorInfo;
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
import org.molgenis.data.annotation.core.resources.impl.tabix.TabixRepositoryFactory;
import org.molgenis.data.annotation.web.settings.SingleFileLocationCmdLineAnnotatorSettingsConfigurer;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

import static org.molgenis.MolgenisFieldTypes.AttributeType.STRING;
import static org.molgenis.data.annotation.web.settings.FitConAnnotatorSettings.Meta.FITCON_LOCATION;

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
	private AttributeMetaDataFactory attributeMetaDataFactory;
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
		List<AttributeMetaData> attributes = new ArrayList<>();
		AttributeMetaData dann_score = attributeMetaDataFactory.create().setName(FITCON_SCORE).setDataType(STRING)
				.setDescription("fitness consequence score annotation of genetic variants using Fitcon scoring.")
				.setLabel(FITCON_SCORE_LABEL);

		attributes.add(dann_score);

		AnnotatorInfo fitconInfo = AnnotatorInfo
				.create(AnnotatorInfo.Status.READY, AnnotatorInfo.Type.EFFECT_PREDICTION, NAME,
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
		EntityAnnotator entityAnnotator = new AnnotatorImpl(FITCON_TABIX_RESOURCE, fitconInfo,
				new LocusQueryCreator(vcfAttributes), new MultiAllelicResultFilter(attributes, vcfAttributes),
				dataService, resources,
				new SingleFileLocationCmdLineAnnotatorSettingsConfigurer(FITCON_LOCATION, fitConAnnotatorSettings));

		annotator.init(entityAnnotator);
	}

	@Bean
	Resource fitconResource()
	{
		Resource fitConTabixResource = null;
		fitConTabixResource = new ResourceImpl(FITCON_TABIX_RESOURCE,
				new SingleResourceConfig(FITCON_LOCATION, fitConAnnotatorSettings))
		{

			@Override
			public RepositoryFactory getRepositoryFactory()
			{

				EntityType repoMetaData = entityTypeFactory.create().setName(FITCON_TABIX_RESOURCE);
				repoMetaData.addAttribute(vcfAttributes.getChromAttribute());
				repoMetaData.addAttribute(vcfAttributes.getPosAttribute());
				repoMetaData.addAttribute(vcfAttributes.getRefAttribute());
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("Anc"));
				repoMetaData.addAttribute(vcfAttributes.getAltAttribute());
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("Type"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("Length"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("isTv"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("isDerived"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("AnnoType"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("Consequence"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("ConsScore"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("ConsDetail"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("GC"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("CpG"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("mapAbility20bp"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("mapAbility35bp"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("scoreSegDup"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("priPhCons"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("mamPhCons"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("verPhCons"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("priPhyloP"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("mamPhyloP"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("verPhyloP"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("GerpN"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("GerpS"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("GerpRS"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("GerpRSpval"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("bStatistic"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("mutIndex"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("dnaHelT"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("dnaMGW"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("dnaProT"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("dnaRoll"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("mirSVR-Score"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("mirSVR-E"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("mirSVR-Aln"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("targetScan"));
				// fitcons can be NA so we need to catch the string value
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("FITCON_SCORE"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("cHmmTssA"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("cHmmTssAFlnk"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("cHmmTxFlnk"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("cHmmTx"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("cHmmTxWk"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("cHmmEnhG"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("cHmmEnh"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("cHmmZnfRpts"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("cHmmHet"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("cHmmTssBiv"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("cHmmBivFlnk"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("cHmmEnhBiv"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("cHmmReprPC"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("cHmmReprPCWk"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("cHmmQuies"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("EncExp"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("EncH3K27Ac"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("EncH3K4Me1"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("EncH3K4Me3"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("EncNucleo"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("EncOCC"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("EncOCCombPVal"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("EncOCDNasePVal"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("EncOCFairePVal"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("EncOCpolIIPVal"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("EncOCctcfPVal"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("EncOCmycPVal"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("EncOCDNaseSig"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("EncOCFaireSig"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("EncOCpolIISig"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("EncOCctcfSig"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("EncOCmycSig"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("Segway"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("tOverlapMotifs"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("motifDist"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("motifECount"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("motifEName"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("motifEHIPos"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("motifEScoreChng"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("TFBS"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("TFBSPeaks"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("TFBSPeaksMax"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("isKnownVariant"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("ESP_AF"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("ESP_AFR"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("ESP_EUR"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("TG_AF"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("TG_ASN"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("TG_AMR"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("TG_AFR"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("TG_EUR"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("minDistTSS"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("minDistTSE"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("GeneID"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("FeatureID"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("CCDS"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("GeneName"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("cDNApos"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("relcDNApos"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("CDSpos"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("relCDSpos"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("protPos"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("relProtPos"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("Domain"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("Dst2Splice"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("Dst2SplType"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("Exon"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("Intron"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("oAA"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("nAA"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("Grantham"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("PolyPhenCat"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("PolyPhenVal"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("SIFTcat"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("SIFTval"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("RawScore"));
				repoMetaData.addAttribute(attributeMetaDataFactory.create().setName("PHRED"));

				AttributeMetaData idAttributeMetaData = attributeMetaDataFactory.create().setName("id")
						.setVisible(false);
				repoMetaData.addAttribute(idAttributeMetaData);
				repoMetaData.setIdAttribute(idAttributeMetaData);

				return new TabixRepositoryFactory(repoMetaData);
			}
		};

		return fitConTabixResource;
	}
}
