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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Iterators;

public class SnpEffRunnerTest
{
	private final ArrayList<Entity> entities = newArrayList();
	private final List<Entity> expectedEffectEntities = newArrayList();

	private DefaultEntityMetaData metaDataCanAnnotate;
	private VcfEffectsMetaData effectsEMD;

	@InjectMocks
	private SnpEffRunner snpEffRunner;

	@Mock
	private JarRunner jarRunner;

	@Mock
	private Entity snpEffAnnotatorSettings;

	@BeforeMethod
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

		Entity entity1 = new MapEntity(metaDataCanAnnotate);
		entity1.set(VcfRepository.CHROM, "1");
		entity1.set(VcfRepository.POS, 13380);
		entity1.set(VcfRepository.REF, "C");
		entity1.set(VcfRepository.ALT, "G");

		Entity expected1 = new MapEntity(effectsEMD);
		expected1.set(VcfEffectsMetaData.ID, null);
		expected1.set(VcfEffectsMetaData.ALT, "G");
		expected1.set(VcfEffectsMetaData.GENE, "DDX11L1");
		expected1.set(VcfEffectsMetaData.VARIANT, entity1);
		expected1.set(ANNOTATION, "non_coding_exon_variant");
		expected1.set(PUTATIVE_IMPACT, "MODIFIER");
		expected1.set(GENE_NAME, "DDX11L1");
		expected1.set(GENE_ID, "DDX11L1");
		expected1.set(FEATURE_TYPE, "transcript");
		expected1.set(FEATURE_ID, "NR_046018.2");
		expected1.set(TRANSCRIPT_BIOTYPE, "Noncoding");
		expected1.set(RANK_TOTAL, "3/3");
		expected1.set(HGVS_C, "n.623C>G");
		expected1.set(HGVS_P, "");
		expected1.set(C_DNA_POSITION, "");
		expected1.set(CDS_POSITION, "");
		expected1.set(PROTEIN_POSITION, "");
		expected1.set(DISTANCE_TO_FEATURE, "");
		expected1.set(ERRORS, "");

		Entity entity2 = new MapEntity(metaDataCanAnnotate);
		entity2.set(VcfRepository.CHROM, "1");
		entity2.set(VcfRepository.POS, 13980);
		entity2.set(VcfRepository.REF, "T");
		entity2.set(VcfRepository.ALT, "C");

		Entity expected2 = new MapEntity(effectsEMD);
		expected2.set(VcfEffectsMetaData.ID, null);
		expected2.set(VcfEffectsMetaData.ALT, "C");
		expected2.set(VcfEffectsMetaData.GENE, "DDX11L1");
		expected2.set(VcfEffectsMetaData.VARIANT, entity2);
		expected2.set(ANNOTATION, "non_coding_exon_variant");
		expected2.set(PUTATIVE_IMPACT, "MODIFIER");
		expected2.set(GENE_NAME, "DDX11L1");
		expected2.set(GENE_ID, "DDX11L1");
		expected2.set(FEATURE_TYPE, "transcript");
		expected2.set(FEATURE_ID, "NR_046018.2");
		expected2.set(TRANSCRIPT_BIOTYPE, "Noncoding");
		expected2.set(RANK_TOTAL, "3/3");
		expected2.set(HGVS_C, "n.1223T>C");
		expected2.set(HGVS_P, "");
		expected2.set(C_DNA_POSITION, "");
		expected2.set(CDS_POSITION, "");
		expected2.set(PROTEIN_POSITION, "");
		expected2.set(DISTANCE_TO_FEATURE, "");
		expected2.set(ERRORS, "");

		Entity entity3 = new MapEntity(metaDataCanAnnotate);
		entity3.set(VcfRepository.CHROM, "1");
		entity3.set(VcfRepository.POS, 78383467);
		entity3.set(VcfRepository.REF, "G");
		entity3.set(VcfRepository.ALT, "A");

		Entity expected3 = new MapEntity(effectsEMD);
		expected3.set(VcfEffectsMetaData.ID, null);
		expected3.set(VcfEffectsMetaData.ALT, "A");
		expected3.set(VcfEffectsMetaData.GENE, "NEXN");
		expected3.set(VcfEffectsMetaData.VARIANT, entity3);
		expected3.set(ANNOTATION, "intron_variant");
		expected3.set(PUTATIVE_IMPACT, "MODIFIER");
		expected3.set(GENE_NAME, "NEXN");
		expected3.set(GENE_ID, "NEXN");
		expected3.set(FEATURE_TYPE, "transcript");
		expected3.set(FEATURE_ID, "NM_144573.3");
		expected3.set(TRANSCRIPT_BIOTYPE, "Coding");
		expected3.set(RANK_TOTAL, "3/12");
		expected3.set(HGVS_C, "c.219+25G>A");
		expected3.set(HGVS_P, "");
		expected3.set(C_DNA_POSITION, "");
		expected3.set(CDS_POSITION, "");
		expected3.set(PROTEIN_POSITION, "");
		expected3.set(DISTANCE_TO_FEATURE, "");
		expected3.set(ERRORS, "");

