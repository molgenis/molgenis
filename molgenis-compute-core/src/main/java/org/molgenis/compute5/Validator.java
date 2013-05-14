package org.molgenis.compute5;

public class Validator
{
	public static void validateParameterName(String name)
	{
		if (!name.matches("^[a-zA-Z0-9]+$"))
		{
			System.err.println(">> ERROR >> Parameter '" + name + "' is not allowed. Allowed characters are: a-zA-Z0-9.");
			System.err.println(">> System exits with status code 1");
			System.exit(1);
		}
	}
}
