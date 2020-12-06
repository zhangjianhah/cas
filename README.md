# cas-server 配置手册

## 1.初始下载配置

1. 前往<https://github.com/apereo/cas-overlay-template>下载指定版本，cas6以后都采用gradle管理，所以可以采用低版本的5.3版本。

2. 然后会得到一个cas-overlay-template-5.3文件夹，用idea打开后，其中的插件会自动下载overlays的文件夹，里面包含着cas的核心内容。**这里目录内其实是一个完整的cas服务，我们引入的目的是为了在某些地方改写替换这个webapp**

3. 在cas-overlay-template-5.3中创建src，并且在src\main中创建java、resource、webapp等目录。（这里的webapp是因为本项目默认有两个webapp，但是我们的自定义修改静态资源、jsp都放在这里）。

4. 将overlays\org.apereo.cas.cas-server-webapp-tomcat-5.3.16\WEB-INF\classes下的application.properties复制到我们自定义的resource文件夹下面，同时添加**```cas.tgc.secure=false```**这个一定要添加，不然无法生成TGC。

   原因是：cas5以后，默认cas有安全性检查，在非SSL下，cas server无法获取TGC的cookie，这样会导致cas server无法校验是否登录。当然，如果采用SSL，那么不必修改。

5. 此时配置tomcat即可启动。默认的启动登录地址是<http://localhost:8443/cas/login> .默认采用清单的方式记录可登录用户，即保存在```cas.authn.accept.users```中，这是个list，多以可以保存多个。

6. 注意：

   因为我个人使用的jdk11 版本的，而cas-server原生带的**common-lang3**版本较低（为3.7），与JDK11不匹配所以需要在项目中引入

   ```xml
   <!-- https://mvnrepository.com/artifact/org.apache.commons/commons-lang3 -->
                   <dependency>
                       <groupId>org.apache.commons</groupId>
                       <artifactId>commons-lang3</artifactId>
                       <version>3.11</version>
                   </dependency>
   ```

   

## 自定义登录校验

所有的登录校验需要实现```AbstractUsernamePasswordAuthenticationHandler```抽象类，其默认的清单方式和数据库方式即是如此。  

参考```com.zj.cas.config``` 下的```MyAuthenticationHandler```,```MyAuthenticationConfiguration```,```DBConfiguration```即可。注意，cas采用了spring.factories的方式，用来注册所有的配置类，按照已有的格式将两个配置类```MyAuthenticationConfiguration```,```DBConfiguration```加入即可。

这个spring.factories位置为``cas-server\overlays\org.apereo.cas.cas-server-webapp-tomcat-5.3.16\WEB-INF\classes\META-INF``中，推荐将整个META-INF复制到resources中。





## 兼容http的客户端

1. 开启服务配置

   ```properties
   # Json services 配置位置设定
   cas.serviceRegistry.json.location=classpath:/services
   
   
   #开启识别json文件，默认false
   cas.serviceRegistry.initFromJson=true
   ```

   

2. 配置正则校验，控制被允许的服务

   cas支持配置是否允许客户端服务连入，具体配置就是在**overlays/overlays\org.apereo.cas.cas-server-webapp-tomcat-5.3.16\WEB-INF\classes\services**中添加json文件，一个客户端服务一个配置文件。

   以下举例说明：

   ```json
   {
     "@class": "org.apereo.cas.services.RegexRegisteredService",
     "serviceId": "^(http)://localhost:9003/client3.*",
     "name": "本地服务3",
     "id": 10000003,
     "description": "这是本地client3服务，serviceId即是URL规则",
     "evaluationOrder": 1,
     "logoutUrl": "http://localhost:9003/client3/logout"
   }
   ```
   
   - @class：默认值，不需要改变。
   - serviceId：这是一个正则表达式，配置可以连入的服务url
   - name：服务名称
   - id：客户端服务ID，每个服务不同。
- description：客服端描述
   - evaluationOrder：执行顺序，可以相同。
   - logoutUrl：当退出cas的是否，已登录的服务会回调这个地址去退出。这个地址是用来做一些自己的操作的，cas本身的退出会走过滤器单点时候完成。
   
   ## cas单点退出
   
   cas支持单点退出，其基本原理是，cas会记录所有的服务的list，当某一台服务发出退出请求后，会找到这个票据所有注册的服务（所有的服务用json格式，在service中注明，并写好logoutUrl的值），
   casserver会利用该值进行退出操作。具体代码在```DefaultSingleLogoutServiceMessageHandler```中。
   casclient监听器会监听本请求，并注销该服务。
   
   



# 2.搭建cas-client客户端（Spring Boot）

与cas-server不同的是，cas-client并没有指定tomcat版本和类型，我们可以使用Spring Boot进行配置。

