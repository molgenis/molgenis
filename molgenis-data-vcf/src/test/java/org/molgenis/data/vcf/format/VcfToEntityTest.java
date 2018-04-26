package org.molgenis.data.vcf.format;

import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.*;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.util.EntityUtils;
import org.molgenis.data.vcf.config.VcfTestConfig;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.molgenis.vcf.VcfReader;
import org.molgenis.vcf.VcfRecord;
import org.molgenis.vcf.meta.VcfMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.StringReader;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.vcf.model.VcfAttributes.INFO;
import static org.molgenis.data.vcf.model.VcfAttributes.INTERNAL_ID;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@ContextConfiguration(classes = { VcfToEntityTest.Config.class })
public class VcfToEntityTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private VcfAttributes vcfAttrs;

	@Autowired
	private EntityTypeFactory entityTypeFactory;

	@Autowired
	private AttributeFactory attrMetaFactory;

	private VcfToEntity vcfToEntitySmall;
	private VcfMeta vcfMetaSmall;

	private static VcfMeta parseHeaders(String headers) throws IOException
	{
		try (VcfReader vcfReader = new VcfReader(new StringReader(headers)))
		{
			return vcfReader.getVcfMeta();
		}
	}

	@BeforeMethod
	public void beforeTest() throws IOException
	{
		String headersSmall = "##fileformat=VCFv4.1\n" + "##fileDate=2012/11/05\n" + "##source=NextGENeV2.2\n"
				+ "##reference=C:\\Program_Files_(x86)\\SoftGenetics\\NextGENe\\References\\Human_v37_2_dna\n"
				+ "##contig=<ID=1,length=249240621>\n"
				+ "##INFO=<ID=NS,Number=1,Type=Integer,Description=\"Number of Samples With Data\">\n"
				+ "##INFO=<ID=DF,Number=0,Type=Flag,Description=\"Flag field\">\n"
				+ "##INFO=<ID=DF2,Number=0,Type=Flag,Description=\"Flag field 2\">\n"
				+ "##INFO=<ID=CHAR,Number=0,Type=Character,Description=\"char field\">\n"
				+ "#CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO\n";
		vcfMetaSmall = parseHeaders(headersSmall);
		vcfToEntitySmall = new VcfToEntity("EntityNameSmall", vcfMetaSmall, vcfAttrs, entityTypeFactory,
				attrMetaFactory);
	}

	@Test
	public void testGetEntityType()
	{
		EntityType expectedEntityType = entityTypeFactory.create("EntityNameSmall");
		expectedEntityType.addAttribute(vcfAttrs.getChromAttribute());
		expectedEntityType.addAttribute(vcfAttrs.getAltAttribute());
		expectedEntityType.addAttribute(vcfAttrs.getPosAttribute());
		expectedEntityType.addAttribute(vcfAttrs.getRefAttribute());
		expectedEntityType.addAttribute(vcfAttrs.getFilterAttribute());
		expectedEntityType.addAttribute(vcfAttrs.getQualAttribute());
		expectedEntityType.addAttribute(vcfAttrs.getIdAttribute());

		Attribute internalIdMeta = attrMetaFactory.create().setName(INTERNAL_ID).setDataType(STRING).setVisible(false);
		expectedEntityType.addAttribute(internalIdMeta, ROLE_ID);

		Attribute infoMetaData = attrMetaFactory.create().setName(INFO).setDataType(COMPOUND).setNillable(true);

		Attribute infoNS = attrMetaFactory.create()
										  .setName("NS")
										  .setDataType(INT)
										  .setDescription("Number of Samples With Data")
										  .setAggregatable(true)
										  .setParent(infoMetaData);
		Attribute infoDF = attrMetaFactory.create()
										  .setName("DF")
										  .setDataType(BOOL)
										  .setDescription("Flag field")
										  .setAggregatable(true)
										  .setParent(infoMetaData);
		Attribute infoDF2 = attrMetaFactory.create()
										   .setName("DF2")
										   .setDataType(BOOL)
										   .setDescription("Flag field 2")
										   .setAggregatable(true)
										   .setParent(infoMetaData);
		Attribute infoChar2 = attrMetaFactory.create()
											 .setName("CHAR")
											 .setDataType(STRING)
											 .setDescription("char field")
											 .setAggregatable(true)
											 .setParent(infoMetaData);

		expectedEntityType.addAttribute(infoNS);
		expectedEntityType.addAttribute(infoDF);
		expectedEntityType.addAttribute(infoDF2);
		expectedEntityType.addAttribute(infoChar2);
		expectedEntityType.addAttribute(infoMetaData);

		EntityType actualEntityType = vcfToEntitySmall.getEntityType();
		String backend = "test";
		expectedEntityType.setBackend(backend);
		actualEntityType.setBackend(backend);
		Package package_ = mock(Package.class);
		when(package_.getIdValue()).thenReturn("pck0");
		expectedEntityType.setPackage(package_);
		actualEntityType.setPackage(package_);

		expectedEntityType.setId("dummyId");
		expectedEntityType.setLabel("EntityNameSmall");
		expectedEntityType.getOwnAllAttributes().forEach(attr -> attr.setIdentifier(null));
		actualEntityType.setId("dummyId");
		actualEntityType.getOwnAllAttributes().forEach(attr -> attr.setIdentifier(null));

		assertTrue(EntityUtils.equals(expectedEntityType, actualEntityType));
	}

	@Test
	public void testToEntity() throws IOException
	{
		VcfRecord record = new VcfRecord(vcfMetaSmall,
				new String[] { "10", "12345", "id3", "A", "C", "7.9123", "pass", "DF;;CHAR=-" });
		Entity entity = vcfToEntitySmall.toEntity(record);
		Entity expected = new DynamicEntity(vcfToEntitySmall.getEntityType());
		expected.set("#CHROM", "10");
		expected.set("ALT", "C");
		expected.set("POS", 12345);
		expected.set("REF", "A");
		expected.set("FILTER", "pass");
		expected.set("QUAL", "7.9123");
		expected.set("ID", "id3");
		expected.set("INTERNAL_ID", entity.get("INTERNAL_ID"));
		expected.set("DF", true);
		// Flag fields whose flag is not present are set to false
		expected.set("DF2", false);
		expected.set("CHAR", "-");
		assertTrue(EntityUtils.equals(entity, expected));
	}

	@Test
	public void testToEntityAlternativeAlleles() throws IOException
	{
		VcfRecord record = new VcfRecord(vcfMetaSmall,
				new String[] { "10", "12345", "id3", "A", "A,C,G,T,N,*", "7.9123", "pass", "DF;DF2;CHAR=-" });
		Entity entity = vcfToEntitySmall.toEntity(record);
		Entity expected = new DynamicEntity(vcfToEntitySmall.getEntityType());
		expected.set("#CHROM", "10");
		expected.set("ALT", "A,C,G,T,N,*");
		expected.set("POS", 12345);
		expected.set("REF", "A");
		expected.set("FILTER", "pass");
		expected.set("QUAL", "7.9123");
		expected.set("ID", "id3");
		expected.set("INTERNAL_ID", entity.get("INTERNAL_ID"));
		expected.set("DF", true);
		expected.set("DF2", true);
		expected.set("CHAR", "-");
		assertTrue(EntityUtils.equals(entity, expected));
	}

	// https://github.com/molgenis/molgenis/issues/5329
	@Test
	public void testToEntityMissingValues() throws IOException
	{
		String vcfHeaders = "##fileformat=VCFv4.1\n"
				+ "##INFO=<ID=GoNL_AF,Number=.,Type=Float,Description=\"The allele frequency for variants seen in the population used for the GoNL project\">\n"
				+ "#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO\n";
		VcfMeta vcfMeta = parseHeaders(vcfHeaders);
		VcfToEntity vcfToEntity = new VcfToEntity("entityTypeName", vcfMeta, vcfAttrs, entityTypeFactory,
				attrMetaFactory);
		VcfRecord record = new VcfRecord(vcfMeta,
				new String[] { "1", "54728", ".", "G", "T,C", ".", ".", "GoNL_AF=0.01,." });
		Entity entity = vcfToEntity.toEntity(record);
		assertEquals(entity.getString("GoNL_AF"), "0.01,.");
	}

	@Configuration
	@Import({ VcfTestConfig.class })
	public static class Config
	{

	}
}
