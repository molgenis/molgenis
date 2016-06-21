package org.molgenis.data.elasticsearch.request;

public class SortGeneratorTest
{
	//	private SortGenerator sortGenerator;
	//	private SearchRequestBuilder searchRequestBuilder;
	//	private EntityMetaData entityMetaData;
	//
	//	@BeforeMethod
	//	public void beforeMethod()
	//	{
	//		sortGenerator = new SortGenerator();
	//		searchRequestBuilder = mock(SearchRequestBuilder.class);
	//		EntityMetaData entityMetaData = new EntityMetaData("entity");
	//		entityMetaData.addAttribute("int").setDataType(MolgenisFieldTypes.INT);
	//		entityMetaData.addAttribute("string").setDataType(MolgenisFieldTypes.STRING);
	//		this.entityMetaData = entityMetaData;
	//	}
	//
	//	@Test
	//	public void testGenerateNoSort()
	//	{
	//		QueryImpl<Entity> query = new QueryImpl<>();
	//		sortGenerator.generate(searchRequestBuilder, query, entityMetaData);
	//		verifyZeroInteractions(searchRequestBuilder);
	//	}
	//
	//	@Test
	//	public void testGenerateAsc()
	//	{
	//		Query<Entity> query = new QueryImpl<>().sort(new Sort("int", Direction.ASC));
	//		sortGenerator.generate(searchRequestBuilder, query, entityMetaData);
	//		ArgumentCaptor<FieldSortBuilder> argument = ArgumentCaptor.forClass(FieldSortBuilder.class);
	//		verify(searchRequestBuilder).addSort(argument.capture());
	//		FieldSortBuilder sortBuilder = argument.getValue();
	//		assertEquals(sortBuilder.toString().replaceAll("\\s", ""), "\"int\"{\"order\":\"asc\",\"mode\":\"min\"}");
	//	}
	//
	//	@Test
	//	public void testGenerateAscRaw()
	//	{
	//		Query<Entity> query = new QueryImpl<>().sort(new Sort("string", Direction.ASC));
	//		sortGenerator.generate(searchRequestBuilder, query, entityMetaData);
	//		ArgumentCaptor<FieldSortBuilder> argument = ArgumentCaptor.forClass(FieldSortBuilder.class);
	//		verify(searchRequestBuilder).addSort(argument.capture());
	//		FieldSortBuilder sortBuilder = argument.getValue();
	//		assertEquals(sortBuilder.toString().replaceAll("\\s", ""), "\"string.raw\"{\"order\":\"asc\",\"mode\":\"min\"}");
	//	}
	//
	//	@Test
	//	public void testGenerateDesc()
	//	{
	//		Query<Entity> query = new QueryImpl<>().sort(new Sort("int", Direction.DESC));
	//		sortGenerator.generate(searchRequestBuilder, query, entityMetaData);
	//		ArgumentCaptor<FieldSortBuilder> argument = ArgumentCaptor.forClass(FieldSortBuilder.class);
	//		verify(searchRequestBuilder).addSort(argument.capture());
	//		FieldSortBuilder sortBuilder = argument.getValue();
	//		assertEquals(sortBuilder.toString().replaceAll("\\s", ""), "\"int\"{\"order\":\"desc\",\"mode\":\"min\"}");
	//	}
	//
	//	@Test
	//	public void testGenerateDescRaw()
	//	{
	//		Query<Entity> query = new QueryImpl<>().sort(new Sort("string", Direction.DESC));
	//		sortGenerator.generate(searchRequestBuilder, query, entityMetaData);
	//		ArgumentCaptor<FieldSortBuilder> argument = ArgumentCaptor.forClass(FieldSortBuilder.class);
	//		verify(searchRequestBuilder).addSort(argument.capture());
	//		FieldSortBuilder sortBuilder = argument.getValue();
	//		assertEquals(sortBuilder.toString().replaceAll("\\s", ""),
	//				"\"string.raw\"{\"order\":\"desc\",\"mode\":\"min\"}");
	//	}
	//
	//	@Test
	//	public void testGenerateDescAscRaw()
	//	{
	//		Query<Entity> query = new QueryImpl<>().sort(new Sort().on("int", Direction.DESC).on("string", Direction.ASC));
	//		sortGenerator.generate(searchRequestBuilder, query, entityMetaData);
	//		ArgumentCaptor<FieldSortBuilder> argument = ArgumentCaptor.forClass(FieldSortBuilder.class);
	//		verify(searchRequestBuilder, times(2)).addSort(argument.capture());
	//		List<FieldSortBuilder> sortBuilder = argument.getAllValues();
	//		assertEquals(sortBuilder.size(), 2);
	//		assertEquals(sortBuilder.get(0).toString().replaceAll("\\s", ""),
	//				"\"int\"{\"order\":\"desc\",\"mode\":\"min\"}");
	//		assertEquals(sortBuilder.get(1).toString().replaceAll("\\s", ""),
	//				"\"string.raw\"{\"order\":\"asc\",\"mode\":\"min\"}");
	//	}
}
