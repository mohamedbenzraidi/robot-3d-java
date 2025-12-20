package org.example.scene;


import com.jme3.app.SimpleApplication;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeCanvasContext;

import com.jme3.scene.plugins.gltf.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
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
    private MetSceneManager metSceneManager;
    private float minDistance = 2.5f;

    public static void main() {
        JmeApp app = new JmeApp();
        app.setSettings(setSettings());
        app.setShowSettings(false); // Ne pas afficher le panneau de config au démarrage
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


    public static AppSettings setSettings(){
        AppSettings settings = new AppSettings(true);
        settings.setTitle("Museum 3D - Virtual Tour");
        settings.setResolution(1920, 1080);
        settings.setVSync(true);
        settings.setSamples(4); // Anti-aliasing

        try{
            BufferedImage[] icons = new BufferedImage[]{
                    ImageIO.read(new File("src/main/resources/Textures/museum-16.png")),
                    ImageIO.read(new File("src/main/resources/Textures/museum-32.png"))
            };
            settings.setIcons(icons);
        } catch (Exception e) {
            e.printStackTrace();
        }

        settings.setFullscreen(true);

        return settings;
    }

    public com.jme3.input.FlyByCamera getFlyByCamera() {
        return flyCam;
    }

    /**
     * ✅ Crée un réticule au centre de l'écran
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
     * Configure la position et les paramètres de la caméra
     */
    private void setupCamera() {
        // Position de départ de la caméra
        cam.setLocation(new Vector3f(0, 3f, 30f));
        cam.lookAt(new Vector3f(0, 3f, 0), Vector3f.UNIT_Y);

        // Paramètres de la caméra
        cam.setFrustumPerspective(45f, (float) cam.getWidth() / cam.getHeight(), 0.1f, 1000f);

        flyCam.setEnabled(true);

        flyCam.setMoveSpeed(0);
        flyCam.unregisterInput();
        // Vitesse de déplacement par défaut
//        flyCam.setMoveSpeed(walkSpeed);

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
        // 1. Safety Check: Don't do anything if scene isn't loaded
        if (sceneManager == null || !sceneManager.isSceneReady() || sceneManager.getRobot() == null) {
            return;
        }

        // 2. Stop movement if Chat Panel is open
        if (sceneManager.isChatPanelVisible()) {
            // ✅ NEW: While chat is open, force robot to look at the player
            Spatial robot = sceneManager.getRobot();
            if (robot != null) {
                robot.lookAt(cam.getLocation(), Vector3f.UNIT_Y);
            }
            // Also call update to keep the bubble floating correctly
            sceneManager.update(tpf, cam);
            return;
        }

        // ==========================================
        // 🛑 COLLISION DETECTION LOGIC
        // ==========================================
        boolean blockedForward = false;

        if (moveForward) {
            Vector3f rayDir = cam.getDirection().clone();
            rayDir.y = 0;
            rayDir.normalizeLocal();

            Ray ray = new Ray(cam.getLocation(), rayDir);
            // Increase limit slightly to ensure we catch walls
            ray.setLimit(minDistance + 2f);

            CollisionResults results = new CollisionResults();
            sceneManager.getGalleryNode().collideWith(ray, results);

            if (results.size() > 0) {
                CollisionResult closest = results.getClosestCollision();

                // --- DEBUG PRINT ---
                System.out.println("⚠️ HIT: " + closest.getGeometry().getName());
                System.out.println("   Distance: " + closest.getDistance());
                // -------------------

                if (closest.getDistance() < minDistance) {
                    blockedForward = true;
                    System.out.println("⛔ BLOCKED! Too close to wall.");
                }
            } else {
                System.out.println("✅ Path Clear");
            }
        }

        // ==========================================
        // 🚶 MANUAL MOVEMENT LOGIC
        // ==========================================
        Vector3f camDir = cam.getDirection().clone().multLocal(walkSpeed * tpf);
        Vector3f camLeft = cam.getLeft().clone().multLocal(walkSpeed * tpf);

        // Flatten to X/Z plane (FPS style walking)
        camDir.y = 0;
        camLeft.y = 0;

        Vector3f walkDirection = new Vector3f(0, 0, 0);

        // 3. Normalize! (CRITICAL FIX: This ensures consistent speed even if looking up/down)
        camDir.normalizeLocal().multLocal(walkSpeed * tpf);
        camLeft.normalizeLocal().multLocal(walkSpeed * tpf);

        // 4. Apply movement
        // "W" key
        if (moveForward && !blockedForward) {
            walkDirection.addLocal(camDir);
        }
        // "S" key (Always allowed)
        if (moveBackward) {
            walkDirection.addLocal(camDir.negate());
        }
        // "A" key
        if (moveLeft) {
            walkDirection.addLocal(camLeft);
        }
        // "D" key
        if (moveRight) {
            walkDirection.addLocal(camLeft.negate());
        }

        // 5. Final Application
        cam.setLocation(cam.getLocation().add(walkDirection));

        // ==========================================
        // 🤖 FLOATING ROBOT LOGIC
        // ==========================================
        Spatial robot = sceneManager.getRobot();

        if (robot != null) {
            Vector3f camPos = cam.getLocation();
            Vector3f camDirection = cam.getDirection();
            Vector3f camLeftSide = cam.getLeft();

            // Position: Forward and to the Right of camera
            Vector3f forwardOffset = camDirection.mult(2.0f);
            Vector3f rightOffset = camLeftSide.mult(-1.2f); // Negative Left = Right

            Vector3f newRobotPos = camPos.add(forwardOffset).add(rightOffset);
            newRobotPos.y = camPos.y - 0.5f;

            robot.setLocalTranslation(newRobotPos);

            // ✅ NEW: Logic for rotation when chat is NOT open (Default)
            // Look parallel to the camera (forward) - appears as looking "left" relative to the robot's body
            Vector3f lookTarget = camPos.add(forwardOffset).add(camDirection.mult(10f));
            lookTarget.y = newRobotPos.y;

            robot.lookAt(lookTarget, Vector3f.UNIT_Y);
        }

        sceneManager.update(tpf, cam);

        if (crosshair != null) {
            boolean lookingAt = sceneManager.isLookingAtPainting();
            crosshair.setColor(lookingAt ? ColorRGBA.Green : ColorRGBA.White);
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

}
