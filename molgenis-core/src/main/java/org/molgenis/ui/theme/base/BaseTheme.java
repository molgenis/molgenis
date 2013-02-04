package org.molgenis.ui.theme.base;

import java.util.Map;
import java.util.TreeMap;

import org.molgenis.ui.Label;
import org.molgenis.ui.MolgenisComponent;
import org.molgenis.ui.SelectInput;
import org.molgenis.ui.theme.RenderException;
import org.molgenis.ui.theme.Theme;
import org.molgenis.ui.theme.TwoStepView;

/**
 * Base theme were default layouts go (that are not theme dependent).
 * It contains vanilla views for most components (should be: all).
 */
public class BaseTheme implements Theme
{

	protected Map<String, TwoStepView<? extends MolgenisComponent>> renderers = new TreeMap<String, TwoStepView<?>>();

	public BaseTheme()
	{
		renderers.put(Label.class.getName(), new LabelView());
		renderers.put(SelectInput.class.getName(), new SelectInputView());
	}
	
	/** Convert the model into the layout 
	 * @throws RenderException */
	@Override
	@SuppressWarnings(
	{ "unchecked", "rawtypes" })
	public <E extends MolgenisComponent> String render(E e) throws RenderException
	{
		if (renderers.get(e.getClass().getName()) != null)
		{
			TwoStepView v = renderers.get(e.getClass().getName());
			return v.render(e, this);
		}
		throw new RenderException("no view found for component "+e.getClass());
	}


}
