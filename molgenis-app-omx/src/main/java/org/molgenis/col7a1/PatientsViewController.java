package org.molgenis.col7a1;

import static org.molgenis.col7a1.PatientsViewController.URI;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.mysql.MysqlRepository;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(URI)
public class PatientsViewController extends MolgenisPluginController
{
	public static final String ID = "col7a1_patients";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	private static final Logger logger = Logger.getLogger(PatientsViewController.class);
	private final static List<String> HEADERS_NAMES = Arrays.asList("Patient ID", "Phenotype", "Mutation",
			"cDNA change", "Protein change", "Exon", "Consequence", "Reference");
	private final static String PATH_TO_INSERT_QUERY = File.separator + "mysql" + File.separator
			+ "patientview_col7a1_prototype.sql";
	private static final String ENTITYNAME_PATIENTS = "import_patients";
	private static final String ENTITYNAME_PATIENTSVIEW = "import_patientsview";
	private static final String PATIENT_ID = "Patient ID";
	private static final String TITLE = "Patients view";
	private final DataService dataService;
	private final MysqlViewService mysqlViewService;


	@Autowired
	public PatientsViewController(DataService dataService, MysqlViewService mysqlViewService)
	{
		super(URI);
		if (dataService == null) throw new IllegalArgumentException("dataService is null");
		this.dataService = dataService;

		if (mysqlViewService == null) throw new IllegalArgumentException("mysqlViewService is null");
		this.mysqlViewService = mysqlViewService;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init(Model model)
	{
		MysqlRepository patientsViewRepo = (MysqlRepository) dataService
				.getRepositoryByEntityName(ENTITYNAME_PATIENTSVIEW);

		patientsViewRepo.truncate();
		patientsViewRepo.populateWithQuery(patientsViewRepo.getMySqlQueryFromFile(PATH_TO_INSERT_QUERY));

		MysqlRepository patientsRepo = (MysqlRepository) dataService
				.getRepositoryByEntityName(ENTITYNAME_PATIENTS);

		List<Row> rows = createRows(patientsRepo, patientsViewRepo);
		model.addAttribute("headers", HEADERS_NAMES);
		model.addAttribute("rows", rows);
		model.addAttribute("title", TITLE);
		return "view-col7a1";
	}

	protected List<Row> createRows(MysqlRepository patientsRepo, MysqlRepository patientsViewRepo)
	{
		Iterator<Entity> iterator = patientsRepo.iterator();
		List<Row> rows = new ArrayList<Row>();
		while (iterator.hasNext())
		{
			Entity entity = iterator.next();
			String patientId = entity.getString(PATIENT_ID);
			if (null != patientId)
			{
				Iterable<Entity> iterable = patientsViewRepo.findAll(new QueryImpl().eq(PATIENT_ID, patientId));
				Map<String, List<Value>> valuesPerHeader = this.mysqlViewService.valuesPerHeader(HEADERS_NAMES,
						iterable);
				rows.add(this.mysqlViewService.createRowByMergingValuesIfEquales(HEADERS_NAMES,
						valuesPerHeader));
			}
		}
		return rows;
	}
}
