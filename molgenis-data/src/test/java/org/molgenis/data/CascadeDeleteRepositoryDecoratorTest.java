package org.molgenis.data;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.data.meta.AttributeType.*;
import static org.testng.Assert.assertEquals;

public class CascadeDeleteRepositoryDecoratorTest
{
	private static final String XREF_ATTR_NAME = "xrefAttr";
	private static final String REF_ENTITY_TYPE_NAME = "refEntityType";
	private static final Object REF_ENTITY_ID = "REF_ENTITY_ID";

	@Mock
	private Repository<Entity> decoratedRepository;

	@Mock
	private DataService dataService;

	private CascadeDeleteRepositoryDecorator cascadeDeleteRepositoryDecorator;

	@Mock
	private EntityType entityType;
	@Mock
	private Attribute stringAttr;
	@Mock
	private Attribute xrefAttr;
	@Mock
	private EntityType refEntityType;
	@Mock
	private Entity entity;
	@Mock
	private Entity refEntity;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		initMocks(this);
		cascadeDeleteRepositoryDecorator = new CascadeDeleteRepositoryDecorator(decoratedRepository, dataService);

		when(refEntityType.getId()).thenReturn(REF_ENTITY_TYPE_NAME);
		when(refEntityType.getAtomicAttributes()).thenReturn(emptyList());

		when(refEntity.getIdValue()).thenReturn(REF_ENTITY_ID);
		when(stringAttr.getName()).thenReturn("stringAttr");
		when(stringAttr.getDataType()).thenReturn(STRING);
		when(xrefAttr.getName()).thenReturn(XREF_ATTR_NAME);
		when(xrefAttr.getDataType()).thenReturn(XREF);
		when(xrefAttr.getRefEntity()).thenReturn(refEntityType);

		when(entityType.getAtomicAttributes()).thenReturn(asList(stringAttr, xrefAttr));
		when(decoratedRepository.getEntityType()).thenReturn(entityType);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testDelegate() throws Exception
	{
		new CascadeDeleteRepositoryDecorator(null, null);
	}

	@Test
	public void testDeleteNoCascade() throws Exception
	{
		when(xrefAttr.getCascadeDelete()).thenReturn(null);
		when(entity.getEntity(XREF_ATTR_NAME)).thenReturn(refEntity);
		cascadeDeleteRepositoryDecorator.delete(entity);
		verify(decoratedRepository).delete(entity);
		verify(dataService, never()).delete(REF_ENTITY_TYPE_NAME, refEntity);
	}

	@Test
	public void testDeleteCascadeNotNull() throws Exception
	{
		when(xrefAttr.getCascadeDelete()).thenReturn(Boolean.TRUE);
		when(entity.getEntity(XREF_ATTR_NAME)).thenReturn(refEntity);
		when(dataService.findOneById(REF_ENTITY_TYPE_NAME, REF_ENTITY_ID)).thenReturn(refEntity);
		cascadeDeleteRepositoryDecorator.delete(entity);
		verify(decoratedRepository).delete(entity);
		verify(dataService).delete(REF_ENTITY_TYPE_NAME, refEntity);
	}

	@Test
	public void testDeleteCascadeNull() throws Exception
	{
		when(xrefAttr.getCascadeDelete()).thenReturn(Boolean.TRUE);
		when(entity.getEntity(XREF_ATTR_NAME)).thenReturn(null);
		cascadeDeleteRepositoryDecorator.delete(entity);
		verify(decoratedRepository).delete(entity);
		verify(dataService, never()).delete(REF_ENTITY_TYPE_NAME, refEntity);
	}

	@Test
	public void testDeleteCascadeMrefEmpty() throws Exception
	{
		String mrefAttrName = "mrefAttrName";
		Attribute mrefAttr = mock(Attribute.class);
		when(mrefAttr.getName()).thenReturn(mrefAttrName);
		when(mrefAttr.getDataType()).thenReturn(MREF);
		when(mrefAttr.getRefEntity()).thenReturn(refEntityType);
		when(entityType.getAtomicAttributes()).thenReturn(asList(stringAttr, mrefAttr));

		when(mrefAttr.getCascadeDelete()).thenReturn(Boolean.TRUE);
		when(entity.getEntities(mrefAttrName)).thenReturn(emptyList());
		cascadeDeleteRepositoryDecorator.delete(entity);
		verify(decoratedRepository).delete(entity);
		verify(dataService, never()).delete(REF_ENTITY_TYPE_NAME, refEntity);
	}

	@Test
	public void testDeleteCascadeMrefNotEmpty() throws Exception
	{
		String mrefAttrName = "mrefAttrName";
		Attribute mrefAttr = mock(Attribute.class);
		when(mrefAttr.getName()).thenReturn(mrefAttrName);
		when(mrefAttr.getDataType()).thenReturn(MREF);
		when(mrefAttr.getRefEntity()).thenReturn(refEntityType);
		when(entityType.getAtomicAttributes()).thenReturn(asList(stringAttr, mrefAttr));
		when(dataService.findOneById(REF_ENTITY_TYPE_NAME, REF_ENTITY_ID)).thenReturn(refEntity);
		when(mrefAttr.getCascadeDelete()).thenReturn(Boolean.TRUE);
		when(entity.getEntities(mrefAttrName)).thenReturn(singletonList(refEntity));
		cascadeDeleteRepositoryDecorator.delete(entity);
		verify(decoratedRepository).delete(entity);
		verify(dataService).delete(REF_ENTITY_TYPE_NAME, refEntity);
	}

