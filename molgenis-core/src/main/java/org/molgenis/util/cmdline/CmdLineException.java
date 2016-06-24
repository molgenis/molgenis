/**
 * File: util/cmdline/CmdLineException.java <br>
 * Copyright: Inventory 2000-2006, GBIC 2005, all rights reserved <br>
 * Changelog:
 * <ul>
 * <li>2005-11-16; 1.0.0; RA Scheltema; Creation.
 * </ul>
 */

package org.molgenis.util.cmdline;

// imports

/**
 * The general exception-class for the command-line parser. This class provides
 * a specialization of the Exception-class and typically only introduces the
 * class-name.
 * 
 * @author RA Scheltema
 * @version 1.0.0
 */
public class CmdLineException extends Exception
{
	/** The version-id used to distinguish between two versions of this class */
	private static final long serialVersionUID = -8572474027909970735L;

	/**
	 * Standard constructor accepting a string containing the message. The
	 * message is stored in the class and can be retrieved with the method
	 * getMessage.
	 * 
	 * @param message
	 *            The message linked to this exception.
	 */
	public CmdLineException(final String message)
	{
		super(message);
	}
}
