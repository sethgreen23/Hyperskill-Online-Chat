package chat.server;

import chat.Database;
import javafx.util.Pair;

import javax.xml.crypto.Data;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import java.net.ServerSocket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * A chat server that delivers public and private messages and files.
 */
public class Server {

    // The server socket.
    private static ServerSocket serverSocket = null;
    // The client socket.
    private static Socket clientSocket = null;
    public static Database database = new Database();
    public static List<Pair<String, String>> messages = new ArrayList<>();
    public static ArrayList<clientThread> clients = new ArrayList<clientThread>();

    public static void main(String args[]) {

        // The default port number.
        int portNumber = 1234;


        if (args.length < 1)
        {

            //System.out.println("No port specified by user.\nServer is running using default port number=" + portNumber);

        }
        else
        {
            portNumber = Integer.valueOf(args[0]).intValue();

            //System.out.println("Server is running using specified port number=" + portNumber);
        }

        /*
         * Open a server socket on the portNumber (default 1234).
         */
        try {
            serverSocket = new ServerSocket(portNumber);
        } catch (IOException e) {
            System.out.println("Server Socket cannot be created");
        }

        /*
         * Create a client socket for each connection and pass it to a new client
         * thread.
         */

        int clientNum = 1;
        while (true) {
            try {

                clientSocket = serverSocket.accept();
                clientThread curr_client =  new clientThread(clientSocket, clients);
                clients.add(curr_client);
                curr_client.start();
                //  System.out.println("Client "  + clientNum + " is connected!");
                clientNum++;

            } catch (IOException e) {

                System.out.println("Client could not be connected");
            }


        }

    }
}

/*
 * This client thread class handles individual clients in their respective threads
 * by opening a separate input and output streams.
 */
class clientThread extends Thread {

    private String clientName = null;
    private ObjectInputStream is = null;
    private ObjectOutputStream os = null;
    private Socket clientSocket = null;
    private final ArrayList<clientThread> clients;
    private boolean close = false;
    private boolean authorized = false;

    public clientThread(Socket clientSocket, ArrayList<clientThread> clients) {

        this.clientSocket = clientSocket;
        this.clients = clients;

    }


    public void stopProcess() {
        close = true;
    }



    public void run() {

        ArrayList<clientThread> clients = this.clients;

        try {
            /*
             * Create input and output streams for this client.
             */
            is = new ObjectInputStream(clientSocket.getInputStream());
            os = new ObjectOutputStream(clientSocket.getOutputStream());

            String name = "" + Math.random();
            boolean previousAnotherName = false;
         /*   while (true) {

                synchronized(this)
                {
                    if (!previousAnotherName) {
                        this.os.writeObject("Server: write your name");
                        this.os.flush();
                    }

                    name = ((String) this.is.readObject()).trim();

                    boolean usernameExists = false;

                    for (clientThread curr_client : clients)
                    {
                        if (curr_client.clientName != null) {

                            if (curr_client.clientName.substring(1).equals(name)) {
                                usernameExists = true;
                            }
                        }
                    }

                    if (!usernameExists) {
                        break;
                    } else {
                        previousAnotherName = true;
                        this.os.writeObject("Server: This name is already in use! Choose another one");
                        this.os.flush();
                    }
                }
            }*/

            /* Welcome the new the client. */
         //   Server.database.addUserChat(name, "password");
        //    System.out.println(Server.database.getCorrespondenceMessages());
            //System.out.println("Client Name is " + name);
            List<Pair<String, String>> message = Server.messages.subList(Math.max(Server.messages.size() - 10, 0), Server.messages.size());
            for (Pair<String, String> line : message) {
                this.os.writeObject("" + line.getKey() + ": " + line.getValue());
                this.os.flush();
            }
                /*this.os.writeObject("*** Welcome " + name + " to our chat room ***\nEnter /quit to leave the chat room");
                this.os.flush();*/

                /*this.os.writeObject("Directory Created");
                this.os.flush();*/
            synchronized(this)
            {

                for (clientThread curr_client : clients)
                {
                    if (curr_client != null && curr_client == this) {
                        clientName = "@" + name;
                        break;
                    }
                }

            /*  for (clientThread curr_client : clients) {
                    if (curr_client != null && curr_client != this) {
                        curr_client.os.writeObject(name + " has joined");
                        curr_client.os.flush();

                    }

                }*/
            }

            /* Start the conversation. */

            while (true) {

            /*  this.os.writeObject("");
                this.os.flush();*/
                System.out.println(Server.database.getCorrespondenceMessages());
                System.out.println(Server.database.getUsers());
                String line = (String) is.readObject();


                if (line.startsWith("/exit")) {

                    break;
                } else if (line.startsWith("/registration")) {
                    String[] words = line.split(" ");
                    if (Server.database.usernameExists(words[1])) {
                        this.os.writeObject("Server: this login is already taken!");
                        this.os.flush();
                    } else {
                        if (words[2].length() < 8) {
                            this.os.writeObject("Server: the password is too short!");
                            this.os.flush();
                        } else {
                            Server.database.addUserChat(words[1], words[2]);
                        }
                    }
                } else if (line.startsWith("/auth")) {

                } else if (line.startsWith("/chat")) {
                    if (!authorized) {
                        this.os.writeObject("Server: you are not in the chat!");
                        this.os.flush();
                    } else {

                    }
                } else if (line.startsWith("/list")) {
                    if (!authorized) {
                        this.os.writeObject("Server: you are not in the chat!");
                        this.os.flush();
                    } else {

                    }
                } else if (line.startsWith("/")) {
                    this.os.writeObject("Server: incorrect command!");
                    this.os.flush();
                } else {
                    if (!authorized) {
                        this.os.writeObject("Server: you are not in the chat!");
                        this.os.flush();
                    } else {
                        broadcast(line,name, true);
                    }

                }

            }

            /* Terminate the Session for a particluar user */

            this.os.writeObject("/stop");
            this.os.flush();
            //System.out.println(name + " disconnected.");
            clients.remove(this);


            synchronized(this) {

                if (!clients.isEmpty()) {

                    for (clientThread curr_client : clients) {


                    /*  if (curr_client != null && curr_client != this && curr_client.clientName != null) {
                            curr_client.os.writeObject("*** The user " + name + " disconnected ***");
                            curr_client.os.flush();
                        }*/




                    }
                }
            }


            this.is.close();
            this.os.close();
            clientSocket.close();

        } catch (IOException e) {

            //  System.out.println("User Session terminated");

        } catch (ClassNotFoundException e) {

            //  System.out.println("Class Not Found");
        }
    }



