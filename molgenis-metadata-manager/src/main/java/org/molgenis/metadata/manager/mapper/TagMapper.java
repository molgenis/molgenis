package org.molgenis.metadata.manager.mapper;

import com.google.common.collect.ImmutableList;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.Tag;
import org.molgenis.data.meta.model.TagFactory;
import org.molgenis.data.support.LazyEntity;
import org.molgenis.metadata.manager.model.EditorTagIdentifier;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

@Component
class TagMapper
{
	private final TagFactory tagFactory;
	private final DataService dataService;

	TagMapper(TagFactory tagFactory, DataService dataService)
	{
		this.tagFactory = requireNonNull(tagFactory);
		this.dataService = requireNonNull(dataService);
	}

	Iterable<Tag> toTagReferences(List<EditorTagIdentifier> tags)
	{
		return tags.stream().map(this::toTagReference).collect(toList());
	}

	private Tag toTagReference(EditorTagIdentifier editorTagIdentifier)
	{
		return new Tag(new LazyEntity(tagFactory.getEntityType(), dataService, editorTagIdentifier.getId()));
	}

	ImmutableList<EditorTagIdentifier> toEditorTags(Iterable<Tag> tags)
	{
		return ImmutableList.copyOf(stream(tags.spliterator(), false).map(this::toEditorTag).iterator());
	}

	private EditorTagIdentifier toEditorTag(Tag tag)
	{
		return EditorTagIdentifier.create(tag.getId(), tag.getLabel());
	}
}
