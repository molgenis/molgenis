package org.molgenis.data.meta;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.stream.Stream;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.molgenis.data.DataService;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.RepositoryCollectionRegistry;
import org.molgenis.data.meta.system.SystemEntityMetaDataRegistry;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MetaDataServiceImplTest
{
	private MetaDataServiceImpl metaDataServiceImpl;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		DataService dataService = mock(DataService.class);
		RepositoryCollectionRegistry repoCollectionRegistry = mock(RepositoryCollectionRegistry.class);
		RepositoryCollection defaultRepoCollection = mock(RepositoryCollection.class);
		when(defaultRepoCollection.getLanguageCodes()).thenAnswer(new Answer<Stream<String>>()
		{
			@Override
			public Stream<String> answer(InvocationOnMock invocation) throws Throwable
			{
				return Stream.of("en", "nl");
			}
		});
		when(repoCollectionRegistry.getDefaultRepoCollection()).thenReturn(defaultRepoCollection);
		SystemEntityMetaDataRegistry systemEntityMetaRegistry = mock(SystemEntityMetaDataRegistry.class);
		metaDataServiceImpl = new MetaDataServiceImpl(dataService, repoCollectionRegistry, systemEntityMetaRegistry);
	}

	@Test
	public void getLanguageCodes()
	{
		assertEquals(metaDataServiceImpl.getLanguageCodes().collect(toList()), asList("en", "nl"));
	}

	// TODO add test cases
}
