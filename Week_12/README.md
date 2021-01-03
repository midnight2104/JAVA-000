> 23课作业：
> 1、（必做）配置redis的主从复制，sentiel高可用，Cluster集群。
> 提交如下内容到github：
> 1）config配置文件，
> 2）启动和操作、验证集群下数据读写的命令步骤。



### 搭建Redis单机

1. redis下载与安装

   ```c
   $ wget https://download.redis.io/releases/redis-6.0.9.tar.gz
   $ tar xzf redis-6.0.9.tar.gz
   $ cd redis-6.0.9
   $ make
   ```

2. 编译完成后，修改redis.conf文件

   > daemonize yes  # 开启守护进程模式，在后台运行
   > bind 192.168.14.130 # 绑定真正的物理机IP

3. 启动redis服务:
   ```$ src/redis-server redis.conf```

4. 查看是否启动成功：
   ```$ ps -ef | grep redis```

5. 客户端连接：
   ```c
   
   192.168.14.130:6379> set hello world
   OK
   192.168.14.130:6379> get hello
   "world"
   
   ```



### 搭建Redis主从

1. 将之前的压缩文件再解压一份出来，作为从机slave.
   ```$ tar xzf redis-6.0.9.tar.gz -C redis-slave```
   进入slave的目录，进行make编译

2. 编译完成后，修改redis.conf文件。主机的配置文件重命名为`redis-master.conf`，从机的配置文件重命名为`redis-slave.conf`

   也可以在客户端使用命令```slaveof ip port```，指定主从关系。```slaveof no one```,取消主从关系。

   > daemonize yes  # 开启守护进程模式，在后台运行
   > bind 192.168.14.130 # 绑定真正的物理机IP
   > port 6389 # 端口
   > replicaof 192.168.14.130 6379 # 指定主机

3. 启动从机:
   ```$ src/redis-server redis-slave.conf```

4. 客户端连接：

   ```c
   $ src/redis-cli -h 192.168.14.130 -p 6389
   192.168.14.130:6389> keys *
   "hello"
   ```

   可以看见主机的key已经同步到从机了。

#### Redis Sentinel高可用

1. 设置哨兵
   修改sentinel.conf文件，配置文件重命名为sentinel0.conf

   > daemonize no  # 前台运行，方便看日志
   > bind 192.168.14.130 # 绑定真正的物理机IP
   > port 26379 # 端口
   > sentinel monitor mymaster 192.168.14.130 6379 2 #监控主节点
   > sentiel down-after-milseconds mymaster 10000 # 10秒后没有PING/PONG，则主观下线
   > sentiel failover-timeout mymaster 180000 # 定义故障切换超时时间3分钟
   > sentiel parle-syncs mymaster 1 #在failover期间，允许多少个slave同时指向新的主节点，主要用于同步

2. 复制sentinel.conf文件，用于启动第二个哨兵.

   配置文件重命名为sentinel1.conf，注意修改myid和端口.

3. 启动哨兵:

```c
$ src/redis-sentinel sentinel0.conf
$ src/redis-sentinel sentinel1.conf
```

4. 主机shutdown
   $ src/redis-cli -h 192.168.14.130 -p 6379 shutdown

哨兵在10s后进行选举，选举过程如下：

