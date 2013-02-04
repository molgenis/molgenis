package org.molgenis.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.FileUtils;

import com.ice.tar.InvalidHeaderException;
import com.ice.tar.TarArchive;
import com.ice.tar.TarBuffer;
import com.ice.tar.TarEntry;

public class TarGz
{

	public static File tarDir(File dir) throws IOException
	{
		File archiveLocation = new File(dir.getParentFile() + File.separator + dir.getName() + ".tar.gz");

		boolean unixArchiveFormat = true;

		TarArchive archive = null;

		OutputStream outStream = System.out;

		if (outStream != null)
		{

			try
			{
				outStream = new GZIPOutputStream(new FileOutputStream(archiveLocation));
			}
			catch (IOException ex)
			{
				outStream = null;
				ex.printStackTrace(System.err);
			}

			archive = new TarArchive(outStream, TarBuffer.DEFAULT_BLKSIZE);
		}

		// write

		File[] files = dir.listFiles();
		for (File file : files)
		{
			if (!file.isDirectory())
			{
				// make regular entry
				TarEntry entry = new TarEntry(file);
				if (unixArchiveFormat)
				{
					entry.setUnixTarFormat();
				}
				else
				{
					entry.setUSTarFormat();
				}
				// FIXME: index -1 wanneer verkeerde file sep!
				entry.setName(entry.getName().substring(entry.getName().lastIndexOf("/") + 1, entry.getName().length()));

				// add to tar. just a file, no need for recursion ('false')
				archive.writeEntry(entry, false);

			}
			else
			{
				// entry is a directory, so tar the content :)
				TarEntry entry = new TarEntry(TarGz.tarDir(file));

				if (unixArchiveFormat)
				{
					entry.setUnixTarFormat();
				}
				else
				{
					entry.setUSTarFormat();
				}
				// FIXME: index -1 wanneer verkeerde file sep!
				entry.setName(entry.getName().substring(entry.getName().lastIndexOf("/") + 1, entry.getName().length()));

				// write entry (now a tar) to tar
				archive.writeEntry(entry, false);
			}
		}

		// close
		if (archive != null) // CLOSE ARCHIVE
		{
			try
			{
				archive.closeArchive();
			}
			catch (IOException ex)
			{
				ex.printStackTrace(System.err);
			}
		}

		return archiveLocation;

	}

