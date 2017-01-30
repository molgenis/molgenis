package org.molgenis.ui.metadataeditor.mapper;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.molgenis.data.meta.model.*;
import org.molgenis.data.meta.model.Package;
import org.molgenis.ui.metadataeditor.model.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.ImmutableList.of;
import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.*;
import static org.molgenis.data.support.AttributeUtils.getI18nAttributeName;
import static org.testng.Assert.assertEquals;

public class EntityTypeMapperTest
{
	@Mock
	private EntityTypeFactory entityTypeFactory;

	@Mock
	private AttributeMapper attributeMapper;

	@Mock
	private AttributeReferenceMapper attributeReferenceMapper;

	@Mock
	private PackageMapper packageMapper;

	@Mock
	private TagMapper tagMapper;

	@Mock
	private EntityTypeReferenceMapper entityTypeReferenceMapper;

	@Mock
	private EntityTypeParentMapper entityTypeParentMapper;

	private EntityTypeMapper entityTypeMapper;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		MockitoAnnotations.initMocks(this);
		EntityTypeMetadata entityTypeMetadata = mock(EntityTypeMetadata.class);
		when(entityTypeFactory.getEntityType()).thenReturn(entityTypeMetadata);
		entityTypeMapper = new EntityTypeMapper(entityTypeFactory, attributeMapper, attributeReferenceMapper,
				packageMapper, tagMapper, entityTypeReferenceMapper, entityTypeParentMapper);

	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testEntityTypeMapper()
	{
		new EntityTypeMapper(null, null, null, null, null, null, null);
	}

	@Test
	public void testToEntityType()
	{
		String id = "id";
		String name = "name";
		String label = "label";
		String i18nLabelLangEn = "en";
		String i18nLabelValue = "en label";
		Map<String, String> i18nLabel = singletonMap(i18nLabelLangEn, i18nLabelValue);
		String description = "description";
		String i18nDescriptionLangEn = "en";
		String i18nDescriptionValue = "en description";
		Map<String, String> i18nDescription = singletonMap(i18nDescriptionLangEn, i18nDescriptionValue);
		boolean abstract_ = true;
		String backend = "backend";
		EditorPackageIdentifier editorPackageIdentifier = EditorPackageIdentifier.create("packageId", "package label");
		EditorEntityTypeParent editorEntityTypeParent = EditorEntityTypeParent
				.create("entityTypeParentId", "entity type parent label",
						of(EditorAttributeIdentifier.create("parentAttrId0", "parent attribute #0")), null);
		List<EditorEntityTypeIdentifier> entityTypeChildren = of(
				EditorEntityTypeIdentifier.create("entityTypeChild0", "entity type #0"));
		List<EditorAttribute> editorAttributes = of(mock(EditorAttribute.class));
		ImmutableList<EditorTagIdentifier> editorTags = of(EditorTagIdentifier.create("tag0", "tag #0"));
		EditorAttributeIdentifier idAttribute = EditorAttributeIdentifier.create("idAttr", "id attribute");
		EditorAttributeIdentifier labelAttribute = EditorAttributeIdentifier.create("labelAttr", "label attribute");
		List<EditorAttributeIdentifier> lookupAttributes = of(
				EditorAttributeIdentifier.create("attr0", "attribute #0"));

		EditorEntityType editorEntityType = EditorEntityType
				.create(id, name, label, i18nLabel, description, i18nDescription, abstract_, backend,
						editorPackageIdentifier, editorEntityTypeParent, entityTypeChildren, editorAttributes,
						editorTags, idAttribute, labelAttribute, lookupAttributes);

		EntityType entityType = mock(EntityType.class);
		when(entityTypeFactory.create()).thenReturn(entityType);
		Package package_ = mock(Package.class);
		when(packageMapper.toPackageReference(editorPackageIdentifier)).thenReturn(package_);
		EntityType parentEntityType = mock(EntityType.class);
		when(entityTypeParentMapper.toEntityTypeReference(editorEntityTypeParent)).thenReturn(parentEntityType);
		@SuppressWarnings("unchecked")
		Iterable<Tag> tags = mock(Iterable.class);
		when(tagMapper.toTagReferences(editorTags)).thenReturn(tags);
		@SuppressWarnings("unchecked")
		Iterable<Attribute> attributes = mock(Iterable.class);
		when(attributeMapper.toAttributes(editorAttributes, editorEntityType)).thenReturn(attributes);
		assertEquals(entityType, entityTypeMapper.toEntityType(editorEntityType));
		// FIXME id label lookup attribute modifications
		verify(entityType).setName(id);
		verify(entityType).setSimpleName(name);
		verify(entityType).setLabel(label);
		verify(entityType).setLabel(i18nLabelLangEn, i18nLabelValue);
		verify(entityType).setLabel("nl", null);
		verify(entityType).setLabel("de", null);
		verify(entityType).setLabel("es", null);
		verify(entityType).setLabel("it", null);
		verify(entityType).setLabel("pt", null);
		verify(entityType).setLabel("fr", null);
		verify(entityType).setLabel("xx", null);
		verify(entityType).setDescription(description);
		verify(entityType).setDescription(i18nDescriptionLangEn, i18nDescriptionValue);
		verify(entityType).setDescription("nl", null);
		verify(entityType).setDescription("de", null);
		verify(entityType).setDescription("es", null);
		verify(entityType).setDescription("it", null);
		verify(entityType).setDescription("pt", null);
		verify(entityType).setDescription("fr", null);
		verify(entityType).setDescription("xx", null);
		verify(entityType).setAbstract(abstract_);
		verify(entityType).setBackend(backend);
		verify(entityType).setPackage(package_);
		verify(entityType).setExtends(parentEntityType);
		verify(entityType).setOwnAllAttributes(attributes);
		verify(entityType).setTags(tags);
		verifyNoMoreInteractions(entityType);
	}

