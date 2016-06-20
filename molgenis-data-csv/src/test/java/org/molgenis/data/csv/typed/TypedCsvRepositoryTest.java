package org.molgenis.data.csv.typed;

public class TypedCsvRepositoryTest
{
	//	private static EntityMetaData ENTITY_META_DATA;
	//
	//	public TypedCsvRepositoryTest()
	//	{
	//		EntityMetaData meta = new EntityMetaData("Test");
	//
	//		AttributeMetaData attr = new AttributeMetaData("index", INT);
	//		meta.addAttribute(attr, ROLE_ID);
	//
	//		meta.addAttribute(new AttributeMetaData("col1", STRING));
	//		meta.addAttribute(new AttributeMetaData("col2", STRING));
	//
	//		ENTITY_META_DATA = meta;
	//	}
	//
	//	@Test
	//	public void typedIterator() throws FileNotFoundException, IOException
	//	{
	//		InputStream in = getClass().getResourceAsStream("/testdata.csv");
	//		File csvFile = new File(FileUtils.getTempDirectory(), "testdata.csv");
	//		FileCopyUtils.copy(in, new FileOutputStream(csvFile));
	//
	//		TypedCsvRepository<TestEntity> repo = new TypedCsvRepository<TestEntity>(csvFile, ENTITY_META_DATA, ',', 1,
	//				new TestEntityLineMapper());
	//
	//		assertEquals(Iterables.size(repo), 5);
	//
	//		CloseableIterator<TestEntity> it = repo.typedIterator();
	//		try
	//		{
	//			TestEntity entity = it.next();
	//			assertEquals(entity.getCol1(), "val1");
	//			assertEquals(entity.getCol2(), "val2");
	//		}
	//		finally
	//		{
	//			it.close();
	//		}
	//	}
	//
	//	private static class TestEntityLineMapper implements LineMapper<TestEntity>
	//	{
	//		@Override
	//		public TestEntity mapLine(String[] values, int lineNumber)
	//		{
	//			return new TestEntity(lineNumber, values[0], values[1]);
	//		}
	//	}
	//
	//	private static class TestEntity extends AbstractEntity
	//	{
	//		private static final long serialVersionUID = -2658561504524427609L;
	//		private final Integer index;
	//		private final String col1;
	//		private final String col2;
	//
	//		public TestEntity(Integer index, String col1, String col2)
	//		{
	//			super();
	//			this.index = index;
	//			this.col1 = col1;
	//			this.col2 = col2;
	//		}
	//
	//		@Override
	//		public EntityMetaData getEntityMetaData()
	//		{
	//			return ENTITY_META_DATA;
	//		}
	//
	//		@Override
	//		public Iterable<String> getAttributeNames()
	//		{
	//			return Lists.newArrayList("index", "col1", "col2");
	//		}
	//
	//		@Override
	//		public Object getIdValue()
	//		{
	//			return index;
	//		}
	//
	//		@Override
	//		public Object get(String attributeName)
	//		{
	//			if (attributeName.equalsIgnoreCase("index"))
	//			{
	//				return getIndex();
	//			}
	//
	//			if (attributeName.equalsIgnoreCase("col1"))
	//			{
	//				return getCol1();
	//			}
	//
	//			if (attributeName.equalsIgnoreCase("col2"))
	//			{
	//				return getCol2();
	//			}
	//
	//			return null;
	//		}
	//
	//		@Override
	//		public void set(String attributeName, Object value)
	//		{
	//			throw new UnsupportedOperationException();
	//		}
	//
	//		public String getCol1()
	//		{
	//			return col1;
	//		}
	//
	//		public String getCol2()
	//		{
	//			return col2;
	//		}
	//
	//		public Integer getIndex()
	//		{
	//			return index;
	//		}
	//
	//		@Override
	//		public void set(Entity values)
	//		{
	//			throw new UnsupportedOperationException();
	//		}
	//
	//	}
}
