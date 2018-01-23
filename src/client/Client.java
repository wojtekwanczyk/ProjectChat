package client;

import message.*;

import java.net.*;
import java.io.*;

public class Client {
    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;
    private Socket socket;
    private ClientGUI cg;

    private String server, username;
    private int port;


    Client(String server, int port, String username, ClientGUI cg){
        this.server = server;
        this.port = port;
        this.username = username;
        this.cg = cg;
    }

    public boolean start(){
        try{
            socket = new Socket(server,port);
        }
        catch(Exception e){
            display("Error connecting to server:\n" + e);
            return false;
        }

        String msg = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();
        display(msg);

        // creating both data streams
        try{
            sInput = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        }
        catch(IOException e){
            display("Exception while creating In/Out Streams:\n" + e);
            return false;
        }

        // starting thread that will listen to a server
        new ListenFromServer().start();

        try{
            sOutput.writeObject(username);
        } catch (IOException e) {
            display("Exception while logging in: " + e);
            disconnect();
            return false;
        }

        return true;
    }

    // func. sending message to GUI
    private void display(String msg){
        if(cg != null)
            cg.append(msg + "\n");
    }

    //sending message to a server
    void sendMessage(Message msg){
        try{
            sOutput.writeObject(msg);
        }
        catch(IOException e){
            display("Exception while writing to server: " + e);
        }
    }

    private void disconnect(){
        try{
            if(sInput != null)
                sInput.close();
        }
        catch(Exception e){}

        try{
            if(sOutput != null)
                sOutput.close();
        } catch (IOException e) {}

        try{
            if(socket != null)
                socket.close();
        } catch (IOException e) {}

        if(cg != null)
            cg.connectionFailed();
    }

    public void saveHistory(String text){
        try{
            FileWriter fw = new FileWriter("history.txt", true);
            text = text.replace("\t", System.lineSeparator());
            System.out.println(text);
            fw.write(text);
            fw.close();
        }
        catch(IOException e){
            display("Exception while creating/opening file:\n" + e);
        }

    }



    //class that waits for message from server to append to gui
    class ListenFromServer extends Thread {

        public void run(){
            while(true){
                try{
                    String msg = (String)sInput.readObject();
                    if(cg != null){
                        cg.append(msg);
                    }
                }
                catch(IOException e){
                    display("\tServer has closed the connection.\n");
                    if(cg != null)
                        cg.connectionFailed();
                    break;
                }
                catch(ClassNotFoundException e){
                    // probably wont happen
                }
            }
        }
    }

}
