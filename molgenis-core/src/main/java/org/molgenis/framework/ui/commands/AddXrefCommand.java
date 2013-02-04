package org.molgenis.framework.ui.commands;

import java.io.OutputStream;
import java.util.List;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.ui.FormController;
import org.molgenis.framework.ui.FormModel;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.framework.ui.ScreenMessage;
import org.molgenis.framework.ui.ScreenModel;
import org.molgenis.framework.ui.html.AbstractRefInput;
import org.molgenis.framework.ui.html.ActionInput;
import org.molgenis.framework.ui.html.ActionInput.Type;
import org.molgenis.framework.ui.html.EntityForm;
import org.molgenis.framework.ui.html.HiddenInput;
import org.molgenis.framework.ui.html.HtmlElement.UiToolkit;
import org.molgenis.framework.ui.html.HtmlInput;
import org.molgenis.framework.ui.html.XrefInput;
import org.molgenis.util.Entity;

/**
 * The command to add a new record
 */
public class AddXrefCommand<E extends Entity> extends AddCommand<E>
{
	private static final long serialVersionUID = 1512493344265778285L;
	private E xrefEntity;
	private EntityForm<?> xrefForm;

	// private UiToolkit library = HtmlSettings.uiToolkit;

	public AddXrefCommand(ScreenController<?> parent)
	{
		this("", parent, null, null);
	}

	public AddXrefCommand(String name, ScreenController<?> parent, E xrefEntity, EntityForm<?> xrefForm)
	{
		// if the parent is a command then we need to getParent() until we find
		// a form...
		super(name, getParentController(parent));
		this.xrefEntity = xrefEntity;
		this.xrefForm = xrefForm;
		if (xrefEntity != null)
		{
			this.setLabel("Add " + xrefEntity.getClass().getSimpleName());
		}
		this.setIcon("img/new.png");
		this.setDialog(true);
		this.setMenu("Edit");
		this.setToolbar(false);
	}

	// get the formController that this command is part of.
	private static FormController<?> getParentController(ScreenController<?> parent)
	{
		ScreenController<?> formController = parent;
		while (formController.hasParent() && !(formController instanceof FormController))
			formController = formController.getParent();
		return (FormController<?>) formController;
	}

	@Override
	public List<ActionInput> getActions()
	{
		List<ActionInput> actions = super.getActions();

		// Override functionality of standard save button:
		// postData(): post data via rest api, retrieve object including id back
		// setXrefOption(): set newly inserted object as current option
		for (int i = 0; i < actions.size(); i++)
		{
			if (actions.get(i).getType() == Type.SAVE)
			{
				if (actions.get(i).getUiToolkit() == UiToolkit.ORIGINAL) actions
						.get(i)
						.setJavaScriptAction(
								"if( validateForm(document.forms[0],molgenis_required) ) { if( window.opener.name == '' ){ window.opener.name = 'molgenis'+new Date().getTime();} var entity = postData(document.forms[0].entity_name.value); window.opener.setXrefOption(document.forms[0].__action.value, document.forms[0].id_field.value, document.forms[0].label_field.value, entity); window.close();} else return false;");
				else
					actions.get(i)
							.setJavaScriptAction(
									"if( $(this.form).valid() && validateForm(document.forms[0],molgenis_required) ) { if( window.opener.name == '' ){ window.opener.name = 'molgenis'+new Date().getTime();} var entity = postData(document.forms[0].entity_name.value); window.opener.setXrefOption(document.forms[0].__action.value, document.forms[0].id_field.value, document.forms[0].label_field.value, entity); ;window.close();} return false;");
			}
		}
		return actions;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<HtmlInput<?>> getInputs() throws DatabaseException
	{
		List<HtmlInput<?>> inputs = this.xrefForm.getInputs();
		for (int i = 0; i < inputs.size(); i++)
		{
			if (inputs.get(i) instanceof XrefInput)
			{
				((AbstractRefInput<E>) inputs.get(i)).setIncludeAddButton(false);
			}
		}
		// add three hidden fields for javascript to know entity name, id field
		// and label field
		inputs.add(new HiddenInput("entity_name", xrefEntity.getClass().getSimpleName().toLowerCase()));
		inputs.add(new HiddenInput("id_field", this.xrefEntity.getIdField()));
		inputs.add(new HiddenInput("label_field", this.xrefEntity.getLabelFields().get(0)));
		return inputs;
	}

	@Override
	public boolean isVisible()
	{
		return false;
	}

	@Override
	public ScreenModel.Show handleRequest(Database db, MolgenisRequest request, OutputStream downloadStream)
			throws Exception
	{
		if (request.getString(FormModel.INPUT_SHOW) == null)
		{
			ScreenMessage msg = null;
			try
			{
				db.beginTx();
				xrefEntity.set(request);
				int updatedRows = db.add(xrefEntity);
				db.commitTx();
				msg = new ScreenMessage("ADD SUCCESS: affected " + updatedRows, null, true);
			}
			catch (Exception e)
			{
				msg = new ScreenMessage("ADD FAILED: " + e.getMessage(), null, false);
				e.printStackTrace();
				if (db.inTx()) db.rollbackTx();
			}
			((FormController<?>) this.getController()).getModel().getMessages().add(msg);
		}
		return ScreenModel.Show.SHOW_MAIN;
	}
}