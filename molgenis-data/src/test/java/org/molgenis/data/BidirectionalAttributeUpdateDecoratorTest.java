package org.molgenis.data;

import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.stream.Stream;

import static freemarker.template.utility.Collections12.singletonList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.*;
import static org.molgenis.MolgenisFieldTypes.AttributeType.ONE_TO_MANY;
import static org.molgenis.MolgenisFieldTypes.AttributeType.XREF;
import static org.testng.Assert.assertEquals;

public class BidirectionalAttributeUpdateDecoratorTest
{
	private EntityMetaData entityMeta;
	private Repository<Entity> decoratedRepo;
	private DataService dataService;
	private BidirectionalAttributeUpdateDecorator bidiAttrUpdateDecorator;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		entityMeta = mock(EntityMetaData.class);
		//noinspection unchecked
		decoratedRepo = mock(Repository.class);
		when(decoratedRepo.getEntityMetaData()).thenReturn(entityMeta);
		dataService = mock(DataService.class);
		bidiAttrUpdateDecorator = new BidirectionalAttributeUpdateDecorator(decoratedRepo, dataService);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void bidirectionalAttributeUpdateDecorator()
	{
		new BidirectionalAttributeUpdateDecorator(null, null);
	}

	@Test
	public void delegate()
	{
		assertEquals(bidiAttrUpdateDecorator.delegate(), decoratedRepo);
	}

	@Test
	public void addEntityInversedByAttrNull()
	{
		when(entityMeta.hasBidirectionalAttributes()).thenReturn(true);

		String oneToManyAttrName = "oneToManyAttr";
		AttributeMetaData oneToManyAttr = mock(AttributeMetaData.class);
		when(oneToManyAttr.getDataType()).thenReturn(ONE_TO_MANY);
		when(oneToManyAttr.getName()).thenReturn(oneToManyAttrName);

		String xrefAttrName = "xrefAttr";
		AttributeMetaData xrefAttr = mock(AttributeMetaData.class);
		when(xrefAttr.getName()).thenReturn(xrefAttrName);
		when(xrefAttr.getDataType()).thenReturn(XREF);

		when(oneToManyAttr.getRefEntity()).thenReturn(entityMeta);
		when(oneToManyAttr.getMappedBy()).thenReturn(xrefAttr);

		EntityMetaData refEntityMeta = mock(EntityMetaData.class);
		when(refEntityMeta.getMappedByAttributes()).thenReturn(Stream.of(xrefAttr));
		when(refEntityMeta.getInversedByAttributes()).thenReturn(Stream.empty());
		when(xrefAttr.getRefEntity()).thenReturn(refEntityMeta);
		when(xrefAttr.getInversedBy()).thenReturn(oneToManyAttr);

		when(entityMeta.getMappedByAttributes()).thenReturn(Stream.empty());
		when(entityMeta.getInversedByAttributes()).thenReturn(Stream.of(xrefAttr));

		Entity entity = mock(Entity.class);
		when(entity.getEntity(xrefAttrName)).thenReturn(null);
		bidiAttrUpdateDecorator.add(entity);
		verify(decoratedRepo, atLeast(1)).getEntityMetaData();
		verify(decoratedRepo).add(entity);
		verifyNoMoreInteractions(decoratedRepo);
		verifyZeroInteractions(dataService);
	}

	@Test
	public void addEntityInversedByAttrNotNull()
	{
		when(entityMeta.hasBidirectionalAttributes()).thenReturn(true);

		String oneToManyAttrName = "oneToManyAttr";
		AttributeMetaData oneToManyAttr = mock(AttributeMetaData.class);
		when(oneToManyAttr.getDataType()).thenReturn(ONE_TO_MANY);
		when(oneToManyAttr.getName()).thenReturn(oneToManyAttrName);

		String xrefAttrName = "xrefAttr";
		AttributeMetaData xrefAttr = mock(AttributeMetaData.class);
		when(xrefAttr.getName()).thenReturn(xrefAttrName);
		when(xrefAttr.getDataType()).thenReturn(XREF);

		when(oneToManyAttr.getRefEntity()).thenReturn(entityMeta);
		when(oneToManyAttr.getMappedBy()).thenReturn(xrefAttr);

		String refEntityName = "refEntity";
		EntityMetaData refEntityMeta = mock(EntityMetaData.class);
		when(refEntityMeta.getName()).thenReturn(refEntityName);
		when(xrefAttr.getRefEntity()).thenReturn(refEntityMeta);
		when(xrefAttr.getInversedBy()).thenReturn(oneToManyAttr);

		when(entityMeta.getMappedByAttributes()).thenReturn(Stream.empty());
		when(entityMeta.getInversedByAttributes()).thenReturn(Stream.of(xrefAttr));

		Entity entity = mock(Entity.class);
		Entity refEntity = mock(Entity.class);
		when(refEntity.getEntities(oneToManyAttrName)).thenReturn(singleton(entity));
		when(entity.getEntity(xrefAttrName)).thenReturn(refEntity);
		bidiAttrUpdateDecorator.add(entity);
		verify(decoratedRepo, atLeast(1)).getEntityMetaData();
		verify(decoratedRepo).add(entity);
		verifyNoMoreInteractions(decoratedRepo);
		verify(dataService).update(refEntityName, refEntity);
		verifyNoMoreInteractions(dataService);
	}

