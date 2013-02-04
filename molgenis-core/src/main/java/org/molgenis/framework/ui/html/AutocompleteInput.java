package org.molgenis.framework.ui.html;

public class AutocompleteInput extends HtmlInput<String>
{
	private String entityClass;
	private String entityField;

	public AutocompleteInput(String name, String label, String entityClass, String entityField, String description)
	{
		super(name, label, null, true, false, description);
		this.entityClass = entityClass;
		this.entityField = entityField;
	}

	@Override
	public String toHtml()
	{
		return "<input id=\""
				+ this.getId()
				+ "\" name=\""
				+ getName()
				+ "\" type=\"text\" onfocus=\"autoComplete(this)\"/>"
				+ "<script type=\"text/javascript\">\n"
				+ "function autoComplete(elem) {\n"
				+ "	$(elem).autocomplete({\n"
				+ "		source: function(req, resp) {\n"
				+ "			var url         = \"xref/find\";\n"
				+ "			var suggestions = [];\n"
				+ "			successFunction = function(data, textStatus) {\n"
				+ "				$.each(data, function(key, val) { suggestions.push(key); });\n"
				+ "               suggestions.sort();"
				+ "				return suggestions;\n"
				+ "			};\n"
				+ "           errorFunction = function(jqXHR, textStatus, errorThrown) {\n"
				+ "               alert(textStatus);\n"
				+ "           };\n"
				+ "			var dataHash = new Object();\n"
				+ "			dataHash['"
				+ AbstractRefInput.XREF_ENTITY
				+ "'] = '"
				+ this.entityClass
				+ "';\n"
				+ "			dataHash['"
				+ AbstractRefInput.XREF_FIELD
				+ "']  = '"
				+ this.entityField
				+ "';\n"
				+ "			dataHash['"
				+ AbstractRefInput.XREF_LABELS
				+ "']  = '"
				+ this.entityField
				+ "';\n"
				+ "			dataHash['"
				+ AbstractRefInput.SEARCH_TERM
				+ "'] = req.term;\n"
				+ "           dataHash['"
				+ AbstractRefInput.NILLABLE
				+ "'] = 1;\n"
				+ "			jQuery.ajax({ url: url, data: dataHash, dataType: \"json\", type: \"POST\", async: false, success: successFunction, error: errorFunction });\n"
				+ "			resp(suggestions);\n" + "		},\n" + "		select: function(e, ui) { },\n"
				+ "		change: function() { }\n" + "	});\n" + "}\n" + "</script>\n";
	}
}
