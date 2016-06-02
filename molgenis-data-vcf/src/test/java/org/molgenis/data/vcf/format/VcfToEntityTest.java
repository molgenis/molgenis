package org.molgenis.data.vcf.format;

public class VcfToEntityTest
{
	//	private VcfToEntity vcfToEntitySmall;
	//	private VcfMeta vcfMetaSmall;
	//
	//	private static VcfMeta parseHeaders(String headers) throws IOException
	//	{
	//		VcfReader vcfReader;
	//		vcfReader = new VcfReader(new StringReader(headers));
	//		VcfMeta result = vcfReader.getVcfMeta();
	//		vcfReader.close();
	//		return result;
	//	}
	//
	//	@BeforeTest
	//	public void beforeTest() throws IOException
	//	{
	//		String headersSmall = "##fileformat=VCFv4.1\n" + "##fileDate=2012/11/05\n" + "##source=NextGENeV2.2\n"
	//				+ "##reference=C:\\Program_Files_(x86)\\SoftGenetics\\NextGENe\\References\\Human_v37_2_dna\n"
	//				+ "##contig=<ID=1,length=249240621>\n"
	//				+ "##INFO=<ID=NS,Number=1,Type=Integer,Description=\"Number of Samples With Data\">\n"
	//				+ "##INFO=<ID=DF,Number=0,Type=Flag,Description=\"Flag field\">\n"
	//				+ "##INFO=<ID=DF2,Number=0,Type=Flag,Description=\"Flag field 2\">\n"
	//				+ "#CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO\n";
	//		vcfMetaSmall = parseHeaders(headersSmall);
	//		vcfToEntitySmall = new VcfToEntity("EntityNameSmall", vcfMetaSmall);
	//	}
	//
	//	@Test
	//	public void testGetEntityMetaData()
	//	{
	//		EntityMetaData expected = new EntityMetaDataImpl("EntityName");
	//		expected.addAttributes(
	//				Arrays.asList(CHROM_META, ALT_META, POS_META, REF_META, FILTER_META, QUAL_META, ID_META));
	//		AttributeMetaData internalIdMeta = new AttributeMetaData(INTERNAL_ID, STRING);
	//		internalIdMeta.setVisible(false);
	//		expected.addAttribute(internalIdMeta, ROLE_ID);
	//
	//		AttributeMetaData infoMetaData = new AttributeMetaData(INFO, COMPOUND).setNillable(true);
	//
	//		AttributeMetaData infoNS = new AttributeMetaData("NS", INT)
	//				.setNillable(true).setDescription("Number of Samples With Data");
	//		infoMetaData.addAttributePart(infoNS);
	//		AttributeMetaData infoDF = new AttributeMetaData("DF", BOOL)
	//				.setNillable(false).setDescription("Flag field");
	//		infoMetaData.addAttributePart(infoDF);
	//		AttributeMetaData infoDF2 = new AttributeMetaData("DF2", BOOL)
	//				.setNillable(false).setDescription("Flag field 2");
	//		infoMetaData.addAttributePart(infoDF2);
	//
	//		expected.addAttribute(infoMetaData);
	//
	//		EntityMetaData actualEntityMetaData = vcfToEntitySmall.getEntityMetaData();
	//
	//		ArrayList<AttributeMetaData> actualAttributes = Lists.newArrayList(actualEntityMetaData.getAtomicAttributes());
	//		ArrayList<AttributeMetaData> expectedAttributes = Lists.newArrayList(expected.getAtomicAttributes());
	//		assertEquals(actualAttributes, expectedAttributes);
	//	}
	//
	//	@Test
	//	public void testToEntity() throws IOException
	//	{
	//		VcfRecord record = new VcfRecord(vcfMetaSmall, new String[]
	//		{ "10", "12345", "id3", "A", "C", "7.9123", "pass", "DF" });
	//		Entity entity = vcfToEntitySmall.toEntity(record);
	//		Entity expected = new MapEntity(vcfToEntitySmall.getEntityMetaData());
	//		expected.set("#CHROM", "10");
	//		expected.set("ALT", "C");
	//		expected.set("POS", 12345);
	//		expected.set("REF", "A");
	//		expected.set("FILTER", "pass");
	//		expected.set("QUAL", "7.9123");
	//		expected.set("ID", "id3");
	//		expected.set("INTERNAL_ID", entity.get("INTERNAL_ID"));
	//		expected.set("DF", true);
	//		// Flag fields whose flag is not present are set to false
	//		expected.set("DF2", false);
	//		assertEquals(entity, expected);
	//	}
	//
	//	@Test
	//	public void testToEntityAlternativeAlleles() throws IOException
	//	{
	//		VcfRecord record = new VcfRecord(vcfMetaSmall, new String[]
	//		{ "10", "12345", "id3", "A", "A,C,G,T,N,*", "7.9123", "pass", "DF;DF2" });
	//		Entity entity = vcfToEntitySmall.toEntity(record);
	//		Entity expected = new MapEntity(vcfToEntitySmall.getEntityMetaData());
	//		expected.set("#CHROM", "10");
	//		expected.set("ALT", "A,C,G,T,N,*");
	//		expected.set("POS", 12345);
	//		expected.set("REF", "A");
	//		expected.set("FILTER", "pass");
	//		expected.set("QUAL", "7.9123");
	//		expected.set("ID", "id3");
	//		expected.set("INTERNAL_ID", entity.get("INTERNAL_ID"));
	//		expected.set("DF", true);
	//		expected.set("DF2", true);
	//		assertEquals(entity, expected);
	//	}
}