	@Test
	public void addEntityMappedByNull()
	{
		when(entityMeta.hasBidirectionalAttributes()).thenReturn(true);
		when(entityMeta.getName()).thenReturn("entity");

		String oneToManyAttrName = "oneToManyAttr";
		AttributeMetaData oneToManyAttr = mock(AttributeMetaData.class);
		when(oneToManyAttr.getDataType()).thenReturn(ONE_TO_MANY);
		when(oneToManyAttr.getName()).thenReturn(oneToManyAttrName);

		String xrefAttrName = "xrefAttr";
		AttributeMetaData xrefAttr = mock(AttributeMetaData.class);
		when(xrefAttr.getName()).thenReturn(xrefAttrName);
		when(xrefAttr.getDataType()).thenReturn(XREF);

		when(oneToManyAttr.getRefEntity()).thenReturn(entityMeta);
		when(oneToManyAttr.getMappedBy()).thenReturn(xrefAttr);

		EntityMetaData refEntityMeta = mock(EntityMetaData.class);
		when(xrefAttr.getRefEntity()).thenReturn(refEntityMeta);
		when(xrefAttr.getInversedBy()).thenReturn(oneToManyAttr);

		when(entityMeta.getMappedByAttributes()).thenReturn(Stream.of(oneToManyAttr));
		when(entityMeta.getInversedByAttributes()).thenReturn(Stream.empty());

		Entity entity = mock(Entity.class);
		when(entity.getEntities(oneToManyAttrName)).thenReturn(emptyList());
		bidiAttrUpdateDecorator.add(entity);
		verify(decoratedRepo, atLeast(1)).getEntityMetaData();
		verify(decoratedRepo).add(entity);
		verifyNoMoreInteractions(decoratedRepo);
		verifyZeroInteractions(dataService);
	}

	@Test
	public void addEntityMappedByNotNull()
	{
		when(entityMeta.hasBidirectionalAttributes()).thenReturn(true);
		when(entityMeta.getName()).thenReturn("entity");

		String refEntityName = "refEntity";
		EntityMetaData refEntityMeta = mock(EntityMetaData.class);

		String oneToManyAttrName = "oneToManyAttr";
		AttributeMetaData oneToManyAttr = mock(AttributeMetaData.class);
		when(oneToManyAttr.getDataType()).thenReturn(ONE_TO_MANY);
		when(oneToManyAttr.getName()).thenReturn(oneToManyAttrName);

		String xrefAttrName = "xrefAttr";
		AttributeMetaData xrefAttr = mock(AttributeMetaData.class);
		when(xrefAttr.getName()).thenReturn(xrefAttrName);
		when(xrefAttr.getDataType()).thenReturn(XREF);

		when(oneToManyAttr.getRefEntity()).thenReturn(refEntityMeta);
		when(oneToManyAttr.getMappedBy()).thenReturn(xrefAttr);

		when(refEntityMeta.getName()).thenReturn(refEntityName);
		when(xrefAttr.getRefEntity()).thenReturn(entityMeta);
		when(xrefAttr.getInversedBy()).thenReturn(oneToManyAttr);

		when(entityMeta.getMappedByAttributes()).thenReturn(Stream.of(oneToManyAttr));
		when(entityMeta.getInversedByAttributes()).thenReturn(Stream.empty());

		Entity entity = mock(Entity.class);
		Entity refEntity = mock(Entity.class);
		when(entity.getEntities(oneToManyAttrName)).thenReturn(singleton(refEntity));
		when(refEntity.getEntity(xrefAttrName)).thenReturn(entity);
		bidiAttrUpdateDecorator.add(entity);
		verify(decoratedRepo, atLeast(1)).getEntityMetaData();
		verify(decoratedRepo).add(entity);
		verifyNoMoreInteractions(decoratedRepo);
		verify(dataService).update(refEntityName, refEntity);
		verifyNoMoreInteractions(dataService);
	}

