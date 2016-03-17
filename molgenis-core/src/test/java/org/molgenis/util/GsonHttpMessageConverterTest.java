package org.molgenis.util;

import static org.testng.Assert.assertTrue;

import org.springframework.http.MediaType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.gson.Gson;

public class GsonHttpMessageConverterTest
{
	private GsonHttpMessageConverter gsonHttpMessageConverter;

	@BeforeMethod
	public void setUp()
	{
		Gson gson = new Gson();
		gsonHttpMessageConverter = new GsonHttpMessageConverter(gson);
	}

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
