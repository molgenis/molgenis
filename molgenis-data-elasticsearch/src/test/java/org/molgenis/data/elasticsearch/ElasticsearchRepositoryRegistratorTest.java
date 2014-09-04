package org.molgenis.data.elasticsearch;

import org.mockito.ArgumentCaptor;
import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.springframework.core.Ordered;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class ElasticsearchRepositoryRegistratorTest{
    private DataService dataService;
    private RepositoryCollection repositoryCollection;
    private ElasticsearchRepositoryRegistrator elasticsearchRepositoryRegistrator;
    private Repository testRepo1;
    private Repository testRepo2;

    @BeforeMethod
	public void setUp() throws IOException
	{
		dataService = mock(DataService.class);
        repositoryCollection = mock(RepositoryCollection.class);
        elasticsearchRepositoryRegistrator = new ElasticsearchRepositoryRegistrator(dataService, repositoryCollection);
        testRepo1 = mock(Repository.class);
        testRepo2 = mock(Repository.class);

        when(repositoryCollection.getEntityNames()).thenReturn(Arrays.asList(new String[]{"test1", "test2"}));
        when(repositoryCollection.getRepositoryByEntityName("test1")).thenReturn(testRepo1);
        when(repositoryCollection.getRepositoryByEntityName("test2")).thenReturn(testRepo2);
    }

	@Test
	public void getOrder()
	{
		assertEquals(elasticsearchRepositoryRegistrator.getOrder(), Ordered.HIGHEST_PRECEDENCE);
	}

	@Test
	public void onApplicationEvent()
	{
		elasticsearchRepositoryRegistrator.onApplicationEvent(null);
        verify(repositoryCollection).getEntityNames();
        ArgumentCaptor<Repository> captor = ArgumentCaptor.forClass(Repository.class);

        verify(dataService, times(2)).addRepository(captor.capture());
        List<Repository> params = captor.getAllValues();
        assertTrue(params.contains(testRepo1));
        assertTrue(params.contains(testRepo2));
    }
}
