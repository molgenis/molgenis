package org.molgenis.data.validation.meta;

import org.molgenis.data.meta.model.Tag;
import org.molgenis.data.semantic.Relation;
import org.molgenis.data.validation.MolgenisValidationException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
		tagValidator.validate(tag);
	}

	@Test(expectedExceptions = MolgenisValidationException.class)
	public void validateInvalid() throws Exception
	{
		Tag tag = mock(Tag.class);
		when(tag.getRelationIri()).thenReturn("blaat");
		tagValidator.validate(tag);
	}
}