		Entity entity4 = new MapEntity(metaDataCanAnnotate);
		entity4.set(VcfRepository.CHROM, "1");
		entity4.set(VcfRepository.POS, 231094050);
		entity4.set(VcfRepository.REF, "GAA");
		entity4.set(VcfRepository.ALT, "G,GAAA,GA");

		Entity expected4 = new MapEntity(effectsEMD);
		expected4.set(VcfEffectsMetaData.ID, null);
		expected4.set(VcfEffectsMetaData.ALT, "G");
		expected4.set(VcfEffectsMetaData.GENE, "TTC13");
		expected4.set(VcfEffectsMetaData.VARIANT, entity4);
		expected4.set(ANNOTATION, "splice_region_variant&intron_variant");
		expected4.set(PUTATIVE_IMPACT, "LOW");
		expected4.set(GENE_NAME, "TTC13");
		expected4.set(GENE_ID, "TTC13");
		expected4.set(FEATURE_TYPE, "transcript");
		expected4.set(FEATURE_ID, "NM_024525.4");
		expected4.set(TRANSCRIPT_BIOTYPE, "Coding");
		expected4.set(RANK_TOTAL, "2/22");
		expected4.set(HGVS_C, "c.367-7_367-6delTT");
		expected4.set(HGVS_P, "");
		expected4.set(C_DNA_POSITION, "");
		expected4.set(CDS_POSITION, "");
		expected4.set(PROTEIN_POSITION, "");
		expected4.set(DISTANCE_TO_FEATURE, "");
		expected4.set(ERRORS, "");

		Entity expected5 = new MapEntity(effectsEMD);
		expected5.set(VcfEffectsMetaData.ID, null);
		expected5.set(VcfEffectsMetaData.ALT, "GA");
		expected5.set(VcfEffectsMetaData.GENE, "TTC13");
		expected5.set(VcfEffectsMetaData.VARIANT, entity4);
		expected5.set(ANNOTATION, "splice_region_variant&intron_variant");
		expected5.set(PUTATIVE_IMPACT, "LOW");
		expected5.set(GENE_NAME, "TTC13");
		expected5.set(GENE_ID, "TTC13");
		expected5.set(FEATURE_TYPE, "transcript");
		expected5.set(FEATURE_ID, "NM_024525.4");
		expected5.set(TRANSCRIPT_BIOTYPE, "Coding");
		expected5.set(RANK_TOTAL, "2/22");
		expected5.set(HGVS_C, "c.367-7delT");
		expected5.set(HGVS_P, "");
		expected5.set(C_DNA_POSITION, "");
		expected5.set(CDS_POSITION, "");
		expected5.set(PROTEIN_POSITION, "");
		expected5.set(DISTANCE_TO_FEATURE, "");
		expected5.set(ERRORS, "");

		Entity expected6 = new MapEntity(effectsEMD);
		expected6.set(VcfEffectsMetaData.ID, null);
		expected6.set(VcfEffectsMetaData.ALT, "GAAA");
		expected6.set(VcfEffectsMetaData.GENE, "TTC13");
		expected6.set(VcfEffectsMetaData.VARIANT, entity4);
		expected6.set(ANNOTATION, "splice_region_variant&intron_variant");
		expected6.set(PUTATIVE_IMPACT, "LOW");
		expected6.set(GENE_NAME, "TTC13");
		expected6.set(GENE_ID, "TTC13");
		expected6.set(FEATURE_TYPE, "transcript");
		expected6.set(FEATURE_ID, "NM_024525.4");
		expected6.set(TRANSCRIPT_BIOTYPE, "Coding");
		expected6.set(RANK_TOTAL, "2/22");
		expected6.set(HGVS_C, "c.367-8_367-7insT");
		expected6.set(HGVS_P, "");
		expected6.set(C_DNA_POSITION, "");
		expected6.set(CDS_POSITION, "");
		expected6.set(PROTEIN_POSITION, "");
		expected6.set(DISTANCE_TO_FEATURE, "");
		expected6.set(ERRORS, "");

		Entity entity5 = new MapEntity(metaDataCanAnnotate);
		entity5.set(VcfRepository.CHROM, "2");
		entity5.set(VcfRepository.POS, 171570151);
		entity5.set(VcfRepository.REF, "C");
		entity5.set(VcfRepository.ALT, "T");

