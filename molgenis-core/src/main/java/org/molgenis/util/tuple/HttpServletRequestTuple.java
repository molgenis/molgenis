package org.molgenis.util.tuple;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

/**
 * Simple Map based implementation of Tuple that wraps HttpServletRequest.
 * <p>
 * HttpRequestTuple can thus be questioned as if it was a Tuple. It uses the <a
 * href="http://jakarta.apache.org/commons/fileupload/using.html">org.apache.
 * commons.fileupload</a> to parse multipart requests
 */
public class HttpServletRequestTuple extends AbstractTuple
{
	private static final Logger logger = Logger.getLogger(HttpServletRequestTuple.class);

	private static final long serialVersionUID = 1L;

	private final transient HttpServletRequest request;
	// naughty hack but we sometimes need this as well for redirects
	private final transient HttpServletResponse response;

	/** indicates whether this request contains multipart content */
	private boolean isMultipartRequest;
	/** params for request with multipart content */
	private Map<String, Object> multipartParams;

	// counter for the amount of files in the request
	private int fileCtr = 0;
	private String previousFieldName = "";

	public HttpServletRequestTuple(HttpServletRequest request) throws IOException
	{
		this(request, null);
	}

	@SuppressWarnings("deprecation")
	public HttpServletRequestTuple(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		if (request == null) throw new IllegalArgumentException("request is null");
		this.request = request;
		this.response = response;

		// parse multipart content request
		this.isMultipartRequest = ServletFileUpload.isMultipartContent(request);
		if (this.isMultipartRequest)
		{
			this.multipartParams = new HashMap<String, Object>();
			parseMultipartContentRequest();
		}
	}

	public HttpServletRequest getRequest()
	{
		return request;
	}

	public HttpServletResponse getResponse()
	{
		return response;
	}

	@Override
	public int getNrCols()
	{
		return isMultipartRequest ? multipartParams.size() : request.getParameterMap().size();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterable<String> getColNames()
	{
		return isMultipartRequest ? multipartParams.keySet() : (Set<String>) (request.getParameterMap().keySet());
	}

	@Override
	public Object get(String colName)
	{
		return isMultipartRequest ? multipartParams.get(colName) : request.getParameter(colName);
	}

	@Override
	public Object get(int col)
	{
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getList(String colName)
	{
		if (isMultipartRequest)
		{
			Object multipartParam = multipartParams.get(colName);
			if (multipartParam instanceof List<?>) return (List<String>) multipartParam;
			else if (multipartParam instanceof String) return Collections.singletonList((String) multipartParam);
		}
		return null;
	}

	@Override
	public List<String> getList(int col)
	{
		throw new UnsupportedOperationException();
	}

	public File getFile(String fileName)
	{
		return isMultipartRequest ? (File) multipartParams.get(fileName) : null;
	}

	private void parseMultipartContentRequest() throws IOException
	{
		FileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);
		logger.debug("Current upload max size: " + upload.getSizeMax());
		upload.setSizeMax(Long.MAX_VALUE);

		List<?> multipart;
		try
		{
			multipart = upload.parseRequest(request);
		}
		catch (FileUploadException e)
		{
			logger.warn(e);
			throw new IOException(e);
		}

		// get the separate elements
		for (int i = 0; i < multipart.size(); i++)
		{
			FileItem item = (FileItem) multipart.get(i);
			if (item.isFormField())
			{
				getFormFieldValues(multipart, i, item);
			}
			else
			{
				getAttachedFileValues(item); // is a file
			}
		}
	}

	private void getAttachedFileValues(FileItem item) throws IOException
	{
		// http://jakarta.apache.org/commons/fileupload/using.html
		if (item.getSize() != 0)
		{
			// copy the file to a tempfile
			String filename = item.getName();
			String fileNumber;

			// handle multiple files in a filefield and multiple filefields in a
			// form
			if (item.getFieldName().equals(this.previousFieldName))
			{
				// increase the filecounter
				this.fileCtr++;
				fileNumber = Integer.toString(this.fileCtr);

			}
			else
			{

				// for backwards compatibility reasons there should not be a
				// filenumber added in the string if there is only 1 file.
				fileNumber = "";

				// reset the file counter in case there is more then one file
				// field in the form
				this.fileCtr = 0;

			}
			this.previousFieldName = item.getFieldName();

			// copy the file to a tempfile
			String extension = "text"; // default extension
			if (filename.lastIndexOf('.') > 0)
			{
				extension = filename.substring(filename.lastIndexOf('.'));
			}

			File uploadedFile = File.createTempFile("molgenis", extension);
			try
			{
				item.write(uploadedFile);
			}
			catch (Exception e)
			{
				throw new IOException(e);
			}

			// add the file to the tuple
			multipartParams.put(item.getFieldName() + fileNumber, uploadedFile);

			// also add the original filename
			multipartParams.put(item.getFieldName() + fileNumber + "OriginalFileName", filename);
		}
	}

	private void getFormFieldValues(List<?> multipart, int i, FileItem item)
	{
		// try to find if there are more of this name
		String name = item.getFieldName();
		List<String> elements = new ArrayList<String>();

		elements.add(item.getString());
		for (int j = i + 1; j < multipart.size(); j++)
		{
			FileItem item2 = (FileItem) multipart.get(j);
			if (item2.getFieldName().equals(name))
			{
				elements.add(item2.getString());
				multipart.remove(j--);
			}
		}

		if (elements.size() == 1)
		{
			if (item.getString().isEmpty()) multipartParams.put(item.getFieldName(), null);
			else
				multipartParams.put(item.getFieldName(), item.getString());
		}
		else
		{
			// strip out null values
			for (int j = 0; j < elements.size(); j++)
			{
				if (elements.get(j) == null || elements.get(j).isEmpty()) elements.remove(j);
			}
			multipartParams.put(item.getFieldName(), elements);
		}
	}
}