    /**** This function transfers message or files to all the client except a particular client connected to the server ***/

    void blockcast(String line, String name) throws IOException, ClassNotFoundException {

        String[] words = line.split(":", 2);

        /* Transferring a File to all the clients except a particular client */


        if (words.length > 1 && words[1] != null) {
            words[1] = words[1].trim();
            if (!words[1].isEmpty()) {
                synchronized (this){
                    for (clientThread curr_client : clients) {
                        if (curr_client != null && curr_client != this && curr_client.clientName != null
                                && !curr_client.clientName.equals("@"+words[0].substring(1))) {
                            curr_client.os.writeObject("<" + name + "> " + words[1]);
                            curr_client.os.flush();


                        }
                    }
                    /* Echo this message to let the user know the blocked message was sent.*/

                    this.os.writeObject(">>Blockcast message sent to everyone except "+words[0].substring(1));
                    this.os.flush();
                    //  System.out.println("Message sent by "+ this.clientName.substring(1) + " to everyone except " + words[0].substring(1));
                }
            }
        }

    }

    /**** This function transfers message or files to all the client connected to the server ***/

    void broadcast(String line, String name, boolean writeHistory) throws IOException, ClassNotFoundException {

        if (writeHistory) {
            Server.messages.add(new Pair<>(name, line));
        }
        /* Transferring a File to all the clients */

        if (line.split("\\s")[0].toLowerCase().equals("sendfile"))
        {

            byte[] file_data = (byte[]) is.readObject();
            synchronized(this){
                for (clientThread curr_client : clients) {
                    if (curr_client != null && curr_client.clientName != null && curr_client.clientName!=this.clientName)
                    {
                        curr_client.os.writeObject("Sending_File:"+line.split("\\s",2)[1].substring(line.split("\\s",2)[1].lastIndexOf(File.separator)+1));
                        curr_client.os.writeObject(file_data);
                        curr_client.os.flush();

                    }
                }

                this.os.writeObject("Broadcast file sent successfully");
                this.os.flush();
                //System.out.println("Broadcast file sent by " + this.clientName.substring(1));
            }
        }

        else
        {
            /* Transferring a message to all the clients */

            synchronized(this){

                for (clientThread curr_client : clients) {

                    if (curr_client != null && curr_client.clientName != null)
                    {

                        curr_client.os.writeObject("" + name + ": " + line);
                        curr_client.os.flush();

                    }
                }

                //  this.os.writeObject("Broadcast message sent successfully.");
                this.os.flush();
                //  System.out.println("Broadcast message sent by " + this.clientName.substring(1));
            }

        }

    }

    /**** This function transfers message or files to a particular client connected to the server ***/

    void unicast(String line, String name) throws IOException, ClassNotFoundException {



        /* Transferring File to a particular client */



        for (clientThread curr_client : clients) {

            if (curr_client != null && curr_client.clientName != null
                    && curr_client.clientName.substring(1).equals(name)) {
                System.out.println("yay");
                curr_client.os.writeObject("<" + name + "> " + line);
                curr_client.os.flush();

                        /*  System.out.println(this.clientName.substring(1) + " transferred a private message to client "+ curr_client.clientName.substring(1));

                            /* Echo this message to let the sender know the private message was sent.

                            this.os.writeObject("Private Message sent to " + curr_client.clientName.substring(1));*/
                this.os.flush();
                break;
            }


        }
    }


}