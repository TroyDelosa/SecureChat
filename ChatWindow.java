import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import javax.crypto.*;
import javax.crypto.spec.DESedeKeySpec;
import javax.swing.JOptionPane;

public class ChatWindow implements ActionListener {
	private ChatFrame chatFrame;
	private Key key;
	private String name;
	private Socket socket;
	private DataInputStream inputStream;
	private DataOutputStream outputStream;
	private Key publicKey;
	private Key privateKey;
	
	//Client Constructor
	public ChatWindow(String username)
	{
		name = username;
		String ip = JOptionPane.showInputDialog(null,"IP Address/Host Name");
		
		if(ip != null) 
		{
			chatFrame = new ChatFrame(this);
			chatFrame.setVisible(true);
			//Attempt to connect to client
			try {
				
				socket = new Socket();
				socket.connect(new InetSocketAddress(ip, 9876), 500);
				inputStream = new DataInputStream(socket.getInputStream());
				outputStream = new DataOutputStream(socket.getOutputStream());
				
				generateKeyPair();
				outputStream.write(publicKey.getEncoded());
				listenForSecretKey();
				
				String hostname = socket.getInetAddress().getHostName()+" ("+socket.getInetAddress().getHostAddress()+")";
				chatFrame.display.append("Connection established with " +hostname +"\n");
				
				//Begin listening for messages
				new Thread(new MessageListener()).start();
			} 
			//Connection failed, display message and close the chat window
			catch (IOException e) {
				JOptionPane.showMessageDialog(null, ip +" is not available.");
				chatFrame.dispose();
			}
		}
	}
	
	//Server Constructor
	public ChatWindow(String username, Socket server)
	{
		name = username;
		socket = server;
		String hostname = socket.getInetAddress().getHostName()+" ("+socket.getInetAddress().getHostAddress()+")";
		
		chatFrame = new ChatFrame(this);
		chatFrame.setVisible(true);
		
		//Attempt to connect to another client
		try {
			inputStream = new DataInputStream(socket.getInputStream());
			outputStream = new DataOutputStream(socket.getOutputStream());
			
			key = KeyGenerator.getInstance("DESede").generateKey();
			listenForPublicKey();
			outputStream.write(publicKeyEncrypt(key.getEncoded()));
			
			chatFrame.display.append("Connection Established with " +hostname +"\n");
			
			new Thread(new MessageListener()).start();
		} 
		catch (Exception e) {
			JOptionPane.showMessageDialog(null, hostname +" is not available.");
			chatFrame.dispose();
		}
	}
	
	private void generateKeyPair() {
		try {
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
			kpg.initialize(1024);
			KeyPair kp = kpg.genKeyPair();
			publicKey = kp.getPublic();
			privateKey = kp.getPrivate();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void listenForSecretKey() {
		while(true){
			byte[] buffer = new byte[256];
			try {
				int length = inputStream.read(buffer);
				
				byte[] data = new byte[length];
				System.arraycopy(buffer, 0, data, 0, length);
				
				byte[] decrypted = privateKeyDecrypt(data);
				
				key = SecretKeyFactory.getInstance("DESede").generateSecret(new DESedeKeySpec(decrypted));
				break;
				
			} catch (Exception e) {
				//Error
			}
		}
	}
	
	private void listenForPublicKey() {
		while(true){
			byte[] buffer = new byte[256];
			try {
				int length = inputStream.read(buffer);
				
				byte[] data = new byte[length];
				System.arraycopy(buffer, 0, data, 0, length);
				
				publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(data));
				break;
				
			} catch (Exception e) {
				//Error
			}
		}
	}
	
	private byte[] publicKeyEncrypt(byte[] message) throws Exception {
		// Get a cipher object.
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING");
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		
		// Gets the raw bytes to encrypt, UTF8 is needed for
		// having a standard character set
		byte[] stringBytes = message;
		
		// encrypt using the cipher
		byte[] encrypted = cipher.doFinal(stringBytes);
	 
		return encrypted;
	}
	
	private byte[] privateKeyDecrypt(byte[] message) throws Exception {
		// Get a cipher object.
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING");
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		
		// Gets the raw bytes to encrypt, UTF8 is needed for
		// having a standard character set
		byte[] stringBytes = message;
		
		// encrypt using the cipher
		byte[] encrypted = cipher.doFinal(stringBytes);
	 
		return encrypted;
	}
	
	private byte[] secretKeyEncrypt(byte[] message) throws Exception {
		// Get a cipher object.
		Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		
		// Gets the raw bytes to encrypt, UTF8 is needed for
		// having a standard character set
		byte[] stringBytes = message;
		
		// encrypt using the cipher
		byte[] encrypted = cipher.doFinal(stringBytes);
	 
		return encrypted;
	}
	
	private byte[] secretKeyDecrypt(byte[] encrypted) throws Exception {
		// Get a cipher object.
		Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, key);
		
		//decode the message
		byte[] stringBytes = cipher.doFinal(encrypted);
		
		return stringBytes;
	}
	
