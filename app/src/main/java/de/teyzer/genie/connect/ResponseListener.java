package de.teyzer.genie.connect;


public interface ResponseListener {
    /**
     * Wird ausgelöst, wenn eine Status-Antwort vom Server gekommen ist
     *
     * @param sourceAction Die Ursprungsaktion
     * @param response     Die Antwort
     */
    void responseReceived(Action sourceAction, String response);
}
