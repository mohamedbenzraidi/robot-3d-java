package org.example.scene;

import com.jme3.app.SimpleApplication;
import com.jme3.math.Vector3f;
import com.jme3.math.ColorRGBA;
import com.simsilica.lemur.*;
import com.simsilica.lemur.component.BorderLayout;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import com.simsilica.lemur.component.SpringGridLayout;
import com.simsilica.lemur.component.TextEntryComponent;
import com.simsilica.lemur.event.CursorEventControl;
import com.simsilica.lemur.event.DragHandler;
import com.simsilica.lemur.event.KeyAction;
import com.simsilica.lemur.event.KeyActionListener;
import org.example.TTS.TTSService;
import org.example.ai.ChatBot;
import org.example.config.ConfigLoader;

public class ChatPanelUI {

    private final SimpleApplication app;
    private final Object sceneManager;

    private Container mainContainer;
    private TextField chatInput;
    private TextField apiKeyInput;

    // UI Elements for the "Audio HUD" style
    private Label statusLabel;
    private Label lastQueryLabel;
    private Label contextLabel;
    private Container statusContainer;

    private ChatBot chatBot;
    private String currentPaintingContext;

    public ChatPanelUI(SimpleApplication app, Object sceneManager) {
        this.app = app;
        this.sceneManager = sceneManager;
        initialize();
    }

    private void initialize() {
        // Main Container - Compact HUD style
        mainContainer = new Container(new BorderLayout());
        mainContainer.setPreferredSize(new Vector3f(500, 320, 0)); // Increased height slightly for spacing

        // Modern, slightly transparent dark background
        mainContainer.setBackground(new QuadBackgroundComponent(
                new ColorRGBA(0.05f, 0.05f, 0.05f, 0.9f)
        ));

        // ============ HEADER ============
        Container header = new Container(new SpringGridLayout());
        header.setBackground(new QuadBackgroundComponent(
                new ColorRGBA(0.1f, 0.12f, 0.15f, 0.9f)
        ));
        header.setInsets(new Insets3f(10, 15, 10, 15));

        Label titleLabel = header.addChild(new Label("🎧 AI Audio Guide"));
        titleLabel.setFontSize(20);
        titleLabel.setColor(ColorRGBA.White);
        mainContainer.addChild(header, BorderLayout.Position.North);

        // ============ STATUS CENTER ============
        statusContainer = new Container(new SpringGridLayout(Axis.Y, Axis.X, FillMode.None, FillMode.Even));
        statusContainer.setInsets(new Insets3f(20, 20, 20, 20));
        statusContainer.setBackground(new QuadBackgroundComponent(new ColorRGBA(0,0,0,0)));

        // --- NEW SECTION: CONTEXT PIN ---
        Label contextTitle = statusContainer.addChild(new Label("Current Focus:"));
        contextTitle.setFontSize(12);
        contextTitle.setColor(new ColorRGBA(0.6f, 0.6f, 0.6f, 1f));
        // ✅ ADDED: Small margin between "Current Focus" and the actual name
        contextTitle.setInsets(new Insets3f(0, 0, 3, 0));

        contextLabel = statusContainer.addChild(new Label("Unknown"));
        contextLabel.setFontSize(16);
        contextLabel.setColor(new ColorRGBA(1f, 0.8f, 0.0f, 1f)); // Gold/Orange color
        // ✅ UPDATED: Increased bottom margin (20f) to separate from "Last Inquiry"
        contextLabel.setInsets(new Insets3f(0, 0, 20, 0));
        // --------------------------------

        // 1. Label to show what the user just asked (Confirmation)
        Label youAskedTitle = statusContainer.addChild(new Label("Last Inquiry:"));
        youAskedTitle.setFontSize(12);
        youAskedTitle.setColor(new ColorRGBA(0.6f, 0.6f, 0.6f, 1f));
        youAskedTitle.setInsets(new Insets3f(0, 0, 3, 0));

        lastQueryLabel = statusContainer.addChild(new Label("..."));
        lastQueryLabel.setFontSize(14);
        lastQueryLabel.setColor(new ColorRGBA(0.8f, 0.9f, 1f, 1f));
        lastQueryLabel.setInsets(new Insets3f(0, 0, 15, 0)); // Spacing before status

        // 2. Big Status Indicator (Thinking / Speaking)
        statusLabel = statusContainer.addChild(new Label("Ready"));
        statusLabel.setFontSize(24);
        statusLabel.setTextHAlignment(HAlignment.Center);
        statusLabel.setColor(new ColorRGBA(0.5f, 1f, 0.5f, 1f)); // Start Green

        mainContainer.addChild(statusContainer, BorderLayout.Position.Center);

        // ============ INPUT AREA ============
        Container bottom = new Container(new SpringGridLayout());
        bottom.setInsets(new Insets3f(10, 15, 15, 15));

        // API Key Input (Hidden by default)
        apiKeyInput = bottom.addChild(new TextField("probleme d'API..."));
        apiKeyInput.setPreferredSize(new Vector3f(470, 30, 0));
        apiKeyInput.setBackground(new QuadBackgroundComponent(new ColorRGBA(0.2f, 0.1f, 0.1f, 1f)));

        Container inputRow = bottom.addChild(new Container(new SpringGridLayout(Axis.X, Axis.Y)));

        chatInput = inputRow.addChild(new TextField("Ask a question..."));
        chatInput.setPreferredSize(new Vector3f(370, 35, 0));
        chatInput.setFontSize(14);
        chatInput.setColor(ColorRGBA.White);

        Button sendBtn = inputRow.addChild(new Button("Speak ➤"));
        sendBtn.setPreferredSize(new Vector3f(80, 35, 0));
        sendBtn.setBackground(new QuadBackgroundComponent(new ColorRGBA(0.2f, 0.4f, 0.8f, 1f)));
        sendBtn.addClickCommands(btn -> onSend());

        mainContainer.addChild(bottom, BorderLayout.Position.South);

        // Enable Dragging
        CursorEventControl.addListenersToSpatial(mainContainer, new DragHandler());

        KeyActionListener sendListener = new KeyActionListener() {
            @Override
            public void keyAction(TextEntryComponent source, KeyAction key) {
                onSend();
            }
        };

        chatInput.getActionMap().put(new KeyAction(com.jme3.input.KeyInput.KEY_RETURN), sendListener);
        chatInput.getActionMap().put(new KeyAction(com.jme3.input.KeyInput.KEY_NUMPADENTER), sendListener);
    }

