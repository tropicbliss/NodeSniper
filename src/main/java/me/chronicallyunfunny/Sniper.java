package me.chronicallyunfunny;

import java.io.IOException;
import java.net.URISyntaxException;

public interface Sniper {
    void authenticate() throws URISyntaxException, IOException, InterruptedException;

    void parseAccountFile() throws IOException, URISyntaxException;

    boolean isSecurityQuestionsNeeded() throws URISyntaxException, IOException, InterruptedException;

    void sendSecurityQuestions() throws URISyntaxException, IOException, InterruptedException;

    boolean getSecurityQuestionsID() throws URISyntaxException, IOException, InterruptedException;

    void printSplashScreen();

    void getUsernameChoice();

    boolean parseConfigFile() throws IOException;

    void execute() throws URISyntaxException, InterruptedException, IOException;

    void checkNameAvailabilityTime() throws URISyntaxException, IOException, InterruptedException;

    void isNameAvailable() throws URISyntaxException, IOException, InterruptedException;

    void isNameChangeEligible() throws URISyntaxException, IOException, InterruptedException;

    void autoOffsetCalculation() throws URISyntaxException, IOException, InterruptedException;
}
