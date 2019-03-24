# 使用 Feign 实现 声明式 REST 调用

## 一. Feign简介
 Feign 是 Netflix 开发的声明式，模板化的 HTTP 客户端，其灵感来自Retrofit、JAXRS-2.0 以及 WebSocket。Feign 可帮助我们更加便捷、优雅地调用 HTTP API。
 
 在Spring Cloud 中，使用 Feign 非常简单——创建一个接口，并在接口上添加一些注解，代码就完成了。Feign 支持多种注解，例如 Feign 自带的注解或者JAX-RX注解等。
 
 Spring Cloud 对 Feign 进行了增强，使 Feign 支持了 Spring MVC 注解，并整合了 Ribbon 和 Eureka，从而让 Feign 的使用更加方便。
 
 ## 二. Feign 对继承的支持
 Feign 支持继承。使用继承，可将一些公共操作分组到一些公共接口中，从而简化 Feign 的开发，下面展示简单的例子：
 
 基础接口：UserService.java
 ~~~java
 public interface UserService{
    
    @GetMapping("/user/{id}")
    User getUser(@PathVariable("id") Long id);
 
 }
 ~~~
 服务提供者Controller:
 ~~~java
 @RestController
 public class UserResource implements Userservice{
    //...
 }
 ~~~
 服务消费者：UserClient.java
 ~~~java
 @FeignClient("users")
 public interface UserClient extends UserService{
    
 }
 ~~~
 ## 三. Feign 对压缩的支持
 在一些场景下，可能需要对请求或响应进行压缩，此时可使用以下属性启用 Feign 的压缩功能。
 ~~~properties
 feign.compression.request.enabled=true
 feign.compression.response.enabled=true
~~~
对于请求的压缩，feign还提供更详细的配置：
~~~properties
feign.compression.request.enabled=true
feign.compression.request.mime-types=text/xml,application/xml,application/json
feign.compression.request.min-request-size=2048
~~~
## 四. Feign 对多参数请求的支持
假设请求参数带有 id 和 username 的参数
 ### 4.1 GET请求
 方法一：
 ~~~java
 @FeignClient(name="feign-provider-user")
 public interface  UserFeignClient{
    
    @GetMapping("/get")
    public User get(@RequestParam("id")Long id,@RequestParam("username")String username);
 }
 ~~~
方法二：
~~~java
 @FeignClient(name="feign-provider-user")
 public interface  UserFeignClient{
    
    @GetMapping("/get")
    public User get(@RequestParam Map<String,Object> paramMap);
 }
 ~~~
 使用 Map 类型作为参数类型，在调用时需构建 map 对象
 ### 4.2 POST请求
 
 ~~~java
  @FeignClient(name="feign-provider-user")
  public interface  UserFeignClient{
     
     @PostMapping("/post")
     public User get(@RequestBody User user);
  }
  ~~~
  
## 五. Feign 对上传文件的支持
在实际应用中，我们可能会使用 Feign 上传文件，Feign 官方提供了子项目 feign-form 支持这个功能，其中实现了上传所需的 Encoder .
 1. 为应用添加 feign-form 的相关依赖
    ~~~xml
    <dependency>
       <groupId>io.github.openfeign.form</groupId>
        <artifactId>feign-form</artifactId>
        <version>3.0.3</version>
    </dependency>
    <dependency>
        <groupId>io.github.openfeign.form</groupId>
        <artifactId>feign-form-spring</artifactId>
        <version>3.0.3</version>
    </dependency>
    ~~~
 2. 编写 Feign Client
    ~~~java
    @FeignClient(name="feign-file-upload",configuration = UploadFeignClient.MultiPartSupportConfig.class)
    public interface UploadFeignClient {
    
    @PostMapping(value = "/upload",produces = {MediaType.APPLICATION_JSON_UTF8_VALUE},consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    String handleFileUpload(@RequestPart(value = "file")MultipartFile file);
        
        class MultiPartSupportConfig {
            @Bean
            public Encoder feignFormEncoder() {
                return new SpringFormEncoder();
            }
        }
    }
    ~~~
