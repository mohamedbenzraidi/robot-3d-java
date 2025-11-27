package org.example.scene;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.input.InputMapper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Application principale du Musée 3D Louvre
 */
public class JmeApp extends SimpleApplication {

    private SceneManager sceneManager;
    private float walkSpeed = 10f;
    private boolean moveForward, moveBackward, moveLeft, moveRight;
    private BitmapText crosshair;
    private BitmapText instructions;

    public static void main() {
        JmeApp app = new JmeApp();

        // Configuration de l'application
        AppSettings settings = new AppSettings(true);
        settings.setTitle("Museum 3D - Virtual Tour");
        settings.setResolution(1920, 1080);
        settings.setFullscreen(false);
        settings.setVSync(true);
        settings.setSamples(4); // Anti-aliasing

        try {
            BufferedImage[] icons = new BufferedImage[]{
                    ImageIO.read(new File("src/main/resources/Textures/museum-16.png")),
                    ImageIO.read(new File("src/main/resources/Textures/museum-32.png"))
            };
            settings.setIcons(icons);
        } catch (Exception e) {
            e.printStackTrace();
        }

        settings.setFullscreen(true);

        app.setSettings(settings);
        app.setShowSettings(false);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        // Désactiver les stats par défaut
        setDisplayFps(true);
        setDisplayStatView(false);

        // IMPORTANT: Initialiser Lemur AVANT toute utilisation
        com.simsilica.lemur.GuiGlobals.initialize(this);
//        GuiGlobals.getInstance().setCursorEventsEnabled(false);
        //  DÉSACTIVER le comportement par défaut de ESC
        if (inputManager.hasMapping(INPUT_MAPPING_EXIT)) {
            inputManager.deleteMapping(INPUT_MAPPING_EXIT);
        }

        // 1. Créer le réticule mais le CACHER
        createCrosshair();
        hideCrosshair();

        // 2. Initialiser le gestionnaire de scène
        sceneManager = new SceneManager(this);

        // 3. Charger les assets (le crosshair sera affiché dans le callback)
        sceneManager.setAssetsToLoad();

        // 4. Configurer la caméra
        setupCamera();

        // 5. Configurer les contrôles IMMÉDIATEMENT (pas après loading)
        setupControls();

        // 6. Cacher le curseur
        inputManager.setCursorVisible(false);

        System.out.println("✅ Application initialisée - En attente du chargement...");
    }

    public com.jme3.input.FlyByCamera getFlyByCamera() {
        return flyCam;
    }

    /**
     * ✅ Crée un réticule visible au centre de l'écran
     */
    private void createCrosshair() {
        BitmapFont font = assetManager.loadFont("Interface/Fonts/Default.fnt");

        // === RÉTICULE PRINCIPAL ===
        crosshair = new BitmapText(font);
        crosshair.setSize(font.getCharSet().getRenderedSize() * 2);
        crosshair.setText("+");
        crosshair.setColor(ColorRGBA.White);

        float x = cam.getWidth() / 2f - crosshair.getLineWidth() / 2f;
        float y = cam.getHeight() / 2f + crosshair.getLineHeight() / 2f;
        crosshair.setLocalTranslation(x, y, 0);

        guiNode.attachChild(crosshair);

        // === INSTRUCTIONS ===
        instructions = new BitmapText(font);
        instructions.setSize(font.getCharSet().getRenderedSize());
        instructions.setText("Visez un tableau avec le reticule (+) et cliquez (clic gauche)");
        instructions.setColor(ColorRGBA.Yellow);

        float instX = (cam.getWidth() - instructions.getLineWidth()) / 2f;
        instructions.setLocalTranslation(instX, cam.getHeight() - 30, 0);

        guiNode.attachChild(instructions);

        System.out.println("✅ Réticule créé à (" + x + ", " + y + ")");
        System.out.println("   Résolution cam: " + cam.getWidth() + "x" + cam.getHeight());
    }