	@Test
	public void addEntityNoBidirectionalAttrs()
	{
		when(entityMeta.hasBidirectionalAttributes()).thenReturn(false);
		Entity entity = mock(Entity.class);
		bidiAttrUpdateDecorator.add(entity);
		verify(decoratedRepo).getEntityMetaData();
		verify(decoratedRepo).add(entity);
		verifyNoMoreInteractions(decoratedRepo);
		verifyZeroInteractions(dataService);
	}

	@Test
	public void addStreamBidirectionalAttrs()
	{
		when(entityMeta.hasBidirectionalAttributes()).thenReturn(true);

		String oneToManyAttrName = "oneToManyAttr";
		AttributeMetaData oneToManyAttr = mock(AttributeMetaData.class);
		when(oneToManyAttr.getDataType()).thenReturn(ONE_TO_MANY);
		when(oneToManyAttr.getName()).thenReturn(oneToManyAttrName);

		String xrefAttrName = "xrefAttr";
		AttributeMetaData xrefAttr = mock(AttributeMetaData.class);
		when(xrefAttr.getName()).thenReturn(xrefAttrName);
		when(xrefAttr.getDataType()).thenReturn(XREF);

		when(oneToManyAttr.getRefEntity()).thenReturn(entityMeta);
		when(oneToManyAttr.getMappedBy()).thenReturn(xrefAttr);

		String refEntityName = "refEntity";
		EntityMetaData refEntityMeta = mock(EntityMetaData.class);
		when(refEntityMeta.getName()).thenReturn(refEntityName);
		when(xrefAttr.getRefEntity()).thenReturn(refEntityMeta);
		when(xrefAttr.getInversedBy()).thenReturn(oneToManyAttr);

		when(entityMeta.getMappedByAttributes()).thenReturn(Stream.empty());
		when(entityMeta.getInversedByAttributes()).thenReturn(Stream.of(xrefAttr));

		Entity entity = mock(Entity.class);
		Entity refEntity = mock(Entity.class);
		when(refEntity.getEntities(oneToManyAttrName)).thenReturn(singleton(entity));
		when(entity.getEntity(xrefAttrName)).thenReturn(refEntity);
		bidiAttrUpdateDecorator.add(Stream.of(entity));
		verify(decoratedRepo, atLeast(1)).getEntityMetaData();
		verify(decoratedRepo).add(entity);
		verifyNoMoreInteractions(decoratedRepo);
		verify(dataService).update(refEntityName, refEntity);
		verifyNoMoreInteractions(dataService);
	}

	@Test
	public void addStreamNoBidirectionalAttrs()
	{
		when(entityMeta.hasBidirectionalAttributes()).thenReturn(false);
		Entity entity = mock(Entity.class);
		bidiAttrUpdateDecorator.add(Stream.of(entity));
		verify(decoratedRepo).getEntityMetaData();
		//noinspection unchecked
		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(decoratedRepo).add(captor.capture());
		assertEquals(captor.getValue().collect(toList()), singletonList(entity));
		verifyNoMoreInteractions(decoratedRepo);
		verifyZeroInteractions(dataService);
	}

	@Test
	public void updateEntityInversedByAttrUnchanged()
	{
		when(entityMeta.hasBidirectionalAttributes()).thenReturn(true);

		String oneToManyAttrName = "oneToManyAttr";
		AttributeMetaData oneToManyAttr = mock(AttributeMetaData.class);
		when(oneToManyAttr.getDataType()).thenReturn(ONE_TO_MANY);
		when(oneToManyAttr.getName()).thenReturn(oneToManyAttrName);

		String xrefAttrName = "xrefAttr";
		AttributeMetaData xrefAttr = mock(AttributeMetaData.class);
		when(xrefAttr.getName()).thenReturn(xrefAttrName);
		when(xrefAttr.getDataType()).thenReturn(XREF);

		when(oneToManyAttr.getRefEntity()).thenReturn(entityMeta);
		when(oneToManyAttr.getMappedBy()).thenReturn(xrefAttr);

		EntityMetaData refEntityMeta = mock(EntityMetaData.class);
		when(refEntityMeta.getMappedByAttributes()).thenReturn(Stream.of(xrefAttr));
		when(refEntityMeta.getInversedByAttributes()).thenReturn(Stream.empty());
		when(xrefAttr.getRefEntity()).thenReturn(refEntityMeta);
		when(xrefAttr.getInversedBy()).thenReturn(oneToManyAttr);

		when(entityMeta.getMappedByAttributes()).thenReturn(Stream.empty());
		when(entityMeta.getInversedByAttributes()).thenReturn(Stream.of(xrefAttr));

		Entity entity = mock(Entity.class);
		Object id = mock(Object.class);
		when(entity.getIdValue()).thenReturn(id);
		when(entity.getEntity(xrefAttrName)).thenReturn(null);

		Entity existingEntity = mock(Entity.class);
		when(existingEntity.getIdValue()).thenReturn(id);
		when(existingEntity.getEntity(xrefAttrName)).thenReturn(null);

		when(decoratedRepo.findOneById(id)).thenReturn(existingEntity);
		bidiAttrUpdateDecorator.update(entity);
		verify(decoratedRepo, atLeast(1)).getEntityMetaData();
		verify(decoratedRepo).findOneById(id);
		verify(decoratedRepo).update(entity);
		verifyNoMoreInteractions(decoratedRepo);
		verifyZeroInteractions(dataService);
	}

