package client;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

@SuppressWarnings({ "serial", "rawtypes"})
public class ListItem extends JLabel implements ListCellRenderer{
	private String name;
	private boolean isDir;
	private boolean isFile;
	public boolean isDir() {
		return isDir;
	}
	public void setDir(boolean isDir) {
		this.isDir = isDir;
	}
	public boolean isFile() {
		return isFile;
	}
	public void setFile(boolean isFile) {
		this.isFile = isFile;
	}
	public ListItem(String type,String name){
		this.setText(name);
		this.name=name;
		if (type.equals("isDir")){
			isDir=true;
			isFile=false;
		}else if (type.equals("isFile")){
			isDir=false;
			isFile=true;
		}
	}
	public String getName(){
		return this.name;
	}
	@Override
	public Component getListCellRendererComponent(JList arg0, Object arg1,
			int arg2, boolean arg3, boolean arg4) {
		// TODO 自动生成的方法存根
		this.setText(name);
		return this;
	}
	@Override
	public String toString(){
		return this.name;
	}
}
