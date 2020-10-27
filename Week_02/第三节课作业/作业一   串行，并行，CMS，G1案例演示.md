作业一. 使用 GCLogAnalysis.java 自己演练一遍串行/并行/CMS/G1的案例。

测试环境：
jdk1.8，win10，4核8线程，16G内存

演示代码：程序在1秒内生成许多对象，每个对象随机放在数组里。有的位置就会被重置，就会存在未被使用的对象，等待被回收。
```java

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
/*
演示GC日志生成与解读
*/
public class GCLogAnalysis {
    private static Random random = new Random();
    public static void main(String[] args) {
        // 当前毫秒时间戳
        long startMillis = System.currentTimeMillis();
        // 持续运行毫秒数; 可根据需要进行修改
        long timeoutMillis = TimeUnit.SECONDS.toMillis(1);
        // 结束时间戳
        long endMillis = startMillis + timeoutMillis;
        LongAdder counter = new LongAdder();
        System.out.println("正在执行...");
        // 缓存一部分对象; 进入老年代
        int cacheSize = 2000;
        Object[] cachedGarbage = new Object[cacheSize];
        // 在此时间范围内,持续循环
        while (System.currentTimeMillis() < endMillis) {
            // 生成垃圾对象
            Object garbage = generateGarbage(100*1024);
            counter.increment();
            int randomIndex = random.nextInt(2 * cacheSize);
            if (randomIndex < cacheSize) {
                cachedGarbage[randomIndex] = garbage;
            }
        }
        System.out.println("执行结束!共生成对象次数:" + counter.longValue());
    }

    // 生成对象
    private static Object generateGarbage(int max) {
        int randomSize = random.nextInt(max);
        int type = randomSize % 4;
        Object result = null;
        switch (type) {
            case 0:
                result = new int[randomSize];
                break;
            case 1:
                result = new byte[randomSize];
                break;
            case 2:
                result = new double[randomSize];
                break;
            default:
                StringBuilder builder = new StringBuilder();
                String randomString = "randomString-Anything";
                while (builder.length() < randomSize) {
                    builder.append(randomString);
                    builder.append(max);
                    builder.append(randomSize);
                }
                result = builder.toString();
                break;
        }
        return result;
    }
}
```

编译代码：
```java
javac GCLogAnalysis.java
```

**1.串行GC**

串行GC在年轻代使用标记复制算法，在老年代使用标记整理算法。都是单线程，不能并行处理，不能利用多核CPU的优势，只会使用一个核心进行垃圾收集。都会发生STW（Stop The World），暂停应用线程。

案例一（-Xms128m -Xmx128m）：
```java
D:\course\JavaPro\week2>java -XX:+UseSerialGC -Xms128m -Xmx128m -XX:+PrintGCDetails GCLogAnalysis
正在执行...
[GC (Allocation Failure) [DefNew: 34764K->4352K(39296K), 0.0047306 secs] 34764K->11281K(126720K), 0.0050491 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [DefNew: 39132K->4350K(39296K), 0.0057493 secs] 46062K->24412K(126720K), 0.0059074 secs] [Times: user=0.00 sys=0.02, real=0.01 secs]
[GC (Allocation Failure) [DefNew: 38894K->4350K(39296K), 0.0061968 secs] 58956K->36298K(126720K), 0.0064120 secs] [Times: user=0.02 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [DefNew: 38890K->4338K(39296K), 0.0042303 secs] 70839K->47521K(126720K), 0.0049833 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [DefNew: 39282K->4350K(39296K), 0.0048158 secs] 82465K->60594K(126720K), 0.0050332 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [DefNew: 38959K->4347K(39296K), 0.0054938 secs] 95203K->72362K(126720K), 0.0056512 secs] [Times: user=0.01 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [DefNew: 39291K->4344K(39296K), 0.0052938 secs] 107306K->86767K(126720K), 0.0055081 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[GC (Allocation Failure) [DefNew: 39063K->39063K(39296K), 0.0002362 secs][Tenured: 82422K->86879K(87424K), 0.0071027 secs] 121486K->95746K(126720K), [Metaspace: 2613K->2613K(1056768K)], 0.0078279 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
[Full GC (Allocation Failure) [Tenured: 87366K->87356K(87424K), 0.0120194 secs] 126429K->102427K(126720K), [Metaspace: 2613K->2613K(1056768K)], 0.0127868 secs] [Times: user=0.01 sys=0.00, real=0.01 secs]
[Full GC (Allocation Failure) [Tenured: 87356K->87285K(87424K), 0.0172295 secs] 126105K->109247K(126720K), [Metaspace: 2613K->2613K(1056768K)], 0.0302168 secs] [Times: user=0.02 sys=0.00, real=0.03 secs]
[Full GC (Allocation Failure) [Tenured: 87414K->87406K(87424K), 0.0200069 secs] 126682K->108997K(126720K), [Metaspace: 2613K->2613K(1056768K)], 0.0207435 secs] [Times: user=0.02 sys=0.00, real=0.02 secs]
[Full GC (Allocation Failure) [Tenured: 87406K->87406K(87424K), 0.0036416 secs] 126653K->114393K(126720K), [Metaspace: 2613K->2613K(1056768K)], 0.0041416 secs] [Times: user=0.02 sys=0.00, real=0.00 secs]
[Full GC (Allocation Failure) [Tenured: 87406K->87406K(87424K), 0.0054763 secs] 126491K->119369K(126720K), [Metaspace: 2613K->2613K(1056768K)], 0.0073075 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
[Full GC (Allocation Failure) [Tenured: 87406K->87406K(87424K), 0.0020103 secs] 126700K->121661K(126720K), [Metaspace: 2613K->2613K(1056768K)], 0.0032594 secs] [Times: user=0.02 sys=0.00, real=0.00 secs]
[Full GC (Allocation Failure) [Tenured: 87406K->87394K(87424K), 0.0201265 secs] 126671K->120003K(126720K), [Metaspace: 2613K->2613K(1056768K)], 0.0313952 secs] [Times: user=0.03 sys=0.00, real=0.03 secs]
[Full GC (Allocation Failure) [Tenured: 87394K->87394K(87424K), 0.0016258 secs] 126587K->123028K(126720K), [Metaspace: 2613K->2613K(1056768K)], 0.0021521 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[Full GC (Allocation Failure) [Tenured: 87394K->87394K(87424K), 0.0061530 secs] 126125K->124056K(126720K), [Metaspace: 2613K->2613K(1056768K)], 0.0067304 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
[Full GC (Allocation Failure) [Tenured: 87394K->87394K(87424K), 0.0015619 secs] 126670K->124861K(126720K), [Metaspace: 2613K->2613K(1056768K)], 0.0073016 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
[Full GC (Allocation Failure) [Tenured: 87394K->87000K(87424K), 0.0183339 secs] 126308K->124371K(126720K), [Metaspace: 2613K->2613K(1056768K)], 0.0203635 secs] [Times: user=0.01 sys=0.00, real=0.02 secs]
[Full GC (Allocation Failure) [Tenured: 87000K->87000K(87424K), 0.0022133 secs] 125969K->125300K(126720K), [Metaspace: 2613K->2613K(1056768K)], 0.0031001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[Full GC (Allocation Failure) [Tenured: 87319K->87319K(87424K), 0.0013100 secs] 126506K->126331K(126720K), [Metaspace: 2613K->2613K(1056768K)], 0.0026087 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[Full GC (Allocation Failure) [Tenured: 87319K->87257K(87424K), 0.0200753 secs] 126331K->125740K(126720K), [Metaspace: 2613K->2613K(1056768K)], 0.0207463 secs] [Times: user=0.02 sys=0.00, real=0.02 secs]
[Full GC (Allocation Failure) [Tenured: 87397K->87316K(87424K), 0.0013764 secs] 126690K->126182K(126720K), [Metaspace: 2613K->2613K(1056768K)], 0.0018680 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[Full GC (Allocation Failure) [Tenured: 87316K->87316K(87424K), 0.0012834 secs] 126513K->126342K(126720K), [Metaspace: 2613K->2613K(1056768K)], 0.0017306 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[Full GC (Allocation Failure) [Tenured: 87358K->87358K(87424K), 0.0023136 secs] 126641K->126312K(126720K), [Metaspace: 2613K->2613K(1056768K)], 0.0028844 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[Full GC (Allocation Failure) [Tenured: 87358K->87358K(87424K), 0.0032867 secs] 126540K->126468K(126720K), [Metaspace: 2613K->2613K(1056768K)], 0.0037958 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[Full GC (Allocation Failure) [Tenured: 87358K->87316K(87424K), 0.0020602 secs] 126612K->126497K(126720K), [Metaspace: 2613K->2613K(1056768K)], 0.0077848 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
[Full GC (Allocation Failure) [Tenured: 87316K->87316K(87424K), 0.0024295 secs] 126497K->126497K(126720K), [Metaspace: 2613K->2613K(1056768K)], 0.0118297 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
Exception in thread "main" java.lang.OutOfMemoryError: Java heap space
        at java.util.Arrays.copyOf(Unknown Source)
        at java.lang.AbstractStringBuilder.ensureCapacityInternal(Unknown Source)
        at java.lang.AbstractStringBuilder.append(Unknown Source)
        at java.lang.StringBuilder.append(Unknown Source)
        at GCLogAnalysis.generateGarbage(GCLogAnalysis.java:56)
        at GCLogAnalysis.main(GCLogAnalysis.java:25)
Heap
 def new generation   total 39296K, used 39192K [0x00000000f8000000, 0x00000000faaa0000, 0x00000000faaa0000)
  eden space 34944K, 100% used [0x00000000f8000000, 0x00000000fa220000, 0x00000000fa220000)
  from space 4352K,  97% used [0x00000000fa660000, 0x00000000faa86050, 0x00000000faaa0000)
  to   space 4352K,   0% used [0x00000000fa220000, 0x00000000fa220000, 0x00000000fa660000)
 tenured generation   total 87424K, used 87316K [0x00000000faaa0000, 0x0000000100000000, 0x0000000100000000)
   the space 87424K,  99% used [0x00000000faaa0000, 0x00000000fffe5000, 0x00000000fffe5000, 0x0000000100000000)
 Metaspace       used 2643K, capacity 4486K, committed 4864K, reserved 1056768K
  class space    used 291K, capacity 386K, committed 512K, reserved 1048576K

```

当前配置的初始堆内存（-Xms）是128M，最大堆内存（-Xmx）也是128M。这对当前应用程序来说是比较小的内存，所以最终会内存溢出（OutOfMemoryError）。

详细解释一下GC日志中的关键信息，以第一条为例：
```java
[GC (Allocation Failure) [DefNew: 34764K->4352K(39296K), 0.0047306 secs] 34764K->11281K(126720K), 0.0050491 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
```
- ==GC (Allocation Failure)==：在年轻代发生了一次`minor gc`，发生的原因是`Young`区没有足够的内存，存放新的数据，内存分配失败。
- ==DefNew: 34764K->4352K(39296K), 0.0047306 secs==：垃圾收集器的名称叫`DefNew`（基于标记-复制算法）。在GC前年轻代使用了`34764K`，在gc后年轻代的内存是`4352K`，年轻代总共使用`39296K`。此次GC使用时间是`0.0047306`秒。可以计算出这一次GC回收内存大约有：(34764-4352)/39296=77%。
- ==34764K->11281K(126720K)==：`34764K->11281K`表示整个堆在GC前后的使用量，`126720K`表示可用堆的总空间大小。
-  ==0.0050491 secs==：GC使用的时间。
- ==[Times: user=0.00 sys=0.00, real=0.01 secs]==: 分别表示用户线程占用时间（这里主要是GC耗时），系统占用时间，停顿时间。

从日志信息中，可以看到，在年轻代中，GC回收了内存有：`34764K-4352K=30412k`。而整个堆的回收情况是：`34764K->11281K=23483k`，这说明了有`30412k-23483k=6929k`内存对象进入到了老年代。另外，因为一开始老年代还没有使用，所以在GC前年轻代的使用量就是堆的使用量`34764k`。

日志中其他字段含义是一样的。在后面发生了`Full GC (Allocation Failure)`，在老年代的垃圾收集器是`Tenured`（基于标记-整理算法）。


案例二（-Xms256m -Xmx256m）：
```java
D:\course\JavaPro\week2>java -XX:+UseSerialGC -Xms256m -Xmx256m -XX:+PrintGCDetails GCLogAnalysis
正在执行...
[GC (Allocation Failure) [DefNew: 69952K->8703K(78656K), 0.0097565 secs] 69952K->24866K(253440K), 0.0102402 secs] [Times: user=0.02 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [DefNew: 78366K->8701K(78656K), 0.0123995 secs] 94529K->47525K(253440K), 0.0126920 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [DefNew: 78653K->8698K(78656K), 0.0089781 secs] 117477K->68240K(253440K), 0.0093191 secs] [Times: user=0.02 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [DefNew: 78503K->8703K(78656K), 0.0095831 secs] 138045K->93878K(253440K), 0.0099495 secs] [Times: user=0.00 sys=0.02, real=0.01 secs]
[GC (Allocation Failure) [DefNew: 78638K->8703K(78656K), 0.0094579 secs] 163813K->113496K(253440K), 0.0096384 secs] [Times: user=0.02 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [DefNew: 78251K->8703K(78656K), 0.0090190 secs] 183044K->135148K(253440K), 0.0093462 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [DefNew: 78557K->8701K(78656K), 0.0109435 secs] 205002K->164375K(253440K), 0.0116513 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [DefNew: 78653K->78653K(78656K), 0.0002535 secs][Tenured: 155674K->158789K(174784K), 0.0207991 secs] 234327K->158789K(253440K), [Metaspace: 2613K->2613K(1056768K)], 0.0222370 secs] [Times: user=0.03 sys=0.00, real=0.02 secs]
[GC (Allocation Failure) [DefNew: 69952K->69952K(78656K), 0.0002448 secs][Tenured: 158789K->164659K(174784K), 0.0235148 secs] 228741K->164659K(253440K), [Metaspace: 2613K->2613K(1056768K)], 0.0249595 secs] [Times: user=0.02 sys=0.00, real=0.03 secs]
[GC (Allocation Failure) [DefNew: 69952K->69952K(78656K), 0.0001411 secs][Tenured: 164659K->174670K(174784K), 0.0250073 secs] 234611K->178363K(253440K), [Metaspace: 2613K->2613K(1056768K)], 0.0263389 secs] [Times: user=0.02 sys=0.00, real=0.03 secs]
[Full GC (Allocation Failure) [Tenured: 174670K->174712K(174784K), 0.0303998 secs] 253011K->186422K(253440K), [Metaspace: 2613K->2613K(1056768K)], 0.0312608 secs] [Times: user=0.03 sys=0.00, real=0.03 secs]
[Full GC (Allocation Failure) [Tenured: 174712K->174522K(174784K), 0.0128326 secs] 253337K->206255K(253440K), [Metaspace: 2613K->2613K(1056768K)], 0.0150980 secs] [Times: user=0.02 sys=0.00, real=0.02 secs]
[Full GC (Allocation Failure) [Tenured: 174522K->174710K(174784K), 0.0186135 secs] 252876K->215746K(253440K), [Metaspace: 2613K->2613K(1056768K)], 0.0195909 secs] [Times: user=0.01 sys=0.00, real=0.02 secs]
[Full GC (Allocation Failure) [Tenured: 174710K->174734K(174784K), 0.0220202 secs] 252797K->222061K(253440K), [Metaspace: 2613K->2613K(1056768K)], 0.0230878 secs] [Times: user=0.01 sys=0.00, real=0.02 secs]
[Full GC (Allocation Failure) [Tenured: 174734K->174745K(174784K), 0.0319161 secs] 253124K->216348K(253440K), [Metaspace: 2613K->2613K(1056768K)], 0.0327276 secs] [Times: user=0.03 sys=0.00, real=0.03 secs]
[Full GC (Allocation Failure) [Tenured: 174745K->174745K(174784K), 0.0085533 secs] 253309K->228345K(253440K), [Metaspace: 2613K->2613K(1056768K)], 0.0165057 secs] [Times: user=0.00 sys=0.00, real=0.02 secs]
[Full GC (Allocation Failure) [Tenured: 174745K->174773K(174784K), 0.0133595 secs] 253349K->234416K(253440K), [Metaspace: 2613K->2613K(1056768K)], 0.0140097 secs] [Times: user=0.02 sys=0.00, real=0.01 secs]
[Full GC (Allocation Failure) [Tenured: 174773K->174474K(174784K), 0.0168179 secs] 253417K->236804K(253440K), [Metaspace: 2613K->2613K(1056768K)], 0.0292995 secs] [Times: user=0.02 sys=0.00, real=0.03 secs]
[Full GC (Allocation Failure) [Tenured: 174618K->174332K(174784K), 0.0394010 secs] 253232K->231695K(253440K), [Metaspace: 2613K->2613K(1056768K)], 0.0399098 secs] [Times: user=0.05 sys=0.00, real=0.04 secs]
[Full GC (Allocation Failure) [Tenured: 174332K->174332K(174784K), 0.0110215 secs] 252325K->237166K(253440K), [Metaspace: 2613K->2613K(1056768K)], 0.0206104 secs] [Times: user=0.01 sys=0.00, real=0.02 secs]
[Full GC (Allocation Failure) [Tenured: 174332K->174332K(174784K), 0.0117216 secs] 252966K->243903K(253440K), [Metaspace: 2613K->2613K(1056768K)], 0.0124997 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
[Full GC (Allocation Failure) [Tenured: 174332K->174429K(174784K), 0.0126842 secs] 252469K->247362K(253440K), [Metaspace: 2613K->2613K(1056768K)], 0.0202157 secs] [Times: user=0.02 sys=0.00, real=0.02 secs]
[Full GC (Allocation Failure) [Tenured: 174756K->174510K(174784K), 0.0346842 secs] 253387K->239952K(253440K), [Metaspace: 2613K->2613K(1056768K)], 0.0441785 secs] [Times: user=0.05 sys=0.00, real=0.04 secs]
[Full GC (Allocation Failure) [Tenured: 174510K->174510K(174784K), 0.0087470 secs] 253155K->244909K(253440K), [Metaspace: 2613K->2613K(1056768K)], 0.0095264 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
[Full GC (Allocation Failure) [Tenured: 174599K->174599K(174784K), 0.0066105 secs] 253229K->247033K(253440K), [Metaspace: 2613K->2613K(1056768K)], 0.0130625 secs] [Times: user=0.00 sys=0.00, real=0.02 secs]
[Full GC (Allocation Failure) [Tenured: 174599K->174599K(174784K), 0.0117250 secs] 253123K->248629K(253440K), [Metaspace: 2613K->2613K(1056768K)], 0.0126458 secs] [Times: user=0.02 sys=0.00, real=0.01 secs]
[Full GC (Allocation Failure) [Tenured: 174778K->174778K(174784K), 0.0338238 secs] 253412K->245128K(253440K), [Metaspace: 2613K->2613K(1056768K)], 0.0414770 secs] [Times: user=0.03 sys=0.00, real=0.04 secs]
[Full GC (Allocation Failure) [Tenured: 174778K->174778K(174784K), 0.0098128 secs] 253270K->246365K(253440K), [Metaspace: 2613K->2613K(1056768K)], 0.0105843 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
[Full GC (Allocation Failure) [Tenured: 174778K->174778K(174784K), 0.0053007 secs] 253344K->249086K(253440K), [Metaspace: 2613K->2613K(1056768K)], 0.0139627 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
[Full GC (Allocation Failure) [Tenured: 174778K->174778K(174784K), 0.0120832 secs] 253277K->250125K(253440K), [Metaspace: 2613K->2613K(1056768K)], 0.0210114 secs] [Times: user=0.02 sys=0.00, real=0.02 secs]
[Full GC (Allocation Failure) [Tenured: 174778K->174251K(174784K), 0.0353347 secs] 252871K->247582K(253440K), [Metaspace: 2613K->2613K(1056768K)], 0.0447751 secs] [Times: user=0.05 sys=0.00, real=0.04 secs]
[Full GC (Allocation Failure) [Tenured: 174251K->174251K(174784K), 0.0108172 secs] 252863K->248753K(253440K), [Metaspace: 2613K->2613K(1056768K)], 0.0114180 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
[Full GC (Allocation Failure) [Tenured: 174762K->174762K(174784K), 0.0044149 secs] 253392K->251108K(253440K), [Metaspace: 2613K->2613K(1056768K)], 0.0190374 secs] [Times: user=0.00 sys=0.00, real=0.02 secs]
[Full GC (Allocation Failure) [Tenured: 174762K->174762K(174784K), 0.0153088 secs] 253415K->251968K(253440K), [Metaspace: 2613K->2613K(1056768K)], 0.0159621 secs] [Times: user=0.01 sys=0.00, real=0.02 secs]
[Full GC (Allocation Failure) [Tenured: 174762K->174499K(174784K), 0.0325001 secs] 253349K->249815K(253440K), [Metaspace: 2613K->2613K(1056768K)], 0.0421176 secs] [Times: user=0.03 sys=0.00, real=0.04 secs]
[Full GC (Allocation Failure) [Tenured: 174741K->174741K(174784K), 0.0089539 secs] 253392K->251294K(253440K), [Metaspace: 2613K->2613K(1056768K)], 0.0095564 secs] [Times: user=0.02 sys=0.00, real=0.01 secs]
[Full GC (Allocation Failure) [Tenured: 174741K->174741K(174784K), 0.0070162 secs] 253337K->250993K(253440K), [Metaspace: 2613K->2613K(1056768K)], 0.0161804 secs] [Times: user=0.00 sys=0.00, real=0.02 secs]
[Full GC (Allocation Failure) [Tenured: 174741K->174741K(174784K), 0.0028436 secs] 252753K->251508K(253440K), [Metaspace: 2613K->2613K(1056768K)], 0.0118574 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
[Full GC (Allocation Failure) [Tenured: 174741K->174538K(174784K), 0.0303192 secs] 252951K->251739K(253440K), [Metaspace: 2613K->2613K(1056768K)], 0.0385329 secs] [Times: user=0.02 sys=0.00, real=0.04 secs]
执行结束!共生成对象次数:4482
Heap
 def new generation   total 78656K, used 77767K [0x00000000f0000000, 0x00000000f5550000, 0x00000000f5550000)
  eden space 69952K, 100% used [0x00000000f0000000, 0x00000000f4450000, 0x00000000f4450000)
  from space 8704K,  89% used [0x00000000f4cd0000, 0x00000000f5471ce8, 0x00000000f5550000)
  to   space 8704K,   0% used [0x00000000f4450000, 0x00000000f4450000, 0x00000000f4cd0000)
 tenured generation   total 174784K, used 174538K [0x00000000f5550000, 0x0000000100000000, 0x0000000100000000)
   the space 174784K,  99% used [0x00000000f5550000, 0x00000000fffc29b0, 0x00000000fffc2a00, 0x0000000100000000)
 Metaspace       used 2619K, capacity 4486K, committed 4864K, reserved 1056768K
  class space    used 288K, capacity 386K, committed 512K, reserved 1048576K

```

