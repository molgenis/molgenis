package org.molgenis.annotation.test.cmd.conversion;

import com.google.common.collect.Iterables;
import org.mockito.quality.Strictness;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.core.utils.EffectStructureConverter;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.data.vcf.model.VcfAttributes.*;
import static org.molgenis.data.vcf.utils.VcfWriterUtils.EFFECT;
import static org.molgenis.data.vcf.utils.VcfWriterUtils.VARIANT;
import static org.testng.Assert.*;

@ContextConfiguration(classes = { EffectStructureConverterTest.Config.class })
public class EffectStructureConverterTest extends AbstractMolgenisSpringTest
{
	@Autowired
	EntityTypeFactory entityTypeFactory;

	@Autowired
	AttributeFactory attributeFactory;

	@Autowired
	VcfAttributes vcfAttributes;

	@Autowired
	EffectStructureConverter effectStructureConverter;

	private EntityType annotatedEntityType;
	public EntityType vcfInputEntityType;
	public EntityType effectEntityType;
	public EntityType variantEntityType;

	public Attribute attributeChrom;
	public Attribute attributePos;
	public Attribute attributeRef;
	public Attribute attributeAlt;
	public Attribute attributeCantAnnotateChrom;

	public Entity entity1;
	public Entity entity2;
	public Entity entity3;

	public ArrayList<Entity> entities;
	private Entity variant1;
	private Entity variant2;

	public EffectStructureConverterTest()
	{
		super(Strictness.WARN);
	}

