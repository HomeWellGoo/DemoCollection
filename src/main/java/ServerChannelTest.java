import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @Author zhl
 * @Date 2020/3/19 16:01
 * @Description
 */
public class ServerChannelTest {

    private Selector selector;

    public static void main(String[] args) {

        ServerChannelTest channelTest=new ServerChannelTest();

    }

    public ServerChannelTest() {

        try {
            //生成服务器端的通道
            ServerSocketChannel scl=ServerSocketChannel.open();
            //绑定服务器地址
            scl.socket().bind(new InetSocketAddress("127.0.0.1",1234));
            //设置为非阻塞模式
            scl.configureBlocking(false);
            //获取选择器
            selector=Selector.open();
            //注册channel，并设置为待接收状态
            scl.register(selector, SelectionKey.OP_ACCEPT);

            while (true){

                System.out.println("server1:"+ServerChannelTest.getCurrentDate());

                //选择器阻塞等待就绪的channel,每隔1秒进行重试
                int i=selector.select(3000);

                System.out.println("server2:"+ServerChannelTest.getCurrentDate());

                if(i>0){

                   Set<SelectionKey> set= selector.selectedKeys();

                   Iterator<SelectionKey> iterator =set.iterator();

                   while (iterator.hasNext()){

                       SelectionKey key=iterator.next();
                       //证明该客户端channel的状态为待接收装填
                       if(key.isAcceptable()){

                           ServerSocketChannel  soc=(ServerSocketChannel)key.channel();
                           //服务器为每个新的客户端连接创建一个SocketChannel
                           SocketChannel clientChannel=soc.accept();
                           //设置为非阻塞模式
                           clientChannel.configureBlocking(false);
                           //设置为读就绪状态，服务器可以读取客户端的信息了
                           clientChannel.register(selector,SelectionKey.OP_READ);

                       }else if(key.isReadable()){//读就绪

                           SocketChannel soc=null;

                           try {

                               soc=(SocketChannel)key.channel();

                               String s=readChannel(soc);

                               System.out.println(s);

                               try {
                                   TimeUnit.SECONDS.sleep(10);
                               } catch (InterruptedException e) {
                                   e.printStackTrace();
                               }

                               writeChannel(soc,"服务器消息返回");

                           }finally {

                               soc.close();

                           }

                       }

                        iterator.remove();

                   }

                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void writeChannel(SocketChannel soc,String str) {

        ByteBuffer buffer=ByteBuffer.allocate(str.getBytes().length);

        buffer.put(str.getBytes());

        buffer.flip();

        try {

            soc.write(buffer);

        } catch (IOException e) {

            e.printStackTrace();

        }
    }

    public static String readChannel(SocketChannel soc){

        ByteBuffer buffer=ByteBuffer.allocate(1024);

        byte[] array=null;

        while (true){

            try {

                int num=soc.read(buffer);

                if(num<=0) break;
                //读写转换
                buffer.flip();
                //获取上限
                int limit=buffer.limit();

                array=new byte[limit];

                for (int i=0;i<limit;i++){

                    array[i]=buffer.get(i);

                }
                buffer.clear();

            } catch (IOException e) {

                e.printStackTrace();

            }

        }
          if(array==null||array.length==0)return "";

          return new String(array);

    }

    public static String getCurrentDate(){

        Date date=new Date();

        SimpleDateFormat sdf=new SimpleDateFormat("HH:mm:ss");

        return sdf.format(date);

    }
}
