package org.molgenis.data.index;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.EnumSet;
import java.util.stream.Stream;

import static java.util.Collections.singleton;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.molgenis.data.RepositoryCapability.INDEXABLE;
import static org.molgenis.data.RepositoryCapability.MANAGABLE;
import static org.testng.Assert.assertEquals;

public class IndexActionRepositoryDecoratorTest
{
	private Repository<Entity> decoratedRepo;
	private IndexActionRegisterService indexActionRegisterService;
	private IndexActionRepositoryDecorator indexActionRepositoryDecorator;
	private EntityType entityType;
	private EntityType mappedByEntity;
	private EntityType inversedByEntity;

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void setUpBeforeMethod()
	{
		decoratedRepo = mock(Repository.class);
		when(decoratedRepo.getName()).thenReturn("entity");
		when(decoratedRepo.getCapabilities()).thenReturn(singleton(MANAGABLE));
		entityType = mock(EntityType.class);
		when(decoratedRepo.getEntityType()).thenReturn(entityType);
		indexActionRegisterService = mock(IndexActionRegisterService.class);
		indexActionRepositoryDecorator = new IndexActionRepositoryDecorator(decoratedRepo, indexActionRegisterService);
	}

	@Test
	public void updateEntity()
	{
		initEntityMeta();

		Entity entity0 = mock(Entity.class);
		when(entity0.getIdValue()).thenReturn("1");
		indexActionRepositoryDecorator.update(entity0);
		verify(decoratedRepo, times(1)).update(entity0);
		verify(indexActionRegisterService).register(entityType, "1");
		verifyNoMoreInteractions(indexActionRegisterService);
	}

	@Test
	public void updateEntityBidi()
	{
		initEntityMetaBidi();

		Entity entity0 = mock(Entity.class);
		when(entity0.getIdValue()).thenReturn("1");
		indexActionRepositoryDecorator.update(entity0);
		verify(decoratedRepo, times(1)).update(entity0);
		verify(indexActionRegisterService).register(entityType, "1");
		verify(indexActionRegisterService).register(mappedByEntity, null);
		verify(indexActionRegisterService).register(inversedByEntity, null);
		verifyNoMoreInteractions(indexActionRegisterService);
	}

	@Test
	public void getCapabilities()
	{
		assertEquals(indexActionRepositoryDecorator.getCapabilities(), EnumSet.of(INDEXABLE, MANAGABLE));
	}

	@Test
	public void updateStreamEntities()
	{
		initEntityMeta();

		Stream<Entity> entities = Stream.empty();
		indexActionRepositoryDecorator.update(entities);
		verify(decoratedRepo, times(1)).update(entities);
		verify(indexActionRegisterService).register(entityType, null);
		verifyNoMoreInteractions(indexActionRegisterService);
	}

	@Test
	public void updateStreamEntitiesBidi()
	{
		initEntityMetaBidi();

		Stream<Entity> entities = Stream.empty();
		indexActionRepositoryDecorator.update(entities);
		verify(decoratedRepo, times(1)).update(entities);
		verify(indexActionRegisterService).register(entityType, null);
		verify(indexActionRegisterService).register(mappedByEntity, null);
		verify(indexActionRegisterService).register(inversedByEntity, null);
		verifyNoMoreInteractions(indexActionRegisterService);
	}

	@Test
	public void deleteEntity()
	{
		initEntityMeta();

		Entity entity0 = mock(Entity.class);
		when(entity0.getIdValue()).thenReturn("1");
		indexActionRepositoryDecorator.delete(entity0);
		verify(decoratedRepo, times(1)).delete(entity0);
		verify(indexActionRegisterService).register(entityType, "1");
		verifyNoMoreInteractions(indexActionRegisterService);
	}

