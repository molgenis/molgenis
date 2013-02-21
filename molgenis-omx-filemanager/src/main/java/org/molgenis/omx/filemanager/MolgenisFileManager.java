package org.molgenis.omx.filemanager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.List;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.ui.FormController;
import org.molgenis.framework.ui.FormModel;
import org.molgenis.framework.ui.PluginModel;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.framework.ui.ScreenMessage;
import org.molgenis.omx.core.MolgenisFile;
import org.molgenis.omx.decorators.MolgenisFileHandler;
import org.molgenis.omx.services.FileUploadUtils;
import org.molgenis.util.Entity;

public class MolgenisFileManager extends PluginModel<Entity>
{
	private static final long serialVersionUID = 1L;

	private final transient MolgenisFileManagerModel model = new MolgenisFileManagerModel();
	private transient MolgenisFileHandler mfh;
	private String appLoc;

	public MolgenisFileManager(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	public MolgenisFileManagerModel getMyModel()
	{
		return model;
	}

	@Override
	public String getViewName()
	{
		return "plugins_molgenisfile_MolgenisFileManager";
	}

	@Override
	public String getViewTemplate()
	{
		return "templates/" + MolgenisFileManager.class.getName().replace('.', '/') + ".ftl";
	}

	@Override
	public void handleRequest(Database db, MolgenisRequest request)
	{
		appLoc = request.getAppLocation();
		try
		{
			if (request.getString("__action") != null)
			{
				String action = request.getString("__action");

				File file = null;

				if (request.getString("__action").equals("uploadTextArea"))
				{
					String content = request.getString("inputTextArea");
					File inputTextAreaContent = new File(System.getProperty("java.io.tmpdir") + File.separator
							+ "tmpTextAreaInput" + System.nanoTime() + ".txt");
					BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
							inputTextAreaContent), Charset.forName("UTF-8")));
					out.write(content);
					out.close();
					file = inputTextAreaContent;
				}
				else if (action.equals("upload"))
				{
					file = request.getFile("upload");
				}
				else if (action.equals("showApplet"))
				{
					this.model.setShowApplet(true);
				}
				else if (action.equals("hideApplet"))
				{
					this.model.setShowApplet(false);
				}

				if (file == null)
				{
					throw new FileNotFoundException("No file selected");
				}

				FileUploadUtils.doUpload(db, this.model.getMolgenisFile(), file, false);
				this.setMessages(new ScreenMessage("File uploaded", true));
			}

		}

		catch (Exception e)
		{
			e.printStackTrace();
			this.setMessages(new ScreenMessage(e.getMessage() != null ? e.getMessage() : "null", false));
		}
	}

	@Override
	public void reload(Database db)
	{

		try
		{

			if (this.model.getShowApplet() == null)
			{
				this.model.setShowApplet(false);
			}

			ScreenController<?> parentController = this.getParent();
			FormModel<MolgenisFile> parentForm = (FormModel<MolgenisFile>) ((FormController) parentController)
					.getModel();

			List<MolgenisFile> molgenisFileList = parentForm.getRecords();
			MolgenisFile molgenisFile = null;

			if (molgenisFileList.size() == 0)
			{
				return;
			}
			else
			{
				molgenisFile = molgenisFileList.get(0);
			}

			this.model.setMolgenisFile(molgenisFile);

			if (mfh == null)
			{
				mfh = new MolgenisFileHandler(db);
			}

			boolean hasFile = false;
			File theFile = null;

			try
			{
				theFile = mfh.getFile(molgenisFile, db);
				hasFile = true;
			}
			catch (FileNotFoundException e)
			{
				// no file found, assume there is none for this MolgenisFile
				// object :)
			}

			this.model.setHasFile(hasFile);

			// set app location
			if (this.model.getDb_path() == null)
			{
				this.model.setDb_path(appLoc);
			}

			if (hasFile)
			{
				this.model.setFileSize(theFile.length());
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
			this.setMessages(new ScreenMessage(e.getMessage() != null ? e.getMessage() : "null", false));

		}
	}

}