	@Test
	public void updateEntityInversedByAttrChanged()
	{
		when(entityMeta.hasBidirectionalAttributes()).thenReturn(true);
		when(entityMeta.getName()).thenReturn("entity");

		String oneToManyAttrName = "oneToManyAttr";
		AttributeMetaData oneToManyAttr = mock(AttributeMetaData.class);
		when(oneToManyAttr.getDataType()).thenReturn(ONE_TO_MANY);
		when(oneToManyAttr.getName()).thenReturn(oneToManyAttrName);

		String xrefAttrName = "xrefAttr";
		AttributeMetaData xrefAttr = mock(AttributeMetaData.class);
		when(xrefAttr.getName()).thenReturn(xrefAttrName);
		when(xrefAttr.getDataType()).thenReturn(XREF);

		when(oneToManyAttr.getRefEntity()).thenReturn(entityMeta);
		when(oneToManyAttr.getMappedBy()).thenReturn(xrefAttr);

		String refEntityName = "refEntity";
		EntityMetaData refEntityMeta = mock(EntityMetaData.class);
		when(refEntityMeta.getName()).thenReturn(refEntityName);
		when(refEntityMeta.getMappedByAttributes()).thenReturn(Stream.of(xrefAttr));
		when(refEntityMeta.getInversedByAttributes()).thenReturn(Stream.empty());
		when(xrefAttr.getRefEntity()).thenReturn(refEntityMeta);
		when(xrefAttr.getInversedBy()).thenReturn(oneToManyAttr);

		when(entityMeta.getMappedByAttributes()).thenReturn(Stream.empty());
		when(entityMeta.getInversedByAttributes()).thenReturn(Stream.of(xrefAttr));

		Entity entity = mock(Entity.class);
		Object id = mock(Object.class);
		when(entity.getIdValue()).thenReturn(id);
		when(entity.getEntity(xrefAttrName)).thenReturn(null);

		Entity refEntity = mock(Entity.class);
		when(refEntity.getEntities(oneToManyAttrName)).thenReturn(singleton(entity));

		Entity existingEntity = mock(Entity.class);
		when(existingEntity.getIdValue()).thenReturn(id);
		when(existingEntity.getEntity(xrefAttrName)).thenReturn(refEntity);

		when(decoratedRepo.findOneById(id)).thenReturn(existingEntity);
		bidiAttrUpdateDecorator.update(entity);
		verify(decoratedRepo, atLeast(1)).getEntityMetaData();
		verify(decoratedRepo).findOneById(id);
		verify(decoratedRepo).update(entity);
		verifyNoMoreInteractions(decoratedRepo);
		verify(dataService).update(refEntityName, refEntity);
		verifyNoMoreInteractions(dataService);
	}

	@Test
	public void updateEntityMappedByAttrChanged()
	{
		when(entityMeta.hasBidirectionalAttributes()).thenReturn(true);
		when(entityMeta.getName()).thenReturn("entity");

		String refEntityName = "refEntity";
		EntityMetaData refEntityMeta = mock(EntityMetaData.class);

		String oneToManyAttrName = "oneToManyAttr";
		AttributeMetaData oneToManyAttr = mock(AttributeMetaData.class);
		when(oneToManyAttr.getDataType()).thenReturn(ONE_TO_MANY);
		when(oneToManyAttr.getName()).thenReturn(oneToManyAttrName);

		String xrefAttrName = "xrefAttr";
		AttributeMetaData xrefAttr = mock(AttributeMetaData.class);
		when(xrefAttr.getName()).thenReturn(xrefAttrName);
		when(xrefAttr.getDataType()).thenReturn(XREF);

		when(oneToManyAttr.getRefEntity()).thenReturn(refEntityMeta);
		when(oneToManyAttr.getMappedBy()).thenReturn(xrefAttr);

		when(refEntityMeta.getName()).thenReturn(refEntityName);
		when(xrefAttr.getRefEntity()).thenReturn(entityMeta);
		when(xrefAttr.getInversedBy()).thenReturn(oneToManyAttr);

		when(entityMeta.getMappedByAttributes()).thenReturn(Stream.of(oneToManyAttr));
		when(entityMeta.getInversedByAttributes()).thenReturn(Stream.empty());

		Entity entity = mock(Entity.class);
		Object id = mock(Object.class);
		when(entity.getIdValue()).thenReturn(id);
		when(entity.getEntities(oneToManyAttrName)).thenReturn(emptyList());
		Entity refEntity = mock(Entity.class);
		Object refId = mock(Object.class);
		when(refEntity.getIdValue()).thenReturn(refId);
		when(refEntity.getEntity(xrefAttrName)).thenReturn(entity);

		Entity existingEntity = mock(Entity.class);
		when(existingEntity.getIdValue()).thenReturn(id);
		when(existingEntity.getEntities(oneToManyAttrName)).thenReturn(singleton(refEntity));

		when(decoratedRepo.findOneById(id)).thenReturn(existingEntity);
		bidiAttrUpdateDecorator.update(entity);
		verify(decoratedRepo, atLeast(1)).getEntityMetaData();
		verify(decoratedRepo).update(entity);
		verify(decoratedRepo).findOneById(id);
		verifyNoMoreInteractions(decoratedRepo);
		verify(dataService).update(refEntityName, refEntity);
		verifyNoMoreInteractions(dataService);
	}

