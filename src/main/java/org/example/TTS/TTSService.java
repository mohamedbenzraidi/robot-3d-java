package org.example.TTS;


import com.github.pemistahl.lingua.api.Language;
import com.github.pemistahl.lingua.api.LanguageDetector;
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder;
import javazoom.jl.player.Player;
import org.example.config.ConfigLoader;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class TTSService {
    private static final String API_KEY = ConfigLoader.get("voiceRss.api.key");


    public static void say(String text) {
        if (text == null || text.trim().isEmpty()) return;

        new Thread(() -> {
            try {
                System.out.println("DEBUG: Loaded API Key: " + API_KEY);

                if (API_KEY == null || API_KEY.isEmpty()) {
                    System.err.println("ERROR: API Key is missing! Check config.properties");
                    return;
                }

                String language = "en-gb";
                String voice = "Harry";

                LanguageDetector detector = LanguageDetectorBuilder.fromLanguages(Language.ENGLISH, Language.FRENCH).build();
                Language languageDetected = detector.detectLanguageOf(text);

                if (languageDetected == Language.FRENCH) {
                    language = "fr-fr";
                    voice = "Axel";
                }

                String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
                String urlStr = "https://api.voicerss.org/?key=" + API_KEY +
                        "&hl=" + language +
                        "&c=mp3" +
                        "&v=" + voice +
                        "&f=44khz_16bit_stereo" +
                        "&src=" + encodedText;

                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                int responseCode = conn.getResponseCode();
                System.out.println("DEBUG: Server Response Code: " + responseCode);
                String contentType = conn.getContentType();

                if (responseCode == 200 && contentType != null && !contentType.contains("audio")) {
                    System.err.println("ERROR: The server returned text instead of audio.");
                    try (java.util.Scanner s = new java.util.Scanner(conn.getInputStream()).useDelimiter("\\A")) {
                        System.err.println("Server said: " + (s.hasNext() ? s.next() : ""));
                    }
                    return;
                }

                InputStream audioStream = conn.getInputStream();
                Player player = new Player(audioStream);
                player.play();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) {
        say("Hello, welcome to the museum.");
        say("Bienvenue au musée.");
    }
}
