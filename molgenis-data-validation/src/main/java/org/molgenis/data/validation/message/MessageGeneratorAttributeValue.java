package org.molgenis.data.validation.message;

import org.molgenis.data.AttributeValue;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.validation.constraint.AttributeValueConstraintViolation;
import org.molgenis.util.UnexpectedEnumException;

class MessageGeneratorAttributeValue
{
	private MessageGeneratorAttributeValue()
	{
	}

	static ConstraintViolationMessage createMessage(AttributeValueConstraintViolation constraintViolation)
	{
		AttributeValue attributeValue = constraintViolation.getAttributeValue();
		Attribute attribute = attributeValue.getAttribute();
		String message = String.format("constraint:%s type:%s attribute:%s value:%s",
				constraintViolation.getConstraint().name(), attribute.getEntity().getId(), attribute.getIdentifier(),
				attributeValue.toString());

		String errorCode;
		String localizedMessage;
		switch (constraintViolation.getConstraint())
		{

			case EMAIL:
				errorCode = "Vab";
				localizedMessage = message;
				break;
			case ENTITY_REFERENCE:
				errorCode = "Vbc";
				localizedMessage = message; // TODO implement (something like: Unknown xref value '%s' for attribute '%s' of entity '%s'.)
				break;
			case ENUM:
				errorCode = "Vcd";
				localizedMessage = message;
				break;
			case EXPRESSION:
				errorCode = "Vde";
				localizedMessage = message;
				break;
			case HYPERLINK:
				errorCode = "Vef";
				localizedMessage = message;
				break;
			case MAX_LENGTH:
				errorCode = "Vfg";
				localizedMessage = message;
				break;
			case NOT_NULL:
				errorCode = "Vgh";
				localizedMessage = message;
				break;
			case RANGE:
				errorCode = "Vhi";
				localizedMessage = message;
				break;
			case READ_ONLY:
				errorCode = "Vij";
				localizedMessage = message;
				break;
			case TYPE:
				errorCode = "Vjk";
				localizedMessage = message;
				break;
			case UNIQUE:
				errorCode = "Vkl";
				localizedMessage = message;
				break;
			default:
				throw new UnexpectedEnumException(constraintViolation.getConstraint());
		}
		return ConstraintViolationMessage.create(errorCode, message, localizedMessage);
	}
}
