package org.example.scene;

import com.jme3.app.SimpleApplication;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;


/**
 * Application principale du Musée 3D Louvre
 */
public class JmeMetApp extends SimpleApplication {

    private float walkSpeed = 10f;
    private boolean moveForward, moveBackward, moveLeft, moveRight;
    private BitmapText crosshair;
    private BitmapText instructions;
    private MetSceneManager metSceneManager;
    private float minDistance = 2.5f;

    public static void main() {
        JmeMetApp app = new JmeMetApp();
        app.setSettings(setSettings());
        app.setShowSettings(false);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        setDisplayFps(true);
        setDisplayStatView(false);

        com.simsilica.lemur.GuiGlobals.initialize(this);
        if (inputManager.hasMapping(INPUT_MAPPING_EXIT)) {
            inputManager.deleteMapping(INPUT_MAPPING_EXIT);
        }

        createCrosshair();
        hideCrosshair();

        metSceneManager = new MetSceneManager(this);
        metSceneManager.setAssetsToLoad();

        setupCamera();
        setupControls();

        inputManager.setCursorVisible(false);
        System.out.println("✅ Application initialisée - En attente du chargement...");
    }

    public static AppSettings setSettings(){
        AppSettings settings = new AppSettings(true);
        settings.setTitle("Museum 3D - Virtual Tour");
        settings.setResolution(1920, 1080);
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
        return settings;
    }

    public com.jme3.input.FlyByCamera getFlyByCamera() {
        return flyCam;
    }

    private void createCrosshair() {
        BitmapFont font = assetManager.loadFont("Interface/Fonts/Default.fnt");

        crosshair = new BitmapText(font);
        crosshair.setSize(font.getCharSet().getRenderedSize() * 2);
        crosshair.setText("+");
        crosshair.setColor(ColorRGBA.White);

        float x = cam.getWidth() / 2f - crosshair.getLineWidth() / 2f;
        float y = cam.getHeight() / 2f + crosshair.getLineHeight() / 2f;
        crosshair.setLocalTranslation(x, y, 0);

        guiNode.attachChild(crosshair);

        instructions = new BitmapText(font);
        instructions.setSize(font.getCharSet().getRenderedSize());
        instructions.setText("Visez un tableau avec le reticule (+) et cliquez (clic gauche)");
        instructions.setColor(ColorRGBA.Yellow);

        float instX = (cam.getWidth() - instructions.getLineWidth()) / 2f;
        instructions.setLocalTranslation(instX, cam.getHeight() - 30, 0);

        guiNode.attachChild(instructions);
    }

    private void hideCrosshair() {
        if (crosshair != null) {
            crosshair.setCullHint(Spatial.CullHint.Always);
        }
        if (instructions != null) {
            instructions.setCullHint(Spatial.CullHint.Always);
        }
    }

    private void setupCamera() {
//        cam.setLocation(new Vector3f(0, 3f, 30f));
        cam.setLocation(new Vector3f(0, 1f, 30f));
        cam.lookAt(new Vector3f(0, 3f, 0), Vector3f.UNIT_Y);
        cam.setFrustumPerspective(45f, (float) cam.getWidth() / cam.getHeight(), 0.1f, 1000f);
        flyCam.setEnabled(true);
        flyCam.setMoveSpeed(0);
        flyCam.unregisterInput();
    }

