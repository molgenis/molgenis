package org.molgenis.data.validation.meta;

import org.molgenis.data.meta.model.Tag;
import org.molgenis.data.semantic.Relation;
import org.molgenis.data.validation.ValidationException;
import org.molgenis.data.validation.constraint.TagConstraint;
import org.molgenis.data.validation.constraint.TagConstraintViolation;
import org.springframework.stereotype.Component;

/**
 * {@link org.molgenis.data.meta.model.Tag Tag} validator
 */
@Component
public class TagValidator
{
	/**
	 * Validates tag
	 *
	 * @param tag tag
	 * @throws ValidationException if tag is not valid
	 */
	@SuppressWarnings("MethodMayBeStatic")
	public void validate(Tag tag)
	{
		String relationIri = tag.getRelationIri();
		Relation relation = Relation.forIRI(relationIri);
		if (relation == null)
		{
			throw new ValidationException(new TagConstraintViolation(TagConstraint.UNKNOWN_RELATION_IRI, tag));
		}
	}
}
