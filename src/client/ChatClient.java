package client;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;


/**
 *
 */
public class ChatClient {
    private String serverAddress;
    private int serverPort;
    private Scanner in;
    private PrintWriter out;
    private JFrame frame = new JFrame("Chatter");
    private JTextField textField = new JTextField(50);
    private JTextArea messageArea = new JTextArea(16,50);
    private Font font=new Font("宋体",Font.PLAIN,20);



    public ChatClient(String serverAddress,int serverPort){
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        messageArea.setFont(font);
        textField.setFont(font);
        textField.setEditable(false);
        messageArea.setEditable(false);
        frame.getContentPane().add(textField, BorderLayout.SOUTH);
        frame.getContentPane().add(new JScrollPane(messageArea),BorderLayout.CENTER);
        frame.pack();

        // event listener,send message client input to server
        textField.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String input = textField.getText();
                if (input != null  &&  !input.isBlank()){
                    out.println(input);
                    textField.setText("");
                }
            }
        });
    }


    // get name from client what input to TextField
    private String getName(){
        return JOptionPane.showInputDialog(frame,"Choose a screen name:", "Screen name selection",JOptionPane.PLAIN_MESSAGE);
    }


    private void run(){
        try {
            Socket socket = new Socket(serverAddress,serverPort);
            in = new Scanner(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(),true);

            // get and handle server response
            while (in.hasNextLine()){
                // get message from server
                String line = in.nextLine();
                System.out.println(line);

                if (line.startsWith("SUBMIT YOUR NAME")){
                    out.println(getName());
                }else if (line.startsWith("NAME ACCEPTED:")){
                    this.frame.setTitle("Chatter - " + line.substring(line.indexOf(":")+1));
                    textField.setEditable(true);
                }else if (line.startsWith("MESSAGE")){
                    messageArea.append(line.substring(8)+"\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            frame.setVisible(false);
            frame.dispose();
        }
    }


    public static void main(String[] args) {
        ChatClient client = new ChatClient("localhost",44583);

        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);

        client.run();
    }
}