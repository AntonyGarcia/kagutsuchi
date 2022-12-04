package com.wpi.kagutsuchi;

import java.awt.Color;
import java.awt.LayoutManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class CellPanel extends JPanel {

    private LineBorder border;
    private boolean selectedState = false;
    private boolean blackCell = false;
    private int cellIndex = 0;
    private String imageName;
    private JSONObject settingsFile;

    public CellPanel(String imageName, int index, JSONObject settingsFile, boolean blackCellFlag) {
        this.imageName = imageName;
        this.cellIndex = index;
        this.settingsFile = settingsFile;

        paintCells(blackCellFlag);

        this.setToolTipText("Cell number: " + cellIndex);

        this.setOpaque(false);
        this.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                if (!blackCell) {
                    if (!selectedState) {
                        border = new LineBorder(Color.YELLOW, 2);
                        setBorder(border);
                    }
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                if (!blackCell) {
                    if (!selectedState) {
                        border = new LineBorder(Color.WHITE, 1);
                        setBorder(border);
                    }
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                if (!blackCell) {
                    selectedState = !selectedState;
                    if (selectedState) {
                        border = new LineBorder(Color.GREEN, 4);
                        setBorder(border);
                    } else {
                        border = new LineBorder(Color.YELLOW, 2);
                        setBorder(border);
                    }

                    JSONArray array = (JSONArray) ((JSONObject) ((JSONArray) settingsFile.get("positive_cells")).get(0)).get(imageName);
                    List<Integer> indexes = new ArrayList<>();
                    for (int i = 0; i < array.size(); i++) {
                        if (Integer.parseInt(array.get(i) + "") != cellIndex) {
                            indexes.add(Integer.parseInt(array.get(i) + ""));
                        }
                    }
                    if (!indexes.contains(cellIndex)) {
                        if (selectedState) {
                            indexes.add(cellIndex);
                        }
                    }
                    indexes = indexes.stream().sorted((a, b) -> Integer.valueOf(a).compareTo(Integer.valueOf(b))).collect(Collectors.toList());
                    array = new JSONArray();
                    for (int i = 0; i < indexes.size(); i++) {
                        array.add(indexes.get(i));
                    }

                    ((JSONObject) ((JSONArray) settingsFile.get("positive_cells")).get(0)).put(imageName, array);
                }
            }
        });
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public void paintCells(boolean blackCellsFlag) {

        this.selectedState = false;
        this.blackCell = false;
        border = new LineBorder(Color.WHITE, 1);
        this.setBorder(border);

        JSONArray array = (JSONArray) ((JSONObject) ((JSONArray) settingsFile.get("positive_cells")).get(0)).get(imageName);

        for (int i = 0; i < array.size(); i++) {
            if (Integer.parseInt(array.get(i) + "") == cellIndex) {
                border = new LineBorder(Color.GREEN, 4);
                setBorder(border);
                this.selectedState = true;
            }
        }
        if (blackCellsFlag) {
            array = (JSONArray) ((JSONObject) ((JSONArray) settingsFile.get("black_cells")).get(0)).get(imageName);
            for (int i = 0; i < array.size(); i++) {
                if (Integer.parseInt(array.get(i) + "") == cellIndex) {
                    border = new LineBorder(new Color(114, 40, 111), 4);
                    setBorder(border);
                    this.selectedState = true;
                    this.blackCell = true;
                }
            }
        }
    }

}