案例三（-Xms512m -Xmx512m）：
```java
D:\course\JavaPro\week2>java -XX:+UseSerialGC -Xms512m -Xmx512m -XX:+PrintGCDetails GCLogAnalysis
正在执行...
[GC (Allocation Failure) [DefNew: 139776K->17472K(157248K), 0.0320139 secs] 139776K->43166K(506816K), 0.0322926 secs] [Times: user=0.01 sys=0.02, real=0.03 secs]
[GC (Allocation Failure) [DefNew: 157117K->17470K(157248K), 0.0293402 secs] 182812K->90393K(506816K), 0.0294984 secs] [Times: user=0.01 sys=0.02, real=0.03 secs]
[GC (Allocation Failure) [DefNew: 157246K->17469K(157248K), 0.0190698 secs] 230169K->132524K(506816K), 0.0192439 secs] [Times: user=0.00 sys=0.01, real=0.02 secs]
[GC (Allocation Failure) [DefNew: 157245K->17471K(157248K), 0.0191730 secs] 272300K->174138K(506816K), 0.0195399 secs] [Times: user=0.02 sys=0.02, real=0.02 secs]
[GC (Allocation Failure) [DefNew: 157247K->17471K(157248K), 0.0193048 secs] 313914K->215704K(506816K), 0.0194564 secs] [Times: user=0.02 sys=0.00, real=0.02 secs]
[GC (Allocation Failure) [DefNew: 157247K->17471K(157248K), 0.0211645 secs] 355480K->260011K(506816K), 0.0215438 secs] [Times: user=0.02 sys=0.00, real=0.02 secs]
[GC (Allocation Failure) [DefNew: 157247K->17471K(157248K), 0.0190800 secs] 399787K->303079K(506816K), 0.0194231 secs] [Times: user=0.02 sys=0.00, real=0.02 secs]
[GC (Allocation Failure) [DefNew: 157247K->17471K(157248K), 0.0163864 secs] 442855K->344316K(506816K), 0.0166085 secs] [Times: user=0.02 sys=0.00, real=0.02 secs]
[GC (Allocation Failure) [DefNew: 157247K->157247K(157248K), 0.0006452 secs][Tenured: 326844K->273638K(349568K), 0.0329581 secs] 484092K->273638K(506816K), [Metaspace: 2613K->2613K(1056768K)], 0.0348695 secs] [Times: user=0.03 sys=0.00, real=0.04 secs]
[GC (Allocation Failure) [DefNew: 139776K->17471K(157248K), 0.0072768 secs] 413414K->315550K(506816K), 0.0076043 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [DefNew: 157247K->17470K(157248K), 0.0126462 secs] 455326K->361635K(506816K), 0.0128757 secs] [Times: user=0.02 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [DefNew: 157246K->157246K(157248K), 0.0004033 secs][Tenured: 344165K->313645K(349568K), 0.0383527 secs] 501411K->313645K(506816K), [Metaspace: 2613K->2613K(1056768K)], 0.0399101 secs] [Times: user=0.03 sys=0.00, real=0.04 secs]
[GC (Allocation Failure) [DefNew: 139631K->139631K(157248K), 0.0004263 secs][Tenured: 313645K->323128K(349568K), 0.0389438 secs] 453277K->323128K(506816K), [Metaspace: 2613K->2613K(1056768K)], 0.0404867 secs] [Times: user=0.03 sys=0.00, real=0.04 secs]
[GC (Allocation Failure) [DefNew: 139776K->139776K(157248K), 0.0002775 secs][Tenured: 323128K->317897K(349568K), 0.0434062 secs] 462904K->317897K(506816K), [Metaspace: 2613K->2613K(1056768K)], 0.0447298 secs] [Times: user=0.05 sys=0.00, real=0.05 secs]
[GC (Allocation Failure) [DefNew: 139096K->139096K(157248K), 0.0003314 secs][Tenured: 317897K->335489K(349568K), 0.0225404 secs] 456994K->335489K(506816K), [Metaspace: 2613K->2613K(1056768K)], 0.0239900 secs] [Times: user=0.01 sys=0.00, real=0.02 secs]
[GC (Allocation Failure) [DefNew: 139776K->139776K(157248K), 0.0002469 secs][Tenured: 335489K->345625K(349568K), 0.0415287 secs] 475265K->345625K(506816K), [Metaspace: 2613K->2613K(1056768K)], 0.0427386 secs] [Times: user=0.05 sys=0.00, real=0.04 secs]
[GC (Allocation Failure) [DefNew: 139776K->139776K(157248K), 0.0001684 secs][Tenured: 345625K->349345K(349568K), 0.0409780 secs] 485401K->351539K(506816K), [Metaspace: 2613K->2613K(1056768K)], 0.0434839 secs] [Times: user=0.05 sys=0.00, real=0.04 secs]
[Full GC (Allocation Failure) [Tenured: 349345K->342265K(349568K), 0.0460241 secs] 506168K->342265K(506816K), [Metaspace: 2613K->2613K(1056768K)], 0.0470453 secs] [Times: user=0.05 sys=0.00, real=0.05 secs]
[GC (Allocation Failure) [DefNew: 139776K->139776K(157248K), 0.0002614 secs][Tenured: 342265K->349159K(349568K), 0.0305656 secs] 482041K->368839K(506816K), [Metaspace: 2613K->2613K(1056768K)], 0.0317684 secs] [Times: user=0.03 sys=0.00, real=0.03 secs]
[Full GC (Allocation Failure) [Tenured: 349456K->349414K(349568K), 0.0433216 secs] 506623K->374469K(506816K), [Metaspace: 2613K->2613K(1056768K)], 0.0440407 secs] [Times: user=0.05 sys=0.00, real=0.04 secs]
执行结束!共生成对象次数:10660
Heap
 def new generation   total 157248K, used 39122K [0x00000000e0000000, 0x00000000eaaa0000, 0x00000000eaaa0000)
  eden space 139776K,  27% used [0x00000000e0000000, 0x00000000e2634858, 0x00000000e8880000)
  from space 17472K,   0% used [0x00000000e8880000, 0x00000000e8880000, 0x00000000e9990000)
  to   space 17472K,   0% used [0x00000000e9990000, 0x00000000e9990000, 0x00000000eaaa0000)
 tenured generation   total 349568K, used 349414K [0x00000000eaaa0000, 0x0000000100000000, 0x0000000100000000)
   the space 349568K,  99% used [0x00000000eaaa0000, 0x00000000fffd9878, 0x00000000fffd9a00, 0x0000000100000000)
 Metaspace       used 2619K, capacity 4486K, committed 4864K, reserved 1056768K
  class space    used 288K, capacity 386K, committed 512K, reserved 1048576K

```
案例四（-Xms1g -Xmx1g）：
```java
D:\course\JavaPro\week2>java -XX:+UseSerialGC -Xms1g -Xmx1g -XX:+PrintGCDetails GCLogAnalysis
正在执行...
[GC (Allocation Failure) [DefNew: 279534K->34944K(314560K), 0.0258260 secs] 279534K->84050K(1013632K), 0.0261899 secs] [Times: user=0.00 sys=0.02, real=0.03 secs]
[GC (Allocation Failure) [DefNew: 314560K->34943K(314560K), 0.0339486 secs] 363666K->163176K(1013632K), 0.0341207 secs] [Times: user=0.02 sys=0.02, real=0.03 secs]
[GC (Allocation Failure) [DefNew: 314559K->34943K(314560K), 0.0323238 secs] 442792K->243056K(1013632K), 0.0326154 secs] [Times: user=0.02 sys=0.01, real=0.03 secs]
[GC (Allocation Failure) [DefNew: 314105K->34942K(314560K), 0.0302813 secs] 522218K->317155K(1013632K), 0.0306010 secs] [Times: user=0.01 sys=0.02, real=0.03 secs]
[GC (Allocation Failure) [DefNew: 314558K->34943K(314560K), 0.0321500 secs] 596771K->395696K(1013632K), 0.0324712 secs] [Times: user=0.03 sys=0.00, real=0.03 secs]
[GC (Allocation Failure) [DefNew: 314559K->34943K(314560K), 0.0332325 secs] 675312K->477836K(1013632K), 0.0336039 secs] [Times: user=0.02 sys=0.02, real=0.03 secs]
[GC (Allocation Failure) [DefNew: 314559K->34944K(314560K), 0.0314314 secs] 757452K->553663K(1013632K), 0.0316246 secs] [Times: user=0.02 sys=0.02, real=0.03 secs]
[GC (Allocation Failure) [DefNew: 314560K->34943K(314560K), 0.0338595 secs] 833279K->640738K(1013632K), 0.0340344 secs] [Times: user=0.02 sys=0.02, real=0.03 secs]
[GC (Allocation Failure) [DefNew: 314437K->314437K(314560K), 0.0004803 secs][Tenured: 605794K->380615K(699072K), 0.0444502 secs] 920231K->380615K(1013632K), [Metaspace: 2613K->2613K(1056768K)], 0.0459032 secs] [Times: user=0.05 sys=0.00, real=0.05 secs]
[GC (Allocation Failure) [DefNew: 279616K->34943K(314560K), 0.0131446 secs] 660231K->466683K(1013632K), 0.0134274 secs] [Times: user=0.02 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [DefNew: 314559K->34943K(314560K), 0.0143578 secs] 746299K->544236K(1013632K), 0.0145414 secs] [Times: user=0.01 sys=0.00, real=0.02 secs]
[GC (Allocation Failure) [DefNew: 314559K->34943K(314560K), 0.0147676 secs] 823852K->617920K(1013632K), 0.0151750 secs] [Times: user=0.02 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [DefNew: 314559K->34943K(314560K), 0.0261911 secs] 897536K->691055K(1013632K), 0.0265216 secs] [Times: user=0.01 sys=0.02, real=0.03 secs]
[GC (Allocation Failure) [DefNew: 314559K->314559K(314560K), 0.0004336 secs][Tenured: 656111K->407861K(699072K), 0.0486656 secs] 970671K->407861K(1013632K), [Metaspace: 2613K->2613K(1056768K)], 0.0501344 secs] [Times: user=0.05 sys=0.00, real=0.05 secs]
执行结束!共生成对象次数:15211
Heap
 def new generation   total 314560K, used 127779K [0x00000000c0000000, 0x00000000d5550000, 0x00000000d5550000)
  eden space 279616K,  45% used [0x00000000c0000000, 0x00000000c7cc8ee8, 0x00000000d1110000)
  from space 34944K,   0% used [0x00000000d1110000, 0x00000000d1110000, 0x00000000d3330000)
  to   space 34944K,   0% used [0x00000000d3330000, 0x00000000d3330000, 0x00000000d5550000)
 tenured generation   total 699072K, used 407861K [0x00000000d5550000, 0x0000000100000000, 0x0000000100000000)
   the space 699072K,  58% used [0x00000000d5550000, 0x00000000ee39d618, 0x00000000ee39d800, 0x0000000100000000)
 Metaspace       used 2619K, capacity 4486K, committed 4864K, reserved 1056768K
  class space    used 288K, capacity 386K, committed 512K, reserved 1048576K

```

