package org.molgenis.data.validation.meta;

import org.molgenis.data.meta.model.Tag;
import org.molgenis.data.semantic.Relation;
import org.molgenis.data.validation.constraint.TagConstraintViolation;
import org.springframework.stereotype.Component;

import java.util.Collection;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.molgenis.data.validation.constraint.TagConstraint.UNKNOWN_RELATION_IRI;

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
	public Collection<TagConstraintViolation> validate(Tag tag)
	{
		String relationIri = tag.getRelationIri();
		Relation relation = Relation.forIRI(relationIri);
		if (relation == null)
		{
			return singletonList(new TagConstraintViolation(UNKNOWN_RELATION_IRI, tag));
		}
		return emptyList();
	}
}
