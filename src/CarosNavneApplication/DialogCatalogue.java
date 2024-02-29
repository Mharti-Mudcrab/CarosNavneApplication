/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CarosNavneApplication;

import java.awt.Desktop;
import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;
 
public class DialogCatalogue {
 
    public static String ShowChoiceDialogue(Window parent, String name, boolean hasPrevious) {
 
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Select");
        alert.setHeaderText("Choose which gender to assign the name:\n\n\t\t\t\t\t" + name);
        alert.initOwner(parent);
        
        ButtonType woman = new ButtonType("Woman");
        ButtonType unknown = new ButtonType("Unknown");
        ButtonType man = new ButtonType("Man");
        ButtonType getPrevious = new ButtonType("Get Previous", ButtonData.BACK_PREVIOUS);
        
        alert.getDialogPane().getScene().getWindow().setOnCloseRequest((e) -> {});
        
        alert.getButtonTypes().setAll(woman, unknown, man, getPrevious);
        
            if (!hasPrevious)
                ((Button) alert.getDialogPane().lookupButton(getPrevious)).setDisable(true);
        
        Hyperlink link = new Hyperlink("Lookup Name");
        link.setOnAction((event) -> {
            try {
                Desktop.getDesktop().browse(URI.create("http://www.google.com/search?q=Which+gender+is+the+name+\"" + name + "\""));
            } catch (IOException ex) {
                System.out.println("Something went wrong with actiating hyperlink.\n" + ex);
            }
        });
        
        FlowPane fp = new FlowPane(Orientation.HORIZONTAL);
        fp.setAlignment(Pos.BASELINE_RIGHT);
        fp.getChildren().add(link);
        alert.getDialogPane().contentProperty().set(fp);
        
        // Setting keypress shortcuts.
        alert.getDialogPane().setOnKeyPressed(key -> {
            switch (key.getCode()) {
                case LEFT:
                    ((Button) alert.getDialogPane().lookupButton(woman)).fire();
                    break;
                case DOWN:
                    ((Button) alert.getDialogPane().lookupButton(unknown)).fire();
                    break;
                case RIGHT:
                    ((Button) alert.getDialogPane().lookupButton(man)).fire();
                    break;
                case UP:
                    ((Button) alert.getDialogPane().lookupButton(getPrevious)).fire();
                    break;
                case L:
                case G:
                    link.fire();
                    break;
            }
        });
        
        Optional<ButtonType> option = alert.showAndWait();
        if (!option.isPresent()) {
            return "Stop";
        }
        else
            return option.get().getText();
    }
    
    public static void showAlert() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Test Connection");
 
        // Header Text: null
        alert.setHeaderText(null);
        alert.setContentText("Connect to the database successfully!");
 
        alert.showAndWait();
    }
    
    public static String showFileChooser(Window parent, boolean isOpenChooser) {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel File", "*.xlsx", "*.xls"));
        fc.setTitle("Choose Excel document");
        String path = System.getProperty("user.home") + "\\Desktop";
        fc.setInitialDirectory(new File(path));
        
        // Show filechooser
        File file;
        if (isOpenChooser)
            file = fc.showOpenDialog(parent);
        else 
            file = fc.showSaveDialog(parent);
        
        if (file == null) {
            return null;
        }
        else {
            return file.getAbsolutePath();
        }
    }
}