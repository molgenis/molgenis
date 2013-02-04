package org.molgenis.omx.decorators;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.molgenis.omx.core.Identifiable;
import org.molgenis.framework.db.DatabaseException;

/**
 * This class contains functions to enforce the XGAP naming policy for entities
 * and storing files that belong to certain entities.
 * 
 * @author joerivandervelde
 * 
 */
public class NameConvention
{
	/**
	 * Converts file name into its safe version, so that they can be safely
	 * stored on any file system. Trims, then converts uppercase alphabetic
	 * characters to lowercase. Then only leaves alphabetic characters, numbers
	 * and underscore in the output. The output is then truncated at 50
	 * characters. If the output ends up with length 0, a DatabaseException is
	 * thrown. Note that filenames are allowed to start with numerals, while
	 * entitynames are not. In practice however, to-be filenames are already
	 * checked as being valid entitynames first.
	 * 
	 * @param input
	 * @return
	 * @throws Exception
	 */
	public static String escapeFileName(String name) throws DatabaseException
	{
		// trim whitespace
		name = name.trim();

		// leave only lowercase alphbetics
		name = name.toLowerCase();

		// remove bad characters
		Pattern p1 = Pattern.compile("([a-z_0-9])");
		String output = "";
		for (char s : name.toCharArray())
		{
			Matcher m = p1.matcher(s + "");
			if (m.matches())
			{
				output += m.group();
			}
		}

		// truncate at 50 chars
		if (output.length() > 50)
		{
			output = output.substring(0, 50);
		}

		// check final length, may not be empty
		if (output.length() == 0)
		{
			throw new DatabaseException("Escaped filename of '" + name + "' is an empty string.");
		}

		return output;
	}

	/**
	 * Converts an entity name into its safe version, so that they can be used
	 * in contexts such as programming environments where strict names are
	 * needed. Trims, then leaves only alphabetic characters, numerals and
	 * underscores. Then removes any heading numerals. A DatabaseException is
	 * thrown if the final output has length 0. Eg. "123name" becomes "name",
	 * "x123nAmE" stays "x123nAmE", and "@#23" becomes "", which will throw an
	 * exception. This is the inverse of 'validateEntityNameStrict'.
	 * 
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public static String escapeEntityNameStrict(String name) throws DatabaseException
	{
		// trim whitespace
		name = name.trim();

		// remove bad characters
		String output = "";
		Pattern p1 = Pattern.compile("([a-zA-Z0-9_])");
		for (char s : name.toCharArray())
		{
			Matcher m = p1.matcher(s + "");
			if (m.matches())
			{
				output += m.group();
			}
		}

		// remove any heading numerals
		Pattern p2 = Pattern.compile("^[0-9]+(.+)");
		Matcher m = p2.matcher(output);
		if (m.matches())
		{
			output = m.group(1);
		}

		// FIX: when a number is the only character left, it will not be
		// matched! performing extra check here
		if (output.length() == 1)
		{
			Pattern p3 = Pattern.compile("([0-9])");
			Matcher m1 = p3.matcher(output);
			if (m1.matches())
			{
				output = "";
			}
		}

		// check final length, may not be empty
		if (output.length() == 0)
		{
			throw new DatabaseException("Escaped name of '" + name + "' is an empty string.");
		}

		return output;
	}

	/**
	 * Validates a file name, so that they can be safely stored on any file
	 * system. Throws a DatabaseException when input is empty or untrimmed, if
	 * other characters than lowercase alphabetics, numbers or underscore are
	 * used, or when the name is longer than 50 characters. Note that filenames
	 * are allowed to start with numerals, while entitynames are not. In
	 * practice however, to-be filenames are already checked as being valid
	 * entitynames first.
	 * 
	 * @throws DatabaseException
	 */
	public static void validateFileName(String name) throws DatabaseException
	{
		if (name.length() == 0)
		{
			throw new DatabaseException("File name is empty.");
		}

		if (name.length() != name.trim().length())
		{
			throw new DatabaseException("File name '" + name + "' is untrimmed.");
		}

		if (name.length() > 50)
		{
			throw new DatabaseException("File name is longer than 50 characters.");
		}

		// check for illegal characters
		Pattern p1 = Pattern.compile("([a-z_0-9])");
		for (char s : name.toCharArray())
		{
			Matcher m = p1.matcher(s + "");
			if (!m.matches())
			{
				throw new DatabaseException("Illegal character (" + s + ") in file name '" + name
						+ "'. Use only a-z, 0-9, and underscore.");
			}
		}
	}

