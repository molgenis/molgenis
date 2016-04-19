package org.molgenis.data.validation;

import static java.lang.Math.toIntExact;

import java.util.List;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;

public class ConstraintViolation
{
	private final String message;
	private Object invalidValue;
	private Entity entity;
	private AttributeMetaData violatedAttribute;
	private EntityMetaData entityMetaData;
	private String importInfo;
	private Long rownr;

	public ConstraintViolation(String message, Long rownr)
	{
		this.message = message;
		this.rownr = rownr;
	}

	public ConstraintViolation(String message, AttributeMetaData violatedAttribute, Long rownr)
	{
		this.message = message;
		this.violatedAttribute = violatedAttribute;
		this.rownr = rownr;
	}

	public ConstraintViolation(String message, Object invalidValue, Entity entity, AttributeMetaData violatedAttribute,
			EntityMetaData entityMetaData, Long rownr)
	{
		this.message = message;
		this.invalidValue = invalidValue;
		this.entity = entity;
		this.violatedAttribute = violatedAttribute;
		this.entityMetaData = entityMetaData;
		this.rownr = rownr;
	}

	/**
	 * Renumber the violation row number from a list of actual row numbers The list of indices is 0-indexed and the
	 * rownnr are 1-indexed
	 * 
	 * @param indices
	 */
	public void renumberRowIndex(List<Integer> indices)
	{
		this.rownr = new Long(indices.get(toIntExact(this.rownr - 1)));
	}

	public String getMessage()
	{
		if (null != rownr)
		{
			return message + " (entity " + rownr + ")";
		}

		return message;
	}

	public Object getInvalidValue()
	{
		return invalidValue;
	}

	public Entity getEntity()
	{
		return entity;
	}

	public AttributeMetaData getViolatedAttribute()
	{
		return violatedAttribute;
	}

	public EntityMetaData getEntityMetaData()
	{
		return entityMetaData;
	}

	public Long getRownr()
	{
		return rownr;
	}

	public void setRownr(Long rownr)
	{
		this.rownr = rownr;
	}

	public String getImportInfo()
	{
		return importInfo;
	}

	public void setImportInfo(String importInfo)
	{
		this.importInfo = importInfo;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entity == null) ? 0 : entity.hashCode());
		result = prime * result + ((entityMetaData == null) ? 0 : entityMetaData.hashCode());
		result = prime * result + ((importInfo == null) ? 0 : importInfo.hashCode());
		result = prime * result + ((invalidValue == null) ? 0 : invalidValue.hashCode());
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + ((rownr == null) ? 0 : rownr.hashCode());
		result = prime * result + ((violatedAttribute == null) ? 0 : violatedAttribute.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		ConstraintViolation other = (ConstraintViolation) obj;
		if (entity == null)
		{
			if (other.entity != null) return false;
		}
		else if (!entity.equals(other.entity)) return false;
		if (entityMetaData == null)
		{
			if (other.entityMetaData != null) return false;
		}
		else if (!entityMetaData.equals(other.entityMetaData)) return false;
		if (importInfo == null)
		{
			if (other.importInfo != null) return false;
		}
		else if (!importInfo.equals(other.importInfo)) return false;
		if (invalidValue == null)
		{
			if (other.invalidValue != null) return false;
		}
		else if (!invalidValue.equals(other.invalidValue)) return false;
		if (message == null)
		{
			if (other.message != null) return false;
		}
		else if (!message.equals(other.message)) return false;
		if (rownr == null)
		{
			if (other.rownr != null) return false;
		}
		else if (!rownr.equals(other.rownr)) return false;
		if (violatedAttribute == null)
		{
			if (other.violatedAttribute != null) return false;
		}
		else if (!violatedAttribute.equals(other.violatedAttribute)) return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "ConstraintViolation [message=" + message + ", invalidValue=" + invalidValue + ", entity=" + entity
				+ ", violatedAttribute=" + violatedAttribute + ", entityMetaData=" + entityMetaData + ", importInfo="
				+ importInfo + ", rownr=" + rownr + "]";
	}

}
