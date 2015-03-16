package org.molgenis.data.annotation.impl.datastructures;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Sample
{
	String id;
	String genotype;
	
	public Sample()
	{
		super();
	}

	public Sample(String id)
	{
		super();
		this.id = id;
	}
	
	public Sample(String id, String genotype)
	{
		super();
		this.id = id;
		this.genotype = genotype;
	}
	
	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getGenotype()
	{
		return genotype;
	}

	public void setGenotype(String genotype)
	{
		this.genotype = genotype;
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(17, 31).append(id).toHashCode();
	}

	/**
	 * Allows compare with String or Sample object
	 */
	@Override
	public boolean equals(Object obj)
	{
		System.out.println("EQUALS!!");
		if(obj instanceof String){
			System.out.println("COMPARING TO STIRNG!!!");return new EqualsBuilder().append(id, obj.toString()).isEquals();
		}
		if (!(obj instanceof Sample)) return false;
		if (obj == this) return true;
		Sample rhs = (Sample) obj;
		return new EqualsBuilder().append(id, rhs.id).isEquals();
	}
	

//	public boolean equals(String obj)
//	{
//		return equals((Object)obj);
//	}

	@Override
	public String toString()
	{
		return "Sample [id=" + id + ", genotype=" + genotype + "]";
	}
	
	

}
