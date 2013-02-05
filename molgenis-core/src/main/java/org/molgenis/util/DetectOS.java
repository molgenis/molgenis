package org.molgenis.util;

public class DetectOS
{
	// possible outcomes:
	/**
	 * AIX -> unix Digital Unix -> unix FreeBSD -> unix HP UX -> unix Irix ->
	 * unix Linux -> unix Mac OS -> mac Mac OS X -> mac MPE/iX -> unix Netware
	 * 4.11 -> unix OS/2 -> unixlike Solaris -> unixlike Windows 2000 -> windows
	 * Windows 7 -> windows Windows 95 -> windowslegacy Windows 98 ->
	 * windowslegacy Windows NT -> windows Windows Vista -> windows Windows XP
	 * -> windows
	 */

	/**
	 * Get the current OS Java is running on. Since there are many
	 * possibilities, they are cast to five types, of which each type is
	 * expected to behave the same way. Possible outcomes are: unix, mac,
	 * unixlike, windows, windowslegacy. This way it is also possible to wrap
	 * multiple types by using eg String.startsWith("windows") or
	 * String.startsWith("unix"). See class for more details and casting
	 * procedure.
	 */
	public static String getOS()
	{
		String os = System.getProperty("os.name").toLowerCase();

		if (os.indexOf("windows 9") > -1)
		{
			return "windowslegacy";
		}

		else if (os.indexOf("windows") > -1)
		{
			return "windows";
		}

		if (os.indexOf("mac") > -1)
		{
			return "mac";
		}

		else
		{
			return "unix";
		}

		// throw new Exception("OS detection failed");

	}

	/**
	 * Get OS dependent line separator
	 * 
	 * @return
	 */
	public static String getLineSeparator()
	{
		return System.getProperty("line.separator");
	}
}
