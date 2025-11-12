package org.example.scene;

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.system.AppSettings;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class JmeApp extends SimpleApplication {

    private SceneManager sceneManager;
    private float walkSpeed = 10f;
    private boolean moveForward, moveBackward, moveLeft, moveRight;
    private BitmapText crosshair;

    public static void main() {
        JmeApp app = new JmeApp();

        AppSettings settings = new AppSettings(true);
        settings.setTitle("Museum 3D - Virtual Tour");
        settings.setResolution(1920, 1080);
        settings.setFullscreen(false);
        settings.setVSync(true);
        settings.setSamples(4);

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

        app.setSettings(settings);
        app.setShowSettings(false);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        setDisplayFps(true);
        setDisplayStatView(false);

        // Initialiser le gestionnaire de scène
        sceneManager = new SceneManager(this);
        sceneManager.initializeScene();
        sceneManager.initializeClickDetection();

        // Configurer la caméra
        setupCamera();

        // Configurer les contrôles
        setupControls();

        // ✅ NOUVEAU : Créer le réticule
        createCrosshair();

        // Cacher le curseur
        inputManager.setCursorVisible(false);
    }

    /**
     * ✅ Crée un réticule au centre de l'écran
     */
    private void createCrosshair() {
        BitmapFont font = assetManager.loadFont("Interface/Fonts/Default.fnt");
        crosshair = new BitmapText(font, false);
        crosshair.setSize(font.getCharSet().getRenderedSize() * 2);
        crosshair.setText("+");
        crosshair.setColor(ColorRGBA.White);

        // Positionner au centre de l'écran
        float x = settings.getWidth() / 2f - crosshair.getLineWidth() / 2f;
        float y = settings.getHeight() / 2f + crosshair.getLineHeight() / 2f;
        crosshair.setLocalTranslation(x, y, 0);

        guiNode.attachChild(crosshair);

        // ✅ Message d'instructions
        BitmapText instructions = new BitmapText(font, false);
        instructions.setSize(font.getCharSet().getRenderedSize());
        instructions.setText("Visez un tableau avec le réticule (+) et cliquez (clic gauche)");
        instructions.setColor(ColorRGBA.Yellow);
        instructions.setLocalTranslation(10, settings.getHeight() - 30, 0);
        guiNode.attachChild(instructions);
    }

    private void setupCamera() {
        cam.setLocation(new Vector3f(0, 3f, 40f));
        cam.lookAt(new Vector3f(0, 3f, 0), Vector3f.UNIT_Y);
        cam.setFrustumNear(0.1f);
        cam.setFrustumPerspective(45f, (float) cam.getWidth() / cam.getHeight(), 0.1f, 1000f);
        flyCam.setMoveSpeed(walkSpeed);
        flyCam.setEnabled(true);
    }

    private void setupControls() {
        inputManager.addMapping("Forward", new KeyTrigger(KeyInput.KEY_W), new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping("Backward", new KeyTrigger(KeyInput.KEY_S), new KeyTrigger(KeyInput.KEY_DOWN));
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A), new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D), new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("Crouch", new KeyTrigger(KeyInput.KEY_LSHIFT));
        inputManager.addMapping("Exit", new KeyTrigger(KeyInput.KEY_ESCAPE));

        // ✅ NOUVEAU : Mapping pour le clic
        inputManager.addMapping("Click", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));

        ActionListener actionListener = new ActionListener() {
            @Override
            public void onAction(String name, boolean isPressed, float tpf) {
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
                    case "Exit":
                        if (isPressed) {
                            stop();
                        }
                        break;
                    // ✅ NOUVEAU : Gérer le clic
                    case "Click":
                        if (isPressed) {
                            sceneManager.detectPaintingClick();
                        }
                        break;
                }
            }
        };

        inputManager.addListener(actionListener, "Forward", "Backward", "Left", "Right",
                "Jump", "Crouch", "Exit", "Click");
    }

    private Vector3f lastCamPos = new Vector3f();
    private boolean isMoving = false;

    @Override
    public void simpleUpdate(float tpf) {
        Vector3f camPos = cam.getLocation();

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

        lastCamPos.set(camPos.clone());

        if (camPos.y < 2f) cam.setLocation(new Vector3f(camPos.x, 2f, camPos.z));
        if (camPos.y > 8f) cam.setLocation(new Vector3f(camPos.x, 8f, camPos.z));

        // ✅ Robot DEVANT la caméra
        if(sceneManager.getRobot() != null) {
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
        }

        // ✅ NOUVEAU : Mettre à jour la bulle d'info et le mouvement du robot
        sceneManager.update(tpf, cam);

        // ✅ NOUVEAU : Changer couleur du réticule
        updateCrosshairColor();
    }

    /**
     * ✅ Change la couleur du réticule selon ce qu'on vise
     */
    private void updateCrosshairColor() {
        if (sceneManager.isLookingAtPainting()) {
            crosshair.setColor(ColorRGBA.Red);
        } else {
            crosshair.setColor(ColorRGBA.White);
        }
    }
}