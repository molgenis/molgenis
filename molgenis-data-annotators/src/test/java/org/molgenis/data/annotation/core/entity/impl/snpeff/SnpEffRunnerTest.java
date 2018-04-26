package org.molgenis.data.annotation.core.entity.impl.snpeff;

import com.google.common.collect.Iterators;
import org.mockito.Mock;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.config.EffectsTestConfig;
import org.molgenis.data.annotation.core.effects.EffectsMetaData;
import org.molgenis.data.annotation.core.utils.JarRunner;
import org.molgenis.data.annotation.core.utils.JarRunnerImpl;
import org.molgenis.data.meta.model.*;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.populate.IdGeneratorImpl;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.util.EntityUtils;
import org.molgenis.data.vcf.config.VcfTestConfig;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.molgenis.util.ResourceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.annotation.core.effects.EffectsMetaData.*;
import static org.molgenis.data.meta.AttributeType.XREF;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@ContextConfiguration(classes = { SnpEffRunnerTest.Config.class })
public class SnpEffRunnerTest extends AbstractMolgenisSpringTest
{
	@Autowired
	ApplicationContext context;

	@Autowired
	AttributeFactory attributeFactory;

	@Autowired
	EntityTypeFactory entityTypeFactory;

	@Autowired
	VcfAttributes vcfAttributes;

	@Autowired
	EffectsMetaData effectsMetaData;

	@Autowired
	PackageFactory packageFactory;

	private final ArrayList<Entity> singleAlleleEntities = newArrayList();
	private final ArrayList<Entity> multiAlleleEntities = newArrayList();
	private final ArrayList<Entity> multiGeneEntities = newArrayList();

	private final List<Entity> expectedSingleAlleleEffectEntities = newArrayList();
	private final List<Entity> expectedMultiAlleleEffectEntities = newArrayList();
	private final List<Entity> expectedMultiGeneEffectEntities = newArrayList();

	private final ArrayList<Entity> entities = new ArrayList<>();

	private EntityType metaDataCanAnnotate;

	private EntityType effectsEMD;

	private SnpEffRunner snpEffRunner;

	@Mock
	private JarRunner jarRunner;

	@Mock
	private Entity snpEffAnnotatorSettings;

