package server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class ServerGUI extends JFrame implements ActionListener, WindowListener {

    //private static final long serialVersionUID = 1L;
    private JButton stopStart;
    private JTextArea chat, event;
    private JTextField tPortNumber;
    private Server server;

    ServerGUI(int port){
        super("Chat Server");
        server = null;

        JPanel north = new JPanel();
        north.add(new JLabel("Port number: "));
        tPortNumber = new JTextField("   " + port);
        north.add(tPortNumber);



        stopStart = new JButton("Start");
        stopStart.addActionListener(this);
        north.add(stopStart);
        add(north, BorderLayout.NORTH);

        // the event field and chat room field
        JPanel center = new JPanel(new GridLayout(2,1));

        chat = new JTextArea(5,30);
        chat.setEditable(false);
        appendRoom("Chat room. \n");
        center.add(new JScrollPane(chat));

        event = new JTextArea(5,30);
        event.setEditable(false);
        appendEvent("Events log.\n");
        center.add( new JScrollPane(event));

        add(center);



        addWindowListener(this);
        setSize(500,500);
        setVisible(true);
    }

    // append msg to JTextArea
    void appendRoom(String str){
        chat.append(str);
        //chat.setCaretPosition(chat.getText().length() - 1);
    }

    void appendEvent(String str){
        event.append(str);
        //event.setCaretPosition(chat.getText().length() - 1);
    }


    // start and stop
    public void actionPerformed(ActionEvent a){
        if(server != null){
            server.stop();
            server = null;
            tPortNumber.setEditable(true);
            stopStart.setText("Start");
            return;
        }

        int port;
        try{
            port = Integer.parseInt(tPortNumber.getText().trim());
        }
        catch(Exception e){
            appendEvent("Invalid port number.\n");
            return;
        }

        server = new Server(port,this);
        new ServerRunning().start();
        stopStart.setText("Stop");
        tPortNumber.setEditable(false);

    }


    // start of the server
    public static void main(String[] args) {
        new ServerGUI(1000);
    }

    public void windowClosing(WindowEvent e){
        if(server != null){
            try{
                server.stop();
            }
            catch(Exception eClose){
                System.out.println("Error while closing server. \n" + eClose);
            }
            server = null;
        }
        dispose();
        // to be sure:
        System.exit(0);

    }

    // other methods which i dont need
    public void windowClosed(WindowEvent e) {}
    public void windowOpened(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowActivated(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}


    // thread to run Server

    class ServerRunning extends Thread{
        public void run(){
            server.start();

            // if failed
            stopStart.setText("Start");
            tPortNumber.setEditable(true);
            appendEvent("Server closed\n");
            server = null;
        }
    }
}