	@Test
	public void updateEntityNoBidirectionalAttrs()
	{
		when(entityMeta.hasBidirectionalAttributes()).thenReturn(false);
		Entity entity = mock(Entity.class);
		bidiAttrUpdateDecorator.update(entity);
		verify(decoratedRepo).getEntityMetaData();
		verify(decoratedRepo).update(entity);
		verifyNoMoreInteractions(decoratedRepo);
		verifyZeroInteractions(dataService);
	}

	@Test
	public void updateStreamBidirectionalAttrs()
	{
		when(entityMeta.hasBidirectionalAttributes()).thenReturn(true);
		when(entityMeta.getName()).thenReturn("entity");

		String refEntityName = "refEntity";
		EntityMetaData refEntityMeta = mock(EntityMetaData.class);

		String oneToManyAttrName = "oneToManyAttr";
		AttributeMetaData oneToManyAttr = mock(AttributeMetaData.class);
		when(oneToManyAttr.getDataType()).thenReturn(ONE_TO_MANY);
		when(oneToManyAttr.getName()).thenReturn(oneToManyAttrName);

		String xrefAttrName = "xrefAttr";
		AttributeMetaData xrefAttr = mock(AttributeMetaData.class);
		when(xrefAttr.getName()).thenReturn(xrefAttrName);
		when(xrefAttr.getDataType()).thenReturn(XREF);

		when(oneToManyAttr.getRefEntity()).thenReturn(refEntityMeta);
		when(oneToManyAttr.getMappedBy()).thenReturn(xrefAttr);

		when(refEntityMeta.getName()).thenReturn(refEntityName);
		when(xrefAttr.getRefEntity()).thenReturn(entityMeta);
		when(xrefAttr.getInversedBy()).thenReturn(oneToManyAttr);

		when(entityMeta.getMappedByAttributes()).thenReturn(Stream.of(oneToManyAttr));
		when(entityMeta.getInversedByAttributes()).thenReturn(Stream.empty());

		Entity entity = mock(Entity.class);
		Object id = mock(Object.class);
		when(entity.getIdValue()).thenReturn(id);
		when(entity.getEntities(oneToManyAttrName)).thenReturn(emptyList());
		Entity refEntity = mock(Entity.class);
		Object refId = mock(Object.class);
		when(refEntity.getIdValue()).thenReturn(refId);
		when(refEntity.getEntity(xrefAttrName)).thenReturn(entity);

		Entity existingEntity = mock(Entity.class);
		when(existingEntity.getIdValue()).thenReturn(id);
		when(existingEntity.getEntities(oneToManyAttrName)).thenReturn(singleton(refEntity));

		when(decoratedRepo.findOneById(id)).thenReturn(existingEntity);
		bidiAttrUpdateDecorator.update(Stream.of(entity));
		verify(decoratedRepo, atLeast(1)).getEntityMetaData();
		verify(decoratedRepo).update(entity);
		verify(decoratedRepo).findOneById(id);
		verifyNoMoreInteractions(decoratedRepo);
		verify(dataService).update(refEntityName, refEntity);
		verifyNoMoreInteractions(dataService);
	}

