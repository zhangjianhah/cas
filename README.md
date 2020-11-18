# cas-server 配置手册

## 初始下载配置

1. 前往<https://github.com/apereo/cas-overlay-template>下载指定版本，cas6以后都采用gradle管理，所以可以采用低版本的5.3版本。

2. 然后会得到一个cas-overlay-template-5.3文件夹，用idea打开后，其中的插件会自动下载overlays的文件夹，里面包含着cas的核心内容。**这里目录内其实是一个完整的cas服务，我们引入的目的是为了在某些地方改写替换这个webapp**

3. 在cas-overlay-template-5.3中创建src，并且在src\main中创建java、resource、webapp等目录。（这里的webapp是因为本项目默认有两个webapp，但是我们的自定义修改静态资源、jsp都放在这里）。

4. 将overlays\org.apereo.cas.cas-server-webapp-tomcat-5.3.16\WEB-INF\classes下的application.properties复制到我们自定义的resource文件夹下面，同时添加```cas.tgc.secure=false```这个一定要添加，不然无法生成TGC。

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

参考```com.zj.cas.config``` 下的```MyAuthenticationHandler```,```MyAuthenticationConfiguration```,```DBConfiguration```即可。注意，cas采用了spring.factories的方式，用来注册所有的配置类，按照已有的格式将两个配置类```MyAuthenticationConfiguration```,```DBConfiguration```加入即可。、





## 兼容http的客户端

1. 开启服务配置

   ```properties
   # Json services 配置位置设定
   cas.serviceRegistry.json.location=classpath:/services
   
   
   #开启识别json文件，默认false
   cas.serviceRegistry.initFromJson=true
   ```

   

2. 配置正则校验，控制被允许的服务

   在overlays/overlays\org.apereo.cas.cas-server-webapp-tomcat-5.3.16\WEB-INF\classes\services中，打开HTTPSandIMAPS-10000001.json文件，将serviceId中添加```|http```

   这里的配置是为了让cas-server能够识别并管理以下路径的客户端服务。整体如下

   ```json
   {
     "@class" : "org.apereo.cas.services.RegexRegisteredService",
     "serviceId" : "^(https|imaps|http)://.*",
     "name" : "HTTPS and IMAPS",
     "id" : 10000001,
     "description" : "This service definition authorizes all application urls that support HTTPS and IMAPS protocols.",
     "evaluationOrder" : 10000
   }
   
   ```
   
   ## cas单点退出
   cas支持单点退出，其基本原理是，cas会记录所有的服务的list，当某一台服务发出退出请求后，会找到这个票据所有注册的服务（所有的服务用json格式，在service中注明，并写好logoutUrl的值），
   casserver会利用该值进行退出操作。具体代码在```DefaultSingleLogoutServiceMessageHandler```中。
   casclient监听器会监听本请求，并注销该服务。

   



# 搭建cas-client客户端（Spring Boot）

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