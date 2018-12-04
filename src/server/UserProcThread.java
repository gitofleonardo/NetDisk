package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;

public class UserProcThread implements Runnable {
	private Socket socket;
	private InputStream is;
	private InputStreamReader isr;
	private BufferedReader br;
	private OutputStream os;
	private PrintStream ps;
	private File file;
	private String msg;
	private String[] msgs;
	private String commandString;
	private User user;
	private final String dir="D:\\NetDisk\\";
	private long sizeUsed=0;
	private final String isDir="&isDir+";
	private final String isFile="&isFile+";
	public UserProcThread(Socket socket){
		this.socket=socket;
		try {
			//��ȡ���������
			is=socket.getInputStream();
			os=socket.getOutputStream();
			
			isr=new InputStreamReader(is);
			br=new BufferedReader(isr);
			
			ps=new PrintStream(os);
		} catch (IOException e) {
			// TODO �Զ����ɵ� catch ��
			e.printStackTrace();
		}
	}
	public void run() {
		// TODO �Զ����ɵķ������
		//���ȷ���׼����½��Ϣ
		msg=new String(CommandClass._COMMAND_READY);
		ps.print(msg);
		ps.flush();
		System.out.println("Server:"+CommandClass._COMMAND_READY);
		while (!Thread.currentThread().isInterrupted()){
			try{
				msg=br.readLine();
				System.out.println("Client:"+msg);
				commandString=msg.substring(0,20);
				msgs=msg.split("&");
				switch(commandString){
				case CommandClass._COMMAND_LOGIN:
					user=new User(msgs[1],msgs[2]);
					try {
						file=new File(dir+msgs[1]);
						//��ȡ�Ƿ��Ѿ������ļ���
						if (!file.exists()){
							//�����ھʹ���һ���ļ��и�Ŀ¼
							file.mkdir();
						}
						user.setRootFile(file);
						//������������
						user.setTotalSize(1024*1024*1024);
						//�����Ѿ�ʹ�õ�����
						calSize(file.listFiles());
						//�����Ѿ�ʹ�õ�����
						user.setSizeUsed(sizeUsed);
						
						//�����û���Ϣ�����ͻ���
						ps.write((CommandClass._COMMAND_USERINFO
								+"&"+user.getTotalSize()
								+"&"+user.getSizeUsed()
								+"&").getBytes());
						ps.flush();
					}catch (Exception e) {
						// TODO: handle exception
					}
					break;
				case CommandClass._COMMAND_USERINFO:
					//������������
					user.setTotalSize(1024*1024*1024);
					//�����Ѿ�ʹ�õ�����
					calSize(file.listFiles());
					//�����Ѿ�ʹ�õ�����
					user.setSizeUsed(sizeUsed);
					ps.write((CommandClass._COMMAND_USERINFO
							+"&"+user.getTotalSize()
							+"&"+user.getSizeUsed()
							+"&").getBytes());
					ps.flush();
					break;
				case CommandClass._COMMAND_GETDIRS:
					//��ȡ�ļ����µ��ļ�
					if (msgs.length>1)
				    	file = new File(dir+msgs[1]);
					else
					    file =new File(dir);
					if (file.isDirectory()){
					String allDir=""+CommandClass._COMMAND_GETDIRS;
					for (File f:file.listFiles()){
						if (f.isDirectory()){
							allDir+=isDir+f.getName();
						}else if (f.isFile()){
							allDir+=isFile+f.getName();
						}
					}
					allDir+="&";
					System.out.println("Server:"+allDir);
					ps.print(allDir);
					ps.flush();
					}else if (file.isFile()){
						
					}
					break;
				case CommandClass._COMMAND_LOGOUT:
					//�ر�������Դ
					br.close();
					isr.close();
					is.close();
					ps.close();
					os.close();
					socket.close();
					Thread.currentThread().interrupt();
					break;
				case CommandClass._COMMAND_NEWDIR:
					//�½��ļ��в���
					file=new File(dir+msgs[1]);
					if (!file.exists()){
						file.mkdir();
					}
					break;
				case CommandClass._COMMAND_RENAME:
					//�򿪾��ļ����У�
					file = new File(dir+msgs[1]);
					//�����ļ����У�
					File newFile=new File(dir+msgs[2]);
					if (!newFile.exists()){
						file.renameTo(newFile);
					}
					break;
				case CommandClass._CONNAMD_DELDIR:
					file = new File(dir+msgs[1]);
					if (file.isFile()){
						file.delete();
					}else if (file.isDirectory()){
						File[] files=file.listFiles();
						for (int i=0;i<files.length;i++){
							if (files[i].isFile()){
								//���ļ���ֱ��ɾ��
								files[i].delete();
							}else if (files[i].isDirectory()){
								delDir(file.listFiles());
								file.delete();
							}
						}
						file.delete();
					}
					break;
				case CommandClass._COMMAND_DOWNFILE:
					//��ͻ��˴����ļ�
					Thread tt=new Thread(new TransThread(dir+msgs[1],ps));
					tt.start();
					break;
				case CommandClass._COMMAND_UPFILE:
					String[] msgs=msg.split("&");
					Thread thread=new Thread(new DownloadThread(dir+msgs[1],msgs[2],msgs[3]));
					System.out.println(dir+msgs[1]+msgs[2]);
					thread.start();
					break;
				default:
					break;
				}
			    Thread.sleep(500);
			}catch(Exception e){
				e.printStackTrace();
				try {
					socket.close();
				} catch (IOException e1) {
					// TODO �Զ����ɵ� catch ��
					e1.printStackTrace();
				}
				break;
			}
		}
	}
	
	private void calSize(File[] fileArr){
		sizeUsed=0;
		if (fileArr==null || fileArr.length<=0){
			return;
		}
		
		for (File f:fileArr){
			if (f.isFile()){
				sizeUsed+=f.length();
			}else if (f.isDirectory()){
				calSize(f.listFiles());
			}
		}
	}
	private void delDir(File[] files){
		for (File f:files){
			if (f.isDirectory()){
				delDir(f.listFiles());
				f.delete();
			}else if (f.isFile()){
				f.delete();
			}
		}
	}

}