		Entity expected7 = new MapEntity(effectsEMD);
		expected7.set(VcfEffectsMetaData.ID, null);
		expected7.set(VcfEffectsMetaData.ALT, "T");
		expected7.set(VcfEffectsMetaData.GENE, "LOC101926913");
		expected7.set(VcfEffectsMetaData.VARIANT, entity5);
		expected7.set(ANNOTATION, "intron_variant");
		expected7.set(PUTATIVE_IMPACT, "MODIFIER");
		expected7.set(GENE_NAME, "LOC101926913");
		expected7.set(GENE_ID, "LOC101926913");
		expected7.set(FEATURE_TYPE, "transcript");
		expected7.set(FEATURE_ID, "NR_110185.1");
		expected7.set(TRANSCRIPT_BIOTYPE, "Noncoding");
		expected7.set(RANK_TOTAL, "5/5");
		expected7.set(HGVS_C, "n.376+9863G>A");
		expected7.set(HGVS_P, "");
		expected7.set(C_DNA_POSITION, "");
		expected7.set(CDS_POSITION, "");
		expected7.set(PROTEIN_POSITION, "");
		expected7.set(DISTANCE_TO_FEATURE, "");
		expected7.set(ERRORS, "");

		Entity expected8 = new MapEntity(effectsEMD);
		expected8.set(VcfEffectsMetaData.ID, null);
		expected8.set(VcfEffectsMetaData.ALT, "T");
		expected8.set(VcfEffectsMetaData.GENE, "LINC01124");
		expected8.set(VcfEffectsMetaData.VARIANT, entity5);
		expected8.set(ANNOTATION, "non_coding_exon_variant");
		expected8.set(PUTATIVE_IMPACT, "MODIFIER");
		expected8.set(GENE_NAME, "LINC01124");
		expected8.set(GENE_ID, "LINC01124");
		expected8.set(FEATURE_TYPE, "transcript");
		expected8.set(FEATURE_ID, "NR_027433.1");
		expected8.set(TRANSCRIPT_BIOTYPE, "Noncoding");
		expected8.set(RANK_TOTAL, "1/1");
		expected8.set(HGVS_C, "n.927G>A");
		expected8.set(HGVS_P, "");
		expected8.set(C_DNA_POSITION, "");
		expected8.set(CDS_POSITION, "");
		expected8.set(PROTEIN_POSITION, "");
		expected8.set(DISTANCE_TO_FEATURE, "");
		expected8.set(ERRORS, "");

		Entity entity6 = new MapEntity(metaDataCanAnnotate);
		entity6.set(VcfRepository.CHROM, "4");
		entity6.set(VcfRepository.POS, 69964234);
		entity6.set(VcfRepository.REF, "CT");
		entity6.set(VcfRepository.ALT, "CTT,CTTT,C");

		Entity expected9 = new MapEntity(effectsEMD);
		expected9.set(VcfEffectsMetaData.ID, null);
		expected9.set(VcfEffectsMetaData.ALT, "C");
		expected9.set(VcfEffectsMetaData.GENE, "UGT2B7");
		expected9.set(VcfEffectsMetaData.VARIANT, entity6);
		expected9.set(ANNOTATION, "intron_variant");
		expected9.set(PUTATIVE_IMPACT, "MODIFIER");
		expected9.set(GENE_NAME, "UGT2B7");
		expected9.set(GENE_ID, "UGT2B7");
		expected9.set(FEATURE_TYPE, "transcript");
		expected9.set(FEATURE_ID, "NM_001074.2");
		expected9.set(TRANSCRIPT_BIOTYPE, "Coding");
		expected9.set(RANK_TOTAL, "1/5");
		expected9.set(HGVS_C, "c.722-23delT");
		expected9.set(HGVS_P, "");
		expected9.set(C_DNA_POSITION, "");
		expected9.set(CDS_POSITION, "");
		expected9.set(PROTEIN_POSITION, "");
		expected9.set(DISTANCE_TO_FEATURE, "");
		expected9.set(ERRORS, "");

		Entity expected10 = new MapEntity(effectsEMD);
		expected10.set(VcfEffectsMetaData.ID, null);
		expected10.set(VcfEffectsMetaData.ALT, "CTT");
		expected10.set(VcfEffectsMetaData.GENE, "UGT2B7");
		expected10.set(VcfEffectsMetaData.VARIANT, entity6);
		expected10.set(ANNOTATION, "intron_variant");
		expected10.set(PUTATIVE_IMPACT, "MODIFIER");
		expected10.set(GENE_NAME, "UGT2B7");
		expected10.set(GENE_ID, "UGT2B7");
		expected10.set(FEATURE_TYPE, "transcript");
		expected10.set(FEATURE_ID, "NM_001074.2");
		expected10.set(TRANSCRIPT_BIOTYPE, "Coding");
		expected10.set(RANK_TOTAL, "1/5");
		expected10.set(HGVS_C, "c.722-23_722-22insT");
		expected10.set(HGVS_P, "");
		expected10.set(C_DNA_POSITION, "");
		expected10.set(CDS_POSITION, "");
		expected10.set(PROTEIN_POSITION, "");
		expected10.set(DISTANCE_TO_FEATURE, "");
		expected10.set(ERRORS, "");