案例五（-Xms2g -Xmx2g）：
```java
D:\course\JavaPro\week2>java -XX:+UseSerialGC -Xms2g -Xmx2g -XX:+PrintGCDetails GCLogAnalysis
正在执行...
[GC (Allocation Failure) [DefNew: 559232K->69888K(629120K), 0.0423904 secs] 559232K->143612K(2027264K), 0.0427476 secs] [Times: user=0.02 sys=0.03, real=0.04 secs]
[GC (Allocation Failure) [DefNew: 629120K->69887K(629120K), 0.0602143 secs] 702844K->273125K(2027264K), 0.0603847 secs] [Times: user=0.03 sys=0.03, real=0.06 secs]
[GC (Allocation Failure) [DefNew: 629119K->69887K(629120K), 0.0526613 secs] 832357K->402090K(2027264K), 0.0528743 secs] [Times: user=0.05 sys=0.02, real=0.05 secs]
[GC (Allocation Failure) [DefNew: 629119K->69888K(629120K), 0.0528390 secs] 961322K->537153K(2027264K), 0.0530111 secs] [Times: user=0.03 sys=0.02, real=0.05 secs]
[GC (Allocation Failure) [DefNew: 629120K->69888K(629120K), 0.0550856 secs] 1096385K->672467K(2027264K), 0.0554154 secs] [Times: user=0.03 sys=0.03, real=0.06 secs]
[GC (Allocation Failure) [DefNew: 629120K->69887K(629120K), 0.0484408 secs] 1231699K->791933K(2027264K), 0.0486370 secs] [Times: user=0.03 sys=0.01, real=0.05 secs]
[GC (Allocation Failure) [DefNew: 629119K->69887K(629120K), 0.0514293 secs] 1351165K->917837K(2027264K), 0.0519458 secs] [Times: user=0.03 sys=0.03, real=0.05 secs]
[GC (Allocation Failure) [DefNew: 629119K->69887K(629120K), 0.0474890 secs] 1477069K->1031605K(2027264K), 0.0476871 secs] [Times: user=0.03 sys=0.02, real=0.05 secs]
执行结束!共生成对象次数:17061
Heap
 def new generation   total 629120K, used 92445K [0x0000000080000000, 0x00000000aaaa0000, 0x00000000aaaa0000)
  eden space 559232K,   4% used [0x0000000080000000, 0x0000000081607660, 0x00000000a2220000)
  from space 69888K,  99% used [0x00000000a2220000, 0x00000000a665fff0, 0x00000000a6660000)
  to   space 69888K,   0% used [0x00000000a6660000, 0x00000000a6660000, 0x00000000aaaa0000)
 tenured generation   total 1398144K, used 961717K [0x00000000aaaa0000, 0x0000000100000000, 0x0000000100000000)
   the space 1398144K,  68% used [0x00000000aaaa0000, 0x00000000e55cd448, 0x00000000e55cd600, 0x0000000100000000)
 Metaspace       used 2619K, capacity 4486K, committed 4864K, reserved 1056768K
  class space    used 288K, capacity 386K, committed 512K, reserved 1048576K

```


案例六（-Xms4g -Xmx4g）：
```java
D:\course\JavaPro\week2>java -XX:+UseSerialGC -Xms4g -Xmx4g -XX:+PrintGCDetails GCLogAnalysis
正在执行...
[GC (Allocation Failure) [DefNew: 1118528K->139776K(1258304K), 0.0682293 secs] 1118528K->236820K(4054528K), 0.0684769 secs] [Times: user=0.05 sys=0.03, real=0.07 secs]
[GC (Allocation Failure) [DefNew: 1258304K->139775K(1258304K), 0.0899142 secs] 1355348K->398707K(4054528K), 0.0902513 secs] [Times: user=0.06 sys=0.03, real=0.09 secs]
[GC (Allocation Failure) [DefNew: 1258303K->139775K(1258304K), 0.0718516 secs] 1517235K->567743K(4054528K), 0.0721616 secs] [Times: user=0.05 sys=0.03, real=0.07 secs]
[GC (Allocation Failure) [DefNew: 1258303K->139775K(1258304K), 0.0705470 secs] 1686271K->723587K(4054528K), 0.0707853 secs] [Times: user=0.03 sys=0.05, real=0.07 secs]
执行结束!共生成对象次数:16755
Heap
 def new generation   total 1258304K, used 184710K [0x00000006c0000000, 0x0000000715550000, 0x0000000715550000)
  eden space 1118528K,   4% used [0x00000006c0000000, 0x00000006c2be1b80, 0x0000000704450000)
  from space 139776K,  99% used [0x0000000704450000, 0x000000070cccfff8, 0x000000070ccd0000)
  to   space 139776K,   0% used [0x000000070ccd0000, 0x000000070ccd0000, 0x0000000715550000)
 tenured generation   total 2796224K, used 583811K [0x0000000715550000, 0x00000007c0000000, 0x00000007c0000000)
   the space 2796224K,  20% used [0x0000000715550000, 0x0000000738f70d98, 0x0000000738f70e00, 0x00000007c0000000)
 Metaspace       used 2619K, capacity 4486K, committed 4864K, reserved 1056768K
  class space    used 288K, capacity 386K, committed 512K, reserved 1048576K

```

案例七（-Xms512m -Xmx4g）：
```java
D:\course\JavaPro\week2>java -XX:+UseSerialGC -Xms512m -Xmx4g -XX:+PrintGCDetails GCLogAnalysis
正在执行...
[GC (Allocation Failure) [DefNew: 139776K->17471K(157248K), 0.0158409 secs] 139776K->44697K(506816K), 0.0161511 secs] [Times: user=0.00 sys=0.02, real=0.02 secs]
[GC (Allocation Failure) [DefNew: 157247K->17470K(157248K), 0.0232240 secs] 184473K->96218K(506816K), 0.0235964 secs] [Times: user=0.03 sys=0.00, real=0.02 secs]
[GC (Allocation Failure) [DefNew: 157231K->17471K(157248K), 0.0198687 secs] 235980K->143273K(506816K), 0.0201783 secs] [Times: user=0.00 sys=0.02, real=0.02 secs]
[GC (Allocation Failure) [DefNew: 157247K->17471K(157248K), 0.0175572 secs] 283049K->187388K(506816K), 0.0178746 secs] [Times: user=0.01 sys=0.00, real=0.02 secs]
[GC (Allocation Failure) [DefNew: 157247K->17470K(157248K), 0.0183265 secs] 327164K->227734K(506816K), 0.0186434 secs] [Times: user=0.00 sys=0.01, real=0.02 secs]
[GC (Allocation Failure) [DefNew: 157246K->17470K(157248K), 0.0162814 secs] 367510K->269706K(506816K), 0.0166708 secs] [Times: user=0.00 sys=0.02, real=0.02 secs]
[GC (Allocation Failure) [DefNew: 157246K->17471K(157248K), 0.0190609 secs] 409482K->321864K(506816K), 0.0193785 secs] [Times: user=0.02 sys=0.00, real=0.02 secs]
[GC (Allocation Failure) [DefNew: 157247K->17469K(157248K), 0.0211953 secs][Tenured: 354918K->273861K(354956K), 0.0308036 secs] 461640K->273861K(512204K), [Metaspace: 2613K->2613K(1056768K)], 0.0535300 secs] [Times: user=0.05 sys=0.01, real=0.05 secs]
[GC (Allocation Failure) [DefNew: 182656K->22783K(205440K), 0.0128010 secs] 456517K->333911K(661876K), 0.0132203 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [DefNew: 205439K->22782K(205440K), 0.0184139 secs] 516567K->393777K(661876K), 0.0187534 secs] [Times: user=0.00 sys=0.02, real=0.02 secs]
[GC (Allocation Failure) [DefNew: 205438K->22784K(205440K), 0.0224352 secs] 576433K->449380K(661876K), 0.0227988 secs] [Times: user=0.02 sys=0.00, real=0.02 secs]
[GC (Allocation Failure) [DefNew: 205035K->22783K(205440K), 0.0204890 secs][Tenured: 478389K->341569K(478528K), 0.0400969 secs] 631632K->341569K(683968K), [Metaspace: 2613K->2613K(1056768K)], 0.0618706 secs] [Times: user=0.05 sys=0.02, real=0.06 secs]
[GC (Allocation Failure) [DefNew: 227840K->28416K(256256K), 0.0179324 secs] 569409K->419221K(825540K), 0.0181128 secs] [Times: user=0.00 sys=0.02, real=0.02 secs]
[GC (Allocation Failure) [DefNew: 256256K->28415K(256256K), 0.0171297 secs] 647061K->478798K(825540K), 0.0174653 secs] [Times: user=0.00 sys=0.02, real=0.02 secs]
[GC (Allocation Failure) [DefNew: 256255K->28415K(256256K), 0.0224755 secs] 706638K->548806K(825540K), 0.0228648 secs] [Times: user=0.02 sys=0.00, real=0.02 secs]
[GC (Allocation Failure) [DefNew: 256255K->28415K(256256K), 0.0282753 secs][Tenured: 594258K->386411K(594364K), 0.0459395 secs] 776646K->386411K(850620K), [Metaspace: 2613K->2613K(1056768K)], 0.0756578 secs] [Times: user=0.05 sys=0.03, real=0.08 secs]
[GC (Allocation Failure) [DefNew: 257664K->32191K(289856K), 0.0187775 secs] 644075K->468932K(933876K), 0.0190838 secs] [Times: user=0.01 sys=0.00, real=0.02 secs]
[GC (Allocation Failure) [DefNew: 289813K->32191K(289856K), 0.0158623 secs] 726554K->545256K(933876K), 0.0162082 secs] [Times: user=0.02 sys=0.00, real=0.02 secs]
[GC (Allocation Failure) [DefNew: 289855K->32191K(289856K), 0.0145976 secs] 802920K->620222K(933876K), 0.0149842 secs] [Times: user=0.02 sys=0.00, real=0.01 secs]
执行结束!共生成对象次数:13608
Heap
 def new generation   total 289856K, used 42562K [0x00000006c0000000, 0x00000006d3a80000, 0x0000000715550000)
  eden space 257664K,   4% used [0x00000006c0000000, 0x00000006c0a20ab0, 0x00000006cfba0000)
  from space 32192K,  99% used [0x00000006d1b10000, 0x00000006d3a7fdf0, 0x00000006d3a80000)
  to   space 32192K,   0% used [0x00000006cfba0000, 0x00000006cfba0000, 0x00000006d1b10000)
 tenured generation   total 644020K, used 588030K [0x0000000715550000, 0x000000073ca3d000, 0x00000007c0000000)
   the space 644020K,  91% used [0x0000000715550000, 0x000000073938fbf0, 0x000000073938fc00, 0x000000073ca3d000)
 Metaspace       used 2619K, capacity 4486K, committed 4864K, reserved 1056768K
  class space    used 288K, capacity 386K, committed 512K, reserved 1048576K

```
小结：从上述案例可以看到，随着可用内存的增加，GC次数越来越少，在相同时间内（上述案例都是执行1秒）生成对象更多，效率更高。一般需要保持 `–Xms` 和 `–Xmx` 一致，否则应用刚启动可能就有好几个 FullGC。 当两者配置不一致时，堆内存会不断扩容，可能会导致性能抖动，会影响效率的，如上述案例七所示。


**2.并行GC**

并行GC在年轻代使用标记复制算法，在老年代使用标记整理算法。在标记-复制两阶段都是多线程进行，当然标记-整理也是多线程的，通过并行执行，使得GC时间大幅减少。都会发生STW（Stop The World），暂停应用线程。

> 注意，在GC中，并行是指多个线程回收垃圾，会暂停应用线程。并发是指多个线程在运行，垃圾回收线程和应用线程都在运行，虽然应用线程不会停，但是吞吐量会降低，毕竟还有线程在进行垃圾回收。


案例一（-Xms128m -Xmx128m）：
```java
D:\course\JavaPro\week2>java -XX:+UseParallelGC -Xms128m -Xmx128m -XX:+PrintGCDetails GCLogAnalysis
正在执行...
[GC (Allocation Failure) [PSYoungGen: 33280K->5109K(38400K)] 33280K->11237K(125952K), 0.0030870 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[GC (Allocation Failure) [PSYoungGen: 38346K->5119K(38400K)] 44474K->21608K(125952K), 0.0040586 secs] [Times: user=0.08 sys=0.05, real=0.00 secs]
[GC (Allocation Failure) [PSYoungGen: 37961K->5117K(38400K)] 54450K->32135K(125952K), 0.0078671 secs] [Times: user=0.06 sys=0.06, real=0.01 secs]
[GC (Allocation Failure) [PSYoungGen: 38386K->5117K(38400K)] 65404K->44151K(125952K), 0.0050800 secs] [Times: user=0.02 sys=0.11, real=0.01 secs]
[GC (Allocation Failure) [PSYoungGen: 38270K->5113K(38400K)] 77304K->53162K(125952K), 0.0038326 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[GC (Allocation Failure) [PSYoungGen: 38272K->5116K(19968K)] 86320K->65316K(107520K), 0.0082089 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [PSYoungGen: 19964K->8785K(29184K)] 80164K->70577K(116736K), 0.0105870 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [PSYoungGen: 23633K->13349K(29184K)] 85425K->76527K(116736K), 0.0104451 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [PSYoungGen: 28197K->14335K(29184K)] 91375K->81375K(116736K), 0.0039869 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[GC (Allocation Failure) [PSYoungGen: 29124K->10760K(29184K)] 96164K->87524K(116736K), 0.0102316 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
[Full GC (Ergonomics) [PSYoungGen: 10760K->0K(29184K)] [ParOldGen: 76764K->81031K(87552K)] 87524K->81031K(116736K), [Metaspace: 2613K->2613K(1056768K)], 0.0168154 secs] [Times: user=0.09 sys=0.03, real=0.02 secs]
[Full GC (Ergonomics) [PSYoungGen: 14534K->0K(29184K)] [ParOldGen: 81031K->85009K(87552K)] 95565K->85009K(116736K), [Metaspace: 2613K->2613K(1056768K)], 0.0263039 secs] [Times: user=0.13 sys=0.00, real=0.03 secs]
[Full GC (Ergonomics) [PSYoungGen: 14712K->1718K(29184K)] [ParOldGen: 85009K->87349K(87552K)] 99721K->89068K(116736K), [Metaspace: 2613K->2613K(1056768K)], 0.0173111 secs] [Times: user=0.09 sys=0.00, real=0.02 secs]
[Full GC (Ergonomics) [PSYoungGen: 14783K->5983K(29184K)] [ParOldGen: 87349K->87288K(87552K)] 102133K->93272K(116736K), [Metaspace: 2613K->2613K(1056768K)], 0.0280437 secs] [Times: user=0.02 sys=0.00, real=0.03 secs]
[Full GC (Ergonomics) [PSYoungGen: 14516K->6872K(29184K)] [ParOldGen: 87288K->87546K(87552K)] 101805K->94418K(116736K), [Metaspace: 2613K->2613K(1056768K)], 0.0174636 secs] [Times: user=0.13 sys=0.00, real=0.02 secs]
[Full GC (Ergonomics) [PSYoungGen: 14573K->9748K(29184K)] [ParOldGen: 87546K->87142K(87552K)] 102119K->96890K(116736K), [Metaspace: 2613K->2613K(1056768K)], 0.0272944 secs] [Times: user=0.02 sys=0.00, real=0.03 secs]
[Full GC (Ergonomics) [PSYoungGen: 14343K->10837K(29184K)] [ParOldGen: 87142K->87142K(87552K)] 101485K->97980K(116736K), [Metaspace: 2613K->2613K(1056768K)], 0.0035359 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[Full GC (Ergonomics) [PSYoungGen: 14703K->11598K(29184K)] [ParOldGen: 87142K->87163K(87552K)] 101846K->98762K(116736K), [Metaspace: 2613K->2613K(1056768K)], 0.0271543 secs] [Times: user=0.00 sys=0.00, real=0.03 secs]
[Full GC (Ergonomics) [PSYoungGen: 14848K->12267K(29184K)] [ParOldGen: 87163K->87394K(87552K)] 102011K->99662K(116736K), [Metaspace: 2613K->2613K(1056768K)], 0.0177002 secs] [Times: user=0.13 sys=0.00, real=0.02 secs]
[Full GC (Ergonomics) [PSYoungGen: 14557K->14327K(29184K)] [ParOldGen: 87394K->87394K(87552K)] 101951K->101721K(116736K), [Metaspace: 2613K->2613K(1056768K)], 0.0026622 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[Full GC (Allocation Failure) [PSYoungGen: 14327K->14327K(29184K)] [ParOldGen: 87394K->87375K(87552K)] 101721K->101702K(116736K), [Metaspace: 2613K->2613K(1056768K)], 0.0280559 secs] [Times: user=0.05 sys=0.00, real=0.03 secs]
Exception in thread "main" java.lang.OutOfMemoryError: Java heap space
        at GCLogAnalysis.generateGarbage(GCLogAnalysis.java:48)
        at GCLogAnalysis.main(GCLogAnalysis.java:25)
Heap
 PSYoungGen      total 29184K, used 14714K [0x00000000fd580000, 0x0000000100000000, 0x0000000100000000)
  eden space 14848K, 99% used [0x00000000fd580000,0x00000000fe3deb98,0x00000000fe400000)
  from space 14336K, 0% used [0x00000000ff200000,0x00000000ff200000,0x0000000100000000)
  to   space 14336K, 0% used [0x00000000fe400000,0x00000000fe400000,0x00000000ff200000)
 ParOldGen       total 87552K, used 87375K [0x00000000f8000000, 0x00000000fd580000, 0x00000000fd580000)
  object space 87552K, 99% used [0x00000000f8000000,0x00000000fd553c40,0x00000000fd580000)
 Metaspace       used 2643K, capacity 4486K, committed 4864K, reserved 1056768K
  class space    used 291K, capacity 386K, committed 512K, reserved 1048576K

```
解释一下在并行GC中出现的新字段：
- ==Full GC (Ergonomics)==：发生了Full GC，`Ergonomics` 表示JVM内部环境认为此时可以进行一次垃圾收集，跟自适应策略有关。
- ==PSYoungGen==：在并行GC中年轻代垃圾收集器的名称，基于标记-复制算法。
-  ==ParOldGen==：在并行GC中老年代垃圾收集器的名称，基于标记-整理算法。

 

