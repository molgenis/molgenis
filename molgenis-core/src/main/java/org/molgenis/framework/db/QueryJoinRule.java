package org.molgenis.framework.db;

public class QueryJoinRule extends QueryRule
{
	String e1;
	String e2;
	String f1;
	String f2;

	public QueryJoinRule(String entity1, String field1, String entity2, String field2)
	{
		super(entity1 + "." + field1, Operator.JOIN, entity2 + "." + field2);
		e1 = entity1;
		e2 = entity2;
		f1 = field1;
		f2 = field2;
	}

	public String getEntity1()
	{
		return e1;
	}

	public String getEntity2()
	{
		return e2;
	}

	public String getField1()
	{
		return f1;
	}

	public String getField2()
	{
		return f2;
	}

	public void setEntity1(String name)
	{
		e1 = name;
		this.setField(e1 + "." + f1);

	}

	public void setEntity2(String name)
	{
		e2 = name;
		this.setValue(e2 + "." + f2);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((e1 == null) ? 0 : e1.hashCode());
		result = prime * result + ((e2 == null) ? 0 : e2.hashCode());
		result = prime * result + ((f1 == null) ? 0 : f1.hashCode());
		result = prime * result + ((f2 == null) ? 0 : f2.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (getClass() != obj.getClass()) return false;
		QueryJoinRule other = (QueryJoinRule) obj;
		if (e1 == null)
		{
			if (other.e1 != null) return false;
		}
		else if (!e1.equals(other.e1)) return false;
		if (e2 == null)
		{
			if (other.e2 != null) return false;
		}
		else if (!e2.equals(other.e2)) return false;
		if (f1 == null)
		{
			if (other.f1 != null) return false;
		}
		else if (!f1.equals(other.f1)) return false;
		if (f2 == null)
		{
			if (other.f2 != null) return false;
		}
		else if (!f2.equals(other.f2)) return false;
		return true;
	}
}
