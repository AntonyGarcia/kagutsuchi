package com.wpi.kagutsuchi;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JFrameWindow extends javax.swing.JFrame {

    private double scaleFactor = 0.52083333333;
    private int cellNumber = 12;
    private static JSONObject mainSettingsFile;
    private static JSONObject pathSettingsFile;
    private List<String> imagesList = null;
    private static int fillingMode = 1;
    private static int cellSize = 0;
    private static boolean loadFlag = false;

    public class Imagen extends javax.swing.JPanel {

        public Imagen() {
            this.setSize(1000, 750); //se selecciona el tamaño del panel
        }

        public void paint(Graphics grafico) {
            Dimension height = getSize();
            ImageIcon Img = new ImageIcon(mainSettingsFile.get("images_path") + "/" + jTable1.getValueAt(jTable1.getSelectedRow(), 0));
            grafico.drawImage(Img.getImage(), 0, 0, 1000, 750, null);

            setOpaque(false);
            super.paintComponent(grafico);
        }
    }

    public JFrameWindow() {
        initComponents();
        try {
            loadMainSettings();
            loadImagesTable();

            jComboBox4.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent arg0) {
                    if (loadFlag) {
                        pathSettingsFile.put("black_cell_removal_rate", jComboBox4.getSelectedIndex());
                        saveTextFile(mainSettingsFile.get("images_path") + "\\settings.json", pathSettingsFile.toJSONString());
                        updateDatasetInfomation();
                    }
                }
            });

            jComboBox3.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent arg0) {
                    if (loadFlag) {
                        String str = jComboBox3.getSelectedItem() + "";
                        pathSettingsFile.put("training_set_size", str.substring(0, str.indexOf("-")));
                        pathSettingsFile.put("testing_set_size", str.substring(str.indexOf("-") + 1));
                        saveTextFile(mainSettingsFile.get("images_path") + "\\settings.json", pathSettingsFile.toJSONString());
                        updateDatasetInfomation();
                    }
                }
            });

            ListSelectionModel selectionModel = jTable1.getSelectionModel();

            selectionModel.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    cellSize = (int) (15 * Math.pow(2, jSlider1.getValue()));
                    updateGUI(cellSize);
                    saveTextFile(mainSettingsFile.get("images_path") + "\\settings.json", pathSettingsFile.toJSONString());
                    if (loadFlag) {
                        updateDatasetInfomation();
                    }
                }
            });
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(JFrameWindow.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(JFrameWindow.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(JFrameWindow.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(JFrameWindow.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(JFrameWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (jTable1.getRowCount() >= 0) {
            if (jTable1.getRowCount() > 1) {
                jTable1.setRowSelectionInterval(1, 1);
            }
            jTable1.setRowSelectionInterval(0, 0);
        }

        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                saveTextFile(mainSettingsFile.get("images_path") + "\\settings.json", pathSettingsFile.toJSONString());
            }
        });
        jToggleButton1.setSelected(true);

        buildDatasetTable();
        updateDatasetInfomation();

        jScrollPane3.getVerticalScrollBar().setUnitIncrement(10);

        loadFlag = true;

    }

    public void loadMainSettings() throws ParseException {

        try {
            JSONParser parser = new JSONParser();
            mainSettingsFile = (JSONObject) parser.parse(getSettingFile("settings.json"));
            pathSettingsFile = (JSONObject) parser.parse(getSettingFile(mainSettingsFile.get("images_path") + "\\settings.json"));
            sliderUpdate(Integer.parseInt("" + pathSettingsFile.get("grid_resolution")));

            try {
                jSlider1.setValue(Integer.parseInt(pathSettingsFile.get("grid_resolution") + ""));
                jSlider2.setValue(Integer.parseInt(pathSettingsFile.get("black_pixel_color_threshold") + ""));
                jSlider3.setValue(Integer.parseInt(pathSettingsFile.get("black_pixel_count_limit") + ""));
                jComboBox4.setSelectedIndex(Integer.parseInt(pathSettingsFile.get("black_cell_removal_rate") + ""));
                jComboBox3.setSelectedItem(pathSettingsFile.get("training_set_size") + "-" + pathSettingsFile.get("testing_set_size"));
            } catch (Exception e) {
                jSlider1.setValue(0);
                jSlider2.setValue(0);
                jSlider3.setValue(0);
            }

            if (String.valueOf(pathSettingsFile.get("frame_rotation")).equals("1")) {
                jCheckBox1.setSelected(true);
            }

            if (String.valueOf(pathSettingsFile.get("frame_flip")).equals("1")) {
                jCheckBox3.setSelected(true);
            }
            if (String.valueOf(pathSettingsFile.get("frame_flip_rotation")).equals("1")) {
                jCheckBox4.setSelected(true);
            }
            if (String.valueOf(pathSettingsFile.get("remove_surplus")).equals("1")) {
                jCheckBox5.setSelected(true);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            Logger.getLogger(JFrameWindow.class.getName()).log(Level.SEVERE, null, e);
            sliderUpdate(5);
        }
        jTable1.grabFocus();

    }

    public void loadImagesTable() {
        imagesList = new ArrayList<>();
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0);
        try ( Stream<Path> paths = Files.walk(Paths.get("" + mainSettingsFile.get("images_path")))) {
            paths.filter(Files::isRegularFile).forEach(a -> {
                String str = String.valueOf(a).replace(mainSettingsFile.get("images_path") + "\\", "");
                if (str.contains(".jpg")) {
                    model.addRow(new Object[]{str});
                    imagesList.add(str);
                }
            });
        } catch (IOException ex) {
            Logger.getLogger(JFrameWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
        jTable1.setRowSelectionInterval(0, 0);

        /*  JSONArray positiveImageArray = new JSONArray();
        JSONObject obj = new JSONObject();
        for (int i = 0; i < imagesList.size(); i++) {
            obj.put(imagesList.get(i), new JSONArray());
        }
        positiveImageArray.add(obj);
        pathSettingsFile.put("positive_cells", positiveImageArray);
        pathSettingsFile.put("black_cells", positiveImageArray);*/
        saveTextFile(mainSettingsFile.get("images_path") + "\\settings.json", pathSettingsFile.toJSONString());
    }

    public void saveTextFile(String path, String content) {
        try {
            Files.write(Paths.get(path), content.getBytes());
        } catch (IOException ex) {
            Logger.getLogger(JFrameWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getSettingFile(String path) {
        String output = "";
        BufferedReader br = null;
        try {
            File file = new File(path);
            br = new BufferedReader(new FileReader(file));
            String st;
            while ((st = br.readLine()) != null) {
                output += st;
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(JFrameWindow.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(JFrameWindow.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                br.close();
            } catch (IOException ex) {
                Logger.getLogger(JFrameWindow.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return output;
    }

    public void updateGUI(int resolution) {

        jPanel1.removeAll();
        Imagen Imagen = new Imagen();
        int loopsX = 1920 / resolution;
        int loopsY = 1440 / resolution;
        cellNumber = 0;
        for (int j = 0; j < loopsY; j++) {
            for (int i = 0; i < loopsX; i++) {
                CellPanel cell = new CellPanel(jTable1.getValueAt(jTable1.getSelectedRow(), 0) + "", cellNumber, pathSettingsFile, jCheckBox2.isSelected());
                cell.setBounds(((int) Math.round(resolution * scaleFactor)) * i, ((int) Math.round(resolution * scaleFactor)) * j, (int) Math.round(resolution * scaleFactor), (int) Math.round(resolution * scaleFactor));
                jPanel1.add(cell);
                cellNumber++;
            }
        }
        jPanel1.add(Imagen);
        jPanel1.repaint();
    }

    public void buildDatasetTable() {
        DefaultTableModel model = (DefaultTableModel) jTable2.getModel();
        for (int i = 0; i < 16; i++) {
            model.addRow(new Object[]{"", ""});
        }
        model.setValueAt("Images count", 0, 0);
        model.setValueAt("Images size", 1, 0);
        model.setValueAt("Grid resolution", 2, 0);
        model.setValueAt("Pixels per cell", 3, 0);
        model.setValueAt("Cells per image", 4, 0);
        model.setValueAt("Selected positive cells", 5, 0);
        model.setValueAt("Selected negative cells", 6, 0);
        model.setValueAt("Black cells", 7, 0);
        model.setValueAt("Generated positive cells", 8, 0);
        model.setValueAt("Black cells to remove", 9, 0);
        model.setValueAt("Negative cells to remove", 10, 0);
        model.setValueAt("Positive cells to export", 11, 0);
        model.setValueAt("Negative cells to export", 12, 0);
        model.setValueAt("Cells to export", 13, 0);

        model.setValueAt("Train dataset", 14, 0);
        model.setValueAt("Test dataset", 15, 0);
    }

    public void updateDatasetInfomation() {
        DecimalFormat f = new DecimalFormat("#.0");
        ImageIcon Img = new ImageIcon(mainSettingsFile.get("images_path") + "/" + jTable1.getValueAt(jTable1.getSelectedRow(), 0));

        int w = Img.getIconWidth();
        int h = Img.getIconHeight();
        int cellsPerImage = (int) ((w / cellSize) * (h / cellSize));
        int totalCells = cellsPerImage * jTable1.getRowCount();
        int positiveCells = 0;
        int negativeCells = 0;
        int blackCells = 0;
        int genPostCells = 0;
        int cellsToRemove = 0;

        for (int i = 0; i < imagesList.size(); i++) {
            JSONArray gridArray;
            try {
                gridArray = (JSONArray) ((JSONObject) ((JSONArray) pathSettingsFile.get("positive_cells")).get(0)).get(imagesList.get(i));
                if (gridArray.size() == 0) {
                    throw new Exception();
                }
            } catch (Exception e) {
                gridArray = new JSONArray();
            }

            positiveCells = positiveCells + gridArray.size();
        }
        for (int i = 0; i < imagesList.size(); i++) {

            JSONArray gridArray;
            try {
                gridArray = (JSONArray) ((JSONObject) ((JSONArray) pathSettingsFile.get("black_cells")).get(0)).get(imagesList.get(i));
                if (gridArray.size() == 0) {
                    throw new Exception();
                }
            } catch (Exception e) {
                gridArray = new JSONArray();
            }

            blackCells = (blackCells + gridArray.size());
        }

        negativeCells = totalCells - positiveCells;

        double posPerc = (positiveCells * Math.pow(totalCells, -1)) * 100;
        double negPerc = (negativeCells * Math.pow(totalCells, -1)) * 100;
        double blackPerc = (blackCells * Math.pow(totalCells, -1)) * 100;

        jTable2.setValueAt(jTable1.getRowCount(), 0, 1);
        jTable2.setValueAt(w + " x " + h + " px", 1, 1);
        jTable2.setValueAt(cellSize + " px", 2, 1);
        jTable2.setValueAt(cellSize * cellSize + " px", 3, 1);
        jTable2.setValueAt(cellsPerImage, 4, 1);
        jTable2.setValueAt(positiveCells + " (" + f.format(posPerc) + " %)", 5, 1);
        jTable2.setValueAt(negativeCells + " (" + f.format(negPerc) + " %)", 6, 1);
        jTable2.setValueAt(blackCells + " (" + f.format(blackPerc) + " %)", 7, 1);

        if (jCheckBox1.isSelected()) {
            genPostCells = genPostCells + (positiveCells * 3);
        }
        if (jCheckBox3.isSelected()) {
            genPostCells = genPostCells + positiveCells;
        }
        if (jCheckBox4.isSelected()) {
            genPostCells = genPostCells + (positiveCells * 3);
        }

        double genPerc = (genPostCells * Math.pow(totalCells, -1)) * 100;
        jTable2.setValueAt(genPostCells + " (" + f.format(genPerc) + " %)", 8, 1);
        positiveCells = positiveCells + genPostCells;

        totalCells = positiveCells + negativeCells - blackCells;

        int blackCellsToRemove = 0;
        blackCellsToRemove = (int) (blackCells * (Integer.parseInt(String.valueOf(jComboBox4.getSelectedItem()).replace(" %", "")) * 0.01));

        double blackPercToRemove = (blackCellsToRemove * Math.pow(totalCells, -1)) * 100;

        jTable2.setValueAt(blackCellsToRemove + " (" + f.format(blackPercToRemove) + " %)", 9, 1);
        negativeCells = negativeCells - blackCellsToRemove;
        int negativeCellsToRemove = 0;

        negativeCellsToRemove = negativeCells - positiveCells;

        double negativeCellsToRemovePerc = (negativeCellsToRemove * Math.pow(totalCells, -1)) * 100;
        jTable2.setValueAt(negativeCellsToRemove + " (" + f.format(negativeCellsToRemovePerc) + " %)", 10, 1);

        negativeCells = negativeCells - negativeCellsToRemove;

        if (negativeCells <= positiveCells) {
            positiveCells = negativeCells;
        }
        totalCells = positiveCells + negativeCells;

        posPerc = (positiveCells * Math.pow(totalCells, -1)) * 100;
        negPerc = (negativeCells * Math.pow(totalCells, -1)) * 100;

        jTable2.setValueAt(positiveCells + " (" + f.format(posPerc) + " %)", 11, 1);
        jTable2.setValueAt(negativeCells + " (" + f.format(negPerc) + " %)", 12, 1);

        int cellsToExport = positiveCells + negativeCells;
        jTable2.setValueAt(cellsToExport, 13, 1);

        int trainSet = 0;
        int testSet = 0;
        try {
            trainSet = (int) (cellsToExport * Integer.parseInt(pathSettingsFile.get("training_set_size") + "") * 0.01);
            testSet = (int) (cellsToExport * Integer.parseInt(pathSettingsFile.get("testing_set_size") + "") * 0.01);
        } catch (Exception e) {
        }

        jTable2.setValueAt(trainSet, 14, 1);
        jTable2.setValueAt(testSet, 15, 1);

    }

    public void sliderUpdate(int value) {
        cellSize = (int) (15 * Math.pow(2, value));
        /* jLabel1.setText("Grid resolution: " + cellSize + " px");
        jLabel2.setText("Number of cells: " + cellNumber);
        jLabel3.setText("Pixels per cell: " + ((int) Math.pow(cellSize, 2)) + " px");*/

        pathSettingsFile.put("grid_resolution", value);
        pathSettingsFile.put("cell_size", cellSize);
        pathSettingsFile.put("cell_pixels", ((int) Math.pow(cellSize, 2)));
        pathSettingsFile.put("total_cells", cellNumber);
        pathSettingsFile.put("x_cells", (int) (1920 / cellSize));
        pathSettingsFile.put("y_cells", (int) (1440 / cellSize));

        saveTextFile(mainSettingsFile.get("images_path") + "\\settings.json", pathSettingsFile.toJSONString());
        if (imagesList != null) {
            updateGUI(cellSize);
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton2 = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        jSlider1 = new javax.swing.JSlider();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jScrollPane3 = new javax.swing.JScrollPane();
        jPanel10 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jToggleButton1 = new javax.swing.JToggleButton();
        jToggleButton2 = new javax.swing.JToggleButton();
        jToggleButton3 = new javax.swing.JToggleButton();
        jToggleButton4 = new javax.swing.JToggleButton();
        jToggleButton5 = new javax.swing.JToggleButton();
        jToggleButton6 = new javax.swing.JToggleButton();
        jPanel5 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox<>();
        jComboBox2 = new javax.swing.JComboBox<>();
        jLabel7 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jCheckBox2 = new javax.swing.JCheckBox();
        jSlider2 = new javax.swing.JSlider();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jSlider3 = new javax.swing.JSlider();
        jPanel9 = new javax.swing.JPanel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jCheckBox3 = new javax.swing.JCheckBox();
        jCheckBox4 = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();
        jComboBox3 = new javax.swing.JComboBox<>();
        jLabel8 = new javax.swing.JLabel();
        jComboBox4 = new javax.swing.JComboBox<>();
        jCheckBox5 = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(1375, 810));
        setResizable(false);
        setSize(new java.awt.Dimension(1330, 800));

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel1.setPreferredSize(new java.awt.Dimension(1000, 750));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 996, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel2.setPreferredSize(new java.awt.Dimension(300, 780));

        jButton1.setFont(new java.awt.Font("Abel", 1, 16)); // NOI18N
        jButton1.setText("Choose images path");
        jButton1.setPreferredSize(new java.awt.Dimension(140, 30));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jTable1.setFont(new java.awt.Font("Abel", 0, 14)); // NOI18N
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Images in path"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTable1KeyPressed(evt);
            }
        });
        jScrollPane1.setViewportView(jTable1);
        if (jTable1.getColumnModel().getColumnCount() > 0) {
            jTable1.getColumnModel().getColumn(0).setResizable(false);
        }

        jButton2.setFont(new java.awt.Font("Abel", 1, 16)); // NOI18N
        jButton2.setText("Generate datasets");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), null, null, null, new java.awt.Font("Abel", 0, 14))); // NOI18N

        jSlider1.setMaximum(5);
        jSlider1.setMinimum(1);
        jSlider1.setExtent(50);
        jSlider1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlider1StateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSlider1, javax.swing.GroupLayout.DEFAULT_SIZE, 295, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addComponent(jSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5))
        );

        jTable2.setFont(new java.awt.Font("Abel", 0, 14)); // NOI18N
        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Parameter", "Value"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(jTable2);
        if (jTable2.getColumnModel().getColumnCount() > 0) {
            jTable2.getColumnModel().getColumn(0).setResizable(false);
            jTable2.getColumnModel().getColumn(0).setPreferredWidth(140);
            jTable2.getColumnModel().getColumn(1).setResizable(false);
            jTable2.getColumnModel().getColumn(1).setPreferredWidth(110);
        }

        jPanel10.setPreferredSize(new java.awt.Dimension(180, 600));

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), null, null, null, new java.awt.Font("Abel", 0, 14))); // NOI18N
        jPanel3.setMaximumSize(new java.awt.Dimension(220, 63));
        jPanel3.setPreferredSize(new java.awt.Dimension(260, 63));

        jToggleButton1.setIcon(new javax.swing.ImageIcon("C:\\Users\\Antony Garcia\\Desktop\\wpi\\kagutsuchi\\java\\kagutsuchi\\src\\main\\java\\ui_images\\1.jpg")); // NOI18N
        jToggleButton1.setMaximumSize(new java.awt.Dimension(32, 31));
        jToggleButton1.setMinimumSize(new java.awt.Dimension(32, 31));
        jToggleButton1.setPreferredSize(new java.awt.Dimension(32, 31));
        jToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton1ActionPerformed(evt);
            }
        });

        jToggleButton2.setIcon(new javax.swing.ImageIcon("C:\\Users\\Antony Garcia\\Desktop\\wpi\\kagutsuchi\\java\\kagutsuchi\\src\\main\\java\\ui_images\\2.jpg")); // NOI18N
        jToggleButton2.setMaximumSize(new java.awt.Dimension(32, 31));
        jToggleButton2.setMinimumSize(new java.awt.Dimension(32, 31));
        jToggleButton2.setPreferredSize(new java.awt.Dimension(32, 31));
        jToggleButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton2ActionPerformed(evt);
            }
        });

        jToggleButton3.setIcon(new javax.swing.ImageIcon("C:\\Users\\Antony Garcia\\Desktop\\wpi\\kagutsuchi\\java\\kagutsuchi\\src\\main\\java\\ui_images\\3.jpg")); // NOI18N
        jToggleButton3.setMaximumSize(new java.awt.Dimension(32, 31));
        jToggleButton3.setMinimumSize(new java.awt.Dimension(32, 31));
        jToggleButton3.setPreferredSize(new java.awt.Dimension(32, 31));
        jToggleButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton3ActionPerformed(evt);
            }
        });

        jToggleButton4.setIcon(new javax.swing.ImageIcon("C:\\Users\\Antony Garcia\\Desktop\\wpi\\kagutsuchi\\java\\kagutsuchi\\src\\main\\java\\ui_images\\4.jpg")); // NOI18N
        jToggleButton4.setMaximumSize(new java.awt.Dimension(32, 31));
        jToggleButton4.setMinimumSize(new java.awt.Dimension(32, 31));
        jToggleButton4.setPreferredSize(new java.awt.Dimension(32, 31));
        jToggleButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton4ActionPerformed(evt);
            }
        });

        jToggleButton5.setIcon(new javax.swing.ImageIcon("C:\\Users\\Antony Garcia\\Desktop\\wpi\\kagutsuchi\\java\\kagutsuchi\\src\\main\\java\\ui_images\\5.jpg")); // NOI18N
        jToggleButton5.setMaximumSize(new java.awt.Dimension(32, 31));
        jToggleButton5.setMinimumSize(new java.awt.Dimension(32, 31));
        jToggleButton5.setPreferredSize(new java.awt.Dimension(32, 31));
        jToggleButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton5ActionPerformed(evt);
            }
        });

        jToggleButton6.setBackground(new java.awt.Color(255, 255, 255));
        jToggleButton6.setFont(new java.awt.Font("Arial", 1, 22)); // NOI18N
        jToggleButton6.setForeground(new java.awt.Color(0, 0, 0));
        jToggleButton6.setIcon(new javax.swing.ImageIcon("C:\\Users\\Antony Garcia\\Desktop\\wpi\\kagutsuchi\\java\\kagutsuchi\\src\\main\\java\\ui_images\\6.jpg")); // NOI18N
        jToggleButton6.setMaximumSize(new java.awt.Dimension(35, 31));
        jToggleButton6.setMinimumSize(new java.awt.Dimension(35, 31));
        jToggleButton6.setPreferredSize(new java.awt.Dimension(35, 31));
        jToggleButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton6ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jToggleButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToggleButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToggleButton3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToggleButton4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToggleButton5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToggleButton6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jToggleButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 5, Short.MAX_VALUE))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), null, null, null, new java.awt.Font("Abel", 0, 14))); // NOI18N
        jPanel5.setPreferredSize(new java.awt.Dimension(220, 136));

        jLabel4.setFont(new java.awt.Font("Abel", 0, 14)); // NOI18N
        jLabel4.setText("Final width and height: ");
        jLabel4.setPreferredSize(new java.awt.Dimension(140, 30));

        jTextField1.setFont(new java.awt.Font("Abel", 0, 14)); // NOI18N
        jTextField1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField1.setText("256");
        jTextField1.setMinimumSize(new java.awt.Dimension(60, 30));
        jTextField1.setPreferredSize(new java.awt.Dimension(60, 30));

        jLabel5.setFont(new java.awt.Font("Abel", 0, 14)); // NOI18N
        jLabel5.setText(" px");
        jLabel5.setPreferredSize(new java.awt.Dimension(15, 30));

        jLabel6.setFont(new java.awt.Font("Abel", 0, 14)); // NOI18N
        jLabel6.setText("Filling color: ");

        jComboBox1.setFont(new java.awt.Font("Abel", 0, 14)); // NOI18N
        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Black", "White" }));

        jComboBox2.setFont(new java.awt.Font("Abel", 0, 14)); // NOI18N
        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Red, Green, Blue", "Grayscale", " ", " " }));
        jComboBox2.setSelectedItem("Grayscale");

        jLabel7.setFont(new java.awt.Font("Abel", 0, 14)); // NOI18N
        jLabel7.setText("Color scale:");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(2, 2, 2)
                        .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBox1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBox2, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(5, 5, 5))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), null, null, null, new java.awt.Font("Abel", 0, 14))); // NOI18N
        jPanel8.setFont(new java.awt.Font("Abel", 0, 14)); // NOI18N

        jCheckBox2.setFont(new java.awt.Font("Abel", 0, 14)); // NOI18N
        jCheckBox2.setSelected(true);
        jCheckBox2.setText("Enable");
        jCheckBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox2ActionPerformed(evt);
            }
        });

        jSlider2.setMaximum(25);
        jSlider2.setValue(10);
        jSlider2.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlider2StateChanged(evt);
            }
        });
        jSlider2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jSlider2MouseReleased(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Abel", 0, 14)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Black pixel threshold: 10");

        jLabel2.setFont(new java.awt.Font("Abel", 0, 14)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Black cell pixel count:  90%");

        jSlider3.setMinimum(80);
        jSlider3.setValue(90);
        jSlider3.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlider3StateChanged(evt);
            }
        });
        jSlider3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jSlider3MouseReleased(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addComponent(jCheckBox2)
                .addGap(5, 5, 5)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSlider2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSlider3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(2, 2, 2))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jCheckBox2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addComponent(jLabel1)
                .addGap(4, 4, 4)
                .addComponent(jSlider2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSlider3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), null, null, null, new java.awt.Font("Abel", 0, 14))); // NOI18N
        jPanel9.setFont(new java.awt.Font("Abel", 0, 14)); // NOI18N

        jCheckBox1.setFont(new java.awt.Font("Abel", 0, 14)); // NOI18N
        jCheckBox1.setText("Positive frames rotation (90º, 180º, 270º)");
        jCheckBox1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBox1StateChanged(evt);
            }
        });

        jCheckBox3.setFont(new java.awt.Font("Abel", 0, 14)); // NOI18N
        jCheckBox3.setText("Positive frames horizontal flip (mirror)");
        jCheckBox3.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBox3StateChanged(evt);
            }
        });

        jCheckBox4.setFont(new java.awt.Font("Abel", 0, 14)); // NOI18N
        jCheckBox4.setText("Flipped frames rotation (90º, 180º, 270º)");
        jCheckBox4.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBox4StateChanged(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Abel", 0, 14)); // NOI18N
        jLabel3.setText("  Black cells removal rate:  ");

        jComboBox3.setFont(new java.awt.Font("Abel", 0, 14)); // NOI18N
        jComboBox3.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "50-50", "60-40", "70-30", "80-20", "90-10" }));
        jComboBox3.setSelectedItem("80-20");

        jLabel8.setFont(new java.awt.Font("Abel", 0, 14)); // NOI18N
        jLabel8.setText("  Training-testing datasets ratio: ");

        jComboBox4.setFont(new java.awt.Font("Abel", 0, 14)); // NOI18N
        jComboBox4.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "0 %", "10 %", "20 %", "30 %", "40 %", "50 %", "60 %", "70 %", "80 %", "90 %", "100 %" }));
        jComboBox4.setSelectedItem("50 %");

        jCheckBox5.setFont(new java.awt.Font("Abel", 0, 14)); // NOI18N
        jCheckBox5.setText("Remove surplus of negative frames");
        jCheckBox5.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBox5StateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBox1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jCheckBox3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jCheckBox4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBox3, 0, 1, Short.MAX_VALUE))
                    .addComponent(jCheckBox5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addComponent(jCheckBox1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox5)
                .addContainerGap(111, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE)
                        .addGap(6, 6, 6))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel10Layout.createSequentialGroup()
                        .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel10Layout.createSequentialGroup()
                        .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jScrollPane3.setViewportView(jPanel10);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jButton1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(5, 5, 5))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                        .addContainerGap())))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(jScrollPane3)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 662, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 333, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 1154, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 1154, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jSlider1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlider1StateChanged
        int value = jSlider1.getValue();
        sliderUpdate(value);
    }//GEN-LAST:event_jSlider1StateChanged

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File("" + mainSettingsFile.get("images_path")));
        chooser.setDialogTitle("Select the folder with images to be processed");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            mainSettingsFile.put("images_path", chooser.getSelectedFile() + "");
            saveTextFile("settings.json", mainSettingsFile.toJSONString());
        }

    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        int value = jSlider1.getValue();
        sliderUpdate(value);

        int finalImageSize = Integer.parseInt(jTextField1.getText());
        try {
            if (finalImageSize < cellSize) {
                throw new Exception("Final width and height can't be lower than grid cell size");
            }
            pathSettingsFile.put("filling_color", jComboBox1.getSelectedIndex() + "");
            pathSettingsFile.put("final_image_size", finalImageSize);
            pathSettingsFile.put("filling_mode", fillingMode);
            pathSettingsFile.put("filling_gap", (int) (Integer.parseInt(jTextField1.getText()) - cellSize));
            pathSettingsFile.put("output_color_scale", jComboBox2.getSelectedIndex() + "");

            pathSettingsFile.put("positive_cells_to_export", (jTable2.getValueAt(11, 1) + "").substring(0, (jTable2.getValueAt(11, 1) + "").indexOf(" ")));
            pathSettingsFile.put("negative_cells_to_export", (jTable2.getValueAt(12, 1) + "").substring(0, (jTable2.getValueAt(12, 1) + "").indexOf(" ")));

            pathSettingsFile.put("train_dataset_size", jTable2.getValueAt(14, 1) + "");
            pathSettingsFile.put("test_dataset_size", jTable2.getValueAt(15, 1) + "");

            new ImageHandler(mainSettingsFile, pathSettingsFile, imagesList).test();
            saveTextFile(mainSettingsFile.get("images_path") + "\\settings.json", pathSettingsFile.toJSONString());

        } catch (Exception e) {
            Logger.getLogger(JFrameWindow.class.getName()).log(Level.SEVERE, null, e);
        }

        // System.out.println(jComboBox1.getSelectedIndex()+"");
        // saveTextFile(mainSettingsFile.get("images_path") + "\\settings.json", pathSettingsFile.toJSONString());
        /* try {
        
        } catch (IOException ex) {
            Logger.getLogger(JFrameWindow.class.getName()).log(Level.SEVERE, null, ex);
        }*/

    }//GEN-LAST:event_jButton2ActionPerformed

    private void jTable1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTable1KeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_DELETE) {
            JOptionPane.showMessageDialog(null, "outside");
        }
    }//GEN-LAST:event_jTable1KeyPressed

    private void jToggleButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton6ActionPerformed
        jToggleButton1.setSelected(false);
        jToggleButton2.setSelected(false);
        jToggleButton3.setSelected(false);
        jToggleButton4.setSelected(false);
        jToggleButton5.setSelected(false);
        jToggleButton6.setSelected(true);
        fillingMode = 6;
    }//GEN-LAST:event_jToggleButton6ActionPerformed

    private void jToggleButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton5ActionPerformed
        jToggleButton1.setSelected(false);
        jToggleButton2.setSelected(false);
        jToggleButton3.setSelected(false);
        jToggleButton4.setSelected(false);
        jToggleButton5.setSelected(true);
        jToggleButton6.setSelected(false);
        fillingMode = 5;
    }//GEN-LAST:event_jToggleButton5ActionPerformed

    private void jToggleButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton4ActionPerformed
        jToggleButton1.setSelected(false);
        jToggleButton2.setSelected(false);
        jToggleButton3.setSelected(false);
        jToggleButton4.setSelected(true);
        jToggleButton5.setSelected(false);
        jToggleButton6.setSelected(false);
        fillingMode = 4;
    }//GEN-LAST:event_jToggleButton4ActionPerformed

    private void jToggleButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton3ActionPerformed
        jToggleButton1.setSelected(false);
        jToggleButton2.setSelected(false);
        jToggleButton3.setSelected(true);
        jToggleButton4.setSelected(false);
        jToggleButton5.setSelected(false);
        jToggleButton6.setSelected(false);
        fillingMode = 3;
    }//GEN-LAST:event_jToggleButton3ActionPerformed

    private void jToggleButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton2ActionPerformed
        jToggleButton1.setSelected(false);
        jToggleButton2.setSelected(true);
        jToggleButton3.setSelected(false);
        jToggleButton4.setSelected(false);
        jToggleButton5.setSelected(false);
        jToggleButton6.setSelected(false);
        fillingMode = 2;
    }//GEN-LAST:event_jToggleButton2ActionPerformed

    private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton1ActionPerformed
        jToggleButton1.setSelected(true);
        jToggleButton2.setSelected(false);
        jToggleButton3.setSelected(false);
        jToggleButton4.setSelected(false);
        jToggleButton5.setSelected(false);
        jToggleButton6.setSelected(false);
        fillingMode = 1;
    }//GEN-LAST:event_jToggleButton1ActionPerformed

    private void jCheckBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox2ActionPerformed
        Component[] cells = jPanel1.getComponents();
        if (jCheckBox2.isSelected()) {
            jSlider2.setEnabled(true);

            for (int i = 0; i < cells.length - 1; i++) {
                ((CellPanel) cells[i]).paintCells(true);
            }
        } else {
            jSlider2.setEnabled(false);
            for (int i = 0; i < cells.length - 1; i++) {
                ((CellPanel) cells[i]).paintCells(false);
            }
        }
    }//GEN-LAST:event_jCheckBox2ActionPerformed

    private void jSlider2StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlider2StateChanged

        jLabel1.setText("Black pixel threshold: " + jSlider2.getValue());

    }//GEN-LAST:event_jSlider2StateChanged

    private void jSlider2MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jSlider2MouseReleased
        try {
            pathSettingsFile.put("black_pixel_color_threshold", jSlider2.getValue() + "");
            pathSettingsFile.put("black_pixel_count_limit", jSlider3.getValue() + "");
            Component[] cells = jPanel1.getComponents();
            new ImageHandler(mainSettingsFile, pathSettingsFile, imagesList).deadCellsSearch();
            Thread.sleep(250);

            for (int i = 0; i < cells.length - 1; i++) {
                ((CellPanel) cells[i]).paintCells(true);
            }
            saveTextFile(mainSettingsFile.get("images_path") + "\\settings.json", pathSettingsFile.toJSONString());
            updateDatasetInfomation();
        } catch (InterruptedException ex) {
            Logger.getLogger(JFrameWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jSlider2MouseReleased

    private void jSlider3StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlider3StateChanged
        jLabel2.setText("Black cell pixel count: " + jSlider3.getValue() + "%");
    }//GEN-LAST:event_jSlider3StateChanged

    private void jSlider3MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jSlider3MouseReleased
        try {
            pathSettingsFile.put("black_pixel_color_threshold", jSlider2.getValue() + "");
            pathSettingsFile.put("black_pixel_count_limit", jSlider3.getValue() + "");
            Component[] cells = jPanel1.getComponents();
            new ImageHandler(mainSettingsFile, pathSettingsFile, imagesList).deadCellsSearch();
            Thread.sleep(250);
            for (int i = 0; i < cells.length - 1; i++) {
                ((CellPanel) cells[i]).paintCells(true);
            }
            saveTextFile(mainSettingsFile.get("images_path") + "\\settings.json", pathSettingsFile.toJSONString());
            updateDatasetInfomation();
        } catch (InterruptedException ex) {
            Logger.getLogger(JFrameWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jSlider3MouseReleased

    private void jCheckBox1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jCheckBox1StateChanged
        if (loadFlag) {
            if (jCheckBox1.isSelected()) {
                pathSettingsFile.put("frame_rotation", "1");
            } else {
                pathSettingsFile.put("frame_rotation", "0");
            }
            updateDatasetInfomation();
            saveTextFile(mainSettingsFile.get("images_path") + "\\settings.json", pathSettingsFile.toJSONString());
        }
    }//GEN-LAST:event_jCheckBox1StateChanged

    private void jCheckBox3StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jCheckBox3StateChanged
        if (loadFlag) {
            if (jCheckBox3.isSelected()) {
                pathSettingsFile.put("frame_flip", "1");
            } else {
                pathSettingsFile.put("frame_flip", "0");
            }

            updateDatasetInfomation();
            saveTextFile(mainSettingsFile.get("images_path") + "\\settings.json", pathSettingsFile.toJSONString());
        }

    }//GEN-LAST:event_jCheckBox3StateChanged

    private void jCheckBox4StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jCheckBox4StateChanged
        if (loadFlag) {
            if (jCheckBox4.isSelected()) {
                pathSettingsFile.put("frame_flip_rotation", "1");
            } else {
                pathSettingsFile.put("frame_flip_rotation", "0");
            }

            updateDatasetInfomation();
            saveTextFile(mainSettingsFile.get("images_path") + "\\settings.json", pathSettingsFile.toJSONString());
        }
    }//GEN-LAST:event_jCheckBox4StateChanged

    private void jCheckBox5StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jCheckBox5StateChanged
        if (loadFlag) {
            if (jCheckBox5.isSelected()) {
                pathSettingsFile.put("remove_surplus", "1");
            } else {
                pathSettingsFile.put("remove_surplus", "0");
            }

            updateDatasetInfomation();
            saveTextFile(mainSettingsFile.get("images_path") + "\\settings.json", pathSettingsFile.toJSONString());
        }
    }//GEN-LAST:event_jCheckBox5StateChanged
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(JFrameWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(JFrameWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(JFrameWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(JFrameWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new JFrameWindow().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JCheckBox jCheckBox4;
    private javax.swing.JCheckBox jCheckBox5;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JComboBox<String> jComboBox3;
    private javax.swing.JComboBox<String> jComboBox4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSlider jSlider1;
    private javax.swing.JSlider jSlider2;
    private javax.swing.JSlider jSlider3;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JToggleButton jToggleButton2;
    private javax.swing.JToggleButton jToggleButton3;
    private javax.swing.JToggleButton jToggleButton4;
    private javax.swing.JToggleButton jToggleButton5;
    private javax.swing.JToggleButton jToggleButton6;
    // End of variables declaration//GEN-END:variables
}
