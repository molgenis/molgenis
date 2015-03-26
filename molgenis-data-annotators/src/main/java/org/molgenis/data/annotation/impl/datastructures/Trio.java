package org.molgenis.data.annotation.impl.datastructures;

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

}
