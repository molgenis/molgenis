package org.molgenis.metadata.manager.mapper;

import com.google.common.collect.ImmutableList;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeMetadata;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.metadata.manager.model.EditorAttributeIdentifier;
import org.molgenis.metadata.manager.model.EditorEntityTypeIdentifier;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.google.common.collect.ImmutableList.of;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertNull;

public class AttributeReferenceMapperTest
{
	@Mock
	private AttributeMetadata attributeMetadata;
	@Mock
	private DataService dataService;

	private AttributeReferenceMapper attributeReferenceMapper;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		MockitoAnnotations.initMocks(this);
		attributeReferenceMapper = new AttributeReferenceMapper(attributeMetadata, dataService);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testAttributeReferenceMapper()
	{
		new AttributeReferenceMapper(null, null);
	}

	@Test
	public void testToEditorAttributeIdentifiers()
	{
		String id = "id";
		String label = "label";
		String entityTypeId = "id";
		String entityTypeLabel= "label";
		Attribute attribute = mock(Attribute.class);
		EntityType entityType = mock(EntityType.class);
		when(attribute.getIdentifier()).thenReturn(id);
		when(attribute.getLabel()).thenReturn(label);
		when(attribute.getEntity()).thenReturn(entityType);
		when(entityType.getId()).thenReturn(entityTypeId);
		when(entityType.getLabel()).thenReturn(entityTypeLabel);
		ImmutableList<EditorAttributeIdentifier> editorAttributeIdentifier = attributeReferenceMapper.toEditorAttributeIdentifiers(
				of(attribute));
		EditorEntityTypeIdentifier editorEntityTypeIdentifier = EditorEntityTypeIdentifier.create(entityTypeId, entityTypeLabel);
		assertEquals(editorAttributeIdentifier, of(EditorAttributeIdentifier.create(id, label, editorEntityTypeIdentifier)));
	}

	@Test
	public void testToEditorAttributeIdentifier()
	{
		String id = "id";
		String label = "label";
		String entityTypeId = "id";
		String entityTypeLabel= "label";
		Attribute attribute = mock(Attribute.class);
		EntityType entityType = mock(EntityType.class);
		when(attribute.getIdentifier()).thenReturn(id);
		when(attribute.getLabel()).thenReturn(label);
		when(attribute.getEntity()).thenReturn(entityType);
		when(entityType.getId()).thenReturn(entityTypeId);
		when(entityType.getLabel()).thenReturn(entityTypeLabel);
		EditorAttributeIdentifier editorAttributeIdentifier = attributeReferenceMapper.toEditorAttributeIdentifier(
				attribute);

		EditorEntityTypeIdentifier editorEntityTypeIdentifier = EditorEntityTypeIdentifier.create(entityTypeId, entityTypeLabel);
		assertEquals(editorAttributeIdentifier, EditorAttributeIdentifier.create(id, label, editorEntityTypeIdentifier));
	}

	@Test
	public void testToEditorAttributeIdentifierNull()
	{
		assertNull(attributeReferenceMapper.toEditorAttributeIdentifier(null));
	}

	@Test
	public void testToAttributeReference()
	{
		String id = "id";
		String label = "label";
		EditorAttributeIdentifier editorAttributeIdentifier = EditorAttributeIdentifier.create(id, label);
		Attribute attribute = attributeReferenceMapper.toAttributeReference(editorAttributeIdentifier);
		assertEquals(attribute.getIdValue(), id);

	}

	@Test
	public void testToAttributeReferenceNull()
	{
		assertNull(attributeReferenceMapper.toAttributeReference(null));
	}
}