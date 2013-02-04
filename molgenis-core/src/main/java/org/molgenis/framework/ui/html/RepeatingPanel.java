package org.molgenis.framework.ui.html;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * Extension of TablePanel that comes with a [+] and [-] button so one can
 * (un)repeat its contents If used in a 'label'-'value' panel the the repeatable
 * elements will be nested into the value panel. Useful for subforms with
 * repeating information.
 * 
 * Features: <li>can clone itself for repeat <li>can remove clone <li>TODO set
 * option how many clones to show at start <li>TODO set option minimum / maximum
 * number of clones (default 0,unlimited respectively) <li>TODO set custom
 * labels to the 'add row' and 'remove row'
 */
public class RepeatingPanel extends DivPanel
{
	private int maxElems = -1;

	public RepeatingPanel(String name, String label)
	{
		super(name, label);
	}

	/**
	 * Constructor
	 * 
	 * @param name
	 * @param label
	 * @param maxElems
	 *            : maximum number of clones, if maxElems > 1
	 */
	public RepeatingPanel(String name, String label, int maxElems)
	{
		super(name, label);
		this.maxElems = maxElems;
	}

	@Override
	public String toHtml()
	{
		// remove button for each row to remove the div shown above
		ActionInput removeButton = new ActionInput(this.getName() + "_remove", "Remove row", "Remove");
		// JavaScript for remove button: Remove child element and make add
		// button visible if necessary
		removeButton.setJavaScriptAction("this.parentNode.parentNode.removeChild(this.parentNode); "
				+ (this.maxElems > 1 ? "decCount();" : "") + " return false;");
		// add button to clone the div
		ActionInput addButton = new ActionInput(this.getName() + "_add", "Add row", "Add");
		// repeating block
		String repeatableDiv = super.toHtml() + removeButton.toHtml();

		// JavaScript for add button: Add child element and make add button
		// invisible if necessary
		addButton.setJavaScriptAction("var div = document.createElement('DIV'); "
				+ "this.parentNode.insertBefore(div,this); div.innerHTML = '"
				+ StringEscapeUtils.escapeJavaScript(StringEscapeUtils.escapeHtml(repeatableDiv)) + "'; "
				+ (this.maxElems > 1 ? "incCount();" : "") + " return false");

		// create a div to contain the panel
		return "<div style=\"clear:both; display:block\"><script type=\"text/javascript\">var numElems = 1; "
				+ "function decCount() { numElems -= 1; if (numElems < " + this.maxElems
				+ ") { document.getElementById('" + addButton.getName()
				+ "').style.display = 'block'; } } function incCount() { numElems += 1; if (numElems >= "
				+ this.maxElems + ") { document.getElementById('" + addButton.getName()
				+ "').style.display = 'none'; } }</script>" + super.toHtml() + addButton.toHtml() + "</div>";
	}

}
