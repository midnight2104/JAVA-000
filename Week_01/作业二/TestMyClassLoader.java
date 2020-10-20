import org.junit.Test;

import java.lang.reflect.Method;

public class TestMyClassLoader {

    @Test
    public void test() throws Exception {
        String filePath = "D:\\course\\JavaPro\\week1\\Hello\\Hello.xlass";
        //使用自定义类加载器加载类
        MyClassLoader myClassLoader = new MyClassLoader(filePath);
        Class<?> clazz = myClassLoader.loadClass("Hello");

        //实例化对象
        Object obj = clazz.newInstance();
        //获取声明的方法
        Method method = clazz.getDeclaredMethod("hello");
        //方法调用
        method.invoke(obj);

        System.out.println(obj);
        System.out.println(obj.getClass().getClassLoader());
    }
}