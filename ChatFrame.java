import java.awt.*;
import javax.swing.*;

public class ChatFrame extends JFrame {
	public JButton submit = new JButton("Submit");
	public JTextField input = new JTextField("");
	public JTextArea display = new JTextArea("");
	
	public ChatFrame(ChatWindow chatWindow) {
		super("Secure Chat");
		setLayout(new BorderLayout());
		setSize(400, 400);
		setLocationRelativeTo(null);
		
		//add(display);
		display.setEditable(false);
		display.setLineWrap(true);
		display.setWrapStyleWord(true);
		JScrollPane scrollPane = new JScrollPane(display);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		add(scrollPane);
		add(boxPanel(),BorderLayout.SOUTH);
		
		submit.addActionListener(chatWindow);
		input.addActionListener(chatWindow);
	}
	
    public JPanel boxPanel()
    {
    	JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
		
        panel.add(input);
        panel.add(submit,BorderLayout.EAST);
        return panel;
    }
}
