package org.molgenis.ngs.plugins.worksheet;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.ui.PluginModel;
import org.molgenis.framework.ui.ScreenController;
//import org.molgenis.framework.ui.html.TupleTable;
import org.molgenis.util.Entity;
import org.molgenis.util.tuple.Tuple;

import app.JpaDatabase;

public class Worksheet extends PluginModel<Entity>
{
	private static final long serialVersionUID = 1L;

	List<Tuple> currentRows = new ArrayList<Tuple>();

	public Worksheet(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	@Override
	public String getCustomHtmlHeaders()
	{
		return "<link rel=\"stylesheet\" href=\"js/jquery-plugins/jquery.dataTables.css\" type=\"text/css\"/><link rel=\"stylesheet\" href=\"js/jquery-plugins/jquery.dataTables_demo_page.css\" type=\"text/css\"/><script type=\"text/javascript\" language=\"javascript\" src=\"js/jquery-plugins/jquery.dataTables.js\"></script>";
	}

	@Override
	public String getViewName()
	{
		return "plugins_worksheet_Worksheet";
	}

	@Override
	public String getViewTemplate()
	{
		return "templates/" + Worksheet.class.getName().replace('.', '/') + ".ftl";
	}

	@Override
	public void handleRequest(Database db, MolgenisRequest request) throws DatabaseException
	{
	}

	@Override
	public void reload(Database db)
	{
		// currentRows = new ArrayList<Tuple>();
		//
		// WritableTuple row = new KeyValueTuple();
		// row.set("project", "p1");
		// currentRows.add(row);

		try
		{
			this.getMessages().clear();
			// db.getMetaData().getEntity("Project").getFields().get(0).getName();
			// currentRows = ((JpaDatabase)
			// db).sql("select ProjectName from project", "ProjectName");
			currentRows = ((JpaDatabase) db)
					.sql("SELECT sample.InternalId AS internalSampleID, sample.ExternalId AS externalSampleID, project.ProjectName AS project, CONCAT(nue.UserName, ' <' , nue.UserEmail, '>') AS contact, machine.MachineName AS sequencer, sample.LabStatus AS labStatusPhase, sample.SampleComment AS labStatusComments, DATE_FORMAT(flowcell.RunDate, GET_FORMAT(DATE,'ISO')) AS sequencingStartDate, flowcell.Run AS run, flowcell.FlowcellName AS flowcell, flowcelllanesamplebarcode.Lane AS lane, project.SeqType AS seqType, prepkit.PrepKitName AS prepKit, capturingkit.CapturingKitName AS capturingKit, sample.ArrayFile AS arrayFile, sample.ArrayId AS arrayID, nub.UserName AS GAF_QC_Name, DATE_FORMAT(sample.QcWetDate, GET_FORMAT(DATE,'ISO')) AS GAF_QC_Date, DATE_FORMAT(sample.QcWetMet, GET_FORMAT(DATE,'ISO')) AS GAF_QC_Status, nuc.UserName AS GCC_QC_Name, project.GccAnalysis AS GCC_Analysis, sample.QcDryDate AS GCC_QC_Date, sample.QcDryMet AS GCC_QC_Status, DATE_FORMAT(project.ProjectPlannedFinishDate, GET_FORMAT(DATE,'ISO')) AS TargetDateShipment, DATE_FORMAT(project.ResultShippedDate, GET_FORMAT(DATE,'ISO')) AS DataShippedDate, nud.UserName AS DataShippedTo, nua.UserName AS DataShippedBy, flowcelllanesamplebarcode.FlowcellLaneSampleBarcodeComment AS Comments, samplebarcode.SampleBarcodeName AS barcode, samplebarcode.BarcodeType AS barcodeType FROM flowcelllanesamplebarcode LEFT JOIN flowcell ON flowcelllanesamplebarcode.Flowcell = flowcell.id LEFT JOIN machine ON flowcell.Machine = machine.id LEFT JOIN samplebarcode ON flowcelllanesamplebarcode.SampleBarcode = samplebarcode.id LEFT JOIN capturingkit ON flowcelllanesamplebarcode.CapturingKit = capturingkit.id LEFT JOIN sample ON flowcelllanesamplebarcode.Sample = sample.id LEFT JOIN prepkit ON sample.PrepKit = prepkit.id LEFT JOIN ngsuser AS nub ON sample.QcWetUser = nub.id LEFT JOIN ngsuser AS nuc ON sample.QcDryUser = nuc.id LEFT JOIN project ON sample.ProjectId = project.id LEFT JOIN ngsuser AS nua ON project.ResultShippedUser = nua.id LEFT JOIN ngsuser AS nud ON project.ResultShippedTo = nud.id LEFT JOIN ngsuser AS nue ON project.ProjectCustomer = nue.id ORDER BY ABS(sample.InternalId) ASC;",
							"internalSampleID", "externalSampleID", "project", "contact", "sequencer",
							"labStatusPhase", "labStatusComments", "sequencingStartDate", "run", "flowcell", "lane",
							"seqType", "prepKit", "capturingKit", "arrayFile", "arrayID", "GAF_QC_Name", "GAF_QC_Date",
							"GAF_QC_Status", "GCC_Analysis", "GCC_QC_Name", "GCC_QC_Date", "GCC_QC_Status",
							"TargetDateShipment", "DataShippedDate", "DataShippedTo", "DataShippedBy", "Comments",
							"barcode", "barcodeType");
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.setError(e.getMessage());
		}

	}

	@Override
	public boolean isVisible()
	{
		return true;
	}

	public WorksheetModel getMyModel()
	{
		return new WorksheetModel(this.currentRows);
	}
}