		Entity expected11 = new MapEntity(effectsEMD);
		expected11.set(VcfEffectsMetaData.ID, null);
		expected11.set(VcfEffectsMetaData.ALT, "CTTT");
		expected11.set(VcfEffectsMetaData.GENE, "UGT2B7");
		expected11.set(VcfEffectsMetaData.VARIANT, entity6);
		expected11.set(ANNOTATION, "intron_variant");
		expected11.set(PUTATIVE_IMPACT, "MODIFIER");
		expected11.set(GENE_NAME, "UGT2B7");
		expected11.set(GENE_ID, "UGT2B7");
		expected11.set(FEATURE_TYPE, "transcript");
		expected11.set(FEATURE_ID, "NM_001074.2");
		expected11.set(TRANSCRIPT_BIOTYPE, "Coding");
		expected11.set(RANK_TOTAL, "1/5");
		expected11.set(HGVS_C, "c.722-23_722-22insTT");
		expected11.set(HGVS_P, "");
		expected11.set(C_DNA_POSITION, "");
		expected11.set(CDS_POSITION, "");
		expected11.set(PROTEIN_POSITION, "");
		expected11.set(DISTANCE_TO_FEATURE, "");
		expected11.set(ERRORS, "");

		Entity entity7 = new MapEntity(metaDataCanAnnotate);
		entity7.set(VcfRepository.CHROM, "15");
		entity7.set(VcfRepository.POS, 66641732);
		entity7.set(VcfRepository.REF, "G");
		entity7.set(VcfRepository.ALT, "A,C,T");

		Entity expected12 = new MapEntity(effectsEMD);
		expected12.set(VcfEffectsMetaData.ID, null);
		expected12.set(VcfEffectsMetaData.ALT, "A");
		expected12.set(VcfEffectsMetaData.GENE, "TIPIN");
		expected12.set(VcfEffectsMetaData.VARIANT, entity7);
		expected12.set(ANNOTATION, "missense_variant");
		expected12.set(PUTATIVE_IMPACT, "MODERATE");
		expected12.set(GENE_NAME, "TIPIN");
		expected12.set(GENE_ID, "TIPIN");
		expected12.set(FEATURE_TYPE, "transcript");
		expected12.set(FEATURE_ID, "NM_017858.2");
		expected12.set(TRANSCRIPT_BIOTYPE, "Coding");
		expected12.set(RANK_TOTAL, "5/8");
		expected12.set(HGVS_C, "c.332C>T");
		expected12.set(HGVS_P, "p.Ala111Val");
		expected12.set(C_DNA_POSITION, "418/1280");
		expected12.set(CDS_POSITION, "332/906");
		expected12.set(PROTEIN_POSITION, "111/301");
		expected12.set(DISTANCE_TO_FEATURE, "");
		expected12.set(ERRORS, "");

		Entity expected13 = new MapEntity(effectsEMD);
		expected13.set(VcfEffectsMetaData.ID, null);
		expected13.set(VcfEffectsMetaData.ALT, "C");
		expected13.set(VcfEffectsMetaData.GENE, "TIPIN");
		expected13.set(VcfEffectsMetaData.VARIANT, entity7);
		expected13.set(ANNOTATION, "missense_variant");
		expected13.set(PUTATIVE_IMPACT, "MODERATE");
		expected13.set(GENE_NAME, "TIPIN");
		expected13.set(GENE_ID, "TIPIN");
		expected13.set(FEATURE_TYPE, "transcript");
		expected13.set(FEATURE_ID, "NM_017858.2");
		expected13.set(TRANSCRIPT_BIOTYPE, "Coding");
		expected13.set(RANK_TOTAL, "5/8");
		expected13.set(HGVS_C, "c.332C>G");
		expected13.set(HGVS_P, "p.Ala111Gly");
		expected13.set(C_DNA_POSITION, "418/1280");
		expected13.set(CDS_POSITION, "332/906");
		expected13.set(PROTEIN_POSITION, "111/301");
		expected13.set(DISTANCE_TO_FEATURE, "");
		expected13.set(ERRORS, "");

