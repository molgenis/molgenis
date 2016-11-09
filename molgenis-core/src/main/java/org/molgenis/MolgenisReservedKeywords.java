package org.molgenis;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Collection of reserved keywords used by MOLGENIS.
 */
public class MolgenisReservedKeywords
{
	// Case sensitive
	public static final Set<String> MOLGENIS_KEYWORDS = Sets
			.newHashSet("login", "logout", "csv", "base", "exist", "meta");
}
