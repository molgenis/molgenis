/**
 * File: invengine.Version <br>
 * Copyright: Inventory 2000-2006, GBIC 2005, all rights reserved <br>
 * Changelog:
 * <ul>
 * <li>2005-11-30; 1.0.0; RA Scheltema; Creation.
 * <li>2006-04-15; 1.0.0; MA Swertz; Documentation.
 * </ul>
 */
package org.molgenis;

/**
 * Version counter for the invengine package. Enables inquiry of the current
 * api.
 */
public class Version
{
	// static members
	/**
	 * The major part of the version, which is changed for major interface
	 * changes
	 */
	public static final int MAJOR = 4;

	/**
	 * The minor part of the version, which is changed when interfaces have been
	 * added
	 */
	public static final int MINOR = 0;

	/** The maintenance part of the version, which is changed for bug-fixes */
	public static final int MAINTENANCE = 0;

	/** Flag to indicate that this is a testing release */
	public static final boolean TESTING = true;

	// static access methods
	/** string representation of the version */
	public static String convertToString()
	{
		if (!TESTING) return "" + MAJOR + "." + MINOR + "." + MAINTENANCE;
		else
			return "" + MAJOR + "." + MINOR + "." + MAINTENANCE + "-testing";
	}
}
