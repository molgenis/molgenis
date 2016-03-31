package org.molgenis.data.annotation.entity.impl;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.LONG;
import static org.molgenis.MolgenisFieldTypes.STRING;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.support.VcfEffectsMetaData.ANNOTATION;
import static org.molgenis.data.support.VcfEffectsMetaData.CDS_POSITION;
import static org.molgenis.data.support.VcfEffectsMetaData.C_DNA_POSITION;
import static org.molgenis.data.support.VcfEffectsMetaData.DISTANCE_TO_FEATURE;
import static org.molgenis.data.support.VcfEffectsMetaData.ERRORS;
import static org.molgenis.data.support.VcfEffectsMetaData.FEATURE_ID;
import static org.molgenis.data.support.VcfEffectsMetaData.FEATURE_TYPE;
import static org.molgenis.data.support.VcfEffectsMetaData.GENE_ID;
import static org.molgenis.data.support.VcfEffectsMetaData.GENE_NAME;
import static org.molgenis.data.support.VcfEffectsMetaData.HGVS_C;
import static org.molgenis.data.support.VcfEffectsMetaData.HGVS_P;
import static org.molgenis.data.support.VcfEffectsMetaData.PROTEIN_POSITION;
import static org.molgenis.data.support.VcfEffectsMetaData.PUTATIVE_IMPACT;
import static org.molgenis.data.support.VcfEffectsMetaData.RANK_TOTAL;
import static org.molgenis.data.support.VcfEffectsMetaData.TRANSCRIPT_BIOTYPE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.molgenis.data.Entity;
import org.molgenis.data.IdGenerator;
import org.molgenis.data.annotation.utils.JarRunner;
import org.molgenis.data.annotation.utils.JarRunnerImpl;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.UuidGenerator;
import org.molgenis.data.support.VcfEffectsMetaData;
import org.molgenis.data.vcf.VcfRepository;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.Iterators;

public class SnpEffRunnerTest
{
	private final ArrayList<Entity> singleAlleleEntities = newArrayList();
	private final ArrayList<Entity> multiAlleleEntities = newArrayList();
	private final ArrayList<Entity> multiGeneEntities = newArrayList();

	private final List<Entity> expectedSingleAlleleEffectEntities = newArrayList();
	private final List<Entity> expectedMultiAlleleEffectEntities = newArrayList();
	private final List<Entity> expectedMultiGeneEffectEntities = newArrayList();

	private DefaultEntityMetaData metaDataCanAnnotate;
	private VcfEffectsMetaData effectsEMD;

	@InjectMocks
	private SnpEffRunner snpEffRunner;

	@Mock
	private JarRunner jarRunner;

	@Mock
	private Entity snpEffAnnotatorSettings;

