package org.molgenis.data.vcf.utils;

import org.testng.annotations.Test;

@Test
public class VcfUtilsTest
{
	//	private static final Logger LOG = LoggerFactory.getLogger(VcfUtilsTest.class);
	//	private final EntityMetaData annotatedEntityMetadata = new EntityMetaDataImpl("test");
	//	public EntityMetaData metaDataCanAnnotate = new EntityMetaDataImpl("test");
	//	public EntityMetaData metaDataCantAnnotate = new EntityMetaDataImpl("test");
	//
	//	public AttributeMetaData attributeMetaDataChrom = new AttributeMetaData(CHROM,
	//			STRING);
	//	public AttributeMetaData attributeMetaDataPos = new AttributeMetaData(POS, LONG);
	//	public AttributeMetaData attributeMetaDataRef = new AttributeMetaData(REF, STRING);
	//	public AttributeMetaData attributeMetaDataAlt = new AttributeMetaData(ALT, STRING);
	//	public AttributeMetaData attributeMetaDataCantAnnotateChrom = new AttributeMetaData(CHROM,
	//			LONG);
	//	public ArrayList<Entity> input = new ArrayList<Entity>();
	//	public Entity entity;
	//	public Entity entity1;
	//	public Entity entity2;
	//	public Entity entity3;
	//	public Entity entity4;
	//
	//	public ArrayList<Entity> entities;
	//
	//	@BeforeMethod
	//	public void beforeMethod() throws IOException
	//	{
	//		/*
	//		 * 1 10050000 test21 G A . PASS AC=21;AN=22;GTC=0,1,10 1 10050001 test22 G A . PASS AC=22;AN=23;GTC=1,2,11 1
	//		 * 10050002 test23 G A . PASS AC=23;AN=24;GTC=2,3,12
	//		 */
	//		metaDataCanAnnotate.addAttribute(attributeMetaDataChrom, ROLE_ID);
	//		metaDataCanAnnotate.addAttribute(attributeMetaDataPos);
	//		metaDataCanAnnotate.addAttribute(attributeMetaDataRef);
	//		metaDataCanAnnotate.addAttribute(attributeMetaDataAlt);
	//
	//		metaDataCantAnnotate.addAttribute(attributeMetaDataCantAnnotateChrom);
	//		metaDataCantAnnotate.addAttribute(attributeMetaDataPos);
	//		metaDataCantAnnotate.addAttribute(attributeMetaDataRef);
	//		metaDataCantAnnotate.addAttribute(attributeMetaDataAlt);
	//
	//		entity = new MapEntity(metaDataCanAnnotate);
	//		entity1 = new MapEntity(metaDataCanAnnotate);
	//		entity2 = new MapEntity(metaDataCanAnnotate);
	//		entity3 = new MapEntity(metaDataCanAnnotate);
	//		entity4 = new MapEntity(metaDataCanAnnotate);
	//
	//		metaDataCanAnnotate.addAttribute(
	//				new AttributeMetaData(ID, STRING));
	//		metaDataCanAnnotate.addAttribute(
	//				new AttributeMetaData(QUAL, STRING));
	//		metaDataCanAnnotate.addAttribute(
	//				new AttributeMetaData(FILTER, STRING));
	//		AttributeMetaData INFO = new AttributeMetaData("INFO", COMPOUND);
	//		AttributeMetaData AC = new AttributeMetaData("AC", STRING);
	//		AttributeMetaData AN = new AttributeMetaData("AN", STRING);
	//		AttributeMetaData GTC = new AttributeMetaData("GTC", STRING);
	//		INFO.addAttributePart(AC);
	//		INFO.addAttributePart(AN);
	//		INFO.addAttributePart(GTC);
	//		metaDataCanAnnotate.addAttribute(INFO);
	//
	//		annotatedEntityMetadata.addAttribute(attributeMetaDataChrom, ROLE_ID);
	//		annotatedEntityMetadata.addAttribute(attributeMetaDataPos);
	//		annotatedEntityMetadata.addAttribute(attributeMetaDataRef);
	//		annotatedEntityMetadata.addAttribute(attributeMetaDataAlt);
	//
	//		annotatedEntityMetadata.addAttribute(
	//				new AttributeMetaData(ID, STRING));
	//		annotatedEntityMetadata.addAttribute(
	//				new AttributeMetaData(QUAL, STRING));
	//		annotatedEntityMetadata.addAttribute(
	//				(new AttributeMetaData(FILTER, STRING))
	//						.setDescription(
	//								"Test that description is not: '" + VcfRepository.DEFAULT_ATTRIBUTE_DESCRIPTION + "'"));
	//		INFO.addAttributePart(new AttributeMetaData("ANNO", STRING));
	//		annotatedEntityMetadata.addAttribute(INFO);
	//
	//		entity1.set(VcfAttributes.CHROM, "1");
	//		entity1.set(VcfAttributes.POS, 10050000);
	//		entity1.set(VcfAttributes.ID, "test21");
	//		entity1.set(VcfAttributes.REF, "G");
	//		entity1.set(VcfAttributes.ALT, "A");
	//		entity1.set(VcfAttributes.QUAL, ".");
	//		entity1.set(VcfAttributes.FILTER, "PASS");
	//		entity1.set("AC", "21");
	//		entity1.set("AN", "22");
	//		entity1.set("GTC", "0,1,10");
	//
	//		entity2.set(VcfAttributes.CHROM, "1");
	//		entity2.set(VcfAttributes.POS, 10050001);
	//		entity2.set(VcfAttributes.ID, "test22");
	//		entity2.set(VcfAttributes.REF, "G");
	//		entity2.set(VcfAttributes.ALT, "A");
	//		entity2.set(VcfAttributes.QUAL, ".");
	//		entity2.set(VcfAttributes.FILTER, "PASS");
	//
	//		entity3.set(VcfAttributes.CHROM, "1");
	//		entity3.set(VcfAttributes.POS, 10050002);
	//		entity3.set(VcfAttributes.ID, "test23");
	//		entity3.set(VcfAttributes.REF, "G");
	//		entity3.set(VcfAttributes.ALT, "A");
	//		entity3.set(VcfAttributes.QUAL, ".");
	//		entity3.set(VcfAttributes.FILTER, "PASS");
	//
	//		entities = new ArrayList<>();
	//		entities.add(entity1);
	//		entities.add(entity2);
	//		entities.add(entity3);
	//	}
	//
	//	// regression test for https://github.com/molgenis/molgenis/issues/3643
	//	@Test
	//	public void convertToVcfInfoGtFirst() throws MolgenisDataException, IOException
	//	{
	//		String formatDpAttrName = "DP";
	//		String formatEcAttrName = "EC";
	//		String formatGtAttrName = VcfAttributes.FORMAT_GT;
	//
	//		String idAttrName = "idAttr";
	//		String sampleIdAttrName = VcfRepository.NAME;
	//
	//		EntityMetaData sampleEntityMeta = new EntityMetaDataImpl("vcfSampleEntity");
	//		sampleEntityMeta.addAttribute(sampleIdAttrName, ROLE_ID);
	//		sampleEntityMeta.addAttribute(formatDpAttrName);
	//		sampleEntityMeta.addAttribute(formatEcAttrName);
	//		sampleEntityMeta.addAttribute(formatGtAttrName);
	//
	//		EntityMetaData entityMeta = new EntityMetaDataImpl("vcfEntity");
	//		entityMeta.addAttribute(idAttrName, ROLE_ID);
	//		entityMeta.addAttribute(CHROM_META);
	//		entityMeta.addAttribute(POS_META);
	//		entityMeta.addAttribute(ID_META);
	//		entityMeta.addAttribute(REF_META);
	//		entityMeta.addAttribute(ALT_META);
	//		entityMeta.addAttribute(QUAL_META);
	//		entityMeta.addAttribute(FILTER_META);
	//		entityMeta.addAttribute(VcfAttributes.INFO).setDataType(MolgenisFieldTypes.COMPOUND);
	//		entityMeta.addAttribute(SAMPLES).setDataType(MolgenisFieldTypes.MREF).setRefEntity(sampleEntityMeta);
	//
	//		Entity sampleEntity = new MapEntity(sampleEntityMeta);
	//		sampleEntity.set(sampleIdAttrName, "0");
	//		sampleEntity.set(formatDpAttrName, "5");
	//		sampleEntity.set(formatEcAttrName, "5");
	//		sampleEntity.set(formatGtAttrName, "1/1");
	//
	//		Entity vcfEntity = new MapEntity(entityMeta);
	//		vcfEntity.set(idAttrName, "0");
	//		vcfEntity.set(CHROM, "1");
	//		vcfEntity.set(POS, "565286");
	//		vcfEntity.set(ID, "rs1578391");
	//		vcfEntity.set(REF, "C");
	//		vcfEntity.set(ALT, "T");
	//		vcfEntity.set(QUAL, null);
	//		vcfEntity.set(FILTER, "flt");
	//		vcfEntity.set(INFO, null);
	//		vcfEntity.set(SAMPLES, Arrays.asList(sampleEntity));
	//		vcfEntity.set(formatDpAttrName, "AB_val");
	//		vcfEntity.set(formatEcAttrName, "AD_val");
	//		vcfEntity.set(formatGtAttrName, "GT_val");
	//
	//		StringWriter strWriter = new StringWriter();
	//		BufferedWriter writer = new BufferedWriter(strWriter);
	//		try
	//		{
	//			VcfUtils.writeToVcf(vcfEntity, writer);
	//		}
	//		finally
	//		{
	//			writer.close();
	//		}
	//		assertEquals(strWriter.toString(), "1	565286	rs1578391	C	T	.	flt	.	GT:DP:EC	1/1:5:5");
	//	}
	//
	//	@Test
	//	public void createId()
	//	{
	//		assertEquals(VcfUtils.createId(entity1), "yCiiynjHRAtJPcdn7jFDGA");
	//	}
	//
	//	@Test
	//	public void vcfWriterRoundtripTest() throws IOException, MolgenisInvalidFormatException
	//	{
	//		final File outputVCFFile = File.createTempFile("output", ".vcf");
	//		try
	//		{
	//			BufferedWriter outputVCFWriter = new BufferedWriter(
	//					new OutputStreamWriter(new FileOutputStream(outputVCFFile), UTF_8));
	//
	//			File inputVcfFile = new File(ResourceUtils.getFile(getClass(), "/testWriter.vcf").getPath());
	//
	//			VcfUtils.checkPreviouslyAnnotatedAndAddMetadata(inputVcfFile, outputVCFWriter, Collections.emptyList());
	//
	//			for (Entity entity : entities)
	//			{
	//				VcfUtils.writeToVcf(entity, outputVCFWriter);
	//				outputVCFWriter.newLine();
	//			}
	//			outputVCFWriter.close();
	//			assertTrue(FileUtils.contentEqualsIgnoreEOL(inputVcfFile, outputVCFFile, "UTF8"));
	//
	//		}
	//		finally
	//		{
	//			boolean outputVCFFileIsDeleted = outputVCFFile.delete();
	//			LOG.info("Result test file named: " + outputVCFFile.getName() + " is "
	//					+ (outputVCFFileIsDeleted ? "" : "not ") + "deleted");
	//		}
	//	}
	//
	//	@Test
	//	public void vcfWriterAnnotateTest() throws IOException, MolgenisInvalidFormatException
	//	{
	//
	//		entity1.set("ANNO", "TEST_test21");
	//		entity2.set("ANNO", "TEST_test22");
	//		final File outputVCFFile = File.createTempFile("output", ".vcf");
	//		try
	//		{
	//			BufferedWriter outputVCFWriter = new BufferedWriter(
	//					new OutputStreamWriter(new FileOutputStream(outputVCFFile), UTF_8));
	//
	//			File inputVcfFile = new File(ResourceUtils.getFile(getClass(), "/testWriter.vcf").getPath());
	//			File resultVCFWriter = new File(ResourceUtils.getFile(getClass(), "/result_vcfWriter.vcf").getPath());
	//
	//			VcfUtils.checkPreviouslyAnnotatedAndAddMetadata(inputVcfFile, outputVCFWriter,
	//					Lists.newArrayList(annotatedEntityMetadata.getAttributes()));
	//
	//			for (Entity entity : entities)
	//			{
	//				MapEntity mapEntity = new MapEntity(entity, annotatedEntityMetadata);
	//				VcfUtils.writeToVcf(mapEntity, outputVCFWriter);
	//				outputVCFWriter.newLine();
	//			}
	//			outputVCFWriter.close();
	//			assertTrue(FileUtils.contentEqualsIgnoreEOL(resultVCFWriter, outputVCFFile, "UTF8"));
	//		}
	//		finally
	//		{
	//			boolean outputVCFFileIsDeleted = outputVCFFile.delete();
	//			LOG.info("Result test file named: " + outputVCFFile.getName() + " is "
	//					+ (outputVCFFileIsDeleted ? "" : "not ") + "deleted");
	//		}
	//	}

}