	/**
	 * 
	 * @param archive
	 * @param extractDir
	 * @param keepNested
	 *            Extract nested tar.gz files as usual, but do not attempt to
	 *            delete the nested tar.gz files after extraction. This was
	 *            added because MS Windows machines do not always have
	 *            permission to delete these files, causing failed regression
	 *            tests for no good reason. Default call is and should be
	 *            'false'.
	 * @return
	 * @throws InvalidHeaderException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private static File tarExtract(File archive, File extractDir, boolean nested, List<File> markedDeleted)
			throws InvalidHeaderException, IOException, InterruptedException
	{
		InputStream inStream = System.in;

		try
		{
			inStream = new FileInputStream(archive);
		}
		catch (IOException ex)
		{
			inStream = null;
			ex.printStackTrace(System.err);
		}
		return tarExtract(inStream, extractDir, nested, markedDeleted);

	}

	/**
	 * 
	 * @param inStream
	 *            = the inputstream from a file
	 * @param extractDir
	 * @param keepNested
	 *            Extract nested tar.gz files as usual, but do not attempt to
	 *            delete the nested tar.gz files after extraction. This was
	 *            added because MS Windows machines do not always have
	 *            permission to delete these files, causing failed regression
	 *            tests for no good reason. Default call is and should be
	 *            'false'.
	 * @return
	 * @throws InvalidHeaderException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private static File tarExtract(InputStream inStream, File extractDir, boolean nested, List<File> markedDeleted)
			throws InvalidHeaderException, IOException, InterruptedException
	// private static File tarExtract(File archive, File extractDir, boolean
	// nested) throws InvalidHeaderException, IOException
	{
		if (!extractDir.exists())
		{
			extractDir.mkdir();
		}
		else
		{
			FileUtils.cleanDirectory(extractDir);
		}

		TarArchive tarchive = null;

		if (inStream != null)
		{
			try
			{
				inStream = new GZIPInputStream(inStream);
			}
			catch (IOException ex)
			{
				inStream = null;
				ex.printStackTrace(System.err);
			}

			tarchive = new TarArchive(inStream, TarBuffer.DEFAULT_BLKSIZE);
		}

		tarchive.extractContents(extractDir);

		for (File tarGzFile : findTarGzFiles(extractDir))
		{
			File subDir = new File(extractDir.getAbsolutePath() + File.separator
					+ tarGzFile.getName().replace(".tar.gz", ""));
			markedDeleted.add(tarGzFile);
			tarExtract(tarGzFile, subDir, true, markedDeleted);
		}

		// When you try to delete it straight away, MS Windows will not delete
		// the file. Now it will.
		if (!nested)
		{
			for (File f : markedDeleted)
			{
				boolean delSuccess = f.delete();
				if (!delSuccess)
				{
					Thread.sleep(10);
					delSuccess = f.delete();
					if (!delSuccess)
					{
						System.out.println("WARNING could not delete " + f.getAbsolutePath());
					}
				}
			}
		}
		return extractDir;
	}

	/**
	 * Wrapper for private static File tarExtract(File archive, File extractDir,
	 * boolean nested, List<File> markedDeleted). Adds nested = false and empty
	 * file list used ONLY in recursion to avoid MS Windows not deleting the
	 * appropriate files.
	 * 
	 * @param archive
	 * @param extractDir
	 * @return
	 * @throws InvalidHeaderException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static File tarExtract(File archive, File extractDir) throws InvalidHeaderException, IOException,
			InterruptedException
	{
		return tarExtract(archive, extractDir, false, new ArrayList<File>());
	}

	/**
	 * Wrapper for private static File tarExtract(InputStream inStream, File
	 * extractDir, boolean nested, List<File> markedDeleted). Adds nested =
	 * false and empty file list used ONLY in recursion to avoid MS Windows not
	 * deleting the appropriate files.
	 * 
	 * @param inStream
	 *            = the inputstream from a file
	 * @param extractDir
	 * @return
	 * @throws InvalidHeaderException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static File tarExtract(InputStream inStream, File extractDir) throws InvalidHeaderException, IOException,
			InterruptedException
	{
		return tarExtract(inStream, extractDir, false, new ArrayList<File>());
	}

	/**
	 * Wrapper for public static File tarExtract(File archive, File extractDir).
	 * Adds default extractDir by getting the java tmp dir and archive name.
	 * 
	 * @param archive
	 * @param keepNested
	 *            Extract nested tar.gz files as usual, but do not attempt to
	 *            delete the nested tar.gz files after extraction. This was
	 *            added because MS Windows machines do not always have
	 *            permission to delete these files, causing failed regression
	 *            tests for no good reason. Default call is and should be
	 *            'false'.
	 * @return
	 * @throws InvalidHeaderException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static File tarExtract(File archive) throws InvalidHeaderException, IOException, InterruptedException
	{
		String archiveName = archive.getName().substring(0, archive.getName().indexOf("."));
		File extractDir = new File(System.getProperty("java.io.tmpdir") + File.separator + archiveName + "_extract");
		return tarExtract(archive, extractDir);
	}

	/**
	 * Wrapper for public static File tarExtract(File archive, File extractDir).
	 * Adds default extractDir by getting the java tmp dir and archive name.
	 * 
	 * @param inStream
	 *            = the inputstream from a file
	 * @param keepNested
	 *            Extract nested tar.gz files as usual, but do not attempt to
	 *            delete the nested tar.gz files after extraction. This was
	 *            added because MS Windows machines do not always have
	 *            permission to delete these files, causing failed regression
	 *            tests for no good reason. Default call is and should be
	 *            'false'.
	 * @return
	 * @throws InvalidHeaderException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static File tarExtract(InputStream inStream) throws InvalidHeaderException, IOException,
			InterruptedException
	{
		String archiveName = "inputstream_" + System.nanoTime();
		File extractDir = new File(System.getProperty("java.io.tmpdir") + File.separator + archiveName + "_extract");
		return tarExtract(inStream, extractDir);
	}

	private static List<File> findTarGzFiles(File dir)
	{
		ArrayList<File> tars = new ArrayList<File>();
		File[] files = dir.listFiles();
		for (File file : files)
		{
			// all dirs should be tarred at this point, but still check
			if (!file.isDirectory())
			{
				// if file is a .tar.gz, add as such
				if (file.getName().endsWith(".tar.gz"))
				{
					tars.add(file);
				}
			}

		}
		return tars;
	}

	/**
	 * Based on: http://www.javalobby.org/java/forums/t17036.html Copy source
	 * file to destination. If destination is a path then source file name is
	 * appended. If destination file exists then: overwrite=true - destination
	 * file is replaced; overwite=false - exception is thrown.
	 * 
	 * @param src
	 *            source file
	 * @param dst
	 *            destination file or path
	 * @param overwrite
	 *            overwrite destination file
	 * @exception IOException
	 *                I/O problem
	 * @exception IllegalArgumentException
	 *                illegal argument
	 */

