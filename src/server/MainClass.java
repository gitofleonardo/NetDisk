package server;

import java.net.ServerSocket;
import java.net.Socket;

public class MainClass {
	private Thread mainThread;
	private ServerSocket serverSocket;
	private Socket socket;
	private int PORT=8866;
	public MainClass(){
		try{
			serverSocket=new ServerSocket(PORT);
			System.out.println("Port Bind Success...");
			serverSocket.setSoTimeout(1000);
			System.out.println("Time Out Set...");
			mainThread=new Thread(new ListenerThread());
			System.out.println("Thread Initialized...");
			mainThread.start();
			System.out.println("Thread Started...");
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	private class ListenerThread implements Runnable{

		public void run() {
			// TODO �Զ����ɵķ������
			//��ͣ�����˿ڲ鿴�Ƿ�����Ϣ�����Կͻ���
			while (true){
				try{
					socket=serverSocket.accept();
					//���յ����Կͻ��˵ĵ�½����
					//����һ���̴߳�����û���Ҫ��
					if (socket!=null){
						System.out.println("One Client Accepted...");
						new Thread(new UserProcThread(socket)).start();
						System.out.println("Single User Thread Started...");
						socket=null;
						//ʹ�����
					}else{
						//do nothing
					}
				}catch(Exception e){
					//e.printStackTrace();
				}
			}
		}
		
	}
	public static void main(String[] args){
		@SuppressWarnings("unused")
		MainClass mainClass=new MainClass();
	}
}
