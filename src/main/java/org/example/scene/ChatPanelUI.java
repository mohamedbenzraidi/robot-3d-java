package org.example.scene;

import com.jme3.app.SimpleApplication;
import com.jme3.math.Vector3f;
import com.simsilica.lemur.*;
import com.simsilica.lemur.component.BorderLayout;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import com.simsilica.lemur.component.SpringGridLayout;
import com.simsilica.lemur.event.CursorEventControl;
import com.simsilica.lemur.event.DragHandler;
import org.example.ai.ChatBot;
import java.util.ArrayList;
import java.util.List;

public class ChatPanelUI {

    private final SimpleApplication app;
    private final Object sceneManager;

    private Container mainContainer;
    private TextField chatInput;
    private TextField apiKeyInput;
    private Container chatHistoryContainer;
    private List<String> messageHistory;
    private int maxVisibleMessages = 8; // Nombre de messages visibles
    private int scrollOffset = 0; // Pour gérer le scroll

    private ChatBot chatBot;
    private String currentPaintingContext;

    public ChatPanelUI(SimpleApplication app, Object sceneManager) {
        this.app = app;
        this.sceneManager = sceneManager;
        this.messageHistory = new ArrayList<>();
        initialize();
    }

    private void initialize() {
        // Container principal avec BorderLayout
        mainContainer = new Container(new BorderLayout());
        mainContainer.setPreferredSize(new Vector3f(550, 500, 0));

        // Fond moderne avec gradient subtil
        mainContainer.setBackground(new QuadBackgroundComponent(
                new com.jme3.math.ColorRGBA(0.10f, 0.12f, 0.16f, 0.98f)
        ));

        // ============ HEADER ============
        Container header = new Container(new SpringGridLayout());
        header.setBackground(new QuadBackgroundComponent(
                new com.jme3.math.ColorRGBA(0.15f, 0.20f, 0.28f, 1f)
        ));
        header.setInsets(new com.simsilica.lemur.Insets3f(18, 20, 18, 20));

        // Titre moderne
        Label titleLabel = header.addChild(new Label("🎨 Museum AI Assistant"));
        titleLabel.setFontSize(24);
        titleLabel.setColor(new com.jme3.math.ColorRGBA(1f, 1f, 1f, 1f));

        // Sous-titre avec instructions
        Label subtitleLabel = header.addChild(new Label("Press ESC to close • Scroll with Mouse Wheel"));
        subtitleLabel.setFontSize(12);
        subtitleLabel.setColor(new com.jme3.math.ColorRGBA(0.65f, 0.75f, 0.85f, 1f));

        mainContainer.addChild(header, BorderLayout.Position.North);

        // ============ CHAT HISTORY ============
        Container centerWrapper = new Container();
        centerWrapper.setBackground(new QuadBackgroundComponent(
                new com.jme3.math.ColorRGBA(0.08f, 0.10f, 0.14f, 1f)
        ));
        centerWrapper.setInsets(new com.simsilica.lemur.Insets3f(15, 15, 15, 15));

        // Container pour les messages (layout vertical)
        chatHistoryContainer = new Container(new SpringGridLayout(com.simsilica.lemur.Axis.Y, com.simsilica.lemur.Axis.X));
        chatHistoryContainer.setInsets(new com.simsilica.lemur.Insets3f(10, 10, 10, 10));
        chatHistoryContainer.setPreferredSize(new Vector3f(500, 300, 0));

        centerWrapper.addChild(chatHistoryContainer);
        mainContainer.addChild(centerWrapper, BorderLayout.Position.Center);

        // ============ INPUT AREA ============
        Container bottom = new Container(new SpringGridLayout());
        bottom.setBackground(new QuadBackgroundComponent(
                new com.jme3.math.ColorRGBA(0.12f, 0.16f, 0.22f, 1f)
        ));
        bottom.setInsets(new com.simsilica.lemur.Insets3f(15, 20, 15, 20));

        // Champ API Key (caché par défaut si clé existe)
        apiKeyInput = bottom.addChild(new TextField("probleme d'API..."));
        apiKeyInput.setPreferredSize(new Vector3f(490, 32, 0));
        apiKeyInput.setFontSize(13);
        apiKeyInput.setBackground(new QuadBackgroundComponent(
                new com.jme3.math.ColorRGBA(0.18f, 0.22f, 0.30f, 1f)
        ));
        apiKeyInput.setColor(new com.jme3.math.ColorRGBA(0.9f, 0.9f, 0.9f, 1f));

        // Container pour input + bouton (horizontal)
        Container inputRow = bottom.addChild(new Container(new SpringGridLayout(com.simsilica.lemur.Axis.X, com.simsilica.lemur.Axis.Y)));

        // Champ de saisie message
        chatInput = inputRow.addChild(new TextField("Type your message here..."));
        chatInput.setPreferredSize(new Vector3f(390, 38, 0));
        chatInput.setFontSize(14);
        chatInput.setBackground(new QuadBackgroundComponent(
                new com.jme3.math.ColorRGBA(0.18f, 0.22f, 0.30f, 1f)
        ));
        chatInput.setColor(new com.jme3.math.ColorRGBA(1f, 1f, 1f, 1f));

        // Bouton Send moderne
        Button sendBtn = inputRow.addChild(new Button("Send ➤"));
        sendBtn.setPreferredSize(new Vector3f(90, 38, 0));
        sendBtn.setFontSize(15);
        sendBtn.setColor(new com.jme3.math.ColorRGBA(1f, 1f, 1f, 1f));
        sendBtn.setBackground(new QuadBackgroundComponent(
                new com.jme3.math.ColorRGBA(0.25f, 0.55f, 0.85f, 1f)
        ));
        sendBtn.addClickCommands(btn -> onSend());

        mainContainer.addChild(bottom, BorderLayout.Position.South);

        // Activer le drag & drop
        CursorEventControl.addListenersToSpatial(mainContainer, new DragHandler());
    }

