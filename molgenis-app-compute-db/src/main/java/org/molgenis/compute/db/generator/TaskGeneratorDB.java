package org.molgenis.compute.db.generator;


/**
 * Created with IntelliJ IDEA. User: georgebyelas Date: 23/04/2013 Time: 08:47
 * To change this template use File | Settings | File Templates.
 */
public class TaskGeneratorDB
{
 	/*
	 * private static final Logger LOG =
	 * Logger.getLogger(TaskGeneratorDB.class); public static final String
	 * BACKEND = "backend";
	 * 
	 * public void generateTasks(String file, String backendUrl, String runName)
	 * { Compute compute = null; // generate here try { compute =
	 * ComputeCommandLine.create(file); } catch (IOException e) { throw new
	 * ComputeDbException(e.getMessage());
	 * 
	 * }
	 * 
	 * List<Task> tasks = compute.getTasks();
	 * 
	 * LOG.info("Generating task for [" + backendUrl + "] with parametersfile ["
	 * + file + "]");
	 * 
	 * int tasksSize = 0;
	 * 
	 * Database db = WebAppConfig.unathorizedDatabase();
	 * 
	 * try { db.beginTx();
	 * 
	 * ComputeRun run = new ComputeRun(); run.setName(runName);
	 * 
	 * String taskSuffix = "_" + System.currentTimeMillis();
	 * 
	 * for (Task task : tasks) { String name = task.getName() + taskSuffix;
	 * String script = task.getScript();
	 * 
	 * ComputeTask computeTask = new ComputeTask(); computeTask.setName(name);
	 * computeTask.setComputeScript(script);
	 * computeTask.setComputeHost(computeHost);
	 * computeTask.setStatusCode(PilotService.TASK_GENERATED);
	 * computeTask.setInterpreter("bash");
	 * 
	 * // find previous tasks in db Set<String> prevTaskNames =
	 * task.getPreviousTasks();
	 * 
	 * List<ComputeTask> previousTasks = new ArrayList<ComputeTask>(); for
	 * (String prevTaskName : prevTaskNames) { ComputeTask prevTask =
	 * ComputeTask.findByName(db, prevTaskName + taskSuffix);
	 * 
	 * if (prevTask != null) { previousTasks.add(prevTask); } else { throw new
	 * ComputeDbException("No ComputeTask  " + prevTaskName +
	 * " is found, when searching for previous task for " + name); } }
	 * 
	 * if (previousTasks.size() > 0) computeTask.setPrevSteps(previousTasks);
	 * 
	 * db.add(computeTask); tasksSize++;
	 * 
	 * LOG.info("Task [" + computeTask.getName() + "] is added\n");
	 * 
	 * // add parameter values to DB Map<String, Object> tastParameters =
	 * task.getParameters();
	 * 
	 * for (Map.Entry<String, Object> entry : tastParameters.entrySet()) {
	 * String parameterName = entry.getKey(); String parameterValue =
	 * entry.getValue().toString();
	 * 
	 * ComputeParameterValue computeParameterValue = new
	 * ComputeParameterValue(); computeParameterValue.setName(parameterName);
	 * computeParameterValue.setValue(parameterValue);
	 * 
	 * ComputeTask taskInDB =
	 * db.query(ComputeTask.class).equals(ComputeTask.NAME,
	 * computeTask.getName()) .find().get(0);
	 * 
	 * computeParameterValue.setComputeTask(taskInDB);
	 * 
	 * db.add(computeParameterValue); } }
	 * 
	 * LOG.info("Total: " + tasksSize + " is added to database\n");
	 * 
	 * db.commitTx(); } catch (Exception e) { try { db.rollbackTx(); } catch
	 * (DatabaseException e1) { e1.printStackTrace(); } throw new
	 * ComputeDbException(e.getMessage()); } finally { IOUtils.closeQuietly(db);
	 * }
	 * 
	 * }
	 * 
	 * public static void main(String[] args) throws IOException { new
	 * TaskGeneratorDB() .generateTasks(
	 * "/Users/georgebyelas/Development/molgenis/molgenis-compute-core/src/main/resources/workflows/demoNBIC2/parameters.csv"
	 * , "grid"); }
	 */

}