> 68584:X 02 Jan 2021 19:58:38.839 # Sentinel ID is fad9919c3775f115a7839715c6e1c48c9bed67cc
> 68584:X 02 Jan 2021 19:58:38.839 # +monitor master mymaster 192.168.14.130 6379 quorum 2
> 68584:X 02 Jan 2021 19:59:37.790 * +sentinel sentinel fad9919c3775f115a7839715c6e1c48c9bed67cd 192.168.14.130 26380 @ mymaster 192.168.14.130 6379
> 68584:X 02 Jan 2021 20:01:04.578 # +sdown master mymaster 192.168.14.130 6379
> 68584:X 02 Jan 2021 20:01:04.655 # +odown master mymaster 192.168.14.130 6379 #quorum 2/2
> 68584:X 02 Jan 2021 20:01:04.655 # +new-epoch 1
> 68584:X 02 Jan 2021 20:01:04.655 # +try-failover master mymaster 192.168.14.130 6379
> 68584:X 02 Jan 2021 20:01:04.668 # +vote-for-leader fad9919c3775f115a7839715c6e1c48c9bed67cc 1
> 68584:X 02 Jan 2021 20:01:04.679 # fad9919c3775f115a7839715c6e1c48c9bed67cd voted for fad9919c3775f115a7839715c6e1c48c9bed67cc 1
> 68584:X 02 Jan 2021 20:01:04.741 # +elected-leader master mymaster 192.168.14.130 6379
> 68584:X 02 Jan 2021 20:01:04.741 # +failover-state-select-slave master mymaster 192.168.14.130 6379
> 68584:X 02 Jan 2021 20:01:04.797 # +selected-slave slave 192.168.14.130:6389 192.168.14.130 6389 @ mymaster 192.168.14.130 6379
> 68584:X 02 Jan 2021 20:01:04.797 * +failover-state-send-slaveof-noone slave 192.168.14.130:6389 192.168.14.130 6389 @ mymaster 192.168.14.130 6379
> 68584:X 02 Jan 2021 20:01:04.864 * +failover-state-wait-promotion slave 192.168.14.130:6389 192.168.14.130 6389 @ mymaster 192.168.14.130 6379
> 68584:X 02 Jan 2021 20:01:05.704 # +promoted-slave slave 192.168.14.130:6389 192.168.14.130 6389 @ mymaster 192.168.14.130 6379
> 68584:X 02 Jan 2021 20:01:05.704 # +failover-state-reconf-slaves master mymaster 192.168.14.130 6379
> 68584:X 02 Jan 2021 20:01:05.788 # +failover-end master mymaster 192.168.14.130 6379
> 68584:X 02 Jan 2021 20:01:05.789 # +switch-master mymaster 192.168.14.130 6379 192.168.14.130 6389
> 68584:X 02 Jan 2021 20:01:05.790 * +slave slave 192.168.14.130:6379 192.168.14.130 6379 @ mymaster 192.168.14.130 6389
> 68584:X 02 Jan 2021 20:01:15.881 # +sdown slave 192.168.14.130:6379 192.168.14.130 6379 @ mymaster 192.168.14.130 6389

5. 在从机查看主从角色，变成了master

   ```c
   $ info replication
   # Replication
   role:master
   connected_slaves:0
   master_replid:6e2e89e57cd6d15f7410c3a89fe5859e5965728b
   master_replid2:3e9609e878d61e2afb16e158cf554e3cd7315b95
   master_repl_offset:19039
   second_repl_offset:15299
   repl_backlog_active:1
   repl_backlog_size:1048576
   repl_backlog_first_byte_offset:1
   repl_backlog_histlen:19039
   ```

### Redis Cluster集群

1. Redis集群最少需要三台主服务器，三台从服务器。
   端口号分别为：7001~7006

> 第一步：创建7001实例，并编辑redis.conf文件，修改port为7001。
> 第二步：修改redis.conf配置文件，打开`Cluster-enable yes`。
> 第三步：复制7001，创建7002~7006实例，注意端口修改。
> 第四步：启动所有的实例。
> 第五步：创建Redis集群。
>
> ```c
> ./redis-cli --cluster create 192.168.14.130:7001 192.168.14.130:7002 192.168.14.130:7003 192.168.14.130:7004 192.168.14.130:7005 192.168.14.130:7006 --cluster-replicas 1
> ```
>
> 

2. 客户端连接集群：
   `./redis-cli –h 192.168.14.130 –p 7001 –c`

3.   查看集群状态

   ```c
   192.168.14.130:7001> cluster info
   cluster_state:ok
   cluster_slots_assigned:16384
   cluster_slots_ok:16384
   cluster_slots_pfail:0
   cluster_slots_fail:0
   cluster_known_nodes:6
   cluster_size:3
   cluster_current_epoch:6
   cluster_my_epoch:3
   cluster_stats_messages_sent:926
   cluster_stats_messages_received:926
   ```

   

4. 使用redis

   ```
   192.168.14.130:7001> set k1 11
   -> Redirected to slot [12706] located at 192.168.14.130:7003
   OK
   192.168.14.130:7001> set k4 44444
   -> Redirected to slot [8455] located at 192.168.14.130:7002
   OK
   ```

