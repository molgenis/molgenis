package org.molgenis.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.apache.log4j.Logger;

/**
 * Implementation of the ImageResizer interface * Uses a Runtime.exec()to use
 * imagemagick to perform the given conversion operation. Returns true on
 * success, false on failure. Does not check if either file exists.
 * 
 * With help from
 * http://www.darcynorman.net/2005/03/15/jai-vs-imagemagick-image-resizing/
 * 
 * @author D'Arcy Norman
 * @author Morris Swertz
 */
public class ImageMagickResizer implements ImageResizer
{
	private static final Logger logger = Logger.getLogger(ImageMagickResizer.class);

	// assumes the command 'convert' is on classpath
	public static final String IMAGEMAGICK_COMMAND = "C:/Program Files/ImageMagick-6.5.4-Q16/convert";

	@Override
	public boolean resize(File src, File dest, int destWidth, int destHeight) throws Exception
	{
		String command = IMAGEMAGICK_COMMAND + " -resize %sx%s -quality 75 \"%s\" \"%s\"";
		command = String.format(command, destWidth, destHeight, src.getAbsolutePath(), dest.getAbsolutePath());
		logger.debug("Executing ImageMagick: " + command);

		Process process;

		try
		{
			process = Runtime.getRuntime().exec(command);
			process.waitFor();

			// get error messages
			String error = streamToString(process.getErrorStream());
			if (error.length() > 0)
			{
				logger.error("ImageMagick printed errors: " + error);
				return false;
			}

			// get output messages
			String output = streamToString(process.getInputStream());
			if (error.length() > 0)
			{
				logger.debug("ImageMagick printed messages: " + output);
				return false;
			}

			// check exit value
			if (process.exitValue() > 0)
			{
				throw new Exception("" + process.exitValue());
			}

			return true;
		}
		catch (IOException e)
		{
			logger.error("IOException while trying to execute " + command);
			return false;
		}
	}

	/** Helper function to translate streams to strings */
	private String streamToString(InputStream inputStream) throws IOException
	{
		StringBuffer fileContents = new StringBuffer();
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
		String line;
		while ((line = reader.readLine()) != null)
		{
			fileContents.append(line + "\n");
		}
		reader.close();
		inputStream.close();
		return fileContents.toString();
	}
}