	@BeforeClass
	public void beforeClass()
	{
		Attribute identifier = attributeFactory.create()
											   .setName("identifier")
											   .setDataType(STRING)
											   .setIdAttribute(true)
											   .setVisible(false);
		Attribute INFO = attributeFactory.create().setName("INFO").setDataType(COMPOUND);
		Attribute AC = attributeFactory.create().setName("AC").setDataType(STRING).setParent(INFO);
		Attribute AN = attributeFactory.create().setName("AN").setDataType(STRING).setParent(INFO);
		Attribute GTC = attributeFactory.create().setName("GTC").setDataType(STRING).setParent(INFO);
		Attribute annoAttr = attributeFactory.create().setName("ANNO").setDataType(STRING).setParent(INFO);

		annotatedEntityType = entityTypeFactory.create("test");
		vcfInputEntityType = entityTypeFactory.create("test");
		variantEntityType = entityTypeFactory.create("test");
		effectEntityType = entityTypeFactory.create("test");

		attributeChrom = attributeFactory.create().setName(CHROM).setDataType(STRING);
		attributePos = attributeFactory.create().setName(POS).setDataType(INT);
		attributeRef = attributeFactory.create().setName(REF).setDataType(STRING);
		attributeAlt = attributeFactory.create().setName(ALT).setDataType(STRING);

		attributeCantAnnotateChrom = attributeFactory.create().setName(CHROM).setDataType(LONG);

		vcfInputEntityType.addAttribute(identifier);
		vcfInputEntityType.addAttribute(attributeChrom);
		vcfInputEntityType.addAttribute(attributePos);
		vcfInputEntityType.addAttribute(attributeRef);
		vcfInputEntityType.addAttribute(attributeAlt);
		vcfInputEntityType.addAttribute(attributeFactory.create().setName(ID).setDataType(STRING));
		vcfInputEntityType.addAttribute(attributeFactory.create().setName(QUAL).setDataType(STRING));
		vcfInputEntityType.addAttribute(attributeFactory.create().setName(FILTER).setDataType(STRING));
		vcfInputEntityType.addAttribute(attributeFactory.create()
														.setName(EFFECT)
														.setDataType(STRING)
														.setDescription(
																"EFFECT annotations: 'Alt_Allele | Gene_Name | Annotation | Putative_impact | Gene_ID | Feature_type | Feature_ID | Transcript_biotype | Rank_total | HGVS_c | HGVS_p | cDNA_position | CDS_position | Protein_position | Distance_to_feature | Errors'"));
		vcfInputEntityType.addAttribute(INFO);
		vcfInputEntityType.addAttribute(AC);
		vcfInputEntityType.addAttribute(AN);
		vcfInputEntityType.addAttribute(GTC);
		vcfInputEntityType.addAttribute(annoAttr);

		annotatedEntityType.addAttribute(identifier);
		annotatedEntityType.addAttribute(attributeChrom);
		annotatedEntityType.addAttribute(attributePos);
		annotatedEntityType.addAttribute(attributeRef);
		annotatedEntityType.addAttribute(attributeAlt);
		annotatedEntityType.addAttribute(attributeFactory.create().setName(ID).setDataType(STRING));
		annotatedEntityType.addAttribute(attributeFactory.create().setName(QUAL).setDataType(STRING));
		annotatedEntityType.addAttribute((attributeFactory.create().setName(FILTER).setDataType(STRING)).setDescription(
				"Test that description is not: '" + VcfRepository.DEFAULT_ATTRIBUTE_DESCRIPTION + "'"));
		annotatedEntityType.addAttribute(INFO);
		annotatedEntityType.addAttribute(AC);
		annotatedEntityType.addAttribute(AN);
		annotatedEntityType.addAttribute(GTC);
		annotatedEntityType.addAttribute(annoAttr);

		variantEntityType.addAttribute(identifier);
		variantEntityType.addAttribute(attributeChrom);
		variantEntityType.addAttribute(attributePos);
		variantEntityType.addAttribute(attributeRef);
		variantEntityType.addAttribute(attributeAlt);
		variantEntityType.addAttribute(attributeFactory.create().setName(ID).setDataType(STRING));
		variantEntityType.addAttribute(attributeFactory.create().setName(QUAL).setDataType(STRING));
		variantEntityType.addAttribute((attributeFactory.create().setName(FILTER).setDataType(STRING)).setDescription(
				"Test that description is not: '" + VcfRepository.DEFAULT_ATTRIBUTE_DESCRIPTION + "'"));
		variantEntityType.addAttribute(INFO);
		variantEntityType.addAttribute(AC);
		variantEntityType.addAttribute(AN);
		variantEntityType.addAttribute(GTC);
		variantEntityType.addAttribute(annoAttr);

		effectEntityType.addAttribute(attributeFactory.create()
													  .setName("identifier")
													  .setDataType(STRING)
													  .setIdAttribute(true)
													  .setAuto(true)
													  .setVisible(false));
		effectEntityType.addAttribute(attributeFactory.create().setName("Alt_Allele").setDataType(STRING));
		effectEntityType.addAttribute(attributeFactory.create().setName("Gene_Name").setDataType(STRING));
		effectEntityType.addAttribute((attributeFactory.create().setName("Annotation").setDataType(STRING)));
		effectEntityType.addAttribute(attributeFactory.create().setName("Putative_impact").setDataType(STRING));
		effectEntityType.addAttribute(attributeFactory.create().setName("Gene_ID").setDataType(STRING));
		effectEntityType.addAttribute(attributeFactory.create().setName("Feature_type").setDataType(STRING));
		effectEntityType.addAttribute(attributeFactory.create().setName("Feature_ID").setDataType(STRING));
		effectEntityType.addAttribute(attributeFactory.create().setName("Transcript_biotype").setDataType(STRING));
		effectEntityType.addAttribute(attributeFactory.create().setName("Rank_total").setDataType(STRING));
	}

	@BeforeMethod
	public void beforeMethod() throws IOException
	{
		entity1 = new DynamicEntity(vcfInputEntityType);
		entity2 = new DynamicEntity(vcfInputEntityType);
		entity3 = new DynamicEntity(vcfInputEntityType);

		entity1.set("identifier", "variant_ID1");
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
				"A|GEN1|missense_variant|MODERATE|GEN1|transcript|NM_123456.7|Coding|4/4|c.1234C>T|p.Thr123Met|1234/5678|2345/6789|111/222||");

