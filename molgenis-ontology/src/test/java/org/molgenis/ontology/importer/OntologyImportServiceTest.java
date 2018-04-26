package org.molgenis.ontology.importer;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.data.*;
import org.molgenis.data.importer.EntityImportReport;
import org.molgenis.ontology.core.importer.OntologyImportService;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.stream.Stream;

import static com.google.common.collect.ImmutableMap.of;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

public class OntologyImportServiceTest extends AbstractMockitoTest
{
	@Mock
	private DataService dataService;

	private OntologyImportService ontologyImportService;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		this.ontologyImportService = new OntologyImportService(dataService);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testDoImport()
	{
		String entityTypeId0 = "entityTypeId0";
		String entityTypeId1 = "entityTypeId1";
		Entity entity0 = mock(Entity.class);
		Repository<Entity> sourceRepository0 = mock(Repository.class);
		when(sourceRepository0.spliterator()).thenReturn(singletonList(entity0).spliterator());
		Entity entity1 = mock(Entity.class);
		Repository<Entity> sourceRepository1 = mock(Repository.class);
		when(sourceRepository1.spliterator()).thenReturn(singletonList(entity1).spliterator());
		RepositoryCollection repositoryCollection = mock(RepositoryCollection.class);
		when(repositoryCollection.getEntityTypeIds()).thenReturn(asList(entityTypeId0, entityTypeId1));
		doReturn(sourceRepository0).when(repositoryCollection).getRepository(entityTypeId0);
		doReturn(sourceRepository1).when(repositoryCollection).getRepository(entityTypeId1);

		Repository<Entity> targetRepository0 = mock(Repository.class);
		when(targetRepository0.add(any(Stream.class))).thenReturn(1);
		Repository<Entity> targetRepository1 = mock(Repository.class);
		when(targetRepository1.add(any(Stream.class))).thenReturn(1);
		doReturn(targetRepository0).when(dataService).getRepository(entityTypeId0);
		doReturn(targetRepository1).when(dataService).getRepository(entityTypeId1);

		EntityImportReport entityImportReport = ontologyImportService.doImport(repositoryCollection, DatabaseAction.ADD,
				null);

		assertEquals(entityImportReport.getNewEntities(), emptyList());
		assertEquals(entityImportReport.getNrImportedEntitiesMap(), of(entityTypeId0, 1, entityTypeId1, 1));

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Entity>> entityTypeId0Captor = ArgumentCaptor.forClass(Stream.class);
		verify(targetRepository0).add(entityTypeId0Captor.capture());
		assertEquals(entityTypeId0Captor.getValue().collect(toList()), singletonList(entity0));

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Entity>> entityTypeId1Captor = ArgumentCaptor.forClass(Stream.class);
		verify(targetRepository1).add(entityTypeId1Captor.capture());
		assertEquals(entityTypeId1Captor.getValue().collect(toList()), singletonList(entity1));
	}

}