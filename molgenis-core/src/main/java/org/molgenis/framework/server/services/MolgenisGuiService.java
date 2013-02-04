package org.molgenis.framework.server.services;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.security.Login;
import org.molgenis.framework.server.MolgenisContext;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.server.MolgenisResponse;
import org.molgenis.framework.ui.ApplicationController;
import org.molgenis.framework.ui.EasyPluginController;
import org.molgenis.framework.ui.FormModel;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.framework.ui.ScreenModel;
import org.molgenis.framework.ui.ScreenModel.Show;
import org.molgenis.framework.ui.html.FileInput;

public abstract class MolgenisGuiService
{
	Logger logger = Logger.getLogger(MolgenisGuiService.class);

	protected MolgenisContext mc;
	protected Database db;

	public MolgenisGuiService(MolgenisContext mc)
	{
		this.mc = mc;
	}

	/** keep track of window ids */
	// FIXME: still needed?
	private static long newWindowId;

	public static long getNewWindowId()
	{
		newWindowId++;
		return newWindowId;
	}

	public abstract ApplicationController createUserInterface();

	/**
	 * Handle use of molgenis GUI
	 * 
	 * TODO: this method is horrible and should be properly refactored,
	 * documented and tested!
	 * 
	 * @param request
	 * @param response
	 */
	public void handleRequest(MolgenisRequest request, MolgenisResponse response) throws DatabaseException, IOException
	{

		Database db = request.getDatabase();
		this.db = db;

		// logout
		final HttpSession session = request.getRequest().getSession();
		if (request.getRequest().getParameter("__action") != null
				&& request.getRequest().getParameter("__action").equalsIgnoreCase("Logout"))
		{
			session.setAttribute("application", null);
		}

		// Get application from session
		ApplicationController appController = (ApplicationController) session.getAttribute("application");

		// Login credentials from FrontController
		Login userLogin = request.getDatabase().getLogin();

		// Create GUI if null
		if (appController == null)
		{
			// FIXME: never reached? isLoginRequired is FALSE in both
			// implementations?
			// also, what is request.getRequest().getParameter("logout") ?
			// and when session.isNew() ?
			if ((!userLogin.isAuthenticated() && userLogin.isLoginRequired())
					|| (request.getRequest().getParameter("logout") != null && !session.isNew()))
			{
				response.getResponse().setHeader("WWW-Authenticate", "BASIC realm=\"MOLGENIS\"");
				response.getResponse().sendError(HttpServletResponse.SC_UNAUTHORIZED);
				session.invalidate();
				return;
			}
			appController = createUserInterface();
		}

		// Always pass login to GUI
		appController.setLogin(userLogin);

		// handle request
		try
		{

			if (Show.SHOW_JQGRID.equals(request.getString(FormModel.INPUT_SHOW)))
			{
				ScreenController<? extends ScreenModel> controller = appController.get(request
						.getString(ScreenModel.INPUT_TARGET));
				controller.handleRequest(db, request, response.getResponse().getOutputStream());
			}
			// action == download an attached file
			// FIXME move to form controllers handlerequest...
			else if (FileInput.ACTION_DOWNLOAD.equals(request.getString(ScreenModel.INPUT_ACTION)))
			{
				// logger.info(requestTuple);

				File file = new File(db.getFilesource() + "/" + request.getString(FileInput.INPUT_CURRENT_DOWNLOAD));

				FileInputStream filestream = new FileInputStream(file);

				response.getResponse().setContentType("application/x-download");
				response.getResponse().setContentLength((int) file.length());
				response.getResponse().setHeader("Content-Disposition",
						"attachment; filename=" + request.getString(FileInput.INPUT_CURRENT_DOWNLOAD));

				BufferedOutputStream out = new BufferedOutputStream(response.getResponse().getOutputStream());
				byte[] buffer = new byte[1024];
				int bytes_read;
				while ((bytes_read = filestream.read(buffer)) != -1)
				{
					out.write(buffer, 0, bytes_read);
				}
				filestream.close();
				out.flush();
				out.close();
			}

			// json downloads
			else if (!request.isNull("__action") && request.getAction().startsWith("download_json"))
			{
				// get the screen that will hande the download request
				ScreenController<? extends ScreenModel> controller = appController.get(request
						.getString(ScreenModel.INPUT_TARGET));

				response.getResponse().setContentType("application/json");

				controller.handleRequest(db, request, response.getResponse().getOutputStream());

				// TODO: does this fail when stream is already closed??
				response.getResponse().getOutputStream().flush();
				response.getResponse().getOutputStream().close();
			}

			// action == download, but now in a standard way, handled by
			// controller
			else if (ScreenModel.Show.SHOW_DOWNLOAD.equals(request.getString(FormModel.INPUT_SHOW)))
			{
				// get the screen that will hande the download request
				ScreenController<? extends ScreenModel> controller = appController.get(request
						.getString(ScreenModel.INPUT_TARGET));

				// set the headers for the download
				response.getResponse().setContentType("application/x-download");

				String action = request.getString(ScreenModel.INPUT_ACTION);
				String extension = null;
				if (action.startsWith("download_txt_"))
				{
					extension = "txt";
				}
				else if (action.startsWith("download_xls_"))
				{
					extension = "xls";
				}
				else
				{
					throw new Exception("Download type '" + action + "' unsupported!");
				}

				response.getResponse().setHeader("Content-Disposition",
						"attachment; filename=" + controller.getName().toLowerCase() + "." + extension);

				// let the handleRequest produce the content
				controller.handleRequest(db, request, response.getResponse().getOutputStream());

				// TODO: does this fail when stream is already closed??
				response.getResponse().getOutputStream().flush();
				response.getResponse().getOutputStream().close();
			}

			// handle normal event and then write the response
			else
			{
				// capture select
				if (request.getString("select") != null)
				{
					// get the screen to be selected
					ScreenController<?> toBeSelected = appController.get(request.getString("select"));
					// select leaf in its parent
					try
					{
						toBeSelected.getParent().setSelected(request.getString("select"));
					}
					catch (NullPointerException npe)
					{
						// screen does not exists, ignore request
					}
				}

				if (Show.SHOW_CLOSE.equals(appController.handleRequest(db, request, null)))
				{
					// if close, then write a close script
					PrintWriter writer = response.getResponse().getWriter();
					writer.write("<html><head></head><body><script>window.close();</script></body></html>");
					writer.close();
					return;
				}

				// workaround - see comment @
				// EasyPluginController.HTML_WAS_ALREADY_SERVED
				if (EasyPluginController.HTML_WAS_ALREADY_SERVED != null
						&& EasyPluginController.HTML_WAS_ALREADY_SERVED)
				{
					EasyPluginController.HTML_WAS_ALREADY_SERVED = null;
					return;
				}

				// handle request by reloading (...)
				appController.reload(db);

				// session are automatically synchronized...
				session.setAttribute("application", appController);

				// prepare the response
				response.getResponse().setContentType("text/html");
				// response.setBufferSize(10000);
				PrintWriter writer = response.getResponse().getWriter();

				// Render result
				String show = request.getString(FormModel.INPUT_SHOW);
				if (ScreenModel.Show.SHOW_DIALOG.equals(show))
				{
					appController.getModel().setShow(show);
					ScreenController<?> target = appController.get(request.getString("__target"));
					appController.getModel().setTarget(target);
					writer.write(appController.render());
				}
				else if ("massupdate".equals(show))
				{
					appController.getModel().setShow("show");
					writer.write(appController.render());

				}
				else
				{
					appController.getModel().setShow("root");
					writer.write(appController.render());

					// special: set a different selected screen after rendering
					// is done
					// this enables you to for example create a filtered linkout
					// from a plugin,
					// while staying 'within' the plugin (keep the selection
					// state) when you click another
					// button in the plugin that opens in a new tab (a href
					// target="_blank")
					// if you don't set this, the application will switch to the
					// screen selected for the linkout
					// which makes very strange browsing behaviour for users
					//
					// e.g.
					// molgenis.do?select=Markers&target=Markers&__comebacktoscreen=QTLFinder&__action=filter_set[...]
					// '__target' will send the request to the 'Markers' screen
					// 'select' will tell the controller to render the the
					// 'Markers' screen
					// '__comebacktoscreen' will set the view state back to the
					// plugin so you can continue using it normally
					if (request.getString("__comebacktoscreen") != null)
					{
						ScreenController<?> toBeSelected = appController.get(request.getString("__comebacktoscreen"));
						toBeSelected.getParent().setSelected(request.getString("__comebacktoscreen"));
					}
				}

				writer.close();

				// done, get rid of screen messages here?
				appController.clearAllMessages();

			}
		}
		catch (Exception e)
		{
			throw new DatabaseException(e);
		}
	}
}
