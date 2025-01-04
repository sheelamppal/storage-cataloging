package com.zirohcompany.storage_cataloging;
/*
 * This GUI Project is To  List the Storage Devices(Drives) both Integrated and External and it Also Contain Function to Navigate Through File System.
 * It Consist of Drive Information, Drive Type, Total space , Free Space and further Navigation.
 * This is Created by Team 1(Mritunjay Singh, Nihal jain,Prafil Mane,Atul Patel ,Mohit Sharma) Under ZIROH GROUP.   
 */

import java.awt.Toolkit;
import java.io.File;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
/**
 * @author Mritunjay Singh
 */
public class Storage_Cataloging extends javax.swing.JFrame {
int internalDevice, currentDrives;
String driveName, conCat1, conCat2, normalFileName,backTemp;
int threadCount, countStart =0,directoryInItCount=0,filesInItCount=0;;
FileSystemView fm;
DefaultTableModel driveTable;
    
    public Storage_Cataloging() {
        initComponents();
        additional();
        internalDevice =initializer();
        waitAndCheck();
        
    }
    public void additional()
    {
        fm = FileSystemView.getFileSystemView();
        this.setTitle("STORAGE CATALOGING");
        jLableNewDevice.setVisible(false);
        jProgressBar1.setVisible(false);
        lDriveName.setVisible(false);
        lDriveInfo.setVisible(false);
    }
    private int initializer()
    {
        int externalDeviceCount = 0;
          File[] root =File.listRoots();
          for(File r : root)
          {
              if("Local Disk".equals(fm.getSystemTypeDescription(r)))
              {
                  externalDeviceCount++;
              }
          }
          return externalDeviceCount;
    }
    private void DriveTableUpdate()
    {
       driveTable = (DefaultTableModel)jTableDevices.getModel();
        File[] drives = File.listRoots();
        
        int deviceCount = 0;
         driveTable.setRowCount(0);
            for(File drive : drives)//{1,2,3,4]
            {
                String driveName = drive+"";
                String typeOfDrive = fm.getSystemTypeDescription(drive);     
                //type of device
                File tempDrive = new File(drive+"");
                long totalSpace = tempDrive.getTotalSpace()/(1024*1024*1024);
                long freeSpace = tempDrive.getFreeSpace()/(1024*1024*1024);
                driveTable.addRow(new Object[]{driveName,typeOfDrive,totalSpace+"  GB",freeSpace+"  GB"});
                deviceCount++;
            }
            llableTotalDevices.setText(File.listRoots().length+"");
    }
    private void waitAndCheck()
    {
        Thread waitForUpdate = new Thread(new Runnable() {
            @Override
            public void run() {
            DriveTableUpdate();
        while(true)
        {
           if(File.listRoots().length>internalDevice)
            {
             DriveTableUpdate();
             //new Device Detected
             jLableNewDevice.setVisible(true);
             Toolkit.getDefaultToolkit().beep();
             llableTotalDevices.setText(File.listRoots().length+"");
             jLableNewDevice.setText("NEW  DEVICE  DETECTED ");
             internalDevice = File.listRoots().length; 
            }else if(File.listRoots().length<internalDevice)
             {
              DriveTableUpdate();
              llableTotalDevices.setText(File.listRoots().length+"");
              jLableNewDevice.setText(null);
              jLableNewDevice.setVisible(false);
              internalDevice=File.listRoots().length;
             }    
        }  
            }
        });
        waitForUpdate.start();
    }
    private void driveTableMouse(String path)     
    {
        driveName = path;
        File temp = new File(driveName);
       long driveTotalSpace= temp.getTotalSpace()/(1024*1024*1024);
       long driveFreeSpace = temp.getFreeSpace()/(1024*1024*1024);
      
        lDriveName.setText(driveName);
       // progressBarActive(8,200);
       progressBarActive(driveFreeSpace,driveTotalSpace);
       lDriveInfo.setVisible(true);
        lDriveInfo.setText(driveFreeSpace+" GB free of "+driveTotalSpace+" GB");
    }
    private void progressBarActive(long free, long max)
    {
        long maxMinusMin= max-free;
        jProgressBar1.setVisible(true);
        jProgressBar1.setMinimum(0);
        jProgressBar1.setMaximum((int)max);
        jProgressBar1.setValue((int)maxMinusMin);
    }
    public class JTreeModel implements TreeModel {

    private File root;
    private Vector listeners = new Vector();

    public JTreeModel(File rootDirectory) {
        root = rootDirectory;
    }

    @Override
    public Object getRoot() {
        return root;
    }

    @Override
    public Object getChild(Object parent, int index) {
        File directory = (File) parent;
        String[] children = directory.list();
 /*       for (int j = 0; j< children.length; j++ ){
            System.out.println(children[j]);
        }       */
        
        return new JTreeModel.TreeFile(directory, children[index]);
    }

    @Override
    public int getChildCount(Object parent) {
        File file = (File) parent;
        if (file.isDirectory()) {
            String[] fileList = file.list();
          
            if (fileList != null) {
                return file.list().length;
            }
        }
        return 0;
    }

    @Override
    public boolean isLeaf(Object node) {
        File file = (File) node;
        return file.isFile();
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        File directory = (File) parent;
        File file = (File) child;
        String[] children = directory.list();
        for (int i = 0; i < children.length; i++) {
            if (file.getName().equals(children[i])) {
                return i;
            }
        }
        return -1;

    }

