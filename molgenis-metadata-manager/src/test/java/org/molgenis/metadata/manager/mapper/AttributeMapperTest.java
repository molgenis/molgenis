package org.molgenis.metadata.manager.mapper;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.molgenis.data.Sort;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.*;
import org.molgenis.metadata.manager.model.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;

import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.ImmutableList.of;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.*;
import static org.molgenis.data.meta.AttributeType.CATEGORICAL_MREF;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.support.AttributeUtils.getI18nAttributeName;
import static org.testng.Assert.assertEquals;

public class AttributeMapperTest
{
	@Mock
	private AttributeFactory attributeFactory;
	@Mock
	private TagMapper tagMapper;
	@Mock
	private EntityTypeReferenceMapper entityTypeReferenceMapper;
	@Mock
	private AttributeReferenceMapper attributeReferenceMapper;
	@Mock
	private SortMapper sortMapper;

	private AttributeMapper attributeMapper;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		MockitoAnnotations.initMocks(this);
		attributeMapper = new AttributeMapper(attributeFactory, tagMapper, entityTypeReferenceMapper,
				attributeReferenceMapper, sortMapper);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testAttributeMapper() throws Exception
	{
		new AttributeMapper(null, null, null, null, null);
	}

	@Test
	public void testCreateEditorAttribute()
	{
		String id = "id";
		Integer sequenceNumber = 0;
		Attribute attribute = mock(Attribute.class);
		when(attribute.getIdentifier()).thenReturn(id);
		when(attribute.getRangeMin()).thenReturn(null);
		when(attribute.getRangeMax()).thenReturn(null);
		when(attribute.getDataType()).thenReturn(AttributeType.STRING);
		@SuppressWarnings("unchecked")
		Iterable<Tag> tags = mock(Iterable.class);
		when(attribute.getTags()).thenReturn(tags);
		when(attributeFactory.create()).thenReturn(attribute);

		@SuppressWarnings("unchecked")
		ImmutableList<EditorTagIdentifier> editorTags = mock(ImmutableList.class);
		when(tagMapper.toEditorTags(tags)).thenReturn(editorTags);

		EditorAttribute editorAttribute = attributeMapper.createEditorAttribute();
		assertEquals(editorAttribute,
				EditorAttribute.create(id, null, "string", null, null, null, null, null, false, false, false, null,
						ImmutableMap.of(), null, ImmutableMap.of(), false, of(), null, null, false, false, editorTags,
						null, null, null, null, sequenceNumber));
	}

