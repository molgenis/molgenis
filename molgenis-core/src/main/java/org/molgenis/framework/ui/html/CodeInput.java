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
