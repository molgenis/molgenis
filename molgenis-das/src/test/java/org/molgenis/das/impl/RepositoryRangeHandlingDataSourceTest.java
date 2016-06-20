package org.molgenis.das.impl;

public class RepositoryRangeHandlingDataSourceTest
{
	//	RepositoryRangeHandlingDataSource source;
	//	private DasFeature dasFeature;
	//	private DataService dataService;
	//	private ArrayList<Hit> resultList;
	//	private ArrayList<DasFeature> featureList;
	//	private GenomicDataSettings genomicDataSettings;
	//
	//	@BeforeMethod
	//	public void setUp() throws DataSourceException, MalformedURLException
	//	{
	//		dataService = mock(DataService.class);
	//		genomicDataSettings = mock(GenomicDataSettings.class);
	//
	//		ApplicationContext ctx = mock(ApplicationContext.class);
	//		when(ctx.getBean(DataService.class)).thenReturn(dataService);
	//		when(ctx.getBean(GenomicDataSettings.class)).thenReturn(genomicDataSettings);
	//		new ApplicationContextProvider().setApplicationContext(ctx);
	//
	//		EntityMetaData metaData = new EntityMetaData("dataset");
	//		when(dataService.getEntityMetaData("dataset")).thenReturn(metaData);
	//		when(genomicDataSettings.getAttributeNameForAttributeNameArray(ATTRS_CHROM, metaData)).thenReturn("CHROM");
	//
	//		DasType type = new DasType("0", "", "", "type");
	//		DasMethod method = new DasMethod("not_recorded", "not_recorded", "ECO:0000037");
	//		source = new RepositoryRangeHandlingDataSource();
	//		DataSourceConfiguration dataSourceConfig = mock(DataSourceConfiguration.class);
	//		PropertyType propertyType = new PropertyType();
	//		propertyType.setValue("type");
	//		when(dataSourceConfig.getDataSourceProperties()).thenReturn(Collections.singletonMap("type", propertyType));
	//		source.init(null, null, dataSourceConfig);
	//		Map<URL, String> linkout = new HashMap<URL, String>();
	//		linkout.put(new URL("http://www.molgenis.org/"), "Link");
	//
	//		List<DasTarget> dasTarget = new ArrayList<DasTarget>();
	//		dasTarget.add(new MolgenisDasTarget("mutation id", 10, 1000, "description"));
	//		List<String> notes = new ArrayList<String>();
	//		notes.add("track:dataset");
	//		notes.add("source:MOLGENIS");
	//
	//		dasFeature = new DasFeature("mutation id", "description", type, method, 10, 1000, new Double(0),
	//				DasFeatureOrientation.ORIENTATION_NOT_APPLICABLE, DasPhase.PHASE_NOT_APPLICABLE, notes, linkout,
	//				dasTarget, new ArrayList<String>(), null);
	//
	//		Query<Entity> q = new QueryImpl<Entity>().eq("CHROM", "1");
	//		q.pageSize(100);
	//		SearchResult result = mock(SearchResult.class);
	//		EntityMetaData emd = new EntityMetaData("DAS");
	//		emd.addAttribute(new AttributeMetaData("STOP"));
	//		emd.addAttribute(new AttributeMetaData("linkout"));
	//		emd.addAttribute(new AttributeMetaData("NAME"), ROLE_LABEL);
	//		emd.addAttribute(new AttributeMetaData("INFO"));
	//		emd.addAttribute(new AttributeMetaData("POS"));
	//		emd.addAttribute(new AttributeMetaData("ID"), ROLE_ID);
	//		emd.addAttribute(new AttributeMetaData("CHROM"));
	//
	//		MapEntity entity = new MapEntity(emd);
	//
	//		Map<String, Object> map = new HashMap<String, Object>();
	//		map.put("STOP", 1000);
	//		map.put("linkout", "http://www.molgenis.org/");
	//		map.put("NAME", "mutation name");
	//		map.put("INFO", "description");
	//		map.put("POS", 10);
	//		map.put("ID", "mutation id");
	//		map.put("CHROM", "1");
	//
	//		entity.set(new MapEntity(map));
	//
	//		resultList = new ArrayList<>();
	//		resultList.add(new Hit("", "", map));
	//		featureList = new ArrayList<>();
	//		featureList.add(dasFeature);
	//		when(dataService.findAll("dataset", q)).thenAnswer(new Answer<Stream<MapEntity>>()
	//		{
	//			@Override
	//			public Stream<MapEntity> answer(InvocationOnMock invocation) throws Throwable
	//			{
	//				return Stream.of(entity);
	//			}
	//		});
	//		when(result.iterator()).thenReturn(resultList.iterator());
	//
	//		when(genomicDataSettings.getAttributeNameForAttributeNameArray(ATTRS_CHROM, entity.getEntityMetaData()))
	//				.thenReturn("CHROM");
	//		when(genomicDataSettings.getAttributeNameForAttributeNameArray(ATTRS_POS, entity.getEntityMetaData()))
	//				.thenReturn("POS");
	//		when(genomicDataSettings.getAttributeNameForAttributeNameArray(ATTRS_STOP, entity.getEntityMetaData()))
	//				.thenReturn("STOP");
	//		when(genomicDataSettings.getAttributeNameForAttributeNameArray(ATTRS_IDENTIFIER, entity.getEntityMetaData()))
	//				.thenReturn("ID");
	//		when(genomicDataSettings.getAttributeNameForAttributeNameArray(ATTRS_DESCRIPTION, entity.getEntityMetaData()))
	//				.thenReturn("INFO");
	//		when(genomicDataSettings.getAttributeNameForAttributeNameArray(ATTRS_NAME, entity.getEntityMetaData()))
	//				.thenReturn("NAME");
	//		when(genomicDataSettings.getAttributeNameForAttributeNameArray(ATTRS_LINKOUT, entity.getEntityMetaData()))
	//				.thenReturn("linkout");
	//
	//	}
	//
	//	@AfterMethod
	//	public void teardown()
	//	{
	//		Mockito.reset(dataService);
	//	}
	//
	//	@Test
	//	public void getFeaturesRange() throws UnimplementedFeatureException, DataSourceException,
	//			BadReferenceObjectException, CoordinateErrorException
	//	{
	//		assertEquals(new DasAnnotatedSegment("1", 1, 100000, "1.00", "1", featureList).getFeatures(),
	//				source.getFeatures("1,dasdataset_dataset", 1, 100000, 100).getFeatures());
	//		assertEquals(new DasAnnotatedSegment("1", 1, 100000, "1.00", "1", featureList).getSegmentId(),
	//				source.getFeatures("1,dasdataset_dataset", 1, 100000, 100).getSegmentId());
	//		assertEquals(new DasAnnotatedSegment("1", 1, 100000, "1.00", "1", featureList).getStartCoordinate(),
	//				source.getFeatures("1,dasdataset_dataset", 1, 100000, 100).getStartCoordinate());
	//		assertEquals(new DasAnnotatedSegment("1", 1, 100000, "1.00", "1", featureList).getStopCoordinate(),
	//				source.getFeatures("1,dasdataset_dataset", 1, 100000, 100).getStopCoordinate());
	//	}
	//
	//	@Test
	//	public void getTypes() throws UnimplementedFeatureException, DataSourceException, BadReferenceObjectException,
	//			CoordinateErrorException
	//	{
	//		assertEquals(Collections.singleton(new DasType("type", null, "?", "type")), source.getTypes());
	//	}
}
