package org.molgenis.util.trityper.reader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * 
 * @author harmjan
 */
public class TextFile
{
	public static final Pattern tab = Pattern.compile("\t");
	public static final Pattern space = Pattern.compile(" ");
	protected BufferedReader in;

	protected String loc;

	public static final boolean W = true;
	public static final boolean R = false;

	protected BufferedWriter out;
	protected boolean writeable;

	protected static final String ENCODING = "ISO-8859-1";

	public TextFile(String loc, boolean mode) throws IOException
	{
		this.writeable = mode;
		this.loc = loc;
		open();
	}

	public void open() throws IOException
	{
		File locHandle = new File(loc);
		if (!locHandle.exists() && !writeable)
		{
			System.out.println("Could not find file: " + loc);
			System.exit(0);
		}
		else
		{
			if (writeable)
			{
				out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(locHandle), ENCODING));
			}
			else
			{
				in = new BufferedReader(new InputStreamReader(new FileInputStream(locHandle), ENCODING), 8096);
			}
		}
	}

	public String readLine() throws IOException
	{
		String ln = in.readLine();
		if (ln != null)
		{
			return ln;
		}
		else
		{
			return null;
		}
	}

	public void write(String line) throws IOException
	{
		out.write(line);
	}

	public void close() throws IOException
	{
		if (writeable)
		{
			out.close();
		}
		else
		{
			in.close();
		}
	}

	public String[] readLineElems(Pattern p) throws IOException
	{
		if (in != null)
		{
			String ln = readLine();
			if (ln != null)
			{
				return p.split(ln);
			}
			else
			{
				return null;
			}
		}
		else
		{
			return null;
		}

	}

	public int countLines() throws IOException
	{
		String ln = readLine();
		int ct = 0;
		while (ln != null)
		{
			if (ln.trim().length() > 0)
			{
				ct++;
			}
			ln = readLine();
		}
		close();
		open();
		return ct;
	}

	public int countCols(Pattern p) throws IOException
	{
		String ln = readLine();
		int ct = 0;
		if (ln != null)
		{
			String[] elems = p.split(ln);
			ct = elems.length;
		}
		close();
		open();
		return ct;
	}

	public String[] readAsArray() throws IOException
	{
		int numLines = countLines();
		String ln = readLine();
		String[] data = new String[numLines];
		int i = 0;
		while (ln != null)
		{
			if (ln.trim().length() > 0)
			{
				data[i] = ln;
				i++;
			}
			ln = in.readLine();
		}
		return data;
	}

	public ArrayList<String> readAsArrayList() throws IOException
	{
		String ln = readLine();
		ArrayList<String> data = new ArrayList<String>();
		while (ln != null)
		{
			if (ln.trim().length() > 0)
			{
				data.add(ln);
			}
			ln = in.readLine();
		}
		return data;
	}
}
