title: Ribbon 的基本实现
----
### 改造：
基于Eureka的simple工程
1. 确定消费端引入了Ribbon的依赖
    ~~~xml
    <dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-ribbon</artifactId>
    </dependency>
    ~~~
    注意，Eureka-simple工程已经引入Netfilx-client,其已经依赖了Ribbon
2. 为 RestTemplate 添加 **@LoadBalanced** 注解
    ~~~java
    @Bean
        @LoadBalanced
        public RestTemplate restTemplate(){
            return  new RestTemplate();
        }
    ~~~
 3. 对Controller代码进行修改
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
    
        @GetMapping("/user/{id}")
        public User findById(@PathVariable Long id){
            return this.restTemplate.getForObject("http://ribbon-provider-user-simple/user/"+id,User.class);
        }
    
        @GetMapping("/log-user-instance")
        public void logUserInstance(){
            ServiceInstance serviceInstance = this.loadBalancerClient.choose("ribbon-provider-user-simple");
    
            MovieController.LOGGER.info("{}:{}:{}",serviceInstance.getServiceId(),serviceInstance.getHost(),serviceInstance.getPort());
        }
    }
    ~~~ 
4. provider-user 配置不同环境的profiles环境
    ~~~yaml
    spring:
       profiles:
           active: provider2
    ---
    spring:
      profiles: provider1
    server:
      port: 8050
    ---
    spring:
      profiles: provider2
    server:
      port: 8060 
    ~~~
### 测试
1. 开启服务发现注册组件服务端
2. 在 IDEA 环境启动两个provider实例
    启动方式：修改provider-user模块指定profiles环境，的在 IDEA 集成环境中使用 Terminal 工具
    打开 pom 文件的所在目录，使用如下命令：
    ~~~shell
    mvn spring-boot:run
    ~~~
    测试过程使用 -P 参数指定profiles环境失效，只能启动实例前修改yaml文件
 3. 最后启动 consumer-movie  实例
    注册表看到的效果：
    ![Ribbon测试注册表](http://pooccqcjj.bkt.clouddn.com/ribbon-%E6%B3%A8%E5%86%8C%E8%A1%A8.png)
 4. 在浏览器模拟多次访问consumer-movie的接口，查看日志信息，例如本实例：
    http://localhost:8080/user/1,  http://localhost:8080/log-user-instance：
    