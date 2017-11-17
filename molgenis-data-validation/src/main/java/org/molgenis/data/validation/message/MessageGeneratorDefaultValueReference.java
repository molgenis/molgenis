package org.molgenis.data.validation.message;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.validation.constraint.DefaultValueReferenceConstraint;
import org.molgenis.data.validation.constraint.DefaultValueReferenceConstraintViolation;
import org.molgenis.util.UnexpectedEnumException;

import java.util.Collection;
import java.util.Optional;

import static java.util.stream.Collectors.joining;

class MessageGeneratorDefaultValueReference
{
	private MessageGeneratorDefaultValueReference()
	{
	}

	static ConstraintViolationMessage createMessage(DefaultValueReferenceConstraintViolation constraintViolation)
	{
		DefaultValueReferenceConstraint constraint = constraintViolation.getConstraint();
		EntityType entityType = constraintViolation.getEntityType();
		Optional<Object> entityId = constraintViolation.getEntityId();
		Optional<Collection<Attribute>> attributes = constraintViolation.getAttributes();

		String message;
		if (entityId.isPresent() && attributes.isPresent())
		{
			message = String.format("constraint:%s type:%s entity:%s attributes:[%s]", constraint.name(),
					entityType.getId(), entityId.toString(),
					attributes.get().stream().map(Attribute::getIdentifier).collect(joining(",")));
		}
		else
		{
			message = String.format("constraint:%s type:%s", constraint.name(), entityType.getId());
		}

		String errorCode;
		String localizedMessage;
		switch (constraint)
		{
			case REFERENCE_EXISTS:
				errorCode = "V__";
				// TODO '%s' entities are referenced as default value by attributes"
				localizedMessage = message; // TODO implement (something like: '%s' with id '%s' is referenced as default value by attribute(s): '%s')
				break;
			default:
				throw new UnexpectedEnumException(constraint);
		}

		return ConstraintViolationMessage.create(errorCode, message, localizedMessage);
	}
}
