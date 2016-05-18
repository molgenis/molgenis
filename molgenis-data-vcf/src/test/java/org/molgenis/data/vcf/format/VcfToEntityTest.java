package org.molgenis.data.vcf.format;

import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.BOOL;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.COMPOUND;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.INT;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.STRING;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.vcf.VcfRepository.ALT_META;
import static org.molgenis.data.vcf.VcfRepository.CHROM_META;
import static org.molgenis.data.vcf.VcfRepository.FILTER_META;
import static org.molgenis.data.vcf.VcfRepository.ID_META;
import static org.molgenis.data.vcf.VcfRepository.INFO;
import static org.molgenis.data.vcf.VcfRepository.INTERNAL_ID;
import static org.molgenis.data.vcf.VcfRepository.POS_META;
import static org.molgenis.data.vcf.VcfRepository.QUAL_META;
import static org.molgenis.data.vcf.VcfRepository.REF_META;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.AttributeMetaData;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.data.meta.EntityMetaDataImpl;
import org.molgenis.data.support.MapEntity;
import org.molgenis.vcf.VcfReader;
import org.molgenis.vcf.VcfRecord;
import org.molgenis.vcf.meta.VcfMeta;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

public class VcfToEntityTest
{
	private VcfToEntity vcfToEntitySmall;
	private VcfMeta vcfMetaSmall;

	private static VcfMeta parseHeaders(String headers) throws IOException
	{
		VcfReader vcfReader;
		vcfReader = new VcfReader(new StringReader(headers));
		VcfMeta result = vcfReader.getVcfMeta();
		vcfReader.close();
		return result;
	}

	@BeforeTest
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
		vcfToEntitySmall = new VcfToEntity("EntityNameSmall", vcfMetaSmall);
	}

	@Test
	public void testGetEntityMetaData()
	{
		EntityMetaData expected = new EntityMetaDataImpl("EntityName");
		expected.addAttributes(
				Arrays.asList(CHROM_META, ALT_META, POS_META, REF_META, FILTER_META, QUAL_META, ID_META));
		AttributeMetaData internalIdMeta = new AttributeMetaData(INTERNAL_ID, STRING);
		internalIdMeta.setVisible(false);
		expected.addAttribute(internalIdMeta, ROLE_ID);

		AttributeMetaData infoMetaData = new AttributeMetaData(INFO, COMPOUND).setNillable(true);

		AttributeMetaData infoNS = new AttributeMetaData("NS", INT)
				.setNillable(true).setDescription("Number of Samples With Data");
		infoMetaData.addAttributePart(infoNS);
		AttributeMetaData infoDF = new AttributeMetaData("DF", BOOL)
				.setNillable(false).setDescription("Flag field");
		infoMetaData.addAttributePart(infoDF);
		AttributeMetaData infoDF2 = new AttributeMetaData("DF2", BOOL)
				.setNillable(false).setDescription("Flag field 2");
		infoMetaData.addAttributePart(infoDF2);

		expected.addAttribute(infoMetaData);

		EntityMetaData actualEntityMetaData = vcfToEntitySmall.getEntityMetaData();

		ArrayList<AttributeMetaData> actualAttributes = Lists.newArrayList(actualEntityMetaData.getAtomicAttributes());
		ArrayList<AttributeMetaData> expectedAttributes = Lists.newArrayList(expected.getAtomicAttributes());
		assertEquals(actualAttributes, expectedAttributes);
	}

	@Test
	public void testToEntity() throws IOException
	{
		VcfRecord record = new VcfRecord(vcfMetaSmall, new String[]
		{ "10", "12345", "id3", "A", "C", "7.9123", "pass", "DF" });
		Entity entity = vcfToEntitySmall.toEntity(record);
		Entity expected = new MapEntity(vcfToEntitySmall.getEntityMetaData());
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
		assertEquals(entity, expected);
	}

	@Test
	public void testToEntityAlternativeAlleles() throws IOException
	{
		VcfRecord record = new VcfRecord(vcfMetaSmall, new String[]
		{ "10", "12345", "id3", "A", "A,C,G,T,N,*", "7.9123", "pass", "DF;DF2" });
		Entity entity = vcfToEntitySmall.toEntity(record);
		Entity expected = new MapEntity(vcfToEntitySmall.getEntityMetaData());
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
		assertEquals(entity, expected);
	}
}
