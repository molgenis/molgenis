package org.molgenis.data.validation;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

import java.util.List;

import static java.lang.Math.toIntExact;

public class ConstraintViolation
{
	private final String message;
	private Object invalidValue;
	private Entity entity;
	private Attribute violatedAttribute;
	private EntityType entityType;
	private String importInfo;
	private Long rownr;

	public ConstraintViolation(String message)
	{
		this(message, null);
	}

	public ConstraintViolation(String message, Long rownr)
	{
		this.message = message;
		this.rownr = rownr;
	}

	public ConstraintViolation(String message, Attribute violatedAttribute, Long rownr)
	{
		this.message = message;
		this.violatedAttribute = violatedAttribute;
		this.rownr = rownr;
	}

	public ConstraintViolation(String message, Object invalidValue, Entity entity, Attribute violatedAttribute,
			EntityType entityType, Long rownr)
	{
		this.message = message;
		this.invalidValue = invalidValue;
		this.entity = entity;
		this.violatedAttribute = violatedAttribute;
		this.entityType = entityType;
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
		this.rownr = this.rownr != null ? new Long(indices.get(toIntExact(this.rownr - 1))) : null;
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

	public Attribute getViolatedAttribute()
	{
		return violatedAttribute;
	}

	public EntityType getEntityType()
	{
		return entityType;
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
		result = prime * result + ((entityType == null) ? 0 : entityType.hashCode());
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
		if (entityType == null)
		{
			if (other.entityType != null) return false;
		}
		else if (!entityType.equals(other.entityType)) return false;
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
				+ ", violatedAttribute=" + violatedAttribute + ", entityType=" + entityType + ", importInfo="
				+ importInfo + ", rownr=" + rownr + "]";
	}

}