	@Test
	public void deleteEntityBidi()
	{
		initEntityMetaBidi();

		Entity mappedByEntity0 = mock(Entity.class);
		when(mappedByEntity0.getIdValue()).thenReturn("mappedBy0");
		Entity inversedByEntity0 = mock(Entity.class);
		when(inversedByEntity0.getIdValue()).thenReturn("inversedBy0");

		Entity entity0 = mock(Entity.class);
		when(entity0.getEntities("mappedByAttr")).thenReturn(singleton(mappedByEntity0));
		when(entity0.getEntity("inversedByAttr")).thenReturn(inversedByEntity0);
		when(entity0.getIdValue()).thenReturn("1");
		indexActionRepositoryDecorator.delete(entity0);
		verify(decoratedRepo, times(1)).delete(entity0);
		verify(indexActionRegisterService).register(entityType, "1");
		verify(indexActionRegisterService).register(mappedByEntity, "mappedBy0");
		verify(indexActionRegisterService).register(inversedByEntity, "inversedBy0");
		verifyNoMoreInteractions(indexActionRegisterService);
	}

	@Test
	public void deleteStreamEntities()
	{
		initEntityMeta();

		Stream<Entity> entities = Stream.empty();
		indexActionRepositoryDecorator.delete(entities);
		verify(decoratedRepo, times(1)).delete(entities);
		verify(indexActionRegisterService, times(1)).register(entityType, null);
		verifyNoMoreInteractions(indexActionRegisterService);
	}

	@Test
	public void deleteStreamEntitiesBidi()
	{
		initEntityMetaBidi();

		Stream<Entity> entities = Stream.empty();
		indexActionRepositoryDecorator.delete(entities);
		verify(decoratedRepo, times(1)).delete(entities);
		verify(indexActionRegisterService, times(1)).register(entityType, null);
		verify(indexActionRegisterService).register(mappedByEntity, null);
		verify(indexActionRegisterService).register(inversedByEntity, null);
		verifyNoMoreInteractions(indexActionRegisterService);
	}

	@Test
	public void deleteEntityById()
	{
		initEntityMeta();

		Entity entity0 = mock(Entity.class);
		when(entity0.getIdValue()).thenReturn("1");
		indexActionRepositoryDecorator.deleteById("1");
		verify(decoratedRepo, times(1)).deleteById("1");
		verify(indexActionRegisterService).register(entityType, "1");
		verifyNoMoreInteractions(indexActionRegisterService);
	}

	@Test
	public void deleteEntityByIdBidi()
	{
		initEntityMetaBidi();

		Entity entity0 = mock(Entity.class);
		when(entity0.getIdValue()).thenReturn("1");
		indexActionRepositoryDecorator.deleteById("1");
		verify(decoratedRepo, times(1)).deleteById("1");
		verify(indexActionRegisterService).register(entityType, "1");
		verify(indexActionRegisterService).register(mappedByEntity, null);
		verify(indexActionRegisterService).register(inversedByEntity, null);
		verifyNoMoreInteractions(indexActionRegisterService);
	}

	@Test
	public void deleteEntityByIdStream()
	{
		initEntityMeta();

		Stream<Object> ids = Stream.empty();
		indexActionRepositoryDecorator.deleteAll(ids);
		verify(decoratedRepo, times(1)).deleteAll(ids);
		verify(indexActionRegisterService, times(1)).register(entityType, null);
		verifyNoMoreInteractions(indexActionRegisterService);
	}

	@Test
	public void deleteEntityByIdStreamBidi()
	{
		initEntityMetaBidi();

		Stream<Object> ids = Stream.empty();
		indexActionRepositoryDecorator.deleteAll(ids);
		verify(decoratedRepo, times(1)).deleteAll(ids);
		verify(indexActionRegisterService, times(1)).register(entityType, null);
		verify(indexActionRegisterService).register(mappedByEntity, null);
		verify(indexActionRegisterService).register(inversedByEntity, null);
		verifyNoMoreInteractions(indexActionRegisterService);
	}

	@Test
	public void deleteAll()
	{
		initEntityMeta();

		indexActionRepositoryDecorator.deleteAll();
		verify(decoratedRepo, times(1)).deleteAll();
		verify(indexActionRegisterService, times(1)).register(entityType, null);
		verifyNoMoreInteractions(indexActionRegisterService);
	}

