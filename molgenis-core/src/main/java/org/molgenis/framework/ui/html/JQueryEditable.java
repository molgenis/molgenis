package org.molgenis.framework.ui.html;

public class JQueryEditable extends HtmlWidget
{
	private String contents;

	/* Todo : data can be html >> */
	public JQueryEditable(String name, String contents)
	{
		super(name);
		this.setContents(contents);
	}

	/**
	 * 
	 *
	 */
	// private String renderEditable(String contents)
	// {
	// String returnString = null;
	//
	// if (!contents.isEmpty())
	// {
	// returnString = "<div class=\"edit\" id=\"div_1\">Dolor</div>";
	// returnString = "<div class=\"edit_area\" id=\"div_2\">Lorem ipsum "
	// + "dolor sit amet, consectetuer .</div>";
	//
	// // returnString = "<li id = \"" + node.getName().replaceAll(" ",
	// // "_") + "\" class=\"closed"
	// // +
	// //
	// "\" style=\"display:none;\"><span class=\"folder\"><input type=\"checkbox\" id=\""
	// // + node.getEntityID() + "\" name=\"" +
	// // node.getEntityID().split("_identifier_")[0] + "\""
	// // + (selectedLabels.contains(node.getLabel()) ? " checked=\"yes\""
	// // : "") + " />"
	// // + node.getLabel() + "</span>\n" + "<ul>\n";
	// }
	// else
	// {
	// returnString = "No text available!";
	// // returnString = "<li id = \"" + node.getName().replaceAll(" ",
	// // "_") + "\" class=\"closed"
	// // + "\" style=\"display:none;\"><span class=\"folder\">" +
	// // node.getLabel() + "</span>\n"
	// // + "<ul>\n";
	// }
	//
	// return returnString;
	// }

	@Override
	public String toHtml()
	{
		String html = ""
				+ "<script src=\"res/jquery-plugins/editable/jquery.editable-1.3.3.js\" language=\"javascript\"></script>\n"
				+ "<script>\n"
				+ "$(document).ready(function() {\n"
				+ "$('.title').editable( {\n"
				+ "type : 'textarea',\n"
				+ "indicator : 'Saving...',\n"
				+ "tooltip : 'Click to edit...',\n"
				+ "id : 'TitleId',\n"
				+ "name : 'newTitle',\n"
				+ "cancel : 'Cancel',\n"
				+ "submit : 'OK'\n"
				+ " });\n"
				+ "$('.welcomeText').editable( {\n"
				+ "type : 'textarea',\n"
				+ "cancel : 'Cancel',\n"
				+ "submit : 'OK',\n"
				// + "width : 2000,\n"
				// + "height : 3000,\n"
				// + "indicator : '<img src=\"res/img/indicator.gif\">',\n"
				// + "tooltip : 'Click to edit...',\n"
				+ "id : 'newTextId'," + "name : 'newText'\n" + "});\n" + "$('#welcomeText textarea').height(200);\n"
				+ "$('#welcomeText textarea').width(1200);\n" + "$('#submitChanges').click(function(){\n"
				+ "$('input[name=\"__action\"]').val(\"submitChanges\");" + "element1 = $('#title');\n"
				+ "element2 = $('#welcomeText');\n" + "url = \"molgenis.do?__target=BbmriWelcomeScreen\";\n"
				+ "$.ajax(url + \"&\" + element1.attr('name') + \"=\" +\n" + "element1.html() \n"
				+ "+ \"&\" + element2.attr('name') + \"=\" +\n" + "element2.html()).done(function(message){});\n"
				+ "});\n" + "});" + "</script>";

		return html;
	}

	public String getContents()
	{
		return contents;
	}

	public void setContents(String contents)
	{
		this.contents = contents;
	}
}
