[![Build Status](https://travis-ci.org/GenCloud/ioc_container.svg?branch=master)](https://travis-ci.org/GenCloud/ioc_container)

# IoC Starter Framework

### Functional
- introduction of dependencies through annotations;
- lazy initialization of components (on demand);
- aspect implementation
- listeners system
- awarenes and component processor's
- built-in loader configuration files (formats: ini, xml, property);
- The command line argument handler;
- processing modules by creating factories;
- built-in events and listeners;
- embedded informants (Sensibles) to "inform" a component, factory, listener, processor (ComponentProcessor) about the fact that certain information must be loaded into the object depending on the informer;
- a module for managing / creating a thread pool, declaring functions as executable tasks for some time and initializing them in the pool factory, as well as starting from the SimpleTask parameters.
- a module for managing cache's
- a module for managing database (auto-generation queries, repository system, crud operation, transaction's, jpa support)
- a module for managing web requests and controlled web view

### Intro
Add IoC to your project. for maven projects just add this dependency:
```xml
    <repositories>
        <repository>
            <id>ioc</id>
            <url>https://raw.github.com/GenCloud/ioc_container/channel/</url>
            <snapshots>
                <enabled>true</enabled>
                <updatePolicy>always</updatePolicy>
            </snapshots>
        </repository>
    </repositories>
    
    <dependencies>
        <dependency>
            <groupId>org.ioc</groupId>
            <artifactId>context-factory</artifactId>
            <version>2.3.2.Final</version>
        </dependency>
    </dependencies>
```

A typical use of IoC would be:
```java
@ScanPackage(packages = {"org.ioc.test"})
public class MainTest {
    public static void main(String... args) {
        IoCStarter.start(MainTest.class, args);
    }
}
```

A component usage would be:
```java
@Lazy // annotation of component is marked for lazy-loading (firts call is instantiated)
@IoCComponent(scope = Mode.PROTOTYPE) // type for loading - PROTOTYPE | SINGLETON, if !present annotation - component has default type SINGLETON
public class TypeA {
	@Override
	public String toString() {
		return "TypeA{hash=" + hashCode() +"}";
	}
}
```

A component dependency usage would be:
```java
    @IoCDependency // marked field for scanner found dependency
    private TypeA typeA;
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

Create custom listener.
```java
@Fact//compulsory abstract
public class GlobalListener implements IListener {
	private static final Logger log = LoggerFactory.getLogger(TestListener.class);

	@Override
	public boolean dispatch(AbstractFact abstractFact) {
		if (OnContextIsInitializedFact.class.isAssignableFrom(abstractFact.getClass())) {
			log.info("ListenerInform - Context is initialized! [{}]", abstractFact.getSource());
		} else if (OnTypeInitFact.class.isAssignableFrom(abstractFact.getClass())) {
			final OnTypeInitFact ev = (OnTypeInitFact) abstractFact;
			log.info("ListenerInform - Component [{}] in instance [{}] is initialized!", ev.getComponentName(), ev.getSource());
		}
		return true;
	}
}
```
Have to standards of type event:
OnContextIsInitializedFact - informs about full channel initialization
OnContextDestroyFact - informs about channel start destroying
OnTypeInitFact - informs about the full initialization of any component

AOP system
Create custom aop listener:
```java
@IoCAspect //mandatory annotation-marker for initializing aspect type
public class AopTest {
	private static final Logger log = LoggerFactory.getLogger(AopTest.class);

        //function-listener to perform a function of another class
	@PointCut("exec(void org.ioc.test.types.TypeB.initAspect(String))")
	public void testPostAop() {

	}

        //function-listener invocation testPostAop()
	@BeforeInvocation("testPostAop()")
	public void testBefore(JunctionDot junctionDot) {
		log.info("Before: Evaluate point - [{}]", junctionDot);
	}

        //function-listener invocation testPostAop()
	@AfterInvocation("testPostAop()")
	public void testAfter(JunctionDot junctionDot) {
		log.info("After: Evaluate point - [{}]", junctionDot);
	}

        //function-listener invocation testPostAop()
	@AroundExecution("testPostAop()")
	public void testAround(JunctionDot junctionDot) {
		log.info("Around: Evaluate point - [{}]", junctionDot);
	}
}
```

and listening TypeB
```java
@IoCComponent
public class TypeB implements DestroyProcessor {
	private static final Logger log = LoggerFactory.getLogger(TypeB.class);

	@IoCDependency
	private TypeA typeA;

	@IoCDependency
	private ExampleEnvironment exampleEnvironment;

	public void initAspect(String s) {
		log.info(s);
	}

	@Override
	public String toString() {
		return "TypeB{hash=" + hashCode() + " ,typeA=" + typeA + '}';
	}

	@Override
	public void destroy() {
		log.info("I'm destroyed");
	}
}
```

invoke method in MainClass:
```java
@ScanPackage(packages = {"org.ioc.test"})
public class MainTest {
    public static void main(String... args) {
        DefaultIoCContext channel = IoCStarter.start(MainTest.class, args);
        log.info("Getting TypeB from channel");
        final TypeB typeB = channel.getType(TypeB.class);
        assertNotNull(typeB);
        typeB.initAspect("I'm tested Aspects"); // see console result
        log.info(typeB.toString());
    }
}
```

ComponentProcessor's
```java
public interface TypeProcessor {
    /**
     * Processing not installed component before initialization in factories.
     *
     * @param componentName component name
     * @param component     instantiated component
     * @return modified bag
     */
    Object afterComponentInitialization(String componentName, Object component);

    /**
     * Processing installed component after initialization in factories.
     *
     * @param componentName component name
     * @param component     instantiated component
     * @return modified bag
     */
    Object beforeComponentInitialization(String componentName, Object component);
}
```

Override him and start your application:
```java
public class DefaultProcessor implements TypeProcessor, ContextSensible {
	private final Logger log = LoggerFactory.getLogger(DefaultProcessor.class);

	@Override
	public Object afterComponentInitialization(String componentName, Object component) {
		if (component instanceof TypeA) {
			log.info("Sample changing TypeA bag after initialization");
		}
		return component;
	}

	@Override
	public Object beforeComponentInitialization(String componentName, Object component) {
		if (component instanceof TypeB) {
			log.info("Sample changing TypeB bag before initialization");
		}
		return component;
	}

	@Override
	public void contextInform(IoCContext ioCContext) throws IoCException {
		log.info("I'm informed for channel - [{}]", ioCContext);
	}
}
```
Threading
1) Mark sample component of inheritance ThreadFactorySensible
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
2) Default methods of channel
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

Cache
* sample factories: EhFactory, GuavaFactory

1) Mark sample component of inheritance CacheFactorySensible
```java
    @IoCComponent
    public class CacheComponentTest implements CacheFactorySensible {
        private static final Logger log = LoggerFactory.getLogger(CacheComponentTest.class);
    
        private EhFactory factory;
    
        private ICache<String, String> sampleCache;
    
        @PostConstruct
        public void initializeCache() {
            sampleCache = factory.installEternal("sample-test-cache", 200);
    
            log.info("Creating sample cache - [{}]", sampleCache);
    
            sampleCache.put("1", "First");
            sampleCache.put("2", "Second");
            sampleCache.put("3", "Third");
            sampleCache.put("4", "Fourth");
    
            log.info("Loaded size - [{}]", sampleCache.size());
        }
    
        public String getElement(String key) {
            final String value = sampleCache.get(key);
            log.info("Getting value from cache - [{}]", value);
            return value;
        }
    
        public void removeElement(String key) {
            log.info("Remove object from cache");
            sampleCache.remove(key);
        }
    
        public void invalidate() {
            sampleCache.clear();
            log.info("Clear all cache, size - [{}]", sampleCache.size());
        }
    
        @Override
        public void factoryInform(Factory factory) throws IoCException {
            this.factory = (EhFactory) factory;
        }
    
        @Override
        public String toString() {
            return "CacheComponentTest{" +
                    "factory=" + factory +
                    ", sampleCache=" + sampleCache +
                    '}';
        }
    }
```

2) Default methods of factory
- cache management
```java
            //Add pair <K, V> to cache. Notice: if there is already a value with given id in map,
            // {@link IllegalArgumentException} will be thrown.
           void put(K, V);
       
           //Returns cached value correlated to given key.
           V get(K);
       
           //Checks whether this map contains a value related to given key.
           boolean contains(K key);
       
           //Removes an entry from map, that has given key.
           void remove(K key);
       
           //Clears cache.
           void clear();
       
           //size of cache map
           int size();
```
    
### 1. Module 'orm-factory'

### Intro
Add orm-factory module to your project. for maven projects just add this dependency:   
```xml
    <repositories>
        <repository>
            <id>ioc_cache</id>
            <url>https://raw.github.com/GenCloud/ioc_container/orm</url>
            <snapshots>
                <enabled>true</enabled>
                <updatePolicy>always</updatePolicy>
            </snapshots>
        </repository>
    </repositories>
     
    <dependencies>
        <dependency>
            <groupId>org.ioc</groupId>
            <artifactId>orm-factory</artifactId>
            <version>2.3.2.Final</version>
        </dependency>
    </dependencies>
```

A typical use of threads-factory module would be:
1. Add in Main class of application marker-annotation of enabled this module
```java
     @DatabaseModule //default datasource - Orient
     @ScanPackage(packages = {"org.ioc.test"})
     public class MainTest {
         public static void main(String... args){
           IoCStarter.start(MainTest.class, args);
         }
     }
```
* support datasource: OrientDB Schema
* support JPA annotations
 
2. Create custom component, repositories and entity classes:
* entity classes:
```java
@Entity // marker for an inspector defining what an entity is.
// specifies the primary table for annotated entity
@Table(name = "child_entity", indexes = {
		//Indexes for table. These are only used if table generation is in effect. Defaults to no additional indexes.
		@Index(columnList = "name, sample_entity", unique = true) 
})
public class ChildEntity implements Serializable {
	@Id //identifier for primary key entity
	//Provides for specification of generation strategies for values of primary keys
	@GeneratedValue(strategy = GenerationType.SEQUENCE) 
	private long id;

	//Used to specify the mapped column for field
	@Column(name = "name")
	private String name;

        //Specifies column for joining an entity relations or element collection
	@JoinColumn(name = "sample_entity_id")
	//Defines single-valued association to another entity class that has many-to-one multiplicity
	@ManyToOne(fetch = FetchType.LAZY, cascade = ALL)
	private SampleEntity sampleEntity;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public SampleEntity getSampleEntity() {
		return sampleEntity;
	}

	public void setSampleEntity(SampleEntity sampleEntity) {
		this.sampleEntity = sampleEntity;
	}

	@Override
	public String toString() {
		return "ChildEntity{" +
				"id=" + id +
				", name='" + name
				+ '}';
	}
}

@Entity
public class OneToOneEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private long id;

        //Defines single-valued association to another entity that has one-to-one multiplicity
	@OneToOne(fetch = FetchType.LAZY, cascade = ALL)
	private SampleEntity sampleEntity;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public SampleEntity getSampleEntity() {
		return sampleEntity;
	}

	public void setSampleEntity(SampleEntity sampleEntity) {
		this.sampleEntity = sampleEntity;
	}

	@Override
	public String toString() {
		return "OneToOneEntity{" +
				"id=" + id +
				'}';
	}
}

@Entity
@Table(name = "sample_entity")
//Specifies a static, named query in the Java Persistence query language
@NamedQuery(name = "SampleEntity.findById", query = "select from sample_entity where id = :id")
public class SampleEntity implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private long id;

	@Column(name = "name")
	private String name;

	@Column(name = "year")
	private String year;

	@OneToOne(fetch = FetchType.LAZY, cascade = ALL)
	private OneToOneEntity oneToOneEntity;

        //Defines many-valued association with one-to-many multiplicity
	@OneToMany(fetch = FetchType.LAZY, cascade = ALL)
	private List<ChildEntity> childEntities = new ArrayList<>();

	public long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public List<ChildEntity> getChildEntities() {
		return childEntities;
	}

	public void setChildEntities(List<ChildEntity> childEntities) {
		this.childEntities = childEntities;
	}

	public OneToOneEntity getOneToOneEntity() {
		return oneToOneEntity;
	}

	public void setOneToOneEntity(OneToOneEntity oneToOneEntity) {
		this.oneToOneEntity = oneToOneEntity;
	}

	@Override
	public String toString() {
		return "SampleEntity{" +
				"id=" + id +
				", name='" + name + '\'' +
				", year='" + year + '\'' +
				'}';
	}
}
```

* repository classes:
```java
@IoCRepository //mandatory annotation-marker for channel inspector
public interface ChildEntityRepository extends CrudRepository<ChildEntity, Long> {
}

@IoCRepository
public interface OneToOneEntityRepository extends CrudRepository<OneToOneEntity, Long> {
}

@IoCRepository
public interface SampleEntityRepository extends CrudRepository<SampleEntity, Long> {
	@Transactional // generated query will be executed in the transaction
	SampleEntity findByNameEqAndYearEq(String name, String year);

	@Transactional
	List<SampleEntity> findByNameEq(String name);

	@Transactional
	/** Marker annotation for identifying named requests registered in entity and their execution.
	  * name() - query name, defined in @NamedQuery annotation in entity class;
	  * params() - parameters to replace when executing the query.
	  * 
	  * <pre>
	  * &#34;SampleEntity
	  *     @NamedQuery(name = "SampleEntity.findById", query = "select from sample_entity where id = :id")
          *     public class SampleEntity
          *     ...
          * &#42;SampleEntityRepository
          *     //parameter id is replaced when query has been execution
          *     @Query(name = "SampleEntity.findById", params = "id")
          *     SampleEntity namedQuery(long id);
	  */
	@Query(name = "SampleEntity.findById", params = "id")
	SampleEntity namedQuery(long id);
}
```

make test component:
```java
@IoCComponent
public class DatabaseComponent {
	@IoCDependency
	private SampleEntityRepository sampleEntityRepository;

	@IoCDependency
	private OneToOneEntityRepository oneToOneEntityRepository;

	@IoCDependency
	private ChildEntityRepository childEntityRepository;

        //CRUD default repository operation: save entity
	public void saveOneToOneEntity(OneToOneEntity oneToOneEntity) {
		oneToOneEntityRepository.save(oneToOneEntity);
	}

        //CRUD default repository operation: save entity
	public void saveChildEntity(ChildEntity childEntity) {
		childEntityRepository.save(childEntity);
	}

        //CRUD default repository operation: save entity
	public void saveSampleEntity(SampleEntity sampleEntity) {
		sampleEntityRepository.save(sampleEntity);
	}

        //CRUD default repository operation: find entity by your id
	public SampleEntity findSampleEntity(long id) {
		return sampleEntityRepository.fetch(id);
	}

        //find entity by defined named query
	public SampleEntity findByNamedQuery(long id) {
		return sampleEntityRepository.namedQuery(id);
	}

        //find entity by custom query where SampleEntity#name equals name and year
	public SampleEntity findSampleEntityByName(String name) {
		return sampleEntityRepository.findByNameEqAndYearEq(name, "2018");
	}

        //find all entities by custom query where SampleEntity#name equals name
	public List<SampleEntity> findAllByName(String name) {
		return sampleEntityRepository.findByNameEq(name);
	}

        //CRUD default repository operation: find all entities in table
	public List<SampleEntity> findAll() {
		return sampleEntityRepository.fetchAll();
	}

        //CRUD default repository operation: delete entity in table
	public void deleteSampleEntity(SampleEntity sampleEntity) {
		sampleEntityRepository.delete(sampleEntity);
	}
}
```

Try usage component in MainClass:
```java
    @DatabaseModule
    @ScanPackage(packages = {"org.ioc.test"})
    public class MainTest {
        public static void main(String... args) {
            DefaultIoCContext channel = IoCStarter.start(MainTest.class, args);
            final DatabaseComponent databaseComponent = channel.getType(DatabaseComponent.class);
       
       		log.info("Inserting test dataContainer into Schema");
       		final SampleEntity sampleEntity = new SampleEntity();
       		sampleEntity.setName("sample28");
       		sampleEntity.setYear("2018");
       
       		final SampleEntity sampleEntity1 = new SampleEntity();
       		sampleEntity1.setName("sample28");
       		sampleEntity1.setYear("2018");
       		databaseComponent.saveSampleEntity(sampleEntity1);
       
       		final SampleEntity sampleEntity2 = new SampleEntity();
       		sampleEntity2.setName("sample28");
       		sampleEntity2.setYear("2018");
       		databaseComponent.saveSampleEntity(sampleEntity2);
       
       		final SampleEntity sampleEntity3 = new SampleEntity();
       		sampleEntity3.setName("sample28");
       		sampleEntity3.setYear("2018");
       		databaseComponent.saveSampleEntity(sampleEntity3);
       
       		final OneToOneEntity oneToOneEntity = new OneToOneEntity();
       		sampleEntity.setOneToOneEntity(oneToOneEntity);
       		oneToOneEntity.setSampleEntity(sampleEntity);
       		databaseComponent.saveOneToOneEntity(oneToOneEntity);
       
       		final ChildEntity childEntity = new ChildEntity();
       		childEntity.setName("child1");
       		childEntity.setSampleEntity(sampleEntity);
       
       		databaseComponent.saveChildEntity(childEntity);
       
       		sampleEntity.getChildEntities().add(childEntity);
       		databaseComponent.saveSampleEntity(sampleEntity);
       
       		log.info("Fetch test data from Schema by generated query");
       		final SampleEntity get0 = databaseComponent.findSampleEntityByName("sample28");
       		log.info(get0.toString());
       
       		log.info("Fetch test data from Schema by named query");
       		final SampleEntity customQuery = databaseComponent.findByNamedQuery(sampleEntity.getId());
       		log.info(customQuery.toString());
       
       		log.info("Fetch all test data from Schema");
       		final List<SampleEntity> get1 = databaseComponent.findAll();
       		log.info(get1.toString());
       
       		log.info("Fetch all test data from Schema by generated query");
       		final List<SampleEntity> sampleEntityList = databaseComponent.findAllByName("sample28");
       		log.info(sampleEntityList.toString());
       
       		log.info("Fetch all test data from Entity cache");
       		final List<SampleEntity> get2 = databaseComponent.findAll();
       		log.info(get2.toString());
       
       		log.info("Delete all test data from Schema");
       		get2.forEach(databaseComponent::deleteSampleEntity);
        }
    }
```

## 2. Module 'web-factory'

### Intro
Add web-factory module to your project. for maven projects just add this dependency:
```xml
   <repositories>
       <repository>
           <id>ioc_cache</id>
           <url>https://raw.github.com/GenCloud/ioc_container/web</url>
           <snapshots>
               <enabled>true</enabled>
               <updatePolicy>always</updatePolicy>
           </snapshots>
       </repository>
   </repositories>
    
   <dependencies>
       <dependency>
           <groupId>org.ioc</groupId>
           <artifactId>web-factory</artifactId>
           <version>2.3.2.Final</version>
       </dependency>
   </dependencies>
```
    
A typical use of web-factory module would be:
1) Add in Main class of application marker-annotation of enabled this module
```java
    @WebModule
    @ScanPackage(packages = {"org.ioc.test"})
    public class MainTest {
        public static void main(String... args){
          IoCStarter.start(MainTest.class, args);
        }
    }
```

* default configurations for web server
```properties
# Web server
web.server.port=8081
web.server.ssl-enabled=false
web.server.velocity.input.encoding=UTF-8
web.server.velocity.output.encoding=UTF-8
web.server.velocity.resource.loader=file
web.server.velocity.resource.loader.class=org.apache.velocity.runtime.resource.loader.FileResourceLoader
web.server.velocity.resource.loading.path=./site
```

2) Mark sample controller of @IoCController
```java
    @IoCController
    @UrlMapping //default marker for mapping requests
    public class SampleController implements ContextSensible, DestroyProcessor {
    	@UrlMapping("/") //mapping request on path "site.com/"
    	//ModelAndView - map with needed attributes in page
    	public ModelAndView index() {
    		final ModelAndView modelAndView = new ModelAndView();
    		final File directory = new File(home);
    
    		final List<SampleEntity> sampleEntities = databaseComponent.findAll();
    
    		final List<TypeMetadata> converted = new ArrayList<>();
    
    		Map<String, TypeMetadata> proto = context.getPrototypeFactory().getTypes();
    
    		Map<String, TypeMetadata> sing = context.getSingletonFactory().getTypes();
    
    		Map<String, TypeMetadata> req = context.getRequestFactory().getTypes();
    
    		converted.addAll(proto.values());
    		converted.addAll(sing.values());
    		converted.addAll(req.values());
    
    		modelAndView.addAttribute("types", converted);
    		modelAndView.addAttribute("entities", sampleEntities);
    		modelAndView.addAttribute("dir", directory);
    		modelAndView.setView("index");
    
    		return modelAndView;
    	}
    
    	@UrlMapping("/date")
    	//DateFormatter - mandatory annotation with your date reading format from request
    	//Param - mandatory annotation for reading request attribute name
    	public IMessage testDate(@DateFormatter("yyyy-MM-dd HH:mm") @Param("date") Date date) {
    		return new IMessage(date.toString());
    	}
    
    	@UrlMapping(value = "/upload", method = POST)
    	public IMessage upload(File file) {
    		if (file == null) {
    			return new IMessage(IMessage.Type.ERROR, "Can't upload");
    		}
    
    		File directory = new File(home);
    		if (!directory.exists()) {
    			directory.mkdir();
    		}
    
    		File newFile = new File(home + file.getName());
    		file.renameTo(newFile);
    
    		return new IMessage("Uploaded: " + file.getName());
    	}
    
    	@UrlMapping("/download")
    	public File download(HttpRequest request) {
    		QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
    
    		return new File(home + decoder.path().substring(10));
    	}
    
    	@UrlMapping("/remove")
    	public IMessage remove(@Param("name") String name) {
    		File directory = new File(home);
    		if (!directory.exists()) {
    			return new IMessage(IMessage.Type.ERROR, "File don't exists");
    		}
    
    		File[] files = directory.listFiles((dir, filterName) -> name.equals(filterName));
    
    		if (files == null || files.length == 0) {
    			return new IMessage(IMessage.Type.ERROR, "File don't exists");
    		}
    
    		return files[0].delete() ? new IMessage("Deleted") : new IMessage(IMessage.Type.ERROR, "Delete error");
    	}
    
    	@UrlMapping("/clear")
    	public IMessage clear() {
    		File directory = new File(home);
    		if (directory.exists()) {
    			Arrays.stream(Objects.requireNonNull(directory.listFiles())).forEach(File::delete);
    			directory.delete();
    		}
    
    		return new IMessage("Successful cleared");
    	}
    
    	@Override
    	public void destroy() {
    		databaseComponent.findAll().forEach(e -> databaseComponent.deleteSampleEntity(e));
    	}
    }
```
* page template - default usage Velocity

3) Define sample page in configured directory:
```vtl
<html>
<head>
    <meta charset="utf-8"/>
    <title>Netty Server</title>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css"
          integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">

    <link rel="stylesheet" href="/static/css/style.css"/>
    <link rel="stylesheet" href="/static/css/pnotify.custom.min.css"/>
    <link rel="stylesheet" href="/static/css/pnotify.css"/>
    <link rel="stylesheet" href="/static/css/pnotify.buttons.css"/>
</head>
<body>
<div class="container">
    <h1>Netty Server</h1>
    <br>
    <h4>Test uploading file</h4>
    <br>
    <form method="post">
        <button type="button" class="btn btn-info">Upload File</button>
        <input type="file" name="file" class="hide"/>
        <button type="button" class="btn btn-success">Clear All Files</button>
    </form>

    <h4>Test parsing date format</h4>
    <br>
    <form id="form" method="get">
        <input name="date" id="date" type="hidden" value="2018-10-13 23:40"/>

        <button type="button" class="btn btn-danger">Test Date</button>
    </form>

    <table class="table">
        <tr>
            <th>File Name</th>
            <th>File Size</th>
            <th>Option's</th>
        </tr>

        #foreach($item in $!dir.listFiles())
            <tr>
                <td><a href="/download/$item.name">$item.name</a></td>
                <td>
                    #set($kb = $item.length())
                    $kb bytes
                </td>
                <td>
                    <button class="btn btn-warning" name="$item.name">Delete</button>
                </td>
            </tr>
        #end
    </table>

    <h4>Test orm data</h4>
    <br>
    <table class="table">
        <tr>
            <th>#</th>
            <th>Entity</th>
            <th>OneToMany relation (size elements)</th>
        </tr>

        #foreach($item in $!entities)
            <tr>
                <td>$item.id</td>
                <td>$item.name</td>
                #if($item.childEntities.size() > 0)
                    <td>$item.childEntities</td>
                #else
                    <td>List empty</td>
                #end
            </tr>
        #end
    </table>

    <h4>Statistic context</h4>
    <br>
    <table class="table">
        <tr>
            <th>Loading mode</th>
            <th>Name</th>
            <th>Instance</th>
        </tr>

        #foreach($item in $!types)
            <tr>
                <td>$item.mode</td>
                <td>$item.name</td>
                <td>$item.instance</td>
            </tr>
        #end
    </table>
</div>

<script type="text/javascript" src="/static/js/jquery.js"></script>
<script type="text/javascript" src="/static/js/bootstrap.min.js"></script>
<script type="text/javascript" src="/static/js/scripts.js"></script>
<script type="text/javascript" src="/static/js/pnotify.js"></script>
<script type="text/javascript" src="/static/js/pnotify.buttons.js"></script>

</body>
</html>
```
4) Start your app or tests and see results on default path (http://127.0.0.1:8081)
![http://screenshot.ru/7e580e04b2a3e752b176298ea4b5cca0.png](http://screenshot.ru/7e580e04b2a3e752b176298ea4b5cca0.png)

### Contribute
Pull requests are welcomed!!

The license is [GNU GPL V3](https://www.gnu.org/licenses/gpl-3.0.html/).

This library is published as an act of giving and generosity, from developers to developers. 

_GenCloud_
