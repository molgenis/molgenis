package org.molgenis.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

@ContextConfiguration(classes = GsonConfig.class)
public class GsonHttpMessageConverterTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private GsonHttpMessageConverter gsonHttpMessageConverter;

	// regression test for https://github.com/molgenis/molgenis/issues/3078
	@Test
	public void getSupportedMediaTypes()
	{
		boolean containsApplicationJsonWithoutCharset = false;
		for (MediaType mediaType : gsonHttpMessageConverter.getSupportedMediaTypes())
		{
			if (mediaType.getType().equals("application") && mediaType.getSubtype().equals("json")
					&& mediaType.getCharSet() == null)
			{
				containsApplicationJsonWithoutCharset = true;
				break;
			}
		}
		assertTrue(containsApplicationJsonWithoutCharset);
	}
}
