package org.molgenis.data.validation.meta.model;

import org.molgenis.data.AbstractSystemRepositoryDecoratorFactory;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.Tag;
import org.molgenis.data.meta.model.TagMetadata;
import org.molgenis.data.validation.meta.TagRepositoryValidationDecorator;
import org.molgenis.data.validation.meta.TagValidator;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

/**
 * Due to a circular dependency this decorator factory is not stored in molgenis-data.
 */
@Component
public class TagRepositoryDecoratorFactory extends AbstractSystemRepositoryDecoratorFactory<Tag, TagMetadata>
{
	private final TagValidator tagValidator;

	public TagRepositoryDecoratorFactory(TagMetadata tagMetadata, TagValidator tagValidator)
	{
		super(tagMetadata);
		this.tagValidator = requireNonNull(tagValidator);
	}

	@Override
	public Repository<Tag> createDecoratedRepository(Repository<Tag> repository)
	{
		return new TagRepositoryValidationDecorator(repository, tagValidator);
	}
}
