# activemq-auth
activemq 自定义鉴权，支持topic和登录，数据由mysql存储，启动加载redis

使用说明，将代码打包放到activemq/lib下，同时需要增加
commons-dbcp-1.4.jar
commons-pool-1.5.4.jar
commons-pool2-2.3.jar
jedis-2.8.0.jar
mysql-connector-java-6.0.6.jar
这些依赖包

activemq版本apache-activemq-5.15.4
