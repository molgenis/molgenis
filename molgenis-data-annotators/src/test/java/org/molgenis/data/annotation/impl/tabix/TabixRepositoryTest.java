package org.molgenis.data.annotation.impl.tabix;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.DECIMAL;
import static org.molgenis.data.vcf.VcfRepository.ALT;
import static org.molgenis.data.vcf.VcfRepository.ALT_META;
import static org.molgenis.data.vcf.VcfRepository.CHROM;
import static org.molgenis.data.vcf.VcfRepository.CHROM_META;
import static org.molgenis.data.vcf.VcfRepository.POS;
import static org.molgenis.data.vcf.VcfRepository.POS_META;
import static org.molgenis.data.vcf.VcfRepository.REF;
import static org.molgenis.data.vcf.VcfRepository.REF_META;
import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.annotator.tabix.TabixRepository;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class TabixRepositoryTest
{
	private TabixRepository tabixRepository;
	private DefaultEntityMetaData repoMetaData;

	@BeforeTest
	public void beforeTest() throws IOException
	{
		repoMetaData = new DefaultEntityMetaData("CaddTest");
		repoMetaData.addAttributeMetaData(CHROM_META);
		repoMetaData.addAttributeMetaData(POS_META);
		repoMetaData.addAttributeMetaData(REF_META);
		repoMetaData.addAttributeMetaData(ALT_META);
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("CADD", DECIMAL));
		repoMetaData.addAttributeMetaData(new DefaultAttributeMetaData("CADD_SCALED", DECIMAL));
		repoMetaData.addAttribute("id").setIdAttribute(true).setVisible(false);

		tabixRepository = new TabixRepository("target" + File.separator + "test-classes" + File.separator
				+ "cadd_test.vcf.gz", repoMetaData);
	}

	@Test
	public void testGetEntityMetaData()
	{
		assertEquals(tabixRepository.getEntityMetaData(), repoMetaData);
	}

	@Test
	public void testQuery()
	{
		Query query = tabixRepository.query().eq(VcfRepository.CHROM, "1").and().eq(VcfRepository.POS, "100");
		assertEquals(tabixRepository.findAll(query), Arrays.asList(newEntity("1", 100, "C", "T", -0.03, 2.003),
				newEntity("1", 100, "C", "G", -0.4, 4.321), newEntity("1", 100, "C", "A", 2.102, 43.2)));
	}

	@Test
	public void testIterator()
	{
		assertEquals(stream(tabixRepository.spliterator(), false).collect(toList()), Arrays.asList(
				newEntity("1", 100, "C", "T", -0.03, 2.003), newEntity("1", 100, "C", "G", -0.4, 4.321),
				newEntity("1", 100, "C", "A", 2.102, 43.2), newEntity("2", 200, "A", "T", 2.0, 3.012),
				newEntity("2", 200, "A", "G", -2.30, 20.2), newEntity("3", 300, "G", "A", 0.2, 23.1),
				newEntity("3", 300, "G", "T", -2.4, 0.123), newEntity("3", 300, "G", "X", -0.002, 2.3),
				newEntity("3", 300, "G", "C", 0.5, 14.5)));
	}

	private Entity newEntity(String chrom, long pos, String ref, String alt, double cadd, double caddScaled)
	{
		Entity result = new MapEntity(repoMetaData);
		result.set(CHROM, chrom);
		result.set(POS, pos);
		result.set(REF, ref);
		result.set(ALT, alt);
		result.set("CADD", cadd);
		result.set("CADD_SCALED", caddScaled);
		return result;
	}
}