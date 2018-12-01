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
		//��ʼ����Ա����
		refreshItem=new JMenuItem("Refresh");
		refreshItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO �Զ����ɵķ������
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
		//��ʼ������
		jScrollPane=new JScrollPane(fileJList);
		this.add(jMenuBar,BorderLayout.NORTH);
		this.add(jScrollPane);
		this.setTitle("User:"+ID);
		this.setSize(400, 600);
		this.setLocationRelativeTo(null);
		this.setResizable(false);
		this.setVisible(true);
		
		//����ȫ���߳�
		Thread userThread=new Thread(new UserProcThread());
		userThread.start();
	}
	private class UserProcThread implements Runnable{
		//����������
		private byte[] buffer;
		//ת��Ϊ�ַ�����Ϣ
		private String msg;
		@Override
		public void run() {
			// TODO �Զ����ɵķ������
			try {
				IPAddr=Inet4Address.getLocalHost().getHostAddress();
				//���Ի�ȡ��
				os=socket.getOutputStream();
				is=socket.getInputStream();
				
				ps=new PrintStream(os);
				
				//��ȡ�û���Ϣ
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
					//�����ȴ���������Ϣ
					is.read(buffer);
					msg=new String(buffer);
					System.out.println(msg);
					//��������
					switch (msg.substring(0,20)) {
					case CommandClass._COMMAND_USERINFO:
						String[] infos;
						infos=msg.split("&");
						user.setTotalSize(Long.parseLong(infos[1]));
						user.setSizeUsed(Long.parseLong(infos[2]));
						break;
					case CommandClass._COMMAND_GETDIRS:
						//���������ͻ��˷���һ���ļ����ַ�������
						//�ȴ��ͻ��˽���
						//���뵱ǰĿ¼
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
	//��ȡ�û�������Ϣ
	//����һ������������
	private void getUserInfo(){
		//�����û���Ϣ
		//�����շ��������ͻ����ķ�����Ϣ
		//�����������������Ѿ�ʹ�õ�����
		ps.println(CommandClass._COMMAND_LOGIN+"&"+ID+"&"+Passwd+"&");
		ps.flush();
	}
	
	//�½��ļ��в���
	private void newDir(String dirName){
		ps.println(CommandClass._COMMAND_NEWDIR+"&"+currentDir+dirName+"&");
		ps.flush();
	}
	//���ļ��б�����JList��
	//���ղ������ַ���������
	@SuppressWarnings("unchecked")
	private void loadFileList(String files){
		//����һ��Ŀ¼ѹ��ջ
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
	//������������ȡ�ļ���
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
		//fileΪ�����ļ�����·��
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
			// TODO �Զ����ɵķ������
			SizeUsed=user.getSizeUsed();
			TotalSize=user.getTotalSize();
			cal();
			JDialog jDialog=new JDialog();
			jDialog.setTitle("About");
			jDialog.setModal(true);
			jDialog.add(new JLabel("�Ƴ�ϲ����"),BorderLayout.NORTH);
			jDialog.add(new JLabel("�ܿռ䣺"+TotalSize+totalUnit),BorderLayout.CENTER);
			jDialog.add(new JLabel("���ÿռ䣺"+SizeUsed+usedUnit),BorderLayout.SOUTH);
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
			// TODO �Զ����ɵķ������
			//������һ��
			for (String i:fileStack){
				if (i!=null)
				    System.out.println(i);
			}
			System.out.println(fileStack.size());
			if (!fileStack.isEmpty()){
				//������һ��
				currentDir=fileStack.pop();
				getDirs(currentDir);
			}
		}
		
	}
	
	private class ListListener implements ListSelectionListener{
		@Override
		public void valueChanged(ListSelectionEvent e) {
			// TODO �Զ����ɵķ������

		}
		
	}
	private class ListMouseListener implements MouseListener{

		@Override
		public void mouseClicked(MouseEvent arg0) {
			// TODO �Զ����ɵķ������
			if (arg0.getClickCount()==2){
				//��ȡ�������Ŀ
				int i=fileJList.getSelectedIndex();
				if (i>=0){
					ListItem selected=(ListItem) fileJList.getSelectedValue();
					if(selected!=null){
						if (selected.isDir()){
							//�����������ļ���
							//push��ǰ���ļ���Ŀ¼��ջ��
							fileStack.push(new String(currentDir));
							currentDir=currentDir+selected.toString()+"\\";
						    getDirs(currentDir);
						    SelectedDir=selected.toString();
						}else{
							//�����������ļ���ִ�в�����
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
			// TODO �Զ����ɵķ������
			
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
			// TODO �Զ����ɵķ������
			
		}

		@Override
		public void mousePressed(MouseEvent arg0) {
			// TODO �Զ����ɵķ������
			
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
			// TODO �Զ����ɵķ������
			
		}
		
	}
	private class DelListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO �Զ����ɵķ������
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
			// TODO �Զ����ɵķ������
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
					// TODO �Զ����ɵķ������
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
			// TODO �Զ����ɵķ������
			//�ϴ��ļ�
			try {
				IP=InetAddress.getLocalHost().getHostAddress();
			} catch (UnknownHostException e) {
				// TODO �Զ����ɵ� catch ��
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
			// TODO �Զ����ɵķ������
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
						// TODO �Զ����ɵķ������
						jFileChooser=new JFileChooser();
						jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						jFileChooser.showOpenDialog(chooseButton);
					}
				});
				jButton.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent arg0) {
						// TODO �Զ����ɵķ������
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
			// TODO �Զ����ɵķ������
			if (!SelectedDir.equals("")){
				conformButton=new JButton("Conform");
				conformButton.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent arg0) {
						// TODO �Զ����ɵķ������
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