案例二（-Xms256m -Xmx256m）：
```java
D:\course\JavaPro\week2>java -XX:+UseParallelGC -Xms256m -Xmx256m -XX:+PrintGCDetails GCLogAnalysis
正在执行...
[GC (Allocation Failure) [PSYoungGen: 65441K->10743K(76288K)] 65441K->21908K(251392K), 0.0042099 secs] [Times: user=0.02 sys=0.11, real=0.00 secs]
[GC (Allocation Failure) [PSYoungGen: 76279K->10741K(76288K)] 87444K->46042K(251392K), 0.0057952 secs] [Times: user=0.03 sys=0.09, real=0.01 secs]
[GC (Allocation Failure) [PSYoungGen: 76277K->10741K(76288K)] 111578K->66492K(251392K), 0.0047743 secs] [Times: user=0.00 sys=0.13, real=0.01 secs]
[GC (Allocation Failure) [PSYoungGen: 76277K->10735K(76288K)] 132028K->89989K(251392K), 0.0062961 secs] [Times: user=0.05 sys=0.08, real=0.01 secs]
[GC (Allocation Failure) [PSYoungGen: 76166K->10734K(76288K)] 155421K->115377K(251392K), 0.0069691 secs] [Times: user=0.02 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [PSYoungGen: 76025K->10743K(40448K)] 180668K->133494K(215552K), 0.0066121 secs] [Times: user=0.11 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [PSYoungGen: 40183K->19486K(58368K)] 162934K->145211K(233472K), 0.0060221 secs] [Times: user=0.09 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [PSYoungGen: 49182K->26881K(58368K)] 174907K->155946K(233472K), 0.0064958 secs] [Times: user=0.06 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [PSYoungGen: 56115K->28641K(58368K)] 185180K->165714K(233472K), 0.0088630 secs] [Times: user=0.13 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [PSYoungGen: 58336K->19261K(58368K)] 195410K->173749K(233472K), 0.0082225 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
[Full GC (Ergonomics) [PSYoungGen: 19261K->0K(58368K)] [ParOldGen: 154487K->144596K(175104K)] 173749K->144596K(233472K), [Metaspace: 2613K->2613K(1056768K)], 0.0269706 secs] [Times: user=0.23 sys=0.00, real=0.03 secs]
[GC (Allocation Failure) [PSYoungGen: 29094K->11567K(58368K)] 173690K->156163K(233472K), 0.0019648 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[Full GC (Ergonomics) [PSYoungGen: 11567K->0K(58368K)] [ParOldGen: 144596K->149992K(175104K)] 156163K->149992K(233472K), [Metaspace: 2613K->2613K(1056768K)], 0.0212319 secs] [Times: user=0.23 sys=0.00, real=0.02 secs]
[Full GC (Ergonomics) [PSYoungGen: 29696K->0K(58368K)] [ParOldGen: 149992K->156901K(175104K)] 179688K->156901K(233472K), [Metaspace: 2613K->2613K(1056768K)], 0.0293449 secs] [Times: user=0.05 sys=0.00, real=0.03 secs]
[Full GC (Ergonomics) [PSYoungGen: 29557K->0K(58368K)] [ParOldGen: 156901K->161730K(175104K)] 186458K->161730K(233472K), [Metaspace: 2613K->2613K(1056768K)], 0.0256476 secs] [Times: user=0.25 sys=0.00, real=0.03 secs]
[Full GC (Ergonomics) [PSYoungGen: 29609K->0K(58368K)] [ParOldGen: 161730K->170498K(175104K)] 191340K->170498K(233472K), [Metaspace: 2613K->2613K(1056768K)], 0.0276210 secs] [Times: user=0.14 sys=0.00, real=0.03 secs]
[Full GC (Ergonomics) [PSYoungGen: 29696K->1821K(58368K)] [ParOldGen: 170498K->175078K(175104K)] 200194K->176900K(233472K), [Metaspace: 2613K->2613K(1056768K)], 0.0281361 secs] [Times: user=0.23 sys=0.00, real=0.03 secs]
[Full GC (Ergonomics) [PSYoungGen: 29692K->4883K(58368K)] [ParOldGen: 175078K->175098K(175104K)] 204770K->179981K(233472K), [Metaspace: 2613K->2613K(1056768K)], 0.0317907 secs] [Times: user=0.17 sys=0.00, real=0.03 secs]
[Full GC (Ergonomics) [PSYoungGen: 29012K->10227K(58368K)] [ParOldGen: 175098K->174502K(175104K)] 204111K->184730K(233472K), [Metaspace: 2613K->2613K(1056768K)], 0.0284026 secs] [Times: user=0.23 sys=0.00, real=0.03 secs]
[Full GC (Ergonomics) [PSYoungGen: 29241K->11608K(58368K)] [ParOldGen: 174502K->175056K(175104K)] 203744K->186664K(233472K), [Metaspace: 2613K->2613K(1056768K)], 0.0284774 secs] [Times: user=0.25 sys=0.00, real=0.03 secs]
[Full GC (Ergonomics) [PSYoungGen: 29696K->13624K(58368K)] [ParOldGen: 175056K->174930K(175104K)] 204752K->188554K(233472K), [Metaspace: 2613K->2613K(1056768K)], 0.0296458 secs] [Times: user=0.14 sys=0.00, real=0.03 secs]
[Full GC (Ergonomics) [PSYoungGen: 29526K->14875K(58368K)] [ParOldGen: 174930K->174965K(175104K)] 204456K->189841K(233472K), [Metaspace: 2613K->2613K(1056768K)], 0.0280204 secs] [Times: user=0.23 sys=0.00, real=0.03 secs]
[Full GC (Ergonomics) [PSYoungGen: 29645K->17531K(58368K)] [ParOldGen: 174965K->174973K(175104K)] 204610K->192504K(233472K), [Metaspace: 2613K->2613K(1056768K)], 0.0277078 secs] [Times: user=0.25 sys=0.00, real=0.03 secs]
[Full GC (Ergonomics) [PSYoungGen: 29630K->18149K(58368K)] [ParOldGen: 174973K->174772K(175104K)] 204604K->192922K(233472K), [Metaspace: 2613K->2613K(1056768K)], 0.0275498 secs] [Times: user=0.11 sys=0.00, real=0.03 secs]
[Full GC (Ergonomics) [PSYoungGen: 29678K->19992K(58368K)] [ParOldGen: 174772K->175047K(175104K)] 204450K->195039K(233472K), [Metaspace: 2613K->2613K(1056768K)], 0.0304312 secs] [Times: user=0.14 sys=0.00, real=0.03 secs]
[Full GC (Ergonomics) [PSYoungGen: 29567K->21300K(58368K)] [ParOldGen: 175047K->174667K(175104K)] 204614K->195967K(233472K), [Metaspace: 2613K->2613K(1056768K)], 0.0301034 secs] [Times: user=0.14 sys=0.00, real=0.03 secs]
[Full GC (Ergonomics) [PSYoungGen: 29640K->21898K(58368K)] [ParOldGen: 174667K->174815K(175104K)] 204307K->196714K(233472K), [Metaspace: 2613K->2613K(1056768K)], 0.0286860 secs] [Times: user=0.14 sys=0.03, real=0.03 secs]
[Full GC (Ergonomics) [PSYoungGen: 29575K->22677K(58368K)] [ParOldGen: 174815K->175035K(175104K)] 204391K->197712K(233472K), [Metaspace: 2613K->2613K(1056768K)], 0.0221680 secs] [Times: user=0.13 sys=0.00, real=0.02 secs]
[Full GC (Ergonomics) [PSYoungGen: 29656K->24915K(58368K)] [ParOldGen: 175035K->174719K(175104K)] 204691K->199634K(233472K), [Metaspace: 2613K->2613K(1056768K)], 0.0197388 secs] [Times: user=0.25 sys=0.00, real=0.02 secs]
[Full GC (Ergonomics) [PSYoungGen: 29592K->26290K(58368K)] [ParOldGen: 174719K->174838K(175104K)] 204312K->201129K(233472K), [Metaspace: 2613K->2613K(1056768K)], 0.0327591 secs] [Times: user=0.20 sys=0.02, real=0.03 secs]
[Full GC (Ergonomics) [PSYoungGen: 29663K->26271K(58368K)] [ParOldGen: 174838K->174912K(175104K)] 204501K->201183K(233472K), [Metaspace: 2613K->2613K(1056768K)], 0.0297178 secs] [Times: user=0.11 sys=0.01, real=0.03 secs]
[Full GC (Ergonomics) [PSYoungGen: 29498K->26723K(58368K)] [ParOldGen: 174912K->174287K(175104K)] 204410K->201011K(233472K), [Metaspace: 2613K->2613K(1056768K)], 0.0320086 secs] [Times: user=0.19 sys=0.03, real=0.03 secs]
[Full GC (Ergonomics) [PSYoungGen: 29612K->26948K(58368K)] [ParOldGen: 174287K->174283K(175104K)] 203899K->201232K(233472K), [Metaspace: 2613K->2613K(1056768K)], 0.0092342 secs] [Times: user=0.00 sys=0.02, real=0.01 secs]
[Full GC (Ergonomics) [PSYoungGen: 29565K->25802K(58368K)] [ParOldGen: 174283K->175043K(175104K)] 203848K->200846K(233472K), [Metaspace: 2613K->2613K(1056768K)], 0.0283700 secs] [Times: user=0.13 sys=0.00, real=0.03 secs]
[Full GC (Ergonomics) [PSYoungGen: 29696K->26983K(58368K)] [ParOldGen: 175043K->174675K(175104K)] 204739K->201658K(233472K), [Metaspace: 2613K->2613K(1056768K)], 0.0245573 secs] [Times: user=0.11 sys=0.02, real=0.02 secs]
[Full GC (Ergonomics) [PSYoungGen: 29571K->28323K(58368K)] [ParOldGen: 174675K->174579K(175104K)] 204246K->202902K(233472K), [Metaspace: 2613K->2613K(1056768K)], 0.0325271 secs] [Times: user=0.16 sys=0.00, real=0.03 secs]
[Full GC (Ergonomics) [PSYoungGen: 29503K->28533K(58368K)] [ParOldGen: 174579K->174332K(175104K)] 204083K->202866K(233472K), [Metaspace: 2613K->2613K(1056768K)], 0.0140971 secs] [Times: user=0.13 sys=0.00, real=0.01 secs]
[Full GC (Ergonomics) [PSYoungGen: 29640K->28546K(58368K)] [ParOldGen: 174332K->174332K(175104K)] 203972K->202879K(233472K), [Metaspace: 2613K->2613K(1056768K)], 0.0178825 secs] [Times: user=0.00 sys=0.00, real=0.02 secs]
[Full GC (Ergonomics) [PSYoungGen: 29439K->28546K(58368K)] [ParOldGen: 174332K->174332K(175104K)] 203772K->202879K(233472K), [Metaspace: 2613K->2613K(1056768K)], 0.0026563 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[Full GC (Ergonomics) [PSYoungGen: 29588K->28913K(58368K)] [ParOldGen: 174332K->174165K(175104K)] 203921K->203079K(233472K), [Metaspace: 2613K->2613K(1056768K)], 0.0339140 secs] [Times: user=0.20 sys=0.00, real=0.03 secs]
[Full GC (Ergonomics) [PSYoungGen: 29510K->29355K(58368K)] [ParOldGen: 174165K->174165K(175104K)] 203675K->203521K(233472K), [Metaspace: 2613K->2613K(1056768K)], 0.0022547 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[Full GC (Ergonomics) [PSYoungGen: 29666K->29499K(58368K)] [ParOldGen: 174165K->174165K(175104K)] 203832K->203665K(233472K), [Metaspace: 2613K->2613K(1056768K)], 0.0027836 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[Full GC (Ergonomics) [PSYoungGen: 29649K->29211K(58368K)] [ParOldGen: 174165K->174165K(175104K)] 203814K->203377K(233472K), [Metaspace: 2613K->2613K(1056768K)], 0.0021382 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[Full GC (Ergonomics) [PSYoungGen: 29593K->29576K(58368K)] [ParOldGen: 174165K->174165K(175104K)] 203758K->203741K(233472K), [Metaspace: 2613K->2613K(1056768K)], 0.0031293 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
执行结束!共生成对象次数:3272
Heap
 PSYoungGen      total 58368K, used 29696K [0x00000000fab00000, 0x0000000100000000, 0x0000000100000000)
  eden space 29696K, 100% used [0x00000000fab00000,0x00000000fc800000,0x00000000fc800000)
  from space 28672K, 0% used [0x00000000fc800000,0x00000000fc800000,0x00000000fe400000)
  to   space 28672K, 0% used [0x00000000fe400000,0x00000000fe400000,0x0000000100000000)
 ParOldGen       total 175104K, used 174303K [0x00000000f0000000, 0x00000000fab00000, 0x00000000fab00000)
  object space 175104K, 99% used [0x00000000f0000000,0x00000000faa37f08,0x00000000fab00000)
 Metaspace       used 2619K, capacity 4486K, committed 4864K, reserved 1056768K
  class space    used 288K, capacity 386K, committed 512K, reserved 1048576K
```

案例三（-Xms512m -Xmx512m）：
```java
D:\course\JavaPro\week2>java -XX:+UseParallelGC -Xms512m -Xmx512m -XX:+PrintGCDetails GCLogAnalysis
正在执行...
[GC (Allocation Failure) [PSYoungGen: 131584K->21503K(153088K)] 131584K->39172K(502784K), 0.0061464 secs] [Times: user=0.06 sys=0.06, real=0.01 secs]
[GC (Allocation Failure) [PSYoungGen: 153087K->21488K(153088K)] 170756K->77014K(502784K), 0.0100151 secs] [Times: user=0.06 sys=0.06, real=0.01 secs]
[GC (Allocation Failure) [PSYoungGen: 152700K->21496K(153088K)] 208226K->117804K(502784K), 0.0116775 secs] [Times: user=0.02 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [PSYoungGen: 153061K->21495K(153088K)] 249369K->158455K(502784K), 0.0131004 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [PSYoungGen: 153079K->21488K(153088K)] 290039K->190385K(502784K), 0.0113370 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [PSYoungGen: 153072K->21492K(80384K)] 321969K->234460K(430080K), 0.0120406 secs] [Times: user=0.13 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [PSYoungGen: 80372K->32846K(116736K)] 293340K->249207K(466432K), 0.0053407 secs] [Times: user=0.13 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [PSYoungGen: 91726K->42372K(116736K)] 308087K->264024K(466432K), 0.0091360 secs] [Times: user=0.13 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [PSYoungGen: 101166K->48854K(116736K)] 322818K->279866K(466432K), 0.0080609 secs] [Times: user=0.11 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [PSYoungGen: 107734K->36980K(116736K)] 338746K->296938K(466432K), 0.0145134 secs] [Times: user=0.06 sys=0.01, real=0.01 secs]
[GC (Allocation Failure) [PSYoungGen: 95643K->19432K(116736K)] 355601K->314773K(466432K), 0.0103900 secs] [Times: user=0.11 sys=0.02, real=0.01 secs]
[Full GC (Ergonomics) [PSYoungGen: 19432K->0K(116736K)] [ParOldGen: 295341K->224092K(349696K)] 314773K->224092K(466432K), [Metaspace: 2613K->2613K(1056768K)], 0.0391948 secs] [Times: user=0.34 sys=0.00, real=0.04 secs]
[GC (Allocation Failure) [PSYoungGen: 58880K->20825K(116736K)] 282972K->244917K(466432K), 0.0041559 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[GC (Allocation Failure) [PSYoungGen: 79505K->17773K(116736K)] 303597K->260302K(466432K), 0.0059196 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [PSYoungGen: 76650K->20633K(116736K)] 319179K->279999K(466432K), 0.0062660 secs] [Times: user=0.02 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [PSYoungGen: 79285K->19894K(116736K)] 338651K->298263K(466432K), 0.0080933 secs] [Times: user=0.13 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [PSYoungGen: 78774K->19236K(116736K)] 357143K->316177K(466432K), 0.0066268 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [PSYoungGen: 78116K->23382K(116736K)] 375057K->338245K(466432K), 0.0076312 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
[Full GC (Ergonomics) [PSYoungGen: 23382K->0K(116736K)] [ParOldGen: 314862K->271748K(349696K)] 338245K->271748K(466432K), [Metaspace: 2613K->2613K(1056768K)], 0.0449878 secs] [Times: user=0.25 sys=0.00, real=0.04 secs]
[GC (Allocation Failure) [PSYoungGen: 58880K->21027K(116736K)] 330628K->292776K(466432K), 0.0041675 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[GC (Allocation Failure) [PSYoungGen: 79857K->22136K(116736K)] 351606K->314736K(466432K), 0.0099708 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [PSYoungGen: 81016K->22429K(116736K)] 373616K->335004K(466432K), 0.0073628 secs] [Times: user=0.11 sys=0.00, real=0.01 secs]
[Full GC (Ergonomics) [PSYoungGen: 22429K->0K(116736K)] [ParOldGen: 312574K->287830K(349696K)] 335004K->287830K(466432K), [Metaspace: 2613K->2613K(1056768K)], 0.0462200 secs] [Times: user=0.31 sys=0.00, real=0.05 secs]
[GC (Allocation Failure) [PSYoungGen: 58880K->22723K(116736K)] 346710K->310553K(466432K), 0.0062886 secs] [Times: user=0.11 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [PSYoungGen: 81603K->17888K(116736K)] 369433K->326769K(466432K), 0.0060658 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [PSYoungGen: 76732K->15944K(116736K)] 385613K->341804K(466432K), 0.0069494 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
[Full GC (Ergonomics) [PSYoungGen: 15944K->0K(116736K)] [ParOldGen: 325860K->295821K(349696K)] 341804K->295821K(466432K), [Metaspace: 2613K->2613K(1056768K)], 0.0480018 secs] [Times: user=0.30 sys=0.00, real=0.05 secs]
[GC (Allocation Failure) [PSYoungGen: 58847K->19654K(116736K)] 354668K->315475K(466432K), 0.0039456 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[GC (Allocation Failure) [PSYoungGen: 77761K->22472K(116736K)] 373582K->337327K(466432K), 0.0108535 secs] [Times: user=0.11 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [PSYoungGen: 81179K->19667K(117248K)] 396033K->355351K(466944K), 0.0055507 secs] [Times: user=0.13 sys=0.00, real=0.01 secs]
[Full GC (Ergonomics) [PSYoungGen: 19667K->0K(117248K)] [ParOldGen: 335683K->304518K(349696K)] 355351K->304518K(466944K), [Metaspace: 2613K->2613K(1056768K)], 0.0565955 secs] [Times: user=0.31 sys=0.00, real=0.06 secs]
[GC (Allocation Failure) [PSYoungGen: 59392K->21675K(116736K)] 363910K->326194K(466432K), 0.0042472 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[GC (Allocation Failure) [PSYoungGen: 81067K->43401K(114176K)] 385586K->347919K(463872K), 0.0063399 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [PSYoungGen: 102281K->37549K(116736K)] 406799K->362843K(466432K), 0.0082708 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
[Full GC (Ergonomics) [PSYoungGen: 37549K->0K(116736K)] [ParOldGen: 325293K->317024K(349696K)] 362843K->317024K(466432K), [Metaspace: 2613K->2613K(1056768K)], 0.0494176 secs] [Times: user=0.31 sys=0.00, real=0.05 secs]
[GC (Allocation Failure) [PSYoungGen: 58880K->17492K(116736K)] 375904K->334516K(466432K), 0.0038027 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[GC (Allocation Failure) [PSYoungGen: 76372K->22892K(116736K)] 393396K->356087K(466432K), 0.0083967 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
[Full GC (Ergonomics) [PSYoungGen: 22892K->0K(116736K)] [ParOldGen: 333194K->321413K(349696K)] 356087K->321413K(466432K), [Metaspace: 2613K->2613K(1056768K)], 0.0493008 secs] [Times: user=0.34 sys=0.00, real=0.05 secs]
执行结束!共生成对象次数:8486
Heap
 PSYoungGen      total 116736K, used 2824K [0x00000000f5580000, 0x0000000100000000, 0x0000000100000000)
  eden space 58880K, 4% used [0x00000000f5580000,0x00000000f58422c8,0x00000000f8f00000)
  from space 57856K, 0% used [0x00000000f8f00000,0x00000000f8f00000,0x00000000fc780000)
  to   space 55808K, 0% used [0x00000000fc980000,0x00000000fc980000,0x0000000100000000)
 ParOldGen       total 349696K, used 321413K [0x00000000e0000000, 0x00000000f5580000, 0x00000000f5580000)
  object space 349696K, 91% used [0x00000000e0000000,0x00000000f39e17a0,0x00000000f5580000)
 Metaspace       used 2619K, capacity 4486K, committed 4864K, reserved 1056768K
  class space    used 288K, capacity 386K, committed 512K, reserved 1048576K
```