1. 添加依赖

   ```xml
   <dependency>
               <groupId>net.unicon.cas</groupId>
               <artifactId>cas-client-autoconfig-support</artifactId>
               <version>2.1.0-GA</version>
           </dependency>
   ```

   

2. 在启动类这种添加```@EnableCasClient```注解

3. 配置需要访问的cas服务地址

   ```properties
   cas:
     # cas服务端的地址
     server-url-prefix: http://localhost:8443/cas
     # cas服务端的登录地址
     server-login-url: http://localhost:8443/cas/login
     # 当前服务器的地址(客户端)
     client-host-url: http://localhost:8081
     # Ticket校验器使用Cas30ProxyReceivingTicketValidationFilter
     validation-type: cas3
   ```

   

cas已经替我们解决跨域问题，所以不需要使用反向代理控制端口也可以直接使用。



# 3.cas server的集群

这里需要注意的是，即使cas server的端口不一样，他们的session和ticket也是共享单点。

1. 引入相关依赖,cas自己基于自身定义了一些托管方案，需要引用其定制版本的jar包。

   这里的两个依赖，一个是为了实现ticket共享，后者是为了实现session共享。

   ```xml
                  <dependency>
                       <groupId>org.apereo.cas</groupId>
                       <artifactId>cas-server-support-redis-ticket-registry</artifactId>
                       <version>${cas.version}</version>
                   </dependency>
   
                   <dependency>
                       <groupId>org.apereo.cas</groupId>
                       <artifactId>cas-server-webapp-session-redis</artifactId>
                       <version>${cas.version}</version>
                   </dependency>
   ```

   

2. 在application.properties中添加以下配置

   ```properties
   #---------------cas 集群配置开始-------------------------------
   #配置redis存储ticket
   cas.ticket.registry.redis.host=127.0.0.1
   cas.ticket.registry.redis.database=0
   cas.ticket.registry.redis.port=6379
   #cas.ticket.registry.redis.password=wsc123456
   cas.ticket.registry.redis.timeout=2000
   cas.ticket.registry.redis.useSsl=false
   cas.ticket.registry.redis.usePool=true
   cas.ticket.registry.redis.pool.max-active=20
   cas.ticket.registry.redis.pool.maxIdle=8
   cas.ticket.registry.redis.pool.minIdle=0
   cas.ticket.registry.redis.pool.maxActive=8
   cas.ticket.registry.redis.pool.maxWait=-1
   cas.ticket.registry.redis.pool.numTestsPerEvictionRun=0
   cas.ticket.registry.redis.pool.softMinEvictableIdleTimeMillis=0
   cas.ticket.registry.redis.pool.minEvictableIdleTimeMillis=0
   cas.ticket.registry.redis.pool.lifo=true
   cas.ticket.registry.redis.pool.fairness=false
   cas.ticket.registry.redis.pool.testOnCreate=false
   cas.ticket.registry.redis.pool.testOnBorrow=false
   cas.ticket.registry.redis.pool.testOnReturn=false
   cas.ticket.registry.redis.pool.testWhileIdle=false
   #cas.ticket.registry.redis.sentinel.master=mymaster
   #cas.ticket.registry.redis.sentinel.nodes[0]=localhost:26377
   #cas.ticket.registry.redis.sentinel.nodes[1]=localhost:26378
   #cas.ticket.registry.redis.sentinel.nodes[2]=localhost:26379
   
   
   #配置redis存储session
   cas.webflow.autoconfigure=true
   cas.webflow.alwaysPauseRedirect=false
   cas.webflow.refresh=true
   cas.webflow.redirectSameState=false
   cas.webflow.session.lockTimeout=30
   cas.webflow.session.compress=false
   cas.webflow.session.maxConversations=5
   cas.webflow.session.storage=true
   spring.session.store-type=redis
   spring.redis.host=127.0.0.1
   #spring.redis.password=wsc123456
   spring.redis.port=6379
   
   #或者加上这个配置也可以，encryptionKey和signingKey要给他一个默认值，要不然他会自己随机生成一个，多台server的加密串就不一样了
   #配置cas server的集群后，需要处理其本身的加密问题，一共有两种方案，
   # 1.自定义加密串（如果不定义其会自动生成，这会导致多个server的加密串不一致，彼此不能解析）
   #2.取消cas的tgc加密
   #两种任意选一种即可。但是第一种中encryption的长度必须为32，signing的长度必须为64
   #cas.tgc.crypto.encryption.key=nXL-DN7Xbt3HsY_Wwp9zVAUP-r4sFkZOIfAstCfpCXI
   #cas.tgc.crypto.signing.key=tushengN7Xbt3HsY_Wwp9zVAUP-r4sFkZOIfAstCfpCXItushengN7Xbt3HsY_Wwp9zVAUP-r4sFkZOIfAstCfpCXI
   cas.tgc.crypto.enabled=false
   ```

   至此，集群配置完成。