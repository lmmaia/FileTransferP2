/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package p2pfilesharing.UI;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import p2pfilesharing.FileInfo;
import p2pfilesharing.UdpPeerReceive;

/**
 *
 * @author Luís Maia
 */
public class MainFrame extends javax.swing.JFrame {

    private String dwnIp;
    private String dwnFileName;
    private DefaultListModel listModel = new DefaultListModel();

    /**
     * Creates new form MainFrame
     *
     * @param nick
     */
    public MainFrame(String nick) {
        initComponents();
        welcome_txtfield.setText("Welcome " + nick);
    }

    public String getDwnIp() {
        return dwnIp;
    }

    public void setDwnIp(String dwnIp) {
        this.dwnIp = dwnIp;
    }

    public String getDwnFileName() {
        return dwnFileName;
    }

    public void setDwnFileName(String dwnFileName) {
        this.dwnFileName = dwnFileName;
    }

    public void setMessageText(String text) {
        mess_label.setText(text);
    }
    public void addtoLog(String text){
        log_txtarea.append(text);
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        files_list = new javax.swing.JList<>();
        dwnl_bt = new javax.swing.JButton();
        inst_label = new javax.swing.JLabel();
        mess_label = new javax.swing.JLabel();
        welcome_txtfield = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        log_txtarea = new javax.swing.JTextArea();
        log_label = new javax.swing.JLabel();
        list_button = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("P2P FILE SHARING");

        jScrollPane1.setViewportView(files_list);

        dwnl_bt.setText("DOWNLOAD");
        dwnl_bt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dwnl_btActionPerformed(evt);
            }
        });

        inst_label.setText("Select a File to Download");

        mess_label.setText("press to download");

        welcome_txtfield.setText("Welcome ");

        log_txtarea.setColumns(20);
        log_txtarea.setRows(5);
        jScrollPane2.setViewportView(log_txtarea);

        log_label.setText("Log:");

        list_button.setText("LIST IPs");
        list_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                list_buttonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(inst_label)
                    .addComponent(welcome_txtfield))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(log_label)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(dwnl_bt)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(mess_label)
                            .addComponent(list_button))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addComponent(welcome_txtfield))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(log_label)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(inst_label)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(list_button)
                        .addGap(34, 34, 34)
                        .addComponent(dwnl_bt)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(mess_label)))
                .addContainerGap(51, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void dwnl_btActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dwnl_btActionPerformed
        FileInfo f = files_list.getSelectedValue();
        if (f != null) {
            setDwnIp(f.getEndereco_Servidor());
            setDwnFileName(f.getNome_Ficheiro());
            try {
                p2pfilesharing.P2PFileSharing.download();
            } catch (InterruptedException ex) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_dwnl_btActionPerformed

    private void list_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_list_buttonActionPerformed
        try {
            p2pfilesharing.P2PFileSharing.list();
        } catch (InterruptedException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_list_buttonActionPerformed
    public void updateList() {
        ArrayList<FileInfo> fi;
        listModel = new DefaultListModel();
        for (CopyOnWriteArrayList<FileInfo> value : UdpPeerReceive.mapa_Servidor_Ficheiros.values()) {
            for (FileInfo fileInfo : value) {
                listModel.addElement(fileInfo);
            }
        }
        files_list.setModel(listModel);

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton dwnl_bt;
    private javax.swing.JList<FileInfo> files_list;
    private javax.swing.JLabel inst_label;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JButton list_button;
    private javax.swing.JLabel log_label;
    private javax.swing.JTextArea log_txtarea;
    private javax.swing.JLabel mess_label;
    private javax.swing.JLabel welcome_txtfield;
    // End of variables declaration//GEN-END:variables
}
