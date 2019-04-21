package com.luocheng.test;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;

/**
 * @program: zookeeper-connection
 * @description: 建立curator与zkserver的连接演示demo
 * @author: 01
 * @create: 2018-04-28 09:44
 **/
public class CuratorConnect {

    // Curator客户端
    public CuratorFramework client = null;
    // 集群模式则是多个ip
    private static final String zkServerIps = "127.0.0.1:2181";

    public CuratorConnect(){
        /**
         * 同步创建zk示例，原生api是异步的
         * 这一步是设置重连策略
         *
         * ExponentialBackoffRetry构造器参数：
         *  curator链接zookeeper的策略:ExponentialBackoffRetry
         *  baseSleepTimeMs：初始sleep的时间
         *  maxRetries：最大重试次数
         *  maxSleepMs：最大重试时间
         */
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 5);

        // 实例化Curator客户端，Curator的编程风格可以让我们使用方法链的形式完成客户端的实例化
        client = CuratorFrameworkFactory.builder() // 使用工厂类来建造客户端的实例对象
                .connectString(zkServerIps)  // 放入zookeeper服务器ip
                .sessionTimeoutMs(10000).retryPolicy(retryPolicy)  // 设定会话时间以及重连策略
                .build();  // 建立连接通道

        // 启动Curator客户端
        client.start();
    }

    // 关闭zk客户端连接
    private void closeZKClient() {
        if (client != null) {
            this.client.close();
        }
    }

    public static void main(String[] args) throws Exception {
        // 实例化
        CuratorConnect curatorConnect = new CuratorConnect();
        // 获取当前客户端的状态
        CuratorFrameworkState zkState = curatorConnect.client.getState();
        boolean isZkCuratorStarted = zkState.equals(CuratorFrameworkState.STARTED) ? true : false;
        System.out.println("当前客户端的状态：" + (isZkCuratorStarted ? "连接中..." : "已关闭..."));

     // 创建节点
        String nodePath = "/super/testNode";  // 节点路径
        byte[] data = "this is a test data".getBytes();  // 节点数据
       // 创建父节点，也就是会递归创建  // 节点类型  // 节点的acl权限
        String result = curatorConnect.client.create().creatingParentsIfNeeded()
        		.withMode(CreateMode.PERSISTENT)
        		.withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
        		.forPath(nodePath, data);

        System.out.println(result + "节点，创建成功...");

        Thread.sleep(1000);

        // 关闭客户端
        curatorConnect.closeZKClient();

        // 获取当前客户端的状态
        zkState = curatorConnect.client.getState();
        isZkCuratorStarted = zkState.equals(CuratorFrameworkState.STARTED) ? true : false;
        System.out.println("当前客户端的状态：" + (isZkCuratorStarted ? "连接中..." : "已关闭..."));
    }
}