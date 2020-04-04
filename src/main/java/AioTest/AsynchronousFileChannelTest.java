package AioTest;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
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

    protected static  AsynchronousFileChannel sourceChannel;

    protected static AsynchronousFileChannel targetChannel;

    protected static long size;

    protected static volatile  int num=0;

    public AsynchronousFileChannelTest(String source, String target) {

        fileExists(target);

        this.source = source;

        this.target = target;

        try {

            sourceChannel=AsynchronousFileChannel.open(Paths.get(source), StandardOpenOption.READ);

            targetChannel=AsynchronousFileChannel.open(Paths.get(target), StandardOpenOption.WRITE);

            size=sourceChannel.size();

        } catch (IOException e) {

            e.printStackTrace();

        }

    }
    /**
     * 判断文件是否存在
     * */
    public boolean fileExists(String filePath){

        boolean newFile=true;

        File file = new File(filePath);

        if(!file.exists())
            try {
                 newFile = file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

        return newFile;

    }

    public static void main(String[] args) {

        new AsynchronousFileChannelTest("F:\\test\\demo.rar","F:\\test\\copy\\demo.rar").futureCopyFile();
    }

    /**
     * Future方式复制文件
     * */
    public  void futureCopyFile(){

        try {

            ExecutorService exec= Executors.newFixedThreadPool(5);

            for (int i = 0; i < 5; i++) {

                CopyFile copyFile = new CopyFile(i);

                exec.execute(copyFile);
            }

            while (true){
                if(num==5){
                    exec.shutdown();
                    break;
                }
            }

        } catch (Exception e) {

            e.printStackTrace();

        }


    }

}

class CopyFile implements Runnable{

    private int currentNum;

    public CopyFile(int currentNum) {
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

                Future<Integer> read = AsynchronousFileChannelTest.sourceChannel.read(buffer, position);

                while (!read.isDone());

                int len = read.get();

                if(len<1){

                    AsynchronousFileChannelTest.num++;break;
                }

                buffer.flip();

                Future<Integer> write = AsynchronousFileChannelTest.targetChannel.write(buffer, position);

                currentNum+=5;

                while (!write.isDone());

            } catch (InterruptedException e) {

                e.printStackTrace();

            } catch (ExecutionException e) {

                e.printStackTrace();

            }


        }

    }

}

