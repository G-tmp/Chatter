package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 *  1.response "SUBMIT YOUR NAME"
 *  2.response "NAME ACCEPTED:"+name  &&  broadcast [MESSAGE] has joined"+name
 *  3.response ""
 *  4.broadcast [MESSAGE] client input
 *  5.broadcast [MESSAGE] who quit
 */
public class ChatServer {

    //  duplicates name
    private static Set<String> names = new HashSet<>();

    // output stream collection , used for broadcast
    private static Set<PrintWriter> writers = new HashSet<>();


    private static class Handler implements  Runnable{
        private String name;
        private Socket connectionSocket;
        private Scanner in;
        private PrintWriter out;



        public Handler(Socket connectionSocket) {
            this.connectionSocket = connectionSocket;
        }


        @Override
        public void run() {
            try {
                // read message from socket
                in = new Scanner(connectionSocket.getInputStream());
                // write message to socket
                out = new PrintWriter(connectionSocket.getOutputStream(),true);

                // require name until valid  ||  null exit
                while (true){
                    out.println("SUBMIT YOUR NAME");
                    name = in.nextLine();

                    if (name == null)
                        return;

                    // require lock
                    synchronized (name.intern()){
                        if (!name.isBlank() && !names.contains(name)){
                            names.add(name);
                            this.name = name;
                            break;
                        }
                    }
                }

                // response this.client
                out.println("NAME ACCEPTED: "+name);

                // broadcast all clients except this.client
                for (PrintWriter pw:writers){
                    pw.println("MESSAGE "+name+" has joined");
                }
                writers.add(out);

                // receive message from this.client
                while (true){
                    // message from this.client input
                    String input = in.nextLine();

                    // exit
                    if (input.equals("/quit")){
                        return;
                    }

                    // broadcast
                    for (PrintWriter pw:writers){
                        pw.println("MESSAGE ["+name+"] "+input);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if (out!=null){
                    writers.remove(out);
                }

                if (name != null){
                    System.out.println("MESSAGE "+name+" will leave");
                    names.remove(name);

                    for (PrintWriter pw:writers){
                        pw.println("MESSAGE "+name+" has left");
                    }
                }

                try {
                    connectionSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    public static void main(String[] args) {
        ExecutorService pool = Executors.newFixedThreadPool(100);
        int serverPort = 44583;
        System.out.println("chat server is running...");

        try (ServerSocket socket = new ServerSocket(serverPort)){
            while (true) {
                pool.execute(new Handler(socket.accept()));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}