	@Test
	public void updateStreamNoBidirectionalAttrs()
	{
		when(entityMeta.hasBidirectionalAttributes()).thenReturn(false);
		Entity entity = mock(Entity.class);
		bidiAttrUpdateDecorator.update(Stream.of(entity));
		verify(decoratedRepo).getEntityMetaData();
		//noinspection unchecked
		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(decoratedRepo).update(captor.capture());
		assertEquals(captor.getValue().collect(toList()), singletonList(entity));
		verifyNoMoreInteractions(decoratedRepo);
		verifyZeroInteractions(dataService);
	}

	@Test
	public void deleteEntityInversedByAttrNull()
	{
		when(entityMeta.hasBidirectionalAttributes()).thenReturn(true);

		String oneToManyAttrName = "oneToManyAttr";
		AttributeMetaData oneToManyAttr = mock(AttributeMetaData.class);
		when(oneToManyAttr.getDataType()).thenReturn(ONE_TO_MANY);
		when(oneToManyAttr.getName()).thenReturn(oneToManyAttrName);

		String xrefAttrName = "xrefAttr";
		AttributeMetaData xrefAttr = mock(AttributeMetaData.class);
		when(xrefAttr.getName()).thenReturn(xrefAttrName);
		when(xrefAttr.getDataType()).thenReturn(XREF);

		when(oneToManyAttr.getRefEntity()).thenReturn(entityMeta);
		when(oneToManyAttr.getMappedBy()).thenReturn(xrefAttr);

		EntityMetaData refEntityMeta = mock(EntityMetaData.class);
		when(refEntityMeta.getMappedByAttributes()).thenReturn(Stream.of(xrefAttr));
		when(refEntityMeta.getInversedByAttributes()).thenReturn(Stream.empty());
		when(xrefAttr.getRefEntity()).thenReturn(refEntityMeta);
		when(xrefAttr.getInversedBy()).thenReturn(oneToManyAttr);

		when(entityMeta.getMappedByAttributes()).thenReturn(Stream.empty());
		when(entityMeta.getInversedByAttributes()).thenReturn(Stream.of(xrefAttr));

		Entity entity = mock(Entity.class);
		when(entity.getEntity(xrefAttrName)).thenReturn(null);
		bidiAttrUpdateDecorator.delete(entity);
		verify(decoratedRepo, atLeast(1)).getEntityMetaData();
		verify(decoratedRepo).delete(entity);
		verifyNoMoreInteractions(decoratedRepo);
		verifyZeroInteractions(dataService);
	}

	@Test
	public void deleteEntityInversedByAttrNotNull()
	{
		when(entityMeta.hasBidirectionalAttributes()).thenReturn(true);

		String oneToManyAttrName = "oneToManyAttr";
		AttributeMetaData oneToManyAttr = mock(AttributeMetaData.class);
		when(oneToManyAttr.getDataType()).thenReturn(ONE_TO_MANY);
		when(oneToManyAttr.getName()).thenReturn(oneToManyAttrName);

		String xrefAttrName = "xrefAttr";
		AttributeMetaData xrefAttr = mock(AttributeMetaData.class);
		when(xrefAttr.getName()).thenReturn(xrefAttrName);
		when(xrefAttr.getDataType()).thenReturn(XREF);

		when(oneToManyAttr.getRefEntity()).thenReturn(entityMeta);
		when(oneToManyAttr.getMappedBy()).thenReturn(xrefAttr);

		String refEntityName = "refEntity";
		EntityMetaData refEntityMeta = mock(EntityMetaData.class);
		when(refEntityMeta.getName()).thenReturn(refEntityName);
		when(xrefAttr.getRefEntity()).thenReturn(refEntityMeta);
		when(xrefAttr.getInversedBy()).thenReturn(oneToManyAttr);

		when(entityMeta.getMappedByAttributes()).thenAnswer(new Answer<Stream<AttributeMetaData>>()
		{
			@Override
			public Stream<AttributeMetaData> answer(InvocationOnMock invocation) throws Throwable
			{
				return Stream.empty();
			}
		});
		when(entityMeta.getInversedByAttributes()).thenAnswer(new Answer<Stream<AttributeMetaData>>()
		{
			@Override
			public Stream<AttributeMetaData> answer(InvocationOnMock invocation) throws Throwable
			{
				return Stream.of(xrefAttr);
			}
		});
		Entity entity = mock(Entity.class);
		Entity existingEntity = mock(Entity.class);
		Entity refEntity = mock(Entity.class);
		Object refId = mock(Object.class);
		when(refEntity.getIdValue()).thenReturn(refId);
		Object id = mock(Object.class);
		when(entity.getIdValue()).thenReturn(id);
		when(entity.getEntity(xrefAttrName)).thenReturn(refEntity).thenReturn(null);

		when(existingEntity.getIdValue()).thenReturn(id);
		when(existingEntity.getEntity(xrefAttrName)).thenReturn(refEntity);

		when(refEntity.getEntities(oneToManyAttrName)).thenReturn(singleton(entity));

		when(decoratedRepo.findOneById(id)).thenReturn(existingEntity);
		bidiAttrUpdateDecorator.delete(entity);
		verify(decoratedRepo, atLeast(1)).getEntityMetaData();
		verify(decoratedRepo).update(entity);
		verify(decoratedRepo).delete(entity);
		verify(decoratedRepo).findOneById(id);
		verifyNoMoreInteractions(decoratedRepo);
		verify(dataService).update(refEntityName, refEntity);
		verifyNoMoreInteractions(dataService);
	}

