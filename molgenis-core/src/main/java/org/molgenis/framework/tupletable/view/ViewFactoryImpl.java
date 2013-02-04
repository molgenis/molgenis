package org.molgenis.framework.tupletable.view;

import org.molgenis.framework.tupletable.view.renderers.Renderers.CSVRenderer;
import org.molgenis.framework.tupletable.view.renderers.Renderers.ExcelRenderer;
import org.molgenis.framework.tupletable.view.renderers.Renderers.JQGridRenderer;
import org.molgenis.framework.tupletable.view.renderers.Renderers.Renderer;
import org.molgenis.framework.tupletable.view.renderers.Renderers.SPSSRenderer;

public class ViewFactoryImpl implements ViewFactory
{
	@Override
	public Renderer createView(String viewName)
	{
		if (viewName.equals("JQ_GRID"))
		{
			return new JQGridRenderer();
		}
		else if (viewName.equals("EXCEL"))
		{
			return new ExcelRenderer();
		}
		else if (viewName.equals("CSV"))
		{
			return new CSVRenderer();
		}
		else if (viewName.equals("SPSS"))
		{
			return new SPSSRenderer();
		}
		throw new IllegalArgumentException(String.format("view: %s not found", viewName));
	}
}
