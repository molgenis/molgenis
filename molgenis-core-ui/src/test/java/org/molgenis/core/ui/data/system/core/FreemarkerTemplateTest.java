package org.molgenis.core.ui.data.system.core;

import org.mockito.Mockito;
import org.molgenis.data.meta.model.EntityType;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class FreemarkerTemplateTest
{
	@Test
	public void testGetNameWithoutExtensionEndsWithFtl() throws Exception
	{
		FreemarkerTemplate freemarkerTemplate = getFreemarkerTemplateSpy();
		when(freemarkerTemplate.getName()).thenReturn("template.ftl");
		assertEquals(freemarkerTemplate.getNameWithoutExtension(), "template");
	}

	@Test
	public void testGetNameWithoutExtensionNotEndsWithFtl() throws Exception
	{
		FreemarkerTemplate freemarkerTemplate = getFreemarkerTemplateSpy();
		when(freemarkerTemplate.getName()).thenReturn("template");
		assertEquals(freemarkerTemplate.getNameWithoutExtension(), "template");
	}

	private FreemarkerTemplate getFreemarkerTemplateSpy()
	{
		EntityType entityType = mock(EntityType.class);
		return Mockito.spy(new FreemarkerTemplate(entityType));
	}
}