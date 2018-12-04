package client;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.Socket;

import javax.swing.JDialog;
import javax.swing.JProgressBar;

public class DownloadThread implements Runnable {
	//从客户端下载文件
	private JDialog jDialog;
	private JProgressBar progressBar;
	private File file;
	private String IPAddr;
	private int PORT=8856;
	private String path;
	private Socket socket;
	private boolean done=false;
	private long fileSize;
	private long downloadedDize;
	private int percent;
	
	public DownloadThread(String filePath,String FileName,String IPAddr,String size){
		//要传输到的文件夹绝对路径
		downloadedDize=0;
		jDialog = new JDialog();
		jDialog.setTitle("Downloading...");
		jDialog.setLocationRelativeTo(null);
		jDialog.setSize(200,100);
		jDialog.setResizable(false);
		progressBar=new JProgressBar();
		progressBar.setMaximum(100);
		progressBar.setStringPainted(true);
		jDialog.add(progressBar);
		this.fileSize=Long.parseLong(size);
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
			jDialog.setVisible(true);
			downloadedDize+=num;
			percent=(int)((100*downloadedDize)/fileSize);
			progressBar.setValue(percent);
			while (num!=-1){
				raf.write(buffer,0,num);
				raf.skipBytes(num);
				num=dis.read(buffer);
				downloadedDize+=num;
				//System.out.println("Downloading..."+downloadedDize);
				System.out.println(percent);
				percent=(int)((100*downloadedDize)/fileSize);
				progressBar.setValue(percent);
			}
			progressBar.setValue(100);
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