	@BeforeClass
	public void beforeMethod() throws IOException
	{
		jarRunner = mock(JarRunnerImpl.class);

		IdGenerator idGenerator = new UuidGenerator();

		snpEffRunner = new SnpEffRunner(jarRunner, snpEffAnnotatorSettings, idGenerator);

		metaDataCanAnnotate = new DefaultEntityMetaData("test");
		metaDataCanAnnotate.addAttribute(VcfRepository.CHROM, ROLE_ID).setDataType(STRING);
		metaDataCanAnnotate.addAttribute(VcfRepository.POS).setDataType(LONG);
		metaDataCanAnnotate.addAttribute(VcfRepository.REF).setDataType(STRING);
		metaDataCanAnnotate.addAttribute(VcfRepository.ALT).setDataType(STRING);

		effectsEMD = new VcfEffectsMetaData(metaDataCanAnnotate);

		Entity singleAlleleEntity1 = new MapEntity(metaDataCanAnnotate);
		singleAlleleEntity1.set(VcfRepository.CHROM, "1");
		singleAlleleEntity1.set(VcfRepository.POS, 13380);
		singleAlleleEntity1.set(VcfRepository.REF, "C");
		singleAlleleEntity1.set(VcfRepository.ALT, "G");

		Entity expectedSingleAllele1 = new MapEntity(effectsEMD);
		expectedSingleAllele1.set(VcfEffectsMetaData.ID, null);
		expectedSingleAllele1.set(VcfEffectsMetaData.ALT, "G");
		expectedSingleAllele1.set(VcfEffectsMetaData.GENE, "DDX11L1");
		expectedSingleAllele1.set(VcfEffectsMetaData.VARIANT, singleAlleleEntity1);
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

		Entity singleAlleleEntity2 = new MapEntity(metaDataCanAnnotate);
		singleAlleleEntity2.set(VcfRepository.CHROM, "1");
		singleAlleleEntity2.set(VcfRepository.POS, 13980);
		singleAlleleEntity2.set(VcfRepository.REF, "T");
		singleAlleleEntity2.set(VcfRepository.ALT, "C");

		Entity expectedSingleAllele2 = new MapEntity(effectsEMD);
		expectedSingleAllele2.set(VcfEffectsMetaData.ID, null);
		expectedSingleAllele2.set(VcfEffectsMetaData.ALT, "C");
		expectedSingleAllele2.set(VcfEffectsMetaData.GENE, "DDX11L1");
		expectedSingleAllele2.set(VcfEffectsMetaData.VARIANT, singleAlleleEntity2);
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

		Entity singleAlleleEntity3 = new MapEntity(metaDataCanAnnotate);
		singleAlleleEntity3.set(VcfRepository.CHROM, "1");
		singleAlleleEntity3.set(VcfRepository.POS, 78383467);
		singleAlleleEntity3.set(VcfRepository.REF, "G");
		singleAlleleEntity3.set(VcfRepository.ALT, "A");

		Entity expectedSingleAllele3 = new MapEntity(effectsEMD);
		expectedSingleAllele3.set(VcfEffectsMetaData.ID, null);
		expectedSingleAllele3.set(VcfEffectsMetaData.ALT, "A");
		expectedSingleAllele3.set(VcfEffectsMetaData.GENE, "NEXN");
		expectedSingleAllele3.set(VcfEffectsMetaData.VARIANT, singleAlleleEntity3);
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

		Entity singleAlleleEntity4 = new MapEntity(metaDataCanAnnotate);
		singleAlleleEntity4.set(VcfRepository.CHROM, "21");
		singleAlleleEntity4.set(VcfRepository.POS, 46924425);
		singleAlleleEntity4.set(VcfRepository.REF, "CGGCCCCCCA");
		singleAlleleEntity4.set(VcfRepository.ALT, "C");

		Entity expectedSingleAllele4 = new MapEntity(effectsEMD);
		expectedSingleAllele4.set(VcfEffectsMetaData.ID, null);
		expectedSingleAllele4.set(VcfEffectsMetaData.ALT, "C");
		expectedSingleAllele4.set(VcfEffectsMetaData.GENE, "COL18A1");
		expectedSingleAllele4.set(VcfEffectsMetaData.VARIANT, singleAlleleEntity4);
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

		Entity singleAlleleEntity5 = new MapEntity(metaDataCanAnnotate);
		singleAlleleEntity5.set(VcfRepository.CHROM, "X");
		singleAlleleEntity5.set(VcfRepository.POS, 79943569);
		singleAlleleEntity5.set(VcfRepository.REF, "T");
		singleAlleleEntity5.set(VcfRepository.ALT, "C");

		Entity expectedSingleAllele5 = new MapEntity(effectsEMD);
		expectedSingleAllele5.set(VcfEffectsMetaData.ID, null);
		expectedSingleAllele5.set(VcfEffectsMetaData.ALT, "C");
		expectedSingleAllele5.set(VcfEffectsMetaData.GENE, "BRWD3");
		expectedSingleAllele5.set(VcfEffectsMetaData.VARIANT, singleAlleleEntity5);
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

		Entity singleAlleleEntity6 = new MapEntity(metaDataCanAnnotate);
		singleAlleleEntity6.set(VcfRepository.CHROM, "2");
		singleAlleleEntity6.set(VcfRepository.POS, 191904021);
		singleAlleleEntity6.set(VcfRepository.REF, "G");
		singleAlleleEntity6.set(VcfRepository.ALT, "T");

		Entity expectedSingleAllele6 = new MapEntity(effectsEMD);
		expectedSingleAllele6.set(VcfEffectsMetaData.ID, null);
		expectedSingleAllele6.set(VcfEffectsMetaData.ALT, "T");
		expectedSingleAllele6.set(VcfEffectsMetaData.GENE, "STAT4");
		expectedSingleAllele6.set(VcfEffectsMetaData.VARIANT, singleAlleleEntity6);
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

		Entity singleAlleleEntity7 = new MapEntity(metaDataCanAnnotate);
		singleAlleleEntity7.set(VcfRepository.CHROM, "3");
		singleAlleleEntity7.set(VcfRepository.POS, 53219680);
		singleAlleleEntity7.set(VcfRepository.REF, "G");
		singleAlleleEntity7.set(VcfRepository.ALT, "C");

		Entity expectedSingleAllele7 = new MapEntity(effectsEMD);
		expectedSingleAllele7.set(VcfEffectsMetaData.ID, null);
		expectedSingleAllele7.set(VcfEffectsMetaData.ALT, "C");
		expectedSingleAllele7.set(VcfEffectsMetaData.GENE, "PRKCD");
		expectedSingleAllele7.set(VcfEffectsMetaData.VARIANT, singleAlleleEntity7);
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

		Entity singleAlleleEntity8 = new MapEntity(metaDataCanAnnotate);
		singleAlleleEntity8.set(VcfRepository.CHROM, "1");
		singleAlleleEntity8.set(VcfRepository.POS, 1115548);
		singleAlleleEntity8.set(VcfRepository.REF, "G");
		singleAlleleEntity8.set(VcfRepository.ALT, "A");

		Entity expectedSingleAllele8 = new MapEntity(effectsEMD);
		expectedSingleAllele8.set(VcfEffectsMetaData.ID, null);
		expectedSingleAllele8.set(VcfEffectsMetaData.ALT, "A");
		expectedSingleAllele8.set(VcfEffectsMetaData.GENE, "TTLL10");
		expectedSingleAllele8.set(VcfEffectsMetaData.VARIANT, singleAlleleEntity8);
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

		singleAlleleEntities
				.addAll(newArrayList(singleAlleleEntity1, singleAlleleEntity2, singleAlleleEntity3, singleAlleleEntity4,
						singleAlleleEntity5, singleAlleleEntity6, singleAlleleEntity7, singleAlleleEntity8));
		expectedSingleAlleleEffectEntities.addAll(
				newArrayList(expectedSingleAllele1, expectedSingleAllele2, expectedSingleAllele3, expectedSingleAllele4,
						expectedSingleAllele5, expectedSingleAllele6, expectedSingleAllele7, expectedSingleAllele8));

		Entity multiAlleleEntity1 = new MapEntity(metaDataCanAnnotate);
		multiAlleleEntity1.set(VcfRepository.CHROM, "1");
		multiAlleleEntity1.set(VcfRepository.POS, 231094050);
		multiAlleleEntity1.set(VcfRepository.REF, "GAA");
		multiAlleleEntity1.set(VcfRepository.ALT, "G,GAAA,GA");

		Entity expectedMultiAllele1 = new MapEntity(effectsEMD);
		expectedMultiAllele1.set(VcfEffectsMetaData.ID, null);
		expectedMultiAllele1.set(VcfEffectsMetaData.ALT, "G");
		expectedMultiAllele1.set(VcfEffectsMetaData.GENE, "TTC13");
		expectedMultiAllele1.set(VcfEffectsMetaData.VARIANT, multiAlleleEntity1);
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

		Entity expectedMultiAllele2 = new MapEntity(effectsEMD);
		expectedMultiAllele2.set(VcfEffectsMetaData.ID, null);
		expectedMultiAllele2.set(VcfEffectsMetaData.ALT, "GA");
		expectedMultiAllele2.set(VcfEffectsMetaData.GENE, "TTC13");
		expectedMultiAllele2.set(VcfEffectsMetaData.VARIANT, multiAlleleEntity1);
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

		Entity expectedMultiAllele3 = new MapEntity(effectsEMD);
		expectedMultiAllele3.set(VcfEffectsMetaData.ID, null);
		expectedMultiAllele3.set(VcfEffectsMetaData.ALT, "GAAA");
		expectedMultiAllele3.set(VcfEffectsMetaData.GENE, "TTC13");
		expectedMultiAllele3.set(VcfEffectsMetaData.VARIANT, multiAlleleEntity1);
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

		Entity multiAlleleEntity2 = new MapEntity(metaDataCanAnnotate);
		multiAlleleEntity2.set(VcfRepository.CHROM, "4");
		multiAlleleEntity2.set(VcfRepository.POS, 69964234);
		multiAlleleEntity2.set(VcfRepository.REF, "CT");
		multiAlleleEntity2.set(VcfRepository.ALT, "CTT,CTTT,C");

		Entity expectedMultiAllele4 = new MapEntity(effectsEMD);
		expectedMultiAllele4.set(VcfEffectsMetaData.ID, null);
		expectedMultiAllele4.set(VcfEffectsMetaData.ALT, "C");
		expectedMultiAllele4.set(VcfEffectsMetaData.GENE, "UGT2B7");
		expectedMultiAllele4.set(VcfEffectsMetaData.VARIANT, multiAlleleEntity2);
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

		Entity expectedMultiAllele5 = new MapEntity(effectsEMD);
		expectedMultiAllele5.set(VcfEffectsMetaData.ID, null);
		expectedMultiAllele5.set(VcfEffectsMetaData.ALT, "CTT");
		expectedMultiAllele5.set(VcfEffectsMetaData.GENE, "UGT2B7");
		expectedMultiAllele5.set(VcfEffectsMetaData.VARIANT, multiAlleleEntity2);
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

		Entity expectedMultiAllele6 = new MapEntity(effectsEMD);
		expectedMultiAllele6.set(VcfEffectsMetaData.ID, null);
		expectedMultiAllele6.set(VcfEffectsMetaData.ALT, "CTTT");
		expectedMultiAllele6.set(VcfEffectsMetaData.GENE, "UGT2B7");
		expectedMultiAllele6.set(VcfEffectsMetaData.VARIANT, multiAlleleEntity2);
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

		Entity multiAlleleEntity3 = new MapEntity(metaDataCanAnnotate);
		multiAlleleEntity3.set(VcfRepository.CHROM, "15");
		multiAlleleEntity3.set(VcfRepository.POS, 66641732);
		multiAlleleEntity3.set(VcfRepository.REF, "G");
		multiAlleleEntity3.set(VcfRepository.ALT, "A,C,T");

		Entity expectedMultiAllele7 = new MapEntity(effectsEMD);
		expectedMultiAllele7.set(VcfEffectsMetaData.ID, null);
		expectedMultiAllele7.set(VcfEffectsMetaData.ALT, "A");
		expectedMultiAllele7.set(VcfEffectsMetaData.GENE, "TIPIN");
		expectedMultiAllele7.set(VcfEffectsMetaData.VARIANT, multiAlleleEntity3);
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

		Entity expectedMultiAllele8 = new MapEntity(effectsEMD);
		expectedMultiAllele8.set(VcfEffectsMetaData.ID, null);
		expectedMultiAllele8.set(VcfEffectsMetaData.ALT, "C");
		expectedMultiAllele8.set(VcfEffectsMetaData.GENE, "TIPIN");
		expectedMultiAllele8.set(VcfEffectsMetaData.VARIANT, multiAlleleEntity3);
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

		Entity expectedMultiAllele9 = new MapEntity(effectsEMD);
		expectedMultiAllele9.set(VcfEffectsMetaData.ID, null);
		expectedMultiAllele9.set(VcfEffectsMetaData.ALT, "T");
		expectedMultiAllele9.set(VcfEffectsMetaData.GENE, "TIPIN");
		expectedMultiAllele9.set(VcfEffectsMetaData.VARIANT, multiAlleleEntity3);
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

		Entity multiAlleleEntity4 = new MapEntity(metaDataCanAnnotate);
		multiAlleleEntity4.set(VcfRepository.CHROM, "21");
		multiAlleleEntity4.set(VcfRepository.POS, 45650009);
		multiAlleleEntity4.set(VcfRepository.REF, "T");
		multiAlleleEntity4.set(VcfRepository.ALT, "TG, A, G");

		Entity expectedMultiAllele10 = new MapEntity(effectsEMD);
		expectedMultiAllele10.set(VcfEffectsMetaData.ID, null);
		expectedMultiAllele10.set(VcfEffectsMetaData.ALT, "A");
		expectedMultiAllele10.set(VcfEffectsMetaData.GENE, "ICOSLG");
		expectedMultiAllele10.set(VcfEffectsMetaData.VARIANT, multiAlleleEntity4);
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

		Entity expectedMultiAllele11 = new MapEntity(effectsEMD);
		expectedMultiAllele11.set(VcfEffectsMetaData.ID, null);
		expectedMultiAllele11.set(VcfEffectsMetaData.ALT, "G");
		expectedMultiAllele11.set(VcfEffectsMetaData.GENE, "ICOSLG");
		expectedMultiAllele11.set(VcfEffectsMetaData.VARIANT, multiAlleleEntity4);
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

		Entity expectedMultiAllele12 = new MapEntity(effectsEMD);
		expectedMultiAllele12.set(VcfEffectsMetaData.ID, null);
		expectedMultiAllele12.set(VcfEffectsMetaData.ALT, "TG");
		expectedMultiAllele12.set(VcfEffectsMetaData.GENE, "ICOSLG");
		expectedMultiAllele12.set(VcfEffectsMetaData.VARIANT, multiAlleleEntity4);
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

		multiAlleleEntities
				.addAll(newArrayList(multiAlleleEntity1, multiAlleleEntity2, multiAlleleEntity3, multiAlleleEntity4));
		expectedMultiAlleleEffectEntities.addAll(
				newArrayList(expectedMultiAllele1, expectedMultiAllele2, expectedMultiAllele3, expectedMultiAllele4,
						expectedMultiAllele5, expectedMultiAllele6, expectedMultiAllele7, expectedMultiAllele8,
						expectedMultiAllele9, expectedMultiAllele10, expectedMultiAllele11, expectedMultiAllele12));

		Entity multiGeneEntity1 = new MapEntity(metaDataCanAnnotate);
		multiGeneEntity1.set(VcfRepository.CHROM, "2");
		multiGeneEntity1.set(VcfRepository.POS, 171570151);
		multiGeneEntity1.set(VcfRepository.REF, "C");
		multiGeneEntity1.set(VcfRepository.ALT, "T");

		Entity expectedMultiGene1 = new MapEntity(effectsEMD);
		expectedMultiGene1.set(VcfEffectsMetaData.ID, null);
		expectedMultiGene1.set(VcfEffectsMetaData.ALT, "T");
		expectedMultiGene1.set(VcfEffectsMetaData.GENE, "LOC101926913");
		expectedMultiGene1.set(VcfEffectsMetaData.VARIANT, multiGeneEntity1);
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

		Entity expectedMultiGene2 = new MapEntity(effectsEMD);
		expectedMultiGene2.set(VcfEffectsMetaData.ID, null);
		expectedMultiGene2.set(VcfEffectsMetaData.ALT, "T");
		expectedMultiGene2.set(VcfEffectsMetaData.GENE, "LINC01124");
		expectedMultiGene2.set(VcfEffectsMetaData.VARIANT, multiGeneEntity1);
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

		Entity multiGeneEntity2 = new MapEntity(metaDataCanAnnotate);
		multiGeneEntity2.set(VcfRepository.CHROM, "2");
		multiGeneEntity2.set(VcfRepository.POS, 219142023);
		multiGeneEntity2.set(VcfRepository.REF, "G");
		multiGeneEntity2.set(VcfRepository.ALT, "A");

		Entity expectedMultiGene3 = new MapEntity(effectsEMD);
		expectedMultiGene3.set(VcfEffectsMetaData.ID, null);
		expectedMultiGene3.set(VcfEffectsMetaData.ALT, "A");
		expectedMultiGene3.set(VcfEffectsMetaData.GENE, "PNKD");
		expectedMultiGene3.set(VcfEffectsMetaData.VARIANT, multiGeneEntity2);
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

		Entity expectedMultiGene4 = new MapEntity(effectsEMD);
		expectedMultiGene4.set(VcfEffectsMetaData.ID, null);
		expectedMultiGene4.set(VcfEffectsMetaData.ALT, "A");
		expectedMultiGene4.set(VcfEffectsMetaData.GENE, "TMBIM1");
		expectedMultiGene4.set(VcfEffectsMetaData.VARIANT, multiGeneEntity2);
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
		expectedMultiGeneEffectEntities
				.addAll(newArrayList(expectedMultiGene1, expectedMultiGene2, expectedMultiGene3, expectedMultiGene4));
	}

