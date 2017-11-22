package org.molgenis.data.validation.meta;

import org.molgenis.data.meta.model.Tag;
import org.molgenis.data.semantic.Relation;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

import static org.molgenis.data.validation.meta.TagConstraint.UNKNOWN_RELATION_IRI;

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
	 * @return constraint violation collection or empty collection if tag is valid
	 */
	@SuppressWarnings("MethodMayBeStatic")
	public TagValidationResult validate(Tag tag)
	{
		EnumSet<TagConstraint> constraintViolations = EnumSet.noneOf(TagConstraint.class);

		if (!isValidRelationIri(tag))
		{
			constraintViolations.add(UNKNOWN_RELATION_IRI);
		}

		return TagValidationResult.create(tag, constraintViolations);
	}

	private static boolean isValidRelationIri(Tag tag)
	{
		String relationIri = tag.getRelationIri();
		Relation relation = Relation.forIRI(relationIri);
		return relation != null;
	}
}
