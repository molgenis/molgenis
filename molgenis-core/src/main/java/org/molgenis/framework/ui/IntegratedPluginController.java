package org.molgenis.framework.ui;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.molgenis.util.tuple.HttpServletRequestTuple;
import org.molgenis.util.tuple.Tuple;

public abstract class IntegratedPluginController<M extends ScreenModel> extends EasyPluginController<M>
{
	private static final long serialVersionUID = 5484188092124337136L;

	public IntegratedPluginController(String name, M model, ScreenController<?> parent)
	{
		super(name, parent);
		this.setModel(model);
	}

	public String include(Tuple request, String path)
	{
		HttpServletRequestTuple rt = (HttpServletRequestTuple) request;
		HttpServletRequest httpRequest = rt.getRequest();
		HttpServletResponse httpResponse = rt.getResponse();
		RedirectTextWrapper respWrapper = new RedirectTextWrapper(httpResponse);

		// Call/include page
		try
		{
			RequestDispatcher dispatcher = httpRequest.getRequestDispatcher(path);
			if (dispatcher != null) dispatcher.include(httpRequest, respWrapper);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return respWrapper.getOutput();
	}

	private class RedirectTextWrapper extends HttpServletResponseWrapper
	{
		private PrintWriter printWriter;
		private StringWriter stringWriter;

		public RedirectTextWrapper(HttpServletResponse response)
		{
			super(response);
			this.stringWriter = new StringWriter();
			this.printWriter = new PrintWriter(stringWriter);
		}

		@Override
		public PrintWriter getWriter()
		{
			return this.printWriter;
		}

		public String getOutput()
		{
			return this.stringWriter.toString();
		}
	}
}
