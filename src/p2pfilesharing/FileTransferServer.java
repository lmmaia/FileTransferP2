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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileTransferServer implements Runnable {

    InetAddress bcastAddress;
    int port;
    String filename;

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
        //Initialize Sockets
        try {
            ServerSocket ssock = new ServerSocket(port);

            Socket socket = ssock.accept();

            //The InetAddress specification
            InetAddress IA = bcastAddress;

            //Specify the file
            String folder = "download/";
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
            
            this.filename = P2PFileSharing.downloadingFile;
            File file = new File("shared/" + filename);
            FileInputStream fis = (created) ? new FileInputStream(f.toString() + "/" + filename) : new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);

            //Get socket's output stream
            OutputStream os = socket.getOutputStream();

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
                System.out.print("Sending file ... " + (current * 100) / fileLength + "% complete!");
            }

            os.flush();
            //File transfer done. Close the socket connection!
            socket.close();
            ssock.close();
        } catch (IOException ex) {
            Logger.getLogger(FileTransferServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("File sent succesfully!");
    }
}
