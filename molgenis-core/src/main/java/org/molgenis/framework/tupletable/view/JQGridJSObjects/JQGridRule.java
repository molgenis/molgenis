package org.molgenis.framework.tupletable.view.JQGridJSObjects;

//{"field":"Country.Code","op":"eq","data":"AGO"}
public class JQGridRule
{
	private final String field;
	private final JQGridOp op;
	private final String data;

	public enum JQGridOp
	{
		eq, ne, lt, le, gt, ge, bw, bn, in, ni, ew, en, cn, nc
	}

	public JQGridRule(String field, JQGridOp op, String data)
	{
		this.field = field;
		this.op = op;
		this.data = data;
	}

	public String getField()
	{
		return field;
	}

	public JQGridOp getOp()
	{
		return op;
	}

	public String getData()
	{
		return data;
	}
}