    @Override
    public void valueForPathChanged(TreePath path, Object value) {
        File oldFile = (File) path.getLastPathComponent();
        String fileParentPath = oldFile.getParent();
        String newFileName = (String) value;
        File targetFile = new File(fileParentPath, newFileName);
        oldFile.renameTo(targetFile);
        File parent = new File(fileParentPath);
        int[] changedChildrenIndices = {getIndexOfChild(parent, targetFile)};
        Object[] changedChildren = {targetFile};
        fireTreeNodesChanged(path.getParentPath(), changedChildrenIndices, changedChildren);

    }

    private void fireTreeNodesChanged(TreePath parentPath, int[] indices, Object[] children) {
        TreeModelEvent event = new TreeModelEvent(this, parentPath, indices, children);
        Iterator iterator = listeners.iterator();
        TreeModelListener listener = null;
        while (iterator.hasNext()) {
            listener = (TreeModelListener) iterator.next();
            listener.treeNodesChanged(event);
        }
    }

    @Override
    public void addTreeModelListener(TreeModelListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeTreeModelListener(TreeModelListener listener) {
        listeners.remove(listener);
    }

    private class TreeFile extends File {

        public TreeFile(File parent, String child) {
            super(parent, child);
        }

        @Override
        public String toString() {
            return getName();
        }
    }
}
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTree1 = new javax.swing.JTree();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTableDevices = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        llableTotalDevices = new javax.swing.JLabel();
        jProgressBar1 = new javax.swing.JProgressBar();
        lDriveName = new javax.swing.JLabel();
        jLableNewDevice = new javax.swing.JLabel();
        lDriveInfo = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(204, 204, 204));

        jPanel1.setBackground(new java.awt.Color(153, 153, 153));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "DRIVE CONTENT", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Nirmala UI", 0, 14))); // NOI18N

        jTree1.setBackground(new java.awt.Color(204, 204, 204));
        jTree1.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 1, 1, 1, new java.awt.Color(102, 102, 102)));
        jTree1.setFont(new java.awt.Font("Sitka Display", 0, 14)); // NOI18N
        jTree1.setForeground(new java.awt.Color(51, 51, 51));
        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
        jTree1.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        jTree1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jTree1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTree1MouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(jTree1);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 451, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane2)
                .addContainerGap())
        );

        jTableDevices.setBackground(new java.awt.Color(204, 204, 204));
        jTableDevices.setBorder(javax.swing.BorderFactory.createCompoundBorder());
        jTableDevices.setFont(new java.awt.Font("Trebuchet MS", 0, 12)); // NOI18N
        jTableDevices.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "NAME", "TYPE", "TOTAL SPACE", "FREE SPACE"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTableDevices.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jTableDevices.setIntercellSpacing(new java.awt.Dimension(0, 0));
        jTableDevices.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTableDevicesMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jTableDevices);
        if (jTableDevices.getColumnModel().getColumnCount() > 0) {
            jTableDevices.getColumnModel().getColumn(1).setResizable(false);
            jTableDevices.getColumnModel().getColumn(2).setResizable(false);
            jTableDevices.getColumnModel().getColumn(3).setResizable(false);
        }

        jLabel1.setFont(new java.awt.Font("Arial", 0, 24)); // NOI18N
        jLabel1.setText("TOTAL DRIVES");
        jLabel1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        llableTotalDevices.setText("/");

        jProgressBar1.setBackground(new java.awt.Color(255, 255, 255));
        jProgressBar1.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jProgressBar1.setBorder(javax.swing.BorderFactory.createCompoundBorder());
        jProgressBar1.setStringPainted(true);

        lDriveName.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lDriveName.setText("/");

        jLableNewDevice.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N

        lDriveInfo.setFont(new java.awt.Font("Nirmala UI", 0, 12)); // NOI18N
        lDriveInfo.setText("free GB");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 517, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(55, 55, 55)
                                .addComponent(llableTotalDevices, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(lDriveName, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLableNewDevice, javax.swing.GroupLayout.PREFERRED_SIZE, 280, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 234, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lDriveInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 196, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(10, 10, 10)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(llableTotalDevices, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(2, 2, 2)))
                .addGap(30, 30, 30)
                .addComponent(jLableNewDevice, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(57, 57, 57)
                .addComponent(lDriveName)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lDriveInfo)
                .addContainerGap(277, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTableDevicesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTableDevicesMouseClicked
lDriveName.setVisible(true);
driveTableMouse(driveTable.getValueAt(jTableDevices.getSelectedRow(),0).toString());
jTree1.setModel(new JTreeModel(new File(driveTable.getValueAt(jTableDevices.getSelectedRow(),0).toString())));

    }//GEN-LAST:event_jTableDevicesMouseClicked

    private void jTree1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTree1MouseClicked







   
    }//GEN-LAST:event_jTree1MouseClicked

    
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Storage_Cataloging.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Storage_Cataloging.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Storage_Cataloging.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Storage_Cataloging.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Storage_Cataloging().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLableNewDevice;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTableDevices;
    private javax.swing.JTree jTree1;
    private javax.swing.JLabel lDriveInfo;
    private javax.swing.JLabel lDriveName;
    private javax.swing.JLabel llableTotalDevices;
    // End of variables declaration//GEN-END:variables
}
