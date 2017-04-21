package org.molgenis.data.matrix.model;

public class Score
{
	private String column;
	private String row;
	private Object value;

	public Score(String row, String column, Object value)
	{
		this.row = row;
		this.column = column;
		this.value = value;
	}

	public String getColumn()
	{
		return column;
	}

	public void setColumn(String column)
	{
		this.column = column;
	}

	public String getRow()
	{
		return row;
	}

	public void setRow(String row)
	{
		this.row = row;
	}

	public Object getValue()
	{
		return value;
	}

	public void setValue(Object value)
	{
		this.value = value;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Score score = (Score) o;

		if (!column.equals(score.column)) return false;
		if (!row.equals(score.row)) return false;
		return value.equals(score.value);
	}

	@Override
	public int hashCode()
	{
		int result = column.hashCode();
		result = 31 * result + row.hashCode();
		result = 31 * result + value.hashCode();
		return result;
	}
}
