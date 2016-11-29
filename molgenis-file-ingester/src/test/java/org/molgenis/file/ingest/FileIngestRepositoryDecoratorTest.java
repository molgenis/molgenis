package org.molgenis.file.ingest;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;

public class FileIngestRepositoryDecoratorTest
{
	private Repository<Entity> decoratedRepo;
	private FileIngestRepositoryDecorator fileIngestRepositoryDecorator;

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void setUpBeforeMethod()
	{
		decoratedRepo = mock(Repository.class);
		FileIngesterJobScheduler fileIngesterJobScheduler = mock(FileIngesterJobScheduler.class);
		DataService dataService = mock(DataService.class);
		fileIngestRepositoryDecorator = new FileIngestRepositoryDecorator(decoratedRepo, fileIngesterJobScheduler,
				dataService);
	}

	@Test
	public void testDelegate() throws Exception
	{
		assertEquals(fileIngestRepositoryDecorator.delegate(), decoratedRepo);
	}

	@Test
	public void testQuery() throws Exception
	{
		assertEquals(fileIngestRepositoryDecorator.query().getRepository(), fileIngestRepositoryDecorator);
	}
}