package org.molgenis.data.validation.meta;

import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.Tag;
import org.molgenis.data.validation.ValidationException;
import org.molgenis.data.validation.constraint.TagValidationResult;

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
		validate(tag);
		super.update(tag);
	}

	@Override
	public void add(Tag tag)
	{
		validate(tag);
		super.add(tag);
	}

	@Override
	public Integer add(Stream<Tag> tagStream)
	{
		return delegate().add(tagStream.filter(tag ->
		{
			validate(tag);
			return true;
		}));
	}

	@Override
	public void update(Stream<Tag> tagStream)
	{
		delegate().update(tagStream.filter(tag ->
		{
			validate(tag);
			return true;
		}));
	}

	private void validate(Tag tag)
	{
		TagValidationResult tagValidationResult = tagValidator.validate(tag);
		if (tagValidationResult.hasConstraintViolations())
		{
			throw new ValidationException(tagValidationResult);
		}
	}
}
