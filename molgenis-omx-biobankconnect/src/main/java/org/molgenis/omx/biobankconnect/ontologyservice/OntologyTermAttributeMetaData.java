package org.molgenis.omx.biobankconnect.ontologyservice;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.fieldtypes.FieldType;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;
import org.molgenis.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;

public class OntologyTermAttributeMetaData implements AttributeMetaData
{
	@Autowired
	private SearchService searchService;

	private final Hit ontologyTerm;
	private final String nodePath;
	private final String ontologyUrl;

	public OntologyTermAttributeMetaData(Hit ontologyTerm)
	{
		if (ontologyTerm == null) throw new IllegalArgumentException("ontologyTerm is null");
		this.ontologyTerm = ontologyTerm;
		this.nodePath = ontologyTerm.getColumnValueMap().get("nodePath").toString();
		this.ontologyUrl = ontologyTerm.getColumnValueMap().get("ontologyIRI").toString();
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
	public FieldType getDataType()
	{
		return MolgenisFieldTypes.getType(FieldTypeEnum.COMPOUND.toString().toLowerCase());
	}

	@Override
	public boolean isNillable()
	{
		return true;
	}

	@Override
	public boolean isReadonly()
	{
		return false;
	}

	@Override
	public boolean isUnique()
	{
		return false;
	}

	@Override
	public boolean isVisible()
	{
		return true;
	}

	@Override
	public Object getDefaultValue()
	{
		return null;
	}

	@Override
	public boolean isIdAtrribute()
	{
		return false;
	}

	@Override
	public boolean isLabelAttribute()
	{
		return false;
	}

	@Override
	public EntityMetaData getRefEntity()
	{
		return new OntologyTermEntityMetaData(ontologyTerm);
	}

	@Override
	public Iterable<AttributeMetaData> getAttributeParts()
	{
		List<AttributeMetaData> lists = new ArrayList<AttributeMetaData>();
		Query q = new QueryImpl().like("nodePath", nodePath).pageSize(5000);
		SearchResult result = searchService.search(new SearchRequest(OntologyService.createOntologyTermDocumentType(ontologyUrl),
				q, null));
		Iterator<Hit> iterator = result.iterator();
		while (iterator.hasNext())
		{
			Hit hit = iterator.next();
			if (OntologyTermEntityMetaData.isParent(nodePath, hit.getColumnValueMap().get("nodePath").toString()))
			{
				lists.add(new OntologyTermAttributeMetaData(hit));
			}
		}
		return lists;
	}

	@Override
	public boolean isAuto()
	{
		return false;
	}

	@Override
	public boolean isLookupAttribute()
	{
		return false;
	}
}
