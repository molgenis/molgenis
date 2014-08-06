package org.molgenis.mutationdb;

import static org.molgenis.mutationdb.PatientsViewController.URI;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.molgenis.data.CrudRepository;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.util.MySqlFileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(URI)
public class PatientsViewController extends MolgenisPluginController
{
	public static final String ID = "col7a1_patients";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	public final static List<String> HEADERS_NAMES = Arrays.asList("Patient ID", "Phenotype", "Mutation",
			"cDNA change", "Protein change", "Exon", "Consequence", "Reference");
	public final static String PATH_TO_INSERT_QUERY = "/mysql/patientview_col7a1_prototype.sql";
	public static final String ENTITYNAME_PATIENTS = "import_patients";
	public static final String ENTITYNAME_PATIENTSVIEW = "import_patientsview";
	public static final String PATIENT_ID = "Patient ID";
	public static final String TITLE = "Patients view";
	public final DataService dataService;
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
		model.addAttribute("title", TITLE);
		return "view-col7a1";
	}

	@RequestMapping(value = "/generate", method = GET, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public boolean refresh()
	{
		if (dataService.hasRepository(ENTITYNAME_PATIENTSVIEW))
		{
			CrudRepository patientsViewRepo = (CrudRepository) dataService
					.getRepositoryByEntityName(ENTITYNAME_PATIENTSVIEW);
			this.mysqlViewService.truncate(patientsViewRepo.getEntityMetaData().getName());
			patientsViewRepo.deleteAll();
			this.mysqlViewService.populateWithQuery(MySqlFileUtil.getMySqlQueryFromFile(PatientsViewController.class,
					PATH_TO_INSERT_QUERY));
			return true;
		}
		else
		{
			return false;
		}
	}

	@RequestMapping(value = "/create", method = GET, produces = APPLICATION_JSON_VALUE)
	public String create(Model model)
	{
		List<Row> rows = null;
		if (dataService.hasRepository(ENTITYNAME_PATIENTS) && dataService.hasRepository(ENTITYNAME_PATIENTSVIEW))
		{
			CrudRepository patientsViewRepo = (CrudRepository) dataService
					.getRepositoryByEntityName(ENTITYNAME_PATIENTSVIEW);
			CrudRepository patientsRepo = (CrudRepository) dataService.getRepositoryByEntityName(ENTITYNAME_PATIENTS);
			rows = createRows(patientsRepo, patientsViewRepo);
		}
		model.addAttribute("rows", rows);
		model.addAttribute("headers", HEADERS_NAMES);
		return "view-col7a1-table";
	}

	private List<Row> createRows(CrudRepository patientsRepo, CrudRepository patientsViewRepo)
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
				rows.add(this.mysqlViewService.createRowByMergingValuesIfEquales(HEADERS_NAMES, valuesPerHeader));
			}
		}
		return rows;
	}
}
