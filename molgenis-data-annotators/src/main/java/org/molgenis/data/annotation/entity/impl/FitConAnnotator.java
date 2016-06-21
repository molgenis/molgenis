package org.molgenis.data.annotation.entity.impl;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.entity.EntityAnnotator;
import org.molgenis.data.annotation.resources.Resource;
import org.molgenis.data.annotation.resources.Resources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FitConAnnotator
{
	public static final String NAME = "fitcon";

	// FIXME: nomenclature: http://cadd.gs.washington.edu/info
	public static final String FITCON_SCORE = "FITCON_SCORE";
	public static final String FITCON_SCORE_LABEL = "FITCON_SCORE";
	public static final String FITCON_TABIX_RESOURCE = "FitConTabixResource";

	@Autowired
	private Entity fitConAnnotatorSettings;

	@Autowired
	private DataService dataService;

	@Autowired
	private Resources resources;

	@Bean
	public RepositoryAnnotator fitcon()
	{
//		List<AttributeMetaData> attributes = new ArrayList<>();
//		AttributeMetaData dann_score = new AttributeMetaData(FITCON_SCORE, DECIMAL)
//				.setDescription("fitness consequence score annotation of genetic variants using Fitcon scoring.")
//				.setLabel(FITCON_SCORE_LABEL);
//
//		attributes.add(dann_score);
//
//		AnnotatorInfo fitconInfo = AnnotatorInfo.create(Status.READY, AnnotatorInfo.Type.EFFECT_PREDICTION, NAME,
//				"Summary: Annotating genetic variants, especially non-coding variants, "
//						+ "for the purpose of identifying pathogenic variants remains a challenge. "
//						+ "Combined annotation-dependent depletion (CADD) is an al- gorithm designed "
//						+ "to annotate both coding and non-coding variants, and has been shown to "
//						+ "outper- form other annotation algorithms. CADD trains a linear kernel support"
//						+ " vector machine (SVM) to dif- ferentiate evolutionarily derived, likely benign,"
//						+ " alleles from simulated, likely deleterious, variants. However, SVMs cannot "
//						+ "capture non-linear relationships among the features, which can limit per- formance. "
//						+ "To address this issue, we have developed FITCON. FITCON uses the same feature set and "
//						+ "training data as CADD to train a deep neural network (DNN). DNNs can capture non-linear"
//						+ " relation- ships among features and are better suited than SVMs for problems with a "
//						+ "large number of samples and features. We exploit Compute Unified Device Architecture-compatible"
//						+ " graphics processing units and deep learning techniques such as dropout and momentum training to"
//						+ " accelerate the DNN train- ing. FITCON achieves about a 19%relative reduction in the error rate and"
//						+ " about a 14%relative increase in the area under the curve (AUC) metric over CADD’s SVMmethodology."
//						+ " All data and source code are available at https://cbcl.ics.uci.edu/ public_data/FITCON/. Contact:",
//				attributes);
		EntityAnnotator entityAnnotator = null; // FIXME new AnnotatorImpl(FITCON_TABIX_RESOURCE, fitconInfo, new LocusQueryCreator(),
//				new MultiAllelicResultFilter(attributes), dataService, resources,
//				new SingleFileLocationCmdLineAnnotatorSettingsConfigurer(FITCON_LOCATION, fitConAnnotatorSettings));

		return new RepositoryAnnotatorImpl(entityAnnotator);
	}

	@Bean
	Resource fitconResource()
	{
		Resource fitConTabixResource = null;
// FIXME
//		EntityMetaDataImpl repoMetaData = new EntityMetaDataImpl(FITCON_TABIX_RESOURCE);
//		repoMetaData.addAttribute(CHROM_META);
//		repoMetaData.addAttribute(POS_META);
//		repoMetaData.addAttribute(REF_META);
//		repoMetaData.addAttribute(new AttributeMetaData("Anc"));
//		repoMetaData.addAttribute(ALT_META);
//		repoMetaData.addAttribute(new AttributeMetaData("Type"));
//		repoMetaData.addAttribute(new AttributeMetaData("Length"));
//		repoMetaData.addAttribute(new AttributeMetaData("isTv"));
//		repoMetaData.addAttribute(new AttributeMetaData("isDerived"));
//		repoMetaData.addAttribute(new AttributeMetaData("AnnoType"));
//		repoMetaData.addAttribute(new AttributeMetaData("Consequence"));
//		repoMetaData.addAttribute(new AttributeMetaData("ConsScore"));
//		repoMetaData.addAttribute(new AttributeMetaData("ConsDetail"));
//		repoMetaData.addAttribute(new AttributeMetaData("GC"));
//		repoMetaData.addAttribute(new AttributeMetaData("CpG"));
//		repoMetaData.addAttribute(new AttributeMetaData("mapAbility20bp"));
//		repoMetaData.addAttribute(new AttributeMetaData("mapAbility35bp"));
//		repoMetaData.addAttribute(new AttributeMetaData("scoreSegDup"));
//		repoMetaData.addAttribute(new AttributeMetaData("priPhCons"));
//		repoMetaData.addAttribute(new AttributeMetaData("mamPhCons"));
//		repoMetaData.addAttribute(new AttributeMetaData("verPhCons"));
//		repoMetaData.addAttribute(new AttributeMetaData("priPhyloP"));
//		repoMetaData.addAttribute(new AttributeMetaData("mamPhyloP"));
//		repoMetaData.addAttribute(new AttributeMetaData("verPhyloP"));
//		repoMetaData.addAttribute(new AttributeMetaData("GerpN"));
//		repoMetaData.addAttribute(new AttributeMetaData("GerpS"));
//		repoMetaData.addAttribute(new AttributeMetaData("GerpRS"));
//		repoMetaData.addAttribute(new AttributeMetaData("GerpRSpval"));
//		repoMetaData.addAttribute(new AttributeMetaData("bStatistic"));
//		repoMetaData.addAttribute(new AttributeMetaData("mutIndex"));
//		repoMetaData.addAttribute(new AttributeMetaData("dnaHelT"));
//		repoMetaData.addAttribute(new AttributeMetaData("dnaMGW"));
//		repoMetaData.addAttribute(new AttributeMetaData("dnaProT"));
//		repoMetaData.addAttribute(new AttributeMetaData("dnaRoll"));
//		repoMetaData.addAttribute(new AttributeMetaData("mirSVR-Score"));
//		repoMetaData.addAttribute(new AttributeMetaData("mirSVR-E"));
//		repoMetaData.addAttribute(new AttributeMetaData("mirSVR-Aln"));
//		repoMetaData.addAttribute(new AttributeMetaData("targetScan"));
//		// fitcons can be NA so we need to catch the string value
//		repoMetaData.addAttribute(new AttributeMetaData("FITCON_SCORE"));
//		repoMetaData.addAttribute(new AttributeMetaData("cHmmTssA"));
//		repoMetaData.addAttribute(new AttributeMetaData("cHmmTssAFlnk"));
//		repoMetaData.addAttribute(new AttributeMetaData("cHmmTxFlnk"));
//		repoMetaData.addAttribute(new AttributeMetaData("cHmmTx"));
//		repoMetaData.addAttribute(new AttributeMetaData("cHmmTxWk"));
//		repoMetaData.addAttribute(new AttributeMetaData("cHmmEnhG"));
//		repoMetaData.addAttribute(new AttributeMetaData("cHmmEnh"));
//		repoMetaData.addAttribute(new AttributeMetaData("cHmmZnfRpts"));
//		repoMetaData.addAttribute(new AttributeMetaData("cHmmHet"));
//		repoMetaData.addAttribute(new AttributeMetaData("cHmmTssBiv"));
//		repoMetaData.addAttribute(new AttributeMetaData("cHmmBivFlnk"));
//		repoMetaData.addAttribute(new AttributeMetaData("cHmmEnhBiv"));
//		repoMetaData.addAttribute(new AttributeMetaData("cHmmReprPC"));
//		repoMetaData.addAttribute(new AttributeMetaData("cHmmReprPCWk"));
//		repoMetaData.addAttribute(new AttributeMetaData("cHmmQuies"));
//		repoMetaData.addAttribute(new AttributeMetaData("EncExp"));
//		repoMetaData.addAttribute(new AttributeMetaData("EncH3K27Ac"));
//		repoMetaData.addAttribute(new AttributeMetaData("EncH3K4Me1"));
//		repoMetaData.addAttribute(new AttributeMetaData("EncH3K4Me3"));
//		repoMetaData.addAttribute(new AttributeMetaData("EncNucleo"));
//		repoMetaData.addAttribute(new AttributeMetaData("EncOCC"));
//		repoMetaData.addAttribute(new AttributeMetaData("EncOCCombPVal"));
//		repoMetaData.addAttribute(new AttributeMetaData("EncOCDNasePVal"));
//		repoMetaData.addAttribute(new AttributeMetaData("EncOCFairePVal"));
//		repoMetaData.addAttribute(new AttributeMetaData("EncOCpolIIPVal"));
//		repoMetaData.addAttribute(new AttributeMetaData("EncOCctcfPVal"));
//		repoMetaData.addAttribute(new AttributeMetaData("EncOCmycPVal"));
//		repoMetaData.addAttribute(new AttributeMetaData("EncOCDNaseSig"));
//		repoMetaData.addAttribute(new AttributeMetaData("EncOCFaireSig"));
//		repoMetaData.addAttribute(new AttributeMetaData("EncOCpolIISig"));
//		repoMetaData.addAttribute(new AttributeMetaData("EncOCctcfSig"));
//		repoMetaData.addAttribute(new AttributeMetaData("EncOCmycSig"));
//		repoMetaData.addAttribute(new AttributeMetaData("Segway"));
//		repoMetaData.addAttribute(new AttributeMetaData("tOverlapMotifs"));
//		repoMetaData.addAttribute(new AttributeMetaData("motifDist"));
//		repoMetaData.addAttribute(new AttributeMetaData("motifECount"));
//		repoMetaData.addAttribute(new AttributeMetaData("motifEName"));
//		repoMetaData.addAttribute(new AttributeMetaData("motifEHIPos"));
//		repoMetaData.addAttribute(new AttributeMetaData("motifEScoreChng"));
//		repoMetaData.addAttribute(new AttributeMetaData("TFBS"));
//		repoMetaData.addAttribute(new AttributeMetaData("TFBSPeaks"));
//		repoMetaData.addAttribute(new AttributeMetaData("TFBSPeaksMax"));
//		repoMetaData.addAttribute(new AttributeMetaData("isKnownVariant"));
//		repoMetaData.addAttribute(new AttributeMetaData("ESP_AF"));
//		repoMetaData.addAttribute(new AttributeMetaData("ESP_AFR"));
//		repoMetaData.addAttribute(new AttributeMetaData("ESP_EUR"));
//		repoMetaData.addAttribute(new AttributeMetaData("TG_AF"));
//		repoMetaData.addAttribute(new AttributeMetaData("TG_ASN"));
//		repoMetaData.addAttribute(new AttributeMetaData("TG_AMR"));
//		repoMetaData.addAttribute(new AttributeMetaData("TG_AFR"));
//		repoMetaData.addAttribute(new AttributeMetaData("TG_EUR"));
//		repoMetaData.addAttribute(new AttributeMetaData("minDistTSS"));
//		repoMetaData.addAttribute(new AttributeMetaData("minDistTSE"));
//		repoMetaData.addAttribute(new AttributeMetaData("GeneID"));
//		repoMetaData.addAttribute(new AttributeMetaData("FeatureID"));
//		repoMetaData.addAttribute(new AttributeMetaData("CCDS"));
//		repoMetaData.addAttribute(new AttributeMetaData("GeneName"));
//		repoMetaData.addAttribute(new AttributeMetaData("cDNApos"));
//		repoMetaData.addAttribute(new AttributeMetaData("relcDNApos"));
//		repoMetaData.addAttribute(new AttributeMetaData("CDSpos"));
//		repoMetaData.addAttribute(new AttributeMetaData("relCDSpos"));
//		repoMetaData.addAttribute(new AttributeMetaData("protPos"));
//		repoMetaData.addAttribute(new AttributeMetaData("relProtPos"));
//		repoMetaData.addAttribute(new AttributeMetaData("Domain"));
//		repoMetaData.addAttribute(new AttributeMetaData("Dst2Splice"));
//		repoMetaData.addAttribute(new AttributeMetaData("Dst2SplType"));
//		repoMetaData.addAttribute(new AttributeMetaData("Exon"));
//		repoMetaData.addAttribute(new AttributeMetaData("Intron"));
//		repoMetaData.addAttribute(new AttributeMetaData("oAA"));
//		repoMetaData.addAttribute(new AttributeMetaData("nAA"));
//		repoMetaData.addAttribute(new AttributeMetaData("Grantham"));
//		repoMetaData.addAttribute(new AttributeMetaData("PolyPhenCat"));
//		repoMetaData.addAttribute(new AttributeMetaData("PolyPhenVal"));
//		repoMetaData.addAttribute(new AttributeMetaData("SIFTcat"));
//		repoMetaData.addAttribute(new AttributeMetaData("SIFTval"));
//		repoMetaData.addAttribute(new AttributeMetaData("RawScore"));
//		repoMetaData.addAttribute(new AttributeMetaData("PHRED"));
//
//		repoMetaData.addAttribute("id", ROLE_ID).setVisible(false);
//
//		fitConTabixResource = new ResourceImpl(FITCON_TABIX_RESOURCE,
//				new SingleResourceConfig(FITCON_LOCATION, fitConAnnotatorSettings),
//				new TabixRepositoryFactory(repoMetaData));

		return fitConTabixResource;
	}
}
