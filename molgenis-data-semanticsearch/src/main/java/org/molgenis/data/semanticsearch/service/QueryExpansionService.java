package org.molgenis.data.semanticsearch.service;

import org.molgenis.data.QueryRule;
import org.molgenis.data.semanticsearch.service.bean.SearchParam;

public interface QueryExpansionService
{
	QueryRule expand(SearchParam searchParam);
}