	public static void fileCopy(final File src, File dst, final boolean overwrite) throws IOException,
			IllegalArgumentException
	{
		// long startTimer = System.currentTimeMillis();

		// checks
		if (!src.isFile() || !src.exists())
		{
			throw new IllegalArgumentException("Source file '" + src.getAbsolutePath() + "' not found.");
		}

		if (dst.exists())
		{
			if (dst.isDirectory())
			{ // Directory? -> use source file name
				dst = new File(dst, src.getName());
			}
			else if (dst.isFile())
			{
				if (!overwrite)
				{
					throw new IllegalArgumentException("Destination file '" + dst.getAbsolutePath()
							+ "' already exists.");
				}
			}
			else
			{
				throw new IllegalArgumentException("Invalid destination object '" + dst.getAbsolutePath() + "'.");
			}
		}

		File dstParent = dst.getParentFile();

		if (!dstParent.exists())
		{
			if (!dstParent.mkdirs())
			{
				throw new IOException("Failed to create directory " + dstParent.getAbsolutePath());
			}
		}

		long fileSize = src.length();

		if (fileSize > 20971520l)
		{ // for larger files (20Mb) use streams
			FileInputStream in = new FileInputStream(src);
			FileOutputStream out = new FileOutputStream(dst);

			try
			{
				int doneCnt = -1;
				int bufSize = 32768;
				byte buf[] = new byte[bufSize];

				while ((doneCnt = in.read(buf, 0, bufSize)) >= 0)
				{
					if (doneCnt == 0)
					{
						Thread.yield();
					}
					else
					{
						out.write(buf, 0, doneCnt);
					}
				}
				out.flush();
			}

			finally
			{
				in.close();
				out.close();
			}

		}

		else
		{ // smaller files, use channels

			FileInputStream fis = new FileInputStream(src);
			FileOutputStream fos = new FileOutputStream(dst);
			FileChannel in = fis.getChannel();
			FileChannel out = fos.getChannel();

			try
			{
				long offs = 0, doneCnt = 0, copyCnt = Math.min(65536, fileSize);
				do
				{
					doneCnt = in.transferTo(offs, copyCnt, out);
					offs += doneCnt;
					fileSize -= doneCnt;
				}
				while (fileSize > 0);
			}

			finally
			{ // cleanup
				in.close();
				out.close();
				fis.close();
				fos.close();
			}
		}

		// System.out.println("filecopied " + String.valueOf(src.length() /
		// 1024) + " Kb in " + String.valueOf(System.currentTimeMillis() -
		// startTimer));

	}

}