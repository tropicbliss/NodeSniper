package me.chronicallyunfunny;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class NodeSniper {
    public static void main(String[] args) {
        try {
            Sniper sniper;
            sniper = isMicrosoftAccount() ? new MSASniper() : new MojangSniper();
            sniper.printSplashScreen();
            System.out.println("Initialising...");
            System.out.println();
            sniper.parseAccountFile();
            sniper.authenticate();
            if (sniper.isSecurityQuestionsNeeded())
                if (sniper.getSecurityQuestionsID())
                    sniper.sendSecurityQuestions();
            sniper.isNameChangeEligible();
            sniper.getUsernameChoice();
            sniper.isNameAvailable();
            sniper.checkNameAvailabilityTime();
            sniper.parseConfigFile();
            sniper.execute();
        // gotta catch them all! ;)
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static boolean isMicrosoftAccount() throws IOException {
        var fileName = Path.of("config.yml");
        var actual = Files.readString(fileName);
        var yaml = new Yaml();
        Map<String, Object> accountData = yaml.load(actual);
        return (boolean) accountData.get("microsoftAuth");
    }
}
