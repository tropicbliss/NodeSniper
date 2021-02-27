package me.chronicallyunfunny;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.yaml.snakeyaml.Yaml;

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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class MojangSniper implements Sniper {
    private final HttpClient client = HttpClient.newHttpClient();
    private String username = null;
    private String password = null;
    private String sq1 = null;
    private String sq2 = null;
    private String sq3 = null;
    private String authToken = null;
    private String snipedUsername = null;
    private final int[] questionIDArray = new int[3];
    private int delay;
    private Instant dropTime;
    private HttpRequest snipeRequest;
    private boolean turboSnipe = false;

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
        var scanner = new Scanner(System.in);
        System.out.print("What name will you like to snipe: ");
        snipedUsername = scanner.next();
        System.out.println("Warning: Some usernames may show up as available but has been blocked by Mojang. Sniping it will not work.");
    }

    @Override
    public void parseAccountFile() throws IOException {
        var fileName = Path.of("account.yml");
        var actual = Files.readString(fileName);
        var yaml = new Yaml();
        Map<String, String> accountData = yaml.load(actual);
        username = accountData.get("username");
        // a hack to protect against noob users whose passwords are made up of all numbers
        password = String.valueOf(accountData.get("password"));
        sq1 = accountData.get("sq1");
        sq2 = accountData.get("sq2");
        sq3 = accountData.get("sq3");
        if ((username == null) || (password == null))
            throw new GeneralSniperException("[ParseAccountFile] The username or password field in account.yml is empty.");
    }

    @Override
    public void parseConfigFile() throws IOException {
        var fileName = Path.of("config.yml");
        var actual = Files.readString(fileName);
        var yaml = new Yaml();
        Map<String, Integer> accountData = yaml.load(actual);
        delay = accountData.get("delay");
        System.out.println("Delay is set to " + delay + "ms.");
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
        }
        throw new GeneralSniperException("[NameAvailabilityChecker] HTTP status code: " + response.statusCode());
    }

    @Override
    public void execute() throws URISyntaxException {
        var timer2 = new Timer();
        TimerTask snipe = new TimerTask() {
            @Override
            public void run() {
                try {
                    final var NO_OF_REQUESTS = 2;
                    var accurateDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").withZone(ZoneId.systemDefault());
                    AtomicBoolean isSuccessful = new AtomicBoolean(false);
                    List<CompletableFuture<Void>> completableFutures = new ArrayList<>();
                    for (var request = 1; request <= NO_OF_REQUESTS; request++) {
                        var now = Instant.now();
                        var snipe = client.sendAsync(snipeRequest, HttpResponse.BodyHandlers.discarding()).thenApply(HttpResponse::statusCode).thenAccept(code -> {
                            var accurateTime = accurateDateFormat.format(now);
                            var keyword = "fail";
                            if (code == 200) {
                                isSuccessful.set(true);
                                keyword = "success";
                            }
                            System.out.println("[" + keyword + "] " + code + " @ " + accurateTime);
                        });
                        completableFutures.add(snipe);
                    }
                    CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).join();
                    if (isSuccessful.get())
                        System.out.println("You have successfully sniped the name " + snipedUsername);
                    System.out.print("Press enter to quit: ");
                    System.in.read();
                    timer2.cancel();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        // I've given up on making the code clean.
        if (turboSnipe) {
            var uri = new URI("https://api.minecraftservices.com/minecraft/profile/namechange");
            snipeRequest = HttpRequest.newBuilder().uri(uri).header("Authorization", "Bearer " + authToken).PUT(HttpRequest.BodyPublishers.noBody()).build();
            snipe.run();
            System.exit(0);
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
                    if (isSecurityQuestionsNeeded()) {
                        getSecurityQuestionsID();
                        sendSecurityQuestions();
                    }
                    isNameChangeEligible();
                    isNameAvailable();
                    System.out.println("Signed in to " + username + ".");
                    var uri = new URI("https://api.minecraftservices.com/minecraft/profile/name/" + snipedUsername);
                    snipeRequest = HttpRequest.newBuilder().uri(uri).header("Authorization", "Bearer " + authToken).PUT(HttpRequest.BodyPublishers.noBody()).build();
                    System.out.println("Setup complete!");
                    timer1.cancel();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        if (new Date().after(authenticationTime))
            authentication.run();
        else
            timer1.schedule(authentication, authenticationTime);
        timer2.schedule(snipe, delayAdjustedTime);
    }

    @Override
    public void isNameChangeEligible() throws URISyntaxException, IOException, InterruptedException {
        var uri = new URI("https://api.minecraftservices.com/minecraft/profile/namechange");
        var request = HttpRequest.newBuilder().uri(uri).header("Authorization", "Bearer " + authToken).GET().build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (!(response.statusCode() == 200))
            throw new GeneralSniperException("[NameChangeEligibilityChecker] HTTP status code: " + response.statusCode());
        var body = response.body();
        var node = new ObjectMapper().readValue(body, ObjectNode.class);
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
        var node = new ObjectMapper().readValue(body, ObjectNode.class);
        try {
            dropTime = Instant.from(DateTimeFormatter.ISO_INSTANT.parse(node.get("drop_time").asText()));
        } catch (NullPointerException ex) {
            turboSnipe = true;
        }
    }

    @Override
    public boolean isSecurityQuestionsNeeded() throws URISyntaxException, IOException, InterruptedException {
        var uri = new URI("https://api.mojang.com/user/security/location");
        var request = HttpRequest.newBuilder().uri(uri).header("Authorization", "Bearer " + authToken).GET().build();
        var response = client.send(request, HttpResponse.BodyHandlers.discarding());
        switch (response.statusCode()) {
            case 204:
                return false;
            case 403:
                return true;
        }
        throw new GeneralSniperException("[SecurityQuestionsCheck] HTTP status code: " + response.statusCode());
    }

    @Override
    public void sendSecurityQuestions() throws URISyntaxException, IOException, InterruptedException {
        var postJSON = "[{\"id\":" + questionIDArray[0] + ",\"answer\":\"" + sq1 + "\"},{\"id\":" + questionIDArray[1] + ",\"answer\":\"" + sq2 + "\"},{\"id\":" + questionIDArray[2] + ",\"answer\":\"" + sq3 + "\"}]";
        var uri = new URI("https://api.mojang.com/user/security/location");
        var request = HttpRequest.newBuilder().uri(uri).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(postJSON)).build();
        var response = client.send(request, HttpResponse.BodyHandlers.discarding());
        if (!(response.statusCode() == 204))
            throw new GeneralSniperException("[SendSecurityQuestions] HTTP status code: " + response.statusCode());
    }

    @Override
    public void getSecurityQuestionsID() throws URISyntaxException, IOException, InterruptedException {
        var uri = new URI("https://api.mojang.com/user/security/challenges");
        var request = HttpRequest.newBuilder().uri(uri).header("Authorization", "Bearer " + authToken).GET().build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (!(response.statusCode() == 200))
            throw new GeneralSniperException("[GetSecurityQuestions] HTTP status code: " + response.statusCode());
        var body = response.body();
        var node = new ObjectMapper().readValue(body, ObjectNode.class);
        for (var index = 0; index <= 2; index++)
            questionIDArray[index] = node.get(index).get("answer").get("id").asInt();
    }

    @Override
    public void authenticate() throws URISyntaxException, IOException, InterruptedException {
        var postJSON = "{\"agent\":{\"name\":\"Minecraft\",\"version\":1},\"username\":\"" + username + "\",\"password\":\"" + password + "\",\"clientToken\":\"Mojang-API-Client\",\"requestUser\":\"true\"}";
        var uri = new URI("https://authserver.mojang.com/authenticate");
        var request = HttpRequest.newBuilder().uri(uri).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(postJSON)).build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (!(response.statusCode() == 200))
            throw new GeneralSniperException("[Authentication] HTTP status code: " + response.statusCode());
        var body = response.body();
        var node = new ObjectMapper().readValue(body, ObjectNode.class);
        authToken = node.get("accessToken").asText();
    }
}
