package NioSocket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * @Author zhl
 * @Date 2020/3/21 11:48
 * @Description
 */
public class DatagramChannelServer {

    public static void main(String[] args) {

        try {
            DatagramChannel channel=DatagramChannel.open();

            channel.bind(new InetSocketAddress(1234));

            ByteBuffer buffer=ByteBuffer.allocate(1024);

            while (true) {

                buffer.clear();

                //接收客户端传输的数据包
                SocketAddress socketAddress = channel.receive(buffer);

                if (socketAddress != null) {

                    System.out.println(new String(buffer.array()));

                    buffer.clear();

                    buffer.put("消息接收成功！".getBytes());

                    buffer.flip();
                    //反馈消息给客户端
                    channel.send(buffer, socketAddress);

                    break;

                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