	@BeforeClass
	public void setUpBeforeClass()
	{
		metaDataCanAnnotate = entityTypeFactory.create("test");
		Attribute attributeChrom = vcfAttributes.getChromAttribute();
		Attribute attributePos = vcfAttributes.getPosAttribute();
		Attribute attributeRef = vcfAttributes.getRefAttribute();
		Attribute attributeAlt = vcfAttributes.getAltAttribute();

		metaDataCanAnnotate.addAttribute(attributeChrom, ROLE_ID);
		metaDataCanAnnotate.addAttribute(attributePos);
		metaDataCanAnnotate.addAttribute(attributeRef);
		metaDataCanAnnotate.addAttribute(attributeAlt);

		effectsEMD = entityTypeFactory.create("testEFFECTS");
		effectsEMD.addAttribute(attributeFactory.create().setName("ID").setAuto(true).setVisible(false));
		effectsEMD.addAttribute(effectsMetaData.getAltAttr());
		effectsEMD.addAttribute(effectsMetaData.getGeneNameAttr());
		effectsEMD.addAttribute(effectsMetaData.getAnnotationAttr());
		effectsEMD.addAttribute(effectsMetaData.getPutativeImpactAttr());
		effectsEMD.addAttribute(effectsMetaData.getGeneIdAttr());
		effectsEMD.addAttribute(effectsMetaData.getFeatureTypeAttr());
		effectsEMD.addAttribute(effectsMetaData.getFeatureIdAttr());
		effectsEMD.addAttribute(effectsMetaData.getTranscriptBiotypeAttr());
		effectsEMD.addAttribute(effectsMetaData.getRankTotalAttr());
		effectsEMD.addAttribute(effectsMetaData.getHgvsCAttr());
		effectsEMD.addAttribute(effectsMetaData.getHgvsPAttr());
		effectsEMD.addAttribute(effectsMetaData.getCdnaPositionAttr());
		effectsEMD.addAttribute(effectsMetaData.getCdsPositionAttr());
		effectsEMD.addAttribute(effectsMetaData.getProteinPositionAttr());
		effectsEMD.addAttribute(effectsMetaData.getDistanceToFeatureAttr());
		effectsEMD.addAttribute(effectsMetaData.getErrorsAttr());
		effectsEMD.addAttribute(attributeFactory.create()
												.setName(EffectsMetaData.VARIANT)
												.setNillable(false)
												.setDataType(XREF)
												.setRefEntity(metaDataCanAnnotate));

		Entity singleAlleleEntity1 = new DynamicEntity(metaDataCanAnnotate);
		singleAlleleEntity1.set(vcfAttributes.CHROM, "1");
		singleAlleleEntity1.set(vcfAttributes.POS, 13380);
		singleAlleleEntity1.set(vcfAttributes.REF, "C");
		singleAlleleEntity1.set(vcfAttributes.ALT, "G");

		Entity singleAlleleEntity2 = new DynamicEntity(metaDataCanAnnotate);
		singleAlleleEntity2.set(vcfAttributes.CHROM, "1");
		singleAlleleEntity2.set(vcfAttributes.POS, 13980);
		singleAlleleEntity2.set(vcfAttributes.REF, "T");
		singleAlleleEntity2.set(vcfAttributes.ALT, "C");

		Entity singleAlleleEntity3 = new DynamicEntity(metaDataCanAnnotate);
		singleAlleleEntity3.set(vcfAttributes.CHROM, "1");
		singleAlleleEntity3.set(vcfAttributes.POS, 78383467);
		singleAlleleEntity3.set(vcfAttributes.REF, "G");
		singleAlleleEntity3.set(vcfAttributes.ALT, "A");

		Entity singleAlleleEntity4 = new DynamicEntity(metaDataCanAnnotate);
		singleAlleleEntity4.set(vcfAttributes.CHROM, "21");
		singleAlleleEntity4.set(vcfAttributes.POS, 46924425);
		singleAlleleEntity4.set(vcfAttributes.REF, "CGGCCCCCCA");
		singleAlleleEntity4.set(vcfAttributes.ALT, "C");

		Entity singleAlleleEntity5 = new DynamicEntity(metaDataCanAnnotate);
		singleAlleleEntity5.set(vcfAttributes.CHROM, "X");
		singleAlleleEntity5.set(vcfAttributes.POS, 79943569);
		singleAlleleEntity5.set(vcfAttributes.REF, "T");
		singleAlleleEntity5.set(vcfAttributes.ALT, "C");

		Entity singleAlleleEntity6 = new DynamicEntity(metaDataCanAnnotate);
		singleAlleleEntity6.set(vcfAttributes.CHROM, "2");
		singleAlleleEntity6.set(vcfAttributes.POS, 191904021);
		singleAlleleEntity6.set(vcfAttributes.REF, "G");
		singleAlleleEntity6.set(vcfAttributes.ALT, "T");

		Entity singleAlleleEntity7 = new DynamicEntity(metaDataCanAnnotate);
		singleAlleleEntity7.set(vcfAttributes.CHROM, "3");
		singleAlleleEntity7.set(vcfAttributes.POS, 53219680);
		singleAlleleEntity7.set(vcfAttributes.REF, "G");
		singleAlleleEntity7.set(vcfAttributes.ALT, "C");

		Entity singleAlleleEntity8 = new DynamicEntity(metaDataCanAnnotate);
		singleAlleleEntity8.set(vcfAttributes.CHROM, "1");
		singleAlleleEntity8.set(vcfAttributes.POS, 1115548);
		singleAlleleEntity8.set(vcfAttributes.REF, "G");
		singleAlleleEntity8.set(vcfAttributes.ALT, "A");

		singleAlleleEntities.addAll(
				newArrayList(singleAlleleEntity1, singleAlleleEntity2, singleAlleleEntity3, singleAlleleEntity4,
						singleAlleleEntity5, singleAlleleEntity6, singleAlleleEntity7, singleAlleleEntity8));

		Entity multiAlleleEntity1 = new DynamicEntity(metaDataCanAnnotate);
		multiAlleleEntity1.set(vcfAttributes.CHROM, "1");
		multiAlleleEntity1.set(vcfAttributes.POS, 231094050);
		multiAlleleEntity1.set(vcfAttributes.REF, "GAA");
		multiAlleleEntity1.set(vcfAttributes.ALT, "G,GAAA,GA");

		Entity multiAlleleEntity2 = new DynamicEntity(metaDataCanAnnotate);
		multiAlleleEntity2.set(vcfAttributes.CHROM, "4");
		multiAlleleEntity2.set(vcfAttributes.POS, 69964234);
		multiAlleleEntity2.set(vcfAttributes.REF, "CT");
		multiAlleleEntity2.set(vcfAttributes.ALT, "CTT,CTTT,C");

		Entity multiAlleleEntity3 = new DynamicEntity(metaDataCanAnnotate);
		multiAlleleEntity3.set(vcfAttributes.CHROM, "15");
		multiAlleleEntity3.set(vcfAttributes.POS, 66641732);
		multiAlleleEntity3.set(vcfAttributes.REF, "G");
		multiAlleleEntity3.set(vcfAttributes.ALT, "A,C,T");

		Entity multiAlleleEntity4 = new DynamicEntity(metaDataCanAnnotate);
		multiAlleleEntity4.set(vcfAttributes.CHROM, "21");
		multiAlleleEntity4.set(vcfAttributes.POS, 45650009);
		multiAlleleEntity4.set(vcfAttributes.REF, "T");
		multiAlleleEntity4.set(vcfAttributes.ALT, "TG, A, G");

		multiAlleleEntities.addAll(
				newArrayList(multiAlleleEntity1, multiAlleleEntity2, multiAlleleEntity3, multiAlleleEntity4));

		Entity multiGeneEntity1 = new DynamicEntity(metaDataCanAnnotate);
		multiGeneEntity1.set(vcfAttributes.CHROM, "2");
		multiGeneEntity1.set(vcfAttributes.POS, 171570151);
		multiGeneEntity1.set(vcfAttributes.REF, "C");
		multiGeneEntity1.set(vcfAttributes.ALT, "T");

		Entity multiGeneEntity2 = new DynamicEntity(metaDataCanAnnotate);
		multiGeneEntity2.set(vcfAttributes.CHROM, "2");
		multiGeneEntity2.set(vcfAttributes.POS, 219142023);
		multiGeneEntity2.set(vcfAttributes.REF, "G");
		multiGeneEntity2.set(vcfAttributes.ALT, "A");

		Entity expectedSingleAllele1 = new DynamicEntity(effectsEMD);
		expectedSingleAllele1.set(EffectsMetaData.ID, null);
		expectedSingleAllele1.set(EffectsMetaData.ALT, "G");
		expectedSingleAllele1.set(EffectsMetaData.GENE_NAME, "DDX11L1");
		expectedSingleAllele1.set(EffectsMetaData.VARIANT, singleAlleleEntity1);
		expectedSingleAllele1.set(ANNOTATION, "non_coding_exon_variant");
		expectedSingleAllele1.set(PUTATIVE_IMPACT, "MODIFIER");
		expectedSingleAllele1.set(GENE_NAME, "DDX11L1");
		expectedSingleAllele1.set(GENE_ID, "DDX11L1");
		expectedSingleAllele1.set(FEATURE_TYPE, "transcript");
		expectedSingleAllele1.set(FEATURE_ID, "NR_046018.2");
		expectedSingleAllele1.set(TRANSCRIPT_BIOTYPE, "Noncoding");
		expectedSingleAllele1.set(RANK_TOTAL, "3/3");
		expectedSingleAllele1.set(HGVS_C, "n.623C>G");
		expectedSingleAllele1.set(HGVS_P, "");
		expectedSingleAllele1.set(C_DNA_POSITION, "");
		expectedSingleAllele1.set(CDS_POSITION, "");
		expectedSingleAllele1.set(PROTEIN_POSITION, "");
		expectedSingleAllele1.set(DISTANCE_TO_FEATURE, "");
		expectedSingleAllele1.set(ERRORS, "");

		Entity expectedSingleAllele2 = new DynamicEntity(effectsEMD);
		expectedSingleAllele2.set(EffectsMetaData.ID, null);
		expectedSingleAllele2.set(EffectsMetaData.ALT, "C");
		expectedSingleAllele2.set(EffectsMetaData.GENE_NAME, "DDX11L1");
		expectedSingleAllele2.set(EffectsMetaData.VARIANT, singleAlleleEntity2);
		expectedSingleAllele2.set(ANNOTATION, "non_coding_exon_variant");
		expectedSingleAllele2.set(PUTATIVE_IMPACT, "MODIFIER");
		expectedSingleAllele2.set(GENE_NAME, "DDX11L1");
		expectedSingleAllele2.set(GENE_ID, "DDX11L1");
		expectedSingleAllele2.set(FEATURE_TYPE, "transcript");
		expectedSingleAllele2.set(FEATURE_ID, "NR_046018.2");
		expectedSingleAllele2.set(TRANSCRIPT_BIOTYPE, "Noncoding");
		expectedSingleAllele2.set(RANK_TOTAL, "3/3");
		expectedSingleAllele2.set(HGVS_C, "n.1223T>C");
		expectedSingleAllele2.set(HGVS_P, "");
		expectedSingleAllele2.set(C_DNA_POSITION, "");
		expectedSingleAllele2.set(CDS_POSITION, "");
		expectedSingleAllele2.set(PROTEIN_POSITION, "");
		expectedSingleAllele2.set(DISTANCE_TO_FEATURE, "");
		expectedSingleAllele2.set(ERRORS, "");

		Entity expectedSingleAllele3 = new DynamicEntity(effectsEMD);
		expectedSingleAllele3.set(EffectsMetaData.ID, null);
		expectedSingleAllele3.set(EffectsMetaData.ALT, "A");
		expectedSingleAllele3.set(EffectsMetaData.GENE_NAME, "NEXN");
		expectedSingleAllele3.set(EffectsMetaData.VARIANT, singleAlleleEntity3);
		expectedSingleAllele3.set(ANNOTATION, "intron_variant");
		expectedSingleAllele3.set(PUTATIVE_IMPACT, "MODIFIER");
		expectedSingleAllele3.set(GENE_NAME, "NEXN");
		expectedSingleAllele3.set(GENE_ID, "NEXN");
		expectedSingleAllele3.set(FEATURE_TYPE, "transcript");
		expectedSingleAllele3.set(FEATURE_ID, "NM_144573.3");
		expectedSingleAllele3.set(TRANSCRIPT_BIOTYPE, "Coding");
		expectedSingleAllele3.set(RANK_TOTAL, "3/12");
		expectedSingleAllele3.set(HGVS_C, "c.219+25G>A");
		expectedSingleAllele3.set(HGVS_P, "");
		expectedSingleAllele3.set(C_DNA_POSITION, "");
		expectedSingleAllele3.set(CDS_POSITION, "");
		expectedSingleAllele3.set(PROTEIN_POSITION, "");
		expectedSingleAllele3.set(DISTANCE_TO_FEATURE, "");
		expectedSingleAllele3.set(ERRORS, "");

		Entity expectedSingleAllele4 = new DynamicEntity(effectsEMD);
		expectedSingleAllele4.set(EffectsMetaData.ID, null);
		expectedSingleAllele4.set(EffectsMetaData.ALT, "C");
		expectedSingleAllele4.set(EffectsMetaData.GENE_NAME, "COL18A1");
		expectedSingleAllele4.set(EffectsMetaData.VARIANT, singleAlleleEntity4);
		expectedSingleAllele4.set(ANNOTATION,
				"frameshift_variant&splice_acceptor_variant&splice_donor_variant&splice_region_variant&splice_region_variant&splice_region_variant&intron_variant");
		expectedSingleAllele4.set(PUTATIVE_IMPACT, "HIGH");
		expectedSingleAllele4.set(GENE_NAME, "COL18A1");
		expectedSingleAllele4.set(GENE_ID, "COL18A1");
		expectedSingleAllele4.set(FEATURE_TYPE, "transcript");
		expectedSingleAllele4.set(FEATURE_ID, "NM_030582.3");
		expectedSingleAllele4.set(TRANSCRIPT_BIOTYPE, "Coding");
		expectedSingleAllele4.set(RANK_TOTAL, "33/42");
		expectedSingleAllele4.set(HGVS_C, "c.3364_3365-2delGGCCCCCCA");
		expectedSingleAllele4.set(HGVS_P, "p.Gly1122fs");
		expectedSingleAllele4.set(C_DNA_POSITION, "3385/5894");
		expectedSingleAllele4.set(CDS_POSITION, "3364/4551");
		expectedSingleAllele4.set(PROTEIN_POSITION, "1122/1516");
		expectedSingleAllele4.set(DISTANCE_TO_FEATURE, "");
		expectedSingleAllele4.set(ERRORS, "");

		Entity expectedSingleAllele5 = new DynamicEntity(effectsEMD);
		expectedSingleAllele5.set(EffectsMetaData.ID, null);
		expectedSingleAllele5.set(EffectsMetaData.ALT, "C");
		expectedSingleAllele5.set(EffectsMetaData.GENE_NAME, "BRWD3");
		expectedSingleAllele5.set(EffectsMetaData.VARIANT, singleAlleleEntity5);
		expectedSingleAllele5.set(ANNOTATION, "missense_variant&splice_region_variant");
		expectedSingleAllele5.set(PUTATIVE_IMPACT, "MODERATE");
		expectedSingleAllele5.set(GENE_NAME, "BRWD3");
		expectedSingleAllele5.set(GENE_ID, "BRWD3");
		expectedSingleAllele5.set(FEATURE_TYPE, "transcript");
		expectedSingleAllele5.set(FEATURE_ID, "NM_153252.4");
		expectedSingleAllele5.set(TRANSCRIPT_BIOTYPE, "Coding");
		expectedSingleAllele5.set(RANK_TOTAL, "34/41");
		expectedSingleAllele5.set(HGVS_C, "c.3863A>G");
		expectedSingleAllele5.set(HGVS_P, "p.Lys1288Arg");
		expectedSingleAllele5.set(C_DNA_POSITION, "4126/12793");
		expectedSingleAllele5.set(CDS_POSITION, "3863/5409");
		expectedSingleAllele5.set(PROTEIN_POSITION, "1288/1802");
		expectedSingleAllele5.set(DISTANCE_TO_FEATURE, "");
		expectedSingleAllele5.set(ERRORS, "");

		Entity expectedSingleAllele6 = new DynamicEntity(effectsEMD);
		expectedSingleAllele6.set(EffectsMetaData.ID, null);
		expectedSingleAllele6.set(EffectsMetaData.ALT, "T");
		expectedSingleAllele6.set(EffectsMetaData.GENE_NAME, "STAT4");
		expectedSingleAllele6.set(EffectsMetaData.VARIANT, singleAlleleEntity6);
		expectedSingleAllele6.set(ANNOTATION, "splice_region_variant&synonymous_variant");
		expectedSingleAllele6.set(PUTATIVE_IMPACT, "LOW");
		expectedSingleAllele6.set(GENE_NAME, "STAT4");
		expectedSingleAllele6.set(GENE_ID, "STAT4");
		expectedSingleAllele6.set(FEATURE_TYPE, "transcript");
		expectedSingleAllele6.set(FEATURE_ID, "NM_001243835.1");
		expectedSingleAllele6.set(TRANSCRIPT_BIOTYPE, "Coding");
		expectedSingleAllele6.set(RANK_TOTAL, "16/24");
		expectedSingleAllele6.set(HGVS_C, "c.1338C>A");
		expectedSingleAllele6.set(HGVS_P, "p.Thr446Thr");
		expectedSingleAllele6.set(C_DNA_POSITION, "1602/2775");
		expectedSingleAllele6.set(CDS_POSITION, "1338/2247");
		expectedSingleAllele6.set(PROTEIN_POSITION, "446/748");
		expectedSingleAllele6.set(DISTANCE_TO_FEATURE, "");
		expectedSingleAllele6.set(ERRORS, "");

		Entity expectedSingleAllele7 = new DynamicEntity(effectsEMD);
		expectedSingleAllele7.set(EffectsMetaData.ID, null);
		expectedSingleAllele7.set(EffectsMetaData.ALT, "C");
		expectedSingleAllele7.set(EffectsMetaData.GENE_NAME, "PRKCD");
		expectedSingleAllele7.set(EffectsMetaData.VARIANT, singleAlleleEntity7);
		expectedSingleAllele7.set(ANNOTATION, "missense_variant");
		expectedSingleAllele7.set(PUTATIVE_IMPACT, "MODERATE");
		expectedSingleAllele7.set(GENE_NAME, "PRKCD");
		expectedSingleAllele7.set(GENE_ID, "PRKCD");
		expectedSingleAllele7.set(FEATURE_TYPE, "transcript");
		expectedSingleAllele7.set(FEATURE_ID, "NM_006254.3");
		expectedSingleAllele7.set(TRANSCRIPT_BIOTYPE, "Coding");
		expectedSingleAllele7.set(RANK_TOTAL, "11/19");
		expectedSingleAllele7.set(HGVS_C, "c.949G>C");
		expectedSingleAllele7.set(HGVS_P, "p.Glu317Gln");
		expectedSingleAllele7.set(C_DNA_POSITION, "1302/2835");
		expectedSingleAllele7.set(CDS_POSITION, "949/2031");
		expectedSingleAllele7.set(PROTEIN_POSITION, "317/676");
		expectedSingleAllele7.set(DISTANCE_TO_FEATURE, "");
		expectedSingleAllele7.set(ERRORS, "");

		Entity expectedSingleAllele8 = new DynamicEntity(effectsEMD);
		expectedSingleAllele8.set(EffectsMetaData.ID, null);
		expectedSingleAllele8.set(EffectsMetaData.ALT, "A");
		expectedSingleAllele8.set(EffectsMetaData.GENE_NAME, "TTLL10");
		expectedSingleAllele8.set(EffectsMetaData.VARIANT, singleAlleleEntity8);
		expectedSingleAllele8.set(ANNOTATION, "missense_variant");
		expectedSingleAllele8.set(PUTATIVE_IMPACT, "MODERATE");
		expectedSingleAllele8.set(GENE_NAME, "TTLL10");
		expectedSingleAllele8.set(GENE_ID, "TTLL10");
		expectedSingleAllele8.set(FEATURE_TYPE, "transcript");
		expectedSingleAllele8.set(FEATURE_ID, "NM_001130045.1");
		expectedSingleAllele8.set(TRANSCRIPT_BIOTYPE, "Coding");
		expectedSingleAllele8.set(RANK_TOTAL, "6/16");
		expectedSingleAllele8.set(HGVS_C, "c.334G>A");
		expectedSingleAllele8.set(HGVS_P, "p.Gly112Arg");
		expectedSingleAllele8.set(C_DNA_POSITION, "485/2259");
		expectedSingleAllele8.set(CDS_POSITION, "334/2022");
		expectedSingleAllele8.set(PROTEIN_POSITION, "112/673");
		expectedSingleAllele8.set(DISTANCE_TO_FEATURE, "");
		expectedSingleAllele8.set(ERRORS, "");

		expectedSingleAlleleEffectEntities.addAll(
				newArrayList(expectedSingleAllele1, expectedSingleAllele2, expectedSingleAllele3, expectedSingleAllele4,
						expectedSingleAllele5, expectedSingleAllele6, expectedSingleAllele7, expectedSingleAllele8));

		Entity expectedMultiAllele1 = new DynamicEntity(effectsEMD);
		expectedMultiAllele1.set(EffectsMetaData.ID, null);
		expectedMultiAllele1.set(EffectsMetaData.ALT, "G");
		expectedMultiAllele1.set(EffectsMetaData.GENE_NAME, "TTC13");
		expectedMultiAllele1.set(EffectsMetaData.VARIANT, multiAlleleEntity1);
		expectedMultiAllele1.set(ANNOTATION, "splice_region_variant&intron_variant");
		expectedMultiAllele1.set(PUTATIVE_IMPACT, "LOW");
		expectedMultiAllele1.set(GENE_NAME, "TTC13");
		expectedMultiAllele1.set(GENE_ID, "TTC13");
		expectedMultiAllele1.set(FEATURE_TYPE, "transcript");
		expectedMultiAllele1.set(FEATURE_ID, "NM_024525.4");
		expectedMultiAllele1.set(TRANSCRIPT_BIOTYPE, "Coding");
		expectedMultiAllele1.set(RANK_TOTAL, "2/22");
		expectedMultiAllele1.set(HGVS_C, "c.367-7_367-6delTT");
		expectedMultiAllele1.set(HGVS_P, "");
		expectedMultiAllele1.set(C_DNA_POSITION, "");
		expectedMultiAllele1.set(CDS_POSITION, "");
		expectedMultiAllele1.set(PROTEIN_POSITION, "");
		expectedMultiAllele1.set(DISTANCE_TO_FEATURE, "");
		expectedMultiAllele1.set(ERRORS, "");

		Entity expectedMultiAllele2 = new DynamicEntity(effectsEMD);
		expectedMultiAllele2.set(EffectsMetaData.ID, null);
		expectedMultiAllele2.set(EffectsMetaData.ALT, "GA");
		expectedMultiAllele2.set(EffectsMetaData.GENE_NAME, "TTC13");
		expectedMultiAllele2.set(EffectsMetaData.VARIANT, multiAlleleEntity1);
		expectedMultiAllele2.set(ANNOTATION, "splice_region_variant&intron_variant");
		expectedMultiAllele2.set(PUTATIVE_IMPACT, "LOW");
		expectedMultiAllele2.set(GENE_NAME, "TTC13");
		expectedMultiAllele2.set(GENE_ID, "TTC13");
		expectedMultiAllele2.set(FEATURE_TYPE, "transcript");
		expectedMultiAllele2.set(FEATURE_ID, "NM_024525.4");
		expectedMultiAllele2.set(TRANSCRIPT_BIOTYPE, "Coding");
		expectedMultiAllele2.set(RANK_TOTAL, "2/22");
		expectedMultiAllele2.set(HGVS_C, "c.367-7delT");
		expectedMultiAllele2.set(HGVS_P, "");
		expectedMultiAllele2.set(C_DNA_POSITION, "");
		expectedMultiAllele2.set(CDS_POSITION, "");
		expectedMultiAllele2.set(PROTEIN_POSITION, "");
		expectedMultiAllele2.set(DISTANCE_TO_FEATURE, "");
		expectedMultiAllele2.set(ERRORS, "");

		Entity expectedMultiAllele3 = new DynamicEntity(effectsEMD);
		expectedMultiAllele3.set(EffectsMetaData.ID, null);
		expectedMultiAllele3.set(EffectsMetaData.ALT, "GAAA");
		expectedMultiAllele3.set(EffectsMetaData.GENE_NAME, "TTC13");
		expectedMultiAllele3.set(EffectsMetaData.VARIANT, multiAlleleEntity1);
		expectedMultiAllele3.set(ANNOTATION, "splice_region_variant&intron_variant");
		expectedMultiAllele3.set(PUTATIVE_IMPACT, "LOW");
		expectedMultiAllele3.set(GENE_NAME, "TTC13");
		expectedMultiAllele3.set(GENE_ID, "TTC13");
		expectedMultiAllele3.set(FEATURE_TYPE, "transcript");
		expectedMultiAllele3.set(FEATURE_ID, "NM_024525.4");
		expectedMultiAllele3.set(TRANSCRIPT_BIOTYPE, "Coding");
		expectedMultiAllele3.set(RANK_TOTAL, "2/22");
		expectedMultiAllele3.set(HGVS_C, "c.367-8_367-7insT");
		expectedMultiAllele3.set(HGVS_P, "");
		expectedMultiAllele3.set(C_DNA_POSITION, "");
		expectedMultiAllele3.set(CDS_POSITION, "");
		expectedMultiAllele3.set(PROTEIN_POSITION, "");
		expectedMultiAllele3.set(DISTANCE_TO_FEATURE, "");
		expectedMultiAllele3.set(ERRORS, "");

		Entity expectedMultiAllele4 = new DynamicEntity(effectsEMD);
		expectedMultiAllele4.set(EffectsMetaData.ID, null);
		expectedMultiAllele4.set(EffectsMetaData.ALT, "C");
		expectedMultiAllele4.set(EffectsMetaData.GENE_NAME, "UGT2B7");
		expectedMultiAllele4.set(EffectsMetaData.VARIANT, multiAlleleEntity2);
		expectedMultiAllele4.set(ANNOTATION, "intron_variant");
		expectedMultiAllele4.set(PUTATIVE_IMPACT, "MODIFIER");
		expectedMultiAllele4.set(GENE_NAME, "UGT2B7");
		expectedMultiAllele4.set(GENE_ID, "UGT2B7");
		expectedMultiAllele4.set(FEATURE_TYPE, "transcript");
		expectedMultiAllele4.set(FEATURE_ID, "NM_001074.2");
		expectedMultiAllele4.set(TRANSCRIPT_BIOTYPE, "Coding");
		expectedMultiAllele4.set(RANK_TOTAL, "1/5");
		expectedMultiAllele4.set(HGVS_C, "c.722-23delT");
		expectedMultiAllele4.set(HGVS_P, "");
		expectedMultiAllele4.set(C_DNA_POSITION, "");
		expectedMultiAllele4.set(CDS_POSITION, "");
		expectedMultiAllele4.set(PROTEIN_POSITION, "");
		expectedMultiAllele4.set(DISTANCE_TO_FEATURE, "");
		expectedMultiAllele4.set(ERRORS, "");

		Entity expectedMultiAllele5 = new DynamicEntity(effectsEMD);
		expectedMultiAllele5.set(EffectsMetaData.ID, null);
		expectedMultiAllele5.set(EffectsMetaData.ALT, "CTT");
		expectedMultiAllele5.set(EffectsMetaData.GENE_NAME, "UGT2B7");
		expectedMultiAllele5.set(EffectsMetaData.VARIANT, multiAlleleEntity2);
		expectedMultiAllele5.set(ANNOTATION, "intron_variant");
		expectedMultiAllele5.set(PUTATIVE_IMPACT, "MODIFIER");
		expectedMultiAllele5.set(GENE_NAME, "UGT2B7");
		expectedMultiAllele5.set(GENE_ID, "UGT2B7");
		expectedMultiAllele5.set(FEATURE_TYPE, "transcript");
		expectedMultiAllele5.set(FEATURE_ID, "NM_001074.2");
		expectedMultiAllele5.set(TRANSCRIPT_BIOTYPE, "Coding");
		expectedMultiAllele5.set(RANK_TOTAL, "1/5");
		expectedMultiAllele5.set(HGVS_C, "c.722-23_722-22insT");
		expectedMultiAllele5.set(HGVS_P, "");
		expectedMultiAllele5.set(C_DNA_POSITION, "");
		expectedMultiAllele5.set(CDS_POSITION, "");
		expectedMultiAllele5.set(PROTEIN_POSITION, "");
		expectedMultiAllele5.set(DISTANCE_TO_FEATURE, "");
		expectedMultiAllele5.set(ERRORS, "");

		Entity expectedMultiAllele6 = new DynamicEntity(effectsEMD);
		expectedMultiAllele6.set(EffectsMetaData.ID, null);
		expectedMultiAllele6.set(EffectsMetaData.ALT, "CTTT");
		expectedMultiAllele6.set(EffectsMetaData.GENE_NAME, "UGT2B7");
		expectedMultiAllele6.set(EffectsMetaData.VARIANT, multiAlleleEntity2);
		expectedMultiAllele6.set(ANNOTATION, "intron_variant");
		expectedMultiAllele6.set(PUTATIVE_IMPACT, "MODIFIER");
		expectedMultiAllele6.set(GENE_NAME, "UGT2B7");
		expectedMultiAllele6.set(GENE_ID, "UGT2B7");
		expectedMultiAllele6.set(FEATURE_TYPE, "transcript");
		expectedMultiAllele6.set(FEATURE_ID, "NM_001074.2");
		expectedMultiAllele6.set(TRANSCRIPT_BIOTYPE, "Coding");
		expectedMultiAllele6.set(RANK_TOTAL, "1/5");
		expectedMultiAllele6.set(HGVS_C, "c.722-23_722-22insTT");
		expectedMultiAllele6.set(HGVS_P, "");
		expectedMultiAllele6.set(C_DNA_POSITION, "");
		expectedMultiAllele6.set(CDS_POSITION, "");
		expectedMultiAllele6.set(PROTEIN_POSITION, "");
		expectedMultiAllele6.set(DISTANCE_TO_FEATURE, "");
		expectedMultiAllele6.set(ERRORS, "");

		Entity expectedMultiAllele7 = new DynamicEntity(effectsEMD);
		expectedMultiAllele7.set(EffectsMetaData.ID, null);
		expectedMultiAllele7.set(EffectsMetaData.ALT, "A");
		expectedMultiAllele7.set(EffectsMetaData.GENE_NAME, "TIPIN");
		expectedMultiAllele7.set(EffectsMetaData.VARIANT, multiAlleleEntity3);
		expectedMultiAllele7.set(ANNOTATION, "missense_variant");
		expectedMultiAllele7.set(PUTATIVE_IMPACT, "MODERATE");
		expectedMultiAllele7.set(GENE_NAME, "TIPIN");
		expectedMultiAllele7.set(GENE_ID, "TIPIN");
		expectedMultiAllele7.set(FEATURE_TYPE, "transcript");
		expectedMultiAllele7.set(FEATURE_ID, "NM_017858.2");
		expectedMultiAllele7.set(TRANSCRIPT_BIOTYPE, "Coding");
		expectedMultiAllele7.set(RANK_TOTAL, "5/8");
		expectedMultiAllele7.set(HGVS_C, "c.332C>T");
		expectedMultiAllele7.set(HGVS_P, "p.Ala111Val");
		expectedMultiAllele7.set(C_DNA_POSITION, "418/1280");
		expectedMultiAllele7.set(CDS_POSITION, "332/906");
		expectedMultiAllele7.set(PROTEIN_POSITION, "111/301");
		expectedMultiAllele7.set(DISTANCE_TO_FEATURE, "");
		expectedMultiAllele7.set(ERRORS, "");

		Entity expectedMultiAllele8 = new DynamicEntity(effectsEMD);
		expectedMultiAllele8.set(EffectsMetaData.ID, null);
		expectedMultiAllele8.set(EffectsMetaData.ALT, "C");
		expectedMultiAllele8.set(EffectsMetaData.GENE_NAME, "TIPIN");
		expectedMultiAllele8.set(EffectsMetaData.VARIANT, multiAlleleEntity3);
		expectedMultiAllele8.set(ANNOTATION, "missense_variant");
		expectedMultiAllele8.set(PUTATIVE_IMPACT, "MODERATE");
		expectedMultiAllele8.set(GENE_NAME, "TIPIN");
		expectedMultiAllele8.set(GENE_ID, "TIPIN");
		expectedMultiAllele8.set(FEATURE_TYPE, "transcript");
		expectedMultiAllele8.set(FEATURE_ID, "NM_017858.2");
		expectedMultiAllele8.set(TRANSCRIPT_BIOTYPE, "Coding");
		expectedMultiAllele8.set(RANK_TOTAL, "5/8");
		expectedMultiAllele8.set(HGVS_C, "c.332C>G");
		expectedMultiAllele8.set(HGVS_P, "p.Ala111Gly");
		expectedMultiAllele8.set(C_DNA_POSITION, "418/1280");
		expectedMultiAllele8.set(CDS_POSITION, "332/906");
		expectedMultiAllele8.set(PROTEIN_POSITION, "111/301");
		expectedMultiAllele8.set(DISTANCE_TO_FEATURE, "");
		expectedMultiAllele8.set(ERRORS, "");

		Entity expectedMultiAllele9 = new DynamicEntity(effectsEMD);
		expectedMultiAllele9.set(EffectsMetaData.ID, null);
		expectedMultiAllele9.set(EffectsMetaData.ALT, "T");
		expectedMultiAllele9.set(EffectsMetaData.GENE_NAME, "TIPIN");
		expectedMultiAllele9.set(EffectsMetaData.VARIANT, multiAlleleEntity3);
		expectedMultiAllele9.set(ANNOTATION, "missense_variant");
		expectedMultiAllele9.set(PUTATIVE_IMPACT, "MODERATE");
		expectedMultiAllele9.set(GENE_NAME, "TIPIN");
		expectedMultiAllele9.set(GENE_ID, "TIPIN");
		expectedMultiAllele9.set(FEATURE_TYPE, "transcript");
		expectedMultiAllele9.set(FEATURE_ID, "NM_017858.2");
		expectedMultiAllele9.set(TRANSCRIPT_BIOTYPE, "Coding");
		expectedMultiAllele9.set(RANK_TOTAL, "5/8");
		expectedMultiAllele9.set(HGVS_C, "c.332C>A");
		expectedMultiAllele9.set(HGVS_P, "p.Ala111Glu");
		expectedMultiAllele9.set(C_DNA_POSITION, "418/1280");
		expectedMultiAllele9.set(CDS_POSITION, "332/906");
		expectedMultiAllele9.set(PROTEIN_POSITION, "111/301");
		expectedMultiAllele9.set(DISTANCE_TO_FEATURE, "");
		expectedMultiAllele9.set(ERRORS, "");

		Entity expectedMultiAllele10 = new DynamicEntity(effectsEMD);
		expectedMultiAllele10.set(EffectsMetaData.ID, null);
		expectedMultiAllele10.set(EffectsMetaData.ALT, "A");
		expectedMultiAllele10.set(EffectsMetaData.GENE_NAME, "ICOSLG");
		expectedMultiAllele10.set(EffectsMetaData.VARIANT, multiAlleleEntity4);
		expectedMultiAllele10.set(ANNOTATION, "intron_variant");
		expectedMultiAllele10.set(PUTATIVE_IMPACT, "MODIFIER");
		expectedMultiAllele10.set(GENE_NAME, "ICOSLG");
		expectedMultiAllele10.set(GENE_ID, "ICOSLG");
		expectedMultiAllele10.set(FEATURE_TYPE, "transcript");
		expectedMultiAllele10.set(FEATURE_ID, "NM_001283050.1");
		expectedMultiAllele10.set(TRANSCRIPT_BIOTYPE, "Coding");
		expectedMultiAllele10.set(RANK_TOTAL, "5/6");
		expectedMultiAllele10.set(HGVS_C, "c.863-37A>T");
		expectedMultiAllele10.set(HGVS_P, "");
		expectedMultiAllele10.set(C_DNA_POSITION, "");
		expectedMultiAllele10.set(CDS_POSITION, "");
		expectedMultiAllele10.set(PROTEIN_POSITION, "");
		expectedMultiAllele10.set(DISTANCE_TO_FEATURE, "");
		expectedMultiAllele10.set(ERRORS, "");

		Entity expectedMultiAllele11 = new DynamicEntity(effectsEMD);
		expectedMultiAllele11.set(EffectsMetaData.ID, null);
		expectedMultiAllele11.set(EffectsMetaData.ALT, "G");
		expectedMultiAllele11.set(EffectsMetaData.GENE_NAME, "ICOSLG");
		expectedMultiAllele11.set(EffectsMetaData.VARIANT, multiAlleleEntity4);
		expectedMultiAllele11.set(ANNOTATION, "intron_variant");
		expectedMultiAllele11.set(PUTATIVE_IMPACT, "MODIFIER");
		expectedMultiAllele11.set(GENE_NAME, "ICOSLG");
		expectedMultiAllele11.set(GENE_ID, "ICOSLG");
		expectedMultiAllele11.set(FEATURE_TYPE, "transcript");
		expectedMultiAllele11.set(FEATURE_ID, "NM_001283050.1");
		expectedMultiAllele11.set(TRANSCRIPT_BIOTYPE, "Coding");
		expectedMultiAllele11.set(RANK_TOTAL, "5/6");
		expectedMultiAllele11.set(HGVS_C, "c.863-37A>C");
		expectedMultiAllele11.set(HGVS_P, "");
		expectedMultiAllele11.set(C_DNA_POSITION, "");
		expectedMultiAllele11.set(CDS_POSITION, "");
		expectedMultiAllele11.set(PROTEIN_POSITION, "");
		expectedMultiAllele11.set(DISTANCE_TO_FEATURE, "");
		expectedMultiAllele11.set(ERRORS, "");

		Entity expectedMultiAllele12 = new DynamicEntity(effectsEMD);
		expectedMultiAllele12.set(EffectsMetaData.ID, null);
		expectedMultiAllele12.set(EffectsMetaData.ALT, "TG");
		expectedMultiAllele12.set(EffectsMetaData.GENE_NAME, "ICOSLG");
		expectedMultiAllele12.set(EffectsMetaData.VARIANT, multiAlleleEntity4);
		expectedMultiAllele12.set(ANNOTATION, "intron_variant");
		expectedMultiAllele12.set(PUTATIVE_IMPACT, "MODIFIER");
		expectedMultiAllele12.set(GENE_NAME, "ICOSLG");
		expectedMultiAllele12.set(GENE_ID, "ICOSLG");
		expectedMultiAllele12.set(FEATURE_TYPE, "transcript");
		expectedMultiAllele12.set(FEATURE_ID, "NM_001283050.1");
		expectedMultiAllele12.set(TRANSCRIPT_BIOTYPE, "Coding");
		expectedMultiAllele12.set(RANK_TOTAL, "5/6");
		expectedMultiAllele12.set(HGVS_C, "c.863-38_863-37insC");
		expectedMultiAllele12.set(HGVS_P, "");
		expectedMultiAllele12.set(C_DNA_POSITION, "");
		expectedMultiAllele12.set(CDS_POSITION, "");
		expectedMultiAllele12.set(PROTEIN_POSITION, "");
		expectedMultiAllele12.set(DISTANCE_TO_FEATURE, "");
		expectedMultiAllele12.set(ERRORS, "");

		expectedMultiAlleleEffectEntities.addAll(
				newArrayList(expectedMultiAllele1, expectedMultiAllele2, expectedMultiAllele3, expectedMultiAllele4,
						expectedMultiAllele5, expectedMultiAllele6, expectedMultiAllele7, expectedMultiAllele8,
						expectedMultiAllele9, expectedMultiAllele10, expectedMultiAllele11, expectedMultiAllele12));

		Entity expectedMultiGene1 = new DynamicEntity(effectsEMD);
		expectedMultiGene1.set(EffectsMetaData.ID, null);
		expectedMultiGene1.set(EffectsMetaData.ALT, "T");
		expectedMultiGene1.set(EffectsMetaData.GENE_NAME, "LOC101926913");
		expectedMultiGene1.set(EffectsMetaData.VARIANT, multiGeneEntity1);
		expectedMultiGene1.set(ANNOTATION, "intron_variant");
		expectedMultiGene1.set(PUTATIVE_IMPACT, "MODIFIER");
		expectedMultiGene1.set(GENE_NAME, "LOC101926913");
		expectedMultiGene1.set(GENE_ID, "LOC101926913");
		expectedMultiGene1.set(FEATURE_TYPE, "transcript");
		expectedMultiGene1.set(FEATURE_ID, "NR_110185.1");
		expectedMultiGene1.set(TRANSCRIPT_BIOTYPE, "Noncoding");
		expectedMultiGene1.set(RANK_TOTAL, "5/5");
		expectedMultiGene1.set(HGVS_C, "n.376+9863G>A");
		expectedMultiGene1.set(HGVS_P, "");
		expectedMultiGene1.set(C_DNA_POSITION, "");
		expectedMultiGene1.set(CDS_POSITION, "");
		expectedMultiGene1.set(PROTEIN_POSITION, "");
		expectedMultiGene1.set(DISTANCE_TO_FEATURE, "");
		expectedMultiGene1.set(ERRORS, "");

		Entity expectedMultiGene2 = new DynamicEntity(effectsEMD);
		expectedMultiGene2.set(EffectsMetaData.ID, null);
		expectedMultiGene2.set(EffectsMetaData.ALT, "T");
		expectedMultiGene2.set(EffectsMetaData.GENE_NAME, "LINC01124");
		expectedMultiGene2.set(EffectsMetaData.VARIANT, multiGeneEntity1);
		expectedMultiGene2.set(ANNOTATION, "non_coding_exon_variant");
		expectedMultiGene2.set(PUTATIVE_IMPACT, "MODIFIER");
		expectedMultiGene2.set(GENE_NAME, "LINC01124");
		expectedMultiGene2.set(GENE_ID, "LINC01124");
		expectedMultiGene2.set(FEATURE_TYPE, "transcript");
		expectedMultiGene2.set(FEATURE_ID, "NR_027433.1");
		expectedMultiGene2.set(TRANSCRIPT_BIOTYPE, "Noncoding");
		expectedMultiGene2.set(RANK_TOTAL, "1/1");
		expectedMultiGene2.set(HGVS_C, "n.927G>A");
		expectedMultiGene2.set(HGVS_P, "");
		expectedMultiGene2.set(C_DNA_POSITION, "");
		expectedMultiGene2.set(CDS_POSITION, "");
		expectedMultiGene2.set(PROTEIN_POSITION, "");
		expectedMultiGene2.set(DISTANCE_TO_FEATURE, "");
		expectedMultiGene2.set(ERRORS, "");

		Entity expectedMultiGene3 = new DynamicEntity(effectsEMD);
		expectedMultiGene3.set(EffectsMetaData.ID, null);
		expectedMultiGene3.set(EffectsMetaData.ALT, "A");
		expectedMultiGene3.set(EffectsMetaData.GENE_NAME, "PNKD");
		expectedMultiGene3.set(EffectsMetaData.VARIANT, multiGeneEntity2);
		expectedMultiGene3.set(ANNOTATION, "intron_variant");
		expectedMultiGene3.set(PUTATIVE_IMPACT, "MODIFIER");
		expectedMultiGene3.set(GENE_NAME, "PNKD");
		expectedMultiGene3.set(GENE_ID, "PNKD");
		expectedMultiGene3.set(FEATURE_TYPE, "transcript");
		expectedMultiGene3.set(FEATURE_ID, "NM_015488.4");
		expectedMultiGene3.set(TRANSCRIPT_BIOTYPE, "Coding");
		expectedMultiGene3.set(RANK_TOTAL, "2/9");
		expectedMultiGene3.set(HGVS_C, "c.236+5751G>A");
		expectedMultiGene3.set(HGVS_P, "");
		expectedMultiGene3.set(C_DNA_POSITION, "");
		expectedMultiGene3.set(CDS_POSITION, "");
		expectedMultiGene3.set(PROTEIN_POSITION, "");
		expectedMultiGene3.set(DISTANCE_TO_FEATURE, "");
		expectedMultiGene3.set(ERRORS, "");

		Entity expectedMultiGene4 = new DynamicEntity(effectsEMD);
		expectedMultiGene4.set(EffectsMetaData.ID, null);
		expectedMultiGene4.set(EffectsMetaData.ALT, "A");
		expectedMultiGene4.set(EffectsMetaData.GENE_NAME, "TMBIM1");
		expectedMultiGene4.set(EffectsMetaData.VARIANT, multiGeneEntity2);
		expectedMultiGene4.set(ANNOTATION, "intron_variant");
		expectedMultiGene4.set(PUTATIVE_IMPACT, "MODIFIER");
		expectedMultiGene4.set(GENE_NAME, "TMBIM1");
		expectedMultiGene4.set(GENE_ID, "TMBIM1");
		expectedMultiGene4.set(FEATURE_TYPE, "transcript");
		expectedMultiGene4.set(FEATURE_ID, "NM_022152.4");
		expectedMultiGene4.set(TRANSCRIPT_BIOTYPE, "Coding");
		expectedMultiGene4.set(RANK_TOTAL, "9/11");
		expectedMultiGene4.set(HGVS_C, "c.639+66C>T");
		expectedMultiGene4.set(HGVS_P, "");
		expectedMultiGene4.set(C_DNA_POSITION, "");
		expectedMultiGene4.set(CDS_POSITION, "");
		expectedMultiGene4.set(PROTEIN_POSITION, "");
		expectedMultiGene4.set(DISTANCE_TO_FEATURE, "");
		expectedMultiGene4.set(ERRORS, "");

		multiGeneEntities.addAll(newArrayList(multiGeneEntity1, multiGeneEntity2));
		expectedMultiGeneEffectEntities.addAll(
				newArrayList(expectedMultiGene1, expectedMultiGene2, expectedMultiGene3, expectedMultiGene4));
	}

