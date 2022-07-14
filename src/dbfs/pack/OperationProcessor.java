package dbfs.pack;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gui.pack.MainGUI;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.TooManyListenersException;

import org.apache.log4j.Logger;

import dbfs.data.InputMessageData;
import dbfs.data.SystemParametersData;


/**
 * Operations class.
 * @author maciej.wojciga
 */

public class OperationProcessor implements SerialPortEventListener {

	/* LOG */
	Logger logger = Logger.getLogger(OperationProcessor.class);
	Log log;

	/* OTHER */
	MainGUI mainGUI;
	private Enumeration availablePorts = null;
	private HashMap portMap = new HashMap();
	private CommPortIdentifier selectedPortIdentifier = null;
	private SerialPort openedSerialPort = null;
	private boolean connectedToPort = false;
	private InputStream inputStream = null;
	private OutputStream outputStream = null;

	boolean actualMS = false;
	boolean errorMS = false;
	boolean compareMS = false;

	Calculations calculations = new Calculations();
	SystemParametersData systemParametersData = new SystemParametersData();

	private String sendedMessage = "initialMessage";

	public boolean calibrateDone = false;
	public boolean shouldWait = false;

	byte[] buffer = new byte[1024];
	int bytes;
	String end = "E";
	StringBuilder curMsg = new StringBuilder();
	public String inputMessage = "";


	public OperationProcessor(MainGUI mainGUI) {
		this.mainGUI = mainGUI;
		log = new Log(mainGUI);
	}

	public ArrayList<String> searchForPorts() {
		ArrayList<String> availableCommPorts = new ArrayList<String>();;
		availablePorts = CommPortIdentifier.getPortIdentifiers();
		while (availablePorts.hasMoreElements()) {
			CommPortIdentifier currentPort = (CommPortIdentifier) availablePorts.nextElement();
			logger.info("Found port: " + currentPort.getName());
			if (currentPort.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				availableCommPorts.add(currentPort.getName());
				portMap.put(currentPort.getName(), currentPort);
				logger.info(currentPort.getName() + " is a serial port. Added.");
			}
		}
		return availableCommPorts;
	}

	public void connect(String selectedPort) {
		logger.info("Connecting to " + selectedPort);
		selectedPortIdentifier = (CommPortIdentifier) portMap.get(selectedPort);
		CommPort commPort = null;
		try {
			commPort = selectedPortIdentifier.open("ShearRobot", SystemParameters.TIMEOUT);
			openedSerialPort = (SerialPort) commPort;
			openedSerialPort.setSerialPortParams(SystemParameters.DATA_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			connectedToPort = true;
			mainGUI.lblConnectedtolbl.setText(selectedPort);
			log.log("Successfully connected to " + commPort.getName() + ".", null);
			logger.info("Successfully connected to " + commPort.getName());
		} catch (PortInUseException e) {
			log.log("Could not connect: port is already in use.", null);
			logger.info("Could not connect: port is already in use.");
		} catch (Exception e) {
			log.log("Could not connect: " + e.toString(), null);
			logger.info("Could not connect: " + e.toString());
		}
	}

	public boolean initIOStream() {
		log.log("Opening IOStream...", null);
		logger.info("Opening IOStream.");
		boolean ioStreamOpened = false;
		try {
			inputStream = openedSerialPort.getInputStream();
			outputStream = openedSerialPort.getOutputStream();
			ioStreamOpened = true;
			logger.info("IOStream successfully opened.");
		} catch (IOException e) {
			log.log("Could not open IOStream.", null);
			logger.info("Could not open IOStream." + e.toString());
		}
		return ioStreamOpened;
	}

	public void initListener() {
		try {
			log.log("Initializing listener...", null);
			logger.info("Initializing listener.");
			openedSerialPort.addEventListener(this);
			openedSerialPort.notifyOnDataAvailable(true);
		} catch (TooManyListenersException e) {
			log.log("Could not add event listener.", null);
			logger.info("Could not add event listener. " + e.toString());
		}
	}

	public void disconnect() {
		if (connectedToPort == true) {
			openedSerialPort.removeEventListener();
			openedSerialPort.close();
			logger.info("Disconnected from " + openedSerialPort.getName());
			try {
				inputStream.close();
				outputStream.close();
				connectedToPort = false;
				logger.info("IOStream closed.");
			} catch (IOException e) {
				logger.info("Could not close IOStream." + e.toString());
			}
		} else {
			logger.info("Tried to disconnect, but no port was opened.");
		}
	}

	@Override
	public void serialEvent(SerialPortEvent event) {
		if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
				// Input message:  
				inputMessage = "";
				bytes = inputStream.read(buffer);
				curMsg.append(new String(buffer, 0, bytes, Charset.forName("UTF-8")));
				int endIdx = curMsg.indexOf(end);
				if (endIdx != -1) {
					inputMessage = curMsg.substring(0, endIdx + end.length()).trim();
					curMsg.delete(0, endIdx + end.length());
					log.log(null, "Received: " + inputMessage);
					logger.info("Received: " + inputMessage);
					System.out.println("[R]: " + inputMessage);
					// Check if there is an error in the message.
					errorMS = false;
					errorMS = checkIfError(inputMessage);
					if (errorMS) {
						log.log("Received a message with an error from " + openedSerialPort.getName(), "Received a message with an error from " + openedSerialPort.getName());
						logger.info("Received a message with an error from " + openedSerialPort.getName());
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				log.log("An exception occured. See more in log file.", "An exception occured. See more in log file.");
				logger.error(e.toString());
			}
		}

	}

	/**
	 * Writes data to uC.
	 * @param rightRelayTime
	 * @param leftRelayTime
	 * @param error
	 */
	public void writeData(String toSend) {
		try {
			// Send the message.
			outputStream.flush();
			System.out.println("[S]: " + toSend);
			outputStream.write(toSend.getBytes());
			outputStream.flush();
		} catch (Exception e) {
			log.log("Could not write data: " + e.toString(), "Could not write data: " + e.toString());
			logger.info("Could not write data: " + e.toString());
		}
	}

	/**
	 * Checks if there has been an error in input message.
	 * @param inputMessage
	 * @return
	 */
	public boolean checkIfError(String inputMessage) {
		boolean errorInMessage = false;
		InputMessageData inputMessageData = new InputMessageData();
		inputMessageData = calculations.processInputMessage(inputMessage, inputMessageData);
		if (inputMessageData.geteIM() == 1) {
			errorInMessage = true;
		}
		return errorInMessage;
	}

	/* GETTERS & SETTERS */

	public boolean isConnectedToPort() {
		return connectedToPort;
	}

	public void setConnectedToPort(boolean connectedToPort) {
		this.connectedToPort = connectedToPort;
	}

	public String getSendedMessage() {
		return sendedMessage;
	}

	public void setSendedMessage(String sendedMessage) {
		this.sendedMessage = sendedMessage;
	}

	public String getInputMessage() {
		return inputMessage;
	}

	public void setInputMessage(String inputMessage) {
		this.inputMessage = inputMessage;
	}

}