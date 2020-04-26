import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class MenuWindow implements ActionListener {
	private MenuFrame menuFrame;
	private boolean isOnline = false;
	private ServerSocket welcomeSocket;
	private ArrayList<ChatWindow> chatWindow;
	
	public MenuWindow() throws UnknownHostException
	{
		menuFrame = new MenuFrame(this);
		menuFrame.setVisible(true);
		chatWindow = new ArrayList<ChatWindow>();
		
		//Close the socket when the Chat Window is closed
		menuFrame.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				for(ChatWindow cw: chatWindow)
				{
					cw.closeConnection();
				}
		    }
		});
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == menuFrame.status)
		{
			if(isOnline){
				menuFrame.status.setText("Status: Offline");
				try {
					welcomeSocket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				isOnline = false;
			}
			else{
				menuFrame.status.setText("Status: Online");
				isOnline = true;
				new Thread(new ConnectionListener()).start();
			}
		}
		if(e.getSource() == menuFrame.connect)
		{
			chatWindow.add(new ChatWindow(menuFrame.name.getText()));
		}
		if(e.getSource() == menuFrame.exit)
		{
			for(ChatWindow cw: chatWindow)
			{
				cw.closeConnection();
			}
			System.exit(0);
		}
	}
	
	public class ConnectionListener implements Runnable {
		public void run() {
			try {
				welcomeSocket = new ServerSocket(9876);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			while(isOnline)
			{
				try {
					Socket serverSocket = welcomeSocket.accept();
					chatWindow.add(new ChatWindow(menuFrame.name.getText(), serverSocket));
				} catch(Exception e) {
					//welcomeSocket closed
				}
			}
		}
	}
}
