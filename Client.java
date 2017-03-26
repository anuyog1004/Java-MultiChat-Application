import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

public class Client extends JFrame{
    private JTextField userText;
    private JTextArea chatWindow;
    private Socket connection;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private String message="";
    private String myMessage;
    private String serverIP;
    private String userName;
    private String storageLocation = "/Users/anuyogrohilla/Desktop/Client1/";
    File myFile;
    
    public Client(String host){
        super("User 1");
        userName = "User 1: ";
        serverIP = host;
        JButton send = new JButton("Image");
        JButton file = new JButton("File");
        userText = new JTextField();
        userText.setEditable(false);
        userText.addActionListener(
                new ActionListener(){
                    public void actionPerformed(ActionEvent event){
                        sendData(event.getActionCommand());
                        userText.setText("");
                    }
                }
        );
        add(userText,BorderLayout.SOUTH);
        chatWindow = new JTextArea();
        add( new JScrollPane(chatWindow) );
        setSize(300,150);
        setVisible(true);
    }
    
    public void startRunning(){
        try{
            connectToServer();
            setUpStreams();
            whileChatting();
        }catch(IOException ioException){
            ioException.printStackTrace();
        }finally{
            closeChat();
        }
    }
    
    private void connectToServer() throws IOException{
        connection = new Socket(InetAddress.getByName(serverIP),6789);
    }
    
    private void setUpStreams() throws IOException{
        output = new ObjectOutputStream(connection.getOutputStream());
        output.flush();
        input = new ObjectInputStream(connection.getInputStream());
    }
    
    private void whileChatting() throws IOException{
        ableToType(true);
        do{
            try{
                message = (String) input.readObject();
                myMessage = message.substring(8,message.length());
            }catch(ClassNotFoundException classNotFoundException){
                classNotFoundException.printStackTrace();
            }
            if(myMessage.length()>=15 && myMessage.substring(0,12).equals("Sending file")){
                showMessage(message);
                MyObject myObject=null;
                try{
                    myObject = (MyObject) input.readObject();
                }catch(ClassNotFoundException classNotFoundException){
                    classNotFoundException.printStackTrace();
                }
                byte[] bytes = myObject.getData();
                FileOutputStream fos = new FileOutputStream(storageLocation + getFileName(myMessage));
                fos.write(bytes);
                fos.close();
            }else{
                showMessage(message);
            }
        }while(!message.equals("User 1: END"));
    }
    
    private void closeChat(){
        ableToType(false);
        try{
            output.close();
            input.close();
            connection.close();
        }catch(IOException ioException){
            ioException.printStackTrace();
        }
    }
    
    private void sendData(String message){
        try{
            if(message.length()>=15 && message.substring(0,12).equals("Sending file"))
            {
                output.writeObject(userName + message);
                output.flush();
                String filename = message.substring(15, message.length());
                Path path = Paths.get(filename);
                byte[] byteArray = Files.readAllBytes(path);
                MyObject myObject = new MyObject();
                myObject.setData(byteArray);
                output.writeObject(myObject);
                output.flush();
            }else{
                output.writeObject(userName + message);
                output.flush();
            }
        }catch(IOException ioException){
            ioException.printStackTrace();
        }
    }
    
    private void showMessage(final String message){
        SwingUtilities.invokeLater(
                new Runnable(){
                    public void run(){
                        chatWindow.append("\n" + message);
                    }
                }
        );
    }
    
    private void ableToType(final boolean tof){
        SwingUtilities.invokeLater(
                new Runnable(){
                    public void run(){
                        userText.setEditable(tof);
                    }
                }
        );
    }
    
    private String getFileName(String path){
        int i,index;
        index=0;
        for(i=0;i<path.length();i++){
            if(path.charAt(i)=='/')
                index = i+1;
        }
        return "downloaded - " + path.substring(index);
    } 
}
