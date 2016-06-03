package org.molgenis.data.elasticsearch.reindex;

public class ReindexActionRepositoryCollectionDecoratorTest
{
	//	private final static String REPOSITORY_NAME = "repo";
	//	private ManageableRepositoryCollection decoratedRepositoryCollection;
	//	private EntityMetaData entityMeta;
	//	private ReindexActionRegisterService reindexActionRegisterService;
	//	private ReindexActionRepositoryCollectionDecorator reindexActionRepositoryCollectionDecorator;
	//
	//	@SuppressWarnings("unchecked")
	//	@BeforeMethod
	//	public void setUpBeforeMethod()
	//	{
	//		decoratedRepositoryCollection = mock(ManageableRepositoryCollection.class);
	//		Repository<Entity> repository = (Repository<Entity>) mock(Repository.class);
	//		when(decoratedRepositoryCollection.getRepository(REPOSITORY_NAME)).thenReturn(repository);
	//		entityMeta = mock(EntityMetaData.class);
	//		when(repository.getEntityMetaData()).thenReturn(entityMeta);
	//		reindexActionRegisterService = mock(ReindexActionRegisterService.class);
	//		reindexActionRepositoryCollectionDecorator = new ReindexActionRepositoryCollectionDecorator(
	//				decoratedRepositoryCollection,
	//				reindexActionRegisterService);
	//	}
	//
	//	@Test
	//	public void deleteEntityMeta()
	//	{
	//		reindexActionRepositoryCollectionDecorator.deleteEntityMeta(REPOSITORY_NAME);
	//		verify(decoratedRepositoryCollection, times(1)).deleteEntityMeta(REPOSITORY_NAME);
	//		verify(reindexActionRegisterService).register(entityMeta, CudType.DELETE, DataType.METADATA, null);
	//	}
	//
	//	@Test
	//	public void addAttribute()
	//	{
	//		DefaultAttributeMetaData attribute = new DefaultAttributeMetaData("attribute");
	//		reindexActionRepositoryCollectionDecorator.addAttribute(REPOSITORY_NAME, attribute);
	//		verify(decoratedRepositoryCollection, times(1)).addAttribute(REPOSITORY_NAME, attribute);
	//		verify(reindexActionRegisterService).register(entityMeta, CudType.UPDATE, DataType.METADATA, null);
	//	}
	//
	//	@Test
	//	public void deleteAttribute()
	//	{
	//		reindexActionRepositoryCollectionDecorator.deleteAttribute(REPOSITORY_NAME, "attribute");
	//		verify(decoratedRepositoryCollection, times(1)).deleteAttribute(REPOSITORY_NAME, "attribute");
	//		verify(reindexActionRegisterService).register(entityMeta, CudType.UPDATE, DataType.METADATA, null);
	//	}
	//
	//	@Test
	//	public void addAttributeSync()
	//	{
	//		DefaultAttributeMetaData attribute = mock(DefaultAttributeMetaData.class);
	//		reindexActionRepositoryCollectionDecorator.addAttributeSync(REPOSITORY_NAME, attribute);
	//		verify(decoratedRepositoryCollection, times(1)).addAttribute(REPOSITORY_NAME, attribute);
	//		verify(reindexActionRegisterService).register(entityMeta, CudType.UPDATE, DataType.METADATA, null);
	//	}
	//
	//	@Test
	//	public void addEntityMeta()
	//	{
	//		reindexActionRepositoryCollectionDecorator.addEntityMeta(entityMeta);
	//		verify(decoratedRepositoryCollection, times(1)).addEntityMeta(entityMeta);
	//		verify(reindexActionRegisterService).register(entityMeta, CudType.ADD, DataType.METADATA, null);
	//	}
}
