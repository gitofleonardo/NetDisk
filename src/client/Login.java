package client;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.*;

@SuppressWarnings({ "unused", "serial" })

public class Login extends JFrame {
	private JPanel label;
	private JPanel input;
	private JTextField IDText;
	private JPasswordField PasswdText;
	private JLabel passwdL;
	private JLabel IDl;
	private JButton loginButton;
	private Socket socket;
	private int PORT=8866;
	private InputStream is;
	private InputStreamReader isr;
	private PrintStream ps;
	private static Login login;

	public Login(){
		//��ʼ������
		label=new JPanel(new BorderLayout());
		input=new JPanel(new BorderLayout());
		IDText=new JTextField(20);
		PasswdText=new JPasswordField(20);
		passwdL=new JLabel("Password:");
		loginButton=new JButton("Login/Register");
		loginButton.setSize(100, 50);
		loginButton.addActionListener(new LoginListener());
		IDl=new JLabel("ID");
		label.add(IDl,BorderLayout.NORTH);
		label.add(passwdL,BorderLayout.SOUTH);
		input.add(IDText,BorderLayout.NORTH);
		input.add(PasswdText,BorderLayout.SOUTH);
		this.add(label,BorderLayout.WEST);
		this.add(input,BorderLayout.EAST);
		this.add(loginButton,BorderLayout.SOUTH);
		this.setSize(300,100);
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTitle("Login");
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	public static void main(String[] args) {
		// TODO �Զ����ɵķ������
		login=new Login();
	}
	private class LoginListener implements ActionListener{
		private String ID;
		private String passwd;
		@SuppressWarnings("deprecation")
		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO �Զ����ɵķ������
			ID=IDText.getText();
			passwd=PasswdText.getText();
			if (ID.equals("") || passwd.equals("")){
				JDialog jd=new JDialog();
				jd.setTitle("Illegal!");
				jd.add(new JLabel("ID or Password cannot be null"));
				jd.setSize(200, 100);
				jd.setLocationRelativeTo(null);
				jd.setModal(true);
				jd.setVisible(true);
			}else{
				//���Ե�½
				try {
					byte[] buffer=new byte[1024];
					socket=new Socket("localhost",PORT);
					is=socket.getInputStream();
					isr=new InputStreamReader(is);
					is.read(buffer);
					System.out.println(new String(buffer).substring(0,20));
					if (new String(buffer).substring(0,20).equals(CommandClass._COMMAND_READY)){
						System.out.println("Client:"+CommandClass._COMMAND_READY);
						//����������
						MainClass mainClass=new MainClass(socket,ID,passwd);
						login.dispose();
					}
				} catch (UnknownHostException e) {
					// TODO �Զ����ɵ� catch ��
					e.printStackTrace();
				} catch (IOException e) {
					// TODO �Զ����ɵ� catch ��
					e.printStackTrace();
				}
			}
		}
		
	}
}
