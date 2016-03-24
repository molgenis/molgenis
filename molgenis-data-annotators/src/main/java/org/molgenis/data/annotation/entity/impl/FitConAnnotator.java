package org.molgenis.data.annotation.entity.impl;

import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.annotator.websettings.FitConAnnotatorSettings.Meta.FITCON_LOCATION;
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
import org.molgenis.data.annotation.filter.MultiAllelicResultFilter;
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
		List<AttributeMetaData> attributes = new ArrayList<>();
		DefaultAttributeMetaData dann_score = new DefaultAttributeMetaData(FITCON_SCORE, FieldTypeEnum.DECIMAL)
				.setDescription("fitness consequence score annotation of genetic variants using Fitcon scoring.")
				.setLabel(FITCON_SCORE_LABEL);

		attributes.add(dann_score);

		AnnotatorInfo fitconInfo = AnnotatorInfo.create(Status.READY, AnnotatorInfo.Type.EFFECT_PREDICTION, NAME,
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
		EntityAnnotator entityAnnotator = new AnnotatorImpl(FITCON_TABIX_RESOURCE, fitconInfo, new LocusQueryCreator(),
				new MultiAllelicResultFilter(attributes), dataService, resources,
				new SingleFileLocationCmdLineAnnotatorSettingsConfigurer(FITCON_LOCATION, fitConAnnotatorSettings));

		return new RepositoryAnnotatorImpl(entityAnnotator);
	}

	@Bean
	Resource fitconResource()
	{
		Resource fitConTabixResource = null;

		DefaultEntityMetaData repoMetaData = new DefaultEntityMetaData(FITCON_TABIX_RESOURCE);
		repoMetaData.addAttributeMetaData(CHROM_META);
		repoMetaData.addAttributeMetaData(POS_META);
		repoMetaData.addAttributeMetaData(REF_META);
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("Anc"));
		repoMetaData.addAttributeMetaData(ALT_META);
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("Type"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("Length"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("isTv"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("isDerived"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("AnnoType"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("Consequence"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("ConsScore"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("ConsDetail"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("GC"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("CpG"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("mapAbility20bp"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("mapAbility35bp"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("scoreSegDup"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("priPhCons"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("mamPhCons"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("verPhCons"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("priPhyloP"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("mamPhyloP"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("verPhyloP"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("GerpN"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("GerpS"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("GerpRS"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("GerpRSpval"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("bStatistic"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("mutIndex"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("dnaHelT"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("dnaMGW"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("dnaProT"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("dnaRoll"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("mirSVR-Score"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("mirSVR-E"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("mirSVR-Aln"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("targetScan"));
		// fitcons can be NA so we need to catch the string value
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("FITCON_SCORE"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("cHmmTssA"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("cHmmTssAFlnk"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("cHmmTxFlnk"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("cHmmTx"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("cHmmTxWk"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("cHmmEnhG"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("cHmmEnh"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("cHmmZnfRpts"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("cHmmHet"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("cHmmTssBiv"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("cHmmBivFlnk"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("cHmmEnhBiv"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("cHmmReprPC"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("cHmmReprPCWk"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("cHmmQuies"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("EncExp"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("EncH3K27Ac"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("EncH3K4Me1"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("EncH3K4Me3"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("EncNucleo"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("EncOCC"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("EncOCCombPVal"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("EncOCDNasePVal"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("EncOCFairePVal"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("EncOCpolIIPVal"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("EncOCctcfPVal"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("EncOCmycPVal"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("EncOCDNaseSig"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("EncOCFaireSig"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("EncOCpolIISig"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("EncOCctcfSig"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("EncOCmycSig"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("Segway"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("tOverlapMotifs"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("motifDist"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("motifECount"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("motifEName"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("motifEHIPos"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("motifEScoreChng"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("TFBS"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("TFBSPeaks"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("TFBSPeaksMax"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("isKnownVariant"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("ESP_AF"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("ESP_AFR"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("ESP_EUR"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("TG_AF"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("TG_ASN"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("TG_AMR"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("TG_AFR"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("TG_EUR"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("minDistTSS"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("minDistTSE"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("GeneID"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("FeatureID"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("CCDS"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("GeneName"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("cDNApos"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("relcDNApos"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("CDSpos"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("relCDSpos"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("protPos"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("relProtPos"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("Domain"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("Dst2Splice"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("Dst2SplType"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("Exon"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("Intron"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("oAA"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("nAA"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("Grantham"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("PolyPhenCat"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("PolyPhenVal"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("SIFTcat"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("SIFTval"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("RawScore"));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("PHRED"));

		repoMetaData.addAttribute("id", ROLE_ID).setVisible(false);

		fitConTabixResource = new ResourceImpl(FITCON_TABIX_RESOURCE,
				new SingleResourceConfig(FITCON_LOCATION, fitConAnnotatorSettings),
				new TabixRepositoryFactory(repoMetaData));

		return fitConTabixResource;
	}
}
