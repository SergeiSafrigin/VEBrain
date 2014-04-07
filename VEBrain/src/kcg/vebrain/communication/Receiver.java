package kcg.vebrain.communication;

import java.io.IOException;
import java.io.InputStream;

import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

public class Receiver implements SerialPortEventListener{
	private Sender sender;
	private boolean debug;
	private InputStream inputStream;

	public Receiver(Sender sender, boolean debug){
		this.sender = sender;
		this.debug = debug;
	}
	
	public void setInputStream(InputStream inputStream){
		this.inputStream = inputStream;
	}

	@Override
	public void serialEvent(SerialPortEvent event) {
		if (inputStream == null)
			return;
		
		switch (event.getEventType()) {
		case SerialPortEvent.BI:
		case SerialPortEvent.OE:
		case SerialPortEvent.FE:
		case SerialPortEvent.PE:
		case SerialPortEvent.CD:
		case SerialPortEvent.CTS:
		case SerialPortEvent.DSR:
		case SerialPortEvent.RI:
		case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
			break;
		case SerialPortEvent.DATA_AVAILABLE:
			byte[] readBuffer = new byte[20];
			try {
				// read data
				int numBytes = inputStream.read(readBuffer);
				inputStream.close();

				//send the received data to the GUI
				String result = new String(readBuffer,0,numBytes);
				decodeMsg(result);
			} catch (IOException e) {
				exceptionReport(e);
			}
			break;
		}
	}
	
	public void exceptionReport(Exception e) {
		if(debug) {
			System.out.println(e.toString());
			e.printStackTrace();
		}
	}
	
	public void decodeMsg(String codedMsg){
		System.out.print(codedMsg);
		String msg = VE_Short_MS.fromMS(codedMsg);
		System.out.println("Decoded Msg: "+msg);
	}
}
