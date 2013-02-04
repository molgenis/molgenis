package org.molgenis.framework.tupletable.view.renderers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Arrays;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.tupletable.TableException;
import org.molgenis.framework.tupletable.TupleTable;
import org.molgenis.framework.tupletable.view.JQGridView;
import org.molgenis.framework.tupletable.view.JQGridJSObjects.JQGridResult;
import org.molgenis.framework.ui.html.HtmlWidget;
import org.molgenis.util.ZipUtils;
import org.molgenis.util.ZipUtils.DirectoryStructure;

import com.google.gson.Gson;

/**
 * Class containing a series of simple renderers to do the administrative
 * busywork required to render from a particular {@link TupleTable} to a
 * particular view (current options are an {@link AbstractExporter} or a
 * {@link HtmlWidget}. See the org.molgenis.modules.datatable.view package.
 */
public class Renderers
{

	public static class HeaderHelper
	{
		public static void setHeader(HttpServletResponse response, String contentType, String fileName)
		{
			response.setContentType(contentType);
			response.addHeader("Content-Disposition", "attachment; filename=" + fileName);
		}
	}

	/**
	 * Interface to render from a Table/request combination to a particular
	 * view. Current implementations are trivial except {@link SPSSRenderer}.
	 */
	public interface Renderer
	{
		public void export(MolgenisRequest request, String datasetName, TupleTable tupleTable, int totalPages, int page)
				throws TableException, IOException;
	}

	public static class JQGridRenderer implements Renderer
	{
		@Override
		public void export(MolgenisRequest request, String fileName, TupleTable tupleTable, int totalPages,
				int currentPage) throws TableException, IOException
		{
			final JQGridResult result = JQGridView.buildJQGridResults(tupleTable.getCount(), totalPages, currentPage,
					tupleTable);

			Writer writer = new OutputStreamWriter(request.getResponse().getOutputStream(), Charset.forName("UTF-8"));
			try
			{
				new Gson().toJson(result, writer);
			}
			finally
			{
				writer.close();
			}
		}
	}

	public static class ExcelRenderer implements Renderer
	{
		@Override
		public void export(MolgenisRequest request, String fileName, TupleTable tupleTable, int totalPages,
				int currentPage) throws TableException, IOException
		{
			HeaderHelper.setHeader(request.getResponse(), "application/ms-excel", fileName + ".xlsx");
			final ExcelExporter excelExport = new ExcelExporter(tupleTable);
			excelExport.export(request.getResponse().getOutputStream());
		}
	}

	public static class CSVRenderer implements Renderer
	{
		@Override
		public void export(MolgenisRequest request, String fileName, TupleTable tupleTable, int totalPages,
				int currentPage) throws TableException, IOException
		{
			HeaderHelper.setHeader(request.getResponse(), "application/ms-excel", fileName + ".csv");
			final CsvExporter csvExporter = new CsvExporter(tupleTable);
			csvExporter.export(request.getResponse().getOutputStream());
		}
	}

	/**
	 * Several things need to happen to export to SPSS:
	 * <ul>
	 * <li>Several files need to be created in the temp dir:
	 * <li>One textfile encoding the actual data, for example as tab-separated
	 * values.</li>
	 * <li>One .sps syntax script that will set the variables and labels in SPSS
	 * and load the data.</li>
	 * <li>One textfile with instructions on how to use the script in SPSS.</li>
	 * <li>These files should be compressed together into a .zip file for
	 * download.</li>
	 * </ul>
	 */
	public static class SPSSRenderer implements Renderer
	{
		@Override
		public void export(MolgenisRequest request, String fileName, TupleTable tupleTable, int totalPages,
				int currentPage) throws TableException, IOException
		{
			try
			{
				final File tempDir = new File(System.getProperty("java.io.tmpdir"));
				final File spssFile = File.createTempFile("spssExport", ".sps", tempDir);
				final File spssCsvFile = File.createTempFile("csvSpssExport", ".csv", tempDir);
				// TODO: instruction .txt file.
				final File zipExport = File.createTempFile("spssExport", ".zip", tempDir);

				final FileOutputStream spssFileStream = new FileOutputStream(spssFile);
				final FileOutputStream spssCsvFileStream = new FileOutputStream(spssCsvFile);
				final SPSSExporter spssExporter = new SPSSExporter(tupleTable);
				spssExporter.export(spssCsvFileStream, spssFileStream, spssCsvFile.getName());

				spssCsvFileStream.close();
				spssFileStream.close();
				ZipUtils.compress(Arrays.asList(spssFile, spssCsvFile), zipExport, DirectoryStructure.EXCLUDE_DIR);
				HeaderHelper.setHeader(request.getResponse(), "application/octet-stream", fileName + ".zip");
				exportFile(zipExport, request.getResponse());
			}
			catch (Exception e)
			{
				throw new TableException(e);
			}
		}

		private void exportFile(File file, HttpServletResponse response) throws IOException
		{
			FileInputStream fileIn = new FileInputStream(file);
			ServletOutputStream out = response.getOutputStream();

			byte[] outputByte = new byte[4096];
			// copy binary content to output stream
			while (fileIn.read(outputByte, 0, 4096) != -1)
			{
				out.write(outputByte, 0, 4096);
			}
			fileIn.close();
			out.flush();
			out.close();
		}
	}
}