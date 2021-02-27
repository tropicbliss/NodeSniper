package me.chronicallyunfunny;

import java.io.IOException;
import java.net.URISyntaxException;

// interface created so I can create an alternative implementation of this sniper with Microsoft account support
public interface Sniper {
    void authenticate() throws URISyntaxException, IOException, InterruptedException;
    void parseAccountFile() throws IOException, URISyntaxException;
    boolean isSecurityQuestionsNeeded() throws URISyntaxException, IOException, InterruptedException;
    void sendSecurityQuestions() throws URISyntaxException, IOException, InterruptedException;
    void getSecurityQuestionsID() throws URISyntaxException, IOException, InterruptedException;
    void printSplashScreen();
    void getUsernameChoice();
    void parseConfigFile() throws IOException;
    void execute() throws URISyntaxException;
    void checkNameAvailabilityTime() throws URISyntaxException, IOException, InterruptedException;
    void isNameAvailable() throws URISyntaxException, IOException, InterruptedException;
    void isNameChangeEligible() throws URISyntaxException, IOException, InterruptedException;
}