案例四（-Xms1g -Xmx1g）：
```java
D:\course\JavaPro\week2>java -XX:+UseParallelGC -Xms1g -Xmx1g -XX:+PrintGCDetails GCLogAnalysis
正在执行...
[GC (Allocation Failure) [PSYoungGen: 262144K->43510K(305664K)] 262144K->82873K(1005056K), 0.0119412 secs] [Times: user=0.08 sys=0.05, real=0.01 secs]
[GC (Allocation Failure) [PSYoungGen: 305600K->43509K(305664K)] 344962K->151935K(1005056K), 0.0156697 secs] [Times: user=0.03 sys=0.09, real=0.02 secs]
[GC (Allocation Failure) [PSYoungGen: 305653K->43516K(305664K)] 414079K->224392K(1005056K), 0.0157361 secs] [Times: user=0.13 sys=0.00, real=0.02 secs]
[GC (Allocation Failure) [PSYoungGen: 305660K->43514K(305664K)] 486536K->297936K(1005056K), 0.0164835 secs] [Times: user=0.13 sys=0.00, real=0.02 secs]
[GC (Allocation Failure) [PSYoungGen: 305658K->43508K(305664K)] 560080K->372536K(1005056K), 0.0166230 secs] [Times: user=0.05 sys=0.08, real=0.02 secs]
[GC (Allocation Failure) [PSYoungGen: 305652K->43518K(160256K)] 634680K->446334K(859648K), 0.0156291 secs] [Times: user=0.11 sys=0.01, real=0.02 secs]
[GC (Allocation Failure) [PSYoungGen: 160254K->75629K(232960K)] 563070K->485042K(932352K), 0.0117534 secs] [Times: user=0.13 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [PSYoungGen: 192365K->100988K(232960K)] 601778K->522363K(932352K), 0.0177530 secs] [Times: user=0.13 sys=0.00, real=0.02 secs]
[GC (Allocation Failure) [PSYoungGen: 217724K->110540K(232960K)] 639099K->546155K(932352K), 0.0202855 secs] [Times: user=0.23 sys=0.00, real=0.02 secs]
[GC (Allocation Failure) [PSYoungGen: 227018K->70508K(232960K)] 662632K->572975K(932352K), 0.0219127 secs] [Times: user=0.11 sys=0.02, real=0.02 secs]
[GC (Allocation Failure) [PSYoungGen: 187244K->43508K(232960K)] 689711K->608044K(932352K), 0.0163429 secs] [Times: user=0.08 sys=0.05, real=0.02 secs]
[GC (Allocation Failure) [PSYoungGen: 160244K->40833K(232960K)] 724780K->642082K(932352K), 0.0129736 secs] [Times: user=0.13 sys=0.00, real=0.01 secs]
[Full GC (Ergonomics) [PSYoungGen: 40833K->0K(232960K)] [ParOldGen: 601248K->325303K(699392K)] 642082K->325303K(932352K), [Metaspace: 2613K->2613K(1056768K)], 0.0549556 secs] [Times: user=0.27 sys=0.02, real=0.06 secs]
[GC (Allocation Failure) [PSYoungGen: 115955K->41962K(232960K)] 441259K->367266K(932352K), 0.0076559 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [PSYoungGen: 158470K->40407K(232960K)] 483774K->403682K(932352K), 0.0123208 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [PSYoungGen: 157143K->38134K(232960K)] 520418K->438227K(932352K), 0.0118936 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [PSYoungGen: 154870K->39924K(232960K)] 554963K->473136K(932352K), 0.0108162 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [PSYoungGen: 156660K->41292K(232960K)] 589872K->510728K(932352K), 0.0119215 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [PSYoungGen: 158028K->34535K(232960K)] 627464K->540590K(932352K), 0.0121683 secs] [Times: user=0.13 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [PSYoungGen: 150947K->40575K(232960K)] 657002K->577390K(932352K), 0.0117252 secs] [Times: user=0.08 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [PSYoungGen: 157011K->40757K(232960K)] 693825K->613291K(932352K), 0.0107136 secs] [Times: user=0.11 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [PSYoungGen: 157493K->38660K(232960K)] 730027K->648864K(932352K), 0.0125106 secs] [Times: user=0.11 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [PSYoungGen: 155390K->33810K(232960K)] 765594K->678897K(932352K), 0.0122271 secs] [Times: user=0.09 sys=0.00, real=0.01 secs]
[Full GC (Ergonomics) [PSYoungGen: 33810K->0K(232960K)] [ParOldGen: 645086K->348602K(699392K)] 678897K->348602K(932352K), [Metaspace: 2613K->2613K(1056768K)], 0.0577264 secs] [Times: user=0.28 sys=0.00, real=0.06 secs]
执行结束!共生成对象次数:13011
Heap
 PSYoungGen      total 232960K, used 4872K [0x00000000eab00000, 0x0000000100000000, 0x0000000100000000)
  eden space 116736K, 4% used [0x00000000eab00000,0x00000000eafc2260,0x00000000f1d00000)
  from space 116224K, 0% used [0x00000000f8e80000,0x00000000f8e80000,0x0000000100000000)
  to   space 116224K, 0% used [0x00000000f1d00000,0x00000000f1d00000,0x00000000f8e80000)
 ParOldGen       total 699392K, used 348602K [0x00000000c0000000, 0x00000000eab00000, 0x00000000eab00000)
  object space 699392K, 49% used [0x00000000c0000000,0x00000000d546e908,0x00000000eab00000)
 Metaspace       used 2619K, capacity 4486K, committed 4864K, reserved 1056768K
  class space    used 288K, capacity 386K, committed 512K, reserved 1048576K
```

案例五（-Xms2g -Xmx2g）：
```java
D:\course\JavaPro\week2>java -XX:+UseParallelGC -Xms2g -Xmx2g -XX:+PrintGCDetails GCLogAnalysis
正在执行...
[GC (Allocation Failure) [PSYoungGen: 524800K->87030K(611840K)] 524800K->153362K(2010112K), 0.0204466 secs] [Times: user=0.05 sys=0.08, real=0.02 secs]
[GC (Allocation Failure) [PSYoungGen: 611830K->87035K(611840K)] 678162K->265024K(2010112K), 0.0271677 secs] [Times: user=0.11 sys=0.14, real=0.03 secs]
[GC (Allocation Failure) [PSYoungGen: 611835K->87037K(611840K)] 789824K->382617K(2010112K), 0.0265658 secs] [Times: user=0.14 sys=0.09, real=0.03 secs]
[GC (Allocation Failure) [PSYoungGen: 611837K->87026K(611840K)] 907417K->492456K(2010112K), 0.0236040 secs] [Times: user=0.16 sys=0.08, real=0.02 secs]
[GC (Allocation Failure) [PSYoungGen: 611826K->87035K(611840K)] 1017256K->606482K(2010112K), 0.0242055 secs] [Times: user=0.13 sys=0.00, real=0.03 secs]
[GC (Allocation Failure) [PSYoungGen: 611835K->87039K(320000K)] 1131282K->732329K(1718272K), 0.0257001 secs] [Times: user=0.03 sys=0.09, real=0.03 secs]
[GC (Allocation Failure) [PSYoungGen: 319999K->140132K(465920K)] 965289K->791175K(1864192K), 0.0181605 secs] [Times: user=0.13 sys=0.00, real=0.02 secs]
[GC (Allocation Failure) [PSYoungGen: 373092K->175787K(465920K)] 1024135K->837839K(1864192K), 0.0233739 secs] [Times: user=0.08 sys=0.00, real=0.02 secs]
[GC (Allocation Failure) [PSYoungGen: 408725K->185487K(465920K)] 1070777K->879593K(1864192K), 0.0287795 secs] [Times: user=0.20 sys=0.05, real=0.03 secs]
[GC (Allocation Failure) [PSYoungGen: 418447K->137373K(463872K)] 1112553K->920658K(1862144K), 0.0280563 secs] [Times: user=0.19 sys=0.03, real=0.03 secs]
[GC (Allocation Failure) [PSYoungGen: 370333K->65864K(465920K)] 1153618K->964411K(1864192K), 0.0248873 secs] [Times: user=0.14 sys=0.11, real=0.03 secs]
[GC (Allocation Failure) [PSYoungGen: 298824K->75228K(465920K)] 1197371K->1025960K(1864192K), 0.0164258 secs] [Times: user=0.08 sys=0.03, real=0.02 secs]
执行结束!共生成对象次数:17354
Heap
 PSYoungGen      total 465920K, used 145342K [0x00000000d5580000, 0x0000000100000000, 0x0000000100000000)
  eden space 232960K, 30% used [0x00000000d5580000,0x00000000d99f8ac0,0x00000000e3900000)
  from space 232960K, 32% used [0x00000000f1c80000,0x00000000f65f7010,0x0000000100000000)
  to   space 232960K, 0% used [0x00000000e3900000,0x00000000e3900000,0x00000000f1c80000)
 ParOldGen       total 1398272K, used 950732K [0x0000000080000000, 0x00000000d5580000, 0x00000000d5580000)
  object space 1398272K, 67% used [0x0000000080000000,0x00000000ba073090,0x00000000d5580000)
 Metaspace       used 2619K, capacity 4486K, committed 4864K, reserved 1056768K
  class space    used 288K, capacity 386K, committed 512K, reserved 1048576K
```

案例六（-Xms4g -Xmx4g）：
```java
D:\course\JavaPro\week2>java -XX:+UseParallelGC -Xms4g -Xmx4g -XX:+PrintGCDetails GCLogAnalysis
正在执行...
[GC (Allocation Failure) [PSYoungGen: 1048576K->174587K(1223168K)] 1048576K->240576K(4019712K), 0.0290771 secs] [Times: user=0.08 sys=0.17, real=0.03 secs]
[GC (Allocation Failure) [PSYoungGen: 1223163K->174582K(1223168K)] 1289152K->363371K(4019712K), 0.0364123 secs] [Times: user=0.09 sys=0.14, real=0.04 secs]
[GC (Allocation Failure) [PSYoungGen: 1223158K->174582K(1223168K)] 1411947K->491245K(4019712K), 0.0361420 secs] [Times: user=0.13 sys=0.11, real=0.04 secs]
[GC (Allocation Failure) [PSYoungGen: 1223158K->174586K(1223168K)] 1539821K->616600K(4019712K), 0.0364896 secs] [Times: user=0.30 sys=0.05, real=0.04 secs]
[GC (Allocation Failure) [PSYoungGen: 1223162K->174585K(1223168K)] 1665176K->756732K(4019712K), 0.0373964 secs] [Times: user=0.20 sys=0.05, real=0.04 secs]
执行结束!共生成对象次数:19822
Heap
 PSYoungGen      total 1223168K, used 216748K [0x000000076ab00000, 0x00000007c0000000, 0x00000007c0000000)
  eden space 1048576K, 4% used [0x000000076ab00000,0x000000076d42ccd8,0x00000007aab00000)
  from space 174592K, 99% used [0x00000007aab00000,0x00000007b557e670,0x00000007b5580000)
  to   space 174592K, 0% used [0x00000007b5580000,0x00000007b5580000,0x00000007c0000000)
 ParOldGen       total 2796544K, used 582147K [0x00000006c0000000, 0x000000076ab00000, 0x000000076ab00000)
  object space 2796544K, 20% used [0x00000006c0000000,0x00000006e3880c50,0x000000076ab00000)
 Metaspace       used 2619K, capacity 4486K, committed 4864K, reserved 1056768K
  class space    used 288K, capacity 386K, committed 512K, reserved 1048576K
```

案例七（-Xms512m -Xmx4g）：
```java
D:\course\JavaPro\week2>java -XX:+UseParallelGC -Xms512m -Xmx4g -XX:+PrintGCDetails GCLogAnalysis
正在执行...
[GC (Allocation Failure) [PSYoungGen: 131584K->21497K(153088K)] 131584K->42243K(502784K), 0.0078582 secs] [Times: user=0.06 sys=0.06, real=0.01 secs]
[GC (Allocation Failure) [PSYoungGen: 153081K->21495K(284672K)] 173827K->90015K(634368K), 0.0117936 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [PSYoungGen: 284663K->21499K(284672K)] 353183K->161569K(634368K), 0.0136564 secs] [Times: user=0.08 sys=0.05, real=0.01 secs]
[GC (Allocation Failure) [PSYoungGen: 284667K->21502K(547840K)] 424737K->230617K(897536K), 0.0136918 secs] [Times: user=0.03 sys=0.08, real=0.01 secs]
[GC (Allocation Failure) [PSYoungGen: 547838K->21501K(547840K)] 756953K->371437K(898560K), 0.0239670 secs] [Times: user=0.11 sys=0.14, real=0.02 secs]
[Full GC (Ergonomics) [PSYoungGen: 21501K->0K(547840K)] [ParOldGen: 349936K->264810K(586240K)] 371437K->264810K(1134080K), [Metaspace: 2613K->2613K(1056768K)], 0.0490146 secs] [Times: user=0.27 sys=0.01, real=0.05 secs]
[GC (Allocation Failure) [PSYoungGen: 526336K->147463K(1014784K)] 791146K->412273K(1601024K), 0.0185975 secs] [Times: user=0.00 sys=0.13, real=0.02 secs]
[GC (Allocation Failure) [PSYoungGen: 1014279K->186869K(1181184K)] 1279089K->548291K(1767424K), 0.0335555 secs] [Times: user=0.14 sys=0.11, real=0.03 secs]
[GC (Allocation Failure) [PSYoungGen: 1181173K->216574K(970240K)] 1542595K->653601K(1556480K), 0.0357316 secs] [Times: user=0.19 sys=0.05, real=0.04 secs]
[GC (Allocation Failure) [PSYoungGen: 970238K->311403K(1065472K)] 1407265K->757655K(1651712K), 0.0375460 secs] [Times: user=0.23 sys=0.00, real=0.04 secs]
[Full GC (Ergonomics) [PSYoungGen: 311403K->0K(1065472K)] [ParOldGen: 446251K->381695K(753664K)] 757655K->381695K(1819136K), [Metaspace: 2613K->2613K(1056768K)], 0.0606180 secs] [Times: user=0.41 sys=0.00, real=0.06 secs]
执行结束!共生成对象次数:16808
Heap
 PSYoungGen      total 1065472K, used 30819K [0x000000076ab00000, 0x00000007c0000000, 0x00000007c0000000)
  eden space 753664K, 4% used [0x000000076ab00000,0x000000076c918f98,0x0000000798b00000)
  from space 311808K, 0% used [0x0000000798b00000,0x0000000798b00000,0x00000007abb80000)
  to   space 332288K, 0% used [0x00000007abb80000,0x00000007abb80000,0x00000007c0000000)
 ParOldGen       total 753664K, used 381695K [0x00000006c0000000, 0x00000006ee000000, 0x000000076ab00000)
  object space 753664K, 50% used [0x00000006c0000000,0x00000006d74bfc18,0x00000006ee000000)
 Metaspace       used 2619K, capacity 4486K, committed 4864K, reserved 1056768K
  class space    used 288K, capacity 386K, committed 512K, reserved 1048576K
```