	/**
	 * Validates an entity name, checking that only characters from the set
	 * [<>/a-zA-Z0-9_\\s\\-:.(),;\\+] are used.
	 * 
	 * @param name
	 * @throws DatabaseException
	 */
	public static void validateEntityName(String name) throws DatabaseException
	{
		// pattern for bbmri ([a-zA-Z0-9_\\s\\-:.(),;\\+])
		String pattern = "([<>/a-zA-Z0-9_\\s\\-:.(),;\\+\\*])";

		if (name == null || name.length() == 0)
		{
			throw new DatabaseException("Name is empty.");
		}

		if (name.length() != name.trim().length())
		{
			throw new DatabaseException("Name '" + name + "' is untrimmed.");
		}

		// check for illegal characters
		Pattern p2 = Pattern.compile(pattern);
		for (char s : name.toCharArray())
		{
			Matcher m2 = p2.matcher(s + "");
			if (!m2.matches())
			{
				throw new DatabaseException("Illegal character (" + s + ") in name '" + name
						+ "'. Use only allowed characters from the set " + pattern);
			}
		}
	}

	/**
	 * Validates an entity name, so that they can be used in contexts such as
	 * programming environments where strict names are needed. Throws a
	 * DatabaseException when this name is empty or untrimmed, if other
	 * characters than alphabetics, numerals or underscore are used, or when the
	 * name starts with a numeral.
	 * 
	 * @param name
	 * @throws DatabaseException
	 */
	public static void validateEntityNameStrict(String name) throws DatabaseException
	{
		if (name.length() == 0)
		{
			throw new DatabaseException("Name is empty.");
		}

		if (name.length() != name.trim().length())
		{
			throw new DatabaseException("Name '" + name + "' is untrimmed.");
		}

		// check if first character is a number
		Pattern p1 = Pattern.compile("([0-9])");
		String firstChar = name.substring(0, 1);
		Matcher m1 = p1.matcher(firstChar);
		if (m1.matches())
		{
			throw new DatabaseException("Name '" + name + "' is not allowed to start with a numeral (" + firstChar
					+ ").");
		}

		// check for illegal characters
		Pattern p2 = Pattern.compile("([a-zA-Z0-9_])");
		for (char s : name.toCharArray())
		{
			Matcher m2 = p2.matcher(s + "");
			if (!m2.matches())
			{
				throw new DatabaseException("Illegal character (" + s + ") in name '" + name
						+ "'. Use only a-z, A-Z, 0-9, and underscore.");
			}
		}
	}

	/**
	 * Validate names of Entities by wrapping
	 * NameConvention.validateEntityName(name)
	 * 
	 * @param <E>
	 * @param entities
	 * @throws DatabaseException
	 */
	public static <E extends Identifiable> void validateEntityNames(List<E> entities) throws DatabaseException
	{
		for (E i : entities)
		{
			String name = i.getName();
			NameConvention.validateEntityName(name);
		}
	}

	/**
	 * Validate names of Entities by wrapping
	 * NameConvention.validateEntityNameStrict(name)
	 * 
	 * @param <E>
	 * @param entities
	 * @throws DatabaseException
	 */
	public static <E extends Identifiable> void validateEntityNamesStrict(List<E> entities) throws DatabaseException
	{
		for (E i : entities)
		{
			String name = i.getName();
			NameConvention.validateEntityNameStrict(name);
		}
	}
}