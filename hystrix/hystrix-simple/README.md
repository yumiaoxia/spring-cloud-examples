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
    