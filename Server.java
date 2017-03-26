import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Server extends JFrame{
    private JTextField userText;
    private JTextArea chatWindow;
    private ServerSocket server;
    private Socket connection;
    private static final int maxClients = 10;
    private static final serverThread[] threads = new serverThread[maxClients];
    
    public Server(){
        super("Main Server");
        userText = new JTextField();
        userText.setEditable(false);
        add(userText,BorderLayout.SOUTH);
        chatWindow = new JTextArea();
        add( new JScrollPane(chatWindow) );
        setSize(300,150);
        setVisible(true);
    }
    
    public void startRunning(){
        try{
            server = new ServerSocket(6789);
            while(true){
                try{
                    connection = server.accept();
                    int i=0;
                    for(i=0;i<maxClients;i++){
                        if(threads[i]==null){
                            ( threads[i] = new serverThread(connection,threads,maxClients,chatWindow) ).start();
                            break;
                        }
                    }
                    if(i==maxClients){
                        connection.close();
                    }
                }catch(IOException ioException){
                    ioException.printStackTrace();
                }
            }
        }catch(IOException ioException){
            ioException.printStackTrace();
        }
    }
}

class serverThread extends Thread{
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private Socket connection;
    private final serverThread[] threads;
    private String message;
    private String myMessage;
    private int maxClients;
    private JTextArea chatWindow;
    
    public serverThread(Socket connection,serverThread[] threads,int maxClients,JTextArea chatWindow){
        this.connection = connection;
        this.threads = threads;
        this.maxClients = maxClients;
        this.chatWindow = chatWindow;
    }
    
    public void run(){
        serverThread[] threads = this.threads;
        try{
            output = new ObjectOutputStream(connection.getOutputStream());
            output.flush();
            input = new ObjectInputStream(connection.getInputStream());
            while(true){
                try{
                    message = (String) input.readObject();
                    myMessage = message.substring(8,message.length());
                }catch(ClassNotFoundException classNotFoundException){
                    classNotFoundException.printStackTrace();
                }
                if(myMessage.length()>=15 && myMessage.substring(0,12).equals("Sending file")){
                    
                    MyObject myObject=null;
                    try{
                        myObject = (MyObject) input.readObject();
                    }catch(ClassNotFoundException classNotFoundException){
                        classNotFoundException.printStackTrace();
                    }
                    
                    for(int i=0;i<maxClients;i++){
                        if(threads[i]!=null && threads[i]!=this){
                            threads[i].output.writeObject(message);
                        }
                    }
                    for(int i=0;i<maxClients;i++){
                        if(threads[i]!=null && threads[i]!=this){
                            threads[i].output.writeObject(myObject);
                        }
                    }
                }else{
                    for(int i=0;i<maxClients;i++){
                        if(threads[i]!=null){
                            threads[i].output.writeObject(message);
                        }
                    }
                }
                if(message.substring(8).equals("END")){
                    break;
                }
            }
            for(int i=0;i<maxClients;i++){
                if(threads[i]==this){
                    threads[i]=null;
                }
            }
        }catch(IOException ioException){
            ioException.printStackTrace();
        }
    }
}