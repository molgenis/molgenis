package org.molgenis.data.annotator.tabix;

public class TabixRepositoryTest
{
	//	private TabixRepository tabixRepository;
	//	private EntityMetaData entityMetaData;
	//	@Mock
	//	private TabixReader tabixReader;
	//	@Mock
	//	private Iterator iterator;
	//
	//	@BeforeTest
	//	public void beforeTest()
	//	{
	//		initMocks(this);
	//		EntityMetaData emd = new EntityMetaDataImpl("MyEntity");
	//		emd.addAttribute(new AttributeMetaData("ID").setAuto(true), ROLE_ID);
	//		emd.addAttributes(asList(CHROM_META, POS_META));
	//		emd.addAttribute(new AttributeMetaData("Description").setNillable(false));
	//
	//		entityMetaData = emd;
	//		tabixRepository = new TabixRepository(tabixReader, entityMetaData, CHROM, POS);
	//	}
	//
	//	@Test
	//	public void testReaderReturnsEmptyIteratorForNullValue()
	//	{
	//		Mockito.when(tabixReader.query("13:12-12")).thenReturn(null);
	//
	//		Stream<Entity> actual = tabixRepository.findAll(tabixRepository.query().eq(CHROM, "13").and().eq(POS, 12));
	//
	//		assertEquals(Collections.emptyList(), actual.collect(toList()));
	//	}
	//
	//	@Test
	//	public void testReaderFiltersRows() throws IOException
	//	{
	//		Mockito.when(tabixReader.query("13:12-12")).thenReturn(iterator);
	//		Mockito.when(iterator.next()).thenReturn("id1\t13\t11\tnope", "id2\t13\t12\tyup", "id3\t13\t12\tyup",
	//				"id3\t13\t13\tnope", null);
	//
	//		Stream<Entity> actual = tabixRepository.findAll(tabixRepository.query().eq(CHROM, "13").and().eq(POS, 12));
	//
	//		Entity e1 = new MapEntity(entityMetaData);
	//		e1.set("ID", "id2");
	//		e1.set("#CHROM", "13");
	//		e1.set("POS", 12l);
	//		e1.set("Description", "yup");
	//
	//		Entity e2 = new MapEntity(entityMetaData);
	//		e2.set("ID", "id3");
	//		e2.set("#CHROM", "13");
	//		e2.set("POS", 12l);
	//		e2.set("Description", "yup");
	//		assertEquals(actual.collect(toList()), Arrays.asList(e1, e2));
	//	}
}
