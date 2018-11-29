package server;

import java.io.File;

public class User {
	private String UserID;
	private long TotalSize;
	private long SizeUsed;
	private String Passwd;
	private File RootFile;
	
	public User(String UserID,String Passwd){
		this.setUserID(UserID);
		this.setPasswd(Passwd);
	}

	public String getUserID() {
		return UserID;
	}

	public void setUserID(String userID) {
		UserID = userID;
	}

	public long getTotalSize() {
		return TotalSize;
	}

	public void setTotalSize(long totalSize) {
		TotalSize = totalSize;
	}

	public long getSizeUsed() {
		return SizeUsed;
	}

	public void setSizeUsed(long sizeUsed) {
		SizeUsed = sizeUsed;
	}

	public String getPasswd() {
		return Passwd;
	}

	public void setPasswd(String passwd) {
		Passwd = passwd;
	}

	public File getRootFile() {
		return RootFile;
	}

	public void setRootFile(File rootFile) {
		RootFile = rootFile;
	}
}
