package kcg.vebrain.main;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.TooManyListenersException;

import kcg.vebrain.communication.Receiver;
import kcg.vebrain.communication.Sender;

public class Main {
	private static final int DEBUG_MODE = 1;
	private static final int NORMAL_MODE = 2;

	private static Scanner reader = new Scanner(System.in);
	private Receiver receiver;
	private Sender sender;
	private CommPortIdentifier portIdentifier;
	private String[] portNames;
	private InputStream inputStream;
	private OutputStream outputStream;
	private SerialPort serialPort;
	private boolean portOpen;
	private boolean debug;
	private boolean running;


	//serial port configuration
	private int baud = 9600;                    //default baud setting
	private int databits = 8;                   //default databits setting
	private int parity = 0;                     //default parity setting
	private int stopbits = 1;

	public static void main(String[] args) {
		System.out.println("Starting..");
		Main main = new Main();
	}

	public Main(){
		portNames = new String[10];
		portOpen = false;
		checkForDebugMode();
		sender = new Sender(debug);
		receiver = new Receiver(sender, debug);
		
		while(!running){
			findPorts();
			System.out.print("Choose the port number you want to open: ");
			int port = reader.nextInt();
			running = startSession(portNames[port-1]);
		}
		
		System.out.println("Send a message");
		while(running){
			System.out.print("> ");
			String msg = reader.nextLine();
			if (msg.equals("-1"))
				running = false;
			else {
				sender.writetoport(msg);
			}
		}
		closePort();
	}

	private void checkForDebugMode(){
		System.out.println("Enter Mode Number ");
		System.out.println(DEBUG_MODE+") Debug Mode.");
		System.out.println(NORMAL_MODE+") Normal Mode.");
		int mode = reader.nextInt();
		switch(mode){
		case DEBUG_MODE:
			debug = true;
			break;
		case NORMAL_MODE:
			debug = false;
			break;
		default:
			debug = false;
			System.out.println("Wrong number, Normal mode is activated");
		}
	}


	private void findPorts() {
		System.out.println("Searching for ports");
		Enumeration<?> portEnum = CommPortIdentifier.getPortIdentifiers();
		int portCount = 0;

		while (portEnum.hasMoreElements()) {
			portIdentifier = (CommPortIdentifier) portEnum.nextElement();

			if(portCount < portNames.length && portIdentifier.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				portNames[portCount++] = portIdentifier.getName();
				System.out.println((portCount)+") "+portIdentifier.getName());
			}
		}        
	}


	public boolean startSession(String portName) {
		if(portOpen) {                          //close any currently open port
			closePort();
		}

		Enumeration<?> portEnum = CommPortIdentifier.getPortIdentifiers();

		while (portEnum.hasMoreElements()) {
			portIdentifier = (CommPortIdentifier) portEnum.nextElement();

			if(portIdentifier.getName().equals(portName) && portIdentifier.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				try {    //try to open the port
					serialPort = (SerialPort)portIdentifier.open("CommUtil", 2000);
					portOpen = true;
					System.out.println(portIdentifier.getName()+" port is now opened");
					break;
				}
				catch (PortInUseException e) {
					exceptionReport(e);
					return false;
				}
				catch (Exception e) {
					exceptionReport(e);
					return false;
				}
			}
		}


		if(portOpen) {
			try {
				inputStream = serialPort.getInputStream();
				receiver.setInputStream(inputStream);
			} catch (IOException e) {
				exceptionReport(e);
				return false;
			}

			try {
				serialPort.addEventListener(receiver);
			} catch (TooManyListenersException e) {
				exceptionReport(e);
				return false;
			}

			// activate the DATA_AVAILABLE notifier
			serialPort.notifyOnDataAvailable(true);

			try {
				//set the port parameters
				serialPort.setSerialPortParams(baud, databits, stopbits, parity);
				System.out.println("Configuration: Baud Rate = "+baud+", Parity = "+parity+", Stop Bits = "+stopbits+", Data Bits = "+databits);
			} catch (UnsupportedCommOperationException e) {
				exceptionReport(e);
			}
			if (!initwritetoport())
				return false;
		}

		return true;
	}


	public boolean initwritetoport(){
		if(portOpen) {
			try {
				// get the outputstream
				outputStream = serialPort.getOutputStream();
				sender.setOutputStream(outputStream);
			} catch (IOException e) {
				exceptionReport(e);
				return false;
			}

			try {
				// activate the OUTPUT_BUFFER_EMPTY notifier
				serialPort.notifyOnOutputEmpty(true);
			} catch (Exception e) {
				exceptionReport(e);
				return false;
			}
		}
		return true;
	}

	private void closePort(){
		try {
			inputStream.close();
		} catch(IOException e) {
			exceptionReport(e);
		}
		System.out.println("closing the opened port");
		serialPort.notifyOnDataAvailable(false);
		serialPort.close();
		serialPort.removeEventListener();
		portOpen = false;
	}

	public void exceptionReport(Exception e) {
		if(debug) {
			System.out.println(e.toString());
			e.printStackTrace();
		}
	}
}