	@Test
	public void deleteAllBidi()
	{
		initEntityMetaBidi();

		indexActionRepositoryDecorator.deleteAll();
		verify(decoratedRepo, times(1)).deleteAll();
		verify(indexActionRegisterService, times(1)).register(entityType, null);
		verify(indexActionRegisterService).register(mappedByEntity, null);
		verify(indexActionRegisterService).register(inversedByEntity, null);
		verifyNoMoreInteractions(indexActionRegisterService);
	}

	@Test
	public void addEntity()
	{
		initEntityMeta();

		Entity entity0 = mock(Entity.class);
		when(entity0.getIdValue()).thenReturn("1");
		indexActionRepositoryDecorator.add(entity0);
		verify(decoratedRepo, times(1)).add(entity0);
		verify(indexActionRegisterService).register(entityType, "1");
		verifyNoMoreInteractions(indexActionRegisterService);
	}

	@Test
	public void addEntityBidi()
	{
		initEntityMetaBidi();

		Entity mappedByEntity0 = mock(Entity.class);
		when(mappedByEntity0.getIdValue()).thenReturn("mappedBy0");
		Entity inversedByEntity0 = mock(Entity.class);
		when(inversedByEntity0.getIdValue()).thenReturn("inversedBy0");

		Entity entity0 = mock(Entity.class);
		when(entity0.getEntities("mappedByAttr")).thenReturn(singleton(mappedByEntity0));
		when(entity0.getEntity("inversedByAttr")).thenReturn(inversedByEntity0);
		when(entity0.getIdValue()).thenReturn("1");
		indexActionRepositoryDecorator.add(entity0);
		verify(decoratedRepo, times(1)).add(entity0);
		verify(indexActionRegisterService).register(entityType, "1");
		verify(indexActionRegisterService).register(mappedByEntity, "mappedBy0");
		verify(indexActionRegisterService).register(inversedByEntity, "inversedBy0");
		verifyNoMoreInteractions(indexActionRegisterService);
	}

	@Test
	public void addEntitiesStream()
	{
		initEntityMeta();

		Stream<Entity> entities = Stream.empty();
		when(decoratedRepo.add(entities)).thenReturn(123);
		assertEquals(indexActionRepositoryDecorator.add(entities), Integer.valueOf(123));
		verify(decoratedRepo, times(1)).add(entities);
		verify(indexActionRegisterService).register(entityType, null);
		verifyNoMoreInteractions(indexActionRegisterService);
	}

	@Test
	public void addEntitiesStreamBidi()
	{
		initEntityMetaBidi();

		Stream<Entity> entities = Stream.empty();
		when(decoratedRepo.add(entities)).thenReturn(123);
		assertEquals(indexActionRepositoryDecorator.add(entities), Integer.valueOf(123));
		verify(decoratedRepo, times(1)).add(entities);
		verify(indexActionRegisterService).register(entityType, null);
		verify(indexActionRegisterService).register(mappedByEntity, null);
		verify(indexActionRegisterService).register(inversedByEntity, null);
		verifyNoMoreInteractions(indexActionRegisterService);
	}

	private void initEntityMeta()
	{
		when(entityType.getMappedByAttributes()).thenReturn(Stream.empty());
		when(entityType.getInversedByAttributes()).thenReturn(Stream.empty());
	}

	private void initEntityMetaBidi()
	{
		mappedByEntity = mock(EntityType.class);
		when(mappedByEntity.getId()).thenReturn("mappedByEntity");
		Attribute mappedByAttr = mock(Attribute.class);
		when(mappedByAttr.getName()).thenReturn("mappedByAttr");
		when(mappedByAttr.getRefEntity()).thenReturn(mappedByEntity);

		inversedByEntity = mock(EntityType.class);
		when(inversedByEntity.getId()).thenReturn("inversedByEntity");
		Attribute inversedByAttr = mock(Attribute.class);
		when(inversedByAttr.getName()).thenReturn("inversedByAttr");
		when(inversedByAttr.getRefEntity()).thenReturn(inversedByEntity);

		when(entityType.getMappedByAttributes()).thenReturn(Stream.of(mappedByAttr));
		when(entityType.getInversedByAttributes()).thenReturn(Stream.of(inversedByAttr));
	}
}
