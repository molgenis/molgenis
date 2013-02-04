package org.molgenis.generators.cpp;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.MolgenisOptions;
import org.molgenis.generators.Generator;
import org.molgenis.model.elements.Model;

public class CPPCassette extends Generator
{
	ArrayList<Generator> generators;

	public CPPCassette()
	{
		generators = new ArrayList<Generator>();
		generators.add(new IncludePerEntityGen());
		generators.add(new SourcePerEntityGen());
		generators.add(new CPPMainGen());
		generators.add(new MakeFileGen());
	}

	@Override
	public void generate(final Model model, final MolgenisOptions options) throws Exception
	{
		List<Thread> threads = new ArrayList<Thread>();
		for (final Generator g : generators)
		{
			Runnable runnable = new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						g.generate(model, options);
					}
					catch (Exception e)
					{
						e.printStackTrace();
						System.exit(-1);
					}
				}
			};
			// executor.execute(runnable);
			Thread thread = new Thread(runnable);
			thread.start();
			threads.add(thread);
		}

		// wait for all threads to complete
		for (Thread thread : threads)
		{
			try
			{
				thread.join();
			}
			catch (InterruptedException ignore)
			{
			}
		}

	}

	@Override
	public String getDescription()
	{
		return "Generates all CPP the code";
	}

}
