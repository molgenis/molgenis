package org.molgenis.col7a1;

import static org.molgenis.col7a1.PatientsViewController.URI;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
	private static final Logger logger = Logger.getLogger(PatientsViewController.class);
	private static final String ENTITYNAME_PATIENTS = "import_patients";
	private static final String ENTITYNAME_PATIENTSVIEW = "import_patientsview";
	private static final String PATIENT_ID = "Patient ID";
	private final DataService dataService;
	public static final String ID = "col7a1_patients";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	private BufferedReader bufferedReader;
	private static List<String> HEADERS_NAMES = Arrays.asList("Patient ID", "Phenotype", "Mutation", "cDNA change",
			"Protein change", "Exon", "Consequence", "Reference");

	@Autowired
	public PatientsViewController(DataService dataService)
	{
		super(URI);
		if (dataService == null) throw new IllegalArgumentException("dataService is null");
		this.dataService = dataService;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init(Model model)
	{
		MysqlRepository entityNamePatientsViewRepo = (MysqlRepository) dataService
				.getRepositoryByEntityName(ENTITYNAME_PATIENTSVIEW);

		try
		{
			entityNamePatientsViewRepo.populateWithQuery("TRUNCATE TABLE " + ENTITYNAME_PATIENTSVIEW + ";");
			entityNamePatientsViewRepo.populateWithQuery("ALTER TABLE " + ENTITYNAME_PATIENTSVIEW
					+ " MODIFY id INT(11) NOT NULL AUTO_INCREMENT;");
			entityNamePatientsViewRepo.populateWithQuery(getViewPopulateSql());
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		MysqlRepository entityNamePatientsRepo = (MysqlRepository) dataService
				.getRepositoryByEntityName(ENTITYNAME_PATIENTS);
		Iterator<Entity> iterator = entityNamePatientsRepo.iterator();

		List<Row> rows = new ArrayList<Row>();
		while(iterator.hasNext()){
			Entity entity = iterator.next();
			String patientId = entity.getString(PATIENT_ID);
			if (null != patientId)
			{
				rows.add(createRow(entityNamePatientsViewRepo, patientId));
			}
		}

		model.addAttribute("headers", HEADERS_NAMES);
		model.addAttribute("rows", rows);
		return "view-col7a1";
	}

	protected Row createRow(MysqlRepository repo, String patientsview_patient_id)
	{
		Iterable<Entity> iterable = repo.findAll(new QueryImpl().eq(PATIENT_ID,
				patientsview_patient_id));
		Iterator<Entity> iterator = iterable.iterator();
		Row row = new Row();
		Map<String, Cell> cellMap= new HashMap<String, Cell>();
		while(iterator.hasNext()){
			Entity entity = iterator.next();
			for (String header : HEADERS_NAMES)
			{
				if(!cellMap.containsKey(header)){
					cellMap.put(header, new Cell());
				}
				
				final Value value;
				if (entity.get(header) != null)
				{
					value = new Value(entity.get(header).toString());
				}
				else
				{
					value = new Value("");
				}
				if(!cellMap.get(header).getValues().contains(value)){
					cellMap.get(header).add(value);
				}
			}
		}
		
		for (String key : HEADERS_NAMES)
		{
			row.add(cellMap.get(key));
		}

		return row;
	}

	protected String getViewPopulateSql() throws IOException
	{
		try
		{
			File f = new File(this.getClass()
					.getResource(File.separator + "mysql" + File.separator + "patientview_col7a1_prototype.sql").getFile());
			 f.toString();
			bufferedReader = new BufferedReader(new FileReader(f.toString()));
			StringBuilder stringBuilder = new StringBuilder();
			String thisLine = null;
			while ((thisLine = bufferedReader.readLine()) != null)
			{
				stringBuilder.append(" ");
				stringBuilder.append(thisLine);
			}

			return stringBuilder.toString();
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public class Row
	{
		private final List<Cell> cells = new ArrayList<Cell>();

		void add(Cell cell)
		{
			this.cells.add(cell);
		}
		
		public List<Cell> getCells()
		{
			return this.cells;
		}
	}

	public class Cell
	{
		private final List<Value> values = new ArrayList<Value>();

		void add(Value value)
		{
			this.values.add(value);
		}

		public List<Value> getValues()
		{
			return this.values;
		}
	}
	
	public class Value
	{
		private final String value;

		Value(String value)
		{
			this.value = value;
		}

		public String getValue()
		{
			return this.value;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj == null)
			{
				return false;
			}

			if (this == obj)
			{
				return true;
			}

			if (this.getClass() != obj.getClass())
			{
				return false;
			}

			final Value v = (Value) obj;
			return this.value.equals(v.value);
		}
	}
}