	@Test
	public void annotateCountTest()
	{
		try
		{
			List<String> params = Arrays.asList("-Xmx2g", null, "hg19", "-noStats", "-noLog", "-lof", "-canon", "-ud",
					"0", "-spliceSiteSize", "5");
			when(jarRunner.runJar(SnpEffAnnotator.NAME, params, new File("src/test/resources/test-edgecases.vcf")))
					.thenReturn(new File("src/test/resources/snpEffOutputCount.vcf"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		Iterator<Entity> results = snpEffRunner.getSnpEffects(singleAlleleEntities.iterator(),
				new File("src/test/resources/test-edgecases.vcf"));
		assertEquals(Iterators.size(results), 8);
	}

	@Test
	public void getInputVcfFileTest()
	{
		BufferedReader br = null;
		try
		{
			File singleAlleleFile = snpEffRunner.getInputVcfFile(singleAlleleEntities.iterator());
			br = new BufferedReader(new FileReader(singleAlleleFile.getAbsolutePath()));

			assertEquals(br.readLine(), "1 13380 . C G".replace(" ", "\t"));
			assertEquals(br.readLine(), "1 13980 . T C".replace(" ", "\t"));
			assertEquals(br.readLine(), "1 78383467 . G A".replace(" ", "\t"));
			assertEquals(br.readLine(), "21 46924425 . CGGCCCCCCA C".replace(" ", "\t"));
			assertEquals(br.readLine(), "X 79943569 . T C".replace(" ", "\t"));
			assertEquals(br.readLine(), "2 191904021 . G T".replace(" ", "\t"));
			assertEquals(br.readLine(), "3 53219680 . G C".replace(" ", "\t"));
			assertEquals(br.readLine(), "1 1115548 . G A".replace(" ", "\t"));
			br.close();

			File multiAlleleFile = snpEffRunner.getInputVcfFile(multiAlleleEntities.iterator());
			br = new BufferedReader(new FileReader(multiAlleleFile.getAbsolutePath()));
			assertEquals(br.readLine(), "1 231094050 . GAA G,GAAA,GA".replace(" ", "\t"));
			assertEquals(br.readLine(), "4 69964234 . CT CTT,CTTT,C".replace(" ", "\t"));
			assertEquals(br.readLine(), "15 66641732 . G A,C,T".replace(" ", "\t"));
			assertEquals(br.readLine(), "21\t45650009\t.\tT\tTG, A, G");
			br.close();

			File multiGeneFile = snpEffRunner.getInputVcfFile(multiGeneEntities.iterator());
			br = new BufferedReader(new FileReader(multiGeneFile.getAbsolutePath()));
			assertEquals(br.readLine(), "2 171570151 . C T".replace(" ", "\t"));
			assertEquals(br.readLine(), "2 219142023 . G A".replace(" ", "\t"));
			br.close();
		}
		catch (Exception e)
		{
			fail();
		}
		finally
		{
			try
			{
				br.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	@Test
	public void getSnpEffectsSingleAlleleTest()
	{
		try
		{
			List<String> params = Arrays.asList("-Xmx2g", null, "hg19", "-noStats", "-noLog", "-lof", "-canon", "-ud",
					"0", "-spliceSiteSize", "5");
			when(jarRunner.runJar(SnpEffAnnotator.NAME, params, new File("src/test/resources/test-snpeff.vcf")))
					.thenReturn(new File("src/test/resources/snpeff-single-allele-output.vcf"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		List<Entity> results = newArrayList(snpEffRunner.getSnpEffects(expectedSingleAlleleEffectEntities.iterator(),
				new File("src/test/resources/test-snpeff.vcf")));

		// Set id to null to prevent matching auto generated identifiers
		for (Entity resultEntity : results)
		{
			resultEntity.set("id", null);
		}
		assertEquals(results, expectedSingleAlleleEffectEntities);
	}

	@Test
	public void getSnpEffectsMultiAlleleTest()
	{
		try
		{
			List<String> params = Arrays.asList("-Xmx2g", null, "hg19", "-noStats", "-noLog", "-lof", "-canon", "-ud",
					"0", "-spliceSiteSize", "5");
			when(jarRunner.runJar(SnpEffAnnotator.NAME, params, new File("src/test/resources/test-snpeff.vcf")))
					.thenReturn(new File("src/test/resources/snpeff-multi-allele-output.vcf"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		List<Entity> results = newArrayList(snpEffRunner.getSnpEffects(multiAlleleEntities.iterator(),
				new File("src/test/resources/test-snpeff.vcf")));

		// Set id to null to prevent matching auto generated identifiers
		for (Entity resultEntity : results)
		{
			resultEntity.set("id", null);
		}
		assertEquals(results, expectedMultiAlleleEffectEntities);
	}

	@Test
	public void getSnpEffectsMultiGeneTest()
	{
		try
		{
			List<String> params = Arrays.asList("-Xmx2g", null, "hg19", "-noStats", "-noLog", "-lof", "-canon", "-ud",
					"0", "-spliceSiteSize", "5");
			when(jarRunner.runJar(SnpEffAnnotator.NAME, params, new File("src/test/resources/test-snpeff.vcf")))
					.thenReturn(new File("src/test/resources/snpeff-multi-gene-output.vcf"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		List<Entity> results = newArrayList(snpEffRunner.getSnpEffects(multiGeneEntities.iterator(),
				new File("src/test/resources/test-snpeff.vcf")));

		// Set id to null to prevent matching auto generated identifiers
		for (Entity resultEntity : results)
		{
			resultEntity.set("id", null);
		}
		assertEquals(results, expectedMultiGeneEffectEntities);
	}
}