小结：并行GC可以利用多核CPU的优势，使用多个线程进行垃圾回收。相比于串行GC，在相同配置下，生成的对象更多，GC耗时更小，整体效率更高。


**3.CMS GC**

CMS的官方名称为 `Mostly Concurrent Mark and Sweep Garbage Collector`(主要并发-标记-清除-垃圾收集器)。其对年轻代采用标记-复制算法, 对老年代使用标记-清除算法。

CMS的设计目标是避免在老年代垃圾收集时出现长时间的卡顿。主要通过两种手段来达成此目标。

- 第一，不对老年代进行整理, 而是使用空闲列表(free-lists)来管理内存空间的回收。
- 第二，在标记-清除阶段的大部分工作和应用线程一起并发执行。

也就是说, 在这些阶段并没有明显的应用线程暂停，低延迟，高响应比。但值得注意的是, 它仍然和应用线程争抢CPU时间，吞吐量会有所下降。默认情况下, CMS 使用的并发线程数等于CPU内核数的 1/4。

CMS过程多，较复杂，这里只给出一个演示案例。

案例（-Xms512m -Xmx51m）：
```java
D:\course\JavaPro\week2>java -XX:+UseConcMarkSweepGC -Xms512m -Xmx512m -XX:+PrintGCDetails GCLogAnalysis
正在执行...
[GC (Allocation Failure) [ParNew: 139524K->17472K(157248K), 0.0081116 secs] 139524K->53858K(506816K), 0.0084194 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [ParNew: 157248K->17461K(157248K), 0.0111317 secs] 193634K->100350K(506816K), 0.0114263 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [ParNew: 157237K->17470K(157248K), 0.0190110 secs] 240126K->147606K(506816K), 0.0191201 secs] [Times: user=0.13 sys=0.00, real=0.02 secs]
[GC (Allocation Failure) [ParNew: 157246K->17471K(157248K), 0.0202185 secs] 287382K->191303K(506816K), 0.0204533 secs] [Times: user=0.11 sys=0.02, real=0.02 secs]
[GC (Allocation Failure) [ParNew: 157247K->17471K(157248K), 0.0202245 secs] 331079K->241173K(506816K), 0.0204438 secs] [Times: user=0.11 sys=0.01, real=0.02 secs]
[GC (CMS Initial Mark) [1 CMS-initial-mark: 223702K(349568K)] 241617K(506816K), 0.0006136 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[CMS-concurrent-mark-start]
[CMS-concurrent-mark: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[CMS-concurrent-preclean-start]
[CMS-concurrent-preclean: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[CMS-concurrent-abortable-preclean-start]
[GC (Allocation Failure) [ParNew: 157247K->17471K(157248K), 0.0201002 secs] 380949K->289532K(506816K), 0.0202149 secs] [Times: user=0.09 sys=0.02, real=0.02 secs]
[GC (Allocation Failure) [ParNew: 157217K->17469K(157248K), 0.0192277 secs] 429279K->333751K(506816K), 0.0193328 secs] [Times: user=0.13 sys=0.00, real=0.02 secs]
[GC (Allocation Failure) [ParNew: 157245K->157245K(157248K), 0.0000573 secs][CMS[CMS-concurrent-abortable-preclean: 0.004/0.091 secs] [Times: user=0.30 sys=0.02, real=0.09 secs]
 (concurrent mode failure): 316281K->252015K(349568K), 0.0440026 secs] 473527K->252015K(506816K), [Metaspace: 2613K->2613K(1056768K)], 0.0445195 secs] [Times: user=0.05 sys=0.00, real=0.04 secs]
[GC (Allocation Failure) [ParNew: 139776K->17470K(157248K), 0.0088589 secs] 391791K->294554K(506816K), 0.0092332 secs] [Times: user=0.09 sys=0.00, real=0.01 secs]
[GC (CMS Initial Mark) [1 CMS-initial-mark: 277083K(349568K)] 297545K(506816K), 0.0005588 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[CMS-concurrent-mark-start]
[CMS-concurrent-mark: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[CMS-concurrent-preclean-start]
[CMS-concurrent-preclean: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[CMS-concurrent-abortable-preclean-start]
[GC (Allocation Failure) [ParNew: 157246K->17471K(157248K), 0.0123281 secs] 434330K->339422K(506816K), 0.0126656 secs] [Times: user=0.02 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [ParNew: 157247K->157247K(157248K), 0.0001621 secs][CMS[CMS-concurrent-abortable-preclean: 0.001/0.045 secs] [Times: user=0.05 sys=0.00, real=0.05 secs]
 (concurrent mode failure): 321950K->293344K(349568K), 0.0528400 secs] 479198K->293344K(506816K), [Metaspace: 2613K->2613K(1056768K)], 0.0537640 secs] [Times: user=0.05 sys=0.00, real=0.05 secs]
[GC (Allocation Failure) [ParNew: 139776K->17471K(157248K), 0.0099173 secs] 433120K->342675K(506816K), 0.0101634 secs] [Times: user=0.09 sys=0.00, real=0.01 secs]
[GC (CMS Initial Mark) [1 CMS-initial-mark: 325204K(349568K)] 342800K(506816K), 0.0029137 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[CMS-concurrent-mark-start]
[CMS-concurrent-mark: 0.002/0.002 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[CMS-concurrent-preclean-start]
[CMS-concurrent-preclean: 0.001/0.001 secs] [Times: user=0.02 sys=0.00, real=0.01 secs]
[CMS-concurrent-abortable-preclean-start]
[CMS-concurrent-abortable-preclean: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
[GC (Allocation Failure) [ParNew: 157247K->157247K(157248K), 0.0001170 secs][CMS (concurrent mode failure): 325204K->321810K(349568K), 0.0477790 secs] 482451K->321810K(506816K), [Metaspace: 2613K->2613K(1056768K)], 0.0533533 secs] [Times: user=0.05 sys=0.00, real=0.05 secs]
[GC (Allocation Failure) [ParNew: 139776K->139776K(157248K), 0.0002987 secs][CMS: 321810K->326699K(349568K), 0.0448380 secs] 461586K->326699K(506816K), [Metaspace: 2613K->2613K(1056768K)], 0.0460650 secs] [Times: user=0.05 sys=0.00, real=0.05 secs]
[GC (CMS Initial Mark) [1 CMS-initial-mark: 326699K(349568K)] 326973K(506816K), 0.0007281 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[CMS-concurrent-mark-start]
[CMS-concurrent-mark: 0.002/0.002 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[CMS-concurrent-preclean-start]
[CMS-concurrent-preclean: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[CMS-concurrent-abortable-preclean-start]
[CMS-concurrent-abortable-preclean: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[GC (CMS Final Remark) [YG occupancy: 28785 K (157248 K)][Rescan (parallel) , 0.0002592 secs][weak refs processing, 0.0000208 secs][class unloading, 0.0008936 secs][scrub symbol table, 0.0002973 secs][scrub string table, 0.0000928 secs][1 CMS-remark: 326699K(349568K)] 355485K(506816K), 0.0049802 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
[CMS-concurrent-sweep-start]
[CMS-concurrent-sweep: 0.001/0.001 secs] [Times: user=0.02 sys=0.00, real=0.01 secs]
[CMS-concurrent-reset-start]
[CMS-concurrent-reset: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[GC (Allocation Failure) [ParNew: 139776K->139776K(157248K), 0.0003069 secs][CMS: 326197K->336731K(349568K), 0.0527671 secs] 465973K->336731K(506816K), [Metaspace: 2613K->2613K(1056768K)], 0.0560247 secs] [Times: user=0.05 sys=0.00, real=0.06 secs]
[GC (CMS Initial Mark) [1 CMS-initial-mark: 336731K(349568K)] 339780K(506816K), 0.0007651 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[CMS-concurrent-mark-start]
[CMS-concurrent-mark: 0.002/0.002 secs] [Times: user=0.05 sys=0.00, real=0.00 secs]
[CMS-concurrent-preclean-start]
[CMS-concurrent-preclean: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[CMS-concurrent-abortable-preclean-start]
[CMS-concurrent-abortable-preclean: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[GC (CMS Final Remark) [YG occupancy: 25128 K (157248 K)][Rescan (parallel) , 0.0003284 secs][weak refs processing, 0.0000299 secs][class unloading, 0.0002353 secs][scrub symbol table, 0.0004058 secs][scrub string table, 0.0001077 secs][1 CMS-remark: 336731K(349568K)] 361859K(506816K), 0.0092770 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
[CMS-concurrent-sweep-start]
[CMS-concurrent-sweep: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[CMS-concurrent-reset-start]
[CMS-concurrent-reset: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[GC (Allocation Failure) [ParNew: 139776K->139776K(157248K), 0.0002000 secs][CMS: 336093K->341106K(349568K), 0.0473734 secs] 475869K->341106K(506816K), [Metaspace: 2613K->2613K(1056768K)], 0.0484223 secs] [Times: user=0.03 sys=0.01, real=0.05 secs]
[GC (CMS Initial Mark) [1 CMS-initial-mark: 341106K(349568K)] 344308K(506816K), 0.0003132 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[CMS-concurrent-mark-start]
[CMS-concurrent-mark: 0.002/0.002 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[CMS-concurrent-preclean-start]
[CMS-concurrent-preclean: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[CMS-concurrent-abortable-preclean-start]
[CMS-concurrent-abortable-preclean: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[GC (CMS Final Remark) [YG occupancy: 36942 K (157248 K)][Rescan (parallel) , 0.0004149 secs][weak refs processing, 0.0000188 secs][class unloading, 0.0002119 secs][scrub symbol table, 0.0002849 secs][scrub string table, 0.0000902 secs][1 CMS-remark: 341106K(349568K)] 378048K(506816K), 0.0040528 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[CMS-concurrent-sweep-start]
[CMS-concurrent-sweep: 0.001/0.001 secs] [Times: user=0.02 sys=0.00, real=0.01 secs]
[CMS-concurrent-reset-start]
[CMS-concurrent-reset: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[GC (Allocation Failure) [ParNew: 139776K->139776K(157248K), 0.0000658 secs][CMS: 338824K->343334K(349568K), 0.0446340 secs] 478600K->343334K(506816K), [Metaspace: 2613K->2613K(1056768K)], 0.0455519 secs] [Times: user=0.05 sys=0.00, real=0.05 secs]
[GC (CMS Initial Mark) [1 CMS-initial-mark: 343334K(349568K)] 343987K(506816K), 0.0006540 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[CMS-concurrent-mark-start]
[CMS-concurrent-mark: 0.001/0.001 secs] [Times: user=0.05 sys=0.00, real=0.00 secs]
[CMS-concurrent-preclean-start]
[CMS-concurrent-preclean: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[CMS-concurrent-abortable-preclean-start]
[CMS-concurrent-abortable-preclean: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[GC (CMS Final Remark) [YG occupancy: 26957 K (157248 K)][Rescan (parallel) , 0.0002413 secs][weak refs processing, 0.0000212 secs][class unloading, 0.0004218 secs][scrub symbol table, 0.0002805 secs][scrub string table, 0.0000875 secs][1 CMS-remark: 343334K(349568K)] 370291K(506816K), 0.0016550 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[CMS-concurrent-sweep-start]
[CMS-concurrent-sweep: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
[CMS-concurrent-reset-start]
[CMS-concurrent-reset: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[GC (Allocation Failure) [ParNew: 139776K->139776K(157248K), 0.0001528 secs][CMS: 342197K->345032K(349568K), 0.0488002 secs] 481973K->345032K(506816K), [Metaspace: 2613K->2613K(1056768K)], 0.0500741 secs] [Times: user=0.05 sys=0.00, real=0.05 secs]
[GC (CMS Initial Mark) [1 CMS-initial-mark: 345032K(349568K)] 345176K(506816K), 0.0008459 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[CMS-concurrent-mark-start]
[CMS-concurrent-mark: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[CMS-concurrent-preclean-start]
[CMS-concurrent-preclean: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[CMS-concurrent-abortable-preclean-start]
[CMS-concurrent-abortable-preclean: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
[GC (CMS Final Remark) [YG occupancy: 21309 K (157248 K)][Rescan (parallel) , 0.0002177 secs][weak refs processing, 0.0000220 secs][class unloading, 0.0006460 secs][scrub symbol table, 0.0003373 secs][scrub string table, 0.0000925 secs][1 CMS-remark: 345032K(349568K)] 366342K(506816K), 0.0022575 secs] [Times: user=0.01 sys=0.00, real=0.00 secs]
[CMS-concurrent-sweep-start]
[CMS-concurrent-sweep: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
[CMS-concurrent-reset-start]
[CMS-concurrent-reset: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
执行结束!共生成对象次数:9876
Heap
 par new generation   total 157248K, used 86018K [0x00000000e0000000, 0x00000000eaaa0000, 0x00000000eaaa0000)
  eden space 139776K,  61% used [0x00000000e0000000, 0x00000000e5400bb0, 0x00000000e8880000)
  from space 17472K,   0% used [0x00000000e8880000, 0x00000000e8880000, 0x00000000e9990000)
  to   space 17472K,   0% used [0x00000000e9990000, 0x00000000e9990000, 0x00000000eaaa0000)
 concurrent mark-sweep generation total 349568K, used 343078K [0x00000000eaaa0000, 0x0000000100000000, 0x0000000100000000)
 Metaspace       used 2619K, capacity 4486K, committed 4864K, reserved 1056768K
  class space    used 288K, capacity 386K, committed 512K, reserved 1048576K

```
现在对CMS GC日志中的关键字段进行解释：

首先发生的是年轻代的`minor gc`:
```java
[GC (Allocation Failure) [ParNew: 139524K->17472K(157248K), 0.0081116 secs] 139524K->53858K(506816K), 0.0084194 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
```
- ==GC (Allocation Failure)== ：GC表明这是一次小型GC(Minor GC)。触发垃圾收集的原因是由于年轻代中没有适当的空间存放新的数据结构引起的。
- ==ParNew==：`ParNew`是垃圾收集器的名称。这个名字表示的是在年轻代中使用的，基于标记-复制算法,专门设计用来配合老年代使用的 CMS。
- ==139524K->17472K(157248K), 0.0081116 secs==：在垃圾收集之前（139524K）和之后（17472K）的年轻代使用量，年轻代的总大小（157248K），该阶段耗时0.0081116秒。
- ==139524K->53858K(506816K), 0.0084194 secs==：在垃圾收集之前（139524K）和之后（53858K）堆内存的使用情况，堆内存总共大小（506816K），该阶段耗时0.0084194秒。包括标记-复制和CMS收集器的通信开销, 提升存活时间达标的对象到老年代,以及垃圾收集后期的一些最终清理。
- ==[Times: user=0.00 sys=0.00, real=0.01 secs]==： GC事件的持续时间。

后面发生的是`Full GC`，主要是CMS收集器作用于老年代，这个过程很复杂，包括7个阶段，这些阶段直接夹杂着`minor gc`。

*第一阶段：* Initial Mark(初始标记)：
这是第一次STW事件。 此阶段的目标是标记老年代中所有存活的对象, 包括 GC Root的直接引用, 以及由年轻代中存活对象指向老年代中的对象。 后者也非常重要, 因为老年代是独立进行回收的。
```java
[GC (CMS Initial Mark) [1 CMS-initial-mark: 223702K(349568K)] 241617K(506816K), 0.0006136 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
```
- ==CMS Initial Mark==：该阶段名称是`Initial Mark`，标记所有的`GC Root`。
- ==223702K(349568K)==：老年代当前使用量和老年代总共可使用的容量。
- ==241617K(506816K)==：堆当前使用量和堆总共可使用的容量。
- == 0.0006136 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]==:此次暂停的持续时间, 以 user, system 和 real time 3个部分进行衡量。

*第二阶段：* concurrent-mark(并发标记)：
从`GC Root`开始并发标记老年代中存活的对象。由于是并发,所以是与应用程序同时运行的,不用暂停的阶段。 注意, 并非所有老年代中存活的对象都在此阶段被标记, 因为在标记过程中应用程序在运行，对象的引用关系就还会发生变化。
```java
[CMS-concurrent-mark: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
```
- ==CMS-concurrent-mark==：该阶段名称叫做`concurrent mark`，遍历老年代并标记所有的存活对象。
- ==0.001/0.001 secs==：此阶段的持续时间, 分别是运行时间和相应的实际时间。
- ==[Times: user=0.00 sys=0.00, real=0.00 secs]==：这部分对并发阶段来说没多少意义, 因为是从并发标记开始时计算的,而这段时间内不仅并发标记在运行,程序也在运行。

*第三阶段：* concurrent-preclean(并发标记预清理)：
此阶段同样是与应用线程并行执行的, 不需要停止应用线程。 因为前一阶段是与程序并发进行的，可能有一些引用已经改变。如果在并发标记过程中发生了引用关系变化，JVM会将发生了改变的区域标记为“脏”区，统计脏对象。
```java
[CMS-concurrent-preclean: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
```
- ==CMS-concurrent-preclean==：该阶段名称叫做`concurrent preclean`，统计此前的标记阶段中发生了改变的对象。
- ==0.001/0.001 secs==：此阶段的持续时间, 分别是运行时间和相应的实际时间。
- ==[Times: user=0.00 sys=0.00, real=0.00 secs]==：这部分对并发阶段来说没多少意义, 因为是从并发标记开始时计算的,而这段时间内不仅并发标记在运行,程序也在运行。


