package AioTest;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @Author HomeWellGo
 * @Date 2020/4/2 15:53
 * @Description 异步IO对文件的操作
 */
public class AsynchronousFileChannelTest {

    private  String source;

    private  String target;

    private  AsynchronousFileChannel sourceChannel;

    private  AsynchronousFileChannel targetChannel;

    private  long size;

    private  volatile  int num=0;

    private  ExecutorService exec= Executors.newFixedThreadPool(5);
    //初始化数据
    public AsynchronousFileChannelTest(String source, String target) {

        this.source = source;

        this.target = target;

        try {

            sourceChannel=AsynchronousFileChannel.open(Paths.get(source), StandardOpenOption.READ);

            targetChannel=AsynchronousFileChannel.open(Paths.get(target), StandardOpenOption.WRITE,StandardOpenOption.CREATE);

            size=sourceChannel.size();

        } catch (IOException e) {

            e.printStackTrace();

        }

    }

    public static void main(String[] args) {

        //多线程异步回调复制文件
        //new AsynchronousFileChannelTest("F:\\test\\demo.rar", "F:\\test\\copy\\demo.rar").callbackCopyFile();
        //多线程异步等待复制文件
        //new AsynchronousFileChannelTest("F:\\test\\demo.rar", "F:\\test\\copy\\demo.rar").futureCopyFile();
        //单线程异步回调复制文件
        //new CallbackCopyFile().copyFile("F:\\test\\demo.rar", "F:\\test\\copy\\demo.rar");
        //单线程异步等待复制文件
        //new FutureCopyFile().copyFile("F:\\test\\demo.rar", "F:\\test\\copy\\demo.rar");


    }
    /**
     * 多线程Future方式复制文件
     * */
    public  void futureCopyFile(){

        try {
            //使用五个线程同时对文件进行复制操作
            for (int i = 0; i < 5; i++) {

                ThreadFutureCopyFile copyFile = new ThreadFutureCopyFile(i);

                exec.execute(copyFile);
            }
            while (true){
                //这里可能会出现问题，可以做一个等待超时的判断。
                if(num==5){

                    exec.shutdown();

                    break;

                }
            }

        } catch (Exception e) {

            e.printStackTrace();

        }finally {

            try {

                sourceChannel.close();

                targetChannel.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }


    }
    /**
     * Future方式线程实现类
     * */
    class ThreadFutureCopyFile implements Runnable{

        private int currentNum;

        public ThreadFutureCopyFile(int currentNum) {
            this.currentNum = currentNum;
        }

        @Override
        public void run() {

            ByteBuffer buffer= ByteBuffer.allocate(1024);

            long position=0;

            while (true){

                try {

                    position=currentNum*1024;

                    buffer.clear();

                    Future<Integer> read = sourceChannel.read(buffer, position);

                    int len = read.get();

                    if(len<1){

                        num++;break;
                    }

                    buffer.flip();

                    Future<Integer> write =targetChannel.write(buffer, position);

                    currentNum+=5;
                    //等待写入完成
                    while (!write.isDone());

                } catch (InterruptedException e) {

                    e.printStackTrace();

                } catch (ExecutionException e) {

                    e.printStackTrace();

                }


            }

        }

    }
    /**
     * 多线程使用CompletionHandler异步回调复制文件
     * */
    public  void callbackCopyFile(){

        try {
            //使用五个线程同时对文件进行复制操作
            for (int i = 0; i < 5; i++) {

                ThreadCallbackCopyFile copyFile = new ThreadCallbackCopyFile(i);

                exec.execute(copyFile);
            }

            while (true){
                //这里可能会出现问题，可以做一个等待超时的判断。
                if(num==5){

                    exec.shutdown();

                    break;
                }
            }

        } catch (Exception e) {

            e.printStackTrace();

        }finally {

            try {

                sourceChannel.close();

                targetChannel.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }


    }
    /**
     * CompletionHandler异步回调线程实现类
     * */
    class ThreadCallbackCopyFile implements Runnable{

        private int currentNum;

        private long position=currentNum*1024;

        private CompletionHandler<Integer,ByteBuffer> readHandler=null;

        private CompletionHandler<Integer,ByteBuffer> writeHandler=null;

        public ThreadCallbackCopyFile(int currentNum) {
            this.currentNum = currentNum;
        }

        @Override
        public void run() {

            ByteBuffer buffer= ByteBuffer.allocate(1024);

            //创建一个CompletionHandler作为文件读取完成的回调处理
            readHandler=new CompletionHandler<Integer,ByteBuffer>(){
                //完成之后调用
                @Override
                public void completed(Integer result, ByteBuffer attachment) {

                    if(result<1){  ++num; return;}

                    attachment.flip();
                    //将缓冲区数据写入通道中
                    targetChannel.write(attachment,position,attachment,writeHandler);
                }
                //失败的话调用该方法
                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    System.out.println("读取失败了！");
                }

            };
            //创建一个CompletionHandler做为写入完成的回调处理
            writeHandler=new CompletionHandler<Integer,ByteBuffer>(){
                //完成之后调用
                @Override
                public void completed(Integer result, ByteBuffer attachment) {

                    position+=5*1024;

                    attachment.clear();

                    sourceChannel.read(attachment,position,attachment,readHandler);
                }
                //失败的话调用该方法
                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    System.out.println("写入失败了！");
                }

            };

            buffer.clear();

            sourceChannel.read(buffer,position,buffer,readHandler);

        }

    }

}
/**
 * 单线程异步回调文件复制
 * */
