/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CarosNavneApplication;

import GenderAPI.HTTPClient;
import java.awt.MultipleGradientPaint;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.paint.RadialGradient;
import javafx.scene.shape.Circle;
import javafx.stage.Window;
import javafx.util.Callback;
import org.apache.poi.EmptyFileException;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 *
 * @author madsw
 */
public class FXMLDocumentController implements Initializable {

    private Workbook wb;
    private Window window;
    private int nrOfRows;
    private int progressIndex;
    private int progressBarInterval;
    private ArrayList<Person> namePool;
    private Stack<Person> nameEntrylist;
    private HashMap<String, Person> nameCatalogue;
    private HTTPClient client;
    private boolean isClientExausted;
    private String sex;
    private String queryName;
    private int newNameCount;
    
    @FXML
    private Button btnChooseFile;
    @FXML
    private TextField txtFilePath;
    @FXML
    private TextField txtRowNr;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private TextArea txtfieldOutput;
    @FXML
    private Button btnStartSorting;
    @FXML
    private TableView<Person> tvNavne;
    @FXML
    private Label lblRowNr;
    @FXML
    private TableColumn<Person, String> columnName;
    @FXML
    private TableColumn<Person, String> columnKoen;
    @FXML
    private Button btnSaveToFile;
    @FXML
    private Button btnUndoLastSelection;
    @FXML
    private Label lblProgesss;
    @FXML
    private ToggleButton btnToggleAPI;
    @FXML
    private Circle apiIndicatorLightOFF;
    @FXML
    private Circle apiIndicatorLightON;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                btnChooseFile.requestFocus();
                
                window = btnChooseFile.getScene().getWindow();
                
                window.setOnCloseRequest((ev) -> {
                    if (wb != null) {
                        try {
                            System.out.println("Recieved notice about application closing. Trying to close workbook");
                            wb.close();
                        } catch (IOException ex) {
                            System.out.println("Trying to close workbook in application exit failed.\n" + ex);
                        }
                    }
                    else {
                        System.out.println("Recieved notice about application closing. Workbook already closed. Np");
                    }
                });
                
            }
        });
        
