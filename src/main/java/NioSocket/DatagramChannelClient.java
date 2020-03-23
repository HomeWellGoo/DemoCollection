package NioSocket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * @Author zhl
 * @Date 2020/3/21 11:48
 * @Description DatagramChannel客户端
 */
public class DatagramChannelClient {

    public static void main(String[] args) {

        DatagramChannel channel = null;

        try {

            channel = DatagramChannel.open();

            ByteBuffer buffer=ByteBuffer.allocate(1024);

            buffer.put("客户端消息发送！".getBytes());

            buffer.flip();

            channel.send(buffer,new InetSocketAddress("127.0.0.1",1234));

            while (true){

                buffer.clear();

                SocketAddress socketAddress=channel.receive(buffer);

                if(socketAddress!=null){

                    System.out.println(new String(buffer.array()));

                    break;

                }

            }

        } catch (IOException e) {

            e.printStackTrace();

        }

    }

}
