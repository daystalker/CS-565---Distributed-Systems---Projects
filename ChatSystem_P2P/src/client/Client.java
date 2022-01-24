package client;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import model.Message;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;
import model.NodeInfo;


public class Client {
    
    private String name;
    NodeInfo nodeInfo;

    ObjectInputStream ois;
    ObjectOutputStream oos;

    Socket socket;

    public Client() throws IOException {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter your name: ");
        name = sc.nextLine();

        System.out.println("Write commands..");
        while (true) {

            try {

                String text = sc.nextLine();
                String[] parts = text.split(" ");

                if (parts[0].equals("JOIN")) {
                    //Read from file
                    nodeInfo = new NodeInfo(parts[1], Integer.parseInt(parts[2]), name);
                    socket = new Socket(nodeInfo.getIP(), nodeInfo.getPort());
                    oos = new ObjectOutputStream(socket.getOutputStream());
                    ois = new ObjectInputStream(socket.getInputStream());

                    Message msg = new Message("JOIN", nodeInfo);
                    oos.writeObject(msg);
                    System.out.println("Chat Group Joined!");

                    Thread thread = new Thread(() -> {
                        while (true) {
                            try {
                                if (socket != null) {
                                    Message msg1 = (Message) ois.readObject();
                                    if (msg1.getType().equals("NOTE")) {
                                        System.out.println(msg1.getContent());
                                    } else if (msg1.getType().equals("SHUTDOWN ALL")) {
                                        System.out.println("Server is closed!");
                                        socket.close();
                                        System.exit(0);
                                    }
                                }
                            } catch (IOException | ClassNotFoundException e) {
                                //e.printStackTrace();
                            }
                        }
                    });
                    thread.start();
                }
                else if (parts[0].equals("LEAVE")) {
                    Message msg = new Message("LEAVE", nodeInfo);
                    oos.writeObject(msg);
                    socket.close();
                    socket = null;
                    ois = null;
                    oos = null;

                    System.out.println("You have left chat group!");

                } else if (text.startsWith("SHUTDOWN ALL")) {
                    if (socket != null) {
                        Message msg = new Message("SHUTDOWN ALL", null);
                        oos.writeObject(msg);
                    }
                } else if (parts[0].equals("SHUTDOWN")) {
                    if (socket != null) {
                        Message msg = new Message("LEAVE", nodeInfo);
                        oos.writeObject(msg);
                        socket.close();
                    }
                    System.exit(0);
                }
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }


    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        new Client();
    }

    private NodeInfo readConnectionInfo() {
        try {
            Scanner scanner = new Scanner(new FileInputStream("properties.txt"));
            String[] parts = scanner.nextLine().split(" ");
            String ip = parts[0];
            int port = Integer.parseInt(parts[1]);
            return new NodeInfo(ip, port, name);
        } catch (FileNotFoundException e) {
            System.out.println("File not found!");
        }
        return null;
    }

}
