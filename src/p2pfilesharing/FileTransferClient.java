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
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileTransferClient implements Runnable {

    String fileName;
    InetAddress inetAddress;
    int port;

    FileTransferClient(InetAddress peerAddres, int port, String fileName) {
        this.inetAddress = peerAddres;
        this.port = port;
        this.fileName = fileName;
    }

    @Override
    public void run() {
        try {
            //Initialize socket
            Socket socket = null;
            try {
                socket = new Socket(inetAddress, port);
            } catch (IOException ex) {
                System.out.println("Failed to connect.");
                System.exit(1);
            }
            byte[] contents = new byte[10000];
            byte[] nameData;
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            DataOutputStream sOut = new DataOutputStream(socket.getOutputStream());
            nameData = fileName.getBytes();
            sOut.write((byte) fileName.length());
            sOut.write(nameData, 0, (byte) fileName.length());
            //Initialize the FileOutputStream to the output file's full path.
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

            FileOutputStream fos = (created) ? new FileOutputStream(f.toString() + "/" + fileName) : new FileOutputStream(fileName);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            InputStream is = socket.getInputStream();

            //No of bytes read in one read() call
            int bytesRead = 0;

            while ((bytesRead = is.read(contents)) != -1) {
                bos.write(contents, 0, bytesRead);
            }

            bos.flush();
            sOut.close();
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(FileTransferServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("File saved successfully!");
    }
}
