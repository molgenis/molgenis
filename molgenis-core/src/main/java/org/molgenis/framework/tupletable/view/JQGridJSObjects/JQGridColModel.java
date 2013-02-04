package org.molgenis.framework.tupletable.view.JQGridJSObjects;

import org.apache.log4j.Logger;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.model.MolgenisModelException;
import org.molgenis.model.elements.Field;

@SuppressWarnings("unused")
@edu.umd.cs.findbugs.annotations.SuppressWarnings(value =
{ "URF_UNREAD_FIELD", "UUF_UNUSED_FIELD" }, justification = "Gson reads private fields")
public class JQGridColModel
{
	private static final Logger logger = Logger.getLogger(JQGridColModel.class);

	private String name;
	private String label;
	private String index;
	private int width = 100;
	private String stype;
	private boolean sortable = false;
	private boolean search = true;
	private boolean fixed = false;
	private SearchOptions searchoptions;
	private SearchRule searchrules;
	private EditOptions editoptions;
	private boolean editable;
	// for tree view
	private String title;
	private boolean isFolder = false;
	private String path;
	private String edittype;
	private String add;
	private String datetype;

	public JQGridColModel(String f)
	{
		this.name = f;
		this.index = f;
		this.title = f;
		this.path = f;
		this.editable = false;
		this.sortable = false;
	}

	// FIXME call JQGridColModel(String)
	// TODO discuss: why JQGridColModel(Field)/JQGridColModel(String) sets
	// editable to true/false
	public JQGridColModel(Field f)
	{
		this.name = f.getSqlName();
		this.index = f.getSqlName();
		this.label = f.getLabel();
		this.title = name;
		this.path = title;
		this.editable = true;
		this.sortable = false;

		this.searchoptions = SearchOptions.create(f.getType().getEnumType());
		this.searchrules = SearchRule.createSearchRule(f.getType().getEnumType());

		// TODO discuss: why try-catch for all code?
		try
		{
			// TODO discuss: possible code smell, hard coded reference
			if (!name.equals("Pa_Id"))
			{

				StringBuilder strBuilder = new StringBuilder();

				if ("enum".equals(f.getType().toString()))
				{

					for (String category : f.getEnumOptions())
					{
						String code = category.split("\\.")[0];
						strBuilder.append(code).append(':').append(category).append(';');
					}
					strBuilder.deleteCharAt(strBuilder.length() - 1);

					edittype = "select";

					String editOptionsStr = strBuilder.toString();
					editoptions = EditOptions.createEditOptions(":;" + editOptionsStr);
					searchoptions.sopt = new String[]
					{ "eq", "ne" };
					this.stype = "select";
					searchoptions.value = editOptionsStr;

				}
				else if ("bool".equals(f.getType().toString()))
				{
					edittype = "select";

				}
				else if ("datetime".equals(f.getType().toString()))
				{
					datetype = "datetype";
				}

			}
			else
			{

				editoptions = EditOptions.createEditOptions();
				editoptions.disabled = "disabled";
				editoptions.style = "width:100px;background:lightgrey";
				fixed = true;
			}

		}
		catch (MolgenisModelException e)
		{
			logger.error(e);
		}

	}

	private static class SearchOptions
	{
		private boolean required = true;
		private boolean searchhidden = true;
		private String value;
		private String[] sopt = new String[]
		{ "eq", "bw", "ew", "cn" };

		private String dataInit = "function(elem){ $(elem).datepicker({dateFormat:\"mm/dd/yyyy\"});}}";

		public SearchOptions()
		{
		}

		public SearchOptions(String[] sopt)
		{
			this.sopt = sopt;
		}

		public static SearchOptions create(FieldTypeEnum fte)
		{
			switch (fte)
			{
				case INT:
				case LONG:
				case DECIMAL:
				case DATE:
				case DATE_TIME:
					return new SearchOptions(new String[]
					{ "eq", "lt", "le", "gt", "ge" });

				default:
					return new SearchOptions();
			}
		}
	}

	private static class SearchRule
	{
		private boolean number = false;
		private boolean integer = false;
		private boolean email = false;
		private boolean date = false;
		private boolean time = false;

		public static SearchRule createSearchRule(FieldTypeEnum fte)
		{
			switch (fte)
			{
				case INT:
				case LONG:
				{
					final SearchRule rule = new SearchRule();
					rule.integer = true;
					return rule;
				}
				case DECIMAL:
				{
					final SearchRule rule = new SearchRule();
					rule.number = true;
					return rule;
				}
				case DATE:
				{
					final SearchRule rule = new SearchRule();
					rule.date = true;
					return rule;
				}

				default:
					return new SearchRule();
			}
		}
	}

	private static class EditOptions
	{
		private String value;
		private String disabled;
		private String style;
		private String name;

		public static EditOptions createEditOptions(String actualValue)
		{
			final EditOptions editOptions = new EditOptions();
			editOptions.value = actualValue;

			return editOptions;
		}

		// make Pa_Id field disabled and lightgrey
		public static EditOptions createEditOptions()
		{
			final EditOptions editOptions = new EditOptions();

			return editOptions;
		}
	}
}