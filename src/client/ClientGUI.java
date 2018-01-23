package client;

import message.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ClientGUI extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1L;

    private JLabel label;
    private JTextField tf;
    private JTextField tfServer, tfPort;
    private JButton login, logout, whoIsIn;

    //chat room
    private JTextArea ta, addresses;
    private boolean connected;
    private Client client;
    private int defaultPort;
    private String defaultHost, defaultAddresses = "Choose server IP:\n localhost\n" +
            " 80.49.249.205\n 45.48.144.135\n";



    ClientGUI(String host, int port){
        super("MyChat");
        defaultPort = port;
        defaultHost = host;

        JPanel northPanel = new JPanel(new GridLayout(4,1));

        JPanel serverAndPort = new JPanel(new GridLayout(1,4));

        tfServer = new JTextField(host);
        tfPort = new JTextField("" + port);

        tfPort.setHorizontalAlignment(SwingConstants.RIGHT);
        tfServer.setHorizontalAlignment(SwingConstants.RIGHT);



        serverAndPort.add(new JLabel("Server Address"));
        serverAndPort.add(tfServer);
        serverAndPort.add(new JLabel("Port Number"));
        serverAndPort.add(tfPort);
        serverAndPort.add(new JLabel(""));
        northPanel.add(serverAndPort);




        // the 3 buttons
        login = new JButton("Login");
        login.addActionListener(this);
        logout = new JButton("Logout");
        logout.addActionListener(this);
        logout.setEnabled(false);
        whoIsIn = new JButton("Who is in");
        whoIsIn.addActionListener(this);
        whoIsIn.setEnabled(false);

        JPanel buttons = new JPanel();
        buttons.add(login);
        buttons.add(logout);
        buttons.add(whoIsIn);
        northPanel.add(buttons);




        label = new JLabel("Enter your username below", SwingConstants.CENTER);
        northPanel.add(label);
        tf = new JTextField("Annonymus");
        tf.setBackground(Color.WHITE);
        northPanel.add(tf);
        add(northPanel, BorderLayout.NORTH);

        // central panel in chat room

        ta = new JTextArea("Welcome to MyChat\n\n", 10,10);
        ta.append(defaultAddresses);
        ta.setBackground(Color.LIGHT_GRAY);
        JPanel centerPanel = new JPanel(new GridLayout(1,1));
        centerPanel.add(new JScrollPane(ta));
        ta.setEditable(false);
        add(centerPanel, BorderLayout.CENTER);




        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(500,500);
        setVisible(true);
        //tf.requestFocus();
    }


    // when client want to append text to textarea
    void append(String str){
        ta.append(str);
        //ta.setCaretPosition(ta.getText().length() - 1);
    }


    // when GUI connection failed - reset all
    void connectionFailed(){
        login.setEnabled(true);
        logout.setEnabled(false);
        whoIsIn.setEnabled(false);
        label.setText("Enter your username below");
        tf.setText("Annonymus");

        // reset port nr and host name
        tfPort.setText("" + defaultPort);
        tfServer.setText(defaultHost);

        // let user make change
        tfServer.setEditable(true);
        tfPort.setEditable(true);

        // save history and reser ta
        client.saveHistory(ta.getText());
        ta.setText("");
        ta.append(defaultAddresses);

        // dont react to CR after username
        tf.removeActionListener(this);
        connected = false;
    }

    // when button or JTextField clicked
    public void actionPerformed(ActionEvent e){
        Object o = e.getSource();

        if(o == logout){
            client.sendMessage(new MessageLogout());
            return;
        }

        if(o == whoIsIn){
            client.sendMessage(new MessageWho());
            return;
        }

        if(connected){
            client.sendMessage(new MessageContent(tf.getText()));
            tf.setText("");
            return;
        }

        if(o == login){
            String username = tf.getText().trim();
            if(username.length() == 0)
                return;

            String server = tfServer.getText().trim();
            if(server.length() == 0)
                return;

            String portNumber = tfPort.getText().trim();
            if(portNumber.length() == 0)
                return;

            int port = 0;
            try{
                port = Integer.parseInt(portNumber);
            }
            catch(Exception e2){
                return;
            }

            // try to create new Client with GUI

            client = new Client(server, port, username, this);

            // change IP adressess to chat area
            ta.setText("");
            //check if everything is ok
            if(!client.start())
                return;

            tf.setText("");
            label.setText("Enter your message: ");
            connected = true;

            login.setEnabled(false);
            logout.setEnabled(true);
            whoIsIn.setEnabled(true);

            tfServer.setEditable(false);
            tfPort.setEditable(false);
            tf.addActionListener(this);
        }
    }


    public static void main(String[] args) {
        new ClientGUI("localhost", 1000);
    }
}
