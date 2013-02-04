package org.molgenis.omx.dataset;

import org.testng.annotations.Test;

public class DataSetViewerPluginTest
{
	/**
	 * Check if we have selected ObservableFeatures in the HttpSession the
	 * columns of the tupletable are shown or hidden correctly
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSelectedObservableFeatures() throws Exception
	{
		// TODO
		// DataSetViewerPlugin plugin = new DataSetViewerPlugin("DataSet",
		// null);
		//
		// TupleTable table = mock(TupleTable.class);
		//
		// // Add the columns of the tupletable
		// List<Field> columns = new ArrayList<Field>();
		// columns.add(new Field("1"));
		// columns.add(new Field("2"));
		// columns.add(new Field("3"));
		// when(table.getAllColumns()).thenReturn(columns);
		//
		// MockHttpServletRequest httpServletRequest = new
		// MockHttpServletRequest();
		// MolgenisRequest request = new MolgenisRequest(httpServletRequest);
		//
		// // Add the selected observablefeatures to the HttpSession
		// List<ObservableFeature> selectedObservableFeatures = new
		// ArrayList<ObservableFeature>();
		// ObservableFeature of1 = new ObservableFeature();
		// of1.setIdentifier("1");
		// selectedObservableFeatures.add(of1);
		// ObservableFeature of2 = new ObservableFeature();
		// of2.setIdentifier("2");
		// selectedObservableFeatures.add(of2);
		//
		// httpServletRequest.getSession().setAttribute("selectedObservableFeatures",
		// selectedObservableFeatures);
		//
		// // Call the plugin, this would normally be called by the JQGridView
		// // plugin.beforeLoadConfig(request, table);
		//
		// // Check if the hide and show column methods of the tupletable were
		// // called
		// Mockito.verify(table).showColumn("1");
		// Mockito.verify(table).showColumn("2");
		// Mockito.verify(table).hideColumn("3");
	}
}