    public void show(String paintingContext) {
        this.currentPaintingContext = paintingContext;

        // Initialiser ChatBot avec la clé API
        if (chatBot == null) {
            String key = "AIzaSyATToTi4ZSATY44gUk3pcpngYXccNQUHy0";
            if (key != null && !key.isEmpty()) {
                try {
                    chatBot = new ChatBot(key);
                    System.out.println("✅ Gemini API Key loaded");

                    // Cacher le champ API key
                    if (apiKeyInput.getParent() != null) {
                        apiKeyInput.removeFromParent();
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error initializing ChatBot: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        // Attacher le panel à l'interface
        if (!app.getGuiNode().hasChild(mainContainer)) {
            app.getGuiNode().attachChild(mainContainer);
        }

        // Positionner en haut à droite
        float x = app.getCamera().getWidth() - mainContainer.getPreferredSize().x - 20;
        float y = app.getCamera().getHeight() - 20;
        mainContainer.setLocalTranslation(x, y, 0);

        // Effacer l'historique et afficher le message de bienvenue
        messageHistory.clear();
        scrollOffset = 0;

        addMessage("SYSTEM", "🎨 " + paintingContext);

        if (chatBot != null) {
            addMessage("SYSTEM", "✅ Connected to Gemini AI. Ask me anything!");
        } else {
            addMessage("SYSTEM", "⚠️ Please enter your Gemini API key below");
        }

        refreshChatDisplay();

        // Configuration de l'interface
        if(this.sceneManager instanceof SceneManager){
            app.getInputManager().setCursorVisible(false);
            ((org.example.scene.JmeApp) app).getFlyByCamera().setEnabled(false);
        }else if(this.sceneManager instanceof MetSceneManager){
            app.getInputManager().setCursorVisible(false);
            ((org.example.scene.JmeMetApp) app).getFlyByCamera().setEnabled(false);
        }


        // Focus automatique sur le champ de texte
        app.enqueue(() -> {
            try {
                com.simsilica.lemur.GuiGlobals.getInstance().requestFocus(chatInput);
                chatInput.setText("");
            } catch (Exception e) {
                System.err.println("⚠️ Cannot focus input: " + e.getMessage());
            }
            return null;
        });

        System.out.println("💬 Chat panel opened");
    }

    public void close() {
        if (app.getGuiNode().hasChild(mainContainer)) {
            app.getGuiNode().detachChild(mainContainer);
        }

        if(this.sceneManager instanceof SceneManager){
            app.getInputManager().setCursorVisible(false);
            ((JmeApp) app).getFlyByCamera().setEnabled(true);
        }else if(this.sceneManager instanceof MetSceneManager){
            app.getInputManager().setCursorVisible(false);
            ((JmeMetApp) app).getFlyByCamera().setEnabled(true);
        }else if(sceneManager instanceof ThSceneManager) {
            app.getInputManager().setCursorVisible(false);
            ((JmeThApp) app).getFlyByCamera().setEnabled(true);
        }

        System.out.println("❎ Chat panel closed");
    }

    public boolean isVisible() {
        return app.getGuiNode().hasChild(mainContainer);
    }

    private void onSend() {
        String message = chatInput.getText().trim();
        if (message.isEmpty() || message.equals("Type your message here...")) return;

        // Vérifier que ChatBot est initialisé
        if (chatBot == null) {
            String key = apiKeyInput.getText().trim();
            if (key.isEmpty() || key.equals("probleme d'API...")) {
                addMessage("SYSTEM", "veuillez resoudre le probleme");
//                refreshChatDisplay();
                return;
            }

            try {
                chatBot = new ChatBot(key);
                apiKeyInput.removeFromParent();
                addMessage("SYSTEM", "✅ API Key accepted!");
                refreshChatDisplay();
            } catch (Exception e) {
                addMessage("SYSTEM", "❌ Invalid API key: " + e.getMessage());
                refreshChatDisplay();
                return;
            }
        }

        // Afficher le message de l'utilisateur
        addMessage("USER", message);
        chatInput.setText("");

        // Afficher un indicateur de chargement
        addMessage("LOADING", "⏳ AI is thinking...");
        refreshChatDisplay();

        // Envoyer le message à l'API dans un thread séparé
        new Thread(() -> {
            try {
                String reply;
                if(sceneManager instanceof SceneManager){
                    reply = chatBot.sendMessage(message, currentPaintingContext, "musée du louvre");
                }else if(sceneManager instanceof MetSceneManager){
                    reply = chatBot.sendMessage(message, currentPaintingContext, "Metropolitan Museum of Art de New York");
                }else if(sceneManager instanceof ThSceneManager){
                    reply = chatBot.sendMessage(message, currentPaintingContext, "Musée d'Orsay");
                }else{
                    reply = null;
                }

                app.enqueue(() -> {
                    // Supprimer le message de chargement
                    if (!messageHistory.isEmpty() && messageHistory.get(messageHistory.size() - 1).startsWith("LOADING:")) {
                        messageHistory.remove(messageHistory.size() - 1);
                    }

                    // Afficher la réponse de l'AI
                    addMessage("AI", reply);
                    refreshChatDisplay();

                    // Afficher la réponse avec le robot
                    if(sceneManager instanceof SceneManager){
                        ((SceneManager) sceneManager).showRobotSpeech(reply);
                    }else if(sceneManager instanceof MetSceneManager){
                        ((MetSceneManager) sceneManager).showRobotSpeech(reply);
                    }else if(sceneManager instanceof ThSceneManager){
                        ((ThSceneManager) sceneManager).showRobotSpeech(reply);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                app.enqueue(() -> {
                    // Supprimer le message de chargement
                    if (!messageHistory.isEmpty() && messageHistory.get(messageHistory.size() - 1).startsWith("LOADING:")) {
                        messageHistory.remove(messageHistory.size() - 1);
                    }
                    addMessage("SYSTEM", "❌ API Error: " + e.getMessage());
                    refreshChatDisplay();
                });
            }
        }).start();
    }

    // Ajouter un message à l'historique
    private void addMessage(String type, String text) {
        messageHistory.add(type + ":" + text);

        // Auto-scroll vers le bas (afficher les derniers messages)
        if (messageHistory.size() > maxVisibleMessages) {
            scrollOffset = messageHistory.size() - maxVisibleMessages;
        }
    }

    // Rafraîchir l'affichage du chat
    private void refreshChatDisplay() {
        app.enqueue(() -> {
            chatHistoryContainer.clearChildren();

            // Calculer quels messages afficher
            int start = Math.max(0, scrollOffset);
            int end = Math.min(messageHistory.size(), start + maxVisibleMessages);

            for (int i = start; i < end; i++) {
                String msg = messageHistory.get(i);
                String[] parts = msg.split(":", 2);
                if (parts.length == 2) {
                    String type = parts[0];
                    String text = parts[1];

                    Container msgContainer = new Container(new SpringGridLayout());
                    msgContainer.setInsets(new com.simsilica.lemur.Insets3f(10, 12, 10, 12));

                    Label label = new Label(text);
                    label.setFontSize(13);
                    label.setTextHAlignment(com.simsilica.lemur.HAlignment.Left);

                    switch (type) {
                        case "USER":
                            msgContainer.setBackground(new QuadBackgroundComponent(
                                    new com.jme3.math.ColorRGBA(0.25f, 0.35f, 0.55f, 0.9f)
                            ));
                            label.setText("You: " + text);
                            label.setColor(new com.jme3.math.ColorRGBA(1f, 1f, 1f, 1f));
                            break;
                        case "AI":
                            msgContainer.setBackground(new QuadBackgroundComponent(
                                    new com.jme3.math.ColorRGBA(0.20f, 0.45f, 0.35f, 0.9f)
                            ));
                            label.setText("🤖 AI: " + text);
                            label.setColor(new com.jme3.math.ColorRGBA(1f, 1f, 1f, 1f));
                            break;
                        case "LOADING":
                            msgContainer.setBackground(new QuadBackgroundComponent(
                                    new com.jme3.math.ColorRGBA(0.30f, 0.30f, 0.35f, 0.8f)
                            ));
                            label.setColor(new com.jme3.math.ColorRGBA(0.9f, 0.9f, 0.5f, 1f));
                            break;
                        default: // SYSTEM
                            msgContainer.setBackground(new QuadBackgroundComponent(
                                    new com.jme3.math.ColorRGBA(0.15f, 0.15f, 0.20f, 0.8f)
                            ));
                            label.setColor(new com.jme3.math.ColorRGBA(0.8f, 0.85f, 0.9f, 1f));
                            label.setTextHAlignment(com.simsilica.lemur.HAlignment.Center);
                            break;
                    }

                    msgContainer.addChild(label);
                    chatHistoryContainer.addChild(msgContainer);
                }
            }

            // Afficher un indicateur si on n'est pas au bas
            if (scrollOffset + maxVisibleMessages < messageHistory.size()) {
                Label moreLabel = new Label("▼ More messages below ▼");
                moreLabel.setFontSize(11);
                moreLabel.setColor(new com.jme3.math.ColorRGBA(0.6f, 0.6f, 0.7f, 1f));
                chatHistoryContainer.addChild(moreLabel);
            }

            return null;
        });
    }
}