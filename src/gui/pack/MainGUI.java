package gui.pack;

import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;

import dbfs.data.SystemParametersData;
import dbfs.pack.ActionProcessor;
import dbfs.pack.OperationProcessor;
import dbfs.pack.SystemParameters;


/**
 * The main class of the software.
 * Initializes the GUI and starts the software.
 * @author maciej.wojciga
 */

public class MainGUI extends JFrame {

	private static final long serialVersionUID = 1L;

	public static Logger logger = Logger.getLogger(MainGUI.class);

	public static MainGUI mainGUIfrm;
	public JPanel mainPane;

	// Visible fields.
	public JLabel lblConnectedtolbl;
	public JTextArea routeTextArea;
	public JTextField toSendTextField;

	// Other
	Properties confProperties;

	OperationProcessor operationProcessor = null;
	ActionProcessor actionProcessor = null;
	SystemParametersData systemParametersData = new SystemParametersData();

	private void createObjects() {
		operationProcessor = new OperationProcessor(this);
		actionProcessor = new ActionProcessor(this);
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					mainGUIfrm = new MainGUI();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 * 
	 * @throws UnsupportedLookAndFeelException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 */
	public MainGUI() {
		configureProperties();
		initialize();
		setVisible(true);
		createObjects();
		operationProcessor.searchForPorts();
		checkIfFirstLaunch();
	}

	/**
	 * Initialize the contents of the frame.
	 * @throws ParseException 
	 * 
	 * @throws UnsupportedLookAndFeelException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 */
	private void initialize() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		Image icon = Toolkit.getDefaultToolkit().getImage("./img/imim_logo.gif");
		setIconImage(icon);
		setTitle(confProperties.getProperty("name") + " v." + confProperties.getProperty("version"));
		setBounds(01000, 01000, 592, 288);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);

		/* MENU */
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);

		JMenuItem mntmConnect = new JMenuItem("Connect...");
		mnFile.add(mntmConnect);

		JMenuItem mntmDisconnect = new JMenuItem("Disconnect");
		mnFile.add(mntmDisconnect);

		JMenuItem mntmAbout = new JMenuItem("About");
		mnFile.add(mntmAbout);

		JSeparator separator = new JSeparator();
		mnFile.add(separator);

		JMenuItem mntmExit = new JMenuItem("Exit");
		mnFile.add(mntmExit);

		/* MAIN FRAME */
		mainPane = new JPanel();
		mainPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(mainPane);
		mainPane.setLayout(null);
		mainPane.setEnabled(false);

		JButton btnSend = new JButton("Send");
		btnSend.setBounds(10, 205, 89, 23);
		mainPane.add(btnSend);

		routeTextArea = new JTextArea();
		routeTextArea.setEditable(false);
		routeTextArea.setBounds(10, 80, 563, 114);
		mainPane.add(routeTextArea);
		routeTextArea.setEnabled(false);

		JLabel lblConnectedTo = new JLabel("Connected to:");
		lblConnectedTo.setBounds(10, 11, 69, 14);
		mainPane.add(lblConnectedTo);

		lblConnectedtolbl = new JLabel("");
		lblConnectedtolbl.setEnabled(false);
		lblConnectedtolbl.setBounds(109, 11, 112, 14);
		mainPane.add(lblConnectedtolbl);
		
		JLabel lblTimeToChange = new JLabel("Message to send:");
		lblTimeToChange.setBounds(10, 36, 89, 14);
		mainPane.add(lblTimeToChange);

		toSendTextField = new JTextField();
		toSendTextField.setBounds(109, 36, 464, 20);
		mainPane.add(toSendTextField);
		toSendTextField.setColumns(10);

		/* Actions */
		mntmConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				mntmConnectActionPerformed(event);
			}
		});

		mntmDisconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				mntmDisconnectActionPerformed(event);
			}
		});
		
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				btnSendActionPerformed(event);
			}
		});
		
		mntmAbout.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				JOptionPane.showMessageDialog(mainGUIfrm, confProperties.getProperty("name") + " v" + confProperties.getProperty("version") + "\nWritten by: " + confProperties.getProperty("author") + "\n\nCooperator: dr. Roman Major\nInstitute of Metallurgy and Materials Science\nPolish Academy of Sciences", "About", JOptionPane.INFORMATION_MESSAGE);
			}
		});
	}

	/* Action methods */
	private void mntmDisconnectActionPerformed(ActionEvent event) {
		logger.trace("[C]: mntmDisconnect");
		operationProcessor.disconnect();
	}
	
	private void btnSendActionPerformed(ActionEvent event) {
		operationProcessor.writeData(toSendTextField.getText());
	}
	
	private void mntmConnectActionPerformed(ActionEvent event) {
		logger.trace("[C]: mntmConnect");
		ArrayList<String> availableCommPorts = operationProcessor.searchForPorts();
		String[] possibilities = availableCommPorts.toArray(new String[availableCommPorts.size()]);
		String selectedPort = (String)JOptionPane.showInputDialog(mainGUIfrm, "Connect to:", "Connect", JOptionPane.PLAIN_MESSAGE, null, possibilities, null);
		if (selectedPort != null) {
			operationProcessor.connect(selectedPort);
			if (operationProcessor.isConnectedToPort() == true)
			{
				if (operationProcessor.initIOStream() == true)
				{
					operationProcessor.initListener();
				}
			}
		}
	}

	/* Other methods */
	public void configureProperties() {
		confProperties = new Properties();
		try {
			FileInputStream propertiesFileIS =  new FileInputStream(SystemParameters.PROPERTYFILE);
			confProperties.load(propertiesFileIS);
			propertiesFileIS.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void checkIfFirstLaunch() {
		try {
			if (confProperties.getProperty("first.time").equals("1")) {
				File changeLogFile = new File(SystemParameters.CHANGELOGFILE);
				String changelog = actionProcessor.takeFileAndWriteToString(changeLogFile, "[" + confProperties.getProperty("version") + "]");
				JOptionPane.showMessageDialog(mainGUIfrm, changelog + "\n\nNote that you can view this information later in the changelog.txt file in \"conf\" directory.", "What is new in version " + confProperties.getProperty("version") + "?", JOptionPane.INFORMATION_MESSAGE);
			}
			confProperties.setProperty("first.time", "0");
			confProperties.store(new FileOutputStream(new File(SystemParameters.PROPERTYFILE)), "Changed.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