	@Test
	public void deleteEntityMappedByAttrNotNull()
	{
		when(entityMeta.hasBidirectionalAttributes()).thenReturn(true);
		when(entityMeta.getName()).thenReturn("entity");

		String refEntityName = "refEntity";
		EntityMetaData refEntityMeta = mock(EntityMetaData.class);

		String oneToManyAttrName = "oneToManyAttr";
		AttributeMetaData oneToManyAttr = mock(AttributeMetaData.class);
		when(oneToManyAttr.getDataType()).thenReturn(ONE_TO_MANY);
		when(oneToManyAttr.getName()).thenReturn(oneToManyAttrName);

		String xrefAttrName = "xrefAttr";
		AttributeMetaData xrefAttr = mock(AttributeMetaData.class);
		when(xrefAttr.getName()).thenReturn(xrefAttrName);
		when(xrefAttr.getDataType()).thenReturn(XREF);

		when(oneToManyAttr.getRefEntity()).thenReturn(refEntityMeta);
		when(oneToManyAttr.getMappedBy()).thenReturn(xrefAttr);

		when(refEntityMeta.getName()).thenReturn(refEntityName);
		when(xrefAttr.getRefEntity()).thenReturn(entityMeta);
		when(xrefAttr.getInversedBy()).thenReturn(oneToManyAttr);

		when(entityMeta.getMappedByAttributes()).thenAnswer(new Answer<Stream<AttributeMetaData>>()
		{
			@Override
			public Stream<AttributeMetaData> answer(InvocationOnMock invocation) throws Throwable
			{
				return Stream.of(oneToManyAttr);
			}
		});
		when(entityMeta.getInversedByAttributes()).thenAnswer(new Answer<Stream<AttributeMetaData>>()
		{
			@Override
			public Stream<AttributeMetaData> answer(InvocationOnMock invocation) throws Throwable
			{
				return Stream.empty();
			}
		});

		Entity entity = mock(Entity.class);
		Entity refEntity = mock(Entity.class);
		Object id = mock(Object.class);
		when(entity.getIdValue()).thenReturn(id);
		when(entity.getEntities(oneToManyAttrName)).thenReturn(singleton(refEntity)).thenReturn(emptyList());
		Object refId = mock(Object.class);
		when(refEntity.getIdValue()).thenReturn(refId);
		when(refEntity.getEntity(xrefAttrName)).thenReturn(entity);

		Entity existingEntity = mock(Entity.class);
		when(existingEntity.getIdValue()).thenReturn(id);
		when(existingEntity.getEntities(oneToManyAttrName)).thenReturn(singleton(refEntity));

		when(decoratedRepo.findOneById(id)).thenReturn(existingEntity);
		bidiAttrUpdateDecorator.delete(entity);
		verify(decoratedRepo, atLeast(1)).getEntityMetaData();
		verify(decoratedRepo).update(entity);
		verify(decoratedRepo).delete(entity);
		verify(decoratedRepo).findOneById(id);
		verifyNoMoreInteractions(decoratedRepo);
		verify(dataService).update(refEntityName, refEntity);
		verifyNoMoreInteractions(dataService);
	}

	@Test
	public void deleteEntityNoBidirectionalAttrs()
	{
		when(entityMeta.hasBidirectionalAttributes()).thenReturn(false);
		Entity entity = mock(Entity.class);
		bidiAttrUpdateDecorator.delete(entity);
		verify(decoratedRepo).getEntityMetaData();
		verify(decoratedRepo).delete(entity);
		verifyNoMoreInteractions(decoratedRepo);
		verifyZeroInteractions(dataService);
	}

