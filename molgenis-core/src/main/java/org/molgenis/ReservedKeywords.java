package org.molgenis;

import java.util.Set;

import com.google.common.collect.Sets;

/**
 * Collection of reserved keywords used by different languages.
 */
public class ReservedKeywords
{
	// http://www.w3schools.com/js/js_reserved.asp
	// Case sensitive
	public static final Set<String> JAVASCRIPT_KEYWORDS = Sets.newHashSet("abstract", "arguments", "boolean", "break",
			"byte", "case", "catch", "char", "class", "const", "continue", "debugger", "default", "delete", "do",
			"double", "else", "enum", "eval", "export", "extends", "false", "final", "finally", "float", "for",
			"function", "goto", "if", "implements", "import", "in", "instanceof", "int", "interface", "let", "long",
			"native", "new", "null", "package", "private", "protected", "public", "return", "short", "static", "super",
			"switch", "synchronized", "this", "throw", "throws", "transient", "true", "try", "typeof", "var", "void",
			"volatile", "while", "with", "yield");

	// Case sensitive
	public static final Set<String> MOLGENIS_KEYWORDS = Sets.newHashSet("login", "logout", "csv", "entities",
			"attributes", "base", "exist", "meta");
}
