package dbfs.pack;

import dbfs.data.InputMessageData;
import dbfs.data.OutputMessageData;
import dbfs.data.SystemParametersData;

/**
 * Calculates things.
 * @author maciej.wojciga
 */

public class Calculations {

	static SystemParametersData systemParametersData = new SystemParametersData();

	/**
	 * Takes input message and outputs useful data.
	 * @param inputMessage
	 * @return
	 */
	public InputMessageData processInputMessage(String inputMessage, InputMessageData inputMessageData) {
		// Sr000l000t000e0E
		int rStartIM = inputMessage.indexOf("r") + 1;
		int rEndIM = inputMessage.indexOf("l");
		int lStartIM = inputMessage.indexOf("l") + 1;
		int lEndIM = inputMessage.indexOf("t");
		int tStartIM = inputMessage.indexOf("t") + 1;
		int tEndIM = inputMessage.indexOf("e");
		int eStartIM = inputMessage.indexOf("e") + 1;
		int eEndIM = inputMessage.indexOf("E");
		// Fill DTO with new values.
		inputMessageData.setrIM(Integer.parseInt(inputMessage.substring(rStartIM, rEndIM)));
		inputMessageData.setlIM(Integer.parseInt(inputMessage.substring(lStartIM, lEndIM)));
		inputMessageData.settIM(Integer.parseInt(inputMessage.substring(tStartIM, tEndIM)));
		inputMessageData.seteIM(Integer.parseInt(inputMessage.substring(eStartIM, eEndIM)));
		return inputMessageData;
	}

	/**
	 * Takes output message and outputs useful data.
	 * @param outputMessage
	 * @param outputMessageData
	 * @return
	 */
	public OutputMessageData processOutputMessage(String outputMessage, OutputMessageData outputMessageData) {
		// Sr000l000e0E
		int rStartIM = outputMessage.indexOf("r") + 1;
		int rEndIM = outputMessage.indexOf("l");
		int lStartIM = outputMessage.indexOf("l") + 1;
		int lEndIM = outputMessage.indexOf("e");
		int eStartIM = outputMessage.indexOf("e") + 1;
		int eEndIM = outputMessage.indexOf("E");
		// Fill DTO with new values.
		outputMessageData.setrIM(Integer.parseInt(outputMessage.substring(rStartIM, rEndIM)));
		outputMessageData.setlIM(Integer.parseInt(outputMessage.substring(lStartIM, lEndIM)));
		outputMessageData.seteIM(Integer.parseInt(outputMessage.substring(eStartIM, eEndIM)));
		return outputMessageData;
	}

}
