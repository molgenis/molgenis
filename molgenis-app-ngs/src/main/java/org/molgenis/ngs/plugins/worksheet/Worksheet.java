package org.molgenis.ngs.plugins.worksheet;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.jpa.JpaDatabase;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.ui.PluginModel;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.framework.ui.ScreenView;
import org.molgenis.util.Entity;
import org.molgenis.util.tuple.Tuple;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;

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
			String worksheetQuery = "SELECT * FROM (SELECT \"\" AS poolInternalId, Sample.InternalId AS internalSampleID, Sample.ExternalId AS externalSampleID, Project.ProjectName AS project, CONCAT(nue.UserName, ' <' , nue.UserEmail, '>') AS contact, Machine.MachineName AS sequencer, Sample.LabStatus AS labStatusPhase, Sample.SampleComment AS labStatusComments, DATE_FORMAT(Flowcell.RunDate, '%y%m%d') AS sequencingStartDate, Flowcell.Run AS run, CONCAT(Flowcell.FlowcellDirection,Flowcell.FlowcellName) AS flowcell, FlowcellLane.Lane AS lane, Project.SeqType AS seqType, PrepKit.PrepKitName AS prepKit, CapturingKit.CapturingKitName AS capturingKit, Sample.ArrayFile AS arrayFile, Sample.ArrayId AS arrayID, nub.UserName AS GAF_QC_Name, DATE_FORMAT(FlowcellLane.QcWetDate, GET_FORMAT(DATE,'ISO')) AS GAF_QC_Date, DATE_FORMAT(FlowcellLane.QcWetMet, GET_FORMAT(DATE,'ISO')) AS GAF_QC_Status, nuc.UserName AS GCC_QC_Name, Project.GccAnalysis AS GCC_Analysis, FlowcellLane.QcDryDate AS GCC_QC_Date, FlowcellLane.QcDryMet AS GCC_QC_Status, DATE_FORMAT(Project.ProjectPlannedFinishDate, GET_FORMAT(DATE,'ISO')) AS TargetDateShipment, DATE_FORMAT(Project.ResultShippedDate, GET_FORMAT(DATE,'ISO')) AS DataShippedDate, nud.UserName AS DataShippedTo, nua.UserName AS DataShippedBy, FlowcellLane.FlowcellLaneComment AS Comments, SampleBarcode.SampleBarcodeSequence AS barcode, SampleBarcodeType.SampleBarcodeTypeName AS barcodeType FROM FlowcellLane LEFT JOIN Flowcell ON FlowcellLane.Flowcell = Flowcell.id LEFT JOIN Machine ON Flowcell.Machine = Machine.id LEFT JOIN Sample ON FlowcellLane.Sample = Sample.id LEFT JOIN Sample_SampleInPool ON Sample.id = Sample_SampleInPool.SampleInPool LEFT JOIN CapturingKit ON Sample.CapturingKit = CapturingKit.id LEFT JOIN SampleBarcode ON Sample.SampleBarcode = SampleBarcode.id LEFT JOIN SampleBarcodeType ON SampleBarcode.SampleBarcodeType = SampleBarcodeType.id LEFT JOIN NgsUser AS nub ON FlowcellLane.QcWetUser = nub.id LEFT JOIN NgsUser AS nuc ON FlowcellLane.QcDryUser = nuc.id LEFT JOIN Project ON Sample.ProjectId = Project.id LEFT JOIN PrepKit ON Project.PrepKit = PrepKit.id LEFT JOIN NgsUser AS nua ON Project.ResultShippedUser = nua.id LEFT JOIN NgsUser AS nud ON Project.ResultShippedTo = nud.id LEFT JOIN NgsUser AS nue ON Project.ProjectCustomer = nue.id WHERE Sample.Id not in (SELECT SampleInPool FROM Sample_SampleInPool) AND Sample.Id not in (SELECT Sample FROM Sample_SampleInPool)) a UNION (SELECT p.InternalId AS poolInternalId, s.InternalId AS internalSampleID, s.ExternalId AS externalSampleID, Project.ProjectName AS project, CONCAT(nue.UserName, ' <' , nue.UserEmail, '>') AS contact, Machine.MachineName AS sequencer, s.LabStatus AS labStatusPhase, s.SampleComment AS labStatusComments, DATE_FORMAT(Flowcell.RunDate, '%y%m%d') AS sequencingStartDate, Flowcell.Run AS run, CONCAT(Flowcell.FlowcellDirection,Flowcell.FlowcellName) AS flowcell, FlowcellLane.Lane AS lane, Project.SeqType AS seqType, PrepKit.PrepKitName AS prepKit, CapturingKit.CapturingKitName AS capturingKit, s.ArrayFile AS arrayFile, s.ArrayId AS arrayID, nub.UserName AS GAF_QC_Name, DATE_FORMAT(FlowcellLane.QcWetDate, GET_FORMAT(DATE,'ISO')) AS GAF_QC_Date, DATE_FORMAT(FlowcellLane.QcWetMet, GET_FORMAT(DATE,'ISO')) AS GAF_QC_Status, nuc.UserName AS GCC_QC_Name, Project.GccAnalysis AS GCC_Analysis, FlowcellLane.QcDryDate AS GCC_QC_Date, FlowcellLane.QcDryMet AS GCC_QC_Status, DATE_FORMAT(Project.ProjectPlannedFinishDate, GET_FORMAT(DATE,'ISO')) AS TargetDateShipment, DATE_FORMAT(Project.ResultShippedDate, GET_FORMAT(DATE,'ISO')) AS DataShippedDate, nud.UserName AS DataShippedTo, nua.UserName AS DataShippedBy, FlowcellLane.FlowcellLaneComment AS Comments, SampleBarcode.SampleBarcodeSequence AS barcode, SampleBarcodeType.SampleBarcodeTypeName AS barcodeType FROM FlowcellLane LEFT JOIN Flowcell ON FlowcellLane.Flowcell = Flowcell.id LEFT JOIN Sample AS p ON FlowcellLane.Sample = p.id LEFT JOIN Machine ON Flowcell.Machine = Machine.id LEFT JOIN Sample_SampleInPool link ON p.id = link.Sample LEFT JOIN Sample AS s ON link.SampleInPool = s.id LEFT JOIN CapturingKit ON s.CapturingKit = CapturingKit.id LEFT JOIN SampleBarcode ON s.SampleBarcode = SampleBarcode.id LEFT JOIN SampleBarcodeType ON SampleBarcode.SampleBarcodeType = SampleBarcodeType.id LEFT JOIN NgsUser AS nub ON FlowcellLane.QcWetUser = nub.id LEFT JOIN NgsUser AS nuc ON FlowcellLane.QcDryUser = nuc.id LEFT JOIN Project ON s.ProjectId = Project.id LEFT JOIN PrepKit ON Project.PrepKit = PrepKit.id LEFT JOIN NgsUser AS nua ON Project.ResultShippedUser = nua.id LEFT JOIN NgsUser AS nud ON Project.ResultShippedTo = nud.id LEFT JOIN NgsUser AS nue ON Project.ProjectCustomer = nue.id WHERE s.InternalId is not null) ORDER BY ABS(internalSampleID) ASC;";
			// Debug
			logger.info(worksheetQuery);

			// Workaround to call sql2 method (which is not desirable in
			// Database interface)
			JpaDatabase jpaDb;
			if (AopUtils.isAopProxy(db) && db instanceof Advised)
			{
				Object target = ((Advised) db).getTargetSource().getTarget();
				jpaDb = (JpaDatabase) target;
			}
			else
			{
				jpaDb = (JpaDatabase) db;
			}

			currentRows = jpaDb.sql(worksheetQuery, "poolInternalID", "internalSampleID", "externalSampleID",
					"project", "contact", "sequencer", "labStatusPhase", "labStatusComments", "sequencingStartDate",
					"run", "flowcell", "lane", "seqType", "prepKit", "capturingKit", "arrayFile", "arrayID",
					"GAF_QC_Name", "GAF_QC_Date", "GAF_QC_Status", "GCC_Analysis", "GCC_QC_Name", "GCC_QC_Date",
					"GCC_QC_Status", "TargetDateShipment", "DataShippedDate", "DataShippedTo", "DataShippedBy",
					"Comments", "barcode", "barcodeType");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			this.setError(e.getMessage());
		}
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
