package me.chronicallyunfunny;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.yaml.snakeyaml.Yaml;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class MSASniper implements Sniper {
    private final HttpClient client = HttpClient.newHttpClient();
    private String authToken = null;
    private String snipedUsername = null;
    private int delay;
    private Instant dropTime;
    private HttpRequest snipeRequest;
    private boolean turboSnipe = false;
    private final AtomicBoolean isSuccessful = new AtomicBoolean(false);
    private final int NO_OF_REQUESTS = 2;
    private final List<CompletableFuture<Void>> completableFutures = new ArrayList<>();
    private final ObjectMapper mapper = new ObjectMapper();
    private final Scanner scanner = new Scanner(System.in);
    private Instant authTime;
    private int spread;

    @Override
    public void printSplashScreen() {
        System.out.println("    _   __          __    _____       _                ");
        System.out.println("   / | / /___  ____/ /__ / ___/____  (_)___  ___  _____");
        System.out.println("  /  |/ / __ \\/ __  / _ \\\\__ \\/ __ \\/ / __ \\/ _ \\/ ___/");
        System.out.println(" / /|  / /_/ / /_/ /  __/__/ / / / / / /_/ /  __/ /    ");
        System.out.println("/_/ |_/\\____/\\__,_/\\___/____/_/ /_/_/ .___/\\___/_/     ");
        System.out.println("                                   /_/                 ");
        System.out.println();
        System.out.println("Developed by @chronicallyunfunny#1113 on Discord.");
        System.out.println();
    }

    @Override
    public void getUsernameChoice() {
        System.out.print("What name will you like to snipe: ");
        snipedUsername = scanner.next();
        if ((snipedUsername.length() < 3) || (snipedUsername.length() > 16) || (!(snipedUsername.matches("[A-Za-z0-9_]+"))))
            throw new GeneralSniperException("[GetUsernameChoice] You entered an invalid username.");
    }

    @Override
    public void parseAccountFile() {
    }

    @Override
    public void parseConfigFile() throws IOException {
        var fileName = Path.of("config.yml");
        var actual = Files.readString(fileName);
        var yaml = new Yaml();
        Map<String, Object> accountData = yaml.load(actual);
        delay = (int) accountData.get("delay");
        spread = (int) accountData.get("spread");
        System.out.println("Delay is set to " + delay + " ms.");
    }

    @Override
    public void isNameAvailable() throws URISyntaxException, IOException, InterruptedException {
        var uri = new URI("https://api.mojang.com/user/profile/agent/minecraft/name/" + snipedUsername);
        var request = HttpRequest.newBuilder().uri(uri).GET().build();
        var response = client.send(request, HttpResponse.BodyHandlers.discarding());
        switch (response.statusCode()) {
            case 204:
                return;
            case 200:
                throw new GeneralSniperException("[NameAvailabilityChecker] Name has been taken.");
            default:
                throw new GeneralSniperException("[NameAvailabilityChecker] HTTP status code: " + response.statusCode());
        }
    }

    @Override
    public void execute() throws URISyntaxException {
        var timer2 = new Timer();
        TimerTask snipe = new TimerTask() {
            @Override
            public void run() {
                try {
                    for (var request = 1; request <= NO_OF_REQUESTS; request++) {
                        var snipe = client.sendAsync(snipeRequest, HttpResponse.BodyHandlers.discarding()).thenApply(HttpResponse::statusCode).thenAccept(code -> {
                            var now = Instant.now();
                            var accurateDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").withZone(ZoneId.systemDefault());
                            var accurateTime = accurateDateFormat.format(now);
                            var keyword = "fail";
                            if (code == 200) {
                                isSuccessful.set(true);
                                keyword = "success";
                            }
                            System.out.println("[" + keyword + "] " + code + " @ " + accurateTime);
                        });
                        completableFutures.add(snipe);
                        Thread.sleep(spread);
                    }
                    CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).join();
                    if (isSuccessful.get())
                        System.out.println("You have successfully sniped the name " + snipedUsername + ".");
                    System.out.print("Press enter to quit: ");
                    System.in.read();
                    timer2.cancel();
                    System.exit(0);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        // I've given up on making the code clean.
        if (turboSnipe) {
            System.out.println("Warning: Some usernames may show up as available but has been blocked by Mojang. Sniping it will not work.");
            var uri = new URI("https://api.minecraftservices.com/minecraft/profile/namechange");
            snipeRequest = HttpRequest.newBuilder().uri(uri).header("Authorization", "Bearer " + authToken).PUT(HttpRequest.BodyPublishers.noBody()).build();
            System.out.println("Setup complete!");
            snipe.run();
        }
        var now = Instant.now();
        var semiAccurateDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
        var niceDropTime = semiAccurateDateFormat.format(dropTime);
        var diffInTime = (dropTime.getEpochSecond() - now.getEpochSecond()) / 60;
        System.out.println("Sniping " + snipedUsername + " in ~" + diffInTime + " minutes | sniping at " + niceDropTime + ".");
        var authenticationTime = Date.from(dropTime.minusSeconds(60));
        var delayAdjustedTime = Date.from(dropTime.minusMillis(delay));
        var timer1 = new Timer();
        final TimerTask authentication = new TimerTask() {
            @Override
            public void run() {
                try {
                    authenticate();
                    if (isSecurityQuestionsNeeded())
                        if (getSecurityQuestionsID())
                            sendSecurityQuestions();
                    isNameChangeEligible();
                    isNameAvailable();
                    var uri = new URI("https://api.minecraftservices.com/minecraft/profile/name/" + snipedUsername);
                    snipeRequest = HttpRequest.newBuilder().uri(uri).header("Authorization", "Bearer " + authToken).PUT(HttpRequest.BodyPublishers.noBody()).build();
                    System.out.println("Setup complete!");
                    timer1.cancel();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        if (new Date().before(authenticationTime))
            timer1.schedule(authentication, authenticationTime);
        else {
            var uri = new URI("https://api.minecraftservices.com/minecraft/profile/namechange");
            snipeRequest = HttpRequest.newBuilder().uri(uri).header("Authorization", "Bearer " + authToken).PUT(HttpRequest.BodyPublishers.noBody()).build();
            System.out.println("Setup complete!");
        }
        timer2.schedule(snipe, delayAdjustedTime);
    }

    @Override
    public void isNameChangeEligible() throws URISyntaxException, IOException, InterruptedException {
        var uri = new URI("https://api.minecraftservices.com/minecraft/profile/namechange");
        var request = HttpRequest.newBuilder().uri(uri).header("Authorization", "Bearer " + authToken).GET().build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (!(response.statusCode() == 200))
            throw new GeneralSniperException("[NameChangeEligibilityChecker] HTTP status code: " + response.statusCode());
        System.out.println("Signed into your account successfully.");
        var body = response.body();
        var node = mapper.readTree(body);
        boolean isAllowed = node.get("nameChangeAllowed").asBoolean();
        if (!isAllowed)
            throw new GeneralSniperException("[NameChangeEligibilityChecker] You cannot name change within the cooldown period.");
    }

    @Override
    public void checkNameAvailabilityTime() throws URISyntaxException, IOException, InterruptedException {
        var uri = new URI("https://api.nathan.cx/check/" + snipedUsername);
        var request = HttpRequest.newBuilder().uri(uri).GET().build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (!(response.statusCode() == 200))
            throw new GeneralSniperException("[CheckNameAvailabilityTime] HTTP status code: " + response.statusCode());
        var body = response.body();
        var node = mapper.readTree(body);
        try {
            dropTime = Instant.from(DateTimeFormatter.ISO_INSTANT.parse(node.get("drop_time").asText()));
            if (dropTime.getEpochSecond() - authTime.getEpochSecond() > 86_400)
                throw new GeneralSniperException("[CheckNameAvailabilityTime] You cannot snipe a name available more than one day later if you are using a Microsoft account.");
        } catch (NullPointerException ex) {
            turboSnipe = true;
        }
    }

    @Override
    public boolean isSecurityQuestionsNeeded() {
        return false;
    }

    @Override
    public void sendSecurityQuestions() {
    }

    @Override
    public boolean getSecurityQuestionsID() {
        return false;
    }

    @Override
    public void authenticate() throws URISyntaxException, IOException, InterruptedException {
        System.out.println("Opening browser...");
        // Gives the user an illusion that something is happening.
        Thread.sleep(3_000);
        var uri = new URI("https://login.live.com/oauth20_authorize.srf?client_id=9abe16f4-930f-4033-b593-6e934115122f&response_type=code&redirect_uri=https%3A%2F%2Fmicroauth.tk%2Ftoken&scope=XboxLive.signin%20XboxLive.offline_access");
        try {
            Desktop.getDesktop().browse(uri);
        } catch (HeadlessException ex) {
            System.out.println("Looks like you are running this program in a headless environment. Copy the following URL into your browser:");
            System.out.println("https://login.live.com/oauth20_authorize.srf?client_id=9abe16f4-930f-4033-b593-6e934115122f&response_type=code&redirect_uri=https%3A%2F%2Fmicroauth.tk%2Ftoken&scope=XboxLive.signin%20XboxLive.offline_access");
        }
        System.out.println("Please make sure that your snipe will not last more than a day or the snipe will fail.");
        System.out.print("Sign in with your Microsoft account and copy the ID from the \"access_token\" field right here: ");
        String accessTokenStr = scanner.next();
        authTime = Instant.now();
        if (accessTokenStr.charAt(0) == '"')
            accessTokenStr = accessTokenStr.substring(1);
        if (accessTokenStr.endsWith("\""))
            accessTokenStr = accessTokenStr.substring(0, accessTokenStr.length() - 2);
        authToken = accessTokenStr;
    }
}
