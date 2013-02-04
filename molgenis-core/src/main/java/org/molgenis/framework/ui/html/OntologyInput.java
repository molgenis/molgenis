///**
// * File: invengine.screen.form.SelectInput <br>
// * Copyright: Inventory 2000-2006, GBIC 2005, all rights reserved <br>
// * Changelog:
// * <ul>
// * <li> 2006-03-07, 1.0.0, DI Matthijssen
// * <li> 2006-05-14; 1.1.0; MA Swertz integration into Inveninge (and major
// * rewrite)
// * <li> 2006-05-14; 1.2.0; RA Scheltema major rewrite + cleanup
// * </ul>
// */
//
//package org.molgenis.framework.ui.html;
//
//// jdk
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Vector;
//
//import org.molgenis.util.tuple.Tuple;
//import org.molgenis.util.ValueLabel;
//
///**
// * (Incomplete) Special input to choose ontology terms from an OntoCat service.
// */
//public class OntologyInput extends HtmlInput<String>
//{
//	private List<ValueLabel> options = new Vector<ValueLabel>();
//	private String targetfield;
//	
//	public OntologyInput(String name, Object value)
//	{
//		super(name, value);
//	}
//	
//	public OntologyInput(String name, String label, String value,
//			boolean nillable, boolean readonly, String description)
//	{
//		super(name,label,value, nillable,readonly, description);
//	}
//	
//	public OntologyInput(Tuple t) throws HtmlInputException
//	{
//		super(t);
//	}
//
//	@Override
//	public String toHtml()
//	{
//		String readonly = (this.isReadonly()) ? " readonly class=\"readonly\" " : "";
//
//		if (this.isHidden())
//		{
//			StringInput input = new StringInput(this.getName(), super.getValue());
//			input.setLabel(this.getLabel());
//			input.setDescription(this.getDescription());
//			input.setHidden(true);
//			return input.toHtml();
//		}
//
//		StringBuffer optionsHtml = new StringBuffer();
//
//		for (ValueLabel choice : options)
//		{
//			if (super.getValue().equals(choice.getValue().toString()))
//			{
//				optionsHtml.append("\t<option selected value=\"" + choice.getValue() + "\">" + choice.getLabel() + "</option>\n");
//			}
//			else if (!this.isReadonly())
//			{
//				optionsHtml.append("\t<option value=\"" + choice.getValue() + "\">" + choice.getLabel() + "</option>\n");
//			}
//		}
//
//		if (super.getValue().toString().equals(""))
//		{
//			optionsHtml.append("\t<option selected value=\"\"></option>\n");
//			// empty option
//		}
//		else if (!this.isReadonly())
//		{
//			optionsHtml.append("\t<option value=\"\"></option>\n");
//			// empty option
//		}
//		return "<select id=\"" + this.getId() + "\" name=\"" + this.getName() + "\" " + readonly + ">\n" + optionsHtml.toString() + "</select>\n";
//	}
//
//	@Override
//	/**
//	 * Note, this returns the labels of the selected values.
//	 */
//	public String getValue()
//	{
//		StringBuffer result = new StringBuffer();
//		for (ValueLabel choice : options)
//		{
//			if (super.getValue().equals(choice.getValue().toString()))
//			{
//				result.append(choice.getLabel() + " ");
//			}
//		}
//		return result.toString();
//	}
//
//	public List<ValueLabel> getChoices()
//	{
//		return options;
//	}
//
//	public void setOptions( ValueLabel... choices )
//	{
//		this.options = Arrays.asList(choices);
//	}
//
//	public void setOptions( List<ValueLabel> choices )
//	{
//		this.options = choices;
//	}
//	
//	public void setOptions( String... choices )
//	{
//		List<ValueLabel> choicePairs = new ArrayList<ValueLabel>();
//		for (String choice : choices)
//		{
//			choicePairs.add(new ValueLabel(choice, choice));
//		}
//		this.setOptions(choicePairs);
//	}
//
//	public String getTargetfield()
//	{
//		return targetfield;
//	}
//
//	public void setTargetfield( String targetfield )
//	{
//		this.targetfield = targetfield;
//	}
//
//	@Override
//	public String toHtml(Tuple params) throws HtmlInputException
//	{
//		return new OntologyInput(params).render();
//	}
// }
