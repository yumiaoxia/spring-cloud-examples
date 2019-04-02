# 对于Feign,通过 Fallback Factory 检查回退原因
## 改造过程
 1. 复制 simple 的 consumer-movie 模块,修改模块名，引入 Feign 的依赖，将该模块加入到项目模块管理和maven管理中
 2. 在 yaml文件中添加feign.hystrix.enabled: true 的配置，从而开启 Feign 的 Hystrix 的支持
 3. FeignClient 接口改造如下
    ~~~java
    @FeignClient(name = "hystrix-provider-user",fallbackFactory = FeignClientFallbackFactory.class)
           public interface UserFeignClient {
               
               @GetMapping("/user/{id}")
               public User findById(@PathVariable("id") Long id);
           }
    ~~~
 4. 编写 FallbackFactory 接口
    ~~~java
    public class FeignClientFallbackFactory implements FallbackFactory<UserFeignClient>{
        private static final Logger LOGGER = LoggerFactory.getLogger(FeignClientFallbackFactory.class);
    
        @Override
        public UserFeignClient create(Throwable cause) {
            return new UserFeignClient() {
                @Override
                public User findById(Long id) {
                    LOGGER.info("fallback;reason was:",cause);
                    User user = new User();
                    user.setId(-1L);
                    user.setUsername("默认用户");
                    return user;
                }
            };
        }
    }
    ~~~
 5.测试

## Feign禁用 Hystrix
### 借助 Feign的自定义配置
 1. 指定名称的 Feign 客户端禁用 Hystrix。
    ~~~java
    @Configuration
    public class FeignDisableHystrixConfiguration{
        @Bean
        @Scope("prototype")
        public Feign.Builder feignBuilder(){
        return Feign.builder();
        }
    }
    ~~~
    想要禁用 Hystrix 的 @FeignClient,引用该配置类即可
    ~~~java
    @FeignClient(name = "user",configuration = FeignDisableHystrixConfiguration.class)
    public interface UserFeignClient{
    //...
    }
    ~~~
### 全局禁用 Hystrix 
 在全局配置文件 yaml 中配置 feign.hystrix.enabled = false 即可