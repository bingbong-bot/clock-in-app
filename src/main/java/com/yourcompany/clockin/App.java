package com.yourcompany.clockin;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.text.Font;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.control.SelectionMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.io.File;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.concurrent.atomic.AtomicBoolean;
import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgcodecs.Imgcodecs;
import com.yourcompany.Datamanager;


public class App extends Application {
    private static boolean openCvLoaded = false;
    private final Datamanager datamanager = new Datamanager();
    private static final String ADMIN_PASSWORD = "admin123";

    @Override
    public void start(Stage primaryStage) {
        // Your JavaFX UI code will go here
        Label titleLabel = new Label("Clock-In");
        titleLabel.setFont(new Font("Arial", 24));

        Label usernameLabel = new Label("Enter your username:");
        TextField usernameTextField = new TextField();

        Label passwordLabel = new Label("Enter your password:");
        PasswordField passwordTextField = new PasswordField();

        Button loginButton = new Button("Login");
        Button createButton = new Button("Create Account");
        Button viewRecordsButton = new Button("View Records (Admin)");
        Button dbViewerButton = new Button("DB Viewer (Admin)");

        //Logging into the account
        loginButton.setOnAction(e -> {
            String username = usernameTextField.getText();
            String password = passwordTextField.getText();
            if (!datamanager.verifyUser(username, password)) {
                showAlert("Login Failed", "Invalid username or password.");
                return;
            }
            //Open new Window
            Stage clockInStage = new Stage();
            primaryStage.hide();
            clockInStage.setOnHidden(ev -> primaryStage.show());
            
            Label clockInLabel = new Label("Clock In/Out System");
            clockInLabel.setFont(new Font("Arial", 24));
            
            // Clock In Button
            Button clockInButton = new Button("Clock In");
            clockInButton.setOnAction(event -> {
                String photoPath = capturePhoto(clockInStage, username, "clock-in");
                if (photoPath == null) {
                    return;
                }
                LocalDateTime clockInTime = LocalDateTime.now();
                datamanager.recordClockIn(username, clockInTime, photoPath);
                System.out.println("Clocked in at: " + clockInTime);
            });
            
            // Clock Out Button
            Button clockOutButton = new Button("Clock Out");
            clockOutButton.setOnAction(event -> {
                String photoPath = capturePhoto(clockInStage, username, "clock-out");
                if (photoPath == null) {
                    return;
                }
                LocalDateTime clockOutTime = LocalDateTime.now();
                datamanager.recordClockOut(username, clockOutTime, photoPath);
                System.out.println("Clocked out at: " + clockOutTime);
            });

            VBox clockInLayout = new VBox(10);
            clockInLayout.setAlignment(Pos.CENTER);
            clockInLayout.setPadding(new Insets(20));
            clockInLayout.getChildren().addAll(clockInLabel, clockInButton, clockOutButton);

            Scene clockInScene = new Scene(clockInLayout, 400, 300);
            clockInStage.setScene(clockInScene);
            clockInStage.setTitle("Clock In/Out");
            clockInStage.show();
        });

        viewRecordsButton.setOnAction(e -> {
            PasswordField adminPass = new PasswordField();
            adminPass.setPromptText("Admin password");
            Button ok = new Button("Open");
            Button cancel = new Button("Cancel");

            Stage adminDialog = new Stage();
            adminDialog.initOwner(primaryStage);
            adminDialog.initModality(Modality.APPLICATION_MODAL);
            adminDialog.setTitle("Admin Access");

            HBox adminButtons = new HBox(10, ok, cancel);
            adminButtons.setAlignment(Pos.CENTER);

            VBox adminLayout = new VBox(10, new Label("Enter admin password:"), adminPass, adminButtons);
            adminLayout.setAlignment(Pos.CENTER);
            adminLayout.setPadding(new Insets(16));
            adminDialog.setScene(new Scene(adminLayout, 320, 160));

            ok.setOnAction(evt -> {
                if (!ADMIN_PASSWORD.equals(adminPass.getText())) {
                    showAlert("Access Denied", "Incorrect admin password.");
                    return;
                }
                adminDialog.close();
                primaryStage.hide();
                openRecordsWindow(primaryStage);
            });
            cancel.setOnAction(evt -> adminDialog.close());

            adminDialog.showAndWait();
        });

        dbViewerButton.setOnAction(e -> {
            PasswordField adminPass = new PasswordField();
            adminPass.setPromptText("Admin password");
            Button ok = new Button("Open");
            Button cancel = new Button("Cancel");

            Stage adminDialog = new Stage();
            adminDialog.initOwner(primaryStage);
            adminDialog.initModality(Modality.APPLICATION_MODAL);
            adminDialog.setTitle("Admin Access");

            HBox adminButtons = new HBox(10, ok, cancel);
            adminButtons.setAlignment(Pos.CENTER);

            VBox adminLayout = new VBox(10, new Label("Enter admin password:"), adminPass, adminButtons);
            adminLayout.setAlignment(Pos.CENTER);
            adminLayout.setPadding(new Insets(16));
            adminDialog.setScene(new Scene(adminLayout, 320, 160));

            ok.setOnAction(evt -> {
                if (!ADMIN_PASSWORD.equals(adminPass.getText())) {
                    showAlert("Access Denied", "Incorrect admin password.");
                    return;
                }
                adminDialog.close();
                primaryStage.hide();
                Platform.runLater(() -> openDbViewerWindow(primaryStage));
            });
            cancel.setOnAction(evt -> adminDialog.close());

            adminDialog.showAndWait();
        });
        
        //Creating an account
        createButton.setOnAction(e -> {
            Stage createAccountStage = new Stage();
            primaryStage.hide();
            createAccountStage.setOnHidden(ev -> primaryStage.show());
            
            Label createTitle = new Label("Create Account");
            createTitle.setFont(new Font("Arial", 20));
            
            Label newUsernameLabel = new Label("Username:");
            TextField newUsernameField = new TextField();
            
            Label newPasswordLabel = new Label("Password:");
            PasswordField newPasswordField = new PasswordField();

            Label confirmPasswordLabel = new Label("Confirm Password:");
            PasswordField confirmPasswordField = new PasswordField();
            
            Button submitButton = new Button("Create");
            
            submitButton.setOnAction(event -> {
                String username = newUsernameField.getText();
                String password = newPasswordField.getText();
                String confirm = confirmPasswordField.getText();
                if (username == null || username.isBlank() || password == null || password.isBlank()) {
                    showAlert("Create Account Failed", "Username and password are required.");
                    return;
                }
                if (!password.equals(confirm)) {
                    showAlert("Create Account Failed", "Passwords do not match.");
                    return;
                }
                String error = datamanager.createUser(username, password);
                if (error != null) {
                    showAlert("Create Account Failed", error);
                    return;
                }
                showAlert("Account Created", "Your account has been created. You can now log in.");
                createAccountStage.close();
            });
            
            VBox createLayout = new VBox(10);
            createLayout.setAlignment(Pos.CENTER);
            createLayout.setPadding(new Insets(20));
            createLayout.getChildren().addAll(
                createTitle,
                newUsernameLabel, newUsernameField,
                newPasswordLabel, newPasswordField,
                confirmPasswordLabel, confirmPasswordField,
                submitButton
            );
            
            Scene createScene = new Scene(createLayout, 350, 300);
            createAccountStage.setScene(createScene);
            createAccountStage.setTitle("Create Account");
            createAccountStage.show();
        });

        GridPane loginForm = new GridPane();
        loginForm.setHgap(10);
        loginForm.setVgap(10);
        loginForm.add(usernameLabel, 0, 0);
        loginForm.add(usernameTextField, 1, 0);
        loginForm.add(passwordLabel, 0, 1);
        loginForm.add(passwordTextField, 1, 1);
        ColumnConstraints leftCol = new ColumnConstraints();
        ColumnConstraints rightCol = new ColumnConstraints();
        rightCol.setHgrow(Priority.ALWAYS);
        loginForm.getColumnConstraints().addAll(leftCol, rightCol);

        HBox loginButtons = new HBox(10, loginButton, createButton, viewRecordsButton, dbViewerButton);
        loginButtons.setAlignment(Pos.CENTER);

        VBox root = new VBox(16);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(24));
        root.getChildren().addAll(titleLabel, loginForm, loginButtons);
        VBox.setVgrow(loginForm, Priority.NEVER);

