package org.molgenis.framework.ui.html;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.server.QueryRuleUtil;
import org.molgenis.framework.server.services.MolgenisGuiService;
import org.molgenis.util.Entity;
import org.molgenis.util.tuple.Tuple;

import com.google.gson.Gson;

public abstract class AbstractRefInput<E> extends HtmlInput<E>
{
	private static String DEFAULT_URL = "xref/find";

	protected abstract String renderOptions();

	// determines how ajax-chosen renders the select (multiple, search)
	protected abstract String getHtmlRefType();

	public static final String XREF_FIELD = "xrefField";
	public static final String XREF_ENTITY = "xrefEntity";
	public static final String XREF_LABELS = "xrefLabels";
	public static final String FILTERS = "filters";
	public static final String SEARCH_TERM = "searchTerm";

	public static final String ERRROR = "error";
	public static final String INCLUDE_ADD_BUTTON = "includeAddButton";

	protected final Class<? extends Entity> xrefEntity;
	protected List<String> xrefLabels = new ArrayList<String>();
	protected String xrefField;
	protected String url = DEFAULT_URL;
	protected boolean prefill = true;
	private String placeholder = "";

	protected List<QueryRule> filters = new ArrayList<QueryRule>();

	protected String error = null;
	protected boolean includeAddButton = false;

	protected AbstractRefInput(String name, Class<? extends Entity> xrefClass, String label, E value, boolean nillable,
			boolean readonly, String description)
	{
		super(name, label, value, nillable, readonly, description);
		xrefEntity = xrefClass;
	}

	protected AbstractRefInput(String name, Class<? extends Entity> xrefClass, E value)
	{
		super(name, value);
		xrefEntity = xrefClass;
	}

	protected AbstractRefInput()
	{
		xrefEntity = null;
	}

	@SuppressWarnings("unchecked")
	public AbstractRefInput(Tuple t) throws HtmlInputException
	{
		super(t);
		Class<? extends Entity> klass = null;
		try
		{
			klass = (Class<? extends Entity>) Class.forName(t.getString(XREF_ENTITY));
		}
		catch (Exception e)
		{
			throw new HtmlInputException(e);
		}
		xrefEntity = klass;

		xrefField = t.getString(XREF_FIELD);
		xrefLabels = t.getList(XREF_LABELS);

		if (!t.isNull(FILTERS))
		{
			filters = (List<QueryRule>) t.get(FILTERS);
		}
		if (!t.isNull(ERRROR))
		{
			error = t.getString(ERRROR);
		}
		if (!t.isNull(INCLUDE_ADD_BUTTON))
		{
			includeAddButton = t.getBoolean(INCLUDE_ADD_BUTTON);
		}
	}

	/**
	 * Set the entity where this xref should get its values from
	 * 
	 * @param xrefEntity
	 * @throws HtmlInputException
	 */
	protected void setXrefEntity(Class<? extends Entity> xrefEntity)
	{
		try
		{
			Entity instance = xrefEntity.newInstance();
			this.xrefField = instance.getIdField();
			this.xrefLabels = instance.getLabelFields();
			this.placeholder = "Choose " + instance.getClass().getSimpleName();
			// this.xrefEntity = xrefEntity;
		}
		catch (Exception e)
		{
			this.error = e.getMessage();
			e.printStackTrace();
		}
	}

	/**
	 * Set the entity where this xref should get its values from
	 * 
	 * @param xrefEntity
	 * @throws HtmlInputException
	 */
	@SuppressWarnings(
	{ "unchecked" })
	public void setXrefEntity(String xrefClassname) throws HtmlInputException
	{
		try
		{
			this.setXrefEntity((Class<? extends Entity>) Class.forName(xrefClassname));
		}
		catch (ClassNotFoundException e)
		{
			throw new HtmlInputException(xrefClassname);
		}
	}

	public String getXrefField()
	{
		return xrefField;
	}

