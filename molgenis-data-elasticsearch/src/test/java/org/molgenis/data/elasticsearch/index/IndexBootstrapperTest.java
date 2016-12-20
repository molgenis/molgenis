package org.molgenis.data.elasticsearch.index;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.elasticsearch.bootstrap.IndexBootstrapper;
import org.molgenis.data.elasticsearch.index.job.IndexJobExecution;
import org.molgenis.data.elasticsearch.index.job.IndexJobExecutionMeta;
import org.molgenis.data.index.IndexActionRegisterService;
import org.molgenis.data.index.meta.IndexAction;
import org.molgenis.data.index.meta.IndexActionMetaData;
import org.molgenis.data.jobs.model.JobExecutionMetaData;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.AttributeMetadata;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.data.jobs.model.JobExecutionMetaData.FAILED;

@ContextConfiguration(classes = { IndexBootstrapperTest.Config.class })
public class IndexBootstrapperTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private Config config;
	@Autowired
	private MetaDataService metaDataService;
	@Autowired
	private SearchService searchService;
	@Autowired
	private IndexActionRegisterService indexActionRegisterService;
	@Autowired
	private DataService dataService;

	IndexBootstrapper indexBootstrapper;

	@BeforeMethod
	public void beforeMethod()
	{
		initMocks(this);
		config.resetMocks();

		indexBootstrapper = new IndexBootstrapper(metaDataService, searchService, indexActionRegisterService,
				dataService);
	}

	@Test
	public void testStartupNoIndex()
	{
		@SuppressWarnings("unchecked")
		Repository<Entity> repo1 = mock(Repository.class);
		when(repo1.getName()).thenReturn("repo1");
		@SuppressWarnings("unchecked")
		Repository<Entity> repo2 = mock(Repository.class);
		when(repo2.getName()).thenReturn("repo2");
		@SuppressWarnings("unchecked")
		Repository<Entity> repo3 = mock(Repository.class);
		when(repo3.getName()).thenReturn("repo3");

		List<Repository<Entity>> repos = Arrays.asList(repo1, repo2, repo3);

		when(searchService.hasMapping(AttributeMetadata.ATTRIBUTE_META_DATA)).thenReturn(false);
		when(metaDataService.getRepositories()).thenReturn(repos.stream());
		indexBootstrapper.bootstrap();

		//verify that new jobs are registered for all repos
		verify(indexActionRegisterService).register("repo1", null);
		verify(indexActionRegisterService).register("repo2", null);
		verify(indexActionRegisterService).register("repo3", null);
	}

	@Test
	public void testStartupFailedIndexJobs()
	{
		when(searchService.hasMapping(AttributeMetadata.ATTRIBUTE_META_DATA)).thenReturn(true);
		IndexJobExecution indexJobExecution = mock(IndexJobExecution.class);
		when(indexJobExecution.getIndexActionJobID()).thenReturn("id");
		IndexAction action = mock(IndexAction.class);
		when(action.getEntityFullName()).thenReturn("myEntity");
		when(action.getEntityId()).thenReturn("myEntityId");

		when(dataService.findAll(IndexJobExecutionMeta.INDEX_JOB_EXECUTION,
				new QueryImpl<IndexJobExecution>().eq(JobExecutionMetaData.STATUS, FAILED), IndexJobExecution.class))
				.thenReturn(Stream.of(indexJobExecution));
		when(dataService.findAll(IndexActionMetaData.INDEX_ACTION,
				new QueryImpl<IndexAction>().eq(IndexActionMetaData.INDEX_ACTION_GROUP_ATTR, "id"), IndexAction.class))
				.thenReturn(Stream.of(action));

		indexBootstrapper.bootstrap();

		//verify that we are not passing through the "missing index" code
		verify(metaDataService, never()).getRepositories();
		//verify that a new job is registered for the failed one
		verify(indexActionRegisterService).register("myEntity", "myEntityId");
	}

	@Test
	public void testStartupAllIsFine()
	{
		when(searchService.hasMapping(AttributeMetadata.ATTRIBUTE_META_DATA)).thenReturn(true);

		when(dataService.findAll(IndexJobExecutionMeta.INDEX_JOB_EXECUTION,
				new QueryImpl<IndexJobExecution>().eq(JobExecutionMetaData.STATUS, FAILED),
				IndexJobExecution.class))
				.thenReturn(Collections.<IndexJobExecution>emptyList().stream());
		indexBootstrapper.bootstrap();

		//verify that no new jobs are registered
		verify(indexActionRegisterService, never()).register(Mockito.anyString(), Mockito.anyString());
	}

	@Configuration
	public static class Config
	{
		@Mock
		DataService dataService;

		@Mock
		SearchService searchService;

		@Mock
		IndexActionRegisterService indexActionRegisterService;

		@Mock
		MetaDataService metaDataService;

		public Config()
		{
			initMocks(this);
		}

		@Bean
		public DataService dataService()
		{
			return dataService;
		}

		@Bean
		public SearchService searchService()
		{
			return searchService;
		}

		@Bean
		public IndexActionRegisterService indexActionRegisterService()
		{
			return indexActionRegisterService;
		}

		@Bean
		public MetaDataService metaDataService()
		{
			return metaDataService;
		}

		void resetMocks()
		{
			reset(dataService, searchService, indexActionRegisterService, metaDataService);
		}

	}
}
