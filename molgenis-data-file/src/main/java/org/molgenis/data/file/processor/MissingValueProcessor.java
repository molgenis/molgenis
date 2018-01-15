package org.molgenis.data.file.processor;

public class MissingValueProcessor extends AbstractCellProcessor
{
	private static final long serialVersionUID = 1L;

	private final String missingValue;
	private final boolean emptyIsMissing;

	public MissingValueProcessor(String missingValue, boolean emptyIsMissing)
	{
		super();
		this.missingValue = missingValue;
		this.emptyIsMissing = emptyIsMissing;
	}

	public MissingValueProcessor(boolean processHeader, boolean processData, String missingValue,
			boolean emptyIsMissing)
	{
		super(processHeader, processData);
		this.missingValue = missingValue;
		this.emptyIsMissing = emptyIsMissing;
	}

	@Override
	public String process(String value)
	{
		return value == null ? missingValue : (emptyIsMissing && value.isEmpty() ? missingValue : value);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (emptyIsMissing ? 1231 : 1237);
		result = prime * result + ((missingValue == null) ? 0 : missingValue.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (getClass() != obj.getClass()) return false;
		MissingValueProcessor other = (MissingValueProcessor) obj;
		if (emptyIsMissing != other.emptyIsMissing) return false;
		if (missingValue == null)
		{
			if (other.missingValue != null) return false;
		}
		else if (!missingValue.equals(other.missingValue)) return false;
		return true;
	}
}