        Rectangle2D screen = Screen.getPrimary().getVisualBounds();
        double width = Math.min(520, screen.getWidth() * 0.6);
        double height = Math.min(420, screen.getHeight() * 0.6);

        Scene scene = new Scene(root, width, height);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Clock-In App");
        primaryStage.setMinWidth(360);
        primaryStage.setMinHeight(280);
        primaryStage.setResizable(true);
        primaryStage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }

    private String capturePhoto(Stage owner, String username, String kind) {
        if (!ensureOpenCvLoaded()) {
            showAlert("Camera Error", "OpenCV failed to load. Please restart the app and try again.");
            return null;
        }

        VideoCapture camera = new VideoCapture(0);
        if (!camera.isOpened()) {
            showAlert("No Camera Found", "No webcam was detected or the camera is in use. You must use a laptop camera to clock in.");
            return null;
        }

        AtomicBoolean captured = new AtomicBoolean(false);
        final String[] savedPath = new String[1];

        Label instruction = new Label("Please take a photo to clock in.");
        ImageView preview = new ImageView();
        preview.setFitWidth(320);
        preview.setPreserveRatio(true);

        Mat frame = new Mat();
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(120), e -> {
            if (camera.read(frame) && !frame.empty()) {
                preview.setImage(SwingFXUtils.toFXImage(matToBufferedImage(frame), null));
            }
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        Button captureButton = new Button("Take Photo");
        Button cancelButton = new Button("Cancel");

        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Clock In - Photo Required");

        captureButton.setOnAction(e -> {
            if (!camera.read(frame) || frame.empty()) {
                showAlert("Capture Failed", "Couldn't capture an image. Please try again.");
                return;
            }

            try {
                Path photosDir = datamanager.getDataDir().resolve("photos");
                Files.createDirectories(photosDir);
                String safeUser = (username == null || username.isBlank()) ? "user" : username.trim();
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
                Path output = photosDir.resolve(safeUser + "-" + kind + "-" + timestamp + ".jpg");
                Imgcodecs.imwrite(output.toString(), frame);
                captured.set(true);
                savedPath[0] = output.toString();
                dialog.close();
            } catch (IOException ex) {
                showAlert("Save Failed", "Couldn't save the photo. Please try again.");
            }
        });

        cancelButton.setOnAction(e -> dialog.close());

        HBox buttons = new HBox(10, captureButton, cancelButton);
        buttons.setAlignment(Pos.CENTER);

        VBox layout = new VBox(12, instruction, preview, buttons);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        dialog.setScene(new Scene(layout, 420, 420));
        dialog.setOnHidden(e -> {
            timeline.stop();
            camera.release();
            frame.release();
        });

        dialog.showAndWait();
        return captured.get() ? savedPath[0] : null;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void openRecordsWindow(Stage owner) {
        Stage recordsStage = new Stage();
        recordsStage.setTitle("Clock Records");
        if (owner != null) {
            recordsStage.initOwner(owner);
            recordsStage.setOnHidden(ev -> owner.show());
        }

        TableView<String[]> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        TableColumn<String[], String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[0]));

        TableColumn<String[], String> userCol = new TableColumn<>("Username");
        userCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[1]));

        TableColumn<String[], String> inTimeCol = new TableColumn<>("Clock In");
        inTimeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[2]));

        TableColumn<String[], String> outTimeCol = new TableColumn<>("Clock Out");
        outTimeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[3]));

        TableColumn<String[], String> inPhotoCol = new TableColumn<>("In Photo");
        inPhotoCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[4]));

        TableColumn<String[], String> outPhotoCol = new TableColumn<>("Out Photo");
        outPhotoCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[5]));

        table.getColumns().addAll(idCol, userCol, inTimeCol, outTimeCol, inPhotoCol, outPhotoCol);
        table.getItems().addAll(datamanager.getAllClockRecords());

        ComboBox<String> userFilter = new ComboBox<>();
        userFilter.getItems().add("All Users");
        userFilter.getItems().addAll(datamanager.getUsernames());
        userFilter.getSelectionModel().selectFirst();

        TextField limitField = new TextField();
        limitField.setPromptText("Max rows");
        limitField.setPrefWidth(100);

        Button refresh = new Button("Refresh");
        refresh.setOnAction(e -> {
            String selectedUser = userFilter.getValue();
            String filter = "All Users".equals(selectedUser) ? null : selectedUser;
            Integer limit = null;
            if (limitField.getText() != null && !limitField.getText().isBlank()) {
                try {
                    limit = Integer.parseInt(limitField.getText().trim());
                } catch (NumberFormatException ex) {
                    showAlert("Invalid Limit", "Max rows must be a number.");
                    return;
                }
            }
            table.getItems().setAll(datamanager.getClockRecords(filter, limit));
        });

        Button dbViewer = new Button("DB Viewer");
        dbViewer.setOnAction(e -> {
            recordsStage.hide();
            openDbViewerWindow(recordsStage);
        });

        HBox actions = new HBox(10, new Label("User:"), userFilter, new Label("Max rows:"), limitField, refresh, dbViewer);
        actions.setAlignment(Pos.CENTER);

        Label inLabel = new Label("Clock In Photo");
        ImageView inImage = new ImageView();
        inImage.setFitWidth(320);
        inImage.setPreserveRatio(true);

        Label outLabel = new Label("Clock Out Photo");
        ImageView outImage = new ImageView();
        outImage.setFitWidth(320);
        outImage.setPreserveRatio(true);

        HBox photos = new HBox(20, new VBox(6, inLabel, inImage), new VBox(6, outLabel, outImage));
        photos.setAlignment(Pos.CENTER);

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) {
                inImage.setImage(null);
                outImage.setImage(null);
                return;
            }
            String inPath = newV[4];
            String outPath = newV[5];
            inImage.setImage(loadImageOrNull(inPath));
            outImage.setImage(loadImageOrNull(outPath));
        });

        VBox layout = new VBox(10, table, photos, actions);
        layout.setPadding(new Insets(12));
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 900, 500);
        recordsStage.setScene(scene);
        recordsStage.setMinWidth(700);
        recordsStage.setMinHeight(400);
        recordsStage.show();
    }

    private void openDbViewerWindow(Stage owner) {
        Stage stage = new Stage();
        stage.setTitle("Database Viewer (Read-Only)");
        if (owner != null) {
            stage.initOwner(owner);
            stage.setOnHidden(ev -> owner.show());
        }

        TextArea sqlInput = new TextArea("SELECT * FROM clock_records ORDER BY id DESC");
        sqlInput.setPrefRowCount(3);

        Label status = new Label();
        Button run = new Button("Run Query");

        TableView<ObservableList<String>> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        run.setOnAction(e -> {
            Datamanager.QueryResult result = datamanager.query(sqlInput.getText());
            table.getColumns().clear();
            table.getItems().clear();
            if (result.error != null) {
                status.setText(result.error);
                return;
            }
            status.setText("");
            for (int i = 0; i < result.columns.size(); i++) {
                final int colIndex = i;
                TableColumn<ObservableList<String>, String> col = new TableColumn<>(result.columns.get(i));
                col.setCellValueFactory(data -> {
                    ObservableList<String> row = data.getValue();
                    String value = colIndex < row.size() ? row.get(colIndex) : "";
                    return new SimpleStringProperty(value);
                });
                table.getColumns().add(col);
            }
            for (var row : result.rows) {
                table.getItems().add(FXCollections.observableArrayList(row));
            }
        });

        HBox controls = new HBox(10, run);
        controls.setAlignment(Pos.CENTER_LEFT);

        VBox layout = new VBox(10, new Label("SQL (SELECT only):"), sqlInput, controls, status, table);
        layout.setPadding(new Insets(12));

        Scene scene = new Scene(layout, 900, 600);
        stage.setScene(scene);
        stage.setMinWidth(700);
        stage.setMinHeight(400);
        stage.show();

        run.fire();
    }

    private Image loadImageOrNull(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        File f = new File(path);
        if (!f.exists()) {
            return null;
        }
        return new Image(f.toURI().toString(), 320, 0, true, true);
    }

    private boolean ensureOpenCvLoaded() {
        if (openCvLoaded) {
            return true;
        }
        try {
            OpenCV.loadLocally();
            openCvLoaded = true;
            return true;
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

    private BufferedImage matToBufferedImage(Mat mat) {
        int type = BufferedImage.TYPE_3BYTE_BGR;
        if (mat.channels() == 1) {
            type = BufferedImage.TYPE_BYTE_GRAY;
        }
        int bufferSize = mat.channels() * mat.cols() * mat.rows();
        byte[] b = new byte[bufferSize];
        mat.get(0, 0, b);
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
        byte[] target = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(b, 0, target, 0, b.length);
        return image;
    }
}
