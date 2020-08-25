
[![Maven Central](https://img.shields.io/maven-central/v/org.noear/solon.svg)](https://mvnrepository.com/search?q=g:org.noear%20AND%20solon)

` QQ交流群：22200020 （同时招募项目参与人员：开发者，测试者，网官设计师等...）` 

# solon for java

一个插件式微型Web框架。

支持jdk8+，主框架90kb。组合不同的插件应对不同需求。

* 采用Handler + Context 架构
* 实现IOC & AOP容器，支持MVC
* 支持Http（Serverlet 或 非Serverlet），WebSocket，Socket信号接入
* 插件可扩展可切换：启动插件，扩展插件，序列化插件，会话状态插件，视图插件(可共存) 等...

### Hello world：

```java
//Handler 模式：
public class App{
    public static void main(String[] args){
        XApp app = XApp.start(App.class,args);
        
        app.get("/",(c)->c.output("Hello world!"));
    }
}

//Controller 模式：
@XController
public class App{
    public static void main(String[] args){
        XApp.start(App.class,args);
    }
  
    @XMapping("/")
    public Object home(XContext c){
        return "Hello world!";  
    }
}
```


### 主框架与插件：

###### 主框架

| 组件 | 说明 |
| --- | --- |
| org.noear:solon-parent | 框架版本管理 |
| org.noear:solon | 主框架 |

###### 快速集成开发包

| 组件 | 说明 |
| --- | --- |
| org.noear:solon-web | 可进行http api,mvc,rpc开发的快速集成包 |



### 附1：入门示例
* Web 示例（aop,mvc,rpc）
```xml
<parent>
    <groupId>org.noear</groupId>
    <artifactId>solon-parent</artifactId>
    <version>1.0.16</version>
</parent>

<dependencies>
    <dependency>
        <groupId>org.noear</groupId>
        <artifactId>solon-web</artifactId>
        <type>pom</type>
    </dependency>
</dependencies>

```
```
//资源路径说明（不用配置）
resources/application.properties（或 application.yml） 为应用配置文件
resources/static/ 为静态文件根目标
resources/WEB-INF/view/ 为视图文件根目标（支持多视图共存）

//调试模式：
启动参数添加：-deubg=1
```
```java
public class App{
    public static void main(String[] args){
        XApp.start(App.class, args);
    }
}

/*
 * mvc控制器
 */
@XController
public class DemoController{
    //for http
    @XMapping("/hallo/{u_u}")
    public ModelAndView hallo(String u_u){
        return new ModelAndView("hallo");
    }
    
    /*
    //for web socket （需添加：solon.boot.websocket 插件）
    @XMapping(value="/hallo/{u_u}", method = XMethod.SEND)
    public ModelAndView hallo_ws(String u_u){
        return new ModelAndView("hallo");
    }
    */
}

/*
 * rpc服务
 */ 
// - interface
@XClient("rpc:/demo/") // 或 demorpc （使用water提供的注册服务；当然也可以改成别的...）
public interface DemoRpc{
    void setName(Integer user_id, String name);
}

// - server
@XMapping("/demo/*")
@XBean(remoting = true)
public class DemoService implements DemoRpc{
    public void setName(Integer user_id, String name){
        
    }
}

// - client - 简单示例
DemoRpc client = new XProxy().upstream(n->"http://127.0.0.1").create(DemoRpc.class); 
client.setName(1,'');
```
* 获取应用配置
```java
//非注入模式
XApp.cfg().get("app_key"); //=>String
XApp.cfg().getInt("app_id",0); //=>int
XApp.cfg().getProp("xxx.datasource"); //=>Properties

//注入模式
@XConfiguration //or @XController, or @XBean
class xxx{
    @XInject("${app_key}")
    String app_key;
}
```

### 附2：更多示例可参考 _test 和 _demo

### 附3：插件开发说明
* 新建一个 maven 项目
* 新建一个 java/{包名}/XPluginImp.java （implements XPlugin）
* 新建一个 resources/`solonplugin`/{包名.properties}
*    添加配置：solon.plugin={包名}.XPluginImp

### 附4：启动顺序参考
* 1.实例化 XApp.global() 并加载配置
* 2.加载扩展文件夹
* 3.扫描插件
* 4.运行builder函数
* 5.运行插件
* 6.扫描并加载java bean
* 7.加载渲染关系
* 8.完成
