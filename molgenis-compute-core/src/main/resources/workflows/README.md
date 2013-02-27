This folder contains workflows.

Definitions
* A workflow is consists of steps
* Each step is implemented in a protocol (contained in protocols directory)
* Each protocol is either in 'protocols', 'workflows', or has a fully qualified path (e.g. protocols/alignment/bwa.ftl)
* Steps are linked using input/output parameter relationships
* Inputs can either come from 'user', from a previous step, or be an expression (!)
* (future) user parameters are auto-wired to input ports if they have the same name
* each step is assumed to run in a seperate directory (framework driven)
* Each protocol may change the parameter (in an expression)

Wish list:
* autogenerate a graph that shows all instances of steps.