		Entity expected14 = new MapEntity(effectsEMD);
		expected14.set(VcfEffectsMetaData.ID, null);
		expected14.set(VcfEffectsMetaData.ALT, "T");
		expected14.set(VcfEffectsMetaData.GENE, "TIPIN");
		expected14.set(VcfEffectsMetaData.VARIANT, entity7);
		expected14.set(ANNOTATION, "missense_variant");
		expected14.set(PUTATIVE_IMPACT, "MODERATE");
		expected14.set(GENE_NAME, "TIPIN");
		expected14.set(GENE_ID, "TIPIN");
		expected14.set(FEATURE_TYPE, "transcript");
		expected14.set(FEATURE_ID, "NM_017858.2");
		expected14.set(TRANSCRIPT_BIOTYPE, "Coding");
		expected14.set(RANK_TOTAL, "5/8");
		expected14.set(HGVS_C, "c.332C>A");
		expected14.set(HGVS_P, "p.Ala111Glu");
		expected14.set(C_DNA_POSITION, "418/1280");
		expected14.set(CDS_POSITION, "332/906");
		expected14.set(PROTEIN_POSITION, "111/301");
		expected14.set(DISTANCE_TO_FEATURE, "");
		expected14.set(ERRORS, "");

		Entity entity8 = new MapEntity(metaDataCanAnnotate);
		entity8.set(VcfRepository.CHROM, "21");
		entity8.set(VcfRepository.POS, 46924425);
		entity8.set(VcfRepository.REF, "CGGCCCCCCA");
		entity8.set(VcfRepository.ALT, "C");

		Entity expected15 = new MapEntity(effectsEMD);
		expected15.set(VcfEffectsMetaData.ID, null);
		expected15.set(VcfEffectsMetaData.ALT, "C");
		expected15.set(VcfEffectsMetaData.GENE, "COL18A1");
		expected15.set(VcfEffectsMetaData.VARIANT, entity8);
		expected15.set(ANNOTATION,
				"frameshift_variant&splice_acceptor_variant&splice_donor_variant&splice_region_variant&splice_region_variant&splice_region_variant&intron_variant");
		expected15.set(PUTATIVE_IMPACT, "HIGH");
		expected15.set(GENE_NAME, "COL18A1");
		expected15.set(GENE_ID, "COL18A1");
		expected15.set(FEATURE_TYPE, "transcript");
		expected15.set(FEATURE_ID, "NM_030582.3");
		expected15.set(TRANSCRIPT_BIOTYPE, "Coding");
		expected15.set(RANK_TOTAL, "33/42");
		expected15.set(HGVS_C, "c.3364_3365-2delGGCCCCCCA");
		expected15.set(HGVS_P, "p.Gly1122fs");
		expected15.set(C_DNA_POSITION, "3385/5894");
		expected15.set(CDS_POSITION, "3364/4551");
		expected15.set(PROTEIN_POSITION, "1122/1516");
		expected15.set(DISTANCE_TO_FEATURE, "");
		expected15.set(ERRORS, "");

		Entity entity9 = new MapEntity(metaDataCanAnnotate);
		entity9.set(VcfRepository.CHROM, "X");
		entity9.set(VcfRepository.POS, 79943569);
		entity9.set(VcfRepository.REF, "T");
		entity9.set(VcfRepository.ALT, "C");

		Entity expected16 = new MapEntity(effectsEMD);
		expected16.set(VcfEffectsMetaData.ID, null);
		expected16.set(VcfEffectsMetaData.ALT, "C");
		expected16.set(VcfEffectsMetaData.GENE, "BRWD3");
		expected16.set(VcfEffectsMetaData.VARIANT, entity9);
		expected16.set(ANNOTATION, "missense_variant&splice_region_variant");
		expected16.set(PUTATIVE_IMPACT, "MODERATE");
		expected16.set(GENE_NAME, "BRWD3");
		expected16.set(GENE_ID, "BRWD3");
		expected16.set(FEATURE_TYPE, "transcript");
		expected16.set(FEATURE_ID, "NM_153252.4");
		expected16.set(TRANSCRIPT_BIOTYPE, "Coding");
		expected16.set(RANK_TOTAL, "34/41");
		expected16.set(HGVS_C, "c.3863A>G");
		expected16.set(HGVS_P, "p.Lys1288Arg");
		expected16.set(C_DNA_POSITION, "4126/12793");
		expected16.set(CDS_POSITION, "3863/5409");
		expected16.set(PROTEIN_POSITION, "1288/1802");
		expected16.set(DISTANCE_TO_FEATURE, "");
		expected16.set(ERRORS, "");

		Entity entity10 = new MapEntity(metaDataCanAnnotate);
		entity10.set(VcfRepository.CHROM, "2");
		entity10.set(VcfRepository.POS, 191904021);
		entity10.set(VcfRepository.REF, "G");
		entity10.set(VcfRepository.ALT, "T");

