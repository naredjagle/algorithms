package com.thenaglecode;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.provider.local.WindowsFileName;
import sun.nio.fs.WindowsFileSystemProvider;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: jxnagl
 * Date: 9/09/13
 * Time: 5:16 PM
 * //todo implement
 */
public class SettingsFileUtil {

    public static String CONFIG_FILE_FOLDER;
    public static final String CONFIG_FOLDER_ABSOLUTE_PATH = new File(CONFIG_FILE_FOLDER).getAbsolutePath();

    static {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            URL url1 = classLoader.getResource("com/thenaglecode/config");
            URI uri1 = url1.toURI();
            URL url2 = classLoader.getResource("./config");
            URI uri2 = url2.toURI();
            CONFIG_FILE_FOLDER = new File(url1.toURI()).getAbsolutePath();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, SettingsFile> settings = new HashMap<>();

    public static void reloadSettings() throws IOException {
        settings.clear();
        FileObject folder = VFS.getManager().resolveFile("file://" + CONFIG_FILE_FOLDER);
        if (folder.getType().hasChildren()) {
            for (FileObject file : folder.getChildren()) {
                if(file.getName().getBaseName().endsWith(".cnf")){
                    SettingsFile setting = SettingsFile.fromFile(file);
                    settings.put(setting.name, setting);
                }
            }
        }
    }

    public static Map<String, SettingsFile> getAllSettings() throws IOException {
        reloadSettings();
        return settings;
    }

    public static Map<String, FileObject> getAllMysqlHomes() throws FileSystemException {
        Map<String, FileObject> files = new HashMap<>();
        String BASE_DIR = "C:/Program Files";
        FileObject programFiles = VFS.getManager().resolveFile("file://" + BASE_DIR);
        for (FileObject file : programFiles.getChildren()) {
            if (file.getName().getBaseName().startsWith("mysql")
                    && file.getType().hasChildren()) {
                files.put(file.getName().getBaseName(), file);
            }
        }
        return files;
    }

    public static Node render(final SettingsFile settings) throws FileSystemException {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        int row = 0;

        Label mysqlInstall = new Label("Mysql version:");
        grid.add(mysqlInstall, 0, row);

        ArrayList<String> listOfInstalls = new ArrayList<>();
        final Map<String, FileObject> mysqlHomes = getAllMysqlHomes();
        for (String name : mysqlHomes.keySet()) {
            listOfInstalls.add(name);
        }
        ObservableList<String> mysqlInstallOptions =
                FXCollections.observableArrayList(
                        listOfInstalls
                );

        ComboBox<String> comboBox = new ComboBox<>(mysqlInstallOptions);
        grid.add(comboBox, 1, row);
        comboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2) {
                settings.baseDir = "C:" + mysqlHomes.get(s2).getName().getPath().replace('/', '\\');
            }
        });

        Label name = new Label("Name");
        grid.add(name, 0, ++row);

        final TextField nameTxt = new TextField(settings.name);
        grid.add(nameTxt, 1, row);
        nameTxt.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2) {
                settings.name = s2;
            }
        });

        Label port = new Label("Port");
        grid.add(port, 0, ++row);

        final TextField portTxt = new TextField(String.valueOf(settings.port));
        portTxt.lengthProperty().addListener(new NumericOnlyTextFieldChangeListener(portTxt));
        grid.add(portTxt, 1, row);
        portTxt.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2) {
                try {
                    settings.port = Integer.valueOf(s2);
                } catch (NumberFormatException e) {
                    settings.port = Integer.valueOf(s);
                }
            }
        });

        Label socket = new Label("Socket");
        grid.add(socket, 0, ++row);

        final TextField socketTxt = new TextField(settings.socket);
        grid.add(socketTxt, 1, row);
        socketTxt.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2) {
                settings.socket = s2;
            }
        });

        ColumnConstraints column1 = new ColumnConstraints();
        column1.setHalignment(HPos.RIGHT);
        column1.setMaxWidth(Double.MAX_VALUE);

        ColumnConstraints column2 = new ColumnConstraints();
        column2.setHalignment(HPos.LEFT);

        grid.getColumnConstraints().addAll(column1, column2);
        grid.setVgap(10);
        grid.setHgap(10);

        return grid;
    }
}