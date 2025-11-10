package org.example.scene;


import com.jme3.app.SimpleApplication;
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
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeCanvasContext;

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

    public static void main() {
        JmeApp app = new JmeApp();

        // Configuration de l'application
        AppSettings settings = new AppSettings(true);
        settings.setTitle("Museum 3D - Virtual Tour");
        settings.setResolution(1920, 1080);
        settings.setFullscreen(false);
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

//        settings.setFullscreen(true);

        app.setSettings(settings);
        app.setShowSettings(false); // Ne pas afficher le panneau de config au démarrage
        app.start();
    }

    @Override
    public void simpleInitApp() {
        // Désactiver les stats par défaut si vous voulez une vue plus propre
        setDisplayFps(true);
        setDisplayStatView(false);

        // Initialiser le gestionnaire de scène
        sceneManager = new SceneManager(this);
        sceneManager.initializeScene();

        // Configurer la caméra
        setupCamera();

        // Configurer les contrôles
        setupControls();

        // Cacher le curseur pour une expérience immersive
        inputManager.setCursorVisible(false);

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
     * Configure les contrôles clavier et souris
     */
    private void setupControls() {
        // Mapper les touches
        inputManager.addMapping("Forward", new KeyTrigger(KeyInput.KEY_W), new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping("Backward", new KeyTrigger(KeyInput.KEY_S), new KeyTrigger(KeyInput.KEY_DOWN));
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A), new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D), new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("Crouch", new KeyTrigger(KeyInput.KEY_LSHIFT));
        inputManager.addMapping("Exit", new KeyTrigger(KeyInput.KEY_ESCAPE));

        // Listener pour les actions
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
                            // Sauter
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
                }
            }
        };

        inputManager.addListener(actionListener, "Forward", "Backward", "Left", "Right",
                "Jump", "Crouch", "Exit");
    }

    @Override
    public void simpleUpdate(float tpf) {
        // Vous pouvez ajouter ici la logique de mise à jour personnalisée
        // Par exemple, limiter la hauteur de la caméra pour simuler la marche
        Vector3f camPos = cam.getLocation();
        if (camPos.y < 2f) {
            cam.setLocation(new Vector3f(camPos.x, 2f, camPos.z));
        }
        if (camPos.y > 8f) {
            cam.setLocation(new Vector3f(camPos.x, 8f, camPos.z));
        }
    }


}