		Entity expected17 = new MapEntity(effectsEMD);
		expected17.set(VcfEffectsMetaData.ID, null);
		expected17.set(VcfEffectsMetaData.ALT, "T");
		expected17.set(VcfEffectsMetaData.GENE, "STAT4");
		expected17.set(VcfEffectsMetaData.VARIANT, entity10);
		expected17.set(ANNOTATION, "splice_region_variant&synonymous_variant");
		expected17.set(PUTATIVE_IMPACT, "LOW");
		expected17.set(GENE_NAME, "STAT4");
		expected17.set(GENE_ID, "STAT4");
		expected17.set(FEATURE_TYPE, "transcript");
		expected17.set(FEATURE_ID, "NM_001243835.1");
		expected17.set(TRANSCRIPT_BIOTYPE, "Coding");
		expected17.set(RANK_TOTAL, "16/24");
		expected17.set(HGVS_C, "c.1338C>A");
		expected17.set(HGVS_P, "p.Thr446Thr");
		expected17.set(C_DNA_POSITION, "1602/2775");
		expected17.set(CDS_POSITION, "1338/2247");
		expected17.set(PROTEIN_POSITION, "446/748");
		expected17.set(DISTANCE_TO_FEATURE, "");
		expected17.set(ERRORS, "");

		Entity entity11 = new MapEntity(metaDataCanAnnotate);
		entity11.set(VcfRepository.CHROM, "3");
		entity11.set(VcfRepository.POS, 53219680);
		entity11.set(VcfRepository.REF, "G");
		entity11.set(VcfRepository.ALT, "C");

		Entity expected18 = new MapEntity(effectsEMD);
		expected18.set(VcfEffectsMetaData.ID, null);
		expected18.set(VcfEffectsMetaData.ALT, "C");
		expected18.set(VcfEffectsMetaData.GENE, "PRKCD");
		expected18.set(VcfEffectsMetaData.VARIANT, entity11);
		expected18.set(ANNOTATION, "missense_variant");
		expected18.set(PUTATIVE_IMPACT, "MODERATE");
		expected18.set(GENE_NAME, "PRKCD");
		expected18.set(GENE_ID, "PRKCD");
		expected18.set(FEATURE_TYPE, "transcript");
		expected18.set(FEATURE_ID, "NM_006254.3");
		expected18.set(TRANSCRIPT_BIOTYPE, "Coding");
		expected18.set(RANK_TOTAL, "11/19");
		expected18.set(HGVS_C, "c.949G>C");
		expected18.set(HGVS_P, "p.Glu317Gln");
		expected18.set(C_DNA_POSITION, "1302/2835");
		expected18.set(CDS_POSITION, "949/2031");
		expected18.set(PROTEIN_POSITION, "317/676");
		expected18.set(DISTANCE_TO_FEATURE, "");
		expected18.set(ERRORS, "");

		Entity entity12 = new MapEntity(metaDataCanAnnotate);
		entity12.set(VcfRepository.CHROM, "2");
		entity12.set(VcfRepository.POS, 219142023);
		entity12.set(VcfRepository.REF, "G");
		entity12.set(VcfRepository.ALT, "A");

		Entity expected19 = new MapEntity(effectsEMD);
		expected19.set(VcfEffectsMetaData.ID, null);
		expected19.set(VcfEffectsMetaData.ALT, "A");
		expected19.set(VcfEffectsMetaData.GENE, "PNKD");
		expected19.set(VcfEffectsMetaData.VARIANT, entity12);
		expected19.set(ANNOTATION, "intron_variant");
		expected19.set(PUTATIVE_IMPACT, "MODIFIER");
		expected19.set(GENE_NAME, "PNKD");
		expected19.set(GENE_ID, "PNKD");
		expected19.set(FEATURE_TYPE, "transcript");
		expected19.set(FEATURE_ID, "NM_015488.4");
		expected19.set(TRANSCRIPT_BIOTYPE, "Coding");
		expected19.set(RANK_TOTAL, "2/9");
		expected19.set(HGVS_C, "c.236+5751G>A");
		expected19.set(HGVS_P, "");
		expected19.set(C_DNA_POSITION, "");
		expected19.set(CDS_POSITION, "");
		expected19.set(PROTEIN_POSITION, "");
		expected19.set(DISTANCE_TO_FEATURE, "");
		expected19.set(ERRORS, "");

