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
import java.time.Duration;
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
    private int offset;
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
    private String skinVariant;
    private boolean isChangeSkin;
    private String skinPath;
    private boolean isAutoOffset;
    private final int BEST_CASE_RESPONSE_DURATION = 100;

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
        System.out.println("Initialising...");
        System.out.println();
    }

    @Override
    public void getUsernameChoice() {
        System.out.print("What name will you like to snipe: ");
        snipedUsername = scanner.nextLine().replaceAll("\\s+", "");
        if ((snipedUsername.length() < 3) || (snipedUsername.length() > 16)
                || (!(snipedUsername.matches("[A-Za-z0-9_]+"))))
            throw new GeneralSniperException("[GetUsernameChoice] You entered an invalid username.");
    }

    @Override
    public void parseAccountFile() {
    }

    @Override
    public boolean parseConfigFile() throws IOException {
        var fileName = Path.of("config.yml");
        var actual = Files.readString(fileName);
        var yaml = new Yaml();
        Map<String, Object> accountData = yaml.load(actual);
        spread = (int) accountData.get("spread");
        skinVariant = ((String) accountData.get("skinModel")).toLowerCase();
        isAutoOffset = (boolean) accountData.get("autoOffset");
        isChangeSkin = (boolean) accountData.get("changeSkin");
        if (isChangeSkin)
            if (!((skinVariant.equals("slim")) || (skinVariant.equals("classic"))))
                throw new GeneralSniperException("[ConfigParser] Invalid skin type.");
        skinPath = (String) accountData.get("skinFileName");
        if (!(isAutoOffset)) {
            offset = (int) accountData.get("offset");
            System.out.println("Offset is set to " + offset + " ms.");
            return false;
        }
        return true;
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
        var timer = new Timer();
        TimerTask snipe = new TimerTask() {
            @Override
            public void run() {
                try {
                    for (var request = 1; request <= NO_OF_REQUESTS; request++) {
                        var snipe = client.sendAsync(snipeRequest, HttpResponse.BodyHandlers.discarding())
                                .thenApply(HttpResponse::statusCode).thenAccept(code -> {
                                    var now = Instant.now();
                                    var accurateDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
                                            .withZone(ZoneId.systemDefault());
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
                    if (isSuccessful.get()) {
                        System.out.println("You have successfully sniped the name " + snipedUsername + "!");
                        if (isChangeSkin) {
                            var file = Files.readAllBytes(Path.of(skinPath));
                            var strFile = Base64.getEncoder().encodeToString(file);
                            var postJSON = "{\"file\":\"" + strFile + "\",\"variant\":\"" + skinVariant + "\"}";
                            var uri = new URI("https://api.minecraftservices.com/minecraft/profile/skins");
                            var request = HttpRequest.newBuilder().uri(uri)
                                    .headers("Authorization", "Bearer " + authToken, "Content-Type",
                                            "multipart/form-data")
                                    .POST(HttpRequest.BodyPublishers.ofString(postJSON)).build();
                            var response = client.send(request, HttpResponse.BodyHandlers.discarding());
                            if (!(response.statusCode() == 200))
                                throw new GeneralSniperException(
                                        "[SkinChanger] HTTP status code: " + response.statusCode());
                            System.out.println("Successfully changed skin!");
                        }
                    }
                    System.out.print("Press ENTER to quit: ");
                    System.in.read();
                    timer.cancel();
                    System.exit(0);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        // I've given up on making the code clean.
        if (turboSnipe) {
            System.out.println(
                    "Warning: Some usernames may show up as available but has been blocked by Mojang. Sniping it will not work.");
            var uri = new URI("https://api.minecraftservices.com/minecraft/profile/name/" + snipedUsername);
            snipeRequest = HttpRequest.newBuilder().uri(uri).header("Authorization", "Bearer " + authToken)
                    .PUT(HttpRequest.BodyPublishers.noBody()).build();
            System.out.println("Setup complete!");
            snipe.run();
        }
        var now = Instant.now();
        var semiAccurateDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault());
        var niceDropTime = semiAccurateDateFormat.format(dropTime);
        var diffInTime = Duration.between(now, dropTime).toMinutes();
        System.out.println(
                "Sniping " + snipedUsername + " in ~" + diffInTime + " minutes | sniping at " + niceDropTime + ".");
        var offsetAdjustedTime = Date.from(dropTime.minusMillis(offset));
        var uri = new URI("https://api.minecraftservices.com/minecraft/profile/name/" + snipedUsername);
        snipeRequest = HttpRequest.newBuilder().uri(uri).header("Authorization", "Bearer " + authToken)
                .PUT(HttpRequest.BodyPublishers.noBody()).build();
        System.out.println("Setup complete!");
        timer.schedule(snipe, offsetAdjustedTime);
    }

    @Override
    public void isNameChangeEligible() throws URISyntaxException, IOException, InterruptedException {
        var uri = new URI("https://api.minecraftservices.com/minecraft/profile/namechange");
        var request = HttpRequest.newBuilder().uri(uri).header("Authorization", "Bearer " + authToken).GET().build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (!(response.statusCode() == 200))
            throw new GeneralSniperException(
                    "[NameChangeEligibilityChecker] HTTP status code: " + response.statusCode());
        System.out.println("Signed into your account successfully.");
        var body = response.body();
        var node = mapper.readTree(body);
        boolean isAllowed = node.get("nameChangeAllowed").asBoolean();
        if (!isAllowed)
            throw new GeneralSniperException(
                    "[NameChangeEligibilityChecker] You cannot name change within the cooldown period.");
    }

    @Override
    public void checkNameAvailabilityTime() throws URISyntaxException, IOException, InterruptedException {
        var uri = new URI("https://api.kqzz.me/api/namemc/droptime/" + snipedUsername);
        var request = HttpRequest.newBuilder().uri(uri).GET().build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (!(response.statusCode() == 200))
            throw new GeneralSniperException("[CheckNameAvailabilityTime] HTTP status code: " + response.statusCode());
        var body = response.body();
        var node = mapper.readTree(body);
        try {
            dropTime = Instant.ofEpochSecond(node.get("droptime").asInt());
            if (Duration.between(authTime, dropTime).toSeconds() > 86_400)
                throw new GeneralSniperException(
                        "[CheckNameAvailabilityTime] You cannot snipe a name available more than one day later if you are using a Microsoft account.");
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
        var uri = new URI(
                "https://login.live.com/oauth20_authorize.srf?client_id=9abe16f4-930f-4033-b593-6e934115122f&response_type=code&redirect_uri=https%3A%2F%2Fmicroauth.tk%2Ftoken&scope=XboxLive.signin%20XboxLive.offline_access");
        authTime = Instant.now();
        try {
            Desktop.getDesktop().browse(uri);
        } catch (HeadlessException ex) {
            System.out.println(
                    "Looks like you are running this program in a headless environment. Copy the following URL into your browser:");
            System.out.println(
                    "https://login.live.com/oauth20_authorize.srf?client_id=9abe16f4-930f-4033-b593-6e934115122f&response_type=code&redirect_uri=https%3A%2F%2Fmicroauth.tk%2Ftoken&scope=XboxLive.signin%20XboxLive.offline_access");
        }
        System.out.println("Please make sure that your snipe will not last more than a day or the snipe will fail.");
        System.out.print(
                "Sign in with your Microsoft account and copy the ID from the \"access_token\" field right here: ");
        authToken = scanner.nextLine();
        authToken = authToken.replaceAll("[\"]", "");
        authToken = authToken.replaceAll("\\s+", "");
    }

    @Override
    public void autoOffsetCalculation() throws URISyntaxException, IOException, InterruptedException {
        System.out.println("Calculating offset...");
        var beforeSend = Instant.now();
        var uri = new URI("https://api.minecraftservices.com/minecraft/profile/name/" + snipedUsername);
        var request = HttpRequest.newBuilder().uri(uri).header("Authorization", "Bearer " + authToken)
                .PUT(HttpRequest.BodyPublishers.noBody()).build();
        var response = client.send(request, HttpResponse.BodyHandlers.discarding());
        var afterSend = Instant.now();
        offset = Math.toIntExact(Duration.between(beforeSend, afterSend).toMillis() - BEST_CASE_RESPONSE_DURATION);
        if (response.statusCode() == 200) {
            var accurateDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
                    .withZone(ZoneId.systemDefault());
            var accurateTime = accurateDateFormat.format(afterSend);
            System.out.println("[success] 200 @ " + accurateTime);
            System.out.println("You have successfully sniped the name " + snipedUsername + "!");
            System.exit(0);
        }
        System.out.println("Offset is set to " + offset + " ms.");
    }
}
