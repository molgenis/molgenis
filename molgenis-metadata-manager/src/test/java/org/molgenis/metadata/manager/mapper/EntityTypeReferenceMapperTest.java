package org.molgenis.metadata.manager.mapper;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.metadata.manager.model.EditorEntityTypeIdentifier;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static com.google.common.collect.ImmutableList.of;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class EntityTypeReferenceMapperTest
{
	@Mock
	private EntityTypeMetadata entityTypeMetadata;

	@Mock
	private DataService dataService;

	private EntityTypeReferenceMapper entityTypeReferenceMapper;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		MockitoAnnotations.initMocks(this);
		entityTypeReferenceMapper = new EntityTypeReferenceMapper(entityTypeMetadata, dataService);
	}

	@Test
	public void testToEntityTypeReference() throws Exception
	{
		String id = "id";
		EntityType entityType = entityTypeReferenceMapper.toEntityTypeReference(id);
		assertEquals(entityType.getIdValue(), id);
	}

	@Test
	public void testToEntityTypeReferenceNull() throws Exception
	{
		assertNull(entityTypeReferenceMapper.toEntityTypeReference(null));
	}

	@Test
	public void testToEditorEntityTypeIdentifiers()
	{
		String id = "id";
		String label = "label";
		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn(id);
		when(entityType.getLabel()).thenReturn(label);
		List<EditorEntityTypeIdentifier> editorEntityTypeIdentifiers = entityTypeReferenceMapper.toEditorEntityTypeIdentifiers(
				of(entityType));
		assertEquals(editorEntityTypeIdentifiers, of(EditorEntityTypeIdentifier.create(id, label)));
	}

	@Test
	public void testToEditorEntityTypeIdentifier()
	{
		String id = "id";
		String label = "label";
		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn(id);
		when(entityType.getLabel()).thenReturn(label);
		EditorEntityTypeIdentifier editorEntityTypeIdentifier = entityTypeReferenceMapper.toEditorEntityTypeIdentifier(
				entityType);
		assertEquals(editorEntityTypeIdentifier, EditorEntityTypeIdentifier.create(id, label));
	}

	@Test
	public void testToEditorEntityTypeIdentifierNull()
	{
		assertNull(entityTypeReferenceMapper.toEditorEntityTypeIdentifier(null));
	}
}