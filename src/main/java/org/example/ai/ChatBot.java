package org.example.ai;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChatBot {

    private String apiKey;
    private List<ChatMessage> conversationHistory;
    private OkHttpClient httpClient;
    private Gson gson;

    // ✅ FIXED: Using Gemini 2.5 Flash (current stable model)
    // Gemini 1.5 models have been retired - use 2.5 Flash instead
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public ChatBot(String apiKey) {
        this.apiKey = apiKey;
        this.conversationHistory = new ArrayList<>();
        this.httpClient = new OkHttpClient();
        this.gson = new Gson();
        System.out.println("✅ ChatBot Gemini initialisé");
    }

    /**
     * ✅ NOUVELLE MÉTHODE : Envoie un message avec contexte automatique
     */
    public String sendMessage(String userMessage, String paintingContext, String museum) {
        try {
            // Si c'est le premier message, ajouter le contexte du tableau
            if (conversationHistory.isEmpty() && paintingContext != null && !paintingContext.isEmpty()) {
                String systemPrompt = "Tu es un guide expert du "+ museum+ ". " +
                        paintingContext +
                        " Réponds toujours avec des réponses très courtes, claires et simples. " +
                        "Maximum 1 ou 2 phrases. Utilise un ton amical et pédagogique.";

                ChatMessage systemMessage = new ChatMessage("system", systemPrompt);
                conversationHistory.add(systemMessage);
            }

            // Ajouter le message de l'utilisateur à l'historique
            conversationHistory.add(new ChatMessage("user", userMessage));

            // Construire la requête JSON pour Gemini
            String reply = callGeminiAPI(buildPromptWithHistory());

            // Ajouter la réponse à l'historique
            conversationHistory.add(new ChatMessage("model", reply));

            return reply;

        } catch (IOException e) {
            System.err.println("❌ Erreur lors de l'envoi du message: " + e.getMessage());
            e.printStackTrace();
            return "Désolé, une erreur s'est produite lors de la communication avec l'assistant.";
        }
    }

    /**
     * ✅ MÉTHODE ORIGINALE : Utilise un historique externe
     */
    public String ask(String userMessage, List<ChatMessage> history) {
        try {
            // Ajouter le message de l'utilisateur
            history.add(new ChatMessage("user", userMessage));

            // Construire le prompt avec l'historique externe
            String prompt = buildPromptFromHistory(history);
            String reply = callGeminiAPI(prompt);

            // Ajouter la réponse à l'historique
            history.add(new ChatMessage("model", reply));

            return reply;

        } catch (IOException e) {
            System.err.println("❌ Erreur lors de l'envoi du message: " + e.getMessage());
            e.printStackTrace();
            return "Désolé, une erreur s'est produite lors de la communication avec l'assistant.";
        }
    }

    /**
     * Construit un prompt avec l'historique interne
     */
    private String buildPromptWithHistory() {
        StringBuilder prompt = new StringBuilder();
        for (ChatMessage msg : conversationHistory) {
            prompt.append(msg.role).append(": ").append(msg.content).append("\n");
        }
        return prompt.toString();
    }

    /**
     * Construit un prompt avec un historique externe
     */
    private String buildPromptFromHistory(List<ChatMessage> history) {
        StringBuilder prompt = new StringBuilder();
        for (ChatMessage msg : history) {
            prompt.append(msg.role).append(": ").append(msg.content).append("\n");
        }
        return prompt.toString();
    }

    /**
     * Appelle l'API REST de Gemini avec gestion d'erreur améliorée
     */
    private String callGeminiAPI(String prompt) throws IOException {
        // Construire le JSON pour la requête
        JsonObject requestBody = new JsonObject();
        JsonArray contents = new JsonArray();
        JsonObject content = new JsonObject();
        JsonArray parts = new JsonArray();
        JsonObject part = new JsonObject();

        part.addProperty("text", prompt);
        parts.add(part);
        content.add("parts", parts);
        contents.add(content);
        requestBody.add("contents", contents);

        // Log de la requête pour débogage
        String requestJson = gson.toJson(requestBody);
        System.out.println("📤 Requête Gemini API:");
        System.out.println("   URL: " + GEMINI_API_URL);
        System.out.println("   Clé API: " + (apiKey != null ? apiKey.substring(0, Math.min(10, apiKey.length())) + "..." : "null"));
        System.out.println("   Prompt length: " + prompt.length() + " caractères");

        // Créer la requête HTTP
        String url = GEMINI_API_URL + "?key=" + apiKey;
        RequestBody body = RequestBody.create(requestJson, JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        // Exécuter la requête
        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";

            System.out.println("📥 Réponse Gemini API:");
            System.out.println("   Code HTTP: " + response.code());
            System.out.println("   Message: " + response.message());

            if (!response.isSuccessful()) {
                System.err.println("❌ Erreur API - Corps de la réponse:");
                System.err.println(responseBody);

                // Essayer de parser l'erreur JSON
                try {
                    JsonObject errorJson = gson.fromJson(responseBody, JsonObject.class);
                    if (errorJson.has("error")) {
                        JsonObject error = errorJson.getAsJsonObject("error");
                        String errorMessage = error.has("message") ? error.get("message").getAsString() : "Erreur inconnue";
                        int errorCode = error.has("code") ? error.get("code").getAsInt() : response.code();
                        throw new IOException("Gemini API error " + errorCode + ": " + errorMessage);
                    }
                } catch (Exception e) {
                    // Si le parsing échoue, utiliser le message par défaut
                }

                throw new IOException("Gemini API error: " + response.code() + " - " + response.message() +
                        "\nDétails: " + (responseBody.length() > 200 ? responseBody.substring(0, 200) + "..." : responseBody));
            }

            System.out.println("   Réponse reçue: " + responseBody.length() + " caractères");

            // Parser la réponse JSON
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);

            // Extraire le texte de la réponse
            JsonArray candidates = jsonResponse.getAsJsonArray("candidates");
            if (candidates != null && candidates.size() > 0) {
                JsonObject candidate = candidates.get(0).getAsJsonObject();
                JsonObject contentObj = candidate.getAsJsonObject("content");
                JsonArray partsArray = contentObj.getAsJsonArray("parts");
                if (partsArray != null && partsArray.size() > 0) {
                    String reply = partsArray.get(0).getAsJsonObject().get("text").getAsString();
                    System.out.println("✅ Réponse extraite avec succès");
                    return reply;
                }
            }

            System.err.println("⚠️ Aucun contenu trouvé dans la réponse");
            return "Aucune réponse reçue de l'assistant.";
        }
    }

    /**
     * ✅ NOUVEAU : Réinitialiser la conversation (pour un nouveau tableau)
     */
    public void resetConversation() {
        conversationHistory.clear();
        System.out.println("✅ Historique de conversation réinitialisé");
    }

    /**
     * ✅ NOUVEAU : Obtenir l'historique complet
     */
    public List<ChatMessage> getConversationHistory() {
        return new ArrayList<>(conversationHistory);
    }

    /**
     * ✅ NOUVEAU : Obtenir le nombre de messages dans l'historique
     */
    public int getHistorySize() {
        return conversationHistory.size();
    }
}