	@Test
	public void testToEditorEntityType()
	{
		String id = "id";
		String name = "name";
		String label = "label";
		String i18nLabelLangEn = "en";
		String i18nLabelValue = "en label";
		Map<String, String> i18nLabel = singletonMap(i18nLabelLangEn, i18nLabelValue);
		String description = "description";
		String i18nDescriptionLangEn = "en";
		String i18nDescriptionValue = "en description";
		Map<String, String> i18nDescription = singletonMap(i18nDescriptionLangEn, i18nDescriptionValue);
		boolean abstract_ = true;
		String backend = "backend";

		EntityType entityType = mock(EntityType.class);
		when(entityType.getName()).thenReturn(id);
		when(entityType.getSimpleName()).thenReturn(name);
		when(entityType.getLabel()).thenReturn(label);
		when(entityType.getString(getI18nAttributeName(EntityTypeMetadata.LABEL, i18nLabelLangEn)))
				.thenReturn(i18nLabelValue);
		when(entityType.getDescription()).thenReturn(description);
		when(entityType.getString(getI18nAttributeName(EntityTypeMetadata.DESCRIPTION, i18nDescriptionLangEn)))
				.thenReturn(i18nDescriptionValue);
		when(entityType.isAbstract()).thenReturn(true);
		when(entityType.getBackend()).thenReturn(backend);
		Package package_ = mock(Package.class);
		when(entityType.getPackage()).thenReturn(package_);
		EntityType extendsEntityType = mock(EntityType.class);
		when(entityType.getExtends()).thenReturn(extendsEntityType);
		@SuppressWarnings("unchecked")
		Iterable<EntityType> extendedBy = mock(Iterable.class);
		when(entityType.getExtendedBy()).thenReturn(extendedBy);
		@SuppressWarnings("unchecked")
		Iterable<Attribute> attributes = mock(Iterable.class);
		when(entityType.getOwnAllAttributes()).thenReturn(attributes);
		@SuppressWarnings("unchecked")
		Iterable<Tag> tags = mock(Iterable.class);
		when(entityType.getTags()).thenReturn(tags);
		Attribute idAttribute = mock(Attribute.class);
		when(entityType.getIdAttribute()).thenReturn(idAttribute);
		Attribute labelAttribute = mock(Attribute.class);
		when(entityType.getLabelAttribute()).thenReturn(labelAttribute);
		@SuppressWarnings("unchecked")
		Iterable<Attribute> lookupAttributes = mock(Iterable.class);
		when(entityType.getLookupAttributes()).thenReturn(lookupAttributes);

		EditorAttributeIdentifier editorIdAttribute = mock(EditorAttributeIdentifier.class);
		when(attributeReferenceMapper.toEditorAttributeIdentifier(idAttribute)).thenReturn(editorIdAttribute);
		EditorAttributeIdentifier editorLabelAttribute = mock(EditorAttributeIdentifier.class);
		when(attributeReferenceMapper.toEditorAttributeIdentifier(labelAttribute)).thenReturn(editorLabelAttribute);
		@SuppressWarnings("unchecked")
		ImmutableList<EditorAttributeIdentifier> editorLookupAttributes = mock(ImmutableList.class);
		when(attributeReferenceMapper.toEditorAttributeIdentifiers(lookupAttributes))
				.thenReturn(editorLookupAttributes);
		@SuppressWarnings("unchecked")
		ImmutableList<EditorAttribute> editorAttributes = mock(ImmutableList.class);
		when(attributeMapper.toEditorAttributes(attributes)).thenReturn(editorAttributes);
		@SuppressWarnings("unchecked")
		ImmutableList<EditorEntityTypeIdentifier> editorEntityTypeChildren = mock(ImmutableList.class);
		when(entityTypeReferenceMapper.toEditorEntityTypeIdentifiers(extendedBy)).thenReturn(editorEntityTypeChildren);
		EditorEntityTypeParent editorEntityTypeParent = mock(EditorEntityTypeParent.class);
		when(entityTypeParentMapper.toEditorEntityTypeParent(extendsEntityType)).thenReturn(editorEntityTypeParent);
		EditorPackageIdentifier editorPackageIdentifier = mock(EditorPackageIdentifier.class);
		when(packageMapper.toEditorPackage(package_)).thenReturn(editorPackageIdentifier);
		@SuppressWarnings("unchecked")
		ImmutableList<EditorTagIdentifier> editorTags = mock(ImmutableList.class);
		when(tagMapper.toEditorTags(tags)).thenReturn(editorTags);

		when(entityTypeFactory.create()).thenReturn(entityType);
		EditorEntityType editorEntityType = entityTypeMapper.createEditorEntityType();

		EditorEntityType expectedEditorEntityType = EditorEntityType
				.create(id, name, label, i18nLabel, description, i18nDescription, abstract_, backend,
						editorPackageIdentifier, editorEntityTypeParent, editorEntityTypeChildren, editorAttributes,
						editorTags, editorIdAttribute, editorLabelAttribute, editorLookupAttributes);
		assertEquals(editorEntityType, expectedEditorEntityType);
	}

