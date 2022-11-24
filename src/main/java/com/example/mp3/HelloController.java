package com.example.mp3;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HelloController implements Initializable {
    @FXML
    private Pane pane;
    @FXML
    private Button playButton, pauseButton, resetButton, previousButton, nextButton, backButton, forwardButton, addMusic, deleteButton,btn;
    @FXML
    private ComboBox<String> speedBox;
    @FXML
    private Slider volumeSlider, progressSlider;
    @FXML
    private ProgressBar songProgressBar;
    @FXML
    private Label timeCurrent,songDuration, songLabel, nextSongLabel;
    @FXML
    private ListView<String> playlist;
    private Media media;
    private MediaPlayer mediaPlayer;
    private File directory;
    private File[] files;
    private ArrayList<File> songs;
    private int songNumber;
    private int[] speeds = {25, 50, 75, 100, 125, 150, 175, 200};
    private Timer timer;
    private TimerTask task;
    private boolean running;

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        File theDir = new File("/C:/Music");
        if (!theDir.exists()){
            theDir.mkdirs();
        }
        songs = new ArrayList<File>();
        directory = new File("C:\\Music");
        files = directory.listFiles();

        if (files != null && files.length > 0) {
            songs.addAll(Arrays.asList(files));
        }
        else{
            resetButton.setDisable(true);
            playButton.setDisable(true);
            nextButton.setDisable(true);
            previousButton.setDisable(true);
            pauseButton.setDisable(true);
            speedBox.setDisable(true);
            backButton.setDisable(true);
            forwardButton.setDisable(true);
        }

        for (int i = 0; i < speeds.length; i++) {
            speedBox.getItems().add(Integer.toString(speeds[i]) + "%");
        }
        playlist.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {

                String test = playlist.getSelectionModel().getSelectedItem();
                System.out.println(test);
                songNumber = playlist.getSelectionModel().getSelectedIndex() - 1;
                nextMedia();
            }
        });
        if(songs.size() == 0){
            System.out.println("No music");
        }else {
            creator();
            progress();
            nextSongName();
            speedBox.setOnAction(this::changeSpeed);
            volumeSlider.valueProperty().addListener((observableValue, number, t1) -> mediaPlayer.setVolume(volumeSlider.getValue() * 0.01));

            songProgressBar.setStyle("-fx-accent : black");
            speedBox.setStyle("-fx-font: 12px \"Arial Black\";");
            playlist.setStyle("-fx-font: 10px \"Arial Black\";");
            //region Playlist
            String[] names = new String[files.length];
            for (int i = 0; i < files.length; i++) {
                names[i] = files[i].getName();
                playlist.getItems().addAll(names[i].replaceAll(".mp3", ""));
            }

            //endregion

            //region Autoplay "Timer"
            Runnable helloRunnable = new Runnable() {
                public void run() {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            autoplay();
                        }
                    });
                }
            };
            ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
            executor.scheduleAtFixedRate(helloRunnable, 1, 5, TimeUnit.SECONDS);
            //endregion
        }
}
    public void musicAdd(ActionEvent event){
        //You can add more music to your playlist
        FileChooser fc = new FileChooser();
        //delete comment to set specific directory for the FileChooser
        //fc.setInitialDirectory(new File("pathname"));
        fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("MP3 file","*.mp3"));
        List<File> selectedFiles = fc.showOpenMultipleDialog(null);
        if(selectedFiles != null && selectedFiles.size() > 0){
            resetButton.setDisable(false);
            playButton.setDisable(false);
            nextButton.setDisable(false);
            previousButton.setDisable(false);
            pauseButton.setDisable(false);
            speedBox.setDisable(false);
            forwardButton.setDisable(false);
            backButton.setDisable(false);
            speedBox.setDisable(false);
            for (File selectedFile : selectedFiles) {
                songs.add(new File(selectedFile.getPath()));

                File sourceFile = new File(selectedFile.getPath());
                //pathname can be changed to your own one
                File destinationFile = new File("/C:/Music/" + selectedFile.getName());
                FileInputStream fileInputStream = null;
                try {
                    fileInputStream = new FileInputStream(sourceFile);

                FileOutputStream fileOutputStream = new FileOutputStream(
                        destinationFile);
                int bufferSize;
                byte[] bufffer = new byte[512];
                while ((bufferSize = fileInputStream.read(bufffer)) > 0) {
                    fileOutputStream.write(bufffer, 0, bufferSize);
                }
                fileInputStream.close();
                fileOutputStream.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                playlist.getItems().add(selectedFile.getName().replaceAll(".mp3", ""));
            }
        }
        else{
            System.out.println("file is not valid");
        }
    }
    public void autoplay(){
        //AlGoRiTmmmHm which plays next song, one after another
    if(formatDuration(mediaPlayer.getCurrentTime()).equals(formatDuration(media.getDuration()))){
        System.out.println("swaws");
        nextMedia();
    }
}
    public void creator(){
        //Creates new media and mediaPlayer every time when needed
            var sn = songNumber;
            if(songNumber < 0 || songNumber > songs.size() - 1){
                sn = 0;
            }
            media = new Media(songs.get(sn).toURI().toString());
            mediaPlayer = new MediaPlayer(media);

            songLabel.setText(songs.get(sn).getName().replaceAll(".mp3", ""));
        }
    public void nextSongName(){
        //AlGoRiTmmmHm which shows next song
        if (songNumber + 1 == songs.size()) {
            nextSongLabel.setText(songs.get(0).getName().replaceAll(".mp3",""));
        } else {
            nextSongLabel.setText(songs.get(songNumber + 1).getName().replaceAll(".mp3",""));
        }
    }
    public void progress(){
        //Progress bar functions
        mediaPlayer.currentTimeProperty().addListener(new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
                    progressSlider.setValue(newValue.toSeconds());
                    timeCurrent.setText(formatDuration(newValue));
            }
        });

        progressSlider.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                mediaPlayer.seek(Duration.seconds(progressSlider.getValue()));

            }
        });

    progressSlider.setOnMouseDragged(new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent mouseEvent) {
            mediaPlayer.seek(Duration.seconds(progressSlider.getValue()));

        }
    });
        mediaPlayer.setOnReady(new Runnable() {
            @Override
            public void run() {
                Duration total = media.getDuration();
                progressSlider.setMax(total.toSeconds());
                songDuration.setText(formatDuration(total));
            }
        });
    }

    public static String formatDuration(Duration duration) {
        // Allow to show correct Duration of media
        long seconds = (long) duration.toSeconds();
        long absSeconds = Math.abs(seconds);
        String positive = String.format(
                "%02d:%02d",
                (absSeconds % 3600) / 60,
                absSeconds % 60);
        return seconds < 0 ? "-" + positive : positive;
    }
    public void playMedia() {
        creator();
        nextSongName();
        progress();
        beginTimer();
        changeSpeed(null);
        mediaPlayer.setVolume(volumeSlider.getValue() * 0.01);
        mediaPlayer.play();
        System.out.println(songNumber);
        }

    public void pauseMedia() {
        cancelTimer();
        mediaPlayer.pause();
    }

    public void previousMedia() {
        double current = mediaPlayer.getCurrentTime().toSeconds();
        if(current > 5){
            resetMedia();
        }
        else if (songNumber > 0) {
            songNumber--;
            mediaPlayer.stop();
            if (running) {
                cancelTimer();
            }
            creator();
            playMedia();
        }
        else if(songNumber == 0){
            mediaPlayer.stop();
            if (running) {
                cancelTimer();
            }
            songNumber = songs.size() - 1;
            creator();
            playMedia();
        }
                else {
            songNumber = 0;
            mediaPlayer.stop();
            if (running) {
                cancelTimer();
            }
            creator();
            playMedia();

        }
    }

    public void nextMedia() {
            if(songNumber < songs.size() - 1) {
                songNumber++;
                if(mediaPlayer != null)
                mediaPlayer.stop();
                if(running) {
                    cancelTimer();
                }
            }
            else {
                songNumber = 0;
                if(mediaPlayer != null)
                mediaPlayer.stop();
            }
            creator();
            playMedia();
        }
        public void resetMedia(){
            songProgressBar.setProgress(0);
            mediaPlayer.seek(Duration.seconds(0));
            playMedia();
        }
        public void changeSpeed(ActionEvent event){
                if(speedBox.getValue() == null){
                    mediaPlayer.setRate(1);
                }
                else {
                    mediaPlayer.setRate(Integer.parseInt(speedBox.getValue().substring(0, speedBox.getValue().length() - 1)) * 0.01);
                }
        }
        public void beginTimer(){
            timer = new Timer();
            task = new TimerTask() {
                public void run(){
                    running = true;
                    double current = mediaPlayer.getCurrentTime().toSeconds();
                    double end = media.getDuration().toSeconds();
                    songProgressBar.setProgress(current/end);
                    if(current/end == 1){
                        cancelTimer();
                    }
                }
            };
            timer.schedule(task, 0, 1000);
        }
        public void cancelTimer() {
            if (timer == null) {
                System.out.println("lol");
            } else {
                running = false;
                timer.cancel();
            }
        }
        public void goBack(ActionEvent event) {
        mediaPlayer.seek(mediaPlayer.getCurrentTime().add(Duration.seconds(-10)));
        }
        public void Forward(ActionEvent event){
            mediaPlayer.seek(mediaPlayer.getCurrentTime().add(Duration.seconds(10)));
        }
}