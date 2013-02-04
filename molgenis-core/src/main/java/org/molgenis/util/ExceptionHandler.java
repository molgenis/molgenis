package org.molgenis.util;

import java.io.PrintStream;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

/**
 * The idea is to use this in every exception, then exception is handle in the
 * same way. At the moment after every exception System.exit(1) is called. This
 * should be changed in the future, for example in test & development but
 * continue in production (some flag in for example molgenis.properties, could
 * be an idea).
 * 
 * @author Joris Lops
 */
public class ExceptionHandler
{
	public static void handle(Throwable t, Logger l)
	{
		printSimpleStackTrace(t, l);
		l.error("Detailed stack trace:");
		ExceptionUtils.printRootCauseStackTrace(t);
	}

	public static void handle(Throwable t)
	{
		handle(t, System.err);
	}

	public static void handle(Throwable t, PrintStream out)
	{
		printSimpleStackTrace(t, out);
		out.println("Detailed stack trace:");
		ExceptionUtils.printRootCauseStackTrace(t);
	}

	private static void printSimpleStackTrace(Throwable t, PrintStream o)
	{
		Throwable cause = t.getCause();
		Throwable prevCause = null;
		StringBuilder tabsBuilder = new StringBuilder();
		while (cause != null && prevCause != cause)
		{
			o.println((String.format("%sCause: %s", tabsBuilder.toString(), cause.getMessage())));
			tabsBuilder.append('\t');

			prevCause = cause;
			cause = cause.getCause();
		}
	}

	private static void printSimpleStackTrace(Throwable t, Logger l)
	{
		Throwable cause = t.getCause();
		Throwable prevCause = null;
		StringBuilder tabsBuilder = new StringBuilder();
		while (cause != null && prevCause != cause)
		{
			l.error(String.format("%sCause: %s", tabsBuilder.toString(), cause.getMessage()));
			tabsBuilder.append('\t');

			prevCause = cause;
			cause = cause.getCause();
		}
	}
}
