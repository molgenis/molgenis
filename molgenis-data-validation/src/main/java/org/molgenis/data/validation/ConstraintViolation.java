package org.molgenis.data.validation;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import static java.lang.Math.toIntExact;

public class ConstraintViolation implements Serializable
{
	private final String message;
	private Long rowNr;

	public ConstraintViolation(String message)
	{
		this(message, null);
	}

	public ConstraintViolation(String message, Long rowNr)
	{
		this.message = message;
		this.rowNr = rowNr;
	}

	/**
	 * Renumber the violation row number from a list of actual row numbers The list of indices is 0-indexed and the
	 * rownnr are 1-indexed
	 */
	void renumberRowIndex(List<Integer> indices)
	{
		this.rowNr = this.rowNr != null ? Long.valueOf(indices.get(toIntExact(this.rowNr - 1))) : null;
	}

	public String getMessage()
	{
		if (null != rowNr)
		{
			return message + " (entity " + rowNr + ")";
		}

		return message;
	}

	Long getRowNr()
	{
		return rowNr;
	}

	void setRowNr(Long rowNr)
	{
		this.rowNr = rowNr;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ConstraintViolation that = (ConstraintViolation) o;
		return Objects.equals(message, that.message) && Objects.equals(rowNr, that.rowNr);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(message, rowNr);
	}

	@Override
	public String toString()
	{
		return "ConstraintViolation{" + "message='" + message + '\'' + ", rowNr=" + rowNr + '}';
	}
}