    /**
     * ✅ Masque le réticule
     */
    private void hideCrosshair() {
        if (crosshair != null) {
            crosshair.setCullHint(Spatial.CullHint.Always);
        }
        if (instructions != null) {
            instructions.setCullHint(Spatial.CullHint.Always);
        }
    }

    /**
     * ✅ Affiche le réticule (appelé après le loading)
     */
    public void showCrosshair() {
        enqueue(() -> {
            if (crosshair != null) {
                crosshair.setCullHint(Spatial.CullHint.Never);
                System.out.println("✅ Crosshair affiché : " + crosshair.getText());
                System.out.println("   Parent : " + crosshair.getParent());
            } else {
                System.err.println("❌ Crosshair est NULL !");
            }

            if (instructions != null) {
                instructions.setCullHint(Spatial.CullHint.Never);
                System.out.println("✅ Instructions affichées");
            } else {
                System.err.println("❌ Instructions sont NULL !");
            }

            return null;
        });
    }

    /**
     * Configure la position et les paramètres de la caméra
     */
    private void setupCamera() {
        // Position de départ de la caméra
        cam.setLocation(new Vector3f(0, 3f, 40f));
        cam.lookAt(new Vector3f(0, 3f, 0), Vector3f.UNIT_Y);

        // Paramètres de la caméra
        cam.setFrustumPerspective(45f, (float) cam.getWidth() / cam.getHeight(), 0.1f, 1000f);

        // Vitesse de déplacement par défaut
        flyCam.setMoveSpeed(walkSpeed);
        flyCam.setEnabled(true);
    }

    /**
     * ✅ Configure les contrôles clavier et souris
     */
    private void setupControls() {
        // Mappings de déplacement
        inputManager.addMapping("Forward", new KeyTrigger(KeyInput.KEY_W), new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping("Backward", new KeyTrigger(KeyInput.KEY_S), new KeyTrigger(KeyInput.KEY_DOWN));
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A), new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D), new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("Crouch", new KeyTrigger(KeyInput.KEY_LSHIFT));

        // ✅ NOUVEAU : Mapping ESC personnalisé
        inputManager.addMapping("ESC_KEY", new KeyTrigger(KeyInput.KEY_ESCAPE));

