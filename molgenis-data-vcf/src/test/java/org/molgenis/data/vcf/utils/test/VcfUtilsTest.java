package org.molgenis.data.vcf.utils.test;

import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.data.vcf.config.VcfTestConfig;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.molgenis.data.vcf.utils.VcfUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;

import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.vcf.model.VcfAttributes.*;
import static org.molgenis.data.vcf.utils.VcfWriterUtils.EFFECT;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = { VcfUtilsTest.Config.class })
public class VcfUtilsTest extends AbstractMolgenisSpringTest
{
	@Autowired
	EntityTypeFactory entityTypeFactory;

	@Autowired
	AttributeFactory attributeFactory;

	@Autowired
	VcfAttributes vcfAttributes;

	@Autowired
	VcfUtils vcfUtils;

	private EntityType annotatedEntityType;
	public EntityType metaDataCanAnnotate;
	public EntityType metaDataCantAnnotate;

	public Attribute attributeChrom;
	public Attribute attributePos;
	public Attribute attributeRef;
	public Attribute attributeAlt;
	public Attribute attributeCantAnnotateChrom;

	public ArrayList<Entity> input = new ArrayList<>();
	public Entity entity;
	public Entity entity1;
	public Entity entity2;
	public Entity entity3;
	public Entity entity4;

	public ArrayList<Entity> entities;
	private EntityType expectedEffectsEntityType;

	@BeforeClass
	public void beforeClass()
	{
		annotatedEntityType = entityTypeFactory.create("test");
		metaDataCanAnnotate = entityTypeFactory.create("test");
		metaDataCantAnnotate = entityTypeFactory.create("test");

		attributeChrom = attributeFactory.create().setName(CHROM).setDataType(STRING);
		attributePos = attributeFactory.create().setName(POS).setDataType(INT);
		attributeRef = attributeFactory.create().setName(REF).setDataType(STRING);
		attributeAlt = attributeFactory.create().setName(ALT).setDataType(STRING);

		attributeCantAnnotateChrom = attributeFactory.create().setName(CHROM).setDataType(LONG);
	}

