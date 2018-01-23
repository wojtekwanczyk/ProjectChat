package server;

import message.*;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;


public class Server {
    private static int ID = 0;
    //id dla klientow
    private ArrayList<ClientThread> al;
    private ServerGUI sg;
    private SimpleDateFormat sdf;
    private int port;
    private boolean keepGoing;


    public Server(int port, ServerGUI sg){
        this.sg = sg;
        this.port = port;
        sdf = new SimpleDateFormat("hh:mm:ss");
        al = new ArrayList<>();
    }

    public void start(){
        keepGoing = true;

        // creating server socket and waiting for connection
        try {
            ServerSocket serverSocket = new ServerSocket(port);

            while(keepGoing){
                display("Server waiting for Clients on port " + port + ".");
                Socket socket = serverSocket.accept();
                if(!keepGoing)
                    break;
                ClientThread t = new ClientThread(socket);

                al.add(t);
                t.start();
            }
            // when asked to stop
            try{
                serverSocket.close();
                for(int i=0;i<al.size();i++){
                    ClientThread tc = al.get(i);
                    try{
                        tc.sInput.close();
                        tc.sOutput.close();
                        tc.socket.close();
                    }
                    catch(IOException ioE){
                        ioE.printStackTrace();
                    }
                }
            }
            catch(Exception e){
                display("Exception closing the server and clients: " + e);
            }
        } catch (IOException e) {
            String msg = sdf.format(new Date()) + " Exception on new ServerSocket: "
                    + e + "\n";
            display(msg);
            e.printStackTrace();
        }
    }

    void stop(){
        keepGoing = false;

        try {
            new Socket("localhost", port);
        }
        catch(Exception e){
            //DK what to do
        }
    }


    // Display an event (not message) to GUI
    private void display(String msg){
        String time = sdf.format(new Date()) + " " + msg;
        if(sg != null)
            sg.appendEvent(time + "\n");
    }


    // display message and send to every client
    private synchronized void broadcast(String message){
        String time = sdf.format(new Date());
        String messageLf = "\t\t" + time + " " + message + "\n";

        if(sg != null)
            sg.appendRoom(messageLf);

        // reverse loop in case of removing Client due to disconnection
        for(int i=al.size(); --i >= 0; ){
            ClientThread ct = al.get(i);

            if(!ct.writeMsg(messageLf)){
                al.remove(i);
                display("Disconnected Client " + ct.username + " removed from list.");
            }
        }
    }


    // logout Client through LOGOUT message
    synchronized void remove(int id){
        for(int i=0;i < al.size() ; ++i){
            ClientThread ct = al.get(i);
            if(ct.id == id) {
                al.remove(i);
                return;
            }
        }
    }


    // Instance of thread below is for each Client
    class ClientThread extends Thread{
        Socket socket;
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;
        int id;
        String username;
        Message m;
        String date;        //connection date

        ClientThread(Socket socket){
            id = ++ID;
            this.socket = socket;

            //System.out.println("Thread trying to create Object Input/Output Streams");
            try{
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput = new ObjectInputStream(socket.getInputStream());
                username = (String)sInput.readObject();
                display(username + " have connected.");
            }
            catch(IOException e){
                display("Exception while creating In/Out Streams" + e);
                return;
            } catch (ClassNotFoundException e) {
                // won't happen, we've read String so it will work
                e.printStackTrace();
                return;
            }
            date = new Date().toString() + "\n";


        }

        public void run(){
            boolean keepGoing = true;
            while(keepGoing){
                //reading String which is an Object

                try{
                    m = (Message)sInput.readObject();
                }
                catch(IOException e){
                    display(username + " Exception while reading Streams: " + e);
                    break;
                }
                catch(ClassNotFoundException e){
                    e.printStackTrace();
                    break;
                    //also wont happen
                }


                if(m instanceof MessageContent)
                    broadcast(" " + username + ":\n " + m.getContent());

                if(m instanceof MessageLogout){
                    display(username + " disconnected with a LOGOUT message");
                    keepGoing = false;
                }

                if(m instanceof MessageWho){
                    writeMsg("List of the users connected at " + sdf.format(new Date()) + "\n");

                    // scanning users
                    for(int i=0;i < al.size(); ++i){
                        ClientThread ct = al.get(i);
                        writeMsg((i+1) +") " + ct.username + " since " + ct.date);
                    }
                }
            }

            remove(id);
            close();
        }

        private void close(){
            try{
                if(sOutput != null)
                    sOutput.close();
            }
            catch(Exception e){}
            try{
                if(sInput != null)
                    sInput.close();
            }
            catch(Exception e) {}
            try{
                if(socket != null)
                    socket.close();
            }
            catch(Exception e) {}
        }

        private boolean writeMsg(String msg){
            if(!socket.isConnected()){
                close();
                return false;
            }

            try{
                sOutput.writeObject(msg);
            }
            catch (IOException e){
                //just informing user
                display("Error sending message to " + username);
                display(e.toString());
            }
            return true;
        }
    }

}
