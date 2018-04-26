package org.molgenis.data.validation.meta;

import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.Tag;

import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Validates {@link Tag tags} before adding or updating the delegated repository
 */
public class TagRepositoryValidationDecorator extends AbstractRepositoryDecorator<Tag>
{
	private final TagValidator tagValidator;

	public TagRepositoryValidationDecorator(Repository<Tag> delegateRepository, TagValidator tagValidator)
	{
		super(delegateRepository);
		this.tagValidator = requireNonNull(tagValidator);
	}

	@Override
	public void update(Tag tag)
	{
		tagValidator.validate(tag);
		super.update(tag);
	}

	@Override
	public void add(Tag tag)
	{
		tagValidator.validate(tag);
		super.add(tag);
	}

	@Override
	public Integer add(Stream<Tag> tagStream)
	{
		return delegate().add(tagStream.filter(tag ->
		{
			tagValidator.validate(tag);
			return true;
		}));
	}

	@Override
	public void update(Stream<Tag> tagStream)
	{
		delegate().update(tagStream.filter(entityType ->
		{
			tagValidator.validate(entityType);
			return true;
		}));
	}
}
