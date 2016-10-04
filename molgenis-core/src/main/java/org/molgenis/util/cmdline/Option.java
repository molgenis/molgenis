package org.molgenis.util.cmdline;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation type helps to describe a single commandline option. With it
 * information about the paramater name, usage, etc is stored. Based on this the
 * CmdLineParser class knows which choices to make when parsing the commandline
 * parameters.
 *
 * @author RA Scheltema
 * @version 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Option
{
	/**
	 * Describes the different types a command-line option can be. Based on this
	 * information the parser knows whether to look for an argument or not for
	 * the option.
	 */
	enum Type
	{
		/**
		 * The option never has an argument (e.g. -o)
		 */
		NO_ARGUMENT, /**
	 * The option always has an argument (-o argument)
	 */
	REQUIRED_ARGUMENT, /**
	 * The option could have an argument (-o -p _or_ -o argument -p)
	 */
	OPTIONAL_ARGUMENT
	}

	enum Param
	{
		BOOLEAN, INTEGER, DOUBLE, STRING, COLLECTION, FILEPATH, DIRPATH, PASSWORD, CLASS, ENUM, LOG4JLEVEL
	}

	/**
	 * Defines the name of the option. This can be a single character, but als a
	 * word.
	 */
	String name();// default "";

	/**
	 * What type of information is passed with this option, eg: filename,
	 * integer, etc.
	 */
	Param param();// default "";

	/**
	 * This is a description of how to use this option. All information can be
	 * gathered here.
	 */
	String usage() default "";

	/**
	 * The type of this option.
	 */
	Option.Type type();// default Option.Type.REQUIRED_ARGUMENT;
}
