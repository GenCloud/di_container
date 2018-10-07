---

## Module 'threads-context'

[![Build Status](https://api.travis-ci.org/GenCloud/di_container.svg?branch=master)](https://api.travis-ci.org/GenCloud/di_container)
### Intro
Add threads-context module to your project. for maven projects just add this dependency:
```xml
   <repositories>
       <repository>
           <id>ioc_threading</id>
           <url>https://raw.github.com/GenCloud/ioc_container/threading/</url>
           <snapshots>
               <enabled>true</enabled>
               <updatePolicy>always</updatePolicy>
           </snapshots>
       </repository>
   </repositories>
    
   <dependencies>
       <dependency>
           <groupId>org.ioc</groupId>
           <artifactId>threads-factory</artifactId>
           <version>2.0.0.RELEASE</version>
       </dependency>
   </dependencies>
```
    
A typical use of threads-context module would be:
1) Add in Main class of application marker-annotated of enabled this module
```java
    @ThreadingModule
    @ScanPackage(packages = {"org.ioc.test"})
    public class MainTest {
        public static void main(String... parameters){
          IoCStarter.start(MainTest.class, parameters);
        }
    }
```
2) Mark sample component of inheritance ThreadFactorySensible
```java
    @IoCComponent
    public class ComponentThreads implements ThreadFactorySensible {
    	private final Logger log = LoggerFactory.getLogger(AbstractTask.class);
    	private final AtomicInteger atomicInteger = new AtomicInteger(0);
    
    	private DefaultThreadPoolFactory threadPoolFactory;
    
    	@PostConstruct
    	public void init() {
    		// scheduling sample task
    		threadPoolFactory.async((Task<Void>) () -> {
            			log.info("Start test thread!");
            			return null;
            		});
    	}
    
    	@Override
    	public void factoryInform(Factory threadPoolFactory) throws IoCException {
    		this.threadPoolFactory = (DefaultThreadPoolFactory) threadPoolFactory;
    	}
    
    	// register method in runnable task and start running it
    	@SimpleTask(startingDelay = 1, fixedInterval = 5)
    	public void schedule() {
    		log.info("I'm Big Daddy, scheduling and incrementing param - [{}]", atomicInteger.incrementAndGet());
    	}
    }

```
3) Default methods of context
- scheduling
```java
        // Executes an asynchronous tasks. Tasks scheduled here will go to an default shared thread pool.
        <T> TaskFuture<T> async(Task<T> callable)
        // Executes an asynchronous tasks at an scheduled time. Please note that resources in scheduled
        // thread pool are limited and tasks should be performed fast.
        <T> TaskFuture<T> async(long delay, TimeUnit unit, Task<T> callable)
        // Executes an asynchronous tasks at an scheduled time. Please note that resources in scheduled
        // thread pool are limited and tasks should be performed fast.
        ScheduledTaskFuture async(long delay, TimeUnit unit, long repeat, Runnable task)
```
    
4) Use it!

### Contribute
Pull requests are welcomed!!

The license is [GNU GPL V3](https://www.gnu.org/licenses/gpl-3.0.html/).

_GenCloud_