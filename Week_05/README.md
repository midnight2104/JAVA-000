1. 写代码实现Spring Bean的装配，方式越多越好（XML、Annotation都可以）,提
交到Github。

代码实现在：spring01\src\main\java\homework目录下


2. 给前面课程提供的Student/Klass/School实现自动配置和Starter
 自动配置项目是school，测试时无法导入成功。
 pom依赖如下：
 ```java
 		<dependency>
			<groupId>com.midnight.school</groupId>
			<artifactId>school</artifactId>
			<version>0.0.1-SNAPSHOT</version>
			<scope>system</scope>
			<systemPath>${project.basedir}/libs/school-0.0.1-SNAPSHOT.jar</systemPath>
		</dependency>
 ```
 
3. 研究一下JDBC接口和数据库连接池，掌握它们的设计和用法。
代码实现在：demo目录下