		entity2.set("identifier", "variant_ID2");
		entity2.set(VcfAttributes.CHROM, "1");
		entity2.set(VcfAttributes.POS, 10050001);
		entity2.set(VcfAttributes.ID, "test22");
		entity2.set(VcfAttributes.REF, "G");
		entity2.set(VcfAttributes.ALT, "A");
		entity2.set(VcfAttributes.QUAL, ".");
		entity2.set(VcfAttributes.FILTER, "PASS");
		entity2.set(EFFECT,
				"A|GEN1|missense_variant|MODERATE|GEN1|transcript|NM_123456.7|Coding|4/4|c.1234C>T|p.Thr123Met|1234/5678|2345/6789|111/222||,A|GEN2|missense_variant|MODERATE|GEN2|transcript|NM_123456.7|Coding|4/4|c.1234C>T|p.Thr123Met|1234/5678|2345/6789|111/222||");

		entity3.set("identifier", "variant_ID3");
		entity3.set(VcfAttributes.CHROM, "1");
		entity3.set(VcfAttributes.POS, 10050002);
		entity3.set(VcfAttributes.ID, "test23");
		entity3.set(VcfAttributes.REF, "G");
		entity3.set(VcfAttributes.ALT, "A");
		entity3.set(VcfAttributes.QUAL, ".");
		entity3.set(VcfAttributes.FILTER, "PASS");

		variant1 = new DynamicEntity(variantEntityType);
		variant2 = new DynamicEntity(variantEntityType);

		variant1.set("identifier", "variant_ID1");
		variant1.set(VcfAttributes.CHROM, "1");
		variant1.set(VcfAttributes.POS, 10050000);
		variant1.set(VcfAttributes.ID, "test21");
		variant1.set(VcfAttributes.REF, "G");
		variant1.set(VcfAttributes.ALT, "A");
		variant1.set(VcfAttributes.QUAL, ".");
		variant1.set(VcfAttributes.FILTER, "PASS");
		variant1.set("AC", "21");
		variant1.set("AN", "22");
		variant1.set("GTC", "0,1,10");

		variant2.set("identifier", "variant_ID2");
		variant2.set(VcfAttributes.CHROM, "1");
		variant2.set(VcfAttributes.POS, 10050001);
		variant2.set(VcfAttributes.ID, "test22");
		variant2.set(VcfAttributes.REF, "G");
		variant2.set(VcfAttributes.ALT, "A");
		variant2.set(VcfAttributes.QUAL, ".");
		variant2.set(VcfAttributes.FILTER, "PASS");

