package org.molgenis.framework.tupletable.view;

import org.molgenis.framework.tupletable.view.renderers.Renderers.Renderer;

public interface ViewFactory
{
	public Renderer createView(String viewName);
}
