package org.molgenis.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.servlet.http.Part;

import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

public class FileUploadUtils
{

	/**
	 * Saves an uploaded file to a tempfile with prefix 'molgenis-', keeps the original file extension
	 * 
	 * @param part
	 * @return the saved temp file or null is no file selected
	 * @throws IOException
	 */
	public static File saveToTempFile(Part part) throws IOException
	{
		String filename = getOriginalFileName(part);
		if (filename == null)
		{
			return null;
		}

		File file = File.createTempFile("molgenis-", "." + StringUtils.getFilenameExtension(filename));
		FileCopyUtils.copy(part.getInputStream(), new FileOutputStream(file));

		return file;
	}

	/**
	 * Get the filename of an uploaded file
	 * 
	 * @param part
	 * @return the filename or null if not present
	 */
	public static String getOriginalFileName(Part part)
	{
		String contentDisposition = part.getHeader("content-disposition");
		if (contentDisposition != null)
		{
			for (String cd : contentDisposition.split(";"))
			{

				if (cd.trim().startsWith("filename"))
				{
					String filename = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
					return StringUtils.isEmpty(filename) ? null : filename;
				}
			}
		}

		return null;
	}

}
