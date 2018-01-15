package org.molgenis.data.file.processor;

public class TrimProcessor extends AbstractCellProcessor
{
	private static final long serialVersionUID = 1L;

	public TrimProcessor()
	{
		super();
	}

	public TrimProcessor(boolean processHeader, boolean processData)
	{
		super(processHeader, processData);
	}

	@Override
	public String process(String value)
	{
		return value != null ? value.trim() : null;
	}
}
