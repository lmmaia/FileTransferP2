/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package p2pfilesharing;

/**
 *
 * @author Luís Maia
 */
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileTransferServer implements Runnable {

    InetAddress bcastAddress;
    int port;
    String filename;

    /**
     *
     */
    public static final int MAXCLI = 100;
    public static Socket[] cliSock = new Socket[MAXCLI];
    public static DataOutputStream[] sOut = new DataOutputStream[MAXCLI];
    public static Boolean[] inUse = new Boolean[MAXCLI];
    public static Semaphore changeLock = new Semaphore(1);

    static ServerSocket ssock;

    FileTransferServer(InetAddress bcastAddress, int port) {
        this.bcastAddress = bcastAddress;
        this.port = port;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Override
    public void run() {
        int i;
        //Initialize Sockets
        try {
            try {
                ssock = new ServerSocket(port);
            } catch (IOException ex) {
                System.out.println("Local port number not available.");
                System.exit(1);
            }
            for (i = 0; i < MAXCLI; i++) {
                inUse[i] = false;
            }
            while (true) {
                changeLock.acquire();
                for (i = 0; i < MAXCLI; i++) {
                    if (!inUse[i]) {
                        break; // find a free socket
                    }
                }
                changeLock.release();
                try {
                    cliSock[i] = ssock.accept();
                } catch (java.net.SocketException ex) {
                    System.out.println("Local port its closed.");
                    break;
                    // System.exit(1);
                }
                inUse[i] = true;
                //The InetAddress specification
                InetAddress IA = bcastAddress;

                //Specify the file0
                String folder = "shared/";
                File f = new File(folder);
                Boolean created = false;
                if (!f.exists()) {
                    try {
                        created = f.mkdir();
                    } catch (Exception e) {
                        System.out.println("Couldn't create the folder, the file will be saved in the current directory!");
                    }
                } else {
                    created = true;
                }
                DataInputStream sIn;
                byte[] data = new byte[300];
                sIn = new DataInputStream(cliSock[i].getInputStream());
                int nChars = sIn.read();
                sIn.read(data, 0, nChars); // read the line
                filename = new String(data, 0, nChars);
                File file;
                try {
                    file = new File("shared/" + filename);

                    FileInputStream fis = (created) ? new FileInputStream(f.toString() + "/" + filename) : new FileInputStream(file);
                    BufferedInputStream bis = new BufferedInputStream(fis);

                    //Get socket's output stream
                    OutputStream os = cliSock[i].getOutputStream();

                    //Read File Contents into contents array 
                    byte[] contents;
                    long fileLength = file.length();
                    long current = 0;

                    long start = System.nanoTime();
                    while (current != fileLength) {
                        int size = 10000;
                        if (fileLength - current >= size) {
                            current += size;
                        } else {
                            size = (int) (fileLength - current);
                            current = fileLength;
                        }
                        contents = new byte[size];
                        bis.read(contents, 0, size);
                        os.write(contents);
                        P2PFileSharing.frame.addtoLog("Sending file ... " + (current * 100) / fileLength + "% complete!\n");
                        System.out.print("Sending file ... " + (current * 100) / fileLength + "% complete!");
                    }

                    os.flush();

                    P2PFileSharing.frame.addtoLog("File sent succesfully!\n");
                    System.out.println("File sent succesfully!");
                } catch (FileNotFoundException e) {
                    P2PFileSharing.frame.addtoLog(filename+"-File Not Found!\n");
                }
                //File transfer done. Close the socket connection!
                inUse[i] = false;
                cliSock[i].close();
            }

        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(FileTransferServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
