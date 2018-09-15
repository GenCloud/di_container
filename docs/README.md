---

# DI (IoC) Container realization

[![Build Status](https://api.travis-ci.org/GenCloud/di_container.svg?branch=master)](https://api.travis-ci.org/GenCloud/di_container)
### Intro
Add IoC to your project. for maven projects just add this dependency:
```xml
    <repositories>
        <repository>
            <id>di_container-mvn-repo</id>
            <url>https://raw.github.com/GenCloud/di_container/context/</url>
            <snapshots>
                <enabled>true</enabled>
                <updatePolicy>always</updatePolicy>
            </snapshots>
        </repository>
    </repositories>
    
    <dependencies>
        <dependency>
            <groupId>org.genfork</groupId>
            <artifactId>context</artifactId>
            <version>0.0.2-STABLE</version>
        </dependency>
    </dependencies>
```

A typical use of IoC would be:
```java
@ScanPackage(packages = {"org.di.test"})
public class MainTest {
    public static void main(String... args) {
        IoCStarter.start(MainTest.class, args);
    }
}
```

A component usage would be:
```java
@Lazy // annotation of component is marked for lazy-loading (firts call is instantiated)
@IoCComponent
@LoadOpt(PROTOTYPE) // type for loading - PROTOTYPE | SINGLETON, if !present annotation - component has default type SINGLETON
public class ComponentA {
    @Override
    public String toString() {
        return "ComponentA{hash:" + Integer.toHexString(hashCode()) + "}";
    }
}
```

A component dependency usage would be:
```java
    @IoCDependency // marked field for scanner found dependency
    private ComponentA componentA;
```

A configuration usage would be:
```java
@Property(path = "configs/ExampleEnvironment.properties") // main annotation for init environment
// path - destination of configuration file
public class ExampleEnvironment extends SamplePropertyListener {

    private String nameApp;

    private String[] components;

    @PropertyFunction
    public SampleProperty value() {
        return new SampleProperty(158);
    }

    @Override
    public String toString() {
        return "ExampleEnvironment{hash: " + Integer.toHexString(hashCode()) + ", nameApp='" + nameApp + '\'' +
                ", components=" + Arrays.toString(components) +
                '}';
    }
    
    public class SampleProperty {
        private int value;
    
        public SampleProperty(int value) {
            this.value = value;
        }
    
        public int getValue() {
            return value;
        }
    }
}
```
# Modules
1. Module 'threads-factory'
   
      [![Build Status](https://api.travis-ci.org/GenCloud/di_container.svg?branch=master)](https://api.travis-ci.org/GenCloud/di_container)
      ### Intro
      Add threads-factory module to your project. for maven projects just add this dependency:
      ```xml
          <repositories>
              <repository>
                  <id>di_container-mvn-repo</id>
                  <url>https://raw.github.com/GenCloud/di_container/threading/</url>
                  <snapshots>
                      <enabled>true</enabled>
                      <updatePolicy>always</updatePolicy>
                  </snapshots>
              </repository>
          </repositories>
      
          <dependencies>
              <dependency>
                  <groupId>org.genfork</groupId>
                  <artifactId>threads-factory</artifactId>
                  <version>0.0.2-STABLE</version>
              </dependency>
          </dependencies>
      ```
      
      A typical use of threads-factory module would be:
      1) Add in Main class of application marker-annotation of enabled this module
      ```java
      @ThreadingModule
      @ScanPackage(packages = {"org.di.test"})
      public class MainTest {
          public static void main(String... args){
            IoCStarter.start(MainTest.class, args);
          }
      }
      ```
      2) Mark sample component of inheritance ThreadFactorySensible<F>
      ```java
      @IoCComponent
      public class ComponentThreads implements ThreadFactorySensible<DefaultThreadingFactory> {
          private final Logger log = LoggerFactory.getLogger(AbstractTask.class);
      
          private DefaultThreadingFactory defaultThreadingFactory; //Thread factory to instantiate by Sensibles
      
          private final AtomicInteger atomicInteger = new AtomicInteger(0);
      
          @PostConstruct
          public void init() {
              // scheduling sample task
              defaultThreadingFactory.async(new AbstractTask<Void>() {
                  @Override
                  public Void call() {
                      log.info("Start test thread!");
                      return null;
                  }
              });
          }
      
          @Override
          public void threadFactoryInform(DefaultThreadingFactory defaultThreadingFactory) throws IoCException {
              this.defaultThreadingFactory = defaultThreadingFactory;
          }
      
          // register method in runnable task and start running it
          @SimpleTask(startingDelay = 1, fixedInterval = 5)
          public void schedule() {
              log.info("I'm Big Daddy, scheduling and incrementing param - [{}]", atomicInteger.incrementAndGet());
          }
      }
      ```
      3) Default methods of factory
      - scheduling
      ```java
          // Executes an asynchronous tasks. Tasks scheduled here will go to an default shared thread pool.
          <T> AsyncFuture<T> async(Task<T>)
          // Executes an asynchronous tasks at an scheduled time. Please note that resources in scheduled
          // thread pool are limited and tasks should be performed fast.
          <T> AsyncFuture<T> async(long, TimeUnit, Task<T>)
          // Executes an asynchronous tasks at an scheduled time. Please note that resources in scheduled
          // thread pool are limited and tasks should be performed fast.
          ScheduledAsyncFuture async(long, TimeUnit, long, Runnable)
      ```
      
      4) Use it!
      
### Contribute
Pull requests are welcomed!!

The license is [GNU GPL V3](https://www.gnu.org/licenses/gpl-3.0.html/).

This library is published as an act of giving and generosity, from developers to developers. 

_GenCloud_