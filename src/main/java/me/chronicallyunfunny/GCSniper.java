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

public class GCSniper implements Sniper {
    private final HttpClient client = HttpClient.newHttpClient();
    private String authToken = null;
    private String snipedUsername = null;
    private int delay;
    private Instant dropTime;
    private HttpRequest snipeRequest;
    private boolean turboSnipe = false;
    private final AtomicBoolean isSuccessful = new AtomicBoolean(false);
    private final int NO_OF_REQUESTS = 10;
    private final List<CompletableFuture<Void>> completableFutures = new ArrayList<>();
    private final ObjectMapper mapper = new ObjectMapper();
    private final Scanner scanner = new Scanner(System.in);
    private Instant authTime;
    private int spread;
    private String giftCode;
    private String skinVariant;
    private boolean isChangeSkin;
    private String skinPath;

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
    public void parseAccountFile() throws IOException, URISyntaxException {
    }

    // Gets giftcode instead (this class implements Sniper interface and this sniper
    // is not meant to GCSnipe)
    @Override
    public boolean isSecurityQuestionsNeeded() throws URISyntaxException, IOException, InterruptedException {
        System.out.print("Enter your gift code (press ENTER if you have already redeemed your gift code): ");
        String input = null;
        input = scanner.nextLine();
        if (input.isEmpty())
            return false;
        giftCode = input.replaceAll("\\s+", "");
        return true;
    }

    @Override
    public void sendSecurityQuestions() throws URISyntaxException, IOException, InterruptedException {
    }

    // Redeems gift code (like I said, had to do this kind of shit to keep the main
    // class clean)
    @Override
    public boolean getSecurityQuestionsID() throws URISyntaxException, IOException, InterruptedException {
        var uri = new URI("https://api.minecraftservices.com/productvoucher/" + giftCode);
        var request = HttpRequest.newBuilder().uri(uri)
                .headers("Accept", "application/json", "Authorization", "Bearer " + authToken)
                .PUT(HttpRequest.BodyPublishers.noBody()).build();
        var response = client.send(request, HttpResponse.BodyHandlers.discarding());
        if (!(response.statusCode() == 200))
            throw new GeneralSniperException("[GiftCodeRedemption] HTTP status code: " + response.statusCode());
        return false; // always returns false no matter what, can't exactly start calling the
                      // sendSecurityQuestions method since it's "abstract"
    }

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
        System.out.println("WARNING: This is a preview version of NodeSniper. Do not redistribute.");
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
    public void parseConfigFile() throws IOException {
        var fileName = Path.of("config.yml");
        var actual = Files.readString(fileName);
        var yaml = new Yaml();
        Map<String, Object> accountData = yaml.load(actual);
        delay = (int) accountData.get("delay");
        spread = (int) accountData.get("spread");
        skinVariant = (String) accountData.get("skinModel");
        skinVariant = skinVariant.toLowerCase();
        isChangeSkin = (boolean) accountData.get("changeSkin");
        if (isChangeSkin)
            if (!((skinVariant.equals("slim")) || (skinVariant.equals("classic"))))
                throw new GeneralSniperException("[ConfigParser] Invalid skin type.");
        skinPath = (String) accountData.get("skinFileName");
        System.out.println("Delay is set to " + delay + " ms.");
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
                    if (isSuccessful.get())
                        System.out.println("You have successfully sniped the name " + snipedUsername + "!");
                    if (isChangeSkin) {
                        var file = Files.readAllBytes(Path.of(skinPath));
                        var strFile = Base64.getEncoder().encodeToString(file);
                        var postJSON = "{\"file\":\"" + strFile + "\",\"variant\":\"" + skinVariant + "\"}";
                        var uri = new URI("https://api.minecraftservices.com/minecraft/profile/skins");
                        var request = HttpRequest.newBuilder().uri(uri)
                                .headers("Authorization", "Bearer " + authToken, "Content-Type", "multipart/form-data")
                                .POST(HttpRequest.BodyPublishers.ofString(postJSON)).build();
                        var response = client.send(request, HttpResponse.BodyHandlers.discarding());
                        if (!(response.statusCode() == 200))
                            throw new GeneralSniperException(
                                    "[SkinChanger] HTTP status code: " + response.statusCode());
                        System.out.println("Successfully changed skin!");
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
            var postJSON = "{\"profileName\":\"" + snipedUsername + "\"}";
            var uri = new URI("https://api.minecraftservices.com/minecraft/profile");
            snipeRequest = HttpRequest.newBuilder().uri(uri)
                    .headers("Accept", "application/json", "Authorization", "Bearer " + authToken)
                    .POST(HttpRequest.BodyPublishers.ofString(postJSON)).build();
            System.out.println("Setup complete!");
            snipe.run();
        }
        var now = Instant.now();
        var semiAccurateDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault());
        var niceDropTime = semiAccurateDateFormat.format(dropTime);
        var diffInTime = (dropTime.getEpochSecond() - now.getEpochSecond()) / 60;
        System.out.println(
                "Sniping " + snipedUsername + " in ~" + diffInTime + " minutes | sniping at " + niceDropTime + ".");
        var delayAdjustedTime = Date.from(dropTime.minusMillis(delay));
        var postJSON = "{\"profileName\":\"" + snipedUsername + "\"}";
        var uri = new URI("https://api.minecraftservices.com/minecraft/profile");
        snipeRequest = HttpRequest.newBuilder().uri(uri)
                .headers("Accept", "application/json", "Authorization", "Bearer " + authToken)
                .POST(HttpRequest.BodyPublishers.ofString(postJSON)).build();
        System.out.println("Setup complete!");
        timer.schedule(snipe, delayAdjustedTime);
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
            if (dropTime.getEpochSecond() - authTime.getEpochSecond() > 86_400)
                throw new GeneralSniperException(
                        "[CheckNameAvailabilityTime] You cannot snipe a name available more than one day later if you are using a Microsoft account.");
        } catch (NullPointerException ex) {
            turboSnipe = true;
        }
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
    public void isNameChangeEligible() throws URISyntaxException, IOException, InterruptedException {
    }
}
