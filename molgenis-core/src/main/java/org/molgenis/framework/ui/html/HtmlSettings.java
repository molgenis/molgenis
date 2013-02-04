package org.molgenis.framework.ui.html;

import org.molgenis.framework.ui.html.HtmlElement.UiToolkit;
import org.molgenis.framework.ui.html.render.LinkoutRenderDecorator;
import org.molgenis.framework.ui.html.render.RenderDecorator;

public class HtmlSettings
{
	public final static UiToolkit uiToolkit = UiToolkit.JQUERY;
	// public static UiToolkit uiToolkit = UiToolkit.ORIGINAL;
	public static RenderDecorator defaultRenderDecorator = new LinkoutRenderDecorator();

	// FIXME: define new MolgenisOption with default 'true'
	public static boolean showDescription = true;
}