	@Test
	public void testDeleteByIdCascadeNotNull() throws Exception
	{
		String entityId = "id";
		when(decoratedRepository.findOneById(entityId)).thenReturn(entity);
		when(xrefAttr.getCascadeDelete()).thenReturn(Boolean.TRUE);
		when(entity.getEntity(XREF_ATTR_NAME)).thenReturn(refEntity);
		when(dataService.findOneById(REF_ENTITY_TYPE_NAME, REF_ENTITY_ID)).thenReturn(refEntity);
		cascadeDeleteRepositoryDecorator.deleteById(entityId);
		verify(decoratedRepository).deleteById(entityId);
		verify(dataService).delete(REF_ENTITY_TYPE_NAME, refEntity);
	}

	@Test
	public void testDeleteByIdNoCascade() throws Exception
	{
		String entityId = "id";
		when(decoratedRepository.findOneById(entityId)).thenReturn(entity);
		when(xrefAttr.getCascadeDelete()).thenReturn(null);

		when(entity.getEntity(XREF_ATTR_NAME)).thenReturn(refEntity);
		cascadeDeleteRepositoryDecorator.deleteById(entityId);
		verify(decoratedRepository).deleteById(entityId);
		verify(dataService, never()).delete(REF_ENTITY_TYPE_NAME, refEntity);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testDeleteAllCascadeNotNull() throws Exception
	{
		when(decoratedRepository.findAll(any(Query.class))).thenReturn(Stream.of(entity));
		when(xrefAttr.getCascadeDelete()).thenReturn(Boolean.TRUE);
		when(entity.getEntity(XREF_ATTR_NAME)).thenReturn(refEntity);
		when(dataService.findOneById(REF_ENTITY_TYPE_NAME, REF_ENTITY_ID)).thenReturn(refEntity);
		cascadeDeleteRepositoryDecorator.deleteAll();
		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepository).delete(captor.capture());
		assertEquals(captor.getValue().collect(toList()), singletonList(entity));
		verify(dataService).delete(REF_ENTITY_TYPE_NAME, refEntity);
	}

	@Test
	public void testDeleteAllNoCascade() throws Exception
	{
		when(xrefAttr.getCascadeDelete()).thenReturn(null);
		when(entity.getEntity(XREF_ATTR_NAME)).thenReturn(refEntity);
		cascadeDeleteRepositoryDecorator.deleteAll();
		verify(decoratedRepository).deleteAll();
		verify(dataService, never()).delete(REF_ENTITY_TYPE_NAME, refEntity);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testDeleteStreamCascadeNotNull() throws Exception
	{
		when(xrefAttr.getCascadeDelete()).thenReturn(Boolean.TRUE);
		when(entity.getEntity(XREF_ATTR_NAME)).thenReturn(refEntity);
		when(dataService.findOneById(REF_ENTITY_TYPE_NAME, REF_ENTITY_ID)).thenReturn(refEntity);
		cascadeDeleteRepositoryDecorator.delete(Stream.of(entity));
		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepository).delete(captor.capture());
		assertEquals(captor.getValue().collect(toList()), singletonList(entity));
		verify(dataService).delete(REF_ENTITY_TYPE_NAME, refEntity);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testDeleteStreamNoCascade() throws Exception
	{
		when(xrefAttr.getCascadeDelete()).thenReturn(null);
		when(entity.getEntity(XREF_ATTR_NAME)).thenReturn(refEntity);
		when(dataService.findOneById(REF_ENTITY_TYPE_NAME, REF_ENTITY_ID)).thenReturn(refEntity);
		cascadeDeleteRepositoryDecorator.delete(Stream.of(entity));
		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepository).delete(captor.capture());
		assertEquals(captor.getValue().collect(toList()), singletonList(entity));
		verify(dataService, never()).delete(REF_ENTITY_TYPE_NAME, refEntity);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testDeleteAllStreamCascadeNotNull() throws Exception
	{
		String entityId = "id";
		when(decoratedRepository.findAll(any(Stream.class))).thenReturn(Stream.of(entity));
		when(xrefAttr.getCascadeDelete()).thenReturn(Boolean.TRUE);
		when(entity.getEntity(XREF_ATTR_NAME)).thenReturn(refEntity);
		when(dataService.findOneById(REF_ENTITY_TYPE_NAME, REF_ENTITY_ID)).thenReturn(refEntity);
		cascadeDeleteRepositoryDecorator.deleteAll(Stream.of(entityId));
		ArgumentCaptor<Stream<Object>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepository).deleteAll(captor.capture());
		assertEquals(captor.getValue().collect(toList()), singletonList(entityId));
		verify(dataService).delete(REF_ENTITY_TYPE_NAME, refEntity);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testDeleteAllStreamNoCascade() throws Exception
	{
		String entityId = "id";
		when(decoratedRepository.findAll(any(Stream.class))).thenReturn(Stream.of(entity));
		when(xrefAttr.getCascadeDelete()).thenReturn(null);
		when(entity.getEntity(XREF_ATTR_NAME)).thenReturn(refEntity);
		when(dataService.findOneById(REF_ENTITY_TYPE_NAME, REF_ENTITY_ID)).thenReturn(refEntity);
		cascadeDeleteRepositoryDecorator.deleteAll(Stream.of(entityId));
		ArgumentCaptor<Stream<Object>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(decoratedRepository).deleteAll(captor.capture());
		assertEquals(captor.getValue().collect(toList()), singletonList(entityId));
		verify(dataService, never()).delete(REF_ENTITY_TYPE_NAME, refEntity);
	}
}