class CallbackCopyFile {

    private AsynchronousFileChannel channel1=null;

    private AsynchronousFileChannel channel2=null;
    //创建一个position记录文件内上一次的读写位置，从零开始读写
    private long position=0;

    private CompletionHandler<Integer,ByteBuffer> readHandler=null;

    private CompletionHandler<Integer,ByteBuffer> writeHandler=null;

    private volatile int num=0;

    public void copyFile(String sourcePath,String targetPath) {

        try {
            channel1=AsynchronousFileChannel.open(Paths.get(sourcePath),StandardOpenOption.READ);
            //open的第二个参数后续为StandardOpenOption的可变参数,可以设置该通道的为几种类型
            channel2=AsynchronousFileChannel.open(Paths.get(targetPath),StandardOpenOption.WRITE,StandardOpenOption.READ,StandardOpenOption.CREATE);

            ByteBuffer buffer= ByteBuffer.allocate(1024);
            //创建一个CompletionHandler作为文件读取完成的回调处理
            readHandler=new CompletionHandler<Integer,ByteBuffer>(){
                //完成之后调用
                @Override
                public void completed(Integer result, ByteBuffer attachment) {

                    if(result<1){

                        num++;

                        return;
                    }

                    attachment.flip();
                    //将缓冲区数据写入通道中
                    channel2.write(attachment,position,attachment,writeHandler);
                }
                //失败的话调用该方法
                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    System.out.println("读取失败！");
                }

            };
            //创建一个CompletionHandler做为写入完成的回调处理
            writeHandler=new CompletionHandler<Integer,ByteBuffer>(){
                //完成之后调用
                @Override
                public void completed(Integer result, ByteBuffer attachment) {

                    position+=result;

                    attachment.clear();
                    //写入完成后再次读取数据，一直递归到无数据为止
                    channel1.read(attachment,position,attachment,readHandler);

                }
                //失败的话调用该方法
                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    System.out.println("写入失败！");
                }

            };

            buffer.clear();
            //读取数据
            channel1.read(buffer,position,buffer,readHandler);

            while (true){

                if(num==1)break;
            }

        } catch (IOException e) { e.printStackTrace();

        }finally {

            try {

                channel1.close();

                channel2.close();

            } catch (IOException e) { e.printStackTrace();}

        }
    }

}
/**
 * 单线程异步等待文件复制
 * */
class FutureCopyFile{

    public  void copyFile(String sourcePath,String targetPath) {

        AsynchronousFileChannel channel1=null;

        AsynchronousFileChannel channel2=null;

        try {

            channel1=AsynchronousFileChannel.open(Paths.get(sourcePath),StandardOpenOption.READ);
            //open的第二个参数后续为StandardOpenOption的可变参数,可以设置该通道的为几种类型
            channel2=AsynchronousFileChannel.open(Paths.get(targetPath),StandardOpenOption.WRITE,
                    StandardOpenOption.READ,StandardOpenOption.CREATE);

            ByteBuffer buffer= ByteBuffer.allocate(1024);
            //创建一个position记录文件内上一次的读写位置，从零开始读写
            long position=0;

            while (true){

                buffer.clear();
                //使用Future来接收结果
                Future<Integer> read = channel1.read(buffer, position);
                //判断数据的长度
                int len = read.get();

                if(len<1) break;
                //读写转换
                buffer.flip();
                //将buffer中的数据写入文件
                Future<Integer> write = channel2.write(buffer, position);
                //对读写位置做更改
                position+=len;
                //等待写入完成
                while (!write.isDone());

            }

        }catch (InterruptedException e) { e.printStackTrace();

        } catch (ExecutionException e) { e.printStackTrace();

        } catch (IOException e) { e.printStackTrace();

        }finally {

            try {

                channel1.close();

                channel2.close();
            } catch (IOException e) { e.printStackTrace();}

        }
    }

}
