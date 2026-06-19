# JAgent — Java Instrumentation Agent 框架

## 使用方式

* 从 [releases](https://gitee.com/ja-netfilter/ja-netfilter/releases) 页面下载
* 添加 `-javaagent:/absolute/path/to/JAgent.jar` JVM 参数（**替换为你的实际路径**）

  * 作为 `java` 命令的参数添加，如：`java -javaagent:/absolute/path/to/JAgent.jar -jar executable_jar_file.jar`
  * 部分应用支持 `JVM Options file`，可在文件中添加一行
  * **注意：不要包含多余的空格字符！**
  * 编辑插件配置文件：在 `JAgent.jar` 所在目录的 `conf` 目录下，创建 `${插件名小写}.conf` 文件
  * `config`、`logs` 和 `plugins` 目录可通过 **javaagent 参数** 指定

  * 如：`-javaagent:/path/to/JAgent.jar=appName`，则配置、日志和插件目录将分别为 `config-appname`、`logs-appname` 和 `plugins-appname`
  * 若未指定 javaagent 参数，默认目录为 `conf`、`logs` 和 `plugins`
  * 此机制可避免不同应用共用同一套配置目录
* 启动你的 Java 应用即可

## 配置文件格式

```
[ABC]
# 指定分区名称

# 示例
[URL]
EQUAL,https://someurl

[DNS]
EQUAL,somedomain

# EQUAL       使用 `equals` 比较
# EQUAL_IC    使用 `equals` 比较，忽略大小写
# KEYWORD     使用 `contains` 比较
# KEYWORD_IC  使用 `contains` 比较，忽略大小写
# PREFIX      使用 `startsWith` 比较
# PREFIX_IC   使用 `startsWith` 比较，忽略大小写
# SUFFIX      使用 `endsWith` 比较
# SUFFIX_IC   使用 `endsWith` 比较，忽略大小写
# REGEXP      使用正则表达式匹配
```

## 调试日志

* `JAgent` 默认**不会**输出调试信息
* 添加环境变量 `JANF_DEBUG=1`（日志级别）并启动即可启用
* 或添加系统属性 `-Djanf.debug=1`（日志级别）启用
* 日志级别：`NONE=0`、`DEBUG=1`、`INFO=2`、`WARN=3`、`ERROR=4`

## 调试输出

* `JAgent` 默认将调试信息输出到控制台
* 添加环境变量 `JANF_OUTPUT=value` 可改变输出方式
* 或添加系统属性 `-Djanf.output=value` 改变输出方式
* 输出方式值：`NONE=0`、`CONSOLE=1`、`FILE=2`、`CONSOLE+FILE=3`、`WITH_PID=4`
* 如要同时输出到控制台和文件并附加 PID，值为 1 + 2 + 4 = 7，即 `-Djanf.output=7`

## 插件系统

* 开发者：

  * 查看插件系统的 [脚手架项目](https://gitee.com/ja-netfilter/ja-netfilter-sample-plugin)
  * 编译插件并发布
  * 发挥你的创意~
* 用户：

  * 下载插件的 JAR 文件
  * 放入 `JAgent.jar` 所在目录下的 `plugins` 子目录
  * 享受插件带来的新能力
  * 若文件后缀为 `.disabled.jar`，该插件将被禁用
