package client;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dimension;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PushbackInputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.acl.Owner;
import java.util.ArrayList;
import java.util.Stack;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

@SuppressWarnings({ "unused", "serial","rawtypes"})
public class MainClass extends JFrame {
	private ArrayList<String> fileList;
	private Socket socket;
	private PrintStream ps;
	private String ID;
	private String Passwd;
	private InputStream is;
	private OutputStream os;
	private User user;
	private JList<ListItem> fileJList;
	private Stack<String> fileStack;
	private String currentDir;
	private String IPAddr;
	private JMenuBar jMenuBar;
	private JScrollPane jScrollPane;
	private JMenuItem jItem1;
	private JMenuItem backJMenuItem;
	private String SelectedDir="";
	private JMenuItem delDirItem;
	private JMenuItem newDirItem;
	private JMenuItem uploadItem;
	private JMenuItem downloadItem;
	private JMenuItem refreshItem;
	private JMenuItem renameItem;
	private JMenu jMenu;
	public MainClass(Socket socket,String ID,String Passwd){
		this.socket=socket;
		this.ID=ID;
		this.Passwd=Passwd;
		currentDir=ID+"\\";
		//初始化成员变量
		refreshItem=new JMenuItem("Refresh");
		refreshItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO 自动生成的方法存根
				getDirs(currentDir);
				getNewInfo();
			}
		});
		jMenu=new JMenu("TODO");
		renameItem=new JMenuItem("Rename");
		renameItem.addActionListener(new RenameListener());
		uploadItem=new JMenuItem("Upload");
		uploadItem.addActionListener(new UploadListener());
		downloadItem=new JMenuItem("DownLoad");
		downloadItem.addActionListener(new DownloadListener());
		newDirItem=new JMenuItem("Add Directory");
		newDirItem.addActionListener(new NewDirListener());
		delDirItem=new JMenuItem("Delete");
		delDirItem.addActionListener(new DelListener());
		fileStack=new Stack<String>();
		jItem1=new JMenuItem("About");
		backJMenuItem=new JMenuItem("Back");
		backJMenuItem.addActionListener(new BackMenuListener());
		jItem1.addActionListener(new AboutItemListener());
		jMenuBar=new JMenuBar();
		jMenu.add(backJMenuItem);
		jMenu.add(refreshItem);
		jMenu.add(newDirItem);
		jMenu.add(delDirItem);
		jMenu.add(downloadItem);
		jMenu.add(uploadItem);
		jMenu.add(renameItem);
		jMenu.add(jItem1);
		jMenuBar.add(jMenu);
		user=new User(ID, Passwd);
		fileJList=new JList<ListItem>();
		//fileJList.addListSelectionListener(new ListListener());
		fileJList.addMouseListener(new ListMouseListener());
		//初始化界面
		jScrollPane=new JScrollPane(fileJList);
		this.add(jMenuBar,BorderLayout.NORTH);
		this.add(jScrollPane);
		this.setTitle("User:"+ID);
		this.setSize(400, 600);
		this.setLocationRelativeTo(null);
		this.setResizable(false);
		this.setVisible(true);
		
		//开启全局线程
		Thread userThread=new Thread(new UserProcThread());
		userThread.start();
	}
	private class UserProcThread implements Runnable{
		//缓冲区数据
		private byte[] buffer;
		//转换为字符串消息
		private String msg;
		@Override
		public void run() {
			// TODO 自动生成的方法存根
			try {
				IPAddr=Inet4Address.getLocalHost().getHostAddress();
				//尝试获取流
				os=socket.getOutputStream();
				is=socket.getInputStream();
				
				ps=new PrintStream(os);
				
				//获取用户信息
				getUserInfo();
				getDirs(ID);
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			while (!Thread.currentThread().isInterrupted() && socket!=null){
				try {
					//to do
					buffer=new byte[1024];
					System.out.println("Reading...");
					//阻塞等待服务器信息
					is.read(buffer);
					msg=new String(buffer);
					System.out.println(msg);
					//处理数据
					switch (msg.substring(0,20)) {
					case CommandClass._COMMAND_USERINFO:
						String[] infos;
						infos=msg.split("&");
						user.setTotalSize(Long.parseLong(infos[1]));
						user.setSizeUsed(Long.parseLong(infos[2]));
						break;
					case CommandClass._COMMAND_GETDIRS:
						//服务器给客户端发送一个文件夹字符串对象
						//等待客户端接收
						//载入当前目录
						loadFileList(msg);
						break;
					/**case CommandClass._COMMAND_DOWN_START:
						String[] msgs=msg.split("&");
						System.out.println("DownloadTo:D:\\"+msgs[1]);
						break;*/
						
					default:
						break;
					}
					System.out.println(msg);
					Thread.sleep(500);
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
					break;
				}
			}
		}
		
	}
	//获取用户网盘信息
	//返回一个长整形数组
	private void getUserInfo(){
		//发送用户信息
		//并接收服务器发送回来的反馈信息
		//包括网盘总容量和已经使用的容量
		ps.println(CommandClass._COMMAND_LOGIN+"&"+ID+"&"+Passwd+"&");
		ps.flush();
	}
	
	//新建文件夹操作
	private void newDir(String dirName){
		ps.println(CommandClass._COMMAND_NEWDIR+"&"+currentDir+dirName+"&");
		ps.flush();
	}
	//将文件列表载入JList中
	//接收参数：字符串数组是
	@SuppressWarnings("unchecked")
	private void loadFileList(String files){
		//将上一个目录压入栈
		//String laString=new String(currentDir);
		//if (!SelectedDir.equals(""))
	    //	currentDir=currentDir+SelectedDir+"\\";
		//fileStack.push(laString);
		String[] singleDoc=files.split("&");
		String[] fileType;
		DefaultListModel listModel=new DefaultListModel();
		//String[] listDataStrings=new String[singleDoc.length-1];
		for (int i=1;i<singleDoc.length-1;i++){
			fileType=singleDoc[i].split("\\+");
			listModel.add(i-1,new ListItem(fileType[0],fileType[1]));
			//listDataStrings[i-1]=fileType[1];
		}
		fileJList.setModel(listModel);
		//fileJList.setListData(listDataStrings);
	}
	//向服务器请求获取文件夹
	private void getDirs(String currentDir){
		ps.println(CommandClass._COMMAND_GETDIRS+"&"+currentDir+"&");
		ps.flush();
	}
	private void delDir(String dirName){
		ps.println(CommandClass._CONNAMD_DELDIR+"&"+currentDir+dirName+"&");
	}
	private void rename(String OldDirName,String NewDirName){
		ps.println(CommandClass._COMMAND_RENAME+"&"+currentDir+OldDirName+"&"+currentDir+NewDirName+"&");
		ps.flush();
	}
	private void download(String fileName){
		ps.println(CommandClass._COMMAND_DOWNFILE+"&"+currentDir+fileName+"&");
		ps.flush();
		Thread thread=new Thread(new DownloadThread("D:\\",fileName,"127.0.0.1"));
		thread.start();
	}
	private void getNewInfo(){
		ps.println(CommandClass._COMMAND_USERINFO);
		ps.flush();
	}
	private void upload(String path,String filename){
		//file为本地文件绝对路径
		ps.println(CommandClass._COMMAND_UPFILE+"&"+currentDir+"&"+filename+"&"+IPAddr+"&");
		ps.flush();
		Thread thread=new Thread(new TransThread(path+filename));
		thread.start();
	}
	private class AboutItemListener implements ActionListener{
		private long SizeUsed;
		private long TotalSize;
		private String usedUnit="byte";
		private String totalUnit="byte";
		
		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO 自动生成的方法存根
			SizeUsed=user.getSizeUsed();
			TotalSize=user.getTotalSize();
			cal();
			JDialog jDialog=new JDialog();
			jDialog.setTitle("About");
			jDialog.setModal(true);
			jDialog.add(new JLabel("黄承喜制作"),BorderLayout.NORTH);
			jDialog.add(new JLabel("总空间："+TotalSize+totalUnit),BorderLayout.CENTER);
			jDialog.add(new JLabel("已用空间："+SizeUsed+usedUnit),BorderLayout.SOUTH);
			jDialog.setSize(200, 150);
			jDialog.setLocationRelativeTo(null);
			jDialog.setResizable(false);
			jDialog.setVisible(true);
		}
		void cal(){
			int used=0;
			int total=0;
			while (SizeUsed>=1024){
				SizeUsed/=1024;
				used++;
			}
			while (TotalSize>=1024){
				TotalSize/=1024;
				total++;
			}
			switch (used) {
			case 0:
				usedUnit="byte";
				break;
			case 1:
				usedUnit="KB";
				break;
			case 2:
				usedUnit="MB";
				break;
			case 3:
				usedUnit="GB";
				break;
			default:
				break;
			}
			switch (total) {
			case 0:
				totalUnit="byte";
				break;
			case 1:
				totalUnit="KB";
				break;
			case 2:
				totalUnit="MB";
				break;
			case 3:
				totalUnit="GB";
				break;
			default:
				break;
			}
		}
	}
	private class BackMenuListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO 自动生成的方法存根
			//返回上一级
			for (String i:fileStack){
				if (i!=null)
				    System.out.println(i);
			}
			System.out.println(fileStack.size());
			if (!fileStack.isEmpty()){
				//返回上一级
				currentDir=fileStack.pop();
				getDirs(currentDir);
			}
		}
		
	}
	
	private class ListListener implements ListSelectionListener{
		@Override
		public void valueChanged(ListSelectionEvent e) {
			// TODO 自动生成的方法存根

		}
		
	}
	private class ListMouseListener implements MouseListener{

		@Override
		public void mouseClicked(MouseEvent arg0) {
			// TODO 自动生成的方法存根
			if (arg0.getClickCount()==2){
				//获取点击的项目
				int i=fileJList.getSelectedIndex();
				if (i>=0){
					ListItem selected=(ListItem) fileJList.getSelectedValue();
					if(selected!=null){
						if (selected.isDir()){
							//如果点击的是文件夹
							//push当前的文件夹目录到栈中
							fileStack.push(new String(currentDir));
							currentDir=currentDir+selected.toString()+"\\";
						    getDirs(currentDir);
						    SelectedDir=selected.toString();
						}else{
							//否则点击的是文件（执行操作）
						}
					}else{
						SelectedDir="";
					}
				}
			}else if (arg0.getClickCount()==1){
				if (fileJList.getSelectedValue()!=null){
					SelectedDir=fileJList.getSelectedValue().toString();
				}
			}
		}

		@Override
		public void mouseEntered(MouseEvent arg0) {
			// TODO 自动生成的方法存根
			
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
			// TODO 自动生成的方法存根
			
		}

		@Override
		public void mousePressed(MouseEvent arg0) {
			// TODO 自动生成的方法存根
			
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
			// TODO 自动生成的方法存根
			
		}
		
	}
	private class DelListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO 自动生成的方法存根
			ListItem listItem=(ListItem) fileJList.getSelectedValue();
			ps.println(CommandClass._CONNAMD_DELDIR+"&"+currentDir+listItem.toString()+"&");
			ps.flush();
			getDirs(currentDir);
		}
		
	}
	private class NewDirListener implements ActionListener{
		String dirName="DefaultDirName";
		JTextField jTextField;
		JButton jButton;
		JPanel jPanel;
		JDialog jDialog;
		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO 自动生成的方法存根
			jTextField=new JTextField(10);
			jButton=new JButton("Conform");
			jPanel=new JPanel();
			jDialog=new JDialog();
			jTextField.setSize(100,30);
			jDialog=new JDialog();
			jPanel.add(new JLabel("Directory Name:"),BorderLayout.WEST);
			jPanel.add(jTextField,BorderLayout.EAST);
			jPanel.add(jButton,BorderLayout.SOUTH);
			jDialog.setSize(200,130);
			jPanel.setSize(200,130);
			jDialog.add(jPanel);
			jDialog.setLocationRelativeTo(null);
			jButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					// TODO 自动生成的方法存根
					if (!jTextField.getText().equals("")){
						dirName=jTextField.getText();
						ps.println(CommandClass._COMMAND_NEWDIR+"&"+currentDir+dirName+"&");
						ps.flush();
						getDirs(currentDir);
						jDialog.dispose();
					}else{
						//nothing
					}
				}
			});
			jDialog.setModal(true);
			jDialog.setVisible(true);
			
		}
		
	}
	private class UploadListener implements ActionListener{
		String fileName;
		JFileChooser jFileChooser;
		String IP;
		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO 自动生成的方法存根
			//上传文件
			try {
				IP=InetAddress.getLocalHost().getHostAddress();
			} catch (UnknownHostException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
			jFileChooser=new JFileChooser();
			jFileChooser.showOpenDialog(uploadItem);
			fileName=jFileChooser.getSelectedFile().getName();
			ps.println(CommandClass._COMMAND_UPFILE+"&"+currentDir+"&"+fileName+"&"+IP+"&");
			ps.flush();
			Thread thread=new Thread(new TransThread(jFileChooser.getSelectedFile().getAbsolutePath()));
			thread.start();
		}
		
	}
	private class DownloadListener implements ActionListener{
		JDialog jDialog;
		JButton jButton;
		JTextField jTextField;
		JButton chooseButton;
		JFileChooser jFileChooser;
		JPanel jPanel;
		ListItem lsItem;
		JTextField fileName;
		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO 自动生成的方法存根
			lsItem=fileJList.getSelectedValue();
			if (lsItem.isFile()){
				ps.println(CommandClass._COMMAND_DOWNFILE+"&"+currentDir+lsItem.toString()+"&");
				ps.flush();
				fileName=new JTextField(10);
				fileName.setText(lsItem.toString());
				jDialog=new JDialog();
				jButton=new JButton("Conform");
				chooseButton=new JButton("...");
				jTextField=new JTextField(10);
				jTextField.setText("D:\\");
				jTextField.setEditable(false);
				jPanel=new JPanel();
				jPanel.add(jTextField);
				jPanel.add(fileName);
				jPanel.add(chooseButton);
				jPanel.add(jButton);
				jDialog.add(jPanel);
				jDialog.setSize(200,130);
				chooseButton.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent arg0) {
						// TODO 自动生成的方法存根
						jFileChooser=new JFileChooser();
						jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						jFileChooser.showOpenDialog(chooseButton);
					}
				});
				jButton.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent arg0) {
						// TODO 自动生成的方法存根
						Thread thread=new Thread(new DownloadThread(jTextField.getText(),fileName.getText(),"localhost"));
						thread.start();
						jDialog.dispose();
					}
				});
				jDialog.setLocationRelativeTo(null);
				jDialog.setVisible(true);
			}
		}
		
	}
	private class RenameListener implements ActionListener{
		JButton conformButton;
		JDialog jDialog;
		JTextField jTextField;
		JLabel hintJLabel;
		JPanel jPanel;
		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO 自动生成的方法存根
			if (!SelectedDir.equals("")){
				conformButton=new JButton("Conform");
				conformButton.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent arg0) {
						// TODO 自动生成的方法存根
						if (!jTextField.getText().equals("") && !SelectedDir.equals("")){
							ps.println(CommandClass._COMMAND_RENAME+"&"+currentDir+SelectedDir+"&"+currentDir+jTextField.getText()+"&");
							ps.flush();
							getDirs(currentDir);
							jDialog.dispose();
						}
					}
				});
				hintJLabel=new JLabel("New Name:");
				jTextField=new JTextField(10);
				jDialog=new JDialog();
				jPanel=new JPanel();
				jPanel.add(hintJLabel,BorderLayout.WEST);
				jPanel.add(jTextField);
				jPanel.add(conformButton);
				jDialog.add(jPanel);
				jDialog.setSize(100,130);
				jDialog.setModal(true);
				jDialog.setLocationRelativeTo(null);
				jDialog.setVisible(true);
			}
		}
		
	}
}