        // Clic souris
        inputManager.addMapping("Click", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));

        // ✅ ActionListener mis à jour
        ActionListener actionListener = new ActionListener() {
            @Override
            public void onAction(String name, boolean isPressed, float tpf) {

                // ✅ GESTION PRIORITAIRE DE LA TOUCHE ESC
                if (name.equals("ESC_KEY") && !isPressed) {
                    // Si le chat panel est ouvert, le fermer
                    if (sceneManager != null && sceneManager.isChatPanelVisible()) {
                        System.out.println("🔐 ESC pressed - Closing chat panel");
                        sceneManager.closeChatPanel();
                    } else {
                        // Sinon, quitter l'application
                        System.out.println("🚪 ESC pressed - Exiting application");
                        stop();
                    }
                    return; // Sortir immédiatement
                }

                // ✅ Si le chat panel est ouvert, bloquer TOUS les autres contrôles
                if (sceneManager != null && sceneManager.isChatPanelVisible()) {
                    return; // Bloquer toutes les actions sauf ESC
                }

                // ✅ Vérifier que TOUT est chargé (pour les actions normales)
                if (sceneManager == null ||
                        sceneManager.getRobot() == null ||
                        !sceneManager.isSceneReady()) {

                    if (isPressed && name.equals("Click")) {
                        System.out.println("⏳ Veuillez attendre la fin du chargement...");
                    }
                    return;
                }

                // ✅ Gestion des contrôles normaux
                switch (name) {
                    case "Forward":
                        moveForward = isPressed;
                        break;
                    case "Backward":
                        moveBackward = isPressed;
                        break;
                    case "Left":
                        moveLeft = isPressed;
                        break;
                    case "Right":
                        moveRight = isPressed;
                        break;
                    case "Jump":
                        if (isPressed) {
                            Vector3f pos = cam.getLocation();
                            cam.setLocation(pos.add(0, 0.5f, 0));
                        }
                        break;
                    case "Crouch":
                        if (isPressed) {
                            flyCam.setMoveSpeed(walkSpeed * 0.5f);
                        } else {
                            flyCam.setMoveSpeed(walkSpeed);
                        }
                        break;
                    case "Click":
                        if (isPressed) {
                            System.out.println("\n🖱️ === CLIC DÉTECTÉ ===");
                            System.out.println("📍 Position caméra : " + cam.getLocation());
                            System.out.println("📐 Direction : " + cam.getDirection());
                            System.out.println("🤖 Position robot : " + sceneManager.getRobot().getLocalTranslation());

                            sceneManager.detectPaintingClick();
                        }
                        break;
                }
            }
        };

        // ✅ Enregistrer le listener avec ESC_KEY au lieu de Exit
        inputManager.addListener(actionListener, "Forward", "Backward", "Left", "Right",
                "Jump", "Crouch", "ESC_KEY", "Click");
    }

    @Override
    public void reshape(int width, int height) {
        super.reshape(width, height);

        // Recentrer le crosshair
        if (crosshair != null) {
            float x = cam.getWidth() / 2f - crosshair.getLineWidth() / 2f;
            float y = cam.getHeight() / 2f + crosshair.getLineHeight() / 2f;
            crosshair.setLocalTranslation(x, y, 0);

            System.out.println("🔄 Crosshair recentré après reshape: (" + x + ", " + y + ")");
        }

        if (instructions != null) {
            float instX = (cam.getWidth() - instructions.getLineWidth()) / 2f;
            instructions.setLocalTranslation(instX, cam.getHeight() - 30, 0);
        }
    }

    private Vector3f lastCamPos = new Vector3f();
    private boolean isMoving = false;

    @Override
    public void simpleUpdate(float tpf) {
        // ✅ Vérifier que sceneManager est initialisé
        if (sceneManager == null || sceneManager.getRobot() == null) {
            return; // Attendre que le loading soit terminé
        }

        Vector3f camPos = cam.getLocation();

        // ✅ Ne pas animer le robot si le chat panel est ouvert
        if (!sceneManager.isChatPanelVisible()) {
            if (camPos.distance(lastCamPos) > 0.05f) {
                if (!isMoving) {
                    sceneManager.playAnimation("Walk");
                    isMoving = true;
                }
            } else {
                if (isMoving) {
                    sceneManager.playAnimation("Idle");
                    isMoving = false;
                }
            }
        }

        lastCamPos.set(camPos.clone());

        // Limites de hauteur
        if (camPos.y < 2f) cam.setLocation(new Vector3f(camPos.x, 2f, camPos.z));
        if (camPos.y > 8f) cam.setLocation(new Vector3f(camPos.x, 8f, camPos.z));

        // ✅ Robot DEVANT la caméra
        Vector3f camDirection = cam.getDirection().normalize();
        Vector3f robotPos = sceneManager.getRobot().getLocalTranslation();

        float distanceInFront = 3.5f;

        Vector3f offset = new Vector3f(
                camDirection.x * distanceInFront,
                0,
                camDirection.z * distanceInFront
        );

        Vector3f newPos = new Vector3f(
                camPos.x + offset.x,
                robotPos.y,
                camPos.z + offset.z
        );

        sceneManager.getRobot().setLocalTranslation(newPos);
        sceneManager.getRobot().lookAt(camPos, Vector3f.UNIT_Y);

        // ✅ Mettre à jour la scène (bulle, timer, etc.)
        sceneManager.update(tpf, cam);

        // ✅ Mettre à jour la couleur du réticule
        if (crosshair != null && !sceneManager.isChatPanelVisible()) {
            boolean lookingAt = sceneManager.isLookingAtPainting();
            crosshair.setColor(lookingAt ? ColorRGBA.Green : ColorRGBA.White);
        }
    }
}