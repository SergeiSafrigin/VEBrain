package kcg.vebrain.communication;

import java.io.IOException;
import java.io.OutputStream;

public class Sender{
	private OutputStream outputStream;
	private boolean debug;

	public Sender(boolean debug){
		this.debug = debug;
	}

	public void writetoport(String outString) {
		if (outputStream == null)
			return;
		try { 
			outputStream.write(outString.getBytes());
		} catch (IOException e) {
			exceptionReport(e);
		}
	}
	
	
	public void exceptionReport(Exception e) {
		if(debug) {
			System.out.println(e.toString());
			e.printStackTrace();
		}
	}
	
	public void setOutputStream(OutputStream outputStream){
		this.outputStream = outputStream;
	}
}