	@Test
	public void testToAttributes()
	{
		String id = "id";
		String name = "name";
		String type = "string";
		String parentId = "parentId";
		EditorAttributeIdentifier editorParentAttributeIdentifier = EditorAttributeIdentifier.create(parentId, "label");
		String refEntityTypeId = "refId";
		EditorEntityTypeIdentifier editorRefEntityType = EditorEntityTypeIdentifier.create(refEntityTypeId, "label");
		EditorAttributeIdentifier editorMappedByAttribute = EditorAttributeIdentifier.create("mappedBy",
				"mappedByLabel");
		EditorSort editorSort = mock(EditorSort.class);
		String expression = "expression";
		boolean nullable = false;
		boolean auto = false;
		boolean visible = false;
		String label = "label";
		String i18nLabelLangEn = "en";
		String i18nLabelValue = "en label";
		Map<String, String> i18nLabel = singletonMap(i18nLabelLangEn, i18nLabelValue);
		String description = "description";
		String i18nDescriptionLangEn = "en";
		String i18nDescriptionValue = "en description";
		Map<String, String> i18nDescription = singletonMap(i18nDescriptionLangEn, i18nDescriptionValue);
		boolean aggregatable = false;
		Long rangeMin = 5L;
		Long rangeMax = 10L;
		boolean readonly = false;
		boolean unique = false;
		@SuppressWarnings("unchecked")
		ImmutableList<EditorTagIdentifier> editorTagIdentifiers = mock(ImmutableList.class);
		String nullableExpression = "nullableExpression";
		String visibleExpression = "visibleExpression";
		String validationExpression = "validationExpression";
		String defaultValue = "defaultValue";
		Integer sequenceNumber = 1;

		Attribute parentAttribute = mock(Attribute.class);
		when(attributeReferenceMapper.toAttributeReference(editorParentAttributeIdentifier)).thenReturn(
				parentAttribute);
		EntityType refEntityType = mock(EntityType.class);
		when(entityTypeReferenceMapper.toEntityTypeReference(refEntityTypeId)).thenReturn(refEntityType);
		Attribute mappedByAttribute = mock(Attribute.class);
		when(attributeReferenceMapper.toAttributeReference(editorMappedByAttribute)).thenReturn(mappedByAttribute);
		Sort sort = mock(Sort.class);
		when(sortMapper.toSort(editorSort)).thenReturn(sort);
		@SuppressWarnings("unchecked")
		Iterable<Tag> tags = mock(Iterable.class);
		when(tagMapper.toTagReferences(editorTagIdentifiers)).thenReturn(tags);
		String entityId = "entityId";
		EntityType entityType = mock(EntityType.class);
		when(entityTypeReferenceMapper.toEntityTypeReference(entityId)).thenReturn(entityType);
		Attribute attr = when(mock(Attribute.class).getIdentifier()).thenReturn(id).getMock();
		Attribute parentAttr = when(mock(Attribute.class).getIdentifier()).thenReturn(parentId).getMock();
		when(attributeFactory.create()).thenReturn(attr, parentAttr);

		EditorEntityType editorEntityType = when(mock(EditorEntityType.class).getId()).thenReturn(entityId).getMock();
		EditorAttributeIdentifier editorIdAttributeIdentifier = EditorAttributeIdentifier.create(id, label);
		when(editorEntityType.getIdAttribute()).thenReturn(editorIdAttributeIdentifier);
		when(editorEntityType.getLabelAttribute()).thenReturn(editorIdAttributeIdentifier);
		when(editorEntityType.getLookupAttributes()).thenReturn(singletonList(editorIdAttributeIdentifier));

		EditorAttribute editorAttribute = EditorAttribute.create(id, name, type, editorParentAttributeIdentifier,
				editorRefEntityType, editorMappedByAttribute, editorSort, expression, nullable, auto, visible, label,
				i18nLabel, description, i18nDescription, aggregatable, of("option0"), rangeMin, rangeMax, readonly,
				unique, editorTagIdentifiers, nullableExpression, visibleExpression, validationExpression, defaultValue,
				sequenceNumber);
		EditorAttribute editorParentAttribute = EditorAttribute.create(parentId, name, type, null, editorRefEntityType,
				editorMappedByAttribute, editorSort, expression, nullable, auto, visible, label, i18nLabel, description,
				i18nDescription, aggregatable, of("option0"), rangeMin, rangeMax, readonly, unique,
				editorTagIdentifiers, nullableExpression, visibleExpression, validationExpression, defaultValue,
				sequenceNumber);
		ImmutableList<Attribute> attributes = copyOf(
				attributeMapper.toAttributes(of(editorAttribute, editorParentAttribute), editorEntityType));
		assertEquals(attributes.size(), 2);
		Attribute attribute = attributes.get(0);
		verify(attribute).setIdentifier(id);
		verify(attribute).setName(name);
		verify(attribute).setEntity(entityType);
		verify(attribute).setSequenceNumber(0);
		verify(attribute).setDataType(STRING);
		verify(attribute).setIdAttribute(true);
		verify(attribute).setLabelAttribute(true);
		verify(attribute).setLookupAttributeIndex(0);
		verify(attribute).setRefEntity(refEntityType);
		verify(attribute).setMappedBy(mappedByAttribute);
		verify(attribute).setOrderBy(sort);
		verify(attribute).setExpression(expression);
		verify(attribute).setNillable(nullable);
		verify(attribute).setAuto(auto);
		verify(attribute).setVisible(visible);
		verify(attribute).setLabel(label);
		verify(attribute).setLabel(i18nLabelLangEn, i18nLabelValue);
		verify(attribute).setLabel("nl", null);
		verify(attribute).setLabel("de", null);
		verify(attribute).setLabel("es", null);
		verify(attribute).setLabel("it", null);
		verify(attribute).setLabel("pt", null);
		verify(attribute).setLabel("fr", null);
		verify(attribute).setLabel("xx", null);
		verify(attribute).setDescription(description);
		verify(attribute).setDescription(i18nDescriptionLangEn, i18nDescriptionValue);
		verify(attribute).setDescription("nl", null);
		verify(attribute).setDescription("de", null);
		verify(attribute).setDescription("es", null);
		verify(attribute).setDescription("it", null);
		verify(attribute).setDescription("pt", null);
		verify(attribute).setDescription("fr", null);
		verify(attribute).setDescription("xx", null);
		verify(attribute).setAggregatable(aggregatable);
		verify(attribute).setEnumOptions(of("option0"));
		verify(attribute).setRangeMin(rangeMin);
		verify(attribute).setRangeMax(rangeMax);
		verify(attribute).setReadOnly(readonly);
		verify(attribute).setUnique(unique);
		verify(attribute).setTags(tags);
		verify(attribute).setVisibleExpression(visibleExpression);
		verify(attribute).setValidationExpression(validationExpression);
		verify(attribute).setDefaultValue(defaultValue);
		verify(attribute).getIdentifier();
		verify(attribute).setParent(parentAttr);
		verify(attributes.get(1)).setIdentifier(parentId);

		verifyNoMoreInteractions(attribute);
	}

