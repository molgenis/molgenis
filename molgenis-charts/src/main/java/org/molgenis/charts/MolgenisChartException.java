package org.molgenis.charts;

public class MolgenisChartException extends RuntimeException
{
	private static final long serialVersionUID = -8031085142387633992L;

	public MolgenisChartException()
	{
	}

	public MolgenisChartException(String msg)
	{
		super(msg);
	}

	public MolgenisChartException(Throwable t)
	{
		super(t);
	}

	public MolgenisChartException(String msg, Throwable t)
	{
		super(msg, t);
	}

}
