package NioSocket;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * @Author HomeWellGo
 * @Date 2020/3/19 16:44
 * @Description 客户端
 */
public class ClientChannelTest {

    private static Selector selector;


    public static void main(String[] args)throws  Exception {

        selector=Selector.open();

        SocketChannel sc = SocketChannel.open();

        sc.configureBlocking(false);

        boolean bool=sc.connect(new InetSocketAddress("127.0.0.1", 1234));

        if(bool){
            //连接成功则注册为读就绪状态
            sc.register(selector,SelectionKey.OP_READ);
            //发送消息给服务器
            ServerChannelTest.writeChannel(sc,"客户端消息发送1");

        }else{
            //注册到选择器中并且将状态更新为连接成功状态。
            sc.register(selector, SelectionKey.OP_CONNECT);

        }


        while (true) {

            System.out.println("client1:"+ServerChannelTest.getCurrentDate());
            //选择器阻塞等待就绪的channel,每隔1秒进行重试
            int i=selector.select(3000);

            System.out.println("client2:"+ServerChannelTest.getCurrentDate());

            if(i>0){
                Iterator<SelectionKey> keys=selector.selectedKeys().iterator();

                while (keys.hasNext()){

                    SelectionKey key=keys.next();

                    SocketChannel soc=(SocketChannel)key.channel();

                    if(key.isConnectable()){
                        //判断当前连接是否成功
                        if(soc.finishConnect()){
                            //注册为读就绪状态，准备接收服务器返回数据
                            soc.register(selector, SelectionKey.OP_READ);

                            try {
                                TimeUnit.SECONDS.sleep(10);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            //向服务器发起数据交互
                            ServerChannelTest.writeChannel(soc,"客户端消息发送2");
                        }

                    }if(key.isReadable()){

                        String result=ServerChannelTest.readChannel(soc);
                        //当连接断开后，为了让你知晓连接已关闭，会产生OP_READ事件;就会不停的轮询出造成一直循环打印空字符。
                        if("".equals(result)){

                            key.cancel();

                        }else{

                            System.out.println(result);
                        }

                    }

                    keys.remove();

                }

            }

        }

    }
}
