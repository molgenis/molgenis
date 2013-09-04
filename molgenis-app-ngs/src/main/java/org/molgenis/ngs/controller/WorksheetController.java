package org.molgenis.ngs.controller;

import static org.molgenis.ngs.controller.WorksheetController.URI;

import java.util.List;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.jpa.JpaDatabase;
import org.molgenis.framework.ui.MolgenisPlugin;
import org.molgenis.framework.ui.html.TupleTable;
import org.molgenis.util.tuple.Tuple;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controller that handles home page requests
 */
@Controller
@RequestMapping(URI)
public class WorksheetController extends MolgenisPlugin
{
	public static final String URI = "/plugin/worksheet";

	private final Database database;

	@Autowired
	public WorksheetController(Database database)
	{
		super(URI);
		if (database == null) throw new IllegalArgumentException("database is null");
		this.database = database;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init(Model model)
	{
		String worksheetQuery = "SELECT * FROM (SELECT \"\" AS poolInternalId, Sample.InternalId AS internalSampleID, Sample.ExternalId AS externalSampleID, Project.ProjectName AS project, CONCAT(nue.UserName, ' <' , nue.UserEmail, '>') AS contact, Machine.MachineName AS sequencer, Sample.LabStatus AS labStatusPhase, Sample.SampleComment AS labStatusComments, DATE_FORMAT(Flowcell.RunDate, '%y%m%d') AS sequencingStartDate, Flowcell.Run AS run, CONCAT(Flowcell.FlowcellDirection,Flowcell.FlowcellName) AS flowcell, FlowcellLane.Lane AS lane, Project.SeqType AS seqType, PrepKit.PrepKitName AS prepKit, CapturingKit.CapturingKitName AS capturingKit, Sample.ArrayFile AS arrayFile, Sample.ArrayId AS arrayID, nub.UserName AS GAF_QC_Name, DATE_FORMAT(FlowcellLane.QcWetDate, GET_FORMAT(DATE,'ISO')) AS GAF_QC_Date, DATE_FORMAT(FlowcellLane.QcWetMet, GET_FORMAT(DATE,'ISO')) AS GAF_QC_Status, nuc.UserName AS GCC_QC_Name, Project.GccAnalysis AS GCC_Analysis, FlowcellLane.QcDryDate AS GCC_QC_Date, FlowcellLane.QcDryMet AS GCC_QC_Status, DATE_FORMAT(Project.ProjectPlannedFinishDate, GET_FORMAT(DATE,'ISO')) AS TargetDateShipment, DATE_FORMAT(Project.ResultShippedDate, GET_FORMAT(DATE,'ISO')) AS DataShippedDate, nud.UserName AS DataShippedTo, nua.UserName AS DataShippedBy, FlowcellLane.FlowcellLaneComment AS Comments, SampleBarcode.SampleBarcodeSequence AS barcode, SampleBarcodeType.SampleBarcodeTypeName AS barcodeType FROM FlowcellLane LEFT JOIN Flowcell ON FlowcellLane.Flowcell = Flowcell.id LEFT JOIN Machine ON Flowcell.Machine = Machine.id LEFT JOIN Sample ON FlowcellLane.Sample = Sample.id LEFT JOIN Sample_SampleInPool ON Sample.id = Sample_SampleInPool.SampleInPool LEFT JOIN CapturingKit ON Sample.CapturingKit = CapturingKit.id LEFT JOIN SampleBarcode ON Sample.SampleBarcode = SampleBarcode.id LEFT JOIN SampleBarcodeType ON SampleBarcode.SampleBarcodeType = SampleBarcodeType.id LEFT JOIN NgsUser AS nub ON FlowcellLane.QcWetUser = nub.id LEFT JOIN NgsUser AS nuc ON FlowcellLane.QcDryUser = nuc.id LEFT JOIN Project ON Sample.ProjectId = Project.id LEFT JOIN PrepKit ON Project.PrepKit = PrepKit.id LEFT JOIN NgsUser AS nua ON Project.ResultShippedUser = nua.id LEFT JOIN NgsUser AS nud ON Project.ResultShippedTo = nud.id LEFT JOIN NgsUser AS nue ON Project.ProjectCustomer = nue.id WHERE Sample.Id not in (SELECT SampleInPool FROM Sample_SampleInPool) AND Sample.Id not in (SELECT Sample FROM Sample_SampleInPool)) a UNION (SELECT p.InternalId AS poolInternalId, s.InternalId AS internalSampleID, s.ExternalId AS externalSampleID, Project.ProjectName AS project, CONCAT(nue.UserName, ' <' , nue.UserEmail, '>') AS contact, Machine.MachineName AS sequencer, s.LabStatus AS labStatusPhase, s.SampleComment AS labStatusComments, DATE_FORMAT(Flowcell.RunDate, '%y%m%d') AS sequencingStartDate, Flowcell.Run AS run, CONCAT(Flowcell.FlowcellDirection,Flowcell.FlowcellName) AS flowcell, FlowcellLane.Lane AS lane, Project.SeqType AS seqType, PrepKit.PrepKitName AS prepKit, CapturingKit.CapturingKitName AS capturingKit, s.ArrayFile AS arrayFile, s.ArrayId AS arrayID, nub.UserName AS GAF_QC_Name, DATE_FORMAT(FlowcellLane.QcWetDate, GET_FORMAT(DATE,'ISO')) AS GAF_QC_Date, DATE_FORMAT(FlowcellLane.QcWetMet, GET_FORMAT(DATE,'ISO')) AS GAF_QC_Status, nuc.UserName AS GCC_QC_Name, Project.GccAnalysis AS GCC_Analysis, FlowcellLane.QcDryDate AS GCC_QC_Date, FlowcellLane.QcDryMet AS GCC_QC_Status, DATE_FORMAT(Project.ProjectPlannedFinishDate, GET_FORMAT(DATE,'ISO')) AS TargetDateShipment, DATE_FORMAT(Project.ResultShippedDate, GET_FORMAT(DATE,'ISO')) AS DataShippedDate, nud.UserName AS DataShippedTo, nua.UserName AS DataShippedBy, FlowcellLane.FlowcellLaneComment AS Comments, SampleBarcode.SampleBarcodeSequence AS barcode, SampleBarcodeType.SampleBarcodeTypeName AS barcodeType FROM FlowcellLane LEFT JOIN Flowcell ON FlowcellLane.Flowcell = Flowcell.id LEFT JOIN Sample AS p ON FlowcellLane.Sample = p.id LEFT JOIN Machine ON Flowcell.Machine = Machine.id LEFT JOIN Sample_SampleInPool link ON p.id = link.Sample LEFT JOIN Sample AS s ON link.SampleInPool = s.id LEFT JOIN CapturingKit ON s.CapturingKit = CapturingKit.id LEFT JOIN SampleBarcode ON s.SampleBarcode = SampleBarcode.id LEFT JOIN SampleBarcodeType ON SampleBarcode.SampleBarcodeType = SampleBarcodeType.id LEFT JOIN NgsUser AS nub ON FlowcellLane.QcWetUser = nub.id LEFT JOIN NgsUser AS nuc ON FlowcellLane.QcDryUser = nuc.id LEFT JOIN Project ON s.ProjectId = Project.id LEFT JOIN PrepKit ON Project.PrepKit = PrepKit.id LEFT JOIN NgsUser AS nua ON Project.ResultShippedUser = nua.id LEFT JOIN NgsUser AS nud ON Project.ResultShippedTo = nud.id LEFT JOIN NgsUser AS nue ON Project.ProjectCustomer = nue.id WHERE s.InternalId is not null) ORDER BY ABS(internalSampleID) ASC;";

		// Workaround to call sql2 method (which is not desirable in
		// Database interface)
		JpaDatabase jpaDb = null;
		try
		{
			if (AopUtils.isAopProxy(database) && database instanceof Advised)
			{
				Object target = ((Advised) database).getTargetSource().getTarget();
				jpaDb = (JpaDatabase) target;
			}
			else
			{
				jpaDb = (JpaDatabase) database;
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}

		List<Tuple> currentRows = jpaDb.sql(worksheetQuery, "poolInternalID", "internalSampleID", "externalSampleID",
				"project", "contact", "sequencer", "labStatusPhase", "labStatusComments", "sequencingStartDate", "run",
				"flowcell", "lane", "seqType", "prepKit", "capturingKit", "arrayFile", "arrayID", "GAF_QC_Name",
				"GAF_QC_Date", "GAF_QC_Status", "GCC_Analysis", "GCC_QC_Name", "GCC_QC_Date", "GCC_QC_Status",
				"TargetDateShipment", "DataShippedDate", "DataShippedTo", "DataShippedBy", "Comments", "barcode",
				"barcodeType");

		model.addAttribute("table", new TupleTable("worksheet", currentRows));

		return "view-worksheet";
	}
}
