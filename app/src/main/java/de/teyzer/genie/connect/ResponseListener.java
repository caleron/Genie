package de.teyzer.genie.connect;


public interface ResponseListener {
    void responseReceived(Action sourceAction, String response);
}
