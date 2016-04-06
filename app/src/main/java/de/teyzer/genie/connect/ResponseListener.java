package de.teyzer.genie.connect;


interface ResponseListener {
    /**
     * Wird ausgelöst, wenn eine Status-Antwort vom Server gekommen ist
     *
     * @param sourceAction Die Ursprungsaktion
     * @param response     Die Antwort
     */
    void responseReceived(Action sourceAction, String response);

    /**
     * Wird ausgelöst, wenn eine Datei nicht gefunden wurde, die abgespielt werden sollte.
     */
    void fileNotFound();

}
