package org.molgenis.data.validation.meta;

import org.molgenis.data.meta.model.Tag;
import org.molgenis.data.semantic.Relation;
import org.molgenis.data.validation.constraint.TagValidationResult;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.EnumSet;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.validation.constraint.TagConstraint.UNKNOWN_RELATION_IRI;
import static org.testng.Assert.assertEquals;

public class TagValidatorTest
{
	private TagValidator tagValidator;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		tagValidator = new TagValidator();
	}

	@Test
	public void validateValid() throws Exception
	{
		Tag tag = mock(Tag.class);
		when(tag.getRelationIri()).thenReturn(Relation.isRealizationOf.getIRI());
		assertEquals(tagValidator.validate(tag), TagValidationResult.create(tag));
	}

	@Test
	public void validateInvalid() throws Exception
	{
		Tag tag = when(mock(Tag.class).getId()).thenReturn("id").getMock();
		when(tag.getRelationIri()).thenReturn("blaat");
		assertEquals(tagValidator.validate(tag), TagValidationResult.create(tag, EnumSet.of(UNKNOWN_RELATION_IRI)));
	}
}