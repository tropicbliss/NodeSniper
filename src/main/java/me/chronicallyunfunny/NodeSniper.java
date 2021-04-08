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
            if (args[0] != null)
                sniper = sniperImplChooser(args[0]);
            else
                sniper = sniperImplChooser();
            sniper.printSplashScreen();
            sniper.parseAccountFile();
            sniper.authenticate();
            if (sniper.isSecurityQuestionsNeeded())
                if (sniper.getSecurityQuestionsID())
                    sniper.sendSecurityQuestions();
            sniper.isNameChangeEligible();
            if (args[0] == null)
                sniper.getUsernameChoice();
            sniper.isNameAvailable();
            sniper.checkNameAvailabilityTime();
            if (sniper.parseConfigFile())
                sniper.autoOffsetCalculation();
            sniper.execute();
            // gotta catch them all! ;)
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static Sniper sniperImplChooser() throws IOException {
        var fileName = Path.of("config.yml");
        var actual = Files.readString(fileName);
        var yaml = new Yaml();
        Map<String, Object> accountData = yaml.load(actual);
        if (!(boolean) (accountData.get("microsoftAuth"))) {
            if ((boolean) (accountData.get("GCSnipe"))) {
                System.out.println(
                        "\"microsoftAuth\" is set to false yet \"GCSnipe\" is set to true. Defaulting to gift code sniping instead.");
                return new GCSniper();
            }
            return new MojangSniper();
        } else {
            if ((boolean) (accountData.get("GCSnipe")))
                return new GCSniper();
            return new MSASniper();
        }
    }

    public static Sniper sniperImplChooser(String name) throws IOException {
        var fileName = Path.of("config.yml");
        var actual = Files.readString(fileName);
        var yaml = new Yaml();
        Map<String, Object> accountData = yaml.load(actual);
        if (!(boolean) (accountData.get("microsoftAuth"))) {
            if ((boolean) (accountData.get("GCSnipe"))) {
                System.out.println(
                        "\"microsoftAuth\" is set to false yet \"GCSnipe\" is set to true. Defaulting to gift code sniping instead.");
                return new GCSniper(name);
            }
            return new MojangSniper(name);
        } else {
            if ((boolean) (accountData.get("GCSnipe")))
                return new GCSniper(name);
            return new MSASniper(name);
        }
    }
}
