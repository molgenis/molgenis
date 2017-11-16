package org.molgenis.data.validation.constraint;

import org.molgenis.data.AttributeValue;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.Tag;
import org.molgenis.util.UnexpectedEnumException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.joining;

public class MessageConstraintViolationVisitor implements ConstraintViolationVisitor
{
	private List<ConstraintViolationMessage> constraintViolationMessages;

	public MessageConstraintViolationVisitor()
	{
		constraintViolationMessages = new ArrayList<>();
	}

	@Override
	public void visit(PackageConstraintViolation packageConstraintViolation)
	{
		Package aPackage = packageConstraintViolation.getPackage();
		PackageConstraint packageConstraint = packageConstraintViolation.getConstraint();
		String message = String.format("constraint:%s type:%s", packageConstraint.name(), aPackage.getId());

		String errorCode;
		String localizedMessage;
		switch (packageConstraint)
		{
			case SYSTEM_PACKAGE_READ_ONLY:
				errorCode = "V667";
				localizedMessage = message; // TODO implement (something like: Modifying system packages is not allowed)
				break;
			default:
				throw new UnexpectedEnumException(packageConstraint);
		}
		constraintViolationMessages.add(ConstraintViolationMessage.create(errorCode, message, localizedMessage));
	}

	@Override
	public void visit(EntityTypeConstraintViolation entityTypeConstraintViolation)
	{
		EntityType entityType = entityTypeConstraintViolation.getEntityType();
		EntityTypeConstraint entityTypeConstraint = entityTypeConstraintViolation.getConstraint();
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
		constraintViolationMessages.add(ConstraintViolationMessage.create(errorCode, message, localizedMessage));
	}

	@Override
	public void visit(AttributeConstraintViolation attributeConstraintViolation)
	{
		Attribute attribute = attributeConstraintViolation.getAttribute();
		AttributeConstraint attributeConstraint = attributeConstraintViolation.getConstraint();
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
		constraintViolationMessages.add(ConstraintViolationMessage.create(errorCode, message, localizedMessage));
	}

	@Override
	public void visit(TagConstraintViolation tagConstraintViolation)
	{
		Tag tag = tagConstraintViolation.getTag();
		TagConstraint tagConstraint = tagConstraintViolation.getConstraint();
		String message = String.format("constraint:%s tag:%s", tagConstraintViolation.getConstraint().name(),
				tag.getId());

		String errorCode;
		String localizedMessage;
		switch (tagConstraint)
		{
			case UNKNOWN_RELATION_IRI:
				errorCode = "V666";
				localizedMessage = message; // TODO implement Unknown relation IRI [%s]
				break;
			default:
				throw new UnexpectedEnumException(tagConstraint);
		}
		constraintViolationMessages.add(ConstraintViolationMessage.create(errorCode, message, localizedMessage));
	}

	@Override
	public void visit(AttributeValueConstraintViolation attributeValueConstraintViolation)
	{
		AttributeValue attributeValue = attributeValueConstraintViolation.getAttributeValue();
		Attribute attribute = attributeValue.getAttribute();
		String message = String.format("constraint:%s type:%s attribute:%s value:%s",
				attributeValueConstraintViolation.getConstraint().name(), attribute.getEntity().getId(),
				attribute.getIdentifier(), attributeValue.toString());

		String errorCode;
		String localizedMessage;
		switch (attributeValueConstraintViolation.getConstraint())
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
				throw new UnexpectedEnumException(attributeValueConstraintViolation.getConstraint());
		}
		constraintViolationMessages.add(ConstraintViolationMessage.create(errorCode, message, localizedMessage));
	}

	@Override
	public void visit(DefaultValueReferenceConstraintViolation entityConstraintViolation)
	{
		DefaultValueReferenceConstraint entityConstraint = entityConstraintViolation.getConstraint();
		EntityType entityType = entityConstraintViolation.getEntityType();
		Optional<Object> entityId = entityConstraintViolation.getEntityId();
		Optional<Collection<Attribute>> attributes = entityConstraintViolation.getAttributes();

		String message;
		if (entityId.isPresent() && attributes.isPresent())
		{
			message = String.format("constraint:%s type:%s entity:%s attributes:[%s]",
					entityConstraint.getType().name(), entityType.getId(), entityId.toString(),
					attributes.get().stream().map(Attribute::getIdentifier).collect(joining(",")));
		}
		else
		{
			message = String.format("constraint:%s type:%s", entityConstraint.getType().name(), entityType.getId());
		}

		String errorCode;
		String localizedMessage;
		switch (entityConstraint)
		{
			case REFERENCE_EXISTS:
				errorCode = "V__";
				// TODO '%s' entities are referenced as default value by attributes"
				localizedMessage = message; // TODO implement (something like: '%s' with id '%s' is referenced as default value by attribute(s): '%s')
				break;
			default:
				throw new UnexpectedEnumException(entityConstraint);
		}

		constraintViolationMessages.add(ConstraintViolationMessage.create(errorCode, message, localizedMessage));
	}

	public List<ConstraintViolationMessage> getConstraintViolationMessages()
	{
		return constraintViolationMessages;
	}
}
