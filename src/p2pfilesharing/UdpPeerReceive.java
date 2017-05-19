/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package p2pfilesharing;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author Luís Maia
 */
public class UdpPeerReceive implements Runnable {

    private static ConcurrentHashMap< String, CopyOnWriteArrayList<FileInfo>> mapa_Servidor_Ficheiros = new ConcurrentHashMap<>();
    static Timer t45 = new Timer();
    private DatagramSocket s;
    
    public UdpPeerReceive(DatagramSocket udp_s) {
        s = udp_s;
    }

    public void run() {
        int i;
        byte[] data = new byte[300];
        String frase;
        DatagramPacket p;
        InetAddress currPeerAddress;

        p = new DatagramPacket(data, data.length);
        t45.schedule(new TimerTask() {
            @Override
            public void run() {
                for (String key : mapa_Servidor_Ficheiros.keySet()) {                    
                    for (FileInfo file : mapa_Servidor_Ficheiros.get(key)) {
                        double lastupdate = (System.nanoTime() - file.getUpdtime()) / 1000000000.0;
                        if (lastupdate > 45) {
                            mapa_Servidor_Ficheiros.get(key).remove(file);
                            System.out.println("removed: "+file.getEndereco_Servidor() + " " + file.getNome_Ficheiro());
                        } else {
                            System.out.println(file.getEndereco_Servidor() + " " + file.getNome_Ficheiro() + " last update:" + lastupdate);
                        }
                    }
                    if(mapa_Servidor_Ficheiros.get(key).isEmpty()){
                        mapa_Servidor_Ficheiros.remove(key);
                        System.out.println("removed: "+key);
                    }
                }
            }
        }, 0, 45000);
        
        while (true) {
            p.setLength(data.length);
            try {
                s.receive(p);
            } catch (IOException ex) {
                return;
            }
            currPeerAddress = p.getAddress();
            switch (data[0]) {
                case 1:
                    try {
                        P2PFileSharing.changeLock.acquire();
                    } catch (InterruptedException ex) {
                        System.out.println("Interrupted");
                    }
                    for (i = 0; i < P2PFileSharing.MAXCLI; i++) {
                        if (P2PFileSharing.peerActive[i]) {
                            if (currPeerAddress.equals(P2PFileSharing.peerAddress[i])) {
                                break;
                            }
                        }
                    }
                    if (i == P2PFileSharing.MAXCLI) // new peer
                    {
                        for (i = 0; i < P2PFileSharing.MAXCLI; i++) {
                            if (!P2PFileSharing.peerActive[i]) {
                                break;
                            }
                        }
                        if (i == P2PFileSharing.MAXCLI) {
                            System.out.println("Sorry, no room for more peers");
                        } else {
                            P2PFileSharing.peerActive[i] = true;
                            P2PFileSharing.peerAddress[i] = currPeerAddress;
                        }
                    }
                    P2PFileSharing.changeLock.release();
                    data[0] = 1;
                    p.setAddress(currPeerAddress); // send back an announcement
                    p.setLength(1);
                    try {
                        s.send(p);
                    } catch (IOException ex) {
                        return;
                    }
                    break;
                case 0:
                    try {
                        P2PFileSharing.changeLock.acquire();
                    } catch (InterruptedException ex) {
                        System.out.println("Interrupted");
                    }
                    for (i = 0; i < P2PFileSharing.MAXCLI; i++) {
                        if (P2PFileSharing.peerActive[i]) {
                            if (currPeerAddress.equals(P2PFileSharing.peerAddress[i])) {
                                break;
                            }
                        }
                    }
                    if (i < P2PFileSharing.MAXCLI) {
                        P2PFileSharing.peerActive[i] = false;
                    }
                    P2PFileSharing.changeLock.release();
                    break;
                default:
                    frase = new String(p.getData(), 0, p.getLength());
                    String[] files = frase.split("\n");
                    for (String file : files) {
                        String[] fileInfo = file.split(",");
                        if (!P2PFileSharing.peerAddress[0].getHostAddress().equals(fileInfo[0])) {
                            if (mapa_Servidor_Ficheiros.containsKey(fileInfo[0])) {
                                FileInfo peerFile = new FileInfo(fileInfo[0], fileInfo[1], true);
                                if (mapa_Servidor_Ficheiros.get(fileInfo[0]).contains(peerFile)) {
                                    int idx = mapa_Servidor_Ficheiros.get(fileInfo[0]).indexOf(peerFile);
                                    mapa_Servidor_Ficheiros.get(fileInfo[0]).get(idx).setUpdtime(System.nanoTime());
                                } else {
                                    mapa_Servidor_Ficheiros.get(fileInfo[0]).add(peerFile);
                                }
                            } else {
                                CopyOnWriteArrayList<FileInfo> peerFiles = new CopyOnWriteArrayList<>();
                                peerFiles.add(new FileInfo(fileInfo[0], fileInfo[1], true));
                                mapa_Servidor_Ficheiros.put(fileInfo[0], peerFiles);
                            }
                        } else {
                            System.out.println("my own list");
                        }
                    }
                    break;
            }
        }
    }
}