	/**
	 * Set the entity field (i.e. database column) that this xref should get its
	 * values from. For example 'id'.
	 * 
	 * @param xrefField
	 *            field name
	 */
	public void setXrefField(String xrefField)
	{
		this.xrefField = xrefField;
	}

	public Class<? extends Entity> getXrefEntity()
	{
		return xrefEntity;
	}

	public List<String> getXrefLabels()
	{
		return xrefLabels;
	}

	/**
	 * Set the entity field (i.e. database column) that provides the values that
	 * should be shown to the user as options in the xref select box. For
	 * example 'name'.
	 * 
	 * @param xrefLabel
	 *            field name
	 */
	public void setXrefLabel(String xrefLabel)
	{
		assert (xrefLabel != null);
		this.xrefLabels.clear();
		this.xrefLabels.add(xrefLabel);
	}

	/**
	 * In case of entities with multiple column keys you can also have multiple
	 * labels concatenated together. For example 'investigation_name, name'.
	 * 
	 * @param xrefLabels
	 *            a list of field names
	 */
	public void setXrefLabels(List<String> xrefLabels)
	{
		assert (xrefLabels != null);
		this.xrefLabels = xrefLabels;
	}

	public List<QueryRule> getXrefFilters()
	{
		return filters;
	}

	public void setXrefFilters(List<QueryRule> xrefFilter)
	{
		this.filters = xrefFilter;
	}

	public String getXrefFilterRESTString()
	{
		return QueryRuleUtil.toRESTstring(filters);
	}

	public String getXrefEntitySimpleName()
	{
		String name = xrefEntity.getSimpleName();
		if (name.contains(".")) return name.substring(name.lastIndexOf(".") + 1);
		return name;
	}

	public ActionInput createAddButton()
	{
		ActionInput addButton = new ActionInput("add", "", "");

		addButton.setId(this.getId() + "_addbutton");
		addButton.setButtonValue("Add new " + this.getXrefEntitySimpleName());
		addButton.setIcon("img/new.png");

		addButton
				.setJavaScriptAction("if( window.name == '' ){ window.name = 'molgenis_"
						+ MolgenisGuiService.getNewWindowId()
						+ "';}document.getElementById('"
						+ this.getId()
						+ "').form.__action.value='"
						+ this.getId()
						+ "';molgenis_window = window.open('','molgenis_edit_new_xref','height=800,width=600,location=no,status=no,menubar=no,directories=no,toolbar=no,resizable=yes,scrollbars=yes');document.getElementById('"
						+ this.getId() + "').form.target='molgenis_edit_new_xref';document.getElementById('"
						+ this.getId() + "').form.__show.value='popup';document.getElementById('" + this.getId()
						+ "').form.submit();molgenis_window.focus();");
		return addButton;
	}

	public void setIncludeAddButton(boolean includeAddButton)
	{
		this.includeAddButton = includeAddButton;
	}

