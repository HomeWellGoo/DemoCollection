package AioTest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * @Author HomeWellGo
 * @Date 2020/4/5 15:48
 * @Description Aio网络编程客户端
 */
public class AsynchronousClientSocket {

    public AsynchronousClientSocket() {
        try {

            AsynchronousSocketChannel socketChannel=AsynchronousSocketChannel.open();

            socketChannel.connect(new InetSocketAddress("127.0.0.1",1234), null, new CompletionHandler<Void, Object>() {
                @Override
                public void completed(Void result, Object attachment) {

                    clientWriteChannel(socketChannel,"测试数据123456");

                }
                @Override
                public void failed(Throwable exc, Object attachment) {

                    exc.printStackTrace();

                }
            });


        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private static   void  clientWriteChannel(AsynchronousSocketChannel socketChannel,String content) {

        ByteBuffer buffer = ByteBuffer.allocate(1024);

        try {

            buffer.put(content.getBytes("UTF-8"));

        } catch (UnsupportedEncodingException e) {

            e.printStackTrace();
        }

            buffer.flip();

        socketChannel.write(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {

            @Override
            public void completed(Integer result, ByteBuffer attachment) {

                System.out.println("客户端写入成功");

                clientReadChannel(socketChannel);

            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {

                exc.printStackTrace();

            }

        });

    }

    private static void clientReadChannel(AsynchronousSocketChannel socketChannel) {

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

                System.out.println("客户端: "+new String(attachment.array()));

            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {

                exc.printStackTrace();

            }

        });

    }


    public static void main(String[] args) {

        new  AsynchronousClientSocket();

        while (true);

    }
}
