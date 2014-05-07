package org.molgenis.data.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.springframework.data.domain.Sort;

public class QueryImpl implements Query
{
	private final List<List<QueryRule>> rules = new ArrayList<List<QueryRule>>();

	private int pageSize;
	private int offset;
	private Sort sort;

	public QueryImpl()
	{
		this.rules.add(new ArrayList<QueryRule>());
	}

	public QueryImpl(Query q)
	{
		this();
		for (QueryRule rule : q.getRules())
		{
			addRule(rule);
		}
		this.pageSize = q.getPageSize();
		this.offset = q.getOffset();
		this.sort = q.getSort();
	}

	public QueryImpl(QueryRule queryRule)
	{
		this(Arrays.asList(queryRule));
	}

	public QueryImpl(List<QueryRule> queryRules)
	{
		this.rules.add(new ArrayList<QueryRule>(queryRules));
	}

	public void addRule(QueryRule rule)
	{
		this.rules.get(this.rules.size() - 1).add(rule);
	}

	@Override
	public List<QueryRule> getRules()
	{
		if (this.rules.size() > 1) throw new MolgenisDataException(
				"Nested query not closed, use unnest() or unnestAll()");

		if (this.rules.size() > 0)
		{
			List<QueryRule> rules = this.rules.get(this.rules.size() - 1);

			return Collections.unmodifiableList(rules);
		}
		else return Collections.emptyList();
	}

	public void setPageSize(int pageSize)
	{
		this.pageSize = pageSize;
	}

	@Override
	public int getPageSize()
	{
		return pageSize;
	}

	@Override
	public int getOffset()
	{
		return offset;
	}

	public void setOffset(int offset)
	{
		this.offset = offset;
	}

	public void setSort(Sort sort)
	{
		this.sort = sort;
	}

	@Override
	public Sort getSort()
	{
		return sort;
	}

	@Override
	public Query search(String searchTerms)
	{
		rules.get(this.rules.size() - 1).add(new QueryRule(Operator.SEARCH, searchTerms));
		return this;
	}

	@Override
	public Query or()
	{
		rules.get(this.rules.size() - 1).add(new QueryRule(Operator.OR));
		return this;
	}

	@Override
	public Query and()
	{
		rules.get(this.rules.size() - 1).add(new QueryRule(Operator.AND));
		return this;
	}

	@Override
	public Query like(String field, Object value)
	{
		rules.get(this.rules.size() - 1).add(new QueryRule(field, Operator.LIKE, value));
		return this;
	}

	@Override
	public Query eq(String field, Object value)
	{
		rules.get(this.rules.size() - 1).add(new QueryRule(field, Operator.EQUALS, value));
		return this;
	}

	@Override
	public Query in(String field, Iterable<?> objectIterator)
	{
		rules.get(this.rules.size() - 1).add(new QueryRule(field, Operator.IN, objectIterator));
		return this;
	}

	@Override
	public Query gt(String field, Object value)
	{
		rules.get(this.rules.size() - 1).add(new QueryRule(field, Operator.GREATER, value));
		return this;
	}

	@Override
	public Query ge(String field, Object value)
	{
		rules.get(this.rules.size() - 1).add(new QueryRule(field, Operator.GREATER_EQUAL, value));
		return this;
	}

	@Override
	public Query le(String field, Object value)
	{
		rules.get(this.rules.size() - 1).add(new QueryRule(field, Operator.LESS_EQUAL, value));
		return this;
	}

	@Override
	public Query lt(String field, Object value)
	{
		rules.get(this.rules.size() - 1).add(new QueryRule(field, Operator.LESS, value));
		return this;
	}

	@Override
	public Query nest()
	{
		// add element to our nesting list...
		this.rules.add(new ArrayList<QueryRule>());
		return this;
	}

	@Override
	public Query unnest()
	{
		if (this.rules.size() == 1) throw new MolgenisDataException("Cannot unnest root element of query");

		// remove last element and add to parent as nested rule
		QueryRule nested = new QueryRule(Operator.NESTED, this.rules.get(this.rules.size() - 1));
		this.rules.remove(this.rules.get(this.rules.size() - 1));
		this.rules.get(this.rules.size() - 1).add(nested);
		return this;
	}

	@Override
	public Query unnestAll()
	{
		while (this.rules.size() > 1)
		{
			unnest();
		}
		return this;
	}

	@Override
	public Query rng(String field, Object smaller, Object bigger)
	{
		this.gt(field, smaller);
		this.lt(field, bigger);
		return this;
	}

	@Override
	public Query pageSize(int pageSize)
	{
		setPageSize(pageSize);
		return this;
	}

	@Override
	public Query offset(int offset)
	{
		setOffset(offset);
		return this;
	}

    @Override
    public Query sort(Sort.Direction direction, String ... fields)
    {
        this.sort(new Sort(direction, fields));
        return this;
    }


    @Override
	public Query sort(Sort sort)
	{
		setSort(sort);
		return this;
	}

	@Override
	public String toString()
	{
		return "QueryImpl [rules=" + getRules() + ", pageSize=" + pageSize + ", offset=" + offset + ", sort=" + sort
				+ "]";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + offset;
		result = prime * result + pageSize;
		result = prime * result + ((rules == null) ? 0 : rules.hashCode());
		result = prime * result + ((sort == null) ? 0 : sort.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		QueryImpl other = (QueryImpl) obj;
		if (offset != other.offset) return false;
		if (pageSize != other.pageSize) return false;
		if (rules == null)
		{
			if (other.rules != null) return false;
		}
		else if (!rules.equals(other.rules)) return false;
		if (sort == null)
		{
			if (other.sort != null) return false;
		}
		else if (!sort.equals(other.sort)) return false;
		return true;
	}
}