    public void show(String paintingContext) {
        this.currentPaintingContext = paintingContext;

        if (contextLabel != null) {
            String displayText = (paintingContext != null && !paintingContext.isEmpty())
                    ? paintingContext
                    : "General Museum Guide";
            contextLabel.setText(displayText);
        }

        // Initialize ChatBot
        if (chatBot == null) {
            String key = ConfigLoader.get("google.api.key");
            if (key != null && !key.isEmpty()) {
                try {
                    chatBot = new ChatBot(key);
                    if (apiKeyInput.getParent() != null) apiKeyInput.removeFromParent();
                } catch (Exception e) {
                    System.err.println("❌ Error initializing ChatBot");
                }
            }
        }

        if (!app.getGuiNode().hasChild(mainContainer)) {
            app.getGuiNode().attachChild(mainContainer);
        }

        // Center-Right positioning
        float x = app.getCamera().getWidth() - mainContainer.getPreferredSize().x - 20;
        float y = app.getCamera().getHeight() / 1.5f;
        mainContainer.setLocalTranslation(x, y, 0);

        // Reset UI State
        setStatus("Ready", ColorRGBA.Green);
        lastQueryLabel.setText("-");

        // Disable Camera / Enable Cursor
        setCameraEnabled(false);

        // Focus Input
        app.enqueue(() -> {
            com.simsilica.lemur.GuiGlobals.getInstance().requestFocus(chatInput);
            chatInput.setText("");
            return null;
        });
    }

    public void close() {
        if (app.getGuiNode().hasChild(mainContainer)) {
            app.getGuiNode().detachChild(mainContainer);
        }
        setCameraEnabled(true);
    }

    public boolean isVisible() {
        return app.getGuiNode().hasChild(mainContainer);
    }

    private void onSend() {
        String message = chatInput.getText().trim();
        if (message.isEmpty() || message.equals("Ask a question...")) return;

        // API Key Check
        if (chatBot == null) {
            String key = apiKeyInput.getText().trim();
            if (key.isEmpty() || key.startsWith("prob")) {
                setStatus("⚠️ Need API Key", ColorRGBA.Red);
                return;
            }
            try {
                chatBot = new ChatBot(key);
                apiKeyInput.removeFromParent();
            } catch (Exception e) {
                setStatus("❌ Invalid Key", ColorRGBA.Red);
                return;
            }
        }

        // Update HUD
        lastQueryLabel.setText("\"" + message + "\""); // Show user what they asked
        chatInput.setText("");
        setStatus("⏳ Thinking...", ColorRGBA.Yellow);

        // AI Thread
        new Thread(() -> {
            try {
                String reply;
                String museumName = "Museum"; // Default

                if(sceneManager instanceof SceneManager) museumName = "musée du louvre";
                else if(sceneManager instanceof MetSceneManager) museumName = "Metropolitan Museum of Art";
                else if(sceneManager instanceof ThSceneManager) museumName = "Musée d'Orsay";

                // 1. Get Text Response (Invisible to user)
                reply = chatBot.sendMessage(message, currentPaintingContext, museumName);

                app.enqueue(() -> {
                    // 2. Play Audio
                    TTSService.say(reply);

                    // 3. Update Visual Status (No text log)
                    setStatus("🔊 Speaking...", new ColorRGBA(0.4f, 0.8f, 1f, 1f));

                    // 4. Update Robot Bubble (if needed by the specific scene manager)
                    if(sceneManager instanceof SceneManager) ((SceneManager) sceneManager).showRobotSpeech(reply);
                    else if(sceneManager instanceof MetSceneManager) ((MetSceneManager) sceneManager).showRobotSpeech(reply);
                    else if(sceneManager instanceof ThSceneManager) ((ThSceneManager) sceneManager).showRobotSpeech(reply);

                });

            } catch (Exception e) {
                e.printStackTrace();
                app.enqueue(() -> setStatus("❌ Connection Error", ColorRGBA.Red));
            }
        }).start();
    }

    // Helper to update the big status label
    private void setStatus(String text, ColorRGBA color) {
        statusLabel.setText(text);
        statusLabel.setColor(color);
    }

    // Helper to handle the specific camera casting logic
    private void setCameraEnabled(boolean enabled) {
        app.getInputManager().setCursorVisible(!enabled);

        if (sceneManager instanceof SceneManager) {
            ((JmeApp) app).getFlyByCamera().setEnabled(enabled);
        } else if (sceneManager instanceof MetSceneManager) {
            ((JmeMetApp) app).getFlyByCamera().setEnabled(enabled);
        } else if (sceneManager instanceof ThSceneManager) {
            ((JmeThApp) app).getFlyByCamera().setEnabled(enabled);
        }
    }
}