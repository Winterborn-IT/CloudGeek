package com.cloudgb.cloud;

import com.cloud.cloudmodel.CloudMessage;
import com.cloud.cloudmodel.FileMessage;
import com.cloud.cloudmodel.ListFiles;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class CloudController implements Initializable {

    @FXML
    public TableView<FileInfo> clientFiles;
    @FXML
    public TableView<FileInfo> serverFiles;
    @FXML
    public TextField clientPathField;
    @FXML
    public TextField serverPathField;

    private String homeDir;

    private Network network;

    TableColumn<FileInfo, String> clientFileTypeColumn;
    TableColumn<FileInfo, String> clientFilenameColumn;
    TableColumn<FileInfo, Long> clientFileSizeColumn;
    TableColumn<FileInfo, String> clientFileDateColumn;

    TableColumn<FileInfo, String> serverFileTypeColumn;
    TableColumn<FileInfo, String> serverFilenameColumn;
    TableColumn<FileInfo, Long> serverFileSizeColumn;
    TableColumn<FileInfo, String> serverFileDateColumn;

    private void readLoop() {
        try {
            while (true) {
                CloudMessage message = network.read();
                if (message instanceof ListFiles listFiles) {
                    Platform.runLater(() -> {
                        serverFiles.getItems().clear();
//                        serverFiles.getItems().addAll(listFiles.getFiles());
                    });
                } else if (message instanceof FileMessage fileMessage) {
                    Path current = Path.of(homeDir).resolve(fileMessage.getName());
                    Files.write(current, fileMessage.getData());
                    Platform.runLater(() -> {
//                        serverview.clear
//                        serverview.addALl
                    });
                }
            }
        } catch (Exception e) {
            System.err.println("Connection lost");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            homeDir = ".";
            network = new Network(8189);
            Thread readThread = new Thread(() -> readLoop());
            readThread.setDaemon(true);
            readThread.start();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        clientFileTypeColumn = new TableColumn<>();
        clientFileTypeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getType().getName()));
        clientFileTypeColumn.setPrefWidth(24);

        clientFilenameColumn = new TableColumn<>("Имя");
        clientFilenameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFilename()));
        clientFilenameColumn.setPrefWidth(240);

        clientFileSizeColumn = new TableColumn<>("Размер");
        clientFileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSize()));
        clientFileSizeColumn.setCellFactory(column -> new TableCell<FileInfo, Long>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    String text = String.format("%,d bytes", item);
                    if (item == -1L) {
                        text = "[DIR]";
                    }
                    setText(text);
                }
            }
        });
        clientFileSizeColumn.setPrefWidth(120);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        clientFileDateColumn = new TableColumn<>("Дата изменения");
        clientFileDateColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLastModified().format(dtf)));
        clientFileDateColumn.setPrefWidth(120);

        clientFiles.getColumns().addAll(clientFileTypeColumn, clientFilenameColumn, clientFileSizeColumn, clientFileDateColumn);
        clientFiles.getSortOrder().add(clientFileTypeColumn);

        serverFileTypeColumn = new TableColumn<>();
        serverFileTypeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getType().getName()));
        serverFileTypeColumn.setPrefWidth(24);

        serverFilenameColumn = new TableColumn<>("Имя");
        serverFilenameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFilename()));
        serverFilenameColumn.setPrefWidth(240);

        serverFileSizeColumn = new TableColumn<>("Размер");
        serverFileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSize()));
        serverFileSizeColumn.setCellFactory(column -> new TableCell<FileInfo, Long>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    String text = String.format("%,d bytes", item);
                    if (item == -1L) {
                        text = "[DIR]";
                    }
                    setText(text);
                }
            }
        });
        serverFileSizeColumn.setPrefWidth(120);

        DateTimeFormatter dtfromat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        serverFileDateColumn = new TableColumn<>("Дата изменения");
        serverFileDateColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLastModified().format(dtfromat)));
        serverFileDateColumn.setPrefWidth(120);

        serverFiles.getColumns().addAll(clientFileTypeColumn, serverFilenameColumn, serverFileSizeColumn, serverFileDateColumn);
        serverFiles.getSortOrder().add(serverFileTypeColumn);

        clientFiles.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    Path path = Paths.get(clientPathField.getText()).resolve(clientFiles.getSelectionModel().getSelectedItem().getFilename());
                    if (Files.isDirectory(path)) {
                        updateList(path);
                    }
                }
            }
        });

        updateList(Paths.get(homeDir));
    }

    private List<String> getFiles(String dir) {
        String[] list = new File(dir).list();
        assert list != null;
        return Arrays.asList(list);
    }

    public void upload(ActionEvent actionEvent) throws IOException {
//        String file = clientView.getSelectionModel().getSelectedItem();
//        network.write(new FileMessage(Path.of(homeDir).resolve(file)));
    }

    public void download(ActionEvent actionEvent) throws IOException {
//        String file = serverView.getSelectionModel().getSelectedItem();
//        network.write(new FileRequest(file));
    }

    public void updateList(Path path) {
        try {
            clientPathField.setText(path.normalize().toAbsolutePath().toString());
            clientFiles.getItems().clear();
            clientFiles.getItems().addAll(Files.list(path).map(FileInfo::new).toList());
            clientFiles.sort();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "По какой-то причине не удалось обновить список файлов", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public String getSelectedFilename() {
        if (!clientFiles.isFocused()) {
            return null;
        }
        return clientFiles.getSelectionModel().getSelectedItem().getFilename();
    }

    public String getCurrentPath() {
        return clientPathField.getText();
    }

    public void btnPathUpClient(ActionEvent actionEvent) {
        Path upperPath = Paths.get(clientPathField.getText()).getParent();
        if (upperPath != null) {
            updateList(upperPath);
        }
    }

    public void btnPathUpServer(ActionEvent actionEvent) {
    }
}
