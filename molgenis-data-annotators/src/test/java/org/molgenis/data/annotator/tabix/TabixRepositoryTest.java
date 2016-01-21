package org.molgenis.data.annotator.tabix;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.data.vcf.VcfRepository.CHROM;
import static org.molgenis.data.vcf.VcfRepository.CHROM_META;
import static org.molgenis.data.vcf.VcfRepository.POS;
import static org.molgenis.data.vcf.VcfRepository.POS_META;
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
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class TabixRepositoryTest
{
	private TabixRepository tabixRepository;
	private EntityMetaData entityMetaData;
	@Mock
	private TabixReader tabixReader;
	@Mock
	private Iterator iterator;

	@BeforeTest
	public void beforeTest()
	{
		initMocks(this);
		DefaultEntityMetaData emd = new DefaultEntityMetaData("MyEntity");
		emd.addAttributeMetaData(new DefaultAttributeMetaData("ID").setAuto(true).setIdAttribute(true));
		emd.addAllAttributeMetaData(asList(CHROM_META, POS_META));
		emd.addAttributeMetaData(new DefaultAttributeMetaData("Description").setNillable(false));

		entityMetaData = emd;
		tabixRepository = new TabixRepository(tabixReader, entityMetaData, CHROM, POS);
	}

	@Test
	public void testReaderReturnsEmptyIteratorForNullValue()
	{
		Mockito.when(tabixReader.query("13:12-12")).thenReturn(null);

		Stream<Entity> actual = tabixRepository.findAll(tabixRepository.query().eq(CHROM, "13").and().eq(POS, 12));

		assertEquals(Collections.emptyList(), actual.collect(toList()));
	}

	@Test
	public void testReaderFiltersRows() throws IOException
	{
		Mockito.when(tabixReader.query("13:12-12")).thenReturn(iterator);
		Mockito.when(iterator.next()).thenReturn("id1\t13\t11\tnope", "id2\t13\t12\tyup", "id3\t13\t12\tyup",
				"id3\t13\t13\tnope", null);

		Stream<Entity> actual = tabixRepository.findAll(tabixRepository.query().eq(CHROM, "13").and().eq(POS, 12));

		Entity e1 = new MapEntity(entityMetaData);
		e1.set("ID", "id2");
		e1.set("#CHROM", "13");
		e1.set("POS", 12l);
		e1.set("Description", "yup");

		Entity e2 = new MapEntity(entityMetaData);
		e2.set("ID", "id3");
		e2.set("#CHROM", "13");
		e2.set("POS", 12l);
		e2.set("Description", "yup");
		assertEquals(actual.collect(toList()), Arrays.asList(e1, e2));
	}
}