	@BeforeMethod
	public void beforeMethod() throws IOException
	{
			/*
			 * 1 10050000 test21 G A . PASS AC=21;AN=22;GTC=0,1,10 1 10050001 test22 G A . PASS AC=22;AN=23;GTC=1,2,11 1
			 * 10050002 test23 G A . PASS AC=23;AN=24;GTC=2,3,12
			 */
		metaDataCanAnnotate.addAttribute(attributeChrom, ROLE_ID);
		metaDataCanAnnotate.addAttribute(attributePos);
		metaDataCanAnnotate.addAttribute(attributeRef);
		metaDataCanAnnotate.addAttribute(attributeAlt);

		metaDataCantAnnotate.addAttribute(attributeCantAnnotateChrom);
		metaDataCantAnnotate.addAttribute(attributePos);
		metaDataCantAnnotate.addAttribute(attributeRef);
		metaDataCantAnnotate.addAttribute(attributeAlt);

		entity = new DynamicEntity(metaDataCanAnnotate);
		entity1 = new DynamicEntity(metaDataCanAnnotate);
		entity2 = new DynamicEntity(metaDataCanAnnotate);
		entity3 = new DynamicEntity(metaDataCanAnnotate);
		entity4 = new DynamicEntity(metaDataCanAnnotate);

		metaDataCanAnnotate.addAttribute(attributeFactory.create().setName(ID).setDataType(STRING));
		metaDataCanAnnotate.addAttribute(attributeFactory.create().setName(QUAL).setDataType(STRING));
		metaDataCanAnnotate.addAttribute(attributeFactory.create().setName(FILTER).setDataType(STRING));
		metaDataCanAnnotate.addAttribute(attributeFactory.create()
														 .setName(EFFECT)
														 .setDataType(STRING)
														 .setDescription(
																 "EFFECT annotations: 'Alt_Allele | Gene_Name | Annotation | Putative_impact | Gene_ID | Feature_type | Feature_ID | Transcript_biotype | Rank_total | HGVS_c | HGVS_p | cDNA_position | CDS_position | Protein_position | Distance_to_feature | Errors'"));
		Attribute INFO = attributeFactory.create().setName("INFO").setDataType(COMPOUND);
		Attribute AC = attributeFactory.create().setName("AC").setDataType(STRING).setParent(INFO);
		Attribute AN = attributeFactory.create().setName("AN").setDataType(STRING).setParent(INFO);
		Attribute GTC = attributeFactory.create().setName("GTC").setDataType(STRING).setParent(INFO);
		metaDataCanAnnotate.addAttribute(INFO);
		metaDataCanAnnotate.addAttribute(AC);
		metaDataCanAnnotate.addAttribute(AN);
		metaDataCanAnnotate.addAttribute(GTC);

		annotatedEntityType.addAttribute(attributeChrom, ROLE_ID);
		annotatedEntityType.addAttribute(attributePos);
		annotatedEntityType.addAttribute(attributeRef);
		annotatedEntityType.addAttribute(attributeAlt);

		annotatedEntityType.addAttribute(attributeFactory.create().setName(ID).setDataType(STRING));
		annotatedEntityType.addAttribute(attributeFactory.create().setName(QUAL).setDataType(STRING));
		annotatedEntityType.addAttribute((attributeFactory.create().setName(FILTER).setDataType(STRING)).setDescription(
				"Test that description is not: '" + VcfRepository.DEFAULT_ATTRIBUTE_DESCRIPTION + "'"));
		Attribute annoAttr = attributeFactory.create().setName("ANNO").setDataType(STRING).setParent(INFO);
		annotatedEntityType.addAttribute(INFO);
		annotatedEntityType.addAttribute(AC);
		annotatedEntityType.addAttribute(AN);
		annotatedEntityType.addAttribute(GTC);
		annotatedEntityType.addAttribute(annoAttr);
		metaDataCanAnnotate.addAttribute(annoAttr);

		entity1.set(VcfAttributes.CHROM, "1");
		entity1.set(VcfAttributes.POS, 10050000);
		entity1.set(VcfAttributes.ID, "test21");
		entity1.set(VcfAttributes.REF, "G");
		entity1.set(VcfAttributes.ALT, "A");
		entity1.set(VcfAttributes.QUAL, ".");
		entity1.set(VcfAttributes.FILTER, "PASS");
		entity1.set("AC", "21");
		entity1.set("AN", "22");
		entity1.set("GTC", "0,1,10");
		entity1.set(EFFECT,
				"A|TUBB8|missense_variant|MODERATE|TUBB8|transcript|NM_177987.2|Coding|4/4|c.1286C>T|p.Thr429Met|1286/1504|1286/1335|429/444||");

		entity2.set(VcfAttributes.CHROM, "1");
		entity2.set(VcfAttributes.POS, 10050001);
		entity2.set(VcfAttributes.ID, "test22");
		entity2.set(VcfAttributes.REF, "G");
		entity2.set(VcfAttributes.ALT, "A");
		entity2.set(VcfAttributes.QUAL, ".");
		entity2.set(VcfAttributes.FILTER, "PASS");
		entity2.set(EFFECT,
				"A|TUBB8|missense_variant|MODERATE|TUBB8|transcript|NM_177987.2|Coding|4/4|c.1286C>T|p.Thr429Met|1286/1504|1286/1335|429/444||,A|GEN2|missense_variant|MODERATE|GEN2|transcript|NM_177987.2|Coding|4/4|c.1286C>T|p.Thr429Met|1286/1504|1286/1335|429/444||");

		entity3.set(VcfAttributes.CHROM, "1");
		entity3.set(VcfAttributes.POS, 10050002);
		entity3.set(VcfAttributes.ID, "test23");
		entity3.set(VcfAttributes.REF, "G");
		entity3.set(VcfAttributes.ALT, "A");
		entity3.set(VcfAttributes.QUAL, ".");
		entity3.set(VcfAttributes.FILTER, "PASS");

		entities = new ArrayList<>();
		entities.add(entity1);
		entities.add(entity2);
		entities.add(entity3);

		expectedEffectsEntityType = entityTypeFactory.create("EFFECTannotations");
		expectedEffectsEntityType.addAttribute(attributeFactory.create().setName("identifier").setDataType(STRING),
				ROLE_ID);
		expectedEffectsEntityType.addAttribute(attributeFactory.create().setName("Alt_Allele").setDataType(STRING));
		expectedEffectsEntityType.addAttribute(attributeFactory.create().setName("Gene_Name").setDataType(STRING));
		expectedEffectsEntityType.addAttribute(attributeFactory.create().setName("Annotation").setDataType(STRING));
		expectedEffectsEntityType.addAttribute(
				attributeFactory.create().setName("Putative_impact").setDataType(STRING));
		expectedEffectsEntityType.addAttribute(attributeFactory.create().setName("Gene_ID").setDataType(STRING));
		expectedEffectsEntityType.addAttribute(attributeFactory.create().setName("Feature_type").setDataType(STRING));
		expectedEffectsEntityType.addAttribute(attributeFactory.create().setName("Feature_ID").setDataType(STRING));
		expectedEffectsEntityType.addAttribute(
				attributeFactory.create().setName("Transcript_biotype").setDataType(STRING));
		expectedEffectsEntityType.addAttribute(attributeFactory.create().setName("Rank_total").setDataType(STRING));
		expectedEffectsEntityType.addAttribute(attributeFactory.create().setName("HGVS_c").setDataType(STRING));
		expectedEffectsEntityType.addAttribute(attributeFactory.create().setName("HGVS_p").setDataType(STRING));
		expectedEffectsEntityType.addAttribute(attributeFactory.create().setName("cDNA_position").setDataType(STRING));
		expectedEffectsEntityType.addAttribute(attributeFactory.create().setName("CDS_position").setDataType(STRING));
		expectedEffectsEntityType.addAttribute(
				attributeFactory.create().setName("Protein_position").setDataType(STRING));
		expectedEffectsEntityType.addAttribute(
				attributeFactory.create().setName("Distance_to_feature").setDataType(STRING));
		expectedEffectsEntityType.addAttribute(attributeFactory.create().setName("Errors").setDataType(STRING));
		expectedEffectsEntityType.addAttribute(
				attributeFactory.create().setName("VARIANT").setDataType(XREF).setRefEntity(metaDataCanAnnotate));
	}

	@Test
	public void createId()
	{
		assertEquals(VcfUtils.createId(entity1), "VWnsXKOj5B7PBk4dwFLsQw");
	}

	@Configuration
	@Import(VcfTestConfig.class)
	public static class Config
	{

	}
}