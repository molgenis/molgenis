/**
 * File: TextInput.java <br>
 * Copyright: Inventory 2000-2006, GBIC 2005, all rights reserved <br>
 * Changelog:
 * <ul>
 * <li>2006-03-08, 1.0.0, DI Matthijssen; Creation
 * <li>2006-05-14, 1.1.0, MA Swertz; Refectoring into Invengine.
 * </ul>
 * TODO look at the depreciated functions.
 */

package org.molgenis.framework.ui.html;

/**
 * Input for rich html text editing (bold, italic, etc).
 * 
 * Thanks to http://www.tinymce.com/
 */
public class RichtextInput extends StringInput
{
	public RichtextInput(String name)
	{
		this(name, null);
	}

	public RichtextInput(String name, String value)
	{
		super(name, value);
		this.setMaxHeight(50);
		this.setMinHeight(3);
	}

	protected RichtextInput()
	{
	}

	@Override
	public String getCustomHtmlHeaders()
	{
		return "<script type=\"text/javascript\" src=\"lib/tinymce-3.4.4/tiny_mce.js\"></script>"
				+ "\n<script type=\"text/javascript\">"
				+ "\ntinyMCE.init({"
				+ "\n        mode : \"textareas\","
				+ "\n        editor_selector : \"mceEditor\","
				+ "\n        editor_deselector : \"mceNoEditor\","
				+ "\n        theme : \"advanced\","
				+ "\n		 skin : \"o2k7\","
				+ "\n		 plugins : \"table,inlinepopups\","
				+ "\n		 skin_variant : \"silver\","
				+ "\n		 theme_advanced_buttons1 : \"bold,italic,underline,strikethrough,|,formatselect,bullist,numlist,link,unlink,image,|,undo,redo,removeformat\","
				+ "\n        theme_advanced_buttons2 : \"table,tablecontrols,|,sub,sup,charmap\","
				+ "\n		 theme_advanced_buttons3 : \"\"," + "\n		 theme_advanced_toolbar_location : \"top\","
				+ "\n		 theme_advanced_toolbar_align : \"left\","
				+ "\n		 theme_advanced_statusbar_location : \"bottom\"," + "\n		 theme_advanced_resizing : true,"
				+ "\n		 apply_source_formatting : true," + "\n		 theme_advanced_path : false,"
				+ "\n		 onchange_callback : function (editor){tinyMCE.triggerSave();}" + "});" + "</script>";
	}

	@Override
	public String toHtml()
	{
		return String.format(
				"<textarea id=\"%s\" name=\"%s\" class=\"mceEditor %s\" cols=\"80\" rows=\"10\">%s</textarea>",
				getId(), getName(), (this.isNillable() ? "" : " required"), getValue());
	}

	/**
	 * Override because hyperlink must not be escaped
	 */
	@Override
	public String getHtmlValue()
	{
		return this.getValue();
	}
}
