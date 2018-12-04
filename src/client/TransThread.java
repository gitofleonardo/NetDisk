package client;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JDialog;
import javax.swing.JProgressBar;

public class TransThread implements Runnable {
	//向服务器传输文件
	private File file;
	private FileInputStream fis;
	int num;
	private int PORT=8848;
	private JDialog jDialog;
	private JProgressBar progressBar;
	private long fileLength;
	private int percent;
	private long uploadedSize;
	public TransThread(String FileName){
		//要传输给服务器的文件绝对路径
		file=new File(FileName);
		fileLength=file.length();
		jDialog=new JDialog();
		jDialog.setTitle("Uploading...");
		progressBar=new JProgressBar();
		progressBar.setMaximum(100);
		progressBar.setStringPainted(true);
		jDialog.setSize(200, 100);
		jDialog.setResizable(false);
		jDialog.setLocationRelativeTo(null);
		jDialog.add(progressBar);
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
			jDialog.setVisible(true);
			uploadedSize+=num;
			percent=(int) ((100*uploadedSize)/fileLength);
			while (num!=-1){
				doc.write(buffer,0,num);
				doc.flush();//刷新缓冲区
				num=fis.read(buffer);  //继续读入缓冲区
				progressBar.setValue(percent);
				uploadedSize+=num;
				percent=(int) ((100*uploadedSize)/fileLength);
			}
			progressBar.setValue(100);
			fis.close();
			doc.close();
			ss.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

}
