package org.molgenis.col7a1;

import static org.molgenis.col7a1.MutationsViewController.URI;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.mysql.MysqlRepository;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(URI)
public class MutationsViewController extends MolgenisPluginController
{
	private static final Logger logger = Logger.getLogger(MutationsViewController.class);
	public static final String ID = "col7a1_mutations";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	private static final String ENTITYNAME_MUTATIONSVIEW = "import_mutationsview";
	private final DataService dataService;
	private BufferedReader bufferedReader;
	private static List<String> HEADERS_NAMES = Arrays.asList("Mutation ID", "cDNA change", "Protein change",
			"Exon/Intron", "Consequence",
			"Inheritance", "Patient ID", "Phenotype");

	@Autowired
	public MutationsViewController(DataService dataService)
	{
		super(URI);
		if (dataService == null) throw new IllegalArgumentException("dataService is null");
		this.dataService = dataService;
	}


	@RequestMapping(method = RequestMethod.GET)
	public String init(Model model)
	{
		MysqlRepository repo = (MysqlRepository) dataService
				.getRepositoryByEntityName(ENTITYNAME_MUTATIONSVIEW);

		try
		{
			repo.populateWithQuery("TRUNCATE TABLE " + ENTITYNAME_MUTATIONSVIEW + ";");
			repo.populateWithQuery("ALTER TABLE " + ENTITYNAME_MUTATIONSVIEW
					+ " MODIFY id INT(11) NOT NULL AUTO_INCREMENT;");
			repo.populateWithQuery(getViewPopulateSql());
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Iterator<Entity> iterator = repo.iterator();

		List<Row> rows = new ArrayList<Row>();
		while (iterator.hasNext())
		{
			rows.add(createRow(iterator.next()));
		}

		model.addAttribute("headers", HEADERS_NAMES);
		model.addAttribute("rows", rows);

		return "view-col7a1";
	}

	protected Row createRow(Entity entity)
	{
		Row row = new Row();

		for (String header : HEADERS_NAMES)
		{
			final Value value;
			if (null != entity.get(header))
			{
				value = new Value(entity.get(header).toString());
			}
			else
			{
				value = new Value("");
			}

			final Cell cell = new Cell();
			cell.add(value);
			row.add(cell);
		}

		return row;
	}

	protected String getViewPopulateSql() throws IOException
	{
		try
		{
			File f = new File(this.getClass()
					.getResource(File.separator + "mysql" + File.separator + "mutationview_col7a1_prototype.sql")
					.getFile());
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
		public boolean equals(Object obj)
		{
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
