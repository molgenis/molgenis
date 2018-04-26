package org.molgenis.metadata.manager.mapper;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.Tag;
import org.molgenis.data.meta.model.TagFactory;
import org.molgenis.data.meta.model.TagMetadata;
import org.molgenis.metadata.manager.model.EditorTagIdentifier;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.ImmutableList.of;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class TagMapperTest
{
	@Mock
	private TagFactory tagFactory;

	@Mock
	private DataService dataService;

	private TagMapper tagMapper;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		MockitoAnnotations.initMocks(this);
		TagMetadata tagMetadata = mock(TagMetadata.class);
		when(tagFactory.getEntityType()).thenReturn(tagMetadata);
		tagMapper = new TagMapper(tagFactory, dataService);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testTagMapper()
	{
		new TagMapper(null, null);
	}

	@Test
	public void testToTagReferences()
	{
		String id0 = "id0";
		String id1 = "id1";
		EditorTagIdentifier tagIdentifier0 = EditorTagIdentifier.create(id0, "label0");
		EditorTagIdentifier tagIdentifier1 = EditorTagIdentifier.create(id1, "label1");
		List<Tag> tags = copyOf(tagMapper.toTagReferences(of(tagIdentifier0, tagIdentifier1)));
		assertEquals(tags.size(), 2);
		assertEquals(tags.get(0).getIdValue(), id0);
		assertEquals(tags.get(1).getIdValue(), id1);
	}

	@Test
	public void testToEditorTags()
	{
		String id0 = "id0";
		String label0 = "label0";
		String id1 = "id1";
		String label1 = "label1";
		Tag tag0 = mock(Tag.class);
		when(tag0.getId()).thenReturn(id0);
		when(tag0.getLabel()).thenReturn(label0);
		Tag tag1 = mock(Tag.class);
		when(tag1.getId()).thenReturn(id1);
		when(tag1.getLabel()).thenReturn(label1);
		List<EditorTagIdentifier> editorTags = tagMapper.toEditorTags(of(tag0, tag1));
		assertEquals(editorTags, of(EditorTagIdentifier.create(id0, label0), EditorTagIdentifier.create(id1, label1)));
	}
}