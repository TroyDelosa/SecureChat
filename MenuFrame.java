import java.awt.*;
import javax.swing.*;

public class MenuFrame extends JFrame {
	public JButton status = new JButton("Status: Offline");
	public JButton connect = new JButton("Connect");
	public JButton exit = new JButton("Exit");
	public JTextField name = new JTextField("User");
	
	public MenuFrame(MenuWindow menuWindow) {
		super("Secure Chat");
		setLayout(new GridLayout(4,1,2,2));
		setSize(300, 150);
		setLocationRelativeTo(null);
		
		add(namePanel());
		add(status);
		add(connect);
		add(exit);
		
		status.addActionListener(menuWindow);
		connect.addActionListener(menuWindow);
		exit.addActionListener(menuWindow);
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
    public JPanel namePanel()
    {
    	JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
		
        panel.add(name);
        panel.add(new JLabel("Name: "),BorderLayout.WEST);
        return panel;
    }
}
