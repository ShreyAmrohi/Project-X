package chatX;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.UIManager;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.Toolkit;

public class ClientWindow extends JFrame implements Runnable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField txtMessage;
	private JTextArea txtArchive;
	private JPanel contentPane;
	
	private ClientX client;
	private Thread listen;
	private Thread run;
	private boolean running=false;
	private JMenuBar menuBar;
	
	private OnlineUsers users;
  	
	public ClientWindow(String name,String address,int port) {
		setIconImage(Toolkit.getDefaultToolkit().getImage("C:\\Users\\Shrey Amrohi\\Desktop\\Images\\logo.jpg"));
		setBackground(UIManager.getColor("Button.foreground"));
		setTitle("Client X");
		client=new ClientX(name,address,port);
		boolean connect=client.openConnection(address);  //Returning boolean value to variable connect 
		if(!connect)
		{
			System.out.println("Connection Failed!!");
			console("Connection Failed!!");
		}
		createWindow();
		console("   \t* Attempting to connect * "+address+ " Port : "+port);
		console("     \t User ID :"+name);
        String  connection="/c/"+name+"/e/";
       client.send(connection.getBytes());
       
       users=new OnlineUsers();
       running=true;
       run=new Thread(this,"Running Thread");
       run.start();
       
       
      }
	
	private void createWindow()
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 750, 743);
		setSize(787,783);
		
		menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu File = new JMenu("File");
		menuBar.add(File);
		
		JMenuItem mntmOnlineUser = new JMenuItem("Online User");
		mntmOnlineUser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				users.setVisible(true);
			}
		});
		File.add(mntmOnlineUser);
		
		JMenuItem mntmExit = new JMenuItem("Exit");
		File.add(mntmExit);
		contentPane = new JPanel();
		contentPane.setBackground(new Color(123, 104, 238));
		contentPane.setForeground(Color.BLACK);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		txtArchive = new JTextArea();  //Chat Archive 
		txtArchive.setEditable(false);
		txtArchive.setWrapStyleWord(true);
		txtArchive.setLineWrap(true);
		txtArchive.setBackground(Color.WHITE);
		txtArchive.setForeground(Color.BLACK);
		txtArchive.setBounds(12, 13, 745, 639);
		contentPane.add(txtArchive);
		
		
		
		txtMessage = new JTextField();           //Message Field for message input
		txtMessage.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode()==KeyEvent.VK_ENTER)
				{                               //Action Performed on Enter Key pressed 
					send(txtMessage.getText(),true);
				}
				
			}
		});
		txtMessage.setBackground(UIManager.getColor("Button.background"));
		txtMessage.setForeground(Color.BLACK);
		txtMessage.setText("\r\n");
		txtMessage.setBounds(12, 665, 581, 32);
		contentPane.add(txtMessage);
		txtMessage.setColumns(10);
		
		JButton btnSend = new JButton("Send");   //Send button 
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				send(txtMessage.getText(),true);    //Action Performed on Click of button       
			}
		});
		btnSend.setForeground(Color.BLACK);
		btnSend.setFont(new Font("Comic Sans MS", Font.BOLD, 14));
		btnSend.setBounds(605, 665, 152, 33);
		contentPane.add(btnSend);
		
		addWindowListener(new WindowAdapter() //Closing Listener
			{
				public void WindowClosing()
			          { 
					     String disconnect="/d/"+client.getID()+"/e/";
					     send(disconnect,false);
					     running=false;	
					     client.close();
					   				     
			          }
			
			
				});
		
		setVisible(true);
		txtMessage.requestFocusInWindow();      //To focus directly on the Message Text for input
	}
	
	public void run()
	{
		listen();
	}
	
    private void send(String message,boolean text)          //Send button function
	{
		if(message.equals("")) return;
		Date thisDate=new Date();
		SimpleDateFormat dateform=new SimpleDateFormat("h:mm a");          //Date Time format Hour:Minute
	   
		if(text)
		{
			message=client.getname()+" : "+message+" "+dateform.format(thisDate);
		    //console(message);          //Message with Name and Message
	        message="/m/"+message+"/e/";
	        txtMessage.setText(" ");
			txtMessage.requestFocusInWindow();
		}
		client.send(message.getBytes());
	    
	}
	
	
	public void console(String message)       //Display on Console Client Message
	{ 
	    Date thisDate=new Date();
	    SimpleDateFormat dateform=new SimpleDateFormat("h:mm a");
	    txtArchive.append(" "+message+" \n");
	    txtArchive.setCaretPosition(txtArchive.getDocument().getLength());
		
	}
	
	public void listen()
	{
		 listen=new Thread("Listen Thread")
		  {  public void run()
		   {
			  while(running)
		     {
			  String message=client.recieve();
				if(message.startsWith("/c/"))     // substring from /c/2496 = 3,message.length()
				{
				   client.setID(Integer.parseInt(message.split("/c/|/e/")[1]));
				   console("Connection Established Successfully to Server Main.  ID: "+client.getID());
				}
				else if(message.startsWith("/m/"))
				{
					
				  String text=message.substring(3);
				  text=text.split("/e")[0];
				  console(text);     //All clients including source console
				}
				
				else if(message.startsWith("/i/"))
				{
					String text="/i/"+client.getID()+"/e/";
					send(text,false);
					
				}
				else if(message.startsWith("/u/"))
				{
					String u[]=message.split("/u/|/n/|/e/");
					
					users.Update(Arrays.copyOfRange(u,1,u.length-1));
					
				}
		     }
		   }	 
		 };
		 listen.start();
	}
}
