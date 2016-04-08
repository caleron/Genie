package de.teyzer.genie.connect;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.Queue;

import de.teyzer.genie.data.Prefs;

public class ServerConnect implements Prefs {

    private static int hostPort = 4732;
    private static String hostIp = "192.168.1.2";

    private final Queue<Action> taskQueue = new LinkedList<>();

    private Socket socket;

    private Thread requestThread;

    private final Activity activity;
    private long lastNoConnectionAlertTime;

    public ServerConnect(Activity activity) {
        this.activity = activity;
        refreshPrefs();
    }

    public void refreshPrefs() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);

        hostIp = sharedPreferences.getString(PREF_HOST_ADRESS, hostIp);
        try {
            hostPort = Integer.parseInt(sharedPreferences.getString(PREF_HOST_PORT, String.valueOf(hostPort)));
        } catch (Exception ignored) {
        }
    }

    /**
     * Führt eine Aktion asynchron aus.
     *
     * @param action Die auszuführende Aktion
     */
    public void executeAction(Action action) {
        taskQueue.add(action);
        if (requestThread == null || !requestThread.isAlive()) {
            requestThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    doRequests();
                }
            });
            requestThread.start();
        }
    }

    private void connect() {
        if (socket == null || !socket.isConnected() || socket.isClosed()) {
            try {
                if (socket != null) {
                    socket.close();
                }
                socket = new Socket(hostIp, hostPort);
                socket.setSoTimeout(5000);

            } catch (IOException e) {
                if (!(e instanceof ConnectException)) {
                    e.printStackTrace();
                }

                long currentTime = System.nanoTime();
                final long timeout = 5000000000L; //entspricht 5 sekunden
                if (lastNoConnectionAlertTime + timeout < currentTime) {
                    lastNoConnectionAlertTime = currentTime;

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity, "Fehler: Verbindung fehlgeschlagen", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }
    }

    private void doRequests() {
        connect();
        int waitCounter = 0;

        while (waitCounter < 100 && !requestThread.isInterrupted()) {
            if (socket == null)
                return;

            if (taskQueue.isEmpty()) {
                waitCounter++;
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ignored) {
                    break;
                }
                continue;
            }

            waitCounter = 0;
            Action action = null;
            //Aktion aus der Queue nehmen
            if (!taskQueue.isEmpty()) {
                action = taskQueue.poll();
            }

            if (action != null) {
                try {
                    action.execute(socket);
                    //System.out.println("action executed");
                } catch (SocketException e) {
                    //Verbindung trennen, neu aufbauen und ein zweites Mal die Aktion ausführen
                    try {
                        socket.close();
                        System.out.println("action failed, reconnect to server and retry");
                        connect();
                        action.execute(socket);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (socket != null) {
            try {
                socket.close();
                System.out.println("socket closed after timeout");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void disconnect() {
        try {
            System.out.println("disconnect now");
            if (socket != null) {
                socket.close();
            }
            if (requestThread != null) {
                requestThread.interrupt();
            }
            taskQueue.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
