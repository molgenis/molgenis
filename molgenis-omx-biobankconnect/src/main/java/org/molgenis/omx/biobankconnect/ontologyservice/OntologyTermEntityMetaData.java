package org.molgenis.omx.biobankconnect.ontologyservice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.support.AbstractEntityMetaData;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;
import org.molgenis.search.SearchService;
import org.molgenis.util.ApplicationContextProvider;

public class OntologyTermEntityMetaData extends AbstractEntityMetaData
{
	private final SearchService searchService;

	private final Hit ontologyTerm;
	private final String nodePath;
	private final String ontologyUrl;

	private transient Iterable<AttributeMetaData> cachedAttributes;

	public OntologyTermEntityMetaData(Hit ontologyTerm)
	{
		if (ontologyTerm == null) throw new IllegalArgumentException("ontologyTerm is null");
		this.ontologyTerm = ontologyTerm;
		nodePath = ontologyTerm.getColumnValueMap().get("nodePath").toString();
		ontologyUrl = ontologyTerm.getColumnValueMap().get("ontologyIRI").toString();
		searchService = ApplicationContextProvider.getApplicationContext().getBean(SearchService.class);
	}

	@Override
	public String getName()
	{
		return ontologyTerm.getColumnValueMap().get("ontologyTermIRI").toString();
	}

	@Override
	public String getLabel()
	{
		return ontologyTerm.getColumnValueMap().get("ontologyTerm").toString();
	}

	@Override
	public String getDescription()
	{
		return StringUtils.EMPTY;
	}

	@Override
	public Iterable<AttributeMetaData> getAttributes()
	{
		if (cachedAttributes == null)
		{
			Query q = new QueryImpl().like("nodePath", nodePath).pageSize(5000);
			SearchResult result = searchService.search(new SearchRequest(OntologyService
					.createOntologyTermDocumentType(ontologyUrl), q, null));
			Iterator<Hit> iterator = result.iterator();
			List<AttributeMetaData> lists = new ArrayList<AttributeMetaData>();
			while (iterator.hasNext())
			{
				Hit hit = iterator.next();
				if (isParent(nodePath, hit.getColumnValueMap().get("nodePath").toString()))
				{
					lists.add(new OntologyTermAttributeMetaData(hit));
				}
			}
			if (lists.size() > 0) cachedAttributes = lists;
		}
		return cachedAttributes;
	}

	public static boolean isParent(String nodePath1, String nodePath2)
	{
		Set<String> path1 = new HashSet<String>(Arrays.asList(nodePath1.split("\\.")));
		Set<String> path2 = new HashSet<String>(Arrays.asList(nodePath2.split("\\.")));
		if (path1.size() + 1 != path2.size()) return false;
		path1.removeAll(path2);
		return path1.size() == 1;
	}
}