//        MenuItem mi1 = new MenuItem("Menu item 1");
//        mi1.setOnAction((ActionEvent event) -> {
//            System.out.println("Menu item 1");
//            Object item = tvNavne.getSelectionModel().getSelectedItem();
//            System.out.println("Selected item: " + item);
//        });
//
//        ContextMenu menu = new ContextMenu();
//        menu.getItems().add(mi1);
//        tvNavne.setContextMenu(menu);
        
        
        tvNavne.setRowFactory(  (TableView<Person> tableView) -> {
                                    final TableRow<Person> row = new TableRow<>();
                                    final ContextMenu rowMenu = new ContextMenu();
                                    MenuItem editItem = new MenuItem("Edit");
                                    editItem.setOnAction(   (e) -> {
                                                                Person p = row.getItem();
                                                                System.out.println("We want to edit item: " + p);
                                                                String newSex = DialogCatalogue.ShowChoiceDialogue(window, p.getName(), false);
                                                                if (newSex.equals("Stop"))
                                                                    return;
                                                                else {
                                                                    Person np = nameCatalogue.get(p.getName());
                                                                    np.setSex(newSex);
                                                                    System.out.println(tvNavne.getItems().indexOf(p));
                                                                    System.out.println(np);
                                                                    tvNavne.getItems().set(tvNavne.getItems().indexOf(p), np);
                                                                    outputToTxtfieldLn("Value of Catalogue item: " + np.getName() + ", changed to: " + np.getSex());
                                                                    //nameCatalogue.put(sex, np);
                                                                }
                                            
                                                            });
                                    MenuItem removeItem = new MenuItem("Delete");
                                    removeItem.setOnAction( (ActionEvent event) -> {
                                                                tvNavne.getItems().remove(row.getItem());
                                                            });
                                    rowMenu.getItems().addAll(editItem, removeItem);

                                    // only display context menu for non-empty rows:
                                    row.contextMenuProperty().bind(
                                            Bindings.when(row.emptyProperty())
                                                    .then((ContextMenu) null)
                                                    .otherwise(rowMenu));
                                    return row;
                                });

        
        columnName.setCellValueFactory(new PropertyValueFactory<>("name"));
        columnKoen.setCellValueFactory(new PropertyValueFactory<>("sex"));
        
        client = new HTTPClient();
        isClientExausted = false;
    }    
    
    private void initSheet(Workbook wb) {
        Sheet sheet = wb.getSheetAt(0);
        
        int rowStart = sheet.getFirstRowNum();
        int rowEnd = sheet.getLastRowNum();
        
        while ( sheet.getRow(rowStart) == null ) {
            rowStart++;
        }
        
        while ( sheet.getRow(rowEnd) == null ) {
            rowEnd--;
        }
        
        newNameCount = 0;
        
        nrOfRows = rowEnd-rowStart +1;
        progressIndex = rowStart;
        if (nrOfRows < 1000 || true)
            progressBarInterval = 1;
        else
            progressBarInterval = nrOfRows / 100;
        txtRowNr.setText(Integer.toString(nrOfRows));
        outputToTxtfieldLn("txtRowNr field updated with: " + rowEnd + " - " + rowStart + " + 1 = " + nrOfRows);
        outputToTxtfieldLn("progressBarInterval updated to be: " + progressBarInterval);
    }
    
    @FXML
    private void btnChooseFileFired(ActionEvent event) {
        // Show filechooser
        String excelFile = DialogCatalogue.showFileChooser(window, true);
        
        // did filechooser return file?
        if (excelFile != null) {
            
            txtFilePath.setText(excelFile);
            outputToTxtfieldLn("File loaded with path: " + excelFile);
            
            try {
                if (namePool == null) {
                    namePool = new ArrayList<>(nrOfRows);
                    nameEntrylist = new Stack<>();
                    nameCatalogue = new HashMap<>();
                }
                else {
                    resetAllContainers();
                }
                
                wb = WorkbookFactory.create(new FileInputStream(excelFile));
                
                enableAll();
                
                initSheet(wb);
                
                updateProgressBar();
                
            } catch (IOException | EncryptedDocumentException | EmptyFileException ex) {
                System.out.println("Something went wrong in creating workbook.\n" + ex);
            }
        }
    }
    
    @FXML
    private void btnUndoLastSelectionFired(ActionEvent event) {
        if (!nameEntrylist.isEmpty()) {
            if (nrOfRows == progressIndex) {
                btnStartSorting.setDisable(false);
            }
            // Remove the first from entrylist, nameCatalogue and tableView
            Person p = nameEntrylist.pop();
            progressIndex = p.getFirstIndex();
            nameCatalogue.remove(p.getName());
            tvNavne.getItems().remove(tvNavne.getItems().size()-1);
            outputToTxtfieldLn("Removed " + p.getName() + " from namePool");
            // while there are still names in entrylist with same firstIndex repeat last step
            while (!nameEntrylist.isEmpty() && progressIndex == nameEntrylist.peek().getFirstIndex()) {
                p = nameEntrylist.pop();
                nameCatalogue.remove(p.getName());
                tvNavne.getItems().remove(tvNavne.getItems().size()-1);
            }
            
            // Now remove from namePool
            if (progressIndex > 0)
                while (progressIndex < namePool.size()) {
                    namePool.remove(namePool.size()-1);
                }
            else
                namePool.clear();
            
            // If button is pressed, go to sorting after.
            if (event != null) {
                btnStartSortingFired(null);
            }
            
            sex = null;
        }
    }
    
    private void editSelection(Person p, String sex) {
        if (!nameEntrylist.isEmpty()) {
            //nameEntrylist.set(nameEntrylist.indexOf(p), p)
            // nameEntrylist
            // nameCatalogue
            // namePool
        }
    }
    
    private void callSortingDialogue()
    {
        synchronized (this) {
            sex = DialogCatalogue.ShowChoiceDialogue(null, queryName, !nameCatalogue.isEmpty());
            notify();
        }
    }
    
    @FXML
    private void btnStartSortingFired(ActionEvent event) {
        
        Thread sortingThread = new Thread(() -> 
        {
            synchronized (this) 
            {
                if (progressIndex == 0) {
                    outputToTxtfieldLn("Sorting Started!");
                }
                else {
                    outputToTxtfieldLn("Sorting resumed.");
                }

                Sheet sheet = wb.getSheetAt(0);

                for (; progressIndex < nrOfRows; progressIndex++) {

                    /*Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            updateProgressBar();
                        }
                    }); */

                    Row row = sheet.getRow(progressIndex);
                    if (row == null) {
                        continue;
                    }
                    Cell cell = row.getCell(0);

                    // Get cellString and cellName array
                    String cellString;
                    if (cell.getCellType() != CellType.STRING) {
                        DataFormatter df = new DataFormatter();
                        cellString = df.formatCellValue(cell).trim();
                    }
                    else {
                        cellString = cell.getStringCellValue().trim();
                    }

                    // Clean and split.
                    String[] cellName = getSplitAndClean(cellString);

                    // Is the list empty? Then we are done here
                    boolean isThereNothing = true;
                    for (String str : cellName) {
                        if (!str.isEmpty()) {
                            isThereNothing = false;
                        }
                    }
                    if (isThereNothing) {
                        sex = "Unknown";
                    }

                    // Is there a match in the list?
                    for (int i = 0; i < cellName.length && !isThereNothing; i++) {
                        cellName[i] = toTitleCase(cellName[i]);
                        if (nameCatalogue.containsKey(cellName[i])) {
                            sex = nameCatalogue.get(cellName[i]).getSex();
                            break;
                        }
                    }

                    // Is there any of the subnames that we do't have in the system?
                    if ((cellName.length > 1 || sex == null) && !isThereNothing) {
                         // If there was no match we gotta call the API or the dialogbox for help... with the sex
                        if (sex == null) {
                            // Let's first ask the API
                            if (!isClientExausted) {
                                for (String name : cellName) {
                                    sex = client.sendHTTP(name);
                                    // If it is exausted we mark exausted and never return
                                    if (sex.equals("error")) {
                                        outputToTxtfieldLn("API trial request limit reached on this conncetion, try switching to a different one and enable API again.");
                                        btnToggleAPI.fire();
                                        break;
                                    }
                                    // If we got a match we are done here
                                    else if (!sex.equals("Unknown")) {
                                        break;
                                    }
                                }
                            }

                            // If API is exausted or failed to return Man or Woman, we need user help.
                            if (sex == null || sex.equals("Unknown") || sex.equals("error")) {
                                for (String st: cellString.split("[ ,]")) {
                                    queryName = st.replaceAll("\\d", "");
                                    if (!st.equals("")) {
                                        Platform.runLater(() -> {
                                            callSortingDialogue();
                                        });
                                        while(sex == null || sex.equals("error"))
                                        {   
                                            try {
                                                wait();
                                            } catch (InterruptedException ex) {
                                                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                                            }
                                        }
                                        if (sex.equals("Stop")) {
                                            sex = null;
                                            outputToTxtfieldLn("Sorting Paused.");
                                            return;
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                        // Was the previous button pressed?
                        if (sex.equals("Get Previous")) {
                            btnUndoLastSelectionFired(null);
                            // Jump out and return to progressIndex++ -1
                            progressIndex--;
                            continue;
                        }

                        // Did we gain new information?
                        for (String name : cellName) {
                            if (!nameCatalogue.containsKey(name)) {
                                nameCatalogue.put(name, new Person(cellString, sex, progressIndex));
                                nameEntrylist.add(new Person(name, sex, progressIndex));
                                outputToTxtfieldLn("Updated nameCatalogue with: " + name + ", " + sex);

                                // Update table view
                                tvNavne.getItems().add(new Person(name, sex, progressIndex));
                                outputToTxtfieldLn("Updated tableView with: " + name + ", " + sex);

                                // Update counter for every new entry
                                newNameCount++; //TOBEREMOVED
                            }
                        }
                    }

                    namePool.add(new Person(cellString, sex, progressIndex));
                    outputToTxtfieldLn("Added to namePool: " + namePool.get(namePool.size()-1));



                    // If ended here, sex was used and needs to be reset for next time
                    sex = null;

        //            if (progressIndex >= 21000) {
        //                break;
        //            }

                    // Report number of new catalogue entries
                    if (progressIndex % 1000 == 0 && progressIndex != 0) {
                        System.out.println(getTimestamp() + " " + progressIndex + ", " + newNameCount);
                        outputToTxtfieldLn("Number of new names added within the last 1000: " + newNameCount);
                        newNameCount = 0;
                    }
                }

                // If sorting is finished
                if (progressIndex == nrOfRows) {
                    outputToTxtfieldLn("Sorting is Done!");

                    System.out.println("Contents of namePool:\n"
                                     + "-------------------------------");
                    for (Person p : namePool) {
                        System.out.printf("%-30s%-8s\n", p.getName(), p.getSex(), p.getFirstIndex());
                    }

                    System.out.println("\nContents of nameCatalogue:\n"
                                     + "-------------------------------");
                    Iterator<String> keys = nameCatalogue.keySet().iterator();
                    Iterator<Person> persons = nameCatalogue.values().iterator();
                    for (; keys.hasNext() && persons.hasNext();) {
                        Person p = persons.next();
                        String s = keys.next();
                        System.out.printf("%-20s%-8s%-5s\n", s, p.getSex(), p.getFirstIndex());
                    }

                    btnSaveToFile.setDisable(false);
                    btnUndoLastSelection.setDisable(false);
                    btnStartSorting.setDisable(true);

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            updateProgressBar();
                        }
                    }); 
                    System.out.println("Finished! progressIndex: " + progressIndex + ", nrOfRows: " + nrOfRows);
                }
            }
        });
        sortingThread.setDaemon(true);
        sortingThread.start();
    }
    
    @FXML
    private void btnSaveToFileFired(ActionEvent event) {
        btnUndoLastSelection.setDisable(true);
        outputToTxtfieldLn("Preparing file output");
        
        // Try writing to sheet.
        // Fill sheet with sex
        Sheet sheet = wb.getSheetAt(0);
        
        try {
            int i = 0;
            int j = 0;
            int col = 1;
            for (; i < namePool.size() && j < nrOfRows;) {
                Row row = sheet.getRow(j);
                while (row == null) {
                    j++;
                    row = sheet.getRow(j);
                }
                System.out.println("Writing first to sheet: " + j + "| " + namePool.get(i));
                
                Cell cell = row.getCell(col);
                if (cell == null) {
                    cell = row.createCell(col);
                }
                cell.setCellType(CellType.STRING);
                cell.setCellValue(namePool.get(i).getSex());
                
                cell = row.getCell(2);
                if (cell == null) {
                    cell = row.createCell(2);
                }
                cell.setCellType(CellType.STRING);
                cell.setCellValue(namePool.get(i).getName());
                
                cell = row.getCell(3);
                if (cell == null) {
                    cell = row.createCell(3);
                }
                cell.setCellType(CellType.STRING);
                cell.setCellValue(i);
                
                i++; j++;
            }
            System.out.println("Stopping with i: " + i + ", j: " + j + ", col: " + col);
            outputToTxtfieldLn("Stopping with i: " + i + ", j: " + j + ", col: " + col);
        } catch (Exception e) {
            outputToTxtfieldLn("An error occured while preparing file output: " + e.getMessage());
            throw e;
        }
        
        outputToTxtfieldLn("Writing to file");
        
        // Write the output to a file
        String outputFile = DialogCatalogue.showFileChooser(window, false);
        try (OutputStream fileOut = new FileOutputStream(outputFile)) {
            wb.write(fileOut);
            outputToTxtfieldLn("File saved successfully!");
        }
        catch (IOException | NullPointerException ex) {
            if (ex.getMessage() == null)
                outputToTxtfieldLn("An error occured while saving file: Window closed");
            else
                outputToTxtfieldLn("An error occured while saving file: " + ex.getMessage());
            System.out.println(ex.getMessage());
        }
        
    }
    
    private void outputToTxtfieldLn(String s) {
        txtfieldOutput.setText(getTimestamp() + s + "\n" + txtfieldOutput.getText());
    }
    
    private String getTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return "[" + sdf.format(new Timestamp(System.currentTimeMillis())) + "] ";
    }

    private void enableAll() {
        btnStartSorting.setDisable(false);
        lblRowNr.setDisable(false);
        progressBar.setDisable(false);
        lblProgesss.setDisable(false);
        tvNavne.setDisable(false);
        txtRowNr.setDisable(false);
        txtfieldOutput.setDisable(false);
        btnToggleAPI.setDisable(false);
    }
    
    private static String toTitleCase(String s) {
        return Character.toTitleCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }       

    private void updateProgressBar() {
        synchronized (this) 
        {            
            if (progressIndex % progressBarInterval == 0 || progressIndex == nrOfRows) {
                progressBar.setProgress(((double)(progressIndex)) / nrOfRows);
                lblProgesss.setText(Math.round(progressBar.getProgress() * 10000) /100.0 + "%");
                if (progressBar.getProgress() == 1) {
                    btnSaveToFile.setDisable(false);
                    btnUndoLastSelection.setDisable(false);
                }
                else {
                    btnSaveToFile.setDisable(true);
                    btnUndoLastSelection.setDisable(true);
                }
            }
        }
    }

    private void resetAllContainers() {
        nameCatalogue.clear();
        nameEntrylist.clear();
        namePool.clear();
        tvNavne.getItems().clear();
        try {
            wb.close();
        } catch (IOException ex) {
            System.out.println("Problem with closing workbook in resetAllContainers.\n" + ex);
        }
        btnSaveToFile.setDisable(true);
        btnUndoLastSelection.setDisable(true);
    }
    
    private static String[] getSplitAndClean(String cellString) {
        // If it contains a @ and no . and is connected to a word replace with a
        if (cellString.contains("@") && !cellString.contains(".")) {
            cellString = cellString.replaceAll("(?<=\\w*)@(?=\\w+)|(?<=\\w+)@(?=\\w*)", "a");
        }
        // Remove @ and everything after. Also remove all numbers and =!@
        cellString = cellString.replaceAll("(@.*\\.+.*)", "").replaceAll("[\\d*=!@]", "");
        // Split into sections with " ,."
        String[] cellNamerough = cellString.split("[ ,.]");
        // Find first not blank string.
        String cellCut = "";
        for (String s : cellNamerough) {
            s = s.replaceAll("^-*|-*$", "");
            if (!s.equals("")) {
                cellCut = s;
                break;
            }
        }
        // Remove numbers and trailing or leading '-' then split with "-" and return
        return cellCut.replaceAll("-+", "-").split("-");
    }

    @FXML
    private void btnToggleAPIFired(ActionEvent event) {
        if (btnToggleAPI.isSelected()) {
            apiIndicatorLightON.setVisible(true);
            apiIndicatorLightOFF.setVisible(false);
            isClientExausted = false;
            outputToTxtfieldLn("API toggled ON");
        }
        else {
            apiIndicatorLightON.setVisible(false);
            apiIndicatorLightOFF.setVisible(true);
            isClientExausted = true;
            outputToTxtfieldLn("API toggled OFF");
        }
    }
}