		Entity expected20 = new MapEntity(effectsEMD);
		expected20.set(VcfEffectsMetaData.ID, null);
		expected20.set(VcfEffectsMetaData.ALT, "A");
		expected20.set(VcfEffectsMetaData.GENE, "TMBIM1");
		expected20.set(VcfEffectsMetaData.VARIANT, entity12);
		expected20.set(ANNOTATION, "intron_variant");
		expected20.set(PUTATIVE_IMPACT, "MODIFIER");
		expected20.set(GENE_NAME, "TMBIM1");
		expected20.set(GENE_ID, "TMBIM1");
		expected20.set(FEATURE_TYPE, "transcript");
		expected20.set(FEATURE_ID, "NM_022152.4");
		expected20.set(TRANSCRIPT_BIOTYPE, "Coding");
		expected20.set(RANK_TOTAL, "9/11");
		expected20.set(HGVS_C, "c.639+66C>T");
		expected20.set(HGVS_P, "");
		expected20.set(C_DNA_POSITION, "");
		expected20.set(CDS_POSITION, "");
		expected20.set(PROTEIN_POSITION, "");
		expected20.set(DISTANCE_TO_FEATURE, "");
		expected20.set(ERRORS, "");

		Entity entity13 = new MapEntity(metaDataCanAnnotate);
		entity13.set(VcfRepository.CHROM, "1");
		entity13.set(VcfRepository.POS, 1115548);
		entity13.set(VcfRepository.REF, "G");
		entity13.set(VcfRepository.ALT, "A");

		Entity expected21 = new MapEntity(effectsEMD);
		expected21.set(VcfEffectsMetaData.ID, null);
		expected21.set(VcfEffectsMetaData.ALT, "A");
		expected21.set(VcfEffectsMetaData.GENE, "TTLL10");
		expected21.set(VcfEffectsMetaData.VARIANT, entity13);
		expected21.set(ANNOTATION, "missense_variant");
		expected21.set(PUTATIVE_IMPACT, "MODERATE");
		expected21.set(GENE_NAME, "TTLL10");
		expected21.set(GENE_ID, "TTLL10");
		expected21.set(FEATURE_TYPE, "transcript");
		expected21.set(FEATURE_ID, "NM_001130045.1");
		expected21.set(TRANSCRIPT_BIOTYPE, "Coding");
		expected21.set(RANK_TOTAL, "6/16");
		expected21.set(HGVS_C, "c.334G>A");
		expected21.set(HGVS_P, "p.Gly112Arg");
		expected21.set(C_DNA_POSITION, "485/2259");
		expected21.set(CDS_POSITION, "334/2022");
		expected21.set(PROTEIN_POSITION, "112/673");
		expected21.set(DISTANCE_TO_FEATURE, "");
		expected21.set(ERRORS, "");

		Entity entity14 = new MapEntity(metaDataCanAnnotate);
		entity14.set(VcfRepository.CHROM, "21");
		entity14.set(VcfRepository.POS, 45650009);
		entity14.set(VcfRepository.REF, "T");
		entity14.set(VcfRepository.ALT, "TG, A, G");

		Entity expected22 = new MapEntity(effectsEMD);
		expected22.set(VcfEffectsMetaData.ID, null);
		expected22.set(VcfEffectsMetaData.ALT, "A");
		expected22.set(VcfEffectsMetaData.GENE, "ICOSLG");
		expected22.set(VcfEffectsMetaData.VARIANT, entity14);
		expected22.set(ANNOTATION, "intron_variant");
		expected22.set(PUTATIVE_IMPACT, "MODIFIER");
		expected22.set(GENE_NAME, "ICOSLG");
		expected22.set(GENE_ID, "ICOSLG");
		expected22.set(FEATURE_TYPE, "transcript");
		expected22.set(FEATURE_ID, "NM_001283050.1");
		expected22.set(TRANSCRIPT_BIOTYPE, "Coding");
		expected22.set(RANK_TOTAL, "5/6");
		expected22.set(HGVS_C, "c.863-37A>T");
		expected22.set(HGVS_P, "");
		expected22.set(C_DNA_POSITION, "");
		expected22.set(CDS_POSITION, "");
		expected22.set(PROTEIN_POSITION, "");
		expected22.set(DISTANCE_TO_FEATURE, "");
		expected22.set(ERRORS, "");

		Entity expected23 = new MapEntity(effectsEMD);
		expected23.set(VcfEffectsMetaData.ID, null);
		expected23.set(VcfEffectsMetaData.ALT, "G");
		expected23.set(VcfEffectsMetaData.GENE, "ICOSLG");
		expected23.set(VcfEffectsMetaData.VARIANT, entity14);
		expected23.set(ANNOTATION, "intron_variant");
		expected23.set(PUTATIVE_IMPACT, "MODIFIER");
		expected23.set(GENE_NAME, "ICOSLG");
		expected23.set(GENE_ID, "ICOSLG");
		expected23.set(FEATURE_TYPE, "transcript");
		expected23.set(FEATURE_ID, "NM_001283050.1");
		expected23.set(TRANSCRIPT_BIOTYPE, "Coding");
		expected23.set(RANK_TOTAL, "5/6");
		expected23.set(HGVS_C, "c.863-37A>C");
		expected23.set(HGVS_P, "");
		expected23.set(C_DNA_POSITION, "");
		expected23.set(CDS_POSITION, "");
		expected23.set(PROTEIN_POSITION, "");
		expected23.set(DISTANCE_TO_FEATURE, "");
		expected23.set(ERRORS, "");