	@Test
	public void deleteStreamBidirectionalAttrs()
	{
		when(entityMeta.hasBidirectionalAttributes()).thenReturn(true);

		String oneToManyAttrName = "oneToManyAttr";
		AttributeMetaData oneToManyAttr = mock(AttributeMetaData.class);
		when(oneToManyAttr.getDataType()).thenReturn(ONE_TO_MANY);
		when(oneToManyAttr.getName()).thenReturn(oneToManyAttrName);

		String xrefAttrName = "xrefAttr";
		AttributeMetaData xrefAttr = mock(AttributeMetaData.class);
		when(xrefAttr.getName()).thenReturn(xrefAttrName);
		when(xrefAttr.getDataType()).thenReturn(XREF);

		when(oneToManyAttr.getRefEntity()).thenReturn(entityMeta);
		when(oneToManyAttr.getMappedBy()).thenReturn(xrefAttr);

		String refEntityName = "refEntity";
		EntityMetaData refEntityMeta = mock(EntityMetaData.class);
		when(refEntityMeta.getName()).thenReturn(refEntityName);
		when(xrefAttr.getRefEntity()).thenReturn(refEntityMeta);
		when(xrefAttr.getInversedBy()).thenReturn(oneToManyAttr);

		when(entityMeta.getMappedByAttributes()).thenAnswer(new Answer<Stream<AttributeMetaData>>()
		{
			@Override
			public Stream<AttributeMetaData> answer(InvocationOnMock invocation) throws Throwable
			{
				return Stream.empty();
			}
		});
		when(entityMeta.getInversedByAttributes()).thenAnswer(new Answer<Stream<AttributeMetaData>>()
		{
			@Override
			public Stream<AttributeMetaData> answer(InvocationOnMock invocation) throws Throwable
			{
				return Stream.of(xrefAttr);
			}
		});
		Entity entity = mock(Entity.class);
		Entity existingEntity = mock(Entity.class);
		Entity refEntity = mock(Entity.class);
		Object refId = mock(Object.class);
		when(refEntity.getIdValue()).thenReturn(refId);
		Object id = mock(Object.class);
		when(entity.getIdValue()).thenReturn(id);
		when(entity.getEntity(xrefAttrName)).thenReturn(refEntity).thenReturn(null);

		when(existingEntity.getIdValue()).thenReturn(id);
		when(existingEntity.getEntity(xrefAttrName)).thenReturn(refEntity);

		when(refEntity.getEntities(oneToManyAttrName)).thenReturn(singleton(entity));

		when(decoratedRepo.findOneById(id)).thenReturn(existingEntity);
		bidiAttrUpdateDecorator.delete(Stream.of(entity));
		verify(decoratedRepo, atLeast(1)).getEntityMetaData();
		verify(decoratedRepo).update(entity);
		verify(decoratedRepo).delete(entity);
		verify(decoratedRepo).findOneById(id);
		verifyNoMoreInteractions(decoratedRepo);
		verify(dataService).update(refEntityName, refEntity);
		verifyNoMoreInteractions(dataService);
	}

	@Test
	public void deleteStreamNoBidirectionalAttrs()
	{
		when(entityMeta.hasBidirectionalAttributes()).thenReturn(false);
		Entity entity = mock(Entity.class);
		bidiAttrUpdateDecorator.delete(Stream.of(entity));
		verify(decoratedRepo).getEntityMetaData();
		//noinspection unchecked
		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(decoratedRepo).delete(captor.capture());
		assertEquals(captor.getValue().collect(toList()), singletonList(entity));
		verifyNoMoreInteractions(decoratedRepo);
		verifyZeroInteractions(dataService);
	}

	@Test
	public void deleteByIdNoBidirectionalAttrs()
	{
		when(entityMeta.hasBidirectionalAttributes()).thenReturn(false);
		Object id = mock(Object.class);
		bidiAttrUpdateDecorator.deleteById(id);
		verify(decoratedRepo).getEntityMetaData();
		verify(decoratedRepo).deleteById(id);
		verifyNoMoreInteractions(decoratedRepo);
		verifyZeroInteractions(dataService);
	}

	@Test
	public void deleteAllNoBidirectionalAttrs()
	{
		when(entityMeta.hasBidirectionalAttributes()).thenReturn(false);
		bidiAttrUpdateDecorator.deleteAll();
		verify(decoratedRepo).getEntityMetaData();
		verify(decoratedRepo).deleteAll();
		verifyNoMoreInteractions(decoratedRepo);
		verifyZeroInteractions(dataService);
	}

	@Test
	public void deleteAllStreamNoBidirectionalAttrs()
	{
		when(entityMeta.hasBidirectionalAttributes()).thenReturn(false);
		Object id = mock(Object.class);
		bidiAttrUpdateDecorator.deleteAll(Stream.of(id));
		verify(decoratedRepo).getEntityMetaData();
		//noinspection unchecked
		ArgumentCaptor<Stream<Object>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(decoratedRepo).deleteAll(captor.capture());
		assertEquals(captor.getValue().collect(toList()), singletonList(id));
		verifyNoMoreInteractions(decoratedRepo);
		verifyZeroInteractions(dataService);
	}
}