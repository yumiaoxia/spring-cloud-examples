# 整合 Feign
### 改造
1. 复制 `consumer-movie`模块，ArtifactId改名: feign-consumer-movie-simple; 配置文件 yaml 修改`spring-application.name=feign-consumer-movie`

   复制 `provider-user`模块,ArtifactId改名:feign-provider-user-simple; 配置文件 yaml 修改`spring-application.name=feign-provider-user`
   
   复制 `eureka-server`模块,ArtifactId改名:feign-eureka-server-simple
   
   如果使用IDEA集成环境，点击`Project Structure>modules`将三个模块加入到项目模块中; 在编辑器右侧工具栏 maven，把模块的pom 文件添加到maven管理，可修改模块根目录名与模块名一直(非必要)
   
2. 添加依赖
    ~~~xml
    <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-openfeign</artifactId>
    </dependency>
    ~~~
3. 创建一个在基包下面创建一个子包 feignclient和一个Feign接口。并添加@FeignClient注解
    ~~~java
    @FeignClient(name = "feign-provider-user")
    public interface UserFeignClient {
    
        @GetMapping("/user/{id}")
        public User findById(@PathVariable("id") Long id);
    }
    ~~~
    @GetMapping 的 value 属性指定的是服务提供者的接口路径，必须与调用接口一致
    
    可以使用url属性指定请求的URL(URL可以是完整的URL或者主机名)，例如
    `@FeignClient(name="feign-provider-user",url="http://localhost:8010/")`
4. 修改 Controller 代码，使其调用 Feign 接口
    ~~~java
    @RestController
    public class MovieController {
    
       private final UserFeignClient userFeignClient;
    
        public MovieController(UserFeignClient userFeignClient) {
            this.userFeignClient = userFeignClient;
        }
    
        @GetMapping("/user/{id}")
        public User findById(@PathVariable Long id){
            return this.userFeignClient.findById(id);
        }
    }
    ~~~
### 测试

1. 启动 feign-eureka-server-simple
2. maven打包，然后命令行启动 2 个或更多 feign-provider-user-simple 实例
    ~~~shell
    java -jar 包名 --server.port=8010
    java -jar 包名 --server.port=8020
    ~~~
3. 启动 feign-consumer-movie-simple
4. 多次访问 *http://localhost:8080/user/1*,返回以下结果：
    ~~~json
   {"id":1,"username":"account1","name":"寮犱笁","age":20,"balance":100.00}
    ~~~
