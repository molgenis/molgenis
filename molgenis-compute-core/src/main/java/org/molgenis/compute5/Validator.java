package org.molgenis.compute5;

public class Validator
{
	public static void validateParameterName(String name) throws ComputeException
	{
		if (!name.matches("^[a-zA-Z0-9]+$") || !name.substring(0,1).matches("[^0-9]"))
		{
			String result = ">> ERROR >> Parameter '" + name + "' is not allowed. Allowed characters are: a-zA-Z0-9";
			throw new ComputeException(result);
		}
	}
}
