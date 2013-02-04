package org.molgenis.framework.ui.html.render;

public class SimpleRenderDecorator implements RenderDecorator
{
	@Override
	public String render(String value)
	{
		return value;
	}
}
