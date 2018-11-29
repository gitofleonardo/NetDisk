package server;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.Socket;


public class DownloadThread implements Runnable {
	//�ӿͻ��������ļ�
	private File file;
	private String IPAddr;
	private int PORT=8848;
	private Socket socket;
	private boolean done=false;
	private String path;
	public DownloadThread(String filePath,String FileName,String IPAddr){
		//Ҫ���䵽���ļ��о���·��
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
	}
	@Override
	public void run() {
		// TODO �Զ����ɵķ������
		while (!done && !Thread.currentThread().isInterrupted()){
			try {
				Thread.sleep(500);
				socket=new Socket(IPAddr,PORT);
				done=true;
			} catch (Exception e) {
				// TODO �Զ����ɵ� catch ��
				e.printStackTrace();
			}
		}
		try {
			//�������ļ�
			file.createNewFile();
			RandomAccessFile raf=new RandomAccessFile(file,"rw");
			//���ӵ����Ͷ�
			//���շ��Ͷ��ļ�
			InputStream is=socket.getInputStream();
			DataInputStream dis=new DataInputStream(is);
			
			//�洢�ֽڵ�������
			byte[] buffer=new byte[2048];
			int num=dis.read(buffer);
			while (num!=-1){
				raf.write(buffer,0,num);
				raf.skipBytes(num);
				num=dis.read(buffer);
			}
			dis.close();
			raf.close();
			socket.close();
			Thread.currentThread().interrupt();
		} catch (IOException e) {
			// TODO �Զ����ɵ� catch ��
			e.printStackTrace();
		}
		
	}

}
