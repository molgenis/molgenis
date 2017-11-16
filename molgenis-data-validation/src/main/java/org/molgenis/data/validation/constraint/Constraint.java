package org.molgenis.data.validation.constraint;

/**
 * Limitation placed on data.
 */
public interface Constraint
{
	/**
	 * Type of limitation placed on data (e.g. entity type, attribute, attribute value)
	 */
	ConstraintType getType();
}