		entities = new ArrayList<>();
		entities.add(entity1);
		entities.add(entity2);
		entities.add(entity3);
	}

	@Test
	public void testGetDescription()
	{
		assertEquals(effectStructureConverter.getEffectDescription(vcfInputEntityType.getAttribute(EFFECT)),
				"EFFECT annotations: 'Alt_Allele | Gene_Name | Annotation | Putative_impact | Gene_ID | Feature_type | Feature_ID | Transcript_biotype | Rank_total | HGVS_c | HGVS_p | cDNA_position | CDS_position | Protein_position | Distance_to_feature | Errors'");
	}

	@Test
	public void testCreateVcfEntityStructure()
	{
		Entity effect1 = mock(Entity.class);
		Entity effect2 = mock(Entity.class);
		Entity effect3 = mock(Entity.class);

		when(effect1.getIdValue()).thenReturn("effect_ID1");
		when(effect1.getEntity(VARIANT)).thenReturn(variant1);
		when(effect1.getEntityType()).thenReturn(effectEntityType);
		when(effect1.get("Alt_Allele")).thenReturn("1");
		when(effect1.get("Gene_Name")).thenReturn("2");
		when(effect1.get("Annotation")).thenReturn("3");
		when(effect1.get("Putative_impact")).thenReturn("4");
		when(effect1.get("Gene_ID")).thenReturn("5");
		when(effect1.get("Feature_type")).thenReturn("6");
		when(effect1.get("Feature_ID")).thenReturn("7");
		when(effect1.get("Transcript_biotype")).thenReturn("8");
		when(effect1.get("Rank_total")).thenReturn("9");

		when(effect2.getIdValue()).thenReturn("effect_ID2");
		when(effect2.getEntity(VARIANT)).thenReturn(variant1);
		when(effect2.getEntityType()).thenReturn(effectEntityType);
		when(effect2.get("Alt_Allele")).thenReturn("1");
		when(effect2.get("Gene_Name")).thenReturn("2");
		when(effect2.get("Annotation")).thenReturn("3");
		when(effect2.get("Putative_impact")).thenReturn("4");
		when(effect2.get("Gene_ID")).thenReturn("5");
		when(effect2.get("Feature_type")).thenReturn("6");
		when(effect2.get("Feature_ID")).thenReturn("7");
		when(effect2.get("Transcript_biotype")).thenReturn("8");
		when(effect2.get("Rank_total")).thenReturn("9");

		when(effect3.getIdValue()).thenReturn("effect_ID3");
		when(effect3.getEntity(VARIANT)).thenReturn(variant2);
		when(effect3.getEntityType()).thenReturn(effectEntityType);
		when(effect3.get("Alt_Allele")).thenReturn("1");
		when(effect3.get("Gene_Name")).thenReturn("2");
		when(effect3.get("Annotation")).thenReturn("3");
		when(effect3.get("Putative_impact")).thenReturn("4");
		when(effect3.get("Gene_ID")).thenReturn("5");
		when(effect3.get("Feature_type")).thenReturn("6");
		when(effect3.get("Feature_ID")).thenReturn("7");
		when(effect3.get("Transcript_biotype")).thenReturn("8");
		when(effect3.get("Rank_total")).thenReturn("9");

		Iterator<Entity> result = effectStructureConverter.createVcfEntityStructure(
				Arrays.asList(effect1, effect2, effect3).iterator());
		assertTrue(result.hasNext());
		Entity expectedVariant1 = result.next();
		assertEquals(2, Iterables.size(expectedVariant1.getEntities("EFFECT")));
		Iterator<Entity> effectsIterator = expectedVariant1.getEntities("EFFECT").iterator();
		assertEquals(effectsIterator.next().getIdValue(), "effect_ID1");
		assertEquals(effectsIterator.next().getIdValue(), "effect_ID2");
		assertTrue(result.hasNext());
		Entity expectedVariant2 = result.next();
		assertEquals(1, Iterables.size(expectedVariant2.getEntities("EFFECT")));
		effectsIterator = expectedVariant2.getEntities("EFFECT").iterator();
		assertEquals(effectsIterator.next().getIdValue(), "effect_ID3");
		assertFalse(result.hasNext());
	}

	@Test
	public void testCreateVariantEffectStructure()
	{
		VcfRepository vcfRepository = mock(VcfRepository.class);
		when(vcfRepository.getEntityType()).thenReturn(vcfInputEntityType);
		when(vcfRepository.spliterator()).thenReturn(entities.spliterator());
		List<Entity> resultEntities = effectStructureConverter.createVariantEffectStructure(EFFECT,
				Collections.emptyList(), vcfRepository).collect(Collectors.toList());

		assertEquals(resultEntities.size(), 3);

		assertEquals(resultEntities.get(0).get("Alt_Allele"), "A");
		assertEquals(resultEntities.get(0).get("Gene_Name"), "GEN1");
		assertEquals(resultEntities.get(0).get("Annotation"), "missense_variant");
		assertEquals(resultEntities.get(0).get("Putative_impact"), "MODERATE");
		assertEquals(resultEntities.get(0).get("Gene_ID"), "GEN1");
		assertEquals(resultEntities.get(0).get("Feature_type"), "transcript");
		assertEquals(resultEntities.get(0).get("Feature_ID"), "NM_123456.7");
		assertEquals(resultEntities.get(0).get("Transcript_biotype"), "Coding");
		assertEquals(resultEntities.get(0).get("Rank_total"), "4/4");
		assertEquals(resultEntities.get(0).get("HGVS_c"), "c.1234C>T");
		assertEquals(resultEntities.get(0).get("HGVS_p"), "p.Thr123Met");
		assertEquals(resultEntities.get(0).get("cDNA_position"), "1234/5678");
		assertEquals(resultEntities.get(0).get("CDS_position"), "2345/6789");
		assertEquals(resultEntities.get(0).get("Protein_position"), "111/222");
		assertEquals(resultEntities.get(0).get("Distance_to_feature"), "");
		assertEquals(resultEntities.get(0).get("Errors"), "");
		assertEquals(resultEntities.get(0).get("VARIANT").toString(), variant1.toString());

		assertEquals(resultEntities.get(1).get("Alt_Allele"), "A");
		assertEquals(resultEntities.get(1).get("Gene_Name"), "GEN1");
		assertEquals(resultEntities.get(1).get("Annotation"), "missense_variant");
		assertEquals(resultEntities.get(1).get("Putative_impact"), "MODERATE");
		assertEquals(resultEntities.get(1).get("Gene_ID"), "GEN1");
		assertEquals(resultEntities.get(1).get("Feature_type"), "transcript");
		assertEquals(resultEntities.get(1).get("Feature_ID"), "NM_123456.7");
		assertEquals(resultEntities.get(1).get("Transcript_biotype"), "Coding");
		assertEquals(resultEntities.get(1).get("Rank_total"), "4/4");
		assertEquals(resultEntities.get(1).get("HGVS_c"), "c.1234C>T");
		assertEquals(resultEntities.get(1).get("HGVS_p"), "p.Thr123Met");
		assertEquals(resultEntities.get(1).get("cDNA_position"), "1234/5678");
		assertEquals(resultEntities.get(1).get("CDS_position"), "2345/6789");
		assertEquals(resultEntities.get(1).get("Protein_position"), "111/222");
		assertEquals(resultEntities.get(1).get("Distance_to_feature"), "");
		assertEquals(resultEntities.get(1).get("Errors"), "");
		assertEquals(resultEntities.get(1).get("VARIANT").toString(), variant2.toString());

		assertEquals(resultEntities.get(2).get("Alt_Allele"), "A");
		assertEquals(resultEntities.get(2).get("Gene_Name"), "GEN2");
		assertEquals(resultEntities.get(2).get("Annotation"), "missense_variant");
		assertEquals(resultEntities.get(2).get("Putative_impact"), "MODERATE");
		assertEquals(resultEntities.get(2).get("Gene_ID"), "GEN2");
		assertEquals(resultEntities.get(2).get("Feature_type"), "transcript");
		assertEquals(resultEntities.get(2).get("Feature_ID"), "NM_123456.7");
		assertEquals(resultEntities.get(2).get("Transcript_biotype"), "Coding");
		assertEquals(resultEntities.get(2).get("Rank_total"), "4/4");
		assertEquals(resultEntities.get(2).get("HGVS_c"), "c.1234C>T");
		assertEquals(resultEntities.get(2).get("HGVS_p"), "p.Thr123Met");
		assertEquals(resultEntities.get(2).get("cDNA_position"), "1234/5678");
		assertEquals(resultEntities.get(2).get("CDS_position"), "2345/6789");
		assertEquals(resultEntities.get(2).get("Protein_position"), "111/222");
		assertEquals(resultEntities.get(2).get("Distance_to_feature"), "");
		assertEquals(resultEntities.get(2).get("Errors"), "");
		assertEquals(resultEntities.get(2).get("VARIANT").toString(), variant2.toString());
	}

	@Configuration
	@Import({ VcfAttributes.class, EffectStructureConverter.class })
	public static class Config
	{

	}
}