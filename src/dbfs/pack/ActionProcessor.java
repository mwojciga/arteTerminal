package dbfs.pack;

import gui.pack.MainGUI;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.Logger;

public class ActionProcessor {
	MainGUI mainGUIfrm = null;
	Calculations calculations = new Calculations();

	/* LOG */
	Logger logger = Logger.getLogger(ActionProcessor.class);
	Log log = new Log(mainGUIfrm);
	
	public ActionProcessor(MainGUI mainGUIfrm){
		this.mainGUIfrm = mainGUIfrm;
	}

	public String takeFileAndWriteToString(File inputFile, String onlyThese){
		String outputString = "";
		try {
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			String line = "";
			while ((line = reader.readLine()) != null){
				if (!onlyThese.equals(null)) {
					if (line.startsWith(onlyThese)) {
						String lineArr[] = line.split("]");
						outputString += lineArr[1] + "\n";
					}
				} else {
					outputString += line + "\n";
				}
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return outputString;
	}
}