	@Test
	public void testToEditorAttributes()
	{
		String id = "id";
		String name = "name";
		String type = "categoricalmref";
		String expression = "expression";
		boolean nullable = false;
		boolean auto = false;
		boolean visible = false;
		String label = "label";
		String i18nLabelLangEn = "en";
		String i18nLabelValue = "en label";
		Map<String, String> i18nLabel = singletonMap(i18nLabelLangEn, i18nLabelValue);
		String description = "description";
		String i18nDescriptionLangEn = "en";
		String i18nDescriptionValue = "en description";
		Map<String, String> i18nDescription = singletonMap(i18nDescriptionLangEn, i18nDescriptionValue);
		boolean aggregatable = false;
		Long rangeMin = 5L;
		Long rangeMax = 10L;
		boolean readonly = false;
		boolean unique = false;
		String nullableExpression = "nullableExpression";
		String visibleExpression = "visibleExpression";
		String validationExpression = "validationExpression";
		String defaultValue = "defaultValue";
		Integer sequenceNumber = 1;

		Attribute attribute = mock(Attribute.class);
		when(attribute.getIdentifier()).thenReturn(id);
		when(attribute.getName()).thenReturn(name);
		when(attribute.getDataType()).thenReturn(CATEGORICAL_MREF);
		Attribute parentAttr = mock(Attribute.class);
		when(attribute.getParent()).thenReturn(parentAttr);
		EntityType refEntityType = mock(EntityType.class);
		when(attribute.getRefEntity()).thenReturn(refEntityType);
		Attribute mappedByAttribute = mock(Attribute.class);
		when(attribute.getMappedBy()).thenReturn(mappedByAttribute);
		Sort sort = mock(Sort.class);
		when(attribute.getOrderBy()).thenReturn(sort);
		when(attribute.getExpression()).thenReturn(expression);
		when(attribute.isNillable()).thenReturn(nullable);
		when(attribute.isAuto()).thenReturn(auto);
		when(attribute.isVisible()).thenReturn(visible);
		when(attribute.getLabel()).thenReturn(label);
		when(attribute.getString(getI18nAttributeName(AttributeMetadata.LABEL, i18nLabelLangEn))).thenReturn(
				i18nLabelValue);
		when(attribute.getDescription()).thenReturn(description);
		when(attribute.getString(
				getI18nAttributeName(AttributeMetadata.DESCRIPTION, i18nDescriptionLangEn))).thenReturn(
				i18nDescriptionValue);
		when(attribute.isAggregatable()).thenReturn(aggregatable);
		when(attribute.getEnumOptions()).thenReturn(of("option0"));
		when(attribute.getRangeMin()).thenReturn(rangeMin);
		when(attribute.getRangeMax()).thenReturn(rangeMax);
		when(attribute.isReadOnly()).thenReturn(readonly);
		when(attribute.isUnique()).thenReturn(nullable);
		@SuppressWarnings("unchecked")
		Iterable<Tag> tags = mock(Iterable.class);
		when(attribute.getTags()).thenReturn(tags);
		when(attribute.getNullableExpression()).thenReturn(nullableExpression);
		when(attribute.getVisibleExpression()).thenReturn(visibleExpression);
		when(attribute.getValidationExpression()).thenReturn(validationExpression);
		when(attribute.getDefaultValue()).thenReturn(defaultValue);
		when(attribute.getSequenceNumber()).thenReturn(1);

		EditorAttributeIdentifier editorParentAttribute = mock(EditorAttributeIdentifier.class);
		when(attributeReferenceMapper.toEditorAttributeIdentifier(parentAttr)).thenReturn(editorParentAttribute);
		EditorEntityTypeIdentifier editorRefEntityType = mock(EditorEntityTypeIdentifier.class);
		when(entityTypeReferenceMapper.toEditorEntityTypeIdentifier(refEntityType)).thenReturn(editorRefEntityType);
		EditorAttributeIdentifier editorMappedByAttribute = mock(EditorAttributeIdentifier.class);
		when(attributeReferenceMapper.toEditorAttributeIdentifier(mappedByAttribute)).thenReturn(
				editorMappedByAttribute);
		EditorSort editorSort = mock(EditorSort.class);
		when(sortMapper.toEditorSort(sort)).thenReturn(editorSort);
		@SuppressWarnings("unchecked")
		ImmutableList<EditorTagIdentifier> editorTags = mock(ImmutableList.class);
		when(tagMapper.toEditorTags(tags)).thenReturn(editorTags);

		ImmutableList<EditorAttribute> editorAttributes = attributeMapper.toEditorAttributes(of(attribute));
		EditorAttribute expectedEditorAttribute = EditorAttribute.create(id, name, type, editorParentAttribute,
				editorRefEntityType, editorMappedByAttribute, editorSort, expression, nullable, auto, visible, label,
				i18nLabel, description, i18nDescription, aggregatable, of("option0"), rangeMin, rangeMax, readonly,
				unique, editorTags, nullableExpression, visibleExpression, validationExpression, defaultValue,
				sequenceNumber);
		assertEquals(editorAttributes, of(expectedEditorAttribute));
	}
}