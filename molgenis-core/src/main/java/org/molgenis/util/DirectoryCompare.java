package org.molgenis.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;

public class DirectoryCompare
{
	public static boolean compareDirs(File dir1, File dir2) throws Exception
	{
		System.out.println("Comparing '" + dir1.getName() + "' vs '" + dir2.getName() + "'..");
		File[] dir1files = dir1.listFiles();
		File[] dir2files = dir2.listFiles();

		// Test 1: Check if both directories contain an equal number of files
		if (dir1files.length != dir2files.length)
		{
			System.out.println("--> Directories contain an unequal amount of files.");
			return false;
		}

		// Test 2: Check if all filenames of dir 1 are present in dir 2
		for (File fileInDir1 : dir1files)
		{
			boolean fileNameFoundInDir2 = false;
			for (File fileInDir2 : dir2files)
			{
				if (fileInDir2.getName().equals(fileInDir1.getName()))
				{
					fileNameFoundInDir2 = true;
				}
			}
			if (!fileNameFoundInDir2)
			{
				System.out.println("--> Filename '" + fileInDir1.getName() + "' was not found in directory 2.");
				return false;
			}
		}

		// Test 3: Check if all filenames of dir 2 are present in dir 1
		for (File fileInDir2 : dir2files)
		{
			boolean fileNameFoundInDir1 = false;
			for (File fileInDir1 : dir1files)
			{
				if (fileInDir1.getName().equals(fileInDir2.getName()))
				{
					fileNameFoundInDir1 = true;
				}
			}
			if (!fileNameFoundInDir1)
			{
				System.out.println("--> Filename '" + fileInDir2.getName() + "' was not found in directory 1.");
				return false;
			}
		}

		// Test 4: Directories contain the same filenames, so we now read the
		// files and compare their content. If we encounter a directory, we get
		// the equivalent from the other dir and call the function recursively.
		for (File fileInDir1 : dir1files)
		{
			for (File fileInDir2 : dir2files)
			{
				if (fileInDir1.getName().equals(fileInDir2.getName()))
				{
					if (fileInDir1.isDirectory())
					{
						compareDirs(fileInDir1, fileInDir2);
					}
					else
					{
						boolean fileContentIsEqual = compareFileContent(fileInDir1, fileInDir2);
						if (!fileContentIsEqual)
						{
							System.out.println("--> Directory file contents are unequal.");
							return false;
						}
					}
				}
			}
		}

		System.out.println("--> Directories are equal.");
		return true;
	}

	public static boolean compareFileContent(File fileInDir1, File fileInDir2) throws Exception
	{
		boolean filesAreEqual = true;
		;
		BufferedReader input1 = new BufferedReader(new InputStreamReader(new FileInputStream(fileInDir1),
				Charset.forName("UTF-8")));
		BufferedReader input2 = new BufferedReader(new InputStreamReader(new FileInputStream(fileInDir2),
				Charset.forName("UTF-8")));
		try
		{
			String line1 = null;
			String line2 = null;

			int input1_count = 0;
			int input2_count = 0;

			while ((line1 = input1.readLine()) != null)
			// while ((line1 = input1.readLine()) != null && filesAreEqual)
			{
				while ((line2 = input2.readLine()) != null)
				// while ((line2 = input2.readLine()) != null && filesAreEqual)
				{
					if (!line1.equals(line2))
					{
						System.out.println("Difference in files detected:");
						System.out.println(" * File 1: " + fileInDir1.getName());
						System.out.println(" * File 2: " + fileInDir2.getName());
						System.out.println(" * Line in file 1: " + line1);
						System.out.println(" * Line in file 2: " + line2);
						filesAreEqual = false;
					}
					input2_count++;
					break;
				}
				input1_count++;
			}

			if (input1_count > input2_count)
			{
				System.out.println("Difference in files detected:");
				System.out.println("File 1 contains more lines (" + input1_count + ") than file 2 (" + input2_count
						+ "), files not equal");
				filesAreEqual = false;
			}
			if (input2_count > input1_count)
			{
				System.out.println("Difference in files detected:");
				System.out.println("File 2 contains more lines (" + input2_count + ")than file 1 (" + input1_count
						+ "), files not equal");
				filesAreEqual = false;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw e;
		}
		finally
		{
			IOUtils.closeQuietly(input1);
			IOUtils.closeQuietly(input2);
		}

		return filesAreEqual;
	}
}
