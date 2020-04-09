package AioTest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author HomeWellGo
 * @Date 2020/4/5 15:47
 * @Description Aio网络编程服务端
 */
public class AsynchronousServerSocket  {

    private static final  int PORT=1234;

    private static final  String HOSTNAME="127.0.0.1";

    private static  AsynchronousServerSocketChannel serverChannel;

    public AsynchronousServerSocket() {
        try {
            //创建一个固定的线程池
            ExecutorService exec= Executors.newFixedThreadPool(5);
            //将线程池绑定到异步通道组中
            AsynchronousChannelGroup group=AsynchronousChannelGroup.withThreadPool(exec);
            //将该异步通道绑定到异步通道组中
            serverChannel=AsynchronousServerSocketChannel.open(group);
            //绑定对应的ip、端口
            serverChannel.bind(new InetSocketAddress(HOSTNAME,PORT));

        } catch (IOException e) {

            e.printStackTrace();

        }

    }
    /**
     * 使用回调方法进行内容获取
     * */
    public void handler(){

            serverChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {

                @Override
                public void completed(AsynchronousSocketChannel socketChannel, Object attachment) {

                    System.out.println("连接成功！");
                    //继续监听
                    serverChannel.accept(attachment,this);

                   serverReadChannel(socketChannel);

                }

                @Override
                public void failed(Throwable exc, Object attachment) {

                    exc.printStackTrace();
                }

            });
        }

    private static void serverReadChannel(AsynchronousSocketChannel socketChannel) {

        ByteBuffer buffer= ByteBuffer.allocate(1024);

        socketChannel.read(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer attachment) {

                if(result<1){
                    try {
                        socketChannel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

                attachment.flip();

                System.out.println("服务端: "+new String(attachment.array()));

                serverWriteChannel(socketChannel,"服务端消息返回");

            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {

                exc.printStackTrace();

            }

        });

    }

    private static   void  serverWriteChannel(AsynchronousSocketChannel socketChannel,String content){

        ByteBuffer buffer= ByteBuffer.allocate(1024);

        try {

            buffer.put(content.getBytes("UTF-8"));

            buffer.flip();

        } catch (UnsupportedEncodingException e) {

            e.printStackTrace();
        }

        socketChannel.write(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {

            @Override
            public void completed(Integer result, ByteBuffer attachment) {

                System.out.println("服务端写入成功");

            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {

                exc.printStackTrace();

            }

        });
    }

    public static void main(String[] args) {

        new AsynchronousServerSocket().handler();

        while (true);

    }

}