	@BeforeMethod
	public void beforeMethod() throws IOException
	{
		jarRunner = mock(JarRunnerImpl.class);

		IdGenerator idGenerator = new IdGeneratorImpl();

		snpEffRunner = new SnpEffRunner(jarRunner, snpEffAnnotatorSettings, idGenerator, vcfAttributes, effectsMetaData,
				entityTypeFactory, attributeFactory);
	}

	@Test
	public void annotateCountTest() throws IOException, InterruptedException
	{
		List<String> params = Arrays.asList("-Xmx2g", null, "hg19", "-noStats", "-noLog", "-lof", "-canon", "-ud", "0",
				"-spliceSiteSize", "5");
		when(jarRunner.runJar(SnpEffAnnotator.NAME, params,
				ResourceUtils.getFile(getClass(), "/test-edgecases.vcf"))).thenReturn(
				ResourceUtils.getFile(getClass(), "/snpEffOutputCount.vcf"));

		Iterator<Entity> results = snpEffRunner.getSnpEffects(singleAlleleEntities.iterator(),
				ResourceUtils.getFile(getClass(), "/test-edgecases.vcf"));
		assertEquals(Iterators.size(results), 8);
	}

	@Test
	public void getInputVcfFileTest() throws IOException
	{
		File singleAlleleFile = snpEffRunner.getInputVcfFile(singleAlleleEntities.iterator());
		try (BufferedReader br = new BufferedReader(new FileReader(singleAlleleFile.getAbsolutePath())))
		{
			assertEquals(br.readLine(), "#CHROM	POS ID REF ALT QUAL FILTER INFO".replace(" ", "\t"));
			assertEquals(br.readLine(), "1 13380 . C G .  . .".replace(" ", "\t"));
			assertEquals(br.readLine(), "1 13980 . T C .  . .".replace(" ", "\t"));
			assertEquals(br.readLine(), "1 78383467 . G A .  . .".replace(" ", "\t"));
			assertEquals(br.readLine(), "21 46924425 . CGGCCCCCCA C .  . .".replace(" ", "\t"));
			assertEquals(br.readLine(), "X 79943569 . T C .  . .".replace(" ", "\t"));
			assertEquals(br.readLine(), "2 191904021 . G T .  . .".replace(" ", "\t"));
			assertEquals(br.readLine(), "3 53219680 . G C .  . .".replace(" ", "\t"));
			assertEquals(br.readLine(), "1 1115548 . G A .  . .".replace(" ", "\t"));
		}

		File multiAlleleFile = snpEffRunner.getInputVcfFile(multiAlleleEntities.iterator());
		try (BufferedReader br = new BufferedReader(new FileReader(multiAlleleFile.getAbsolutePath())))
		{
			assertEquals(br.readLine(), "#CHROM	POS ID REF ALT QUAL FILTER INFO".replace(" ", "\t"));
			assertEquals(br.readLine(), "1 231094050 . GAA G,GAAA,GA .  . .".replace(" ", "\t"));
			assertEquals(br.readLine(), "4 69964234 . CT CTT,CTTT,C .  . .".replace(" ", "\t"));
			assertEquals(br.readLine(), "15 66641732 . G A,C,T .  . .".replace(" ", "\t"));
			assertEquals(br.readLine(), "21\t45650009\t.\tT\tTG, A, G\t.\t\t.\t.");
		}

		File multiGeneFile = snpEffRunner.getInputVcfFile(multiGeneEntities.iterator());
		try (BufferedReader br = new BufferedReader(new FileReader(multiGeneFile.getAbsolutePath())))
		{
			assertEquals(br.readLine(), "#CHROM	POS ID REF ALT QUAL FILTER INFO".replace(" ", "\t"));
			assertEquals(br.readLine(), "2 171570151 . C T .  . .".replace(" ", "\t"));
			assertEquals(br.readLine(), "2 219142023 . G A .  . .".replace(" ", "\t"));
		}
	}

