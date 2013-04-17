package org.molgenis.ngs.plugins.worksheet;

import java.util.ArrayList;
import java.util.List;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.ui.PluginModel;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.framework.ui.ScreenView;
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
		return "<link rel=\"stylesheet\" href=\"js/jquery-plugins/jquery.dataTables.css\" type=\"text/css\"/><link rel=\"stylesheet\" href=\"css/TableTools.css\" type=\"text/css\"/><link rel=\"stylesheet\" href=\"js/jquery-plugins/jquery.dataTables_demo_page.css\" type=\"text/css\"/><script type=\"text/javascript\" src=\"js/jquery-plugins/jquery.dataTables.js\"></script><script type=\"text/javascript\" src=\"js/TableTools.min.js\"></script><script type=\"text/javascript\" charset=\"utf-8\" src=\"js/ZeroClipboard.js\"></script>";
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
		try
		{
			this.getMessages().clear();
			currentRows = ((JpaDatabase) db).sql("SELECT " + "sample.InternalId AS internalSampleID, "
					+ "sample.ExternalId AS externalSampleID, " + "project.ProjectName AS project, "
					+ "CONCAT(nue.UserName, ' <' , nue.UserEmail, '>') AS contact, "
					+ "machine.MachineName AS sequencer, " + "sample.LabStatus AS labStatusPhase, "
					+ "sample.SampleComment AS labStatusComments, "
					+ "DATE_FORMAT(flowcell.RunDate, '%y%m%d') AS sequencingStartDate, " + "flowcell.Run AS run, "
					+ "flowcell.FlowcellName AS flowcell, " + "flowcellLane.Lane AS lane, "
					+ "project.SeqType AS seqType, " + "prepkit.PrepKitName AS prepKit, "
					+ "capturingkit.CapturingKitName AS capturingKit, " + "sample.ArrayFile AS arrayFile, "
					+ "sample.ArrayId AS arrayID, " + "nub.UserName AS GAF_QC_Name, "
					+ "DATE_FORMAT(flowcellLane.QcWetDate, GET_FORMAT(DATE,'ISO')) AS GAF_QC_Date, "
					+ "DATE_FORMAT(flowcellLane.QcWetMet, GET_FORMAT(DATE,'ISO')) AS GAF_QC_Status, "
					+ "nuc.UserName AS GCC_QC_Name, " + "project.GccAnalysis AS GCC_Analysis, "
					+ "flowcellLane.QcDryDate AS GCC_QC_Date, " + "flowcellLane.QcDryMet AS GCC_QC_Status, "
					+ "DATE_FORMAT(project.ProjectPlannedFinishDate, GET_FORMAT(DATE,'ISO')) AS TargetDateShipment, "
					+ "DATE_FORMAT(project.ResultShippedDate, GET_FORMAT(DATE,'ISO')) AS DataShippedDate, "
					+ "nud.UserName AS DataShippedTo, " + "nua.UserName AS DataShippedBy, "
					+ "flowcellLane.FlowcellLaneComment AS Comments, " + "samplebarcode.SampleBarcodeName AS barcode, "
					+ "samplebarcode.BarcodeType AS barcodeType " + "FROM " + "flowcellLane "
					+ "LEFT JOIN flowcell ON flowcellLane.Flowcell = flowcell.id "
					+ "LEFT JOIN machine ON flowcell.Machine = machine.id "
					+ "LEFT JOIN sample AS sample ON flowcellLane.Sample = sample.id "
					+ "LEFT JOIN sample_sampleinpool ON sample.id = sample_sampleinpool.Sample "
					+ "LEFT JOIN sample AS pool ON sample_sampleinpool.Sample = pool.id "
					+ "LEFT JOIN sample AS sampleinpool ON sample_sampleinpool.SampleInPool = sampleinpool.id "
					+ "LEFT JOIN capturingkit ON sample.CapturingKit = capturingkit.id "
					+ "LEFT JOIN samplebarcode ON sample.SampleBarcode = samplebarcode.id "
					+ "LEFT JOIN ngsuser AS nub ON flowcellLane.QcWetUser = nub.id "
					+ "LEFT JOIN ngsuser AS nuc ON flowcellLane.QcDryUser = nuc.id "
					+ "LEFT JOIN project ON sample.ProjectId = project.id "
					+ "LEFT JOIN prepkit ON project.PrepKit = prepkit.id "
					+ "LEFT JOIN ngsuser AS nua ON project.ResultShippedUser = nua.id "
					+ "LEFT JOIN ngsuser AS nud ON project.ResultShippedTo = nud.id "
					+ "LEFT JOIN ngsuser AS nue ON project.ProjectCustomer = nue.id "
					+ "ORDER BY ABS(sample.InternalId) ASC;", "internalSampleID", "externalSampleID", "project",
					"contact", "sequencer", "labStatusPhase", "labStatusComments", "sequencingStartDate", "run",
					"flowcell", "lane", "seqType", "prepKit", "capturingKit", "arrayFile", "arrayID", "GAF_QC_Name",
					"GAF_QC_Date", "GAF_QC_Status", "GCC_Analysis", "GCC_QC_Name", "GCC_QC_Date", "GCC_QC_Status",
					"TargetDateShipment", "DataShippedDate", "DataShippedTo", "DataShippedBy", "Comments", "barcode",
					"barcodeType");
		}
		catch (Exception e)
		{
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

	@Override
	public ScreenView getView()
	{
		return null;
	}
}
