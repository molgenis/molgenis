# Pilot for compute5 functions

## How to run

Checkout 'molgenis' and 'molgenis_compute5'

	git clone https://www.github.com/molgenis/molgenis.git
	git clone https://www.github.com/molgenis/molgenis_compute5.git
	
Import into Eclipse 

	File -> Import ... -> Existing projects into workspace (choose both)
	
Inside Eclipse create a run configuration for ComputeCommandline with arguments

	-w workflows/splitmerge/workflow.csv -o target -p workflows/splitmerge/parameters.csv
	
	
## Known limitations

* Workflow behavior cannot depend on results from previous steps. Examples: based on counts I get a limitted set of chunks.