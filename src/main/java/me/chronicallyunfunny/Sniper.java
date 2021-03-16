package me.chronicallyunfunny;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.util.Map;

public interface Sniper {
    void authenticate() throws URISyntaxException, IOException, InterruptedException;

    void parseAccountFile() throws IOException, URISyntaxException;

    boolean isSecurityQuestionsNeeded() throws URISyntaxException, IOException, InterruptedException;

    void sendSecurityQuestions() throws URISyntaxException, IOException, InterruptedException;

    boolean getSecurityQuestionsID() throws URISyntaxException, IOException, InterruptedException;

    void printSplashScreen();

    void getUsernameChoice();

    boolean parseConfigFile() throws IOException;

    void execute() throws URISyntaxException;

    void checkNameAvailabilityTime() throws URISyntaxException, IOException, InterruptedException;

    void isNameAvailable() throws URISyntaxException, IOException, InterruptedException;

    void isNameChangeEligible() throws URISyntaxException, IOException, InterruptedException;

    void autoOffsetCalculation() throws URISyntaxException, IOException, InterruptedException;

    HttpRequest.BodyPublisher ofMimeMultipartData(Map<Object, Object> data, String boundary) throws IOException;
}
