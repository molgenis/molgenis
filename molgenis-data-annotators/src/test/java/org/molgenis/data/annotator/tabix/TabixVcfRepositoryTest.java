package org.molgenis.data.annotator.tabix;

import static java.util.stream.Collectors.toList;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.data.vcf.VcfRepository.CHROM;
import static org.molgenis.data.vcf.VcfRepository.POS;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotator.tabix.TabixReader.Iterator;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfReaderFactory;
import org.molgenis.vcf.VcfReader;
import org.molgenis.vcf.meta.VcfMeta;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class TabixVcfRepositoryTest
{
	private TabixVcfRepository tabixVcfRepository;
	private EntityMetaData entityMetaData;
	@Mock
	private TabixReader tabixReader;
	@Mock
	private VcfReaderFactory vcfReaderFactory;
	@Mock
	private Iterator iterator;
	@Mock
	private VcfReader vcfReader;

	@BeforeTest
	public void beforeTest() throws IOException
	{
		initMocks(this);
		Mockito.when(vcfReaderFactory.get()).thenReturn(vcfReader);
		VcfMeta vcfMeta = new VcfMeta();
		Mockito.when(vcfReader.getVcfMeta()).thenReturn(vcfMeta);
		tabixVcfRepository = new TabixVcfRepository(vcfReaderFactory, tabixReader, "MyEntity");
		entityMetaData = tabixVcfRepository.getEntityMetaData();
	}

	@Test
	public void testReaderReturnsEmptyIteratorForNullValue()
	{
		Mockito.when(tabixReader.query("13:12-12")).thenReturn(null);

		Stream<Entity> actual = tabixVcfRepository
				.findAll(tabixVcfRepository.query().eq(CHROM, "13").and().eq(POS, 12));

		assertEquals(Collections.emptyList(), actual.collect(toList()));
	}

	@Test
	public void testReaderFiltersRows() throws IOException
	{
		Mockito.when(tabixReader.query("13:12-12")).thenReturn(iterator);
		Mockito.when(iterator.next()).thenReturn("13\t11\tid1\tA\tC\t12\t.\t.\t.", "13\t12\tid2\tA\tC\t12\t.\t.\t.",
				"13\t12\tid3\tA\tG\t12\t.\t.\t.", "13\t13\tid4\tA\tC\t12\t.\t.\t.", null);

		Stream<Entity> actual = tabixVcfRepository
				.findAll(tabixVcfRepository.query().eq(CHROM, "13").and().eq(POS, 12));

		Entity e1 = new MapEntity(entityMetaData);
		e1.set("#CHROM", "13");
		e1.set("ALT", "C");
		e1.set("POS", 12);
		e1.set("REF", "A");
		e1.set("FILTER", null);
		e1.set("QUAL", "12");
		e1.set("ID", "id2");
		e1.set("INTERNAL_ID", "1IRDGOK5Lz_D5OTHDCufFA");

		Entity e2 = new MapEntity(entityMetaData);
		e2.set("#CHROM", "13");
		e2.set("ALT", "G");
		e2.set("POS", 12);
		e2.set("REF", "A");
		e2.set("FILTER", null);
		e2.set("QUAL", "12");
		e2.set("ID", "id3");
		e2.set("INTERNAL_ID", "ld2wCadyeITy89CrL2TnWg");
		assertEquals(actual.collect(toList()), Arrays.asList(e1, e2));
	}
}
