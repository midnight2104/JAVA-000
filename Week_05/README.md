1. 写代码实现Spring Bean的装配，方式越多越好（XML、Annotation都可以）,提
交到Github。

代码实现在：spring01\src\main\java\homework目录下


2. 给前面课程提供的Student/Klass/School实现自动配置和Starter
 
自动配置项目是school。
 pom依赖如下：
 ```java
	<dependency>
		<groupId>com.school</groupId>
		<artifactId>school-spring-boot-starter</artifactId>
		<version>1.0-SNAPSHOT</version>
		<scope>system</scope>
		<systemPath>${project.basedir}/libs/school-spring-boot-starter-1.0-SNAPSHOT.jar</systemPath>
	</dependency>
 ```
 
测试代码：
 
```java 

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = SchoolStarterConfiguration.class)
public class TestStarter {
    @Autowired
    School school;

    @Autowired
    Klass klass;

    @Autowired
    Student student;

    @Test
    public void test() {
        System.out.println(school);
        System.out.println(klass);
        System.out.println(student);
    }
}

```

3. 研究一下JDBC接口和数据库连接池，掌握它们的设计和用法。
代码实现在：demo目录下