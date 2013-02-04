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

import java.util.Map;
import java.util.TreeMap;

import org.molgenis.framework.ui.FreemarkerView;

/**
 * Renders an editor for freemarker including code higlighting and line numbers.
 * 
 * Based on http://codemirror.net/ (thanks!)
 */
public class CodeInput extends StringInput
{
	public enum Parser
	{

		FREEMARKER("\"../contrib/freemarker/js/parsefreemarker.js\"", "css/freemarkercolors.css"), JAVA(
				"[\"../contrib/java/js/tokenizejava.js\",\"../contrib/java/js/parsejava.js\"]",
				"lib/codemirror-1.0/contrib/java/css/javacolors.css");

		private final String path;
		private final String css;

		Parser(String path, String css)
		{
			this.path = path;
			this.css = css;
		}

		public String getPath()
		{
			return path;
		}

		public String getCssPath()
		{
			return css;
		}

	}

	private Parser parser = Parser.FREEMARKER;

	public CodeInput(String name)
	{
		this(name, null, Parser.FREEMARKER);
	}

	public CodeInput(String name, String value, Parser parser)
	{
		super(name, value);
		this.parser = parser;
		this.setMaxHeight(50);
		this.setMinHeight(3);
	}

	protected CodeInput()
	{
	}

	@Override
	public String getCustomHtmlHeaders()
	{
		// borrow style from mce
		return "\n<link rel=\"stylesheet\" style=\"text/css\" type=\"text/css\" href=\"lib/tinymce-3.4.4/themes/advanced/skins/o2k7/ui_silver.css\">"
				+ "\n<link rel=\"stylesheet\" style=\"text/css\" type=\"text/css\" href=\"lib/tinymce-3.4.4/themes/advanced/skins/o2k7/ui.css\">"
				+ "\n<script src=\"lib/codemirror-1.0/js/codemirror.js\" type=\"text/javascript\"></script>\n"
				+ "\n<style type=\"text/css\">"
				+ "\n.CodeMirror-line-numbers {"
				+ "\n 	width: 2em;"
				+ "\n 	color: #aaa;"
				+ "\n 	background-color: #eee;"
				+ "\n 	text-align: right;"
				+ "\n 	padding-right: .3em;"
				+ "\n 	font-family: monospace;"
				+ "\n	font-size: 12px;"
				+ "\n 	line-height: normal;"
				+ "\n 	padding-top: .4em;"
				+ "\n	margin-bottom: 20px;"
				+ "\n }"
				+ "\n.CodeMirror-wrapping {"
				+ "\n	border: 1px #AAA solid;"
				+ "\n	margin: 2px;"
				+ "\n	padding-bottom: 20px;"
				+ "\n  width: 320px;"
				+ "\n  background-color: #EEE;"
				+ "\n}"
				+ "\n.CodeMirror-iframe {"
				+ "\n 	border: 1px #AAA solid;"
				+ "\n}"
				+ "\n .editbox {"
				+ "\n	background: white;"
				+ "\n}"
				+ "\n.CodeMirror-scroll {"
				+ "\n  height: auto;"
				+ "\n  overflow-y: hidden;" + "\n  overflow-x: auto;" + "\n  width: 100%" + "\n}" + "\n</style>";
	}

	public String getParser()
	{
		return this.parser.getPath();
	}

	public String getParserStyle()
	{
		return this.parser.getCssPath();
	}

	@Override
	public String toHtml()
	{
		Map<String, Object> parameters = new TreeMap<String, Object>();
		parameters.put("input", this);

		// delegate to freemarker (sad Java doesn't allow multiline strings).
		return new FreemarkerView("org/molgenis/framework/ui/html/CodeInput.ftl", parameters).render();
	}
}
