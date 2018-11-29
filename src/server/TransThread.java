package server;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TransThread implements Runnable {
	//��ͻ��˴����ļ�
	private File file;
	private FileInputStream fis;
	int num;
	private int PORT=8856;
	private PrintStream ps;
	private String filenameString;
	public TransThread(String FileName,PrintStream ps){
		//Ҫ������ͻ��˵��ļ�����·��
		file=new File(FileName);
		this.filenameString=FileName;
		this.ps=ps;
	}
	@Override
	public void run() {
		// TODO �Զ����ɵķ������
		try {
			//��ȡ�ļ�
			fis=new FileInputStream(file);
			
			//���տͻ�����
			ServerSocket ss=new ServerSocket(PORT);
			ps.println(CommandClass._COMMAND_DOWN_START+"&"+filenameString+"&");
			ps.flush();
			System.out.println("Server:"+CommandClass._COMMAND_DOWN_START+"&"+filenameString+"&");
			
			Socket socket=ss.accept();

			//����������������ṩ���ݰ�װ��
			OutputStream os=socket.getOutputStream();
			OutputStream doc=new DataOutputStream(new BufferedOutputStream(os));
			
			//�����ļ���ȡ������
			byte[] buffer=new byte[2048];
			
			num=fis.read(buffer);
			
			while (num!=-1){
				doc.write(buffer,0,num);
				doc.flush();//ˢ�»�����
				num=fis.read(buffer);  //�������뻺����
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