	@Test
	public void getSnpEffectsSingleAlleleTest() throws IOException, InterruptedException
	{
		List<String> params = Arrays.asList("-Xmx2g", null, "hg19", "-noStats", "-noLog", "-lof", "-canon", "-ud", "0",
				"-spliceSiteSize", "5");
		when(jarRunner.runJar(SnpEffAnnotator.NAME, params,
				ResourceUtils.getFile(getClass(), "/test-snpeff.vcf"))).thenReturn(
				ResourceUtils.getFile(getClass(), "/snpeff-single-allele-output.vcf"));

		List<Entity> results = newArrayList(snpEffRunner.getSnpEffects(singleAlleleEntities.iterator(),
				ResourceUtils.getFile(getClass(), "/test-snpeff.vcf")));

		// Set id to null to prevent matching auto generated identifiers
		for (Entity resultEntity : results)
		{
			resultEntity.set("id", null);
		}
		assertTrue(EntityUtils.entitiesEquals(results, expectedSingleAlleleEffectEntities));
	}

	@Test
	public void getSnpEffectsMultiAlleleTest() throws IOException, InterruptedException
	{
		List<String> params = Arrays.asList("-Xmx2g", null, "hg19", "-noStats", "-noLog", "-lof", "-canon", "-ud", "0",
				"-spliceSiteSize", "5");
		when(jarRunner.runJar(SnpEffAnnotator.NAME, params,
				ResourceUtils.getFile(getClass(), "/test-snpeff.vcf"))).thenReturn(
				ResourceUtils.getFile(getClass(), "/snpeff-multi-allele-output.vcf"));

		List<Entity> results = newArrayList(snpEffRunner.getSnpEffects(multiAlleleEntities.iterator(),
				ResourceUtils.getFile(getClass(), "/test-snpeff.vcf")));

		// Set id to null to prevent matching auto generated identifiers
		for (Entity resultEntity : results)
		{
			resultEntity.set("id", null);
		}
		assertTrue(EntityUtils.entitiesEquals(results, expectedMultiAlleleEffectEntities));
	}

