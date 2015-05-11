package org.molgenis.data.vcf.datastructures;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.molgenis.data.Entity;

public class Sample
{
	String id;

	Entity genotype;

	public Sample()
	{
		super();
	}

	public Sample(String id)
	{
		super();
		this.id = id;
	}

	public Sample(String id, Entity genotype)
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

	public Entity getGenotype()
	{
		return genotype;
	}

	public void setGenotype(Entity genotype)
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
	public boolean equalsSampleOrString(Object obj)
	{
		if (obj instanceof String)
		{
			return new EqualsBuilder().append(id, obj.toString()).isEquals();
		}
		if (!(obj instanceof Sample)) return false;
		if (obj == this) return true;
		Sample rhs = (Sample) obj;
		return new EqualsBuilder().append(id, rhs.id).isEquals();
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof Sample)) return false;

		Sample sample = (Sample) o;

		if (genotype != null ? !genotype.equals(sample.genotype) : sample.genotype != null) return false;
		if (id != null ? !id.equals(sample.id) : sample.id != null) return false;

		return true;
	}

	@Override
	public String toString()
	{
		return "Sample [id=" + id + ", genotype=" + genotype + "]";
	}

}
