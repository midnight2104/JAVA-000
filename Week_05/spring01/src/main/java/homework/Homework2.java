package homework;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 2、（必做）写代码实现Spring Bean的装配，方式越多越好（XML、Annotation都可以）,提
 * 交到Github。
 */
public class Homework2 {
    public static void main(String[] args) {
        //方式一：使用XML装配 bean
        ApplicationContext context = new ClassPathXmlApplicationContext("homework/applicationContext.xml");
        Teacher teacher007 = (Teacher) context.getBean("teacher007");
        System.out.println(teacher007);

        //方式二：半自动注解配置 bean
        Student student = (Student) context.getBean("student");
        System.out.println(student);

        //方式三：Java Config配置 bean
        ApplicationContext annotationContext = new AnnotationConfigApplicationContext(JavaConfig.class);

        People people = (People) annotationContext.getBean("peopleBean");
        System.out.println(people);


    }
}
