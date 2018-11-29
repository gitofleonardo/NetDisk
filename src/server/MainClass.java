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
			// TODO 自动生成的方法存根
			//不停监听端口查看是否有信息发送自客户端
			while (true){
				try{
					socket=serverSocket.accept();
					//接收到来自客户端的登陆请求
					//开启一个线程处理此用户的要求
					if (socket!=null){
						System.out.println("One Client Accepted...");
						new Thread(new UserProcThread(socket)).start();
						System.out.println("Single User Thread Started...");
						socket=null;
						//使命完成
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
