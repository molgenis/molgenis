package org.molgenis.data.vcf.utils;

import autovalue.shaded.com.google.common.common.collect.Iterators;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisInvalidFormatException;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.data.vcf.datastructures.Sample;
import org.molgenis.data.vcf.datastructures.Trio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static org.molgenis.MolgenisFieldTypes.COMPOUND;
import static org.molgenis.MolgenisFieldTypes.LONG;
import static org.molgenis.MolgenisFieldTypes.MREF;
import static org.molgenis.MolgenisFieldTypes.STRING;
import static org.molgenis.MolgenisFieldTypes.XREF;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.vcf.VcfRepository.ALT;
import static org.molgenis.data.vcf.VcfRepository.CHROM;
import static org.molgenis.data.vcf.VcfRepository.FILTER;
import static org.molgenis.data.vcf.VcfRepository.POS;
import static org.molgenis.data.vcf.VcfRepository.QUAL;
import static org.molgenis.data.vcf.VcfRepository.REF;
import static org.molgenis.data.vcf.VcfRepository.SAMPLES;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

@Test
public class VcfUtilsTest
{
	private static final Logger LOG = LoggerFactory.getLogger(VcfWriterUtilsIntegrationTest.class);
	public static final String ID = "ID";
	public static final String PUTATIVE_IMPACT = "Putative_impact";
	public static final String TYPE = "TYPE";
	public static final String VARIANT = "VARIANT";
	public static final String EFFECT = "EFFECT";
	public static final String GENES = "GENES";
	public static final String GTC = "GTC";
	public static final String AN = "AN";
	public static final String AC = "AC";
	private static DefaultAttributeMetaData PUTATIVE_IMPACT_ATTR;
	private static DefaultAttributeMetaData EFFECT_ATTR;
	private static DefaultAttributeMetaData GENES_ATTR;
	private final DefaultEntityMetaData annotatedEntityMetadata = new DefaultEntityMetaData("test");
	public DefaultEntityMetaData metaDataCanAnnotate = new DefaultEntityMetaData("test");
	public DefaultEntityMetaData metaDataCantAnnotate = new DefaultEntityMetaData("test");
	public DefaultEntityMetaData geneMeta = new DefaultEntityMetaData(GENES);

	DefaultEntityMetaData effectMeta = new DefaultEntityMetaData(EFFECT);
	DefaultEntityMetaData vcfMeta = new DefaultEntityMetaData("vcfMeta");
	DefaultEntityMetaData sampleEntityMeta = new DefaultEntityMetaData("vcfSampleEntity");
	public AttributeMetaData attributeMetaDataChrom = new DefaultAttributeMetaData(CHROM,
			MolgenisFieldTypes.FieldTypeEnum.STRING);

	public AttributeMetaData attributeMetaDataPos = new DefaultAttributeMetaData(POS,
			MolgenisFieldTypes.FieldTypeEnum.LONG);
	public AttributeMetaData attributeMetaDataRef = new DefaultAttributeMetaData(REF,
			MolgenisFieldTypes.FieldTypeEnum.STRING);
	public AttributeMetaData attributeMetaDataAlt = new DefaultAttributeMetaData(ALT,
			MolgenisFieldTypes.FieldTypeEnum.STRING);
	public AttributeMetaData attributeMetaDataCantAnnotateChrom = new DefaultAttributeMetaData(CHROM,
			MolgenisFieldTypes.FieldTypeEnum.LONG);
	public Entity entity;
	public Entity entity1;
	public Entity entity2;
	public Entity entity3;
	public Entity entity4;
	public ArrayList<Entity> entities;

