package org.molgenis.framework.ui.html;

import org.testng.Assert;
import org.testng.annotations.Test;

public class DateInputTest
{

	@Test
	public void toJquery()
	{
		DateInput date = new DateInput();
		String s = ".datepicker({dateFormat: 'dd-mm-yy', changeMonth: true, changeYear: true, showButtonPanel: true, beforeShow: function( input ) {\n"
				+ "	setTimeout( function() {\n"
				+ "		var buttonPane = $( input ).datepicker( \"widget\" ).find( \".ui-datepicker-buttonpane\" );\n"
				+ "		$( \"<button>\", {text: \"Clear\", click: function() {$.datepicker._clearDate( input );}}).addClass(\"ui-datepicker-close ui-state-default ui-priority-primary ui-corner-all\").appendTo( buttonPane );\n"
				+ "	}, 1 );\n" + "}}).click(function(){$(this).datepicker('show')});\n" + "</script>";
		Assert.assertTrue(date.toJquery().contains(s));
	}

	@Test
	public void toJquery_yymmdd()
	{
		DateInput date = new DateInput();
		date.setDateFormat("yyyy-MM-dd");
		String s = ".datepicker({dateFormat: 'yy-mm-dd', changeMonth: true, changeYear: true, showButtonPanel: true, beforeShow: function( input ) {\n"
				+ "	setTimeout( function() {\n"
				+ "		var buttonPane = $( input ).datepicker( \"widget\" ).find( \".ui-datepicker-buttonpane\" );\n"
				+ "		$( \"<button>\", {text: \"Clear\", click: function() {$.datepicker._clearDate( input );}}).addClass(\"ui-datepicker-close ui-state-default ui-priority-primary ui-corner-all\").appendTo( buttonPane );\n"
				+ "	}, 1 );\n" + "}}).click(function(){$(this).datepicker('show')});\n" + "</script>";
		Assert.assertTrue(date.toJquery().contains(s));
	}
}