	@Test
	public void getSnpEffectsMultiGeneTest() throws IOException, InterruptedException
	{
		List<String> params = Arrays.asList("-Xmx2g", null, "hg19", "-noStats", "-noLog", "-lof", "-canon", "-ud", "0",
				"-spliceSiteSize", "5");
		when(jarRunner.runJar(SnpEffAnnotator.NAME, params,
				ResourceUtils.getFile(getClass(), "/test-snpeff.vcf"))).thenReturn(
				ResourceUtils.getFile(getClass(), "/snpeff-multi-gene-output.vcf"));

		List<Entity> results = newArrayList(snpEffRunner.getSnpEffects(multiGeneEntities.iterator(),
				ResourceUtils.getFile(getClass(), "/test-snpeff.vcf")));

		// Set id to null to prevent matching auto generated identifiers
		for (Entity resultEntity : results)
		{
			resultEntity.set("id", null);
		}
		assertTrue(EntityUtils.entitiesEquals(results, expectedMultiGeneEffectEntities));
	}

	@Test
	public void testGetOutputMetaData()
	{
		EntityType sourceEMD = entityTypeFactory.create("source");
		sourceEMD.setPackage(packageFactory.create("package"));
		sourceEMD.setBackend("TestBackend");

		EntityType outputEMD = snpEffRunner.getTargetEntityType(sourceEMD);

		assertEquals(outputEMD.getBackend(), "TestBackend");
		assertEquals(outputEMD.getId(), "sourceEFFECTS");
	}

	@Configuration
	@Import({ VcfTestConfig.class, EffectsTestConfig.class })
	public static class Config
	{
	}
}