	public final String toJquery(String htmlOptions, String xrefLabelString)
	{
		if (this.isHidden())
		{
			return this.renderHidden();
		}

		final String cssClasses = String.format("%s %s", this.isReadonly() ? "readonly " : "", this.isNillable() ? ""
				: "required ");

		AjaxChosenData data = new AjaxChosenData();

		data.xrefEntity = this.getXrefEntity().getName();
		data.xrefField = this.getXrefField();
		data.xrefLabels = xrefLabelString;
		data.nillable = isNillable();

		final boolean hasFilters = getXrefFilters() != null && getXrefFilters().size() > 0;
		if (hasFilters)
		{
			data.filters = new Gson().toJson(getXrefFilters());
		}

		// String preloadScript =
		// "$("+getId()+").click(function() {if($(this).find('a.chzn-single-with-drop,.chzn-drop').length > 1 ) "
		// +
		// "{$(this).find('input[type=text]:first').keyup();}});";

		// simulate keyup to load data; then blur by removing
		// class=chzn-single-with-drop
		String preloadScript = "$('#" + getId()
				+ "_chzn').find('input[type=text]:first').focus(function(){$(this).keyup()});";

		// #arg1 = id
		// #arg2 = title
		// #arg3 = input type (xref=""?? or mref="multiple")
		// #arg4 = dataplaceHolder (is this needed?)
		// #arg5 = class(es) --> add required class or none class or extra
		// classes
		// #arg6 = options for the select
		String selectHtml = "<select id='%1$s' name='%1$s' class='%5$s ui-widget-content ui-corner-all' search='' "
				+ "title='%2$s' style='width: 350px; display: none;' data-placeholder='Choose some %4$s' %3$s>%6$s</select>";
		final String select = String.format(selectHtml, getId(), getDescription(), getHtmlRefType(), placeholder,
				cssClasses, htmlOptions);

		// #arg1 = id
		// #arg2 = url of service
		// #arg3 = data
		// #arg4 = SEARCH_TERM
		// #arg5 = prefillScript (see below in this file)
		// #arg6 = htmlRefType determines if it's a xref or mref box
		AjaxChosenConfig config = new AjaxChosenConfig();
		config.url = url;
		config.data = data;
		config.jsonTermKey = SEARCH_TERM;
		Gson gson = new Gson();

		String handleScript = "function (data) {var terms = {}; $.each(data, function (i, val) {terms[i] = val;});return terms;}";
		final String ajaxChosenScript = "<script>$('#" + getId() + "').ajaxChosen(" + gson.toJson(config) + ", "
				+ handleScript + ");" + preloadScript + "</script>";

		final String includeButton = includeAddButton && !this.isReadonly() ? this.createAddButton().toString() : "";
		return select + ajaxChosenScript + includeButton;
	}

	public abstract String renderHidden();

	@Override
	public String toHtml()
	{
		if (this.error != null) return "ERROR: " + error;

		if (getXrefEntity() == null || "".equals(getXrefField()) || getXrefLabels() == null
				|| getXrefLabels().size() == 0)
		{
			throw new RuntimeException("XrefInput(" + this.getName()
					+ ") is missing xrefEntity, xrefField and/or xrefLabels settings");
		}

		final String xrefLabelString = StringUtils.join(getXrefLabels(), ",");
		String readonly = (this.isReadonly()) ? " readonly class=\"readonly\" " : String.format(
				" onfocus=\"showXrefInput(this,'%s','%s','%s','%s'); return false;\" ", getXrefEntity(),
				getXrefField(), xrefLabelString, getXrefFilters());

		String optionsHtml = StringUtils.EMPTY;
		if (super.getObject() != null)
		{
			optionsHtml = renderOptions();
		}

		if (this.uiToolkit == UiToolkit.ORIGINAL)
		{
			final String htmlSelect = "<select id=\"" + this.getId() + "\" name=\"" + this.getName() + "\" " + readonly
					+ ">\n" + renderOptions() + "</select>\n"
					+ (includeAddButton && !this.isReadonly() ? this.createAddButton() : "");
			return htmlSelect;
		}
		else if (this.uiToolkit == UiToolkit.JQUERY)
		{
			return toJquery(optionsHtml, xrefLabelString);
		}
		else
		{
			return "NOT IMPLEMENTED FOR LIBRARY " + this.uiToolkit;
		}
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public boolean isPrefill()
	{
		return prefill;
	}

	public void setPrefill(boolean prefill)
	{
		this.prefill = prefill;
	}

	public String getPlaceholder()
	{
		return this.placeholder;
	}

	public void setPlaceholder(String placeholder)
	{
		this.placeholder = placeholder;
	}

	class AjaxChosenData
	{
		public String xrefEntity;
		public String xrefField;
		public String xrefLabels;
		public boolean nillable;
		public String filters;
	}

	class AjaxChosenConfig
	{
		public String method = "GET";
		public String url = "";
		public AjaxChosenData data;
		public String dataType = "json";
		public int minTermLength = 0;
		public int afterTypeDelay = 300;
		public String jsonTermKey = "";
		public int test = -100;
	}

}
