package de.teyzer.genie.connect;

import android.media.MediaMetadataRetriever;
import android.net.Uri;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Action {
    private static final int BUFFER_SIZE = 8192;
    private Uri fileUri;
    private long fileLength = 0;

    private String command;
    private String[] args;

    private ResponseListener listener;
    private UploadStatusListener uploadStatusListener;


    public Action(Uri fileUri, String command, ResponseListener listener, UploadStatusListener uploadStatusListener) {
        this.fileUri = fileUri;
        this.command = command;
        this.listener = listener;
        this.uploadStatusListener = uploadStatusListener;
    }

    public Action(String command, String[] args, ResponseListener listener) {
        this.command = command;
        this.args = args;
        this.listener = listener;
    }

    public static Action play(ResponseListener listener) {
        return new Action("play", null, listener);
    }

    public static Action pause(ResponseListener listener) {
        return new Action("pause", null, listener);
    }

    public static Action playFile(Uri uri, final ResponseListener listener, UploadStatusListener uploadStatusListener) {
        return new Action(uri, "playFile", listener, uploadStatusListener);
    }

    public static Action togglePlayPause(ResponseListener listener) {
        return new Action("togglePlayPause", null, listener);
    }

    public static Action playFiles(ArrayList<Uri> uris, ResponseListener listener, UploadStatusListener uploadStatusListener) {
        return new Action("", null, listener);
    }

    public static Action setShuffle(boolean shuffle, ResponseListener listener) {
        return new Action("setShuffle", new String[]{String.valueOf(shuffle)}, listener);
    }

    public static Action setRepeatMode(int repeatMode, ResponseListener listener) {
        return new Action("setRepeatMode", new String[]{String.valueOf(repeatMode)}, listener);
    }

    public static Action playNext(ResponseListener listener) {
        return new Action("playNext", null, listener);
    }

    public static Action playPrevious(ResponseListener listener) {
        return new Action("playPrevious", null, listener);
    }

    public static Action setBrightness(ResponseListener listener) {
        return new Action("setBrightness", null, listener);
    }

    public static Action changeVisualisation(ResponseListener listener) {
        return new Action("changeVisualization", null, listener);
    }

    public static Action getStatus(ResponseListener listener) {
        return new Action("getStatus", null, listener);
    }

    public void execute(Socket socket) throws IOException {
        //Befehlsstring basteln

        String action = getCommandString();
        if (action == null)
            return;

        //In bytes kodieren
        byte[] commandBytes = action.getBytes(StandardCharsets.UTF_8);

        //abschicken
        OutputStream os = socket.getOutputStream();
        os.write(commandBytes);

        //Datei hochladen, falls notwendig
        if (command.equals("uploadAndPlayFile")) {
            uploadFile(os);
        }

        //Antwort lesen
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

        String response = in.readLine();

        if (command.equals("playFile") && response.equals("file not found")) {
            new Action(fileUri, "uploadAndPlayFile", listener, uploadStatusListener).execute(socket);
        } else if (listener != null) {
            listener.responseReceived(this, response);
        }
    }

    private String getCommandString() {
        String action = command;
        if (command.equals("playFile")) {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(fileUri.getPath());

            String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);

            action += ";" + title;
            action += ";" + artist;

        } else if (command.equals("uploadAndPlayFile")) {
            File fileToSend = new File(fileUri.getPath());

            if (fileToSend.exists() && fileToSend.isFile()) {

                fileLength = fileToSend.length();
                String fileName = fileToSend.getName();

                action += ";" + fileName;
                action += ";" + fileLength;
                System.out.println("File upload size: " + fileLength);
            } else {
                System.out.println("invalid file, cant upload");
                return null;
            }
        } else {
            if (args != null) {
                for (String arg : args) {
                    action += ";" + arg;
                }
            }
        }
        action += '\n';
        return action;
    }

    private void uploadFile(OutputStream os) throws IOException {
        FileInputStream fis = new FileInputStream(fileUri.getPath());
        byte[] buffer = new byte[BUFFER_SIZE];

        int totalUploadedBytes = 0;

        int readByteCount = fis.read(buffer);
        int iterationCount = 0;
        while (readByteCount > 0) {
            totalUploadedBytes += readByteCount;
            os.write(buffer, 0, readByteCount);

            readByteCount = fis.read(buffer);

            iterationCount++;

            if (uploadStatusListener != null && iterationCount > 20) {
                int statusPercent = Math.min((int) (((totalUploadedBytes * 1.0) / fileLength) * 100), 100);

                String status = String.format("%s hochgeladen (%s/%s)", statusPercent + "%", totalUploadedBytes, fileLength);

                uploadStatusListener.updateStatus(status, statusPercent);
                iterationCount = 0;
            }
        }
        fis.close();
        if (uploadStatusListener != null) {
            uploadStatusListener.updateStatus("finished", 101);
        }
        System.out.println("bytes sent: " + totalUploadedBytes);
    }
}
