package org.molgenis.security.core;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;

import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

/**
 * Permission to perform a specific action on a resource type.
 */
public interface Permission
{
	String name();

	String getDefaultDescription();

	default String getType()
	{
		return getClass().getSimpleName();
	}

	default MessageSourceResolvable getDescription()
	{
		String code = Stream.of("permission", getType(), name(), "description").collect(joining("."));
		return new DefaultMessageSourceResolvable(new String[] { code }, null, getDefaultDescription());
	}

	default MessageSourceResolvable getName()
	{
		String code = Stream.of("permission", getType(), name(), "name").collect(joining("."));
		return new DefaultMessageSourceResolvable(new String[] { code }, null, name());
	}
}
