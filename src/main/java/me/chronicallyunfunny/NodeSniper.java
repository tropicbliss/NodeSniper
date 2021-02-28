package me.chronicallyunfunny;

public class NodeSniper {
    public static void main(String[] args) {
        try {
            Sniper sniper = new MojangSniper();
            sniper.parseConfigFile();
            sniper.printSplashScreen();
            System.out.println("Initialising...");
            System.out.println("");
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
}
