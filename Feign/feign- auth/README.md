# 手动创建Feign

## 实现目标场景
 - 用户为服务的接口需要登陆之后才能调用，并且对于相同的API，不同的角色有不同的行为
 - 让电影微服务中的同一个 Feign 接口使用不同的账号登录，并调用用户为服务的接口
 
 ## 一.改造用户微服务
 1. 复制simple中的 `provider-user` 模块，将artifactId修改为：feign-provider-user-auth。并把该模块加入到项目模块和maven管理起来，修改模块根目录名
 2. 为项目添加依赖
     ~~~xml
     <dependency>
         <groupId>org.springframework.boot</groupId>
         <artifactId>spring-boot-starter-security</artifactId>
     </dependency>
     ~~~
 3. 创建 Spring Security 的配置类
    ~~~java
    @Configuration
    @EnableWebSecurity
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    public class WebsecurityConfig extends WebSecurityConfigurerAdapter {
    
        @Autowired
        private CustomUserDetailsService customUserDetailsService;
    
        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.userDetailsService(this.customUserDetailsService).passwordEncoder(passwordEncoder());
        }
    
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.authorizeRequests()
                    .anyRequest()
                    .authenticated()
                    .and()
                    .httpBasic();
        }
    
        @Bean
        public PasswordEncoder passwordEncoder(){
            return NoOpPasswordEncoder.getInstance();
        }
    }
    ~~~
    ~~~java
    @Component
    public class CustomUserDetailsService implements UserDetailsService {
    
        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            if("user".equals(username)){
                return new SecurityUser(username,"123456","user-role");
            }else if("admin".equals(username)){
                return new SecurityUser(username,"1234","admin-role");
            }else{
                return null;
            }
        }
    }
    ~~~
    ~~~java
    public class SecurityUser implements UserDetails {
    
        private Long id;
        private String username;
        private String password;
        private String role;
    
        public SecurityUser(String username,String password,String role){
            super();
            this.username = username;
            this.password = password;
            this.role = role;
        }
    
    
        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority(this.role);
            authorities.add(authority);
            return authorities;
        }
    
        @Override
        public String getPassword() {
            return password;
        }
    
        @Override
        public String getUsername() {
            return username;
        }
    
        @Override
        public boolean isAccountNonExpired() {
            return true;
        }
    
        @Override
        public boolean isAccountNonLocked() {
            return true;
        }
    
        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }
    
        @Override
        public boolean isEnabled() {
            return true;
        }
    }
    ~~~
 4. 修改 Controller,在其中打印当前登录用户的信息
    ~~~java
    @RestController
    public class UserController {
    
        private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
    
        private final UserRepository userRepository;
    
        public UserController(UserRepository userRepository) {
            this.userRepository = userRepository;
        }
    
        @GetMapping("/user/{id}")
        public User findById(@PathVariable Long id) {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if(principal instanceof UserDetails){
                UserDetails user = (UserDetails)principal;
                Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
                for (GrantedAuthority authority : authorities) {
                    LOGGER.info("当前用户是{}，角色是{}",user.getUsername(),authority.getAuthority());
                }
            }else{
                // do other thing...
            }
            return userRepository.findOne(id);
    
        }
    }
    ~~~  
 5. 可以启动`eureka-server`,`provider-user`，访问接口，查看日志信息
 
## 二. 改造电影微服务
 1. 复制simple中的 `comsumer-movie` 模块，将artifactId修改为：feign-consumer-movie-auth。并把该模块加入到项目模块和maven管理起来，修改模块根目录名
 2. 去掉 Feign 接口 UserFeignClient 上的 `@FeignClient` 注解
 3. 去掉启动类上的 `@EnableFeignClients` 注解。
 4. 修改 MovieController 如下：
    ~~~java
    @Import(FeignClientsConfiguration.class)
    @RestController
    public class MovieController {
    
        private UserFeignClient userUserFeignClient;
    
        private UserFeignClient adminUserFeignClient;
    
        @Autowired
        public MovieController(Decoder decoder, Encoder encoder, Client client, Contract contract) {
            this.userUserFeignClient = Feign.builder()
                    .client(client)
                    .encoder(encoder)
                    .decoder(decoder)
                    .contract(contract)
                    .requestInterceptor(new BasicAuthRequestInterceptor("user","123456"))
                    .target(UserFeignClient.class,"http://feign-provider-user/");
    
            this.adminUserFeignClient = Feign.builder()
                    .client(client)
                    .encoder(encoder)
                    .decoder(decoder)
                    .contract(contract)
                    .requestInterceptor(new BasicAuthRequestInterceptor("admin","1234"))
                    .target(UserFeignClient.class,"http://feign-provider-user/");
        }
    
        @GetMapping("/user-user/{id}")
        public User findByIdUser(@PathVariable Long id) {
            return this.userUserFeignClient.findById(id);
        }
    
        @GetMapping("/user-admin/{id}")
        public User findByIdAdmin(@PathVariable Long id){
            return this.adminUserFeignClient.findById(id);
        }
    
    }
    ~~~
    其中，@Import 导入的 `FeignClientsConfiguration` 是Spring Cloud 为 Feign 默认提供的配置类。
 5. 启动测试
    1. 启动 simple 的 `eureka-server`
    2. 启动本项目的 `provider-user`
    3. 启动本项目的 `consumer-movie`
    4. 访问 *http://localhost:8080/user-user/1*，查看日志信息
    5. 访问 *http://localhost:8080/user-admin/1*,查看日志信息