		Entity expected24 = new MapEntity(effectsEMD);
		expected24.set(VcfEffectsMetaData.ID, null);
		expected24.set(VcfEffectsMetaData.ALT, "TG");
		expected24.set(VcfEffectsMetaData.GENE, "ICOSLG");
		expected24.set(VcfEffectsMetaData.VARIANT, entity14);
		expected24.set(ANNOTATION, "intron_variant");
		expected24.set(PUTATIVE_IMPACT, "MODIFIER");
		expected24.set(GENE_NAME, "ICOSLG");
		expected24.set(GENE_ID, "ICOSLG");
		expected24.set(FEATURE_TYPE, "transcript");
		expected24.set(FEATURE_ID, "NM_001283050.1");
		expected24.set(TRANSCRIPT_BIOTYPE, "Coding");
		expected24.set(RANK_TOTAL, "5/6");
		expected24.set(HGVS_C, "c.863-38_863-37insC");
		expected24.set(HGVS_P, "");
		expected24.set(C_DNA_POSITION, "");
		expected24.set(CDS_POSITION, "");
		expected24.set(PROTEIN_POSITION, "");
		expected24.set(DISTANCE_TO_FEATURE, "");
		expected24.set(ERRORS, "");

		entities.add(entity1);
		entities.add(entity2);
		entities.add(entity3);
		entities.add(entity4);
		entities.add(entity5);
		entities.add(entity6);
		entities.add(entity7);
		entities.add(entity8);
		entities.add(entity9);
		entities.add(entity10);
		entities.add(entity11);
		entities.add(entity12);
		entities.add(entity13);
		entities.add(entity14);

		expectedEffectEntities.addAll(newArrayList(expected1, expected2, expected3, expected4, expected5, expected6,
				expected7, expected8, expected9, expected10, expected11, expected12, expected13, expected14, expected15,
				expected16, expected17, expected18, expected19, expected20, expected21, expected22, expected23,
				expected24));
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

		Iterator<Entity> results = snpEffRunner.getSnpEffects(entities.iterator(),
				new File("src/test/resources/test-edgecases.vcf"));
		assertEquals(Iterators.size(results), 24);
	}

	@Test
	public void getInputVcfFileTest()
	{
		BufferedReader br = null;
		try
		{
			File file = snpEffRunner.getInputVcfFile(entities.iterator());
			br = new BufferedReader(new FileReader(file.getAbsolutePath()));

			assertEquals(br.readLine(), "1 13380 . C G".replace(" ", "\t"));
			assertEquals(br.readLine(), "1 13980 . T C".replace(" ", "\t"));
			assertEquals(br.readLine(), "1 78383467 . G A".replace(" ", "\t"));
			assertEquals(br.readLine(), "1 231094050 . GAA G,GAAA,GA".replace(" ", "\t"));
			assertEquals(br.readLine(), "2 171570151 . C T".replace(" ", "\t"));
			assertEquals(br.readLine(), "4 69964234 . CT CTT,CTTT,C".replace(" ", "\t"));
			assertEquals(br.readLine(), "15 66641732 . G A,C,T".replace(" ", "\t"));
			assertEquals(br.readLine(), "21 46924425 . CGGCCCCCCA C".replace(" ", "\t"));
			assertEquals(br.readLine(), "X 79943569 . T C".replace(" ", "\t"));
			assertEquals(br.readLine(), "2 191904021 . G T".replace(" ", "\t"));
			assertEquals(br.readLine(), "3 53219680 . G C".replace(" ", "\t"));
			assertEquals(br.readLine(), "2 219142023 . G A".replace(" ", "\t"));
			assertEquals(br.readLine(), "1 1115548 . G A".replace(" ", "\t"));
			assertEquals(br.readLine(), "21\t45650009\t.\tT\tTG, A, G");
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
	public void getSnpEffectsTest()
	{
		try
		{
			List<String> params = Arrays.asList("-Xmx2g", null, "hg19", "-noStats", "-noLog", "-lof", "-canon", "-ud",
					"0", "-spliceSiteSize", "5");
			when(jarRunner.runJar(SnpEffAnnotator.NAME, params, new File("src/test/resources/test-snpeff.vcf")))
					.thenReturn(new File("src/test/resources/snpEffOutput.vcf"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		List<Entity> results = newArrayList(
				snpEffRunner.getSnpEffects(entities.iterator(), new File("src/test/resources/test-snpeff.vcf")));

		for (Entity resultEntity : results)
		{
			resultEntity.set("id", null);
		}

		assertEquals(results, expectedEffectEntities);
	}
}
