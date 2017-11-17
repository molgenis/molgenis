package org.molgenis.data.validation.message;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.validation.constraint.AttributeConstraint;
import org.molgenis.data.validation.constraint.AttributeConstraintViolation;
import org.molgenis.util.UnexpectedEnumException;

class MessageGeneratorAttribute
{
	private MessageGeneratorAttribute()
	{
	}

	static ConstraintViolationMessage createMessage(AttributeConstraintViolation constraintViolation)
	{
		Attribute attribute = constraintViolation.getAttribute();
		AttributeConstraint attributeConstraint = constraintViolation.getConstraint();
		String message = String.format("constraint:%s type:%s attribute:%s", attributeConstraint.name(),
				attribute.getEntity().getId(), attribute.getIdentifier());

		String errorCode;
		String localizedMessage;
		switch (attributeConstraint)
		{
			case COMPOUND_PARENT:
				errorCode = "Vlm";
				localizedMessage = message; // TODO implement (something like: Parent attribute [%s] of attribute [%s] is not of type compound)
				break;
			case DEFAULT_VALUE_EMAIL:
				errorCode = "Vmn";
				localizedMessage = message; // TODO implement (something like: Default value [%s] is not a valid email address)
				break;
			case DEFAULT_VALUE_ENTITY_REFERENCE:
				errorCode = "Vno";
				localizedMessage = message; // TODO implement (something like: Default value [%s] refers to an unknown entity / Default value [%s] refers to one or more unknown entities)
				break;
			case DEFAULT_VALUE_ENUM:
				errorCode = "Vop";
				localizedMessage = message; // TODO implement (something like: Invalid default value [%s] for enum [%s] value must be one of %s)
				break;
			case DEFAULT_VALUE_HYPERLINK:
				errorCode = "Vpq";
				localizedMessage = message; // TODO implement (something like: Default value [%s] is not a valid hyperlink.)
				break;
			case DEFAULT_VALUE_MAX_LENGTH: // TODO implement (something like: "Default value for attribute [%s] exceeds the maximum length for datatype %s)
				errorCode = "Vqr";
				localizedMessage = message;
				break;
			case DEFAULT_VALUE_TYPE: // TODO implement (something like: Invalid default value [%s] for data type [%s])
				errorCode = "Vrs";
				localizedMessage = message;
				break;
			case MAPPED_BY_REFERENCE: // TODO implement (something like: Invalid mappedBy attribute [%s] data type [%s].)
				errorCode = "Vst";
				localizedMessage = message; // TODO implement (something like: mappedBy attribute [%s] is not part of entity [%s].)
				break;
			case MAPPED_BY_TYPE:
				errorCode = "Vtu";
				localizedMessage = message;
				break;
			case NAME: // TODO implement (something like: Invalid characters in: [%s] Only letters (a-z, A-Z), digits (0-9), underscores (_) and hashes (#) are allowed.
				errorCode = "Vuv";
				localizedMessage = message;
				break;
			case NON_COMPOUND_CHILDREN: // TODO implement (something like: Attribute [%s] is not of type COMPOUND and can therefor not have children)
				errorCode = "Vvw";
				localizedMessage = message;
				break;
			case ORDER_BY_REFERENCE: // TODO implement (something like: Unknown entity [%s] attribute [%s] referred to by entity [%s] attribute [%s] sortBy [%s])
				errorCode = "Vwx";
				localizedMessage = message;
				break;
			case TYPE_UPDATE_BIDIRECTIONAL: // TODO implement (something like: Attribute data type change not allowed for bidirectional attribute [%s])
				errorCode = "Vxy";
				localizedMessage = message;
				break;
			case TYPE_UPDATE: // TODO implement (something like: Attribute data type update from [%s] to [%s] not allowed, allowed types are %s)
				errorCode = "Vyz";
				localizedMessage = message;
				break;
			default:
				throw new UnexpectedEnumException(attributeConstraint);
		}
		return ConstraintViolationMessage.create(errorCode, message, localizedMessage);
	}
}
