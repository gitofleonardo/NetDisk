package client;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TransThread implements Runnable {
	//向服务器传输文件
	private File file;
	private FileInputStream fis;
	int num;
	private int PORT=8848;
	public TransThread(String FileName){
		//要传输给服务器的文件绝对路径
		file=new File(FileName);
	}
	@Override
	public void run() {
		// TODO 自动生成的方法存根
		try {
			//读取文件
			fis=new FileInputStream(file);
			
			//接收客户请求
			ServerSocket ss=new ServerSocket(PORT);
			Socket socket=ss.accept();
			
			//创建网络输出流并提供数据包装器
			OutputStream os=socket.getOutputStream();
			OutputStream doc=new DataOutputStream(new BufferedOutputStream(os));
			
			//创建文件读取缓冲区
			byte[] buffer=new byte[2048];
			
			num=fis.read(buffer);
			
			while (num!=-1){
				doc.write(buffer,0,num);
				doc.flush();//刷新缓冲区
				num=fis.read(buffer);  //继续读入缓冲区
			}
			fis.close();
			doc.close();
			ss.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

}
