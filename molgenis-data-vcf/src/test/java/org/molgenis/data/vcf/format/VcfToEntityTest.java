package org.molgenis.data.vcf.format;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.*;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.molgenis.util.EntityUtils;
import org.molgenis.vcf.VcfReader;
import org.molgenis.vcf.VcfRecord;
import org.molgenis.vcf.meta.VcfMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.StringReader;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.AttributeType.*;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.vcf.model.VcfAttributes.INFO;
import static org.molgenis.data.vcf.model.VcfAttributes.INTERNAL_ID;
import static org.testng.Assert.assertTrue;

@ContextConfiguration(classes = { VcfToEntityTest.Config.class })
public class VcfToEntityTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private VcfAttributes vcfAttrs;

	@Autowired
	private EntityTypeFactory entityMetaFactory;

	@Autowired
	private AttributeMetaDataFactory attrMetaFactory;

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
				+ "#CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO\n";
		vcfMetaSmall = parseHeaders(headersSmall);
		vcfToEntitySmall = new VcfToEntity("EntityNameSmall", vcfMetaSmall, vcfAttrs, entityMetaFactory,
				attrMetaFactory);
	}

	@Test
	public void testGetEntityMetaData()
	{
		EntityMetaData expectedEntityMeta = entityMetaFactory.create();
		expectedEntityMeta.setSimpleName("EntityNameSmall");
		expectedEntityMeta.setName("EntityNameSmall");
		expectedEntityMeta.addAttribute(vcfAttrs.getChromAttribute());
		expectedEntityMeta.addAttribute(vcfAttrs.getAltAttribute());
		expectedEntityMeta.addAttribute(vcfAttrs.getPosAttribute());
		expectedEntityMeta.addAttribute(vcfAttrs.getRefAttribute());
		expectedEntityMeta.addAttribute(vcfAttrs.getFilterAttribute());
		expectedEntityMeta.addAttribute(vcfAttrs.getQualAttribute());
		expectedEntityMeta.addAttribute(vcfAttrs.getIdAttribute());

		AttributeMetaData internalIdMeta = attrMetaFactory.create().setName(INTERNAL_ID).setDataType(STRING)
				.setVisible(false);
		expectedEntityMeta.addAttribute(internalIdMeta, ROLE_ID);

		AttributeMetaData infoMetaData = attrMetaFactory.create().setName(INFO).setDataType(COMPOUND).setNillable(true);

		AttributeMetaData infoNS = attrMetaFactory.create().setName("NS").setDataType(INT)
				.setDescription("Number of Samples With Data").setAggregatable(true);
		infoMetaData.addAttributePart(infoNS);
		AttributeMetaData infoDF = attrMetaFactory.create().setName("DF").setDataType(BOOL).setDescription("Flag field")
				.setAggregatable(true);
		infoMetaData.addAttributePart(infoDF);
		AttributeMetaData infoDF2 = attrMetaFactory.create().setName("DF2").setDataType(BOOL)
				.setDescription("Flag field 2").setAggregatable(true);
		infoMetaData.addAttributePart(infoDF2);

		expectedEntityMeta.addAttribute(infoMetaData);

		EntityMetaData actualEntityMeta = vcfToEntitySmall.getEntityMetaData();
		String backend = "test";
		expectedEntityMeta.setBackend(backend);
		actualEntityMeta.setBackend(backend);
		Package package_ = mock(Package.class);
		when(package_.getIdValue()).thenReturn("pck0");
		expectedEntityMeta.setPackage(package_);
		actualEntityMeta.setPackage(package_);

		expectedEntityMeta.getAllAttributes().forEach(attr -> attr.setIdentifier(null));
		actualEntityMeta.getAllAttributes().forEach(attr -> attr.setIdentifier(null));
		assertTrue(EntityUtils.equals(expectedEntityMeta, actualEntityMeta));
	}

	@Test
	public void testToEntity() throws IOException
	{
		VcfRecord record = new VcfRecord(vcfMetaSmall,
				new String[] { "10", "12345", "id3", "A", "C", "7.9123", "pass", "DF" });
		Entity entity = vcfToEntitySmall.toEntity(record);
		Entity expected = new DynamicEntity(vcfToEntitySmall.getEntityMetaData());
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
		assertTrue(EntityUtils.equals(entity, expected));
	}

	@Test
	public void testToEntityAlternativeAlleles() throws IOException
	{
		VcfRecord record = new VcfRecord(vcfMetaSmall,
				new String[] { "10", "12345", "id3", "A", "A,C,G,T,N,*", "7.9123", "pass", "DF;DF2" });
		Entity entity = vcfToEntitySmall.toEntity(record);
		Entity expected = new DynamicEntity(vcfToEntitySmall.getEntityMetaData());
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
		assertTrue(EntityUtils.equals(entity, expected));
	}

	@Configuration
	@ComponentScan({ "org.molgenis.data.vcf.model" })
	public static class Config
	{

	}
}
