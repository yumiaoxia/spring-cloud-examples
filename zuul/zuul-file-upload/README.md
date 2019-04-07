title: 使用 Zuul 上传文件

对于小文件（1M以内）上传，无需任何处理，即可正常上传。对于大文件(10M以上)上传，需要为
上传路径添加 `/zuul` 前缀。也可使用 `zuul.servlet-path` 自定义前缀

如果 Zuul 使用了 Ribbon 做负载均衡，那么对于超大的文件（例如500M），需要提升超时设置，如：
~~~yaml
hystrix.command.default.execution.isolution.thread.timeoutInMillisecons: 6000
ribbon:
    ConnectTimeout: 3000
    ReadTimeOut: 60000
~~~

## 构建上传文件的微服务
 1. 创建一个 Maven 模块，名为 zuul-file-upload，Intellij idea 的方式是通过工具栏的 `project structure>modules>new modules`,并为项目添加如下依赖
 ~~~xml
 <dependencies>
     <dependency>
         <groupId>org.springframework.boot</groupId>
         <artifactId>spring-boot-starter-web</artifactId>
     </dependency>
     <dependency>
         <groupId>org.springframework.cloud</groupId>
         <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
     </dependency>
     <dependency>
         <groupId>org.springframework.boot</groupId>
         <artifactId>spring-boot-starter-actuator</artifactId>
     </dependency>
 </dependencies>
 ~~~
 2.编写启动类
 3.编写 Controller
 ~~~java
 @RestController
 public class FileUploadController {
 
     @PostMapping("/upload")
     public String handleFileUpload(@RequestParam(value="file")MultipartFile file) throws IOException{
         byte[] bytes = file.getBytes();
         File fileToSave = new File(file.getOriginalFilename());
         FileCopyUtils.copy(bytes,fileToSave);
         return  fileToSave.getAbsolutePath();
     }
 }
 ~~~
 4.配置文件 application.yaml，添加如下内容：
 ~~~yaml
 server:
   port: 8050
 eureka:
   client:
     serviceUrl:
       defaultZone: http://localhost:8761/eureka/
   instance:
     prefer-ip-address: true
 
 spring:
   application:
     name: zuul-file-upload
   http:
     multipart:
       max-file-size: 2000Mb
       max-request-size: 2500Mb
 ~~~
 ## 测试：
 ### 测试1
 
   1. 启动 zuul-simple 的 zuul-eureka-server-simple，zuul-service
   2. 启动 zuul-file-upload
   3. 打开 命令行工具，使用如下命令上传一个小文件
   ~~~sbtshell
   curl -F "file=@C:\Users\sherman\Pictures\apple_favicon.jpg" localhost:8040/zuul-file-upload/upload
   ~~~
   4. 上传成功，会返回一个上传路径
 ### 测试2
 
 在测试1的基础上，添加如下内容
 ~~~yaml
 hystrix.command.default.execution.isolution.thread.timeoutInMillisecons: 6000
 ribbon:
     ConnectTimeout: 3000
     ReadTimeOut: 60000
 ~~~
 使用如下命令，可以上传一个大文件，注意添加 `/zuul`前缀

 ~~~sbtshell
 curl -H "Transfer-Encoding:chunked" -F"file=@F:\nat123_V1.181128_NET4.zip" localhost:8040/zuul/zuul-file-upload/upload
 ~~~
 
 
