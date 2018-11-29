package client;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.Socket;

public class DownloadThread implements Runnable {
	//从客户端下载文件
	private File file;
	private String IPAddr;
	private int PORT=8856;
	private String path;
	private Socket socket;
	private boolean done=false;
	public DownloadThread(String filePath,String FileName,String IPAddr){
		//要传输到的文件夹绝对路径
		this.path=filePath;
		this.IPAddr=IPAddr;
		file = new File(path+FileName);
		if (file.exists()){
			for (int i=0;;i++){
				file=new File(path+"("+i+")"+FileName);
				if (!file.exists()){
					break;
				}
			}
		}
		System.out.println(path+FileName);

	}
	@Override
	public void run() {
		// TODO 自动生成的方法存根
		while (!done && !Thread.currentThread().isInterrupted()){
			try {
				Thread.sleep(1000);
				socket=new Socket(IPAddr,PORT);
				done=true;
			} catch (Exception e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
		}
			}
		try {
			//生成新文件
			file.createNewFile();
			RandomAccessFile raf=new RandomAccessFile(file,"rw");
			//连接到发送端
			//接收发送端文件
			InputStream is=socket.getInputStream();
			DataInputStream dis=new DataInputStream(is);
			
			//存储字节到缓冲区
			byte[] buffer=new byte[2048];
			int num=dis.read(buffer);
			while (num!=-1){
				raf.write(buffer,0,num);
				raf.skipBytes(num);
				num=dis.read(buffer);
				System.out.println("Downloading..."+num);
			}
			dis.close();
			raf.close();
			socket.close();
			Thread.currentThread().interrupt();
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		
	}

}