*第四阶段：* Concurrent Abortable Preclean(并发可取消的预清理)：
此阶段也不停止应用线程. 本阶段尝试在` STW` 的 `Final Remark` 之前尽可能地多做一些工作。
```java
[CMS-concurrent-abortable-preclean: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
```
- ==CMS-concurrent-abortable-preclean==：该阶段名称叫做`concurrent abortable preclean`。
- ==0.000/0.000 secs==：此阶段的持续时间, 分别是运行时间和相应的实际时间。
- ==[Times: user=0.00 sys=0.00, real=0.01 secs]==：这部分对并发阶段来说没多少意义, 因为是从并发标记开始时计算的,而这段时间内不仅并发标记在运行,程序也在运行。
 
*第五阶段：* Final Remark(最终标记)：
这是此次GC事件中第二次(也是最后一次)STW阶段。本阶段的目标是完成老年代中所有存活对象的标记. 因为之前的 preclean 阶段是并发的, 有可能无法跟上应用程序的变化速度。所以需要 STW暂停来处理复杂情况。
```java
[GC (CMS Final Remark) [YG occupancy: 28785 K (157248 K)][Rescan (parallel) , 0.0002592 secs][weak refs processing, 0.0000208 secs][class unloading, 0.0008936 secs][scrub symbol table, 0.0002973 secs][scrub string table, 0.0000928 secs][1 CMS-remark: 326699K(349568K)] 355485K(506816K), 0.0049802 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]


```
- ==CMS Final Remark==：此阶段的名称为 `Final Remark`, 标记老年代中所有存活的对象，包括在此前的并发标记过程中创建/修改的引用。
- ==YG occupancy: 28785 K (157248 K)==： 当前年轻代的使用量和总容量。
- ==Rescan (parallel) , 0.0002592 secs==: 在程序暂停时重新进行扫描(Rescan),以完成存活对象的标记。此时 rescan 是并行执行的,消耗的时间为 0.0002592秒。
- ==weak refs processing, 0.0000208 secs==： 处理弱引用，消耗的时间是0.0000208秒。
- ==class unloading, 0.0008936 secs==：卸载不使用的类，消耗的时间是0.0008936秒。
- ==scrub symbol table, 0.0002973 secs==：清理持有class级别 metadata 的符号表(symbol tables)，消耗的时间是 0.0002973 秒。
- ==scrub string table, 0.0000928 secs==：清理内部化字符串对应的 string tables，消耗的时间是0.0000928秒。
- ==CMS-remark:326699K(349568K)==：清除完成后老年代的使用量和总容量。
- ==355485K(506816K)==：此阶段完成后整个堆内存的使用量和总容量。
- ==0.0049802 secs==：此阶段的持续时间。
- ==Times: user=0.00 sys=0.00, real=0.00 secs==：GC事件的持续时间, 通过不同的类别来衡量: user, system and real time。

*第六阶段：* concurrent sweep(并发清除)：
阶段与应用程序并发执行,不需要STW停顿。目的是删除未使用的对象,并收回他们占用的空间。
```java
[CMS-concurrent-sweep: 0.001/0.001 secs] [Times: user=0.02 sys=0.00, real=0.01 secs]
```
- ==concurrent-sweep==：该阶段的名称是`concurrent sweep`。
- ==0.001/0.001 secs==：此阶段的持续时间, 分别是运行时间和实际时间。
- ==Times: user=0.01 sys=0.00, real=0.01 secs==：部分对并发阶段来说没有多少意义, 因为是从并发标记开始时计算的,而这段时间内不仅是并发标记在运行,程序也在运行。


*第七阶段：* concurrent reset(并发重置)：
此阶段与应用程序并发执行,重置CMS算法相关的内部数据, 为下一次GC循环做准备。
```java
[CMS-concurrent-reset: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
```
- ==concurrent-reset==：该阶段的名称是`concurrent reset`。
- ==0.000/0.000 secs==：此阶段的持续时间, 分别是运行时间和实际时间。
- ==Times: user=0.00 sys=0.00, real=0.00 secs==：部分对并发阶段来说没有多少意义, 因为是从并发标记开始时计算的,而这段时间内不仅是并发标记在运行,程序也在运行。


小结，CMS垃圾收集器的目标是减少停顿时间，阶段划分的更加详细，大量的并发线程执行的工作并不需要暂停应用线程。CMS的缺点是老年代内存碎片问题, 在某些情况下GC会造成不可预测的暂停时间, 特别是堆内存较大的情况下。


**4.G1 GC**

G1 – Garbage First(垃圾优先算法)，G1 GC最主要的设计目标是： 将STW停顿的时间和分布变成可预期以及可配置的。

G1适合大内存，需要低延迟的场景。G1 GC的日志信息太多了，这里只给出一种配置进行分析。

案例（ -Xms4g -Xmx4g）
```java
D:\course\JavaPro\week2>java -XX:+UseG1GC -Xms4g -Xmx4g -XX:+PrintGCDetails GCLogAnalysis
正在执行...
[GC pause (G1 Evacuation Pause) (young), 0.0099569 secs]
   [Parallel Time: 7.9 ms, GC Workers: 8]
      [GC Worker Start (ms): Min: 180.0, Avg: 180.1, Max: 180.1, Diff: 0.1]
      [Ext Root Scanning (ms): Min: 0.0, Avg: 0.1, Max: 0.2, Diff: 0.2, Sum: 1.1]
      [Update RS (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.0]
         [Processed Buffers: Min: 0, Avg: 0.0, Max: 0, Diff: 0, Sum: 0]
      [Scan RS (ms): Min: 0.0, Avg: 0.0, Max: 0.1, Diff: 0.1, Sum: 0.1]
      [Code Root Scanning (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.0]
      [Object Copy (ms): Min: 7.3, Avg: 7.4, Max: 7.5, Diff: 0.3, Sum: 59.3]
      [Termination (ms): Min: 0.0, Avg: 0.2, Max: 0.3, Diff: 0.3, Sum: 1.4]
         [Termination Attempts: Min: 1, Avg: 1.0, Max: 1, Diff: 0, Sum: 8]
      [GC Worker Other (ms): Min: 0.0, Avg: 0.1, Max: 0.2, Diff: 0.2, Sum: 0.4]
      [GC Worker Total (ms): Min: 7.8, Avg: 7.8, Max: 7.8, Diff: 0.1, Sum: 62.4]
      [GC Worker End (ms): Min: 187.9, Avg: 187.9, Max: 187.9, Diff: 0.0]
   [Code Root Fixup: 0.0 ms]
   [Code Root Purge: 0.0 ms]
   [Clear CT: 0.6 ms]
   [Other: 1.5 ms]
      [Choose CSet: 0.0 ms]
      [Ref Proc: 0.4 ms]
      [Ref Enq: 0.0 ms]
      [Redirty Cards: 0.5 ms]
      [Humongous Register: 0.1 ms]
      [Humongous Reclaim: 0.0 ms]
      [Free CSet: 0.1 ms]
   [Eden: 204.0M(204.0M)->0.0B(178.0M) Survivors: 0.0B->26.0M Heap: 204.0M(4096.0M)->65.2M(4096.0M)]
 [Times: user=0.00 sys=0.00, real=0.01 secs]
[GC pause (G1 Evacuation Pause) (young), 0.0116306 secs]
   [Parallel Time: 9.9 ms, GC Workers: 8]
      [GC Worker Start (ms): Min: 215.6, Avg: 215.7, Max: 215.7, Diff: 0.1]
      [Ext Root Scanning (ms): Min: 0.1, Avg: 0.1, Max: 0.2, Diff: 0.1, Sum: 0.8]
      [Update RS (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.1]
         [Processed Buffers: Min: 0, Avg: 1.0, Max: 3, Diff: 3, Sum: 8]
      [Scan RS (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.1]
      [Code Root Scanning (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.0]
      [Object Copy (ms): Min: 9.4, Avg: 9.5, Max: 9.6, Diff: 0.2, Sum: 75.8]
      [Termination (ms): Min: 0.0, Avg: 0.1, Max: 0.2, Diff: 0.2, Sum: 0.9]
         [Termination Attempts: Min: 1, Avg: 1.0, Max: 1, Diff: 0, Sum: 8]
      [GC Worker Other (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.1]
      [GC Worker Total (ms): Min: 9.7, Avg: 9.7, Max: 9.8, Diff: 0.1, Sum: 77.8]
      [GC Worker End (ms): Min: 225.4, Avg: 225.4, Max: 225.4, Diff: 0.0]
   [Code Root Fixup: 0.0 ms]
   [Code Root Purge: 0.0 ms]
   [Clear CT: 0.2 ms]
   [Other: 1.5 ms]
      [Choose CSet: 0.0 ms]
      [Ref Proc: 0.2 ms]
      [Ref Enq: 0.0 ms]
      [Redirty Cards: 0.7 ms]
      [Humongous Register: 0.1 ms]
      [Humongous Reclaim: 0.0 ms]
      [Free CSet: 0.1 ms]
   [Eden: 178.0M(178.0M)->0.0B(178.0M) Survivors: 26.0M->26.0M Heap: 243.2M(4096.0M)->123.6M(4096.0M)]
 [Times: user=0.02 sys=0.00, real=0.03 secs]
[GC pause (G1 Evacuation Pause) (young), 0.0130914 secs]
   [Parallel Time: 11.0 ms, GC Workers: 8]
      [GC Worker Start (ms): Min: 268.1, Avg: 268.1, Max: 268.2, Diff: 0.1]
      [Ext Root Scanning (ms): Min: 0.1, Avg: 0.1, Max: 0.2, Diff: 0.1, Sum: 0.8]
      [Update RS (ms): Min: 0.0, Avg: 0.1, Max: 0.7, Diff: 0.6, Sum: 1.2]
         [Processed Buffers: Min: 1, Avg: 1.4, Max: 2, Diff: 1, Sum: 11]
      [Scan RS (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.1]
      [Code Root Scanning (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.0]
      [Object Copy (ms): Min: 9.9, Avg: 10.4, Max: 10.7, Diff: 0.7, Sum: 83.3]
      [Termination (ms): Min: 0.0, Avg: 0.2, Max: 0.5, Diff: 0.5, Sum: 1.8]
         [Termination Attempts: Min: 1, Avg: 1.0, Max: 1, Diff: 0, Sum: 8]
      [GC Worker Other (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.1]
      [GC Worker Total (ms): Min: 10.8, Avg: 10.9, Max: 10.9, Diff: 0.1, Sum: 87.1]
      [GC Worker End (ms): Min: 279.0, Avg: 279.0, Max: 279.0, Diff: 0.0]
   [Code Root Fixup: 0.0 ms]
   [Code Root Purge: 0.0 ms]
   [Clear CT: 0.4 ms]
   [Other: 1.7 ms]
      [Choose CSet: 0.0 ms]
      [Ref Proc: 0.3 ms]
      [Ref Enq: 0.0 ms]
      [Redirty Cards: 0.8 ms]
      [Humongous Register: 0.1 ms]
      [Humongous Reclaim: 0.0 ms]
      [Free CSet: 0.1 ms]
   [Eden: 178.0M(178.0M)->0.0B(178.0M) Survivors: 26.0M->26.0M Heap: 301.6M(4096.0M)->183.8M(4096.0M)]
 [Times: user=0.06 sys=0.06, real=0.03 secs]
[GC pause (G1 Evacuation Pause) (young), 0.0114275 secs]
   [Parallel Time: 10.2 ms, GC Workers: 8]
      [GC Worker Start (ms): Min: 320.3, Avg: 320.5, Max: 321.0, Diff: 0.7]
      [Ext Root Scanning (ms): Min: 0.0, Avg: 0.1, Max: 0.2, Diff: 0.2, Sum: 0.7]
      [Update RS (ms): Min: 0.0, Avg: 0.2, Max: 1.1, Diff: 1.1, Sum: 1.4]
         [Processed Buffers: Min: 0, Avg: 1.3, Max: 2, Diff: 2, Sum: 10]
      [Scan RS (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.1]
      [Code Root Scanning (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.0]
      [Object Copy (ms): Min: 8.7, Avg: 9.6, Max: 9.8, Diff: 1.1, Sum: 76.6]
      [Termination (ms): Min: 0.0, Avg: 0.1, Max: 0.1, Diff: 0.1, Sum: 0.6]
         [Termination Attempts: Min: 1, Avg: 1.0, Max: 1, Diff: 0, Sum: 8]
      [GC Worker Other (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.1]
      [GC Worker Total (ms): Min: 9.4, Avg: 9.9, Max: 10.1, Diff: 0.7, Sum: 79.5]
      [GC Worker End (ms): Min: 330.4, Avg: 330.4, Max: 330.4, Diff: 0.0]
   [Code Root Fixup: 0.0 ms]
   [Code Root Purge: 0.0 ms]
   [Clear CT: 0.1 ms]
   [Other: 1.2 ms]
      [Choose CSet: 0.0 ms]
      [Ref Proc: 0.1 ms]
      [Ref Enq: 0.0 ms]
      [Redirty Cards: 0.6 ms]
      [Humongous Register: 0.1 ms]
      [Humongous Reclaim: 0.0 ms]
      [Free CSet: 0.1 ms]
   [Eden: 178.0M(178.0M)->0.0B(178.0M) Survivors: 26.0M->26.0M Heap: 361.8M(4096.0M)->236.2M(4096.0M)]
 [Times: user=0.00 sys=0.00, real=0.02 secs]
[GC pause (G1 Evacuation Pause) (young), 0.0119627 secs]
   [Parallel Time: 9.9 ms, GC Workers: 8]
      [GC Worker Start (ms): Min: 369.7, Avg: 369.8, Max: 369.9, Diff: 0.1]
      [Ext Root Scanning (ms): Min: 0.1, Avg: 0.1, Max: 0.2, Diff: 0.1, Sum: 0.8]
      [Update RS (ms): Min: 0.0, Avg: 0.0, Max: 0.1, Diff: 0.1, Sum: 0.3]
         [Processed Buffers: Min: 1, Avg: 1.3, Max: 2, Diff: 1, Sum: 10]
      [Scan RS (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.1]
      [Code Root Scanning (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.0]
      [Object Copy (ms): Min: 9.3, Avg: 9.4, Max: 9.6, Diff: 0.4, Sum: 75.3]
      [Termination (ms): Min: 0.0, Avg: 0.2, Max: 0.4, Diff: 0.4, Sum: 1.7]
         [Termination Attempts: Min: 1, Avg: 1.0, Max: 1, Diff: 0, Sum: 8]
      [GC Worker Other (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.1]
      [GC Worker Total (ms): Min: 9.7, Avg: 9.8, Max: 9.9, Diff: 0.1, Sum: 78.3]
      [GC Worker End (ms): Min: 379.6, Avg: 379.6, Max: 379.6, Diff: 0.0]
   [Code Root Fixup: 0.0 ms]
   [Code Root Purge: 0.0 ms]
   [Clear CT: 0.4 ms]
   [Other: 1.6 ms]
      [Choose CSet: 0.0 ms]
      [Ref Proc: 0.2 ms]
      [Ref Enq: 0.0 ms]
      [Redirty Cards: 0.9 ms]
      [Humongous Register: 0.1 ms]
      [Humongous Reclaim: 0.0 ms]
      [Free CSet: 0.1 ms]
   [Eden: 178.0M(178.0M)->0.0B(178.0M) Survivors: 26.0M->26.0M Heap: 414.2M(4096.0M)->283.9M(4096.0M)]
 [Times: user=0.00 sys=0.00, real=0.03 secs]
[GC pause (G1 Evacuation Pause) (young), 0.0125605 secs]
   [Parallel Time: 11.1 ms, GC Workers: 8]
      [GC Worker Start (ms): Min: 420.1, Avg: 420.2, Max: 420.3, Diff: 0.2]
      [Ext Root Scanning (ms): Min: 0.0, Avg: 0.1, Max: 0.2, Diff: 0.2, Sum: 0.6]
      [Update RS (ms): Min: 0.0, Avg: 0.1, Max: 0.1, Diff: 0.1, Sum: 0.5]
         [Processed Buffers: Min: 1, Avg: 1.1, Max: 2, Diff: 1, Sum: 9]
      [Scan RS (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.1]
      [Code Root Scanning (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.0]
      [Object Copy (ms): Min: 10.3, Avg: 10.5, Max: 10.7, Diff: 0.4, Sum: 84.1]
      [Termination (ms): Min: 0.0, Avg: 0.1, Max: 0.3, Diff: 0.3, Sum: 1.2]
         [Termination Attempts: Min: 1, Avg: 1.0, Max: 1, Diff: 0, Sum: 8]
      [GC Worker Other (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.1]
      [GC Worker Total (ms): Min: 10.8, Avg: 10.8, Max: 10.9, Diff: 0.2, Sum: 86.5]
      [GC Worker End (ms): Min: 431.0, Avg: 431.0, Max: 431.0, Diff: 0.0]
   [Code Root Fixup: 0.0 ms]
   [Code Root Purge: 0.0 ms]
   [Clear CT: 0.1 ms]
   [Other: 1.3 ms]
      [Choose CSet: 0.0 ms]
      [Ref Proc: 0.2 ms]
      [Ref Enq: 0.0 ms]
      [Redirty Cards: 0.7 ms]
      [Humongous Register: 0.1 ms]
      [Humongous Reclaim: 0.0 ms]
      [Free CSet: 0.1 ms]
   [Eden: 178.0M(178.0M)->0.0B(178.0M) Survivors: 26.0M->26.0M Heap: 461.9M(4096.0M)->343.5M(4096.0M)]
 [Times: user=0.11 sys=0.02, real=0.03 secs]
[GC pause (G1 Evacuation Pause) (young), 0.0114717 secs]
   [Parallel Time: 10.4 ms, GC Workers: 8]
      [GC Worker Start (ms): Min: 473.3, Avg: 473.4, Max: 473.8, Diff: 0.5]
      [Ext Root Scanning (ms): Min: 0.0, Avg: 0.1, Max: 0.1, Diff: 0.1, Sum: 0.8]
      [Update RS (ms): Min: 0.0, Avg: 0.0, Max: 0.1, Diff: 0.1, Sum: 0.4]
         [Processed Buffers: Min: 0, Avg: 1.1, Max: 3, Diff: 3, Sum: 9]
      [Scan RS (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.1]
      [Code Root Scanning (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.0]
      [Object Copy (ms): Min: 9.7, Avg: 9.9, Max: 10.2, Diff: 0.5, Sum: 79.5]
      [Termination (ms): Min: 0.0, Avg: 0.2, Max: 0.3, Diff: 0.3, Sum: 1.5]
         [Termination Attempts: Min: 1, Avg: 1.0, Max: 1, Diff: 0, Sum: 8]
      [GC Worker Other (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.1]
      [GC Worker Total (ms): Min: 9.9, Avg: 10.3, Max: 10.4, Diff: 0.5, Sum: 82.3]
      [GC Worker End (ms): Min: 483.7, Avg: 483.7, Max: 483.7, Diff: 0.0]
   [Code Root Fixup: 0.0 ms]
   [Code Root Purge: 0.0 ms]
   [Clear CT: 0.1 ms]
   [Other: 0.9 ms]
      [Choose CSet: 0.0 ms]
      [Ref Proc: 0.1 ms]
      [Ref Enq: 0.0 ms]
      [Redirty Cards: 0.3 ms]
      [Humongous Register: 0.1 ms]
      [Humongous Reclaim: 0.0 ms]
      [Free CSet: 0.0 ms]
   [Eden: 178.0M(178.0M)->0.0B(210.0M) Survivors: 26.0M->26.0M Heap: 521.5M(4096.0M)->398.9M(4096.0M)]
 [Times: user=0.06 sys=0.05, real=0.02 secs]
[GC pause (G1 Evacuation Pause) (young), 0.0128721 secs]
   [Parallel Time: 11.6 ms, GC Workers: 8]
      [GC Worker Start (ms): Min: 526.4, Avg: 526.5, Max: 526.6, Diff: 0.1]
      [Ext Root Scanning (ms): Min: 0.0, Avg: 0.1, Max: 0.2, Diff: 0.1, Sum: 0.7]
      [Update RS (ms): Min: 0.0, Avg: 0.1, Max: 0.2, Diff: 0.2, Sum: 0.7]
         [Processed Buffers: Min: 0, Avg: 1.3, Max: 3, Diff: 3, Sum: 10]
      [Scan RS (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.1]
      [Code Root Scanning (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.0]
      [Object Copy (ms): Min: 11.0, Avg: 11.1, Max: 11.2, Diff: 0.2, Sum: 89.0]
      [Termination (ms): Min: 0.0, Avg: 0.1, Max: 0.2, Diff: 0.2, Sum: 1.0]
         [Termination Attempts: Min: 1, Avg: 1.0, Max: 1, Diff: 0, Sum: 8]
      [GC Worker Other (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.1]
      [GC Worker Total (ms): Min: 11.4, Avg: 11.5, Max: 11.5, Diff: 0.1, Sum: 91.6]
      [GC Worker End (ms): Min: 538.0, Avg: 538.0, Max: 538.0, Diff: 0.0]
   [Code Root Fixup: 0.0 ms]
   [Code Root Purge: 0.0 ms]
   [Clear CT: 0.4 ms]
   [Other: 0.8 ms]
      [Choose CSet: 0.0 ms]
      [Ref Proc: 0.1 ms]
      [Ref Enq: 0.0 ms]
      [Redirty Cards: 0.3 ms]
      [Humongous Register: 0.1 ms]
      [Humongous Reclaim: 0.0 ms]
      [Free CSet: 0.1 ms]
   [Eden: 210.0M(210.0M)->0.0B(622.0M) Survivors: 26.0M->30.0M Heap: 608.9M(4096.0M)->465.1M(4096.0M)]
 [Times: user=0.00 sys=0.00, real=0.02 secs]
[GC pause (G1 Evacuation Pause) (young), 0.0231822 secs]
   [Parallel Time: 21.5 ms, GC Workers: 8]
      [GC Worker Start (ms): Min: 698.9, Avg: 699.0, Max: 699.1, Diff: 0.1]
      [Ext Root Scanning (ms): Min: 0.0, Avg: 0.1, Max: 0.2, Diff: 0.1, Sum: 0.7]
      [Update RS (ms): Min: 0.0, Avg: 0.1, Max: 0.1, Diff: 0.1, Sum: 0.6]
         [Processed Buffers: Min: 1, Avg: 1.1, Max: 2, Diff: 1, Sum: 9]
      [Scan RS (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.2]
      [Code Root Scanning (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.0]
      [Object Copy (ms): Min: 20.9, Avg: 21.0, Max: 21.1, Diff: 0.2, Sum: 167.9]
      [Termination (ms): Min: 0.0, Avg: 0.1, Max: 0.2, Diff: 0.2, Sum: 0.8]
         [Termination Attempts: Min: 1, Avg: 1.0, Max: 1, Diff: 0, Sum: 8]
      [GC Worker Other (ms): Min: 0.0, Avg: 0.0, Max: 0.1, Diff: 0.0, Sum: 0.2]
      [GC Worker Total (ms): Min: 21.3, Avg: 21.3, Max: 21.4, Diff: 0.1, Sum: 170.4]
      [GC Worker End (ms): Min: 720.3, Avg: 720.3, Max: 720.3, Diff: 0.0]
   [Code Root Fixup: 0.0 ms]
   [Code Root Purge: 0.0 ms]
   [Clear CT: 0.4 ms]
   [Other: 1.3 ms]
      [Choose CSet: 0.0 ms]
      [Ref Proc: 0.2 ms]
      [Ref Enq: 0.0 ms]
      [Redirty Cards: 0.6 ms]
      [Humongous Register: 0.0 ms]
      [Humongous Reclaim: 0.0 ms]
      [Free CSet: 0.1 ms]
   [Eden: 622.0M(622.0M)->0.0B(212.0M) Survivors: 30.0M->82.0M Heap: 1087.1M(4096.0M)->628.2M(4096.0M)]
 [Times: user=0.03 sys=0.06, real=0.08 secs]
[GC pause (G1 Evacuation Pause) (young), 0.0155001 secs]
   [Parallel Time: 14.3 ms, GC Workers: 8]
      [GC Worker Start (ms): Min: 814.4, Avg: 814.5, Max: 814.6, Diff: 0.2]
      [Ext Root Scanning (ms): Min: 0.0, Avg: 0.1, Max: 0.2, Diff: 0.2, Sum: 0.6]
      [Update RS (ms): Min: 0.0, Avg: 0.1, Max: 0.2, Diff: 0.1, Sum: 0.6]
         [Processed Buffers: Min: 1, Avg: 1.1, Max: 2, Diff: 1, Sum: 9]
      [Scan RS (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.1]
      [Code Root Scanning (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.0]
      [Object Copy (ms): Min: 13.6, Avg: 13.8, Max: 13.9, Diff: 0.3, Sum: 110.2]
      [Termination (ms): Min: 0.0, Avg: 0.2, Max: 0.3, Diff: 0.3, Sum: 1.2]
         [Termination Attempts: Min: 1, Avg: 1.0, Max: 1, Diff: 0, Sum: 8]
      [GC Worker Other (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.1]
      [GC Worker Total (ms): Min: 14.1, Avg: 14.1, Max: 14.2, Diff: 0.2, Sum: 112.9]
      [GC Worker End (ms): Min: 828.6, Avg: 828.6, Max: 828.6, Diff: 0.0]
   [Code Root Fixup: 0.0 ms]
   [Code Root Purge: 0.0 ms]
   [Clear CT: 0.2 ms]
   [Other: 1.0 ms]
      [Choose CSet: 0.0 ms]
      [Ref Proc: 0.1 ms]
      [Ref Enq: 0.0 ms]
      [Redirty Cards: 0.5 ms]
      [Humongous Register: 0.1 ms]
      [Humongous Reclaim: 0.0 ms]
      [Free CSet: 0.1 ms]
   [Eden: 212.0M(212.0M)->0.0B(390.0M) Survivors: 82.0M->38.0M Heap: 840.2M(4096.0M)->685.6M(4096.0M)]
 [Times: user=0.05 sys=0.08, real=0.04 secs]
[GC pause (G1 Evacuation Pause) (young), 0.0155628 secs]
   [Parallel Time: 14.6 ms, GC Workers: 8]
      [GC Worker Start (ms): Min: 905.1, Avg: 905.2, Max: 905.2, Diff: 0.1]
      [Ext Root Scanning (ms): Min: 0.1, Avg: 0.1, Max: 0.2, Diff: 0.1, Sum: 0.7]
      [Update RS (ms): Min: 0.0, Avg: 0.1, Max: 0.3, Diff: 0.3, Sum: 0.8]
         [Processed Buffers: Min: 1, Avg: 1.1, Max: 2, Diff: 1, Sum: 9]
      [Scan RS (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.1]
      [Code Root Scanning (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.0]
      [Object Copy (ms): Min: 13.8, Avg: 14.1, Max: 14.3, Diff: 0.5, Sum: 112.6]
      [Termination (ms): Min: 0.0, Avg: 0.1, Max: 0.3, Diff: 0.3, Sum: 1.2]
         [Termination Attempts: Min: 1, Avg: 1.0, Max: 1, Diff: 0, Sum: 8]
      [GC Worker Other (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.1]
      [GC Worker Total (ms): Min: 14.4, Avg: 14.4, Max: 14.5, Diff: 0.1, Sum: 115.6]
      [GC Worker End (ms): Min: 919.6, Avg: 919.6, Max: 919.6, Diff: 0.0]
   [Code Root Fixup: 0.0 ms]
   [Code Root Purge: 0.0 ms]
   [Clear CT: 0.2 ms]
   [Other: 0.7 ms]
      [Choose CSet: 0.0 ms]
      [Ref Proc: 0.0 ms]
      [Ref Enq: 0.0 ms]
      [Redirty Cards: 0.2 ms]
      [Humongous Register: 0.1 ms]
      [Humongous Reclaim: 0.0 ms]
      [Free CSet: 0.1 ms]
   [Eden: 390.0M(390.0M)->0.0B(502.0M) Survivors: 38.0M->54.0M Heap: 1075.6M(4096.0M)->785.2M(4096.0M)]
 [Times: user=0.01 sys=0.11, real=0.03 secs]
[GC pause (G1 Evacuation Pause) (young), 0.0216765 secs]
   [Parallel Time: 19.7 ms, GC Workers: 8]
      [GC Worker Start (ms): Min: 997.7, Avg: 997.7, Max: 997.8, Diff: 0.1]
      [Ext Root Scanning (ms): Min: 0.1, Avg: 0.1, Max: 0.2, Diff: 0.1, Sum: 0.7]
      [Update RS (ms): Min: 0.1, Avg: 0.1, Max: 0.2, Diff: 0.1, Sum: 0.8]
         [Processed Buffers: Min: 1, Avg: 1.1, Max: 2, Diff: 1, Sum: 9]
      [Scan RS (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.2]
      [Code Root Scanning (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.0]
      [Object Copy (ms): Min: 19.1, Avg: 19.2, Max: 19.3, Diff: 0.2, Sum: 153.9]
      [Termination (ms): Min: 0.0, Avg: 0.1, Max: 0.2, Diff: 0.2, Sum: 0.7]
         [Termination Attempts: Min: 1, Avg: 1.0, Max: 1, Diff: 0, Sum: 8]
      [GC Worker Other (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.1]
      [GC Worker Total (ms): Min: 19.5, Avg: 19.6, Max: 19.6, Diff: 0.1, Sum: 156.4]
      [GC Worker End (ms): Min: 1017.3, Avg: 1017.3, Max: 1017.3, Diff: 0.0]
   [Code Root Fixup: 0.0 ms]
   [Code Root Purge: 0.0 ms]
   [Clear CT: 0.4 ms]
   [Other: 1.6 ms]
      [Choose CSet: 0.0 ms]
      [Ref Proc: 0.2 ms]
      [Ref Enq: 0.0 ms]
      [Redirty Cards: 0.8 ms]
      [Humongous Register: 0.1 ms]
      [Humongous Reclaim: 0.0 ms]
      [Free CSet: 0.1 ms]
   [Eden: 502.0M(502.0M)->0.0B(760.0M) Survivors: 54.0M->70.0M Heap: 1287.2M(4096.0M)->920.4M(4096.0M)]
 [Times: user=0.05 sys=0.08, real=0.11 secs]
执行结束!共生成对象次数:11931
Heap
 garbage-first heap   total 4194304K, used 1003944K [0x00000006c0000000, 0x00000006c0204000, 0x00000007c0000000)
  region size 2048K, 66 young (135168K), 35 survivors (71680K)
 Metaspace       used 2619K, capacity 4486K, committed 4864K, reserved 1056768K
  class space    used 288K, capacity 386K, committed 512K, reserved 1048576K

```

