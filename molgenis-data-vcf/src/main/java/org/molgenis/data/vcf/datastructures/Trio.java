package org.molgenis.data.vcf.datastructures;

public class Trio
{
	Sample child;
	Sample mother;
	Sample father;

	public Trio()
	{
		super();
	}

	public Trio(Sample child, Sample mother, Sample father)
	{
		super();
		this.child = child;
		this.mother = mother;
		this.father = father;
	}

	public Sample getChild()
	{
		return child;
	}

	public void setChild(Sample child)
	{
		this.child = child;
	}

	public Sample getMother()
	{
		return mother;
	}

	public void setMother(Sample mother)
	{
		this.mother = mother;
	}

	public Sample getFather()
	{
		return father;
	}

	public void setFather(Sample father)
	{
		this.father = father;
	}

	@Override
	public String toString()
	{
		return "Trio [child=" + child + ", mother=" + mother + ", father=" + father + "]";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((child == null) ? 0 : child.hashCode());
		result = prime * result + ((father == null) ? 0 : father.hashCode());
		result = prime * result + ((mother == null) ? 0 : mother.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Trio other = (Trio) obj;
		if (child == null)
		{
			if (other.child != null) return false;
		}
		else if (!child.equals(other.child)) return false;
		if (father == null)
		{
			if (other.father != null) return false;
		}
		else if (!father.equals(other.father)) return false;
		if (mother == null)
		{
			if (other.mother != null) return false;
		}
		else if (!mother.equals(other.mother)) return false;
		return true;
	}

}
