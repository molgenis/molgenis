# Jobs
## Two kinds of jobs

There are two kinds of jobs. **Ordinary jobs** are simple long-running tasks that
get executed once.
**Scheduled jobs** are jobs that are scheduled to run one or more times when a
certain trigger fires.

An example of an ordinary job is annotating an entity with four different annotators.
It gets configured by the user on the annotator screen in the data explorer and it gets
executed immediately and asynchronously when the user clicks a button.
The job gets executed once.

An example of a scheduled job is a nightly job that imports data from a URL.
A scheduled job can get executed more than once.

### Example: Creating a Hello World job
Let's create a new kind of job, a Hello World job that waits for a specified amount of seconds and
then says Hello to the calling user.

#### Service method
The implementation sits in a method @Service bean.

```java
@Service
public class HelloWorldService
{
  // This is a Service bean, so you can ask Spring to @Autowire any dependency you need to do the job
  
  /**
   * Greets the user.
   */
  public String helloWorld(Progress progress, String who, int delay) throws InterruptedException
  {
    sleep(delay * 1000);
    return "Hello " + who + "!";
  }
}
```
Take a look at the method's parameters. `String who` is needed to create the greeting.
`int delay` determines how long the service waits before returning the greeting.
The `Progress progress` parameter allows the job to easily report on its progress.

#### JobExecution entity
Each time a job gets executed, its execution is represented by an instance of
an entity that extends from the `JobExecution` entity.
This helps:
* To keep a record of all information used to create the job, in particular the parameter values.
* To uniformly log the progress and status of the job execution
* To uniformly show Progress bars and a list of recently executed jobs in the Jobs plugin.

The `JobExecution` entity is abstract. You will not find a repository for it.
So let's extend the JobExecution entity and add a `delay` attribute to store the value of the delay 
parameter. We could also add an attribute to store the value of the who String. But instead, we'll 
use the `user` attribute which we inherit from `JobExecution`

```java
public class HelloWorldJobExecution extends JobExecution
{
  [... standard constructors go here ...]
  
  // getter and setter for the job-specific parameters
  public int getDelay()
  {
    return getInt(DELAY);
  }

  public void setDelay(int delay)
  {
    set(DELAY, delay);
  }
}
```

You'll also need to create classes `HelloWorldJobExecutionMetadata` and `HelloWorldJobExecutionFactory`, 
just like for any system entity you create.

#### Job Factory
Now we must call the service method with the parameters from the HelloWorldJobExecution.
Configure a JobFactory bean that links them together:
```java
  @Bean
  public JobFactory<HelloWorldJobExecution> helloWorldJobFactory()
  {
    return new JobFactory<HelloWorldJobExecution>()
    {
      @Override
      public Job<String> createJob(HelloWorldJobExecution jobExecution)
      {
        final String who = jobExecution.getUser();
        final int delay = jobExecution.getDelay();
        return progress -> helloWorldService.helloWorld(progress, who, delay);
      }
    };
  }
```

#### Running the job
If you want to run a Hello World job, you should
 * `@Autowire` the `HelloWorldJobExecutionFactory`
 * use it to create a new instance of `HelloWorldJobExecution`
 * set the parameter values
 * submit it for execution using `JobExecutor.submit()`
 
 ```java
HelloWorldJobExecution jobExecution = factory.create();
jobExecution.setDelay(1);
jobExecution.setUser("user");
jobExecutor.submit(jobExecution);
```

#### Scheduling the job
But perhaps you want to allow the Hello World job to be scheduled. In that case all you have
to do is add a `ScheduledJobType` bean:
```java
@Lazy
@Bean
public ScheduledJobType helloWorldJobType()
{
  ScheduledJobType result = scheduledJobTypeFactory.create("helloWorld");
  result.setJobExecutionType(helloWorldJobExecutionMetadata);
  result.setLabel("Hello World");
  result.setDescription("Simple job example");
  result.setSchema("{\"title\":\"Hello World Job\",\"type\":\"object\",\"properties\":{\"delay\":{\"type\":\"integer\"}},\"required\":[\"delay\"]}");
  return result;
}
```
> Make sure you specify a unique name for your bean!

The Schema property contains a [JSON schema](http://json-schema.org) that will be used to validate the parameters
attribute for the ScheduledJob when it is scheduled.
The value of the parameters object will be parsed as a Map<String, Object> and the values
will be written to the JobExecution using JavaBean setters. So make sure that the parameter names
match the bean's property names and types. In this particular case, the `delay` property of type integer
will get written to `HelloWorldJobExcution.setDelay()`.

For scheduled jobs, the value of the `user` property of the JobExecution will get set automatically to the user who created 
the `ScheduledJob`.

#### Complete code
Check out [the complete Hello World job example](https://github.com/molgenis/molgenis/tree/master/molgenis-jobs/src/test/java/org/molgenis/data/jobs/model/hello).

### A bit more about the Progress interface
Use the `Progress` interface to log the progress of the job execution.

You, as creator of the job, decide how to report and scale the job's progress.

The value provided to the `progress()` method will be written to the `progressInt`
attribute of the `JobExecution` entity and displayed in the progress bar.
If you specify a value for `progressMax`, the progress bar will be set to a width
proportional to `progressInt/progressMax`. Otherwise it'll be full width, and animated
while running.

The progress message plus the time the method was called will be logged in the `log`
attribute of the `JobExecution` entity.

If your job runs outside of Molgenis, like in R or on the cluster, it should update its 
JobExecution entity through the REST API to keep track of progress and status.

### Transactions and running as user
If you want to run the job as a transaction, annotate your @Service method with @Transactional.
The wisdom of having long-running transactions is debatable, so you may want to think up 
some compensating actions instead to run if the job fails and put those in a catch block.

### Job React Components
You can use the Job React Components to easily display a uniform progress bar.
Use the JobContainer to display a progress bar for a single `JobExecution`.

Use the JobsContainer to display a refreshing overview of `JobExecution`s currently running
and in the past.
It needs a URL prop that it'll query regularly to keep the overview up to date.
The mechanism for updating the screen is very simply polling the server for a complete
overview for all jobs, so be careful not to overdo it.