对`G1 GC`日志中的字段进行解释：
- ==[GC pause (G1 Evacuation Pause) (young), 0.0099569 secs]==:G1转移暂停,只清理年轻代空间。持续的系统时间为0099569秒 。
- ==[Parallel Time: 7.9 ms, GC Workers: 8]==：表明后面的活动由8个 Worker 线程并行执行, 消耗时间为7.9毫秒。
- ==GC Worker Start (ms)==：相对于 pause 开始的时间戳，GC的worker线程开始启动所用时间。如果 Min 和 Max 差别很大，则表明本机其他进程所使用的线程数量过多， 挤占了GC的CPU时间。
- ==Ext Root Scanning (ms)==：用了多长时间来扫描堆外(non-heap)的root, 如 classloaders, JNI引用, JVM的系统root等。后面显示了运行时间, “Sum” 指的是CPU时间。
- ==Update RS (ms)==：更新Remembered Sets使用的时间。
- ==Processed Buffers==：每个 worker 线程处理了多少个本地缓冲区(local buffer)。
- ==Scan RS (ms)==：用了多长时间扫描来自RSet的引用。
- ==Code Root Scanning (ms)==：用了多长时间来扫描实际代码中的 root。
- ==Object Copy (ms)==：用了多长时间来拷贝收集区内的存活对象。
- ==Termination (ms)==：GC的worker线程用了多长时间来确保自身可以安全地停止, 这段时间什么也不用做, stop 之后该线程就终止运行了。
- ==Termination Attempts==：GC的worker 线程尝试多少次 try 和 teminate。如果worker发现还有一些任务没处理完,则这一次尝试就是失败的, 暂时还不能终止。
- ==GC Worker Other (ms)==：处理一些琐碎的小活动所用 的时间，在GC日志中不值得单独列出来。
- ==GC Worker Total (ms)==：GC的worker 线程的工作时间总计。
- ==GC Worker End (ms)==：相对于 pause 开始的时间戳，GC的worker 线程完成作业的时间。通常来说这部分数字应该大致相等, 否则就说明有太多的线程被挂起, 很可能是因为坏邻居效应(noisy neighbor) 所导致的。
- ==Code Root Fixup==：释放用于管理并行活动的内部数据。一般都接近于零。这是串行执行的过程。
- ==Code Root Purge==：清理其他部分数据, 也是非常快的, 但如非必要则几乎等于零。这是串行执行的过程。
- ==Clear CT==：清理 card table 中 cards 的时间。清理工作只是简单地删除“脏”状态, 此状态用来标识一个字段是否被更新的, 供Remembered Sets使用。
- ==Other==：其他活动消耗的时间, 其中有很多也是并行执行的。
- ==Choose CSet==：选择CSet使用时间，CSet是指`Collection Set`，和RSet(Remembered Set)一样，用于辅助GC，用空间换时间。
- ==Ref Proc==：处理非强引用(non-strong)的时间: 进行清理或者决定是否需要清理。
- ===Ref Enq==：用来将剩下的 non-strong 引用排列到合适的 ReferenceQueue中。
- ==Redirty Cards==：重新脏化卡片。排队引用可能会更新RSet，所以需要对关联的Card重新脏化。
- ==Humongous Register==和==Humongous Reclaim==：主要是对巨型对象回收的信息，youngGC阶段会对RSet中有引用的短命的巨型对象进行回收，巨型对象会直接回收而不需要进行转移（转移代价巨大，也没必要）。
- ==Free CSet==：释放CSet中的region到free list使用时间。
- ==Eden: 204.0M(204.0M)->0.0B(178.0M) Survivors: 0.0B->26.0M Heap: 204.0M(4096.0M)->65.2M(4096.0M)==：暂停之前和暂停之后, Eden 区的使用量/总容量。 暂停之前和暂停之后, 存活区的使用量。暂停之前和暂停之后, 整个堆内存的使用量与总容量。
- ==Times: user=0.00 sys=0.00, real=0.01 secs==： GC事件的持续时间, 通过三个部分（用户线程，系统调用， 应用程序暂停的时间）来衡量。

小结：
- G1是一个有整理内存过程的垃圾收集器，不会产生很多内存碎片。
- 在实现高吞吐量的同时，G1的Stop The World(STW)更可控，在停顿时间上添加了预测机制，用户可以指定期望停顿时间。
- G1能与应用程序线程并发执行

参考资料：
- [Java Hotspot G1 GC的一些关键技术](https://tech.meituan.com/2016/09/23/g1.html)
- [JVM性能调优实践——G1 垃圾收集器分析、调优篇
](https://blog.csdn.net/lijingyao8206/article/details/80566384)
- [4. GC 算法(实现篇) - GC参考手册](https://blog.csdn.net/renfufei/article/details/54885190)