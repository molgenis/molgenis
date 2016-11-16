package org.molgenis.data.validation.meta;

import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.validation.meta.AttributeValidator.ValidationMode;

import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class AttributeRepositoryValidationDecorator extends AbstractRepositoryDecorator<Attribute>
{
	private final Repository<Attribute> decoratedRepo;
	private final AttributeValidator attributeValidator;

	public AttributeRepositoryValidationDecorator(Repository<Attribute> decoratedRepo,
			AttributeValidator attributeValidator)
	{
		this.decoratedRepo = requireNonNull(decoratedRepo);
		this.attributeValidator = requireNonNull(attributeValidator);
	}

	@Override
	protected Repository<Attribute> delegate()
	{
		return decoratedRepo;
	}

	@Override
	public void update(Attribute attr)
	{
		attributeValidator.validate(attr, ValidationMode.UPDATE);
		decoratedRepo.update(attr);
	}

	@Override
	public void update(Stream<Attribute> attrs)
	{
		decoratedRepo.update(attrs.filter(attr ->
		{
			attributeValidator.validate(attr, ValidationMode.UPDATE);
			return true;
		}));
	}

	@Override
	public void add(Attribute attr)
	{
		attributeValidator.validate(attr, ValidationMode.ADD);
		decoratedRepo.add(attr);
	}

	@Override
	public Integer add(Stream<Attribute> attrs)
	{
		return decoratedRepo.add(attrs.filter(attr ->
		{
			attributeValidator.validate(attr, ValidationMode.ADD);
			return true;
		}));
	}
}