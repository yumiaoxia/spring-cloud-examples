# 通用方式整合 Hystrix
 1. 复制 ribbon/ribbon-simple 的三个模块，按以往的方式修改ArtifactId，模块目录名，将他们加入modules和maven进行管理
 2. 为 `consumer-movie` 模块添加依赖
    ~~~xml
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
    </dependency>
    ~~~
 3. 在启动类上添加注解 `@EnableCircuiBreaker` 或 `@EnableHystrix`,从而为项目启动断路器支持
 4. 修改 MovieController,让其中的 findById 方法具备容错能力。
    ~~~java
    @RestController
    public class MovieController {
    
        private static Logger LOGGER = LoggerFactory.getLogger(MovieController.class);
    
        private final RestTemplate restTemplate;
        private final LoadBalancerClient loadBalancerClient;
    
        public MovieController(RestTemplate restTemplate, LoadBalancerClient loadBalancerClient) {
            this.restTemplate = restTemplate;
            this.loadBalancerClient = loadBalancerClient;
        }
    
        
        @HystrixCommand(fallbackMethod = "findByIdFallback")
        @GetMapping("/user/{id}")
        public User findById(@PathVariable Long id){
            return this.restTemplate.getForObject("http://hystrix-provider-user/user/" + id, User.class);
        }
        
        public User findByIdFallback(Long id){
            User user = new User();
            user.setId(-1L);
            user.setName("默认用户");
            return user;
        }
    
        @GetMapping("/log-user-instance")
        public void logUserInstance(){
            ServiceInstance serviceInstance = this.loadBalancerClient.choose("ribbon-provider-user");
    
            MovieController.LOGGER.info("{}:{}:{}",serviceInstance.getServiceId(),serviceInstance.getHost(),serviceInstance.getPort());
        }
    }
    ~~~
    `@HystrixCommand` 的配置非常灵活，可使用属性 commandProperties 和 属性 threadProperties 配置注解 `@HystrixProperty` 。例如：
    ~~~java
    @HystrixCommand(fallbackMethod = "findByIdFallback", 
            commandProperties = {
                @HystrixProperty(name="execution.isolation.thread.timeoutInMilliseconds",value = "5000"),
                @HystrixProperty(name="metrics.rollingStats.timeInMilliseconds",value = "10000")
            },
            threadPoolProperties = {
                @HystrixProperty(name="coreSize",value = "1"),
                @HystrixProperty(name="maxQueueSize",value = "10")
            })
        @GetMapping("/user/{id}")
        public User findById(@PathVariable Long id){
        //...
        }
    ~~~
 5. 测试
    1. 启动 `hystrix-eureka-server`
    2. 启动 `hystrix-provider-user`
    3. 启动 `hystrix-consumer-movie`
    4. 访问 *http://localhost:8080/user/1*,获得结果:
        ~~~json
        {
           "id":1,
           "username":"account1",
           "name":"张三",
           "age":20,
           "balance":100.00
        }
        ~~~
    5. 停止 `hystrix-provider-user`,再次访问*http://localhost:8080/user/1*，可获得结果：
        ~~~json
        {
           "id":-1,
           "username":null,
           "name":"默认用户",
           "age":null,
           "balance":null
        }
        ~~~
 ## 拓展
 
 1. 在很多场景下，我们需要获得回退的原因，只需要在 fallback 方法上加一个 Throwable 参数即可
 2. 多数场景下，当发生业务异常时，我们并不想触发 fallback 。此时怎么办呢？ Hystrix 有个 HystrixBadRequestException
    类，这是一个特殊的异常类，当该异常发生时，不会出发回退，因此，可将自定义的业务异常继承该类，从而达到业务异常不回退的效果。
    
 3. 另外， @HystrixCommand 为我们提供了 ignoreException 的属性，也可借助该属性配置不想执行回退的异常类。例如
    ~~~java
    @HystrixCommand(fallbackMethod = "findByIdFallback",ignoreExceptions = {IllegalArgumentException.class})
    @GetMapping("/user/{id}")
    public User findById(@PathVariable Long id){
        return this.userService.findById(id);
    }
    ~~~
 4. Hystrix 线程隔离策略
    Hystrix 的隔离策略有两种：
    - THREAD：线程隔离，使用该方式，HystrixCommand 将在独立的线程上执行，并发请求将受到线程池中的线程数量的限制
    - SEMAPHORE：信号量隔离。使用该方式，HystrixCommand 将在调用线程上执行，开销相对较小，并发请求受到信号量个数的限制。
    
    可在 HystrixCommand 使用`execution.isolation.strategy`属性指定隔离策略，一般来说，只有当调用负载非常高时（例如每个实例
    每秒调用上百次）才需要使用信号量隔离，因为这种场景下使用 THREAD 开销会比较高。信号量隔离一般只适用非网络调用的隔离。
 5. 传播上下文
    如果你想传播线程本地的上下文到 @HystrixCommand, 默认声明不会工作，因为它会在线程池中执行命令(在超时的情况下)。你可以使用
    一些配置，让 Hystrix 使用相同的线程，或者再注解中让 Hystrix 使用不同的隔离策略。例如：
    ~~~java
    @HystrixCommand(fallbackMethod="studyMyService",
        commandProperties = {
        @HystrixProperty(name="execution.isolation.strategy",value="SEMAPHORE")
    })
    ~~~
    这也适用于使用 @SessionScope 或者 @RequestSession 的情况。你会知道什么时候需要这样做，因为会发生一个运行时异常，说它找不到作用域上下文。
    
    你还可以将 hystrix.shareSecurityContext 属性设置为 true,这样将自动配置一个 Hystrix 并发策略插件的 hook,这个 hook 会将 SecurityContext 
    从主线程传输到 Hystrix 的命令。因为 Hystrix 不允许注册多个并发策略。所以可以声明 HystrixConcurrencyStrategy 为一个 Spring Bean 来实现
    拓展。spring cloud 会在 Spring 的上下文中查找你的实现，并将其包装在自己的插件中。
    
 总结如下：
 - Hystrix 的隔离策略有 THREAD 和 SEMAPHORE 两种，默认是 THREAD，正常情况下保持默认即可
 - 如果发生找不到上下文的运行时异常，可考虑将隔离策略设置为 SEMAPHORE。