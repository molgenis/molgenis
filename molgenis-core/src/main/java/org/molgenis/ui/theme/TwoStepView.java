package org.molgenis.ui.theme;

import org.molgenis.ui.MolgenisComponent;

/**
 * Apply the layout to the view. This mechanism allows multiple views for the
 * same conceptual user interface.
 * 
 * See TwoStepView design pattern.
 */
public interface TwoStepView<E extends MolgenisComponent>
{
	public String render(E element, Theme renderer) throws RenderException;
}
