# JAgent — Java Instrumentation Agent 框架

## 使用方式

* 从 [releases](https://github.com/code-crafters-lab/jagent/releases) 页面下载
* 添加 `-javaagent:/absolute/path/to/jagent.jar` JVM 参数（**替换为你的实际路径**）

  * 作为 `java` 命令的参数添加，如：`java -javaagent:/absolute/path/to/jagent.jar -jar executable.jar`
  * 部分应用支持 `JVM Options file`，可在文件中添加一行
  * **注意：不要包含多余的空格字符！**
  * 编辑插件配置文件：在 `jagent.jar` 所在目录的 `conf` 目录下，创建 `${插件名小写}.conf` 文件
  * `conf`、`logs` 和 `plugins` 目录可通过 **javaagent 参数** 指定

  * 如：`-javaagent:/path/to/JAgent.jar=appName`，则配置、日志和插件目录将分别为 `appname/conf`、`appname/logs` 和 `appname/plugins`
  * 若未指定 javaagent 参数，默认目录为 `conf`、`logs` 和 `plugins`
* 启动你的 Java 应用即可

## 插件系统

* 开发者：

  * 查看插件系统的 [脚手架项目](https://gitee.com/ja-netfilter/ja-netfilter-sample-plugin)
  * 编译插件并发布
  * 发挥你的创意~
* 用户：

  * 下载插件的 JAR 文件
  * 放入 `jagent.jar` 所在目录下的 `plugins` 子目录
  * 享受插件带来的新能力
  * 若文件后缀为 `.disabled.jar`，该插件将被禁用