    private void setupControls() {
        inputManager.addMapping("Forward", new KeyTrigger(KeyInput.KEY_W), new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping("Backward", new KeyTrigger(KeyInput.KEY_S), new KeyTrigger(KeyInput.KEY_DOWN));
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A), new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D), new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE), new KeyTrigger(KeyInput.KEY_Q));
        inputManager.addMapping("Crouch", new KeyTrigger(KeyInput.KEY_LSHIFT));
        inputManager.addMapping("ESC_KEY", new KeyTrigger(KeyInput.KEY_ESCAPE));
        inputManager.addMapping("Click", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_E));

        ActionListener actionListener = new ActionListener() {
            @Override
            public void onAction(String name, boolean isPressed, float tpf) {
                if (name.equals("ESC_KEY") && !isPressed) {
                    if (metSceneManager  != null && metSceneManager .isChatPanelVisible()) {
                        metSceneManager .closeChatPanel();
                    } else {
                        stop();
                    }
                    return;
                }

                if (metSceneManager  != null && metSceneManager .isChatPanelVisible()) {
                    return;
                }

                if (metSceneManager  == null ||
                        metSceneManager .getRobot() == null ||
                        !metSceneManager .isSceneReady()) {
                    return;
                }

                switch (name) {
                    case "Forward": moveForward = isPressed; break;
                    case "Backward": moveBackward = isPressed; break;
                    case "Left": moveLeft = isPressed; break;
                    case "Right": moveRight = isPressed; break;
                    case "Jump":
                        if (isPressed) {
                            Vector3f pos = cam.getLocation();
                            cam.setLocation(pos.add(0, 0.5f, 0));
                        }
                        break;
                    case "Down":
                        if (isPressed) {
                            Vector3f pos = cam.getLocation();
                            cam.setLocation(pos.add(0, -0.5f, 0));
                        }
                        break;
                    case "Crouch":
                        if (isPressed) flyCam.setMoveSpeed(walkSpeed * 0.5f);
                        else flyCam.setMoveSpeed(walkSpeed);
                        break;
                    case "Click":
                        if (isPressed) metSceneManager.detectPaintingClick();
                        break;
                }
            }
        };

        inputManager.addListener(actionListener, "Forward", "Backward", "Left", "Right",
                "Jump", "Crouch", "ESC_KEY", "Click", "Down");
    }

    @Override
    public void reshape(int width, int height) {
        super.reshape(width, height);
        if (crosshair != null) {
            float x = cam.getWidth() / 2f - crosshair.getLineWidth() / 2f;
            float y = cam.getHeight() / 2f + crosshair.getLineHeight() / 2f;
            crosshair.setLocalTranslation(x, y, 0);
        }
        if (instructions != null) {
            float instX = (cam.getWidth() - instructions.getLineWidth()) / 2f;
            instructions.setLocalTranslation(instX, cam.getHeight() - 30, 0);
        }
    }

    @Override
    public void simpleUpdate(float tpf) {
        // 1. Safety Check
        if (metSceneManager == null || !metSceneManager.isSceneReady() || metSceneManager.getRobot() == null) {
            return;
        }

        // 2. Stop movement if Chat Panel is open
        if (metSceneManager.isChatPanelVisible()) {
            // ✅ NEW: While chat is open, force robot to look at the player
            Spatial robot = metSceneManager.getRobot();
            if (robot != null) {
                robot.lookAt(cam.getLocation(), Vector3f.UNIT_Y);
            }
            // Also call update to keep the bubble floating correctly
            metSceneManager.update(tpf, cam);
            return;
        }


        boolean blockedForward = false;

        if (moveForward) {
            Vector3f rayDir = cam.getDirection().clone();
            rayDir.y = 0;
            rayDir.normalizeLocal();

            Ray ray = new Ray(cam.getLocation(), rayDir);
            ray.setLimit(minDistance + 2f);

            CollisionResults results = new CollisionResults();
            metSceneManager.getGalleryNode().collideWith(ray, results);

            if (results.size() > 0 && results.getClosestCollision().getDistance() < minDistance) {
                blockedForward = true;
            }
        }

        Vector3f camDir = cam.getDirection().clone().multLocal(walkSpeed * tpf);
        Vector3f camLeft = cam.getLeft().clone().multLocal(walkSpeed * tpf);

        camDir.y = 0;
        camLeft.y = 0;

        Vector3f walkDirection = new Vector3f(0, 0, 0);
        camDir.normalizeLocal().multLocal(walkSpeed * tpf);
        camLeft.normalizeLocal().multLocal(walkSpeed * tpf);

        if (moveForward && !blockedForward) walkDirection.addLocal(camDir);
        if (moveBackward) walkDirection.addLocal(camDir.negate());
        if (moveLeft) walkDirection.addLocal(camLeft);
        if (moveRight) walkDirection.addLocal(camLeft.negate());

        cam.setLocation(cam.getLocation().add(walkDirection));

        // ==========================================
        // 🤖 FLOATING ROBOT LOGIC
        // ==========================================
        Spatial robot = metSceneManager.getRobot();

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

        metSceneManager.update(tpf, cam);

        if (crosshair != null) {
            boolean lookingAt = metSceneManager.isLookingAtPainting();
            crosshair.setColor(lookingAt ? ColorRGBA.Green : ColorRGBA.White);
        }
    }

    public void showCrosshair() {
        enqueue(() -> {
            if (crosshair != null) crosshair.setCullHint(Spatial.CullHint.Never);
            if (instructions != null) instructions.setCullHint(Spatial.CullHint.Never);
            return null;
        });
    }
}