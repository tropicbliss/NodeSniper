package me.chronicallyunfunny;

import com.fasterxml.jackson.databind.ObjectMapper;
import kong.unirest.Unirest;
import org.yaml.snakeyaml.Yaml;

import java.awt.*;
import java.io.File;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class GCSniper implements Sniper {
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build();
    private String authToken = null;
    private String snipedUsername = null;
    private long offset;
    private Instant dropTime;
    private final AtomicBoolean isSuccessful = new AtomicBoolean(false);
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
                "https://login.live.com/oauth20_authorize.srf?client_id=9abe16f4-930f-4033-b593-6e934115122f&response_type=code&redirect_uri=https%3A%2F%2Fapi.gosnipe.tech%2Fapi%2Fauthenticate&scope=XboxLive.signin%20XboxLive.offline_access");
        authTime = Instant.now();
        try {
            Desktop.getDesktop().browse(uri);
        } catch (HeadlessException ex) {
            System.out.println(
                    "Looks like you are running this program in a headless environment. Copy the following URL into your browser:");
            System.out.println(
                    "https://login.live.com/oauth20_authorize.srf?client_id=9abe16f4-930f-4033-b593-6e934115122f&response_type=code&redirect_uri=https%3A%2F%2Fapi.gosnipe.tech%2Fapi%2Fauthenticate&scope=XboxLive.signin%20XboxLive.offline_access");
        }
        System.out.println("Please make sure that your snipe will not last more than a day or the snipe will fail.");
        System.out.print(
                "Sign in with your Microsoft account and copy the ID from the \"access_token\" field right here: ");
        authToken = scanner.nextLine().strip().replaceAll("[\"]", "");
    }

    @Override
    public void parseAccountFile() {
    }

    // Gets giftcode instead (this class implements Sniper interface and this sniper
    // is not meant to GCSnipe)
    @Override
    public boolean isSecurityQuestionsNeeded() {
        System.out.print("Enter your gift code (press ENTER if you have already redeemed your gift code): ");
        var input = scanner.nextLine();
        if (input.isEmpty())
            return false;
        giftCode = input.strip();
        return true;
    }

    @Override
    public void sendSecurityQuestions() {
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
        if (response.statusCode() != 200)
            throw new GeneralSniperException("[GiftCodeRedemption] HTTP status code: " + response.statusCode());
        System.out.println("Signed into your account successfully.");
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
        System.out.println();
        System.out.println("Initialising...");
        System.out.println();
    }

    @Override
    public void getUsernameChoice() {
        System.out.print("What name will you like to snipe: ");
        snipedUsername = scanner.nextLine().strip();
        if ((snipedUsername.length() < 3) || (snipedUsername.length() > 16)
                || (!snipedUsername.matches("[A-Za-z0-9_]+")))
            throw new GeneralSniperException("[GetUsernameChoice] You entered an invalid username.");
    }

    @Override
    public boolean parseConfigFile() throws IOException {
        var fileName = Path.of("config.yml");
        var actual = Files.readString(fileName);
        var yaml = new Yaml();
        Map<String, Object> accountData = yaml.load(actual);
        spread = (int) accountData.get("spread");
        if (spread < 0)
            throw new GeneralSniperException("[ConfigParser] Spread cannot be lower than 0.");
        if (spread > 0)
            System.out.println("Spread is set to " + spread + " ms.");
        skinVariant = ((String) accountData.get("skinModel")).toLowerCase().strip();
        boolean isAutoOffset = (boolean) accountData.get("autoOffset");
        isChangeSkin = (boolean) accountData.get("changeSkin");
        if (isChangeSkin)
            if (!((skinVariant.equals("slim")) || (skinVariant.equals("classic"))))
                throw new GeneralSniperException("[ConfigParser] Invalid skin type.");
        skinPath = ((String) accountData.get("skinFileName")).strip();
        if (!isAutoOffset) {
            try {
                offset = ((Number) accountData.get("offset")).longValue();
            } catch (NullPointerException ex) {
                offset = 0;
            }
            System.out.println("Offset is set to " + offset + " ms.");
            return false;
        }
        return true;
    }

    @Override
    public void execute() throws URISyntaxException, InterruptedException, IOException {
        int NO_OF_REQUESTS = 6;
        var semiAccurateDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault());
        var niceDropTime = semiAccurateDateFormat.format(dropTime);
        var now = Instant.now();
        var duration = Duration.between(now, dropTime).toMinutes();
        if (duration == 0) {
            duration = Duration.between(now, dropTime).toSeconds();
            System.out.println("Sniping " + snipedUsername + " in ~" + duration + " seconds | sniping at " + niceDropTime + ".");
        }
        else
            System.out.println("Sniping " + snipedUsername + " in ~" + duration + " minutes | sniping at " + niceDropTime + ".");
        var postJSON = "{\"profileName\":\"" + snipedUsername + "\"}";
        var uri = new URI("https://api.minecraftservices.com/minecraft/profile");
        var snipeRequest = HttpRequest.newBuilder().uri(uri)
                .headers("Accept", "application/json", "Authorization", "Bearer " + authToken)
                .POST(HttpRequest.BodyPublishers.ofString(postJSON)).build();
        var longDropTime = dropTime.minusMillis(offset).toEpochMilli();
        var longLagTime = longDropTime - 3_000L;
        if (System.currentTimeMillis() < longLagTime)
            Thread.sleep(longLagTime - System.currentTimeMillis());
        while ((System.currentTimeMillis()) < longDropTime)
            Thread.sleep(1);
        for (var request = 1; request < NO_OF_REQUESTS; request++) {
            var snipe = client.sendAsync(snipeRequest, HttpResponse.BodyHandlers.discarding())
                    .thenApply(HttpResponse::statusCode).thenAccept(code -> {
                        var reqTime = Instant.now();
                        var accurateDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
                                .withZone(ZoneId.systemDefault());
                        var accurateTime = accurateDateFormat.format(reqTime);
                        var keyword = "fail";
                        if (code == 200) {
                            isSuccessful.set(true);
                            keyword = "success";
                        }
                        System.out.println("[" + keyword + "] " + code + " @ " + accurateTime);
                    });
            completableFutures.add(snipe);
            if (spread != 0)
                Thread.sleep(spread);
        }
        // I don't want to spawn a new thread when the main thread is free, so hardcoding it in.
        var lastResponse = client.send(snipeRequest, HttpResponse.BodyHandlers.discarding());
        var reqTime = Instant.now();
        var accurateDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
                .withZone(ZoneId.systemDefault());
        var accurateTime = accurateDateFormat.format(reqTime);
        var keyword = "fail";
        if (lastResponse.statusCode() == 200) {
            isSuccessful.set(true);
            keyword = "success";
        }
        System.out.println("[" + keyword + "] " + lastResponse.statusCode() + " @ " + accurateTime);
        CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).join();
        if (isSuccessful.get()) {
            System.out.println("You have successfully sniped the name " + snipedUsername + "!");
            if (isChangeSkin) {
                var response = Unirest
                        .post("https://api.minecraftservices.com/minecraft/profile/skins")
                        .header("Authorization", "Bearer " + authToken).field("variant", skinVariant)
                        .field("file", new File(skinPath)).asEmpty();
                var code = response.getStatus();
                if (code != 200)
                    throw new GeneralSniperException(
                            "[SkinChanger] HTTP status code: " + code);
                System.out.println("Successfully changed skin!");
            }
        }
        System.out.print("Press ENTER to quit: ");
        scanner.nextLine();
        // exits
    }

    @Override
    public void checkNameAvailabilityTime() throws URISyntaxException, IOException, InterruptedException {
        var uri = new URI("https://api.kqzz.me/api/namemc/droptime/" + snipedUsername);
        var request = HttpRequest.newBuilder().uri(uri).GET().build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200)
            throw new GeneralSniperException("[CheckNameAvailabilityTime] HTTP status code: " + response.statusCode());
        var body = response.body();
        var node = mapper.readTree(body);
        try {
            dropTime = Instant.ofEpochSecond(node.get("droptime").asInt());
            if (Duration.between(authTime, dropTime).toSeconds() > 86_400)
                throw new GeneralSniperException(
                        "[CheckNameAvailabilityTime] You cannot snipe a name available more than one day later if you are using a Microsoft account.");
        } catch (NullPointerException ex) {
            throw new GeneralSniperException(
                    "[CheckNameAvailabilityTime] Username is freely available. Claim it manually via \"minecraft.net\".");
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
    public void isNameChangeEligible() {
    }

    @Override
    public void autoOffsetCalculation() throws URISyntaxException, IOException, InterruptedException {
        long beforeSend, afterSend;
        System.out.println("Calculating offset...");
        beforeSend = System.currentTimeMillis();
        var uri = new URI("https://api.minecraftservices.com/minecraft/profile/name/" + snipedUsername);
        var request = HttpRequest.newBuilder().uri(uri).header("Authorization", "Bearer " + authToken)
                .PUT(HttpRequest.BodyPublishers.noBody()).build();
        client.send(request, HttpResponse.BodyHandlers.discarding());
        afterSend = System.currentTimeMillis();
        int SERVER_RESPONSE_DURATION = 100;
        offset = afterSend - beforeSend - SERVER_RESPONSE_DURATION;
        System.out.println("Offset is set to " + offset + " ms.");
    }
}