	@Test
	public void testCreateEditorEntityType()
	{
		String id = "id";
		String backend = "backend";
		@SuppressWarnings("unchecked")
		List<EntityType> extendedBy = mock(List.class);
		@SuppressWarnings("unchecked")
		List<Attribute> attributes = mock(List.class);
		@SuppressWarnings("unchecked")
		List<Tag> tags = mock(List.class);
		@SuppressWarnings("unchecked")
		List<Attribute> lookupAttributes = mock(List.class);

		EntityType entityType = mock(EntityType.class);
		when(entityType.getName()).thenReturn(id);
		when(entityType.getBackend()).thenReturn(backend);
		when(entityType.getOwnAllAttributes()).thenReturn(attributes);
		when(entityType.getExtendedBy()).thenReturn(extendedBy);
		when(entityType.getTags()).thenReturn(tags);
		when(entityType.getLookupAttributes()).thenReturn(lookupAttributes);
		when(entityTypeFactory.create()).thenReturn(entityType);

		when(attributeMapper.toEditorAttributes(attributes)).thenReturn(ImmutableList.of());
		when(entityTypeReferenceMapper.toEditorEntityTypeIdentifiers(extendedBy)).thenReturn(ImmutableList.of());
		when(tagMapper.toEditorTags(tags)).thenReturn(ImmutableList.of());
		when(attributeReferenceMapper.toEditorAttributeIdentifiers(lookupAttributes)).thenReturn(ImmutableList.of());
		EditorEntityType editorEntityType = entityTypeMapper.createEditorEntityType();

		assertEquals(editorEntityType, EditorEntityType
				.create(id, null, null, ImmutableMap.of(), null, ImmutableMap.of(), false, backend, null, null,
						ImmutableList.of(), ImmutableList.of(), ImmutableList.of(), null, null, ImmutableList.of()));

	}
}