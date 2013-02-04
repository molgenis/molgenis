package org.molgenis.framework.ui.html;

import org.molgenis.util.tuple.KeyValueTuple;
import org.molgenis.util.tuple.Tuple;
import org.molgenis.util.tuple.WritableTuple;

/**
 * Input that should be hidden from view. Used for hidden parameters that users
 * don't want/need to see.
 */
public class HiddenInput extends StringInput
{

	public HiddenInput(String name, Object value)
	{
		super(name, value == null ? "" : value.toString());
		this.setHidden(true);
	}

	public HiddenInput()
	{
	}

	@Override
	public String toHtml(Tuple params) throws HtmlInputException
	{
		WritableTuple tuple = new KeyValueTuple();
		for (String colName : params.getColNames())
			tuple.set(colName, params.get(colName));
		tuple.set(HIDDEN, true);
		return super.toHtml(tuple);
	}

}
