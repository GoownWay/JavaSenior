package nonclock;

import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;

/**
 * @author ckx
 * @create 2019-08-21-16:42
 */
public class NonClockTest1 {
   /* public static void main(String[] args) throws IOException {
        //        客户端：
//        1.获取通道
        SocketChannel sChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 9000));
//        2.切换非阻塞模式configureBlocking（false）
        sChannel.configureBlocking(false);
//        3.分配指定大小的缓冲区
        ByteBuffer bBuffer = ByteBuffer.allocate(1024);
//        4.发送数据给服务端(数据装入缓冲区，缓冲区模式切换，发送，清除缓冲区)
        Scanner scan = new Scanner(System.in);
        while (scan.hasNext()){
            String str = scan.nextLine();
            if(str.length() != 0){
                LocalDateTime date = LocalDateTime.now();
                String fmDate = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(date);
                bBuffer.put((fmDate + "\n" + str).getBytes());
                bBuffer.flip();
                sChannel.write(bBuffer);
                bBuffer.clear();
            }
        }
//        5.关闭资源
        sChannel.close();
    }*/

    @Test
    public void client() throws IOException {
//        客户端：
//        1.获取通道
        SocketChannel sChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 9000));
        FileChannel fChannel = FileChannel.open(Paths.get("test.txt"),StandardOpenOption.READ);
//        2.切换非阻塞模式configureBlocking（false）
        sChannel.configureBlocking(false);
//        3.分配指定大小的缓冲区
        ByteBuffer bBuffer = ByteBuffer.allocate(1024);
//        4.发送数据给服务端
        int len;
        while ((len = fChannel.read(bBuffer)) != -1){
            bBuffer.flip();
            sChannel.write(bBuffer);
            bBuffer.clear();
        }
//        5.关闭资源
        fChannel.close();
        sChannel.close();
    }

    @Test
    public void server() throws IOException {
//        服务端：
//        1.获取通道
        ServerSocketChannel ssChannel = ServerSocketChannel.open();
//        2.切换非阻塞模式
        ssChannel.configureBlocking(false);
//        3.绑定连接
        ssChannel.bind(new InetSocketAddress(9000));
//        4.获取选择器
        Selector selector = Selector.open();
//        5.将通道注册到选择器上：SelectionKey读、写、连接、接收
        ssChannel.register(selector, SelectionKey.OP_ACCEPT);
//        6.轮询式的获取选择器上已经“准备就绪”的事件select（）
        FileChannel fChannel = FileChannel.open(Paths.get("test1.txt"), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        while (selector.select() > 0){
//        7.获取当前选择器中所有注册的“选择键”（已就绪的监听事件）
            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
//        8.获取准备”就绪“的事件
            while (it.hasNext()){

                SelectionKey sk = it.next();
//        9判断是什么时间准备就绪“读、写、连接、接收”
                if(sk.isAcceptable()){
//        10.若“接收就绪”获取客户端连接
                    SocketChannel sChannel = ssChannel.accept();
//        11.切换非阻塞模式
                    sChannel.configureBlocking(false);
//        12.将该通道注册到选择器上
                    sChannel.register(selector,SelectionKey.OP_READ);
                }else if(sk.isReadable()){
//        13.获取当前选择器上“读就绪”状态的通道
                    SocketChannel sc = (SocketChannel) sk.channel();
//        14.读取数据
                    ByteBuffer bBuffer = ByteBuffer.allocate(1024);
                    int len =0;
                    while ((len = sc.read(bBuffer)) != -1){
                        bBuffer.flip();
                        fChannel.write(bBuffer);
                        bBuffer.clear();
                    }
                }
//        15.取消选择键SelectionKey
            it.remove();
            }
        }
    }
}
