package org.molgenis.data.validation.message;

import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.validation.constraint.EntityTypeConstraint;
import org.molgenis.data.validation.constraint.EntityTypeConstraintViolation;
import org.molgenis.util.UnexpectedEnumException;

class MessageGeneratorEntityType
{
	private MessageGeneratorEntityType()
	{
	}

	static ConstraintViolationMessage createMessage(EntityTypeConstraintViolation constraintViolation)
	{
		EntityType entityType = constraintViolation.getEntityType();
		EntityTypeConstraint entityTypeConstraint = constraintViolation.getConstraint();
		String message = String.format("constraint:%s type:%s", entityTypeConstraint.name(), entityType.getId());

		String errorCode;
		String localizedMessage;
		switch (entityTypeConstraint)
		{
			case LOOKUP_ATTRIBUTES_EXIST:
				errorCode = "Vzy";
				localizedMessage = message; // TODO implement (something like: Lookup attribute [%s] is not part of the entity attributes)
				break;
			case LABEL_ATTRIBUTE_EXISTS:
				errorCode = "Vyx";
				localizedMessage = message; // TODO implement (Label attribute [%s] is not part of the entity attributes)
				break;
			case ID_ATTRIBUTE_EXISTS:
				errorCode = "Vxw";
				localizedMessage = message; // TODO implement (Entity [%s] ID attribute [%s] is not part of the entity attributes)
				break;
			case ID_ATTRIBUTE_TYPE:
				errorCode = "Vxw";
				localizedMessage = message; // TODO implement (Entity [%s] ID attribute [%s] type [%s] is not allowed)
				break;
			case ID_ATTRIBUTE_UNIQUE:
				errorCode = "Vxw";
				localizedMessage = message; // TODO implement (Entity [%s] ID attribute [%s] is not a unique attribute)
				break;
			case ID_ATTRIBUTE_NOT_NULL:
				errorCode = "Vwv";
				localizedMessage = message; // TODO implement (Entity [%s] ID attribute [%s] is not a non-nillable attribute)
				break;
			case ID_ATTRIBUTE_REQUIRED:
				errorCode = "Vwv";
				localizedMessage = message; // TODO implement (Entity [%s] is missing required ID attribute)
				break;
			case BACKEND_EXISTS:
				errorCode = "Vvu";
				localizedMessage = message; // TODO implement (Unknown backend [%s])
				break;
			case HAS_ATTRIBUTES:
				errorCode = "";
				localizedMessage = message; // TODO implement (Entity [%s] does not contain any attributes. Did you use the correct package+entity name combination in both the entities as well as the attributes sheet?)
				break;
			case ATTRIBUTES_UNIQUE:
				errorCode = "";
				localizedMessage = message; // TODO implement (Entity [%s] contains multiple attributes with name [%s])
				break;
			case ATTRIBUTE_IN_PARENT:
				errorCode = "";
				localizedMessage = message; // TODO implement (An attribute with name [%s] already exists in entity [%s] or one of its parents)
				break;
			case EXTENDS_NOT_ABSTRACT:
				errorCode = "";
				localizedMessage = message; // TODO implement (EntityType [%s] is not abstract; EntityType [%s] can't extend it)
				break;
			case NAME:
				errorCode = "";
				localizedMessage = message; // TODO implement (see NameValidator.validateEntityName)
				break;
			case LABEL_NOT_EMPTY:
				errorCode = "";
				localizedMessage = message; // TODO implement (Label of EntityType [%s] is empty)
				break;
			case LABEL_NOT_WHITESPACE_ONLY:
				errorCode = "";
				localizedMessage = message; // TODO implement (Label of EntityType [%s] contains only white space)
				break;
			case PACKAGE_NOT_SYSTEM:
				errorCode = "";
				localizedMessage = message; // TODO implement (Adding entity [%s] to system package [%s] is not allowed)
				break;
			default:
				throw new UnexpectedEnumException(entityTypeConstraint);
		}
		return ConstraintViolationMessage.create(errorCode, message, localizedMessage);
	}
}