	private byte[] hash(byte[] message) throws Exception {
		// create a MAC and initialize with the above key
		Mac mac = Mac.getInstance("HmacMD5");
		mac.init(key);
		
		// create a digest from the byte array
		byte[] digest = mac.doFinal(message);
		
		return digest;
	}
	
	public void closeConnection() {
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public class MessageListener implements Runnable {
		public void run() {
			
			//Close the socket when the Chat Window is closed
			chatFrame.addWindowListener(new java.awt.event.WindowAdapter() {
			    @Override
			    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
			    	closeConnection();
			    }
			});
			
			while(true){
				byte[] buffer = new byte[256];
				try {
					int length = inputStream.read(buffer);
					
					//If other client closes the connection, 
					//close the window and stop listening for messages
					if(length == -1) {
						JOptionPane.showMessageDialog(null, "Disconnected");
						socket.close();
						chatFrame.dispose();
						break;
					}
					
					//Remove padding from data byte array
					byte[] data = new byte[length];
					System.arraycopy(buffer, 0, data, 0, length);
					
					//Decrypt the data
					byte[] decrypted = secretKeyDecrypt(data);
					
					//Create byte arrays for the Message and its mac
					byte[] mac = new byte[16];
					byte[] message = new byte[decrypted.length - mac.length];
					
					//Split up the message digest from the message
					System.arraycopy(decrypted, 0, mac, 0, mac.length);
					System.arraycopy(decrypted, 16, message, 0, message.length);
					
					//Check the integrity of the received message
					//Hash the received message and compare it with the received mac
					if(Arrays.equals(mac, hash(message)))
					{
						chatFrame.display.append(new String(message) +"\n");
						chatFrame.display.setCaretPosition(chatFrame.display.getDocument().getLength());
					}
					else
					{
						chatFrame.display.append("\nWarning - The integrity of this message has been compromised!\n");
						chatFrame.display.append(new String(message));
						chatFrame.display.append("\nWarning - The integrity of this message has been compromised!\n");
						chatFrame.display.setCaretPosition(chatFrame.display.getDocument().getLength());
					}
					
				} catch (Exception e) {
					
				}
			}
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if((e.getSource() == chatFrame.submit || e.getSource() == chatFrame.input) && outputStream != null)
		{
			//Make sure the sent message is no longer than 200 characters
			if(chatFrame.input.getText().length() > 200)
			{
				chatFrame.input.setText(chatFrame.input.getText().substring(0, 200));
			}
			
			String input = name +": " +chatFrame.input.getText();
			chatFrame.display.append(input +"\n");
			chatFrame.display.setCaretPosition(chatFrame.display.getDocument().getLength());
			chatFrame.input.setText("");
			
			byte[] mac = null;
			try {
				mac = hash(input.getBytes());
			} catch (Exception e2) {
				e2.printStackTrace();
			}
			
			//Create a byte array that contains both the mac and message
			byte[] combined = new byte[input.getBytes().length + mac.length];
			System.arraycopy(mac, 0, combined, 0, mac.length);
			System.arraycopy(input.getBytes(), 0, combined, mac.length, input.getBytes().length);
			
			//
			try {
				outputStream.write(secretKeyEncrypt(combined));
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}
}
