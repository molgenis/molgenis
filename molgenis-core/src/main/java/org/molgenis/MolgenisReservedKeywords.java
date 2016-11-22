package org.molgenis;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Collection of reserved keywords used by MOLGENIS.
 */
public class MolgenisReservedKeywords
{
	/**
	 * Case sensitive
	 */
	public static final Set<String> MOLGENIS_KEYWORDS = Sets
			.newHashSet("login", "logout", "csv", "base", "exist", "meta");

	/**
	 * Elasticsearch
	 * <p>
	 * "type"
	 * 1. Issue reported in MOLGENIS: Sort Data type results in error #5573
	 * 2. Issue reported in elasticsearch: Query with a search value and a sort on field name type, results in error. #21725
	 */
	public static final Set<String> ELASTICSEARCH_1_7_3_KEYWORDS = Sets.newHashSet("type");
}
