package org.molgenis.data.validation.meta;

import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.validation.CompositeValidationResult;
import org.molgenis.data.validation.ValidationException;
import org.molgenis.data.validation.ValidationResult;
import org.molgenis.data.validation.meta.AttributeValidator.ValidationMode;

import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class AttributeRepositoryValidationDecorator extends AbstractRepositoryDecorator<Attribute>
{
	private final AttributeValidator attributeValidator;
	private final AttributeUpdateValidator attributeUpdateValidator;

	public AttributeRepositoryValidationDecorator(Repository<Attribute> delegateRepository,
			AttributeValidator attributeValidator, AttributeUpdateValidator attributeUpdateValidator)
	{
		super(delegateRepository);
		this.attributeValidator = requireNonNull(attributeValidator);
		this.attributeUpdateValidator = requireNonNull(attributeUpdateValidator);
	}

	@Override
	public void update(Attribute attr)
	{
		validate(attr, ValidationMode.UPDATE);
		delegate().update(attr);
	}

	@Override
	public void update(Stream<Attribute> attrs)
	{
		delegate().update(attrs.filter(attr ->
		{
			validate(attr, ValidationMode.UPDATE);
			return true;
		}));
	}

	@Override
	public void add(Attribute attr)
	{
		validate(attr, ValidationMode.ADD);
		delegate().add(attr);
	}

	@Override
	public Integer add(Stream<Attribute> attrs)
	{
		return delegate().add(attrs.filter(attr ->
		{
			validate(attr, ValidationMode.ADD);
			return true;
		}));
	}

	private void validate(Attribute attribute, ValidationMode validationMode)
	{
		ValidationResult validationResult = attributeValidator.validate(attribute, validationMode);
		if (validationMode == ValidationMode.UPDATE)
		{
			Attribute existingAttribute = findOneById(attribute.getIdentifier());
			AttributeUpdateValidationResult attributeUpdateValidationResult = attributeUpdateValidator.validate(
					existingAttribute, attribute);

			CompositeValidationResult compositeValidationResult = new CompositeValidationResult();
			compositeValidationResult.addValidationResult(validationResult);
			compositeValidationResult.addValidationResult(attributeUpdateValidationResult);
		}

		if (validationResult.hasConstraintViolations())
		{
			throw new ValidationException(validationResult);
		}
	}
}