	@BeforeMethod
	public void beforeMethod() throws IOException
	{
		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataPos);
		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataChrom, ROLE_ID);

		metaDataCantAnnotate.addAttributeMetaData(attributeMetaDataCantAnnotateChrom);
		metaDataCantAnnotate.addAttributeMetaData(attributeMetaDataPos);
		metaDataCantAnnotate.addAttributeMetaData(attributeMetaDataRef);
		metaDataCantAnnotate.addAttributeMetaData(attributeMetaDataAlt);

		entity = new MapEntity(metaDataCanAnnotate);
		entity1 = new MapEntity(metaDataCanAnnotate);
		entity2 = new MapEntity(metaDataCanAnnotate);
		entity3 = new MapEntity(metaDataCanAnnotate);
		entity4 = new MapEntity(metaDataCanAnnotate);

		metaDataCanAnnotate.addAttributeMetaData(
				new DefaultAttributeMetaData(VcfRepository.ID, MolgenisFieldTypes.FieldTypeEnum.STRING));
		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataRef);
		metaDataCanAnnotate.addAttributeMetaData(attributeMetaDataAlt);
		metaDataCanAnnotate.addAttributeMetaData(
				new DefaultAttributeMetaData(VcfRepository.QUAL, MolgenisFieldTypes.FieldTypeEnum.STRING));
		metaDataCanAnnotate.addAttributeMetaData(
				new DefaultAttributeMetaData(VcfRepository.FILTER, MolgenisFieldTypes.FieldTypeEnum.STRING));
		DefaultAttributeMetaData INFO = new DefaultAttributeMetaData(VcfRepository.INFO,
				MolgenisFieldTypes.FieldTypeEnum.COMPOUND);
		DefaultAttributeMetaData AC = new DefaultAttributeMetaData(VcfWriterUtilsIntegrationTest.AC,
				MolgenisFieldTypes.FieldTypeEnum.STRING);
		DefaultAttributeMetaData AN = new DefaultAttributeMetaData(VcfWriterUtilsIntegrationTest.AN,
				MolgenisFieldTypes.FieldTypeEnum.STRING);
		DefaultAttributeMetaData GTC = new DefaultAttributeMetaData(VcfWriterUtilsIntegrationTest.GTC,
				MolgenisFieldTypes.FieldTypeEnum.STRING);
		INFO.addAttributePart(AC);
		INFO.addAttributePart(AN);
		INFO.addAttributePart(GTC);
		metaDataCanAnnotate.addAttributeMetaData(INFO);

		annotatedEntityMetadata.addAttributeMetaData(attributeMetaDataChrom, ROLE_ID);
		annotatedEntityMetadata.addAttributeMetaData(attributeMetaDataPos);
		annotatedEntityMetadata.addAttributeMetaData(
				new DefaultAttributeMetaData(VcfRepository.ID, MolgenisFieldTypes.FieldTypeEnum.STRING));
		annotatedEntityMetadata.addAttributeMetaData(attributeMetaDataRef);
		annotatedEntityMetadata.addAttributeMetaData(attributeMetaDataAlt);

		annotatedEntityMetadata.addAttributeMetaData(
				new DefaultAttributeMetaData(VcfRepository.QUAL, MolgenisFieldTypes.FieldTypeEnum.STRING));
		annotatedEntityMetadata.addAttributeMetaData(
				(new DefaultAttributeMetaData(VcfRepository.FILTER, MolgenisFieldTypes.FieldTypeEnum.STRING))
						.setDescription(
								"Test that description is not: '" + VcfRepository.DEFAULT_ATTRIBUTE_DESCRIPTION + "'"));
		INFO.addAttributePart(new DefaultAttributeMetaData("ANNO", MolgenisFieldTypes.FieldTypeEnum.STRING));
		annotatedEntityMetadata.addAttributeMetaData(INFO);

		entity1.set(VcfRepository.CHROM, "1");
		entity1.set(VcfRepository.POS, 10050000);
		entity1.set(VcfRepository.ID, "test21");
		entity1.set(VcfRepository.REF, "G");
		entity1.set(VcfRepository.ALT, "A");
		entity1.set(VcfRepository.QUAL, ".");
		entity1.set(VcfRepository.FILTER, "PASS");
		entity1.set(VcfWriterUtilsIntegrationTest.AC, "21");
		entity1.set(VcfWriterUtilsIntegrationTest.AN, "22");
		entity1.set(VcfWriterUtilsIntegrationTest.GTC, "0,1,10");

		entity2.set(VcfRepository.CHROM, "1");
		entity2.set(VcfRepository.POS, 10050001);
		entity2.set(VcfRepository.ID, "test22");
		entity2.set(VcfRepository.REF, "G");
		entity2.set(VcfRepository.ALT, "A");
		entity2.set(VcfRepository.QUAL, ".");
		entity2.set(VcfRepository.FILTER, "PASS");

		entity3.set(VcfRepository.CHROM, "1");
		entity3.set(VcfRepository.POS, 10050002);
		entity3.set(VcfRepository.ID, "test23");
		entity3.set(VcfRepository.REF, "G");
		entity3.set(VcfRepository.ALT, "A");
		entity3.set(VcfRepository.QUAL, ".");
		entity3.set(VcfRepository.FILTER, "PASS");

		entities = new ArrayList<>();
		entities.add(entity1);
		entities.add(entity2);
		entities.add(entity3);

		geneMeta.addAttribute("id", ROLE_ID).setDataType(STRING).setDescription("Random generated ID")
				.setVisible(false);
		geneMeta.addAttribute("Gene").setDataType(STRING).setDescription("HGNC symbol");
		effectMeta.addAttribute("id", ROLE_ID).setDataType(STRING).setDescription("effect identifier")
				.setVisible(false);
		effectMeta.addAttribute(ALT).setDataType(STRING).setDescription("Alternative allele");
		effectMeta.addAttribute("ALT_GENE").setDataType(STRING).setDescription("Alternative allele and gene");
		effectMeta.addAttribute("GENE").setDataType(STRING).setDescription("Gene identifier (HGNC symbol)");
		effectMeta.addAttribute(PUTATIVE_IMPACT).setDataType(STRING).setDescription("Level of effect on the gene");
		effectMeta.addAttribute(TYPE).setDataType(STRING).setDescription("Type of mutation");

		PUTATIVE_IMPACT_ATTR = new DefaultAttributeMetaData(PUTATIVE_IMPACT, MolgenisFieldTypes.FieldTypeEnum.STRING);
		EFFECT_ATTR = new DefaultAttributeMetaData(EFFECT, MolgenisFieldTypes.FieldTypeEnum.MREF)
				.setRefEntity(effectMeta);
		GENES_ATTR = new DefaultAttributeMetaData(GENES, MolgenisFieldTypes.FieldTypeEnum.MREF).setRefEntity(geneMeta);

		String formatDpAttrName = "DP";
		String formatEcAttrName = "EC";
		String formatGtAttrName = VcfRepository.FORMAT_GT;
		String sampleIdAttrName = VcfRepository.NAME;

		sampleEntityMeta.addAttribute(sampleIdAttrName, ROLE_ID);
		sampleEntityMeta.addAttribute(formatDpAttrName);
		sampleEntityMeta.addAttribute(formatEcAttrName);
		sampleEntityMeta.addAttribute(formatGtAttrName);

		vcfMeta.addAttribute(CHROM, ROLE_ID).setDataType(STRING);
		vcfMeta.addAttribute(POS).setDataType(LONG);
		vcfMeta.addAttribute(VcfRepository.ID).setDataType(STRING);
		vcfMeta.addAttribute(REF).setDataType(STRING);
		vcfMeta.addAttribute(ALT).setDataType(STRING);
		vcfMeta.addAttribute(FILTER).setDataType(COMPOUND);
		vcfMeta.addAttribute(QUAL).setDataType(STRING);

		INFO.addAttributePart(AC);
		INFO.addAttributePart(AN);
		INFO.addAttributePart(GTC);

		vcfMeta.addAttributeMetaData(INFO);
		vcfMeta.addAttribute(EFFECT).setDataType(MREF).setRefEntity(effectMeta);
		vcfMeta.addAttribute(GENES).setDataType(MREF).setRefEntity(geneMeta);
		vcfMeta.addAttribute(SAMPLES).setDataType(MolgenisFieldTypes.MREF).setRefEntity(sampleEntityMeta);
	}

	@Test
	public void reverseXrefMrefRelationTest() throws IOException

	{
		DefaultEntityMetaData variantEntityMetaData = new DefaultEntityMetaData(VARIANT);
		variantEntityMetaData.addAttribute(VcfWriterUtilsIntegrationTest.ID, ROLE_ID);
		variantEntityMetaData.addAttribute("ALL_OTHER_VCF_STUFF");

		Entity variantEntity1 = new MapEntity(variantEntityMetaData);
		variantEntity1.set(VcfWriterUtilsIntegrationTest.ID, "variant1");
		variantEntity1.set("ALL_OTHER_VCF_STUFF", "chrom pos ref alt");

		Entity variantEntity2 = new MapEntity(variantEntityMetaData);
		variantEntity2.set(VcfWriterUtilsIntegrationTest.ID, "variant2");
		variantEntity2.set("ALL_OTHER_VCF_STUFF", "chrom pos ref alt");

		DefaultEntityMetaData annotatedEntityMetaData = new DefaultEntityMetaData(EFFECT);
		annotatedEntityMetaData.addAttribute(VcfWriterUtilsIntegrationTest.ID, ROLE_ID);
		annotatedEntityMetaData.addAttributeMetaData(PUTATIVE_IMPACT_ATTR);
		annotatedEntityMetaData.addAttribute(TYPE).setDataType(STRING);
		annotatedEntityMetaData.addAttribute(VARIANT).setDataType(XREF).setRefEntity(variantEntityMetaData);

		Entity effectEntity1 = new MapEntity(annotatedEntityMetaData);
		effectEntity1.set(ID, 1);
		effectEntity1.set(PUTATIVE_IMPACT, "HIGH");
		effectEntity1.set(TYPE, "MISSENSE");
		effectEntity1.set(VARIANT, variantEntity1);

		Entity effectEntity2 = new MapEntity(annotatedEntityMetaData);
		effectEntity2.set(VcfWriterUtilsIntegrationTest.ID, 2);
		effectEntity2.set(PUTATIVE_IMPACT, "LOW");
		effectEntity2.set(TYPE, "SYNONYMOUS");
		effectEntity2.set(VARIANT, variantEntity2);

		Entity effectEntity3 = new MapEntity(annotatedEntityMetaData);
		effectEntity3.set(VcfWriterUtilsIntegrationTest.ID, 3);
		effectEntity3.set(PUTATIVE_IMPACT, "LOW");
		effectEntity3.set(TYPE, "SYNONYMOUS");
		effectEntity3.set(VARIANT, variantEntity2);

		DefaultEntityMetaData expectedEntityMetaData = new DefaultEntityMetaData(variantEntityMetaData);
		expectedEntityMetaData.addAttribute(PUTATIVE_IMPACT).setDataType(MREF);

		Entity expectedEntity1 = new MapEntity(expectedEntityMetaData);
		expectedEntity1.set(variantEntity1);
		expectedEntity1.set(EFFECT, newArrayList(effectEntity1));

		Entity expectedEntity2 = new MapEntity(expectedEntityMetaData);
		expectedEntity2.set(variantEntity2);
		expectedEntity2.set(EFFECT, newArrayList(effectEntity2, effectEntity3));

		Iterator<Entity> effectRecords = newArrayList(effectEntity1, effectEntity2, effectEntity3).iterator();
		Iterator<Entity> expectedEntities = newArrayList(expectedEntity1, expectedEntity2).iterator();

		Iterator<Entity> resultIterator = VcfUtils.reverseXrefMrefRelation(effectRecords);

		assertTrue(Arrays.equals(Iterators.toArray(resultIterator, Entity.class),
				Iterators.toArray(expectedEntities, Entity.class)));
	}

	@Test
	public void createId()
	{
		assertEquals(VcfUtils.createId(entity1), "yCiiynjHRAtJPcdn7jFDGA");
	}

	@Test
	public void toVcfDataType()
	{
		assertEquals(VcfUtils.toVcfDataType(MolgenisFieldTypes.FieldTypeEnum.BOOL), "Flag");
		assertEquals(VcfUtils.toVcfDataType(MolgenisFieldTypes.FieldTypeEnum.LONG), "Float");
		assertEquals(VcfUtils.toVcfDataType(MolgenisFieldTypes.FieldTypeEnum.DECIMAL), "Float");
		assertEquals(VcfUtils.toVcfDataType(MolgenisFieldTypes.FieldTypeEnum.INT), "Integer");
		assertEquals(VcfUtils.toVcfDataType(MolgenisFieldTypes.FieldTypeEnum.EMAIL), "String");
		assertEquals(VcfUtils.toVcfDataType(MolgenisFieldTypes.FieldTypeEnum.ENUM), "String");
		assertEquals(VcfUtils.toVcfDataType(MolgenisFieldTypes.FieldTypeEnum.HTML), "String");
		assertEquals(VcfUtils.toVcfDataType(MolgenisFieldTypes.FieldTypeEnum.HYPERLINK), "String");
		assertEquals(VcfUtils.toVcfDataType(MolgenisFieldTypes.FieldTypeEnum.STRING), "String");
		assertEquals(VcfUtils.toVcfDataType(MolgenisFieldTypes.FieldTypeEnum.TEXT), "String");
		assertEquals(VcfUtils.toVcfDataType(MolgenisFieldTypes.FieldTypeEnum.DATE), "String");
		assertEquals(VcfUtils.toVcfDataType(MolgenisFieldTypes.FieldTypeEnum.DATE_TIME), "String");
		assertEquals(VcfUtils.toVcfDataType(MolgenisFieldTypes.FieldTypeEnum.CATEGORICAL), "String");
		assertEquals(VcfUtils.toVcfDataType(MolgenisFieldTypes.FieldTypeEnum.XREF), "String");
		assertEquals(VcfUtils.toVcfDataType(MolgenisFieldTypes.FieldTypeEnum.CATEGORICAL_MREF), "String");
		assertEquals(VcfUtils.toVcfDataType(MolgenisFieldTypes.FieldTypeEnum.MREF), "String");
		assertEquals(VcfUtils.toVcfDataType(MolgenisFieldTypes.FieldTypeEnum.XREF), "String");

		try
		{
			VcfUtils.toVcfDataType(MolgenisFieldTypes.FieldTypeEnum.FILE);
			fail();
		}
		catch (RuntimeException e)
		{
		}
		try
		{
			VcfUtils.toVcfDataType(MolgenisFieldTypes.FieldTypeEnum.COMPOUND);
			fail();
		}
		catch (RuntimeException e)
		{
		}
	}

	@Test
	public void getAtomicAttributesFromList()
	{
		DefaultAttributeMetaData compound = new DefaultAttributeMetaData("Compound",
				MolgenisFieldTypes.FieldTypeEnum.COMPOUND);
		DefaultAttributeMetaData part1 = new DefaultAttributeMetaData("part1", MolgenisFieldTypes.FieldTypeEnum.STRING);
		DefaultAttributeMetaData part2 = new DefaultAttributeMetaData("part2", MolgenisFieldTypes.FieldTypeEnum.STRING);
		compound.addAttributePart(part1);
		compound.addAttributePart(part2);
		DefaultAttributeMetaData attr2 = new DefaultAttributeMetaData("attr2", MolgenisFieldTypes.FieldTypeEnum.STRING);
		List<AttributeMetaData> inputList = Arrays.asList(compound, attr2);
		assertTrue(VcfUtils.getAtomicAttributesFromList(inputList).containsAll(Arrays.asList(part1, part2, attr2)));
		assertFalse(VcfUtils.getAtomicAttributesFromList(inputList).containsAll(Arrays.asList(compound)));
	}

	@Test
	public void createEntityStructureForVcf()
	{
		DefaultAttributeMetaData attr1 = new DefaultAttributeMetaData("variantattr1",
				MolgenisFieldTypes.FieldTypeEnum.STRING);
		DefaultAttributeMetaData attr_compound = new DefaultAttributeMetaData(VcfRepository.INFO,
				MolgenisFieldTypes.FieldTypeEnum.COMPOUND);
		DefaultAttributeMetaData attr2 = new DefaultAttributeMetaData("variantattr2",
				MolgenisFieldTypes.FieldTypeEnum.STRING);
		DefaultAttributeMetaData attr3 = new DefaultAttributeMetaData("varianteffect",
				MolgenisFieldTypes.FieldTypeEnum.STRING).setDescription("EFFECT annotations: 'a|b|c'");

		DefaultEntityMetaData inputEntityMeta = new DefaultEntityMetaData("input");
		inputEntityMeta.addAttributeMetaData(attr1, ROLE_ID);
		attr_compound.addAttributePart(attr2);
		attr_compound.addAttributePart(attr3);
		inputEntityMeta.addAttributeMetaData(attr_compound);

		MapEntity inputEntity = new MapEntity(inputEntityMeta);
		inputEntity.set(attr1.getName(), "ID1");
		inputEntity.set(attr2.getName(), "ATTR2");
		inputEntity.set(attr3.getName(), "AAP|NOOT|MIES");

		List<Entity> results = VcfUtils.createEntityStructureForVcf(inputEntityMeta, "varianteffect",
				Stream.of(inputEntity));
		Entity result = results.get(0);
		assertEquals(result.get("a"), "AAP");
		assertEquals(result.get("b"), "NOOT");
		assertEquals(result.get("c"), "MIES");
		assertEquals(result.getEntity("Variant").getIdValue().toString(), "ID1");
		assertEquals(result.getEntity("Variant").get("variantattr2"), "ATTR2");
	}

	@Test
	public void createEntityStructureForVcfTyped()
	{
		DefaultAttributeMetaData attr1 = new DefaultAttributeMetaData("variantattr1",
				MolgenisFieldTypes.FieldTypeEnum.STRING);
		DefaultAttributeMetaData attr_compound = new DefaultAttributeMetaData(VcfRepository.INFO,
				MolgenisFieldTypes.FieldTypeEnum.COMPOUND);
		DefaultAttributeMetaData attr2 = new DefaultAttributeMetaData("variantattr2",
				MolgenisFieldTypes.FieldTypeEnum.STRING);
		DefaultAttributeMetaData attr3 = new DefaultAttributeMetaData("varianteffect",
				MolgenisFieldTypes.FieldTypeEnum.STRING).setDescription("EFFECT annotations: 'a|b|c'");
		DefaultAttributeMetaData attr4 = new DefaultAttributeMetaData("a", MolgenisFieldTypes.FieldTypeEnum.STRING);
		DefaultAttributeMetaData attr5 = new DefaultAttributeMetaData("b", MolgenisFieldTypes.FieldTypeEnum.DECIMAL);
		DefaultAttributeMetaData attr6 = new DefaultAttributeMetaData("c", MolgenisFieldTypes.FieldTypeEnum.TEXT);
		DefaultEntityMetaData inputEntityMeta = new DefaultEntityMetaData("input");
		inputEntityMeta.addAttributeMetaData(attr1, ROLE_ID);
		attr_compound.addAttributePart(attr2);
		attr_compound.addAttributePart(attr3);
		inputEntityMeta.addAttributeMetaData(attr_compound);

		MapEntity inputEntity = new MapEntity(inputEntityMeta);
		inputEntity.set(attr1.getName(), "ID1");
		inputEntity.set(attr2.getName(), "ATTR2");
		inputEntity.set(attr3.getName(), "AAP|2|MIES");

		List<Entity> results = VcfUtils.createEntityStructureForVcf(inputEntityMeta, "varianteffect",
				Stream.of(inputEntity), Arrays.asList(attr4, attr5, attr6));
		Entity result = results.get(0);
		assertEquals(result.get("a"), "AAP");
		assertEquals(result.getEntityMetaData().getAttribute("a").getDataType(),
				MolgenisFieldTypes.STRING);
		assertEquals(result.get("b"), "2");
		assertEquals(result.getEntityMetaData().getAttribute("b").getDataType(),
				MolgenisFieldTypes.DECIMAL);
		assertEquals(result.get("c"), "MIES");
		assertEquals(result.getEntityMetaData().getAttribute("c").getDataType(), MolgenisFieldTypes.TEXT);
		assertEquals(result.getEntity("Variant").getIdValue().toString(), "ID1");
		assertEquals(result.getEntity("Variant").get("variantattr2"), "ATTR2");
	}

	@Test
	public void vcfPedigreeReaderTest() throws IOException, MolgenisInvalidFormatException
	{
		String testHeader = "##PEDIGREE=<Child=27991,Mother=27992,Father=27993>\n"
				+ "##PEDIGREE=<Child=27939,Mother=27940,Father=27941>\n"
				+ "##PEDIGREE=<Child=30982,Mother=30983,Father=30984>";

		Scanner scanner = new Scanner(testHeader);

		HashMap<String, Trio> actualPedigree = VcfUtils.getPedigree(scanner);

		HashMap<String, Trio> expectedPedigree = new HashMap<String, Trio>();
		expectedPedigree.put("27991", new Trio(new Sample("27991"), new Sample("27992"), new Sample("27993")));
		expectedPedigree.put("27939", new Trio(new Sample("27939"), new Sample("27940"), new Sample("27941")));
		expectedPedigree.put("30982", new Trio(new Sample("30982"), new Sample("30983"), new Sample("30984")));

		assertEquals(actualPedigree, expectedPedigree);

	}
}
