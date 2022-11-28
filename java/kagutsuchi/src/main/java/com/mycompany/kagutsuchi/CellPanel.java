package com.mycompany.kagutsuchi;

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

    private LineBorder border = new LineBorder(Color.WHITE, 1);
    private boolean selectedState = false;
    private int cellIndex = 0;
    private String imageName;
    private JSONObject settingsFile;

    public CellPanel(String imageName, int index, JSONObject settingsFile) {
        this.imageName = imageName;
        this.cellIndex = index;
        this.settingsFile = settingsFile;

        JSONArray array = (JSONArray) settingsFile.get(imageName);
        for (int i = 0; i < array.size(); i++) {
            if (Integer.parseInt(array.get(i) + "") == index) {
                border = new LineBorder(Color.GREEN, 4);
                setBorder(border);
            }
        }

        this.setOpaque(false);
        this.setBorder(border);
        this.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                if (!selectedState) {
                    border = new LineBorder(Color.YELLOW, 2);
                    setBorder(border);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                if (!selectedState) {
                    border = new LineBorder(Color.WHITE, 1);
                    setBorder(border);
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                selectedState = !selectedState;
                if (selectedState) {
                    border = new LineBorder(Color.GREEN, 4);
                    setBorder(border);
                } else {
                    border = new LineBorder(Color.YELLOW, 2);
                    setBorder(border);
                }

                JSONArray array = (JSONArray) settingsFile.get(imageName);
                List<Integer> indexes = new ArrayList<>();
                for (int i = 0; i < array.size(); i++) {
                    indexes.add(Integer.parseInt(array.get(i) + ""));
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
                settingsFile.put(imageName, array);
            }
        });
    }

}
