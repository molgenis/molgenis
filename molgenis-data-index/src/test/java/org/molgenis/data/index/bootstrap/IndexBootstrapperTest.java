package org.molgenis.data.index.bootstrap;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.index.IndexActionRegisterService;
import org.molgenis.data.index.IndexService;
import org.molgenis.data.index.job.IndexJobExecution;
import org.molgenis.data.index.job.IndexJobExecutionMeta;
import org.molgenis.data.index.meta.IndexAction;
import org.molgenis.data.index.meta.IndexActionMetaData;
import org.molgenis.data.jobs.model.JobExecutionMetaData;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.AttributeMetadata;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.support.QueryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
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
	private IndexService indexService;
	@Autowired
	private IndexActionRegisterService indexActionRegisterService;
	@Autowired
	private DataService dataService;
	@Autowired
	private AttributeMetadata attributeMetadata;
	@Autowired
	private EntityTypeFactory entityTypeFactory;

	private IndexBootstrapper indexBootstrapper;

	@BeforeMethod
	public void beforeMethod()
	{
		config.resetMocks();

		indexBootstrapper = new IndexBootstrapper(metaDataService, indexService, indexActionRegisterService,
				dataService, attributeMetadata, entityTypeFactory);
	}

	@Test
	public void testStartupNoIndex()
	{
		@SuppressWarnings("unchecked")
		Repository<Entity> repo1 = mock(Repository.class);
		EntityType entityType1 = mock(EntityType.class);
		when(repo1.getEntityType()).thenReturn(entityType1);
		@SuppressWarnings("unchecked")
		Repository<Entity> repo2 = mock(Repository.class);
		EntityType entityType2 = mock(EntityType.class);
		when(repo2.getEntityType()).thenReturn(entityType2);
		@SuppressWarnings("unchecked")
		Repository<Entity> repo3 = mock(Repository.class);
		EntityType entityType3 = mock(EntityType.class);
		when(repo3.getEntityType()).thenReturn(entityType3);

		List<Repository<Entity>> repos = Arrays.asList(repo1, repo2, repo3);

		when(indexService.hasIndex(attributeMetadata)).thenReturn(false);
		when(metaDataService.getRepositories()).thenReturn(repos.stream());
		indexBootstrapper.bootstrap();

		//verify that new jobs are registered for all repos
		verify(indexActionRegisterService).register(entityType1, null);
		verify(indexActionRegisterService).register(entityType2, null);
		verify(indexActionRegisterService).register(entityType3, null);
	}

	@Test
	public void testStartupFailedIndexJobs()
	{
		when(indexService.hasIndex(attributeMetadata)).thenReturn(true);
		IndexJobExecution indexJobExecution = mock(IndexJobExecution.class);
		when(indexJobExecution.getIndexActionJobID()).thenReturn("id");
		IndexAction action = mock(IndexAction.class);
		when(action.getEntityTypeId()).thenReturn("myEntityTypeName");
		when(action.getEntityId()).thenReturn("myEntityId");
		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn("myEntityTypeName");
		when(entityTypeFactory.create("myEntityTypeName")).thenReturn(entityType);
		when(dataService.findAll(IndexJobExecutionMeta.INDEX_JOB_EXECUTION,
				new QueryImpl<IndexJobExecution>().eq(JobExecutionMetaData.STATUS, FAILED),
				IndexJobExecution.class)).thenReturn(Stream.of(indexJobExecution));
		when(dataService.findAll(IndexActionMetaData.INDEX_ACTION,
				new QueryImpl<IndexAction>().eq(IndexActionMetaData.INDEX_ACTION_GROUP_ATTR, "id"),
				IndexAction.class)).thenReturn(Stream.of(action));

		indexBootstrapper.bootstrap();

		//verify that we are not passing through the "missing index" code
		verify(metaDataService, never()).getRepositories();
		//verify that a new job is registered for the failed one
		verify(indexActionRegisterService).register(entityType, "myEntityId");
	}

	@Test
	public void testStartupAllIsFine()
	{
		when(indexService.hasIndex(attributeMetadata)).thenReturn(true);

		when(dataService.findAll(IndexJobExecutionMeta.INDEX_JOB_EXECUTION,
				new QueryImpl<IndexJobExecution>().eq(JobExecutionMetaData.STATUS, FAILED),
				IndexJobExecution.class)).thenReturn(Stream.empty());
		indexBootstrapper.bootstrap();

		//verify that no new jobs are registered
		verify(indexActionRegisterService, never()).register(any(EntityType.class), Mockito.anyString());
	}

	@Configuration
	public static class Config
	{
		@Mock
		IndexService indexService;

		@Mock
		IndexActionRegisterService indexActionRegisterService;

		@Mock
		MetaDataService metaDataService;

		@Mock
		AttributeMetadata attributeMetadata;

		@Mock
		EntityTypeFactory entityTypeFactory;

		public Config()
		{
			initMocks(this);
		}

		@Bean
		public IndexService indexService()
		{
			return indexService;
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

		@Bean
		public EntityTypeFactory entityTypeFactory()
		{
			return entityTypeFactory;
		}

		void resetMocks()
		{
			reset(indexService, indexActionRegisterService, metaDataService, attributeMetadata, entityTypeFactory);
		}
	}
}
