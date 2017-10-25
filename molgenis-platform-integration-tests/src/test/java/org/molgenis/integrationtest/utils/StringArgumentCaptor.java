package org.molgenis.integrationtest.utils;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 * <p>You can use this class to resolve JSON objects from responses in MockMvc.</p>
 */
public class StringArgumentCaptor extends BaseMatcher<String>
{
	private String capturedObject = null;

	public StringArgumentCaptor()
	{
	}

	@Override
	public void describeTo(Description description)
	{
		description.appendText("String argument captor");
	}

	@Override
	public boolean matches(Object o)
	{
		capturedObject = (String) o;
		return true;
	}

	/**
	 * <p></p>
	 *
	 * @return catpured object
	 */
	public String capture()
	{
		return this.capturedObject;
	}
}
