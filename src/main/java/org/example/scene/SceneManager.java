package org.example.scene;

import com.jme3.anim.AnimComposer;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.SpotLight;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;

import org.example.DB.DBManager;
import org.example.robot.RobotManager;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Gestionnaire de scène pour le musée 3D du Louvre
 * Crée une grande galerie avec arches, lumières et tableaux
 */
public class SceneManager {

    private final SimpleApplication app;
    private final AssetManager assetManager;
    private final Node rootNode;
    private final Node galleryNode;
    private RobotManager robotManager;
    private Spatial robot;
    private AnimComposer animComposer;
    private AssetLoader assetLoader;
    private DBManager db;

    // ✅ Variables pour la bulle d'information
    private Node infoBubbleNode;
    private BitmapText bubbleText;
    private Geometry bubbleBackground;
    private float bubbleDisplayTime = 0f;
    private static final float BUBBLE_DURATION = 8f; // 8 secondes d'affichage
    private boolean sceneReady = false;

    // ✅ Chat Panel UI
    private ChatPanelUI chatPanelUI;

    // ✅ Variables pour le mouvement du robot
    private boolean robotWalking = false;
    private float robotWalkTime = 0f;
    private static final float ROBOT_WALK_DURATION = 2f; // 2 secondes de marche
    private Vector3f robotTargetPosition = new Vector3f();
    private Vector3f robotStartPosition = new Vector3f();
    private float robotWalkProgress = 0f;

    // ✅ NOUVEAU : Tableau actif et robot qui reste
    private Geometry currentActivePainting = null;
    private boolean robotStayingNearPainting = false;


    // Dimensions de la galerie
    private static final float GALLERY_WIDTH = 40f;
    private static final float GALLERY_LENGTH = 80f;
    private static final float GALLERY_HEIGHT = 12f;
    private static final float WALL_THICKNESS = 0.5f;

    // Couleurs
    private static final ColorRGBA COLOR_WALL_WARM = new ColorRGBA(0.92f, 0.88f, 0.82f, 1f); // Beige chaud
    private static final ColorRGBA COLOR_WALL_WHITE = new ColorRGBA(0.95f, 0.95f, 0.95f, 1f); // Blanc cassé
    private static final ColorRGBA COLOR_FLOOR = new ColorRGBA(0.85f, 0.82f, 0.78f, 1f); // Marbre beige

    public SceneManager(SimpleApplication app) {
        this.app = app;
        this.assetManager = app.getAssetManager();
        this.rootNode = app.getRootNode();
        this.galleryNode = new Node("GalleryNode");
        rootNode.attachChild(galleryNode);
        this.assetLoader = new AssetLoader();
        app.getStateManager().attach(this.assetLoader);
        this.db = new DBManager();
    }

    /**
     * Initialise la scène complète du musée
     */
    public void initializeScene() {
        createFloor();
        createWalls();
        createArches();
        createSkylight();
        createLighting();
        createPaintings();
        createBenches();
        createPedestals();
        createStaircase();
        loadRobot();
    }

    public void setAssetsToLoad(){
        this.assetLoader
                .addMaterial("Common/MatDefs/Light/Lighting.j3md", "defMat")
                .addTexture("Textures/marble_floor.png", "marble_floor")
                .addTexture("Textures/wall_marble.png", "wall_marble")
                .addTexture("Textures/decore_marble_floor.png", "decore_marble_floor")
                .addModel("Models/robot.glb", "robot")
                .addTexture("Textures/texture.png", "robotTexture");

        String[] paths_left = {"painting1.jpg",
                "painting2.jpg",
                "painting3.jpg",
                "painting4.jpeg",
                "painting5.jpeg"
        };

        int i = 1;
        for(String path : paths_left){
            this.assetLoader.addTexture("Textures/"+ path, path.split("\\.[^.]*$")[0] + "_left_"+i);
            i++;
        }

        String[] paths_right = {"painting6.jpg",
                "painting7.jpeg",
                "painting8.jpg",
                "painting9.jpeg",
                "painting10.jpg"
        };

        i=1;
        for(String path : paths_right){
            this.assetLoader.addTexture("Textures/"+ path, path.split("\\.[^.]*$")[0] +  "_right_"+i);
            i++;
        }

        String[] paths_back = { "painting11.jpg",
                "painting12.jpg",
                "painting13.jpg",
                "painting14.jpg",
                "painting15.jpg"
        };

        i=1;
        for(String path : paths_back){
            this.assetLoader.addTexture("Textures/"+ path,path.split("\\.[^.]*$")[0] + "_back_"+i);
            i++;
        }

        this.assetLoader.onComplete(() -> {
            // This callback runs when loading is complete
            System.out.println("Callback: Loading finished!");
            initializeScene();
            initializeClickDetection();
            ((JmeApp)app).showCrosshair();
            sceneReady = true;

            // Initialize Chat Panel
            System.out.println("🚀 Initializing ChatPanelUI...");
            try {
                this.chatPanelUI = new ChatPanelUI(app, (SceneManager)this);
                System.out.println("✅ ChatPanelUI initialized successfully");
            } catch (Exception e) {
                System.err.println("❌ FATAL ERROR: Failed to initialize ChatPanelUI!");
                e.printStackTrace();
                this.chatPanelUI = null;
            }
        });
    }


    /**
     * Crée le sol en marbre
     */
    private void createFloor() {
        Box floorBox = new Box(GALLERY_WIDTH / 2, 0.1f, GALLERY_LENGTH / 2);
        Geometry floor = new Geometry("Floor", floorBox);
        Material mat = this.assetLoader.getMaterial("defMat").clone();
        mat.setColor("Diffuse", COLOR_FLOOR);
        mat.setColor("Ambient", COLOR_FLOOR);
        mat.setBoolean("UseMaterialColors", true);

        // Texture optionnelle
        Texture floorTex = this.assetLoader.getTexture("marble_floor");
        floorTex.setWrap(Texture.WrapMode.Repeat);
        mat.setTexture("DiffuseMap", floorTex);

        floor.setMaterial(mat);
        floor.setLocalTranslation(0, -0.1f, 0);
        galleryNode.attachChild(floor);
    }

    /**
     * Crée les murs principaux
     */

    private void createWalls() {
        // Mur gauche - Texture brique
        createWall("WallLeft", WALL_THICKNESS, GALLERY_HEIGHT, GALLERY_LENGTH,
                -GALLERY_WIDTH / 2, GALLERY_HEIGHT / 2, 0, "wall_marble");

        // Mur droit - Texture brique
        createWall("WallRight", WALL_THICKNESS, GALLERY_HEIGHT, GALLERY_LENGTH,
                GALLERY_WIDTH / 2, GALLERY_HEIGHT / 2, 0, "wall_marble");

        // Mur du fond - Texture plâtre blanc
        createWall("WallBack", GALLERY_WIDTH, GALLERY_HEIGHT, WALL_THICKNESS,
                0, GALLERY_HEIGHT / 2, -GALLERY_LENGTH / 2, "decore_marble_floor");

        // Mur d'entrée - Texture béton
        createWall("WallFront", GALLERY_WIDTH, GALLERY_HEIGHT, WALL_THICKNESS,
                0, GALLERY_HEIGHT / 2, GALLERY_LENGTH / 2, "decore_marble_floor");
    }

    /**
     * Crée un mur AVEC TEXTURE
     */
    private void createWall(String name, float width, float height, float depth,
                            float x, float y, float z, String textureKey) {
        Box wallBox = new Box(width / 2, height / 2, depth / 2);
        Geometry wall = new Geometry(name, wallBox);
        Material mat = this.assetLoader.getMaterial("defMat").clone();

        try {
            // Charger la texture PNG
            Texture texture = this.assetLoader.getTexture(textureKey);
            texture.setWrap(Texture.WrapMode.Repeat); // permet la répétition
            mat.setTexture("DiffuseMap", texture);

            // Ajuster l’échelle de la texture selon la taille du mur
            float textureScale = calculateTextureScale(width, height);

            // Répéter la texture sur la surface du mur
            wallBox.scaleTextureCoordinates(new Vector2f(textureScale, textureScale));

            mat.setBoolean("UseMaterialColors", true);
            mat.setColor("Diffuse", ColorRGBA.White);
            mat.setColor("Ambient", ColorRGBA.Gray);

            System.out.println("Texture chargée: " + textureKey);

        } catch (Exception e) {
            // Si la texture n'est pas trouvée, utiliser une couleur par défaut
            e.printStackTrace();
            System.out.println("Texture non trouvée: " + textureKey + " - Utilisation couleur par défaut");

            // Couleurs par défaut selon le nom du mur
            ColorRGBA fallbackColor;
            if (name.contains("Left") || name.contains("Right")) {
                fallbackColor = COLOR_WALL_WARM;
            } else {
                fallbackColor = COLOR_WALL_WHITE;
            }

            mat.setColor("Diffuse", fallbackColor);
            mat.setColor("Ambient", fallbackColor);
            mat.setBoolean("UseMaterialColors", true);
        }

        wall.setMaterial(mat);
        wall.setLocalTranslation(x, y, z);
        galleryNode.attachChild(wall);
    }


    /**
     * Calcule l'échelle de texture appropriée selon la taille du mur
     */
    private float calculateTextureScale(float width, float height) {
        // Pour les murs latéraux (longs et étroits)
        if (width < height) {
            return 4.0f; // Plus de répétition pour les murs longs
        }
        // Pour les murs avant/arrière (larges)
        return 2.0f; // Moins de répétition pour les murs larges
    }

    /**
     * Crée les arches caractéristiques du Louvre
     */
    private void createArches() {
        int numArches = 8;
        float spacing = GALLERY_LENGTH / (numArches + 1);

        for (int i = 0; i < numArches; i++) {
            float z = -GALLERY_LENGTH / 2 + spacing * (i + 1);

            // Arche gauche
            createArch("ArchLeft_" + i, -12f, z);

            // Arche droite
            createArch("ArchRight_" + i, 12f, z);
        }
    }

    /**
     * Crée une arche individuelle
     */
    private void createArch(String name, float x, float z) {
        Node archNode = new Node(name);

        // Pilier gauche
        createPillar(name + "_PillarL", -2.5f, 0, archNode);

        // Pilier droit
        createPillar(name + "_PillarR", 2.5f, 0, archNode);

        // Arc supérieur (semi-cylindre)
        createArchTop(name + "_Top", archNode);

        archNode.setLocalTranslation(x, 0, z);
        galleryNode.attachChild(archNode);
    }

    /**
     * Crée un pilier d'arche
     */
    private void createPillar(String name, float x, float z, Node parent) {
        Box pillar = new Box(0.8f, 5f, 0.8f);
        Geometry pillarGeom = new Geometry(name, pillar);
        Material mat = this.assetLoader.getMaterial("defMat").clone();
        mat.setColor("Diffuse", COLOR_WALL_WHITE);
        mat.setColor("Ambient", COLOR_WALL_WHITE);
        mat.setBoolean("UseMaterialColors", true);
        pillarGeom.setMaterial(mat);
        pillarGeom.setLocalTranslation(x, 5f, z);
        parent.attachChild(pillarGeom);
    }

    /**
     * Crée la partie supérieure de l'arche
     */
    private void createArchTop(String name, Node parent) {
        // Arc créé avec plusieurs segments
        int segments = 12;
        float radius = 3f;
        float thickness = 0.8f;

        for (int i = 0; i <= segments; i++) {
            float angle = FastMath.PI * i / segments;
            float x = FastMath.cos(angle) * radius;
            float y = 10f + FastMath.sin(angle) * radius;

            Box segment = new Box(0.3f, 0.3f, thickness);
            Geometry segmentGeom = new Geometry(name + "_Seg" + i, segment);
            Material mat = this.assetLoader.getMaterial("defMat").clone();
            mat.setColor("Diffuse", COLOR_WALL_WHITE);
            mat.setColor("Ambient", COLOR_WALL_WHITE);
            mat.setBoolean("UseMaterialColors", true);
            segmentGeom.setMaterial(mat);
            segmentGeom.setLocalTranslation(x, y, 0);
            parent.attachChild(segmentGeom);
        }
    }

    /**
     * Crée les puits de lumière (skylights)
     */
    private void createSkylight() {
        int numSkylights = 4;
        float spacing = GALLERY_LENGTH / (numSkylights + 1);

        for (int i = 0; i < numSkylights; i++) {
            float z = -GALLERY_LENGTH / 2 + spacing * (i + 1);

            Box skylight = new Box(8f, 0.2f, 8f);
            Geometry skylightGeom = new Geometry("Skylight_" + i, skylight);
            Material mat = this.assetLoader.getMaterial("defMat").clone();
            mat.setColor("Diffuse", new ColorRGBA(0.9f, 0.95f, 1f, 0.3f));
            mat.setColor("Ambient", ColorRGBA.White);
            mat.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);
            mat.setBoolean("UseMaterialColors", true);
            skylightGeom.setMaterial(mat);
            skylightGeom.setLocalTranslation(0, GALLERY_HEIGHT - 0.1f, z);
            galleryNode.attachChild(skylightGeom);
        }
    }

    /**
     * Configure l'éclairage de la galerie
     */
    private void createLighting() {
        // Lumière ambiante douce
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(new ColorRGBA(0.4f, 0.4f, 0.45f, 1f));
        rootNode.addLight(ambient);

        // Lumières directionnelles (skylights)
        DirectionalLight skylight1 = new DirectionalLight();
        skylight1.setDirection(new Vector3f(0.2f, -1f, 0.1f).normalizeLocal());
        skylight1.setColor(new ColorRGBA(0.9f, 0.95f, 1f, 1f));
        rootNode.addLight(skylight1);

        // Spots pour les tableaux
        createPaintingSpotlights();
    }

    /**
     * Crée des spots pour éclairer les tableaux
     */
    private void createPaintingSpotlights() {
        // Mur gauche
        for (int i = 0; i < 10; i++) {
            float z = -35f + i * 8f;
            createSpotlight(-15f, 8f, z, new Vector3f(1, -0.5f, 0));
        }

        // Mur droit
        for (int i = 0; i < 10; i++) {
            float z = -35f + i * 8f;
            createSpotlight(15f, 8f, z, new Vector3f(-1, -0.5f, 0));
        }
    }

    /**
     * Crée un spotlight individuel
     */
    private void createSpotlight(float x, float y, float z, Vector3f direction) {
        SpotLight spot = new SpotLight();
        spot.setPosition(new Vector3f(x, y, z));
        spot.setDirection(direction.normalizeLocal());
        spot.setColor(new ColorRGBA(1f, 0.98f, 0.95f, 1f));
        spot.setSpotRange(15f);
        spot.setSpotInnerAngle(15f * FastMath.DEG_TO_RAD);
        spot.setSpotOuterAngle(35f * FastMath.DEG_TO_RAD);
        rootNode.addLight(spot);
    }

    /**
     * Crée les tableaux sur les murs
     */
    private void createPaintings() {

        String[] keysLeft = new String[5];
        for(int i=0; i < keysLeft.length; i++){
            keysLeft[i] = "painting" + (i+1) + "_left_" + (i+1);
        }

        // Tableaux mur gauche
        createPaintingWall(-GALLERY_WIDTH / 2 + 0.6f, true, keysLeft);


        String[] keysRight = new String[5];
        for(int i=0; i < keysRight.length; i++){
            keysRight[i] = "painting" + (i+6) + "_right_" + (i+1);
        }

        // Tableaux mur droit
        createPaintingWall(GALLERY_WIDTH / 2 - 0.6f, false, keysRight);


        String[] keysBack = new String[5];
        for(int i=0; i < keysBack.length; i++){
            keysBack[i] = "painting" + (i+11) + "_back_" + (i+1);
        }

        // Tableaux mur du fond
        createPaintingBackWall(keysBack);
    }

    /**
     * Crée une série de tableaux sur un mur latéral
     */
    private void createPaintingWall(float x, boolean facingRight, String[] keys) {
        int len = keys.length;

        float spacing = GALLERY_LENGTH / (len + 1);

        for (int i = 0; i < len; i++) {
            float z = -GALLERY_LENGTH / 2 + spacing * (i + 1);
            float height = 5f + (i % 3) * 0.5f;
            float width = 2f + (i % 2) * 0.5f;

            createPainting(keys[i].split("_")[0] + "_" + (facingRight ? "R" : "L") + "_" + i,
                    x, height, z, width, width * 1.3f, facingRight ? 90f : -90f, keys[i]);
        }
    }

    /**
     * Crée des tableaux sur le mur du fond
     */
    private void createPaintingBackWall(String[] keys) {
        float[] positions = {-12f, -6f, 0f, 6f, 12f};

        for (int i = 0; i < positions.length; i++) {
            createPainting(keys[i].split("_")[0] + "_Back_" + i,
                    positions[i], 5.5f, -GALLERY_LENGTH / 2 + 0.6f,
                    2.5f, 3f, 0f, keys[i]);
        }
    }

    /**
     * Crée un tableau individuel
     */
    private void createPainting(String name, float x, float y, float z,
                                float width, float height, float rotationY, String Textkey) {
        // Cadre
        Box frame = new Box(width / 2 + 0.1f, height / 2 + 0.1f, 0.05f);
        Geometry frameGeom = new Geometry(name.split("_")[0] + "_Frame", frame);
        Material frameMat = this.assetLoader.getMaterial("defMat").clone();
        frameMat.setColor("Diffuse", new ColorRGBA(0.2f, 0.15f, 0.1f, 1f));
        frameMat.setColor("Ambient", new ColorRGBA(0.1f, 0.08f, 0.05f, 1f));
        frameMat.setBoolean("UseMaterialColors", true);
        frameGeom.setMaterial(frameMat);

        // Toile
        Quad canvas = new Quad(width, height);
        Geometry canvasGeom = new Geometry(name.split("_")[0] + "_canvas", canvas);
        Material canvasMat = this.assetLoader.getMaterial("defMat").clone();

        // IMPORTANT: Charger votre texture ici
        Texture paintingTex = this.assetLoader.getTexture(Textkey);
        canvasMat.setTexture("DiffuseMap", paintingTex);

        // Couleur temporaire pour démonstration
        canvasMat.setColor("Diffuse", new ColorRGBA(0.8f, 0.75f, 0.7f, 1f));
        canvasMat.setColor("Ambient", new ColorRGBA(0.6f, 0.55f, 0.5f, 1f));
        canvasMat.setBoolean("UseMaterialColors", true);

        canvasGeom.setMaterial(canvasMat);
        canvasGeom.setLocalTranslation(-width / 2, -height / 2, 0.06f);

        // Assemblage
        Node paintingNode = new Node(name.split("_")[0]);
        paintingNode.attachChild(frameGeom);
        paintingNode.attachChild(canvasGeom);
        paintingNode.setLocalTranslation(x, y, z);
        paintingNode.rotate(0, rotationY * FastMath.DEG_TO_RAD, 0);

        galleryNode.attachChild(paintingNode);
    }

    /**
     * Crée des bancs pour les visiteurs
     */
    private void createBenches() {
        float[] positions = {-30f, -10f, 10f, 30f};

        for (float z : positions) {
            createBench("Bench_" + z, 0, 0.5f, z);
        }
    }

    /**
     * Crée un banc individuel
     */
    private void createBench(String name, float x, float y, float z) {
        Node benchNode = new Node(name);

        // Siège
        Box seat = new Box(3f, 0.2f, 1f);
        Geometry seatGeom = new Geometry(name + "_Seat", seat);
        Material mat = this.assetLoader.getMaterial("defMat").clone();
        mat.setColor("Diffuse", new ColorRGBA(0.9f, 0.88f, 0.85f, 1f));
        mat.setColor("Ambient", new ColorRGBA(0.7f, 0.68f, 0.65f, 1f));
        mat.setBoolean("UseMaterialColors", true);
        seatGeom.setMaterial(mat);
        seatGeom.setLocalTranslation(0, 0.5f, 0);
        benchNode.attachChild(seatGeom);

        // Pieds
        float[] legPositions = {-2.5f, -0.8f, 0.8f, 2.5f};
        for (float legX : legPositions) {
            Box leg = new Box(0.15f, 0.3f, 0.15f);
            Geometry legGeom = new Geometry(name + "_Leg", leg);
            legGeom.setMaterial(mat);
            legGeom.setLocalTranslation(legX, 0.2f, 0);
            benchNode.attachChild(legGeom);
        }

        benchNode.setLocalTranslation(x, y, z);
        galleryNode.attachChild(benchNode);
    }

    /**
     * Crée des socles pour sculptures
     */
    private void createPedestals() {
        createPedestal("Pedestal1", -8f, 0, -20f);
        createPedestal("Pedestal2", 8f, 0, -20f);
        createPedestal("Pedestal3", -8f, 0, 20f);
        createPedestal("Pedestal4", 8f, 0, 20f);
    }

    /**
     * Crée un socle individuel
     */
    private void createPedestal(String name, float x, float y, float z) {
        Box pedestal = new Box(1f, 0.8f, 1f);
        Geometry pedestalGeom = new Geometry(name, pedestal);
        Material mat = this.assetLoader.getMaterial("defMat").clone();
        mat.setColor("Diffuse", new ColorRGBA(0.85f, 0.85f, 0.87f, 1f));
        mat.setColor("Ambient", new ColorRGBA(0.7f, 0.7f, 0.72f, 1f));
        mat.setBoolean("UseMaterialColors", true);
        pedestalGeom.setMaterial(mat);
        pedestalGeom.setLocalTranslation(x, 0.8f, z);
        galleryNode.attachChild(pedestalGeom);
    }

    /**
     * Crée un escalier
     */
    private void createStaircase() {
        Node stairNode = new Node("Staircase");

        int numSteps = 12;
        float stepWidth = 6f;
        float stepHeight = 0.2f;
        float stepDepth = 0.8f;

        Material mat = this.assetLoader.getMaterial("defMat").clone();
        mat.setColor("Diffuse", new ColorRGBA(0.88f, 0.85f, 0.82f, 1f));
        mat.setColor("Ambient", new ColorRGBA(0.7f, 0.67f, 0.64f, 1f));
        mat.setBoolean("UseMaterialColors", true);

        for (int i = 0; i < numSteps; i++) {
            Box step = new Box(stepWidth / 2, stepHeight / 2, stepDepth / 2);
            Geometry stepGeom = new Geometry("Step_" + i, step);
            stepGeom.setMaterial(mat);
            stepGeom.setLocalTranslation(0, i * stepHeight, -i * stepDepth);
            stairNode.attachChild(stepGeom);
        }

        stairNode.setLocalTranslation(15f, 0, 25f);
        galleryNode.attachChild(stairNode);
    }

    public void loadRobot() {
            robotManager = new RobotManager(this.assetManager);
            robotManager.setRobot(rootNode, assetLoader);
            robot = robotManager.getRobot();
            robot.scale(1f);



            BoundingBox bbox = (BoundingBox) robot.getWorldBound();
            float minY = 0.1f + bbox.getYExtent();
            robot.setLocalTranslation(0, minY, 0);

            app.getRootNode().attachChild(robot);

            animComposer = robot.getControl(AnimComposer.class);
            if (animComposer != null) {
                System.out.println("✅ Animations disponibles : " + animComposer.getAnimClipsNames());
                if (animComposer.getAnimClipsNames().contains("Idle")) {
                    animComposer.setCurrentAction("Idle");
                } else {
                    String firstAnim = animComposer.getAnimClipsNames().stream().findFirst().orElse(null);
                    if (firstAnim != null) animComposer.setCurrentAction(firstAnim);
                }
            } else {
                System.out.println("⚠️ Aucun AnimComposer trouvé sur le modèle.");
            }

            DirectionalLight robotLight = new DirectionalLight();
            robotLight.setColor(ColorRGBA.White.mult(1.2f));
            robotLight.setDirection(new Vector3f(-0.5f, -1f, -0.3f).normalizeLocal());
            robot.addLight(robotLight);

            AmbientLight softAmbient = new AmbientLight();
            softAmbient.setColor(ColorRGBA.White.mult(0.3f));
            robot.addLight(softAmbient);
    }

    public void playAnimation(String animName) {
        if (animComposer != null && animComposer.getAnimClipsNames().contains(animName)) {
            animComposer.setCurrentAction(animName);
            System.out.println("🎥 Animation jouée : " + animName);
        } else {
            System.out.println("⚠️ Animation '" + animName + "' introuvable.");
        }
    }

    /**
     * Retourne le nœud principal de la galerie
     */
    public Node getGalleryNode() {
        return galleryNode;
    }

    public Spatial getRobot(){
        return this.robot;
    }

    public boolean isSceneReady() {
        return sceneReady;
    }

    public boolean isChatPanelVisible() {
        return chatPanelUI != null && chatPanelUI.isVisible();
    }

    public void closeChatPanel() {
        if (chatPanelUI != null) {
            chatPanelUI.close();
        }
        //  NOUVEAU : Cacher le robot et la bulle quand on ferme le panel
        hideRobot();
    }

    /**
     * ✅ NOUVEAU : Cache le robot et la bulle
     */
    public void hideRobot() {
        // Réinitialiser l'état du robot
        robotStayingNearPainting = false;
        currentActivePainting = null;

        // Cacher la bulle
        if (infoBubbleNode != null) {
            infoBubbleNode.setCullHint(Spatial.CullHint.Always);
        }

        // Arrêter l'animation de parole
        if (animComposer != null) {
            playAnimation("Idle");
        }

        System.out.println("🚫 Robot et bulle cachés");
    }


    // ✅ ============================================
    // ✅ NOUVELLES MÉTHODES POUR LA BULLE ET LE CLIC
    // ✅ ============================================

    /**
     * ✅ Initialise la détection de clic et crée la bulle 3D
     */
    /**
     * ✅ Initialise la détection de clic et crée la bulle 3D
     */
    public void initializeClickDetection() {
        // ✅ CRÉATION DE LA BULLE 3D AVEC DESIGN MODERNE ET PROFESSIONNEL
        infoBubbleNode = new Node("InfoBubble");

        // ============ COUCHE 1 : OMBRE PORTÉE ============
        // Ombre plus grande et plus diffuse
        Quad shadowQuad = new Quad(7.4f, 2.6f);
        Geometry shadow = new Geometry("BubbleShadow", shadowQuad);
        Material shadowMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        shadowMat.setColor("Color", new ColorRGBA(0f, 0f, 0f, 0.5f)); // Ombre plus prononcée
        shadowMat.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);
        shadow.setMaterial(shadowMat);
        shadow.setLocalTranslation(-3.7f, -0.15f, 0f);
        infoBubbleNode.attachChild(shadow);

        // ============ COUCHE 2 : BORDURE EXTÉRIEURE ============
        // Bordure bleu clair pour effet de profondeur
        Quad outerBorderQuad = new Quad(7.2f, 2.4f);
        Geometry outerBorder = new Geometry("BubbleOuterBorder", outerBorderQuad);
        Material outerBorderMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        outerBorderMat.setColor("Color", new ColorRGBA(0.25f, 0.45f, 0.75f, 0.9f)); // Bleu moyen
        outerBorderMat.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);
        outerBorder.setMaterial(outerBorderMat);
        outerBorder.setLocalTranslation(-3.6f, -0.1f, 0.005f);
        infoBubbleNode.attachChild(outerBorder);

        // ============ COUCHE 3 : BORDURE INTÉRIEURE ============
        // Bordure bleu plus foncé
        Quad innerBorderQuad = new Quad(7f, 2.2f);
        Geometry innerBorder = new Geometry("BubbleInnerBorder", innerBorderQuad);
        Material innerBorderMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        innerBorderMat.setColor("Color", new ColorRGBA(0.15f, 0.25f, 0.45f, 0.95f)); // Bleu foncé
        innerBorderMat.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);
        innerBorder.setMaterial(innerBorderMat);
        innerBorder.setLocalTranslation(-3.5f, -0.05f, 0.01f);
        infoBubbleNode.attachChild(innerBorder);

        // ============ COUCHE 4 : FOND PRINCIPAL ============
        // Fond bleu foncé élégant (harmonisé avec le panel)
        Quad bubbleQuad = new Quad(6.8f, 2f);
        bubbleBackground = new Geometry("BubbleBackground", bubbleQuad);
        Material bubbleMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        bubbleMat.setColor("Color", new ColorRGBA(0.08f, 0.12f, 0.20f, 0.98f)); // Même couleur que le panel
        bubbleMat.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);
        bubbleBackground.setMaterial(bubbleMat);
        bubbleBackground.setLocalTranslation(-3.4f, 0f, 0.015f);
        infoBubbleNode.attachChild(bubbleBackground);

        // ============ COUCHE 5 : HIGHLIGHT (REFLET) ============
        // Petit reflet en haut pour effet glossy
        Quad highlightQuad = new Quad(6.6f, 0.3f);
        Geometry highlight = new Geometry("BubbleHighlight", highlightQuad);
        Material highlightMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        highlightMat.setColor("Color", new ColorRGBA(0.4f, 0.6f, 0.9f, 0.3f)); // Bleu clair transparent
        highlightMat.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);
        highlight.setMaterial(highlightMat);
        highlight.setLocalTranslation(-3.3f, 1.6f, 0.02f);
        infoBubbleNode.attachChild(highlight);

        // ============ COUCHE 6 : TEXTE ============
        // Texte avec meilleure lisibilité
        BitmapFont font = assetManager.loadFont("Interface/Fonts/Default.fnt");
        bubbleText = new BitmapText(font);
        bubbleText.setSize(0.22f); // Plus grand pour meilleure lisibilité
        bubbleText.setColor(ColorRGBA.White); // Blanc pur
        bubbleText.setText(null);
        bubbleText.setLocalTranslation(-3.2f, 1.1f, 0.025f); // Centré verticalement
        infoBubbleNode.attachChild(bubbleText);

        // ✅ Rendre la bulle invisible au départ
        infoBubbleNode.setCullHint(Spatial.CullHint.Always);

        rootNode.attachChild(infoBubbleNode);

        System.out.println("✅ Bulle 3D moderne créée avec 6 couches (ombre, bordures, fond, highlight, texte)");
    }


    /**
     * ✅ Détecte si on clique sur un tableau (utilise le CENTRE de l'écran)
     */
    public void detectPaintingClick() {
        try {
            CollisionResults results = new CollisionResults();

            // ✅ Utiliser le CENTRE de l'écran au lieu du curseur
            Vector2f screenCenter = new Vector2f(
                    app.getCamera().getWidth() / 2f,
                    app.getCamera().getHeight() / 2f
            );


            Vector3f origin = app.getCamera().getWorldCoordinates(screenCenter, 0f);
            Vector3f dir = app.getCamera().getWorldCoordinates(screenCenter, 1f)
                    .subtractLocal(origin).normalizeLocal();

            Ray ray = new Ray(origin, dir);
            rootNode.collideWith(ray, results);

            if (results.size() > 0) {
                CollisionResult closest = results.getClosestCollision();
                Geometry geom = closest.getGeometry();

                String name = geom.getName();
                if (name.contains("canvas") || name.contains("painting")) {
                    System.out.println("🖱️ Tableau cliqué : " + name);

                    // ✅ Réinitialiser l'ancien tableau si changement
                    if (currentActivePainting != geom) {
                        System.out.println("🔄 Changement de tableau - Réinitialisation");
                        robotStayingNearPainting = false;
                        currentActivePainting = null;
                    }

                    // ✅ Obtenir les infos du tableau
                    String paintingInfo = getPaintingInfo(name);

                    // ✅ VÉRIFIER SI paintingInfo EST NULL (database error)
                    if (paintingInfo == null || paintingInfo.trim().isEmpty()) {
                        System.err.println("⚠️ ERROR: Could not get painting info from database!");
                        System.err.println("⚠️ Make sure Docker container is running:");
                        System.err.println("   docker run --name my-postgres-container -e POSTGRES_USER=user -e POSTGRES_PASSWORD=password -e POSTGRES_DB=tour_3d_db -p 5432:5432 -d postgres");
                        paintingInfo = "Database connection error. Please check if Docker container is running.";
                    }

                    // ✅ Ouvrir le panneau de chat
                    System.out.println("🔍 Checking chatPanelUI: " + (chatPanelUI == null ? "NULL ❌" : "OK ✅"));
                    if (chatPanelUI != null) {
                        System.out.println("📞 Calling chatPanelUI.show()...");
                        chatPanelUI.show(paintingInfo);
                    } else {
                        System.out.println("⚠️ ERROR: chatPanelUI is NULL! Cannot show panel.");
                    }

                    // ✅ Afficher la bulle d'information
                    showInfoBubble(paintingInfo);

                    // ✅ Faire marcher le robot vers le tableau
                    if (robot != null) {
                        startRobotWalkTowardsPainting(geom);
                        playAnimation("Talk"); // Animation de parole
                    }

                } else {
                    System.out.println("⚠️ Objet cliqué (pas un tableau) : " + name);
                }
            } else {
                System.out.println("⚠️ Aucun objet cliqué");
            }
        } catch (Exception e) {
            System.err.println("❌ CRITICAL ERROR in detectPaintingClick():");
            e.printStackTrace();
            // Don't crash the app, just log the error
        }
    }

    /**
     * ✅ Vérifie si on regarde un tableau (pour changer la couleur du réticule)
     */
    public boolean isLookingAtPainting() {
        CollisionResults results = new CollisionResults();
        Vector2f screenCenter = new Vector2f(
                app.getCamera().getWidth() / 2f,
                app.getCamera().getHeight() / 2f
        );

        Vector3f origin = app.getCamera().getWorldCoordinates(screenCenter, 0f);
        Vector3f dir = app.getCamera().getWorldCoordinates(screenCenter, 1f)
                .subtractLocal(origin).normalizeLocal();

        Ray ray = new Ray(origin, dir);
        rootNode.collideWith(ray, results);

        if (results.size() > 0) {
            String name = results.getClosestCollision().getGeometry().getName();
            return name.contains("canvas") || name.contains("painting");
        }
        return false;
    }

    /**
     * ✅ Retourne les informations d'un tableau selon son nom
     */
    private String getPaintingInfo(String paintingName) {
        if (paintingName.contains("canvas") || paintingName.contains("painting")) {
            paintingName = paintingName.split("_")[0];
        }

        ResultSet res = null;

        try {
            System.out.println("🔍 Querying database for painting: " + paintingName);
            res = this.db.getPaintingById(paintingName);

            if (res != null && res.next()) {
                System.out.print("✅ Found painting - id : " + res.getString("id") + ", ");
                System.out.print("title : " + res.getString("title") + ", ");
                System.out.print("artist : " + res.getString("artist") + ", ");
                System.out.println("year : " + res.getInt("year"));

                String description = res.getString("description");

                res.close();

                return description;
            } else {
                System.err.println("⚠️ No painting found in database with id: " + paintingName);
                return "Painting information not available.";
            }
        } catch (SQLException e) {
            System.err.println("❌ DATABASE ERROR: Could not retrieve painting info!");
            System.err.println("❌ SQLException: " + e.getMessage());
            System.err.println("⚠️ Make sure Docker PostgreSQL container is running:");
            System.err.println("   docker ps  (to check if container is running)");
            System.err.println("   docker start my-postgres-container  (to start it)");
            e.printStackTrace();
            return null; // Signal database error
        } catch (Exception e) {
            System.err.println("❌ UNEXPECTED ERROR getting painting info:");
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (res != null) res.close();
            } catch (SQLException e) {
                System.err.println("⚠️ Error closing ResultSet:");
                e.printStackTrace();
            }
        }
    }

    /**
     * ✅ Affiche la réponse de l'IA dans la bulle du robot
     */
    public void showRobotSpeech(String text) {
        if (bubbleText != null) {
            // Couper le texte s'il est trop long pour la bulle
            String display = text.length() > 100 ? text.substring(0, 97) + "..." : text;
            showInfoBubble(display);
        }
    }

    /**
     * ✅ Affiche la bulle d'information au-dessus du robot
     */
    private void showInfoBubble(String text) {
        if (bubbleText != null && infoBubbleNode != null) {
            bubbleText.setText(text);
            infoBubbleNode.setCullHint(Spatial.CullHint.Never);
            bubbleDisplayTime = BUBBLE_DURATION; // Réinitialiser à 2 secondes
            System.out.println("💬 Bulle affichée pour 2 secondes : " + text);
        }
    }

    /**
     * ✅ Démarre le mouvement du robot vers le tableau
     */
    private void startRobotWalkTowardsPainting(Geometry painting) {
        if (robot == null) return;

        // ✅ Sauvegarder le tableau actif
        currentActivePainting = painting;
        robotStayingNearPainting = true;

        robotStartPosition.set(robot.getLocalTranslation());

        // Calculer la position cible (1.5 mètres devant le tableau)
        Vector3f paintingPos = painting.getWorldTranslation();
        Vector3f paintingNormal = painting.getWorldRotation().mult(Vector3f.UNIT_Z);

        robotTargetPosition.set(paintingPos.add(paintingNormal.mult(1.5f)));
        robotTargetPosition.y = robotStartPosition.y; // Garder la même hauteur

        // Orienter le robot vers le tableau (VERTICAL uniquement)
        Vector3f direction = paintingPos.subtract(robotStartPosition);
        direction.y = 0; // Ignorer la composante verticale pour rester droit
        direction.normalizeLocal();

        float angle = FastMath.atan2(direction.x, direction.z);
        robot.setLocalRotation(new Quaternion().fromAngleAxis(angle, Vector3f.UNIT_Y));

        // Démarrer l'animation de marche
        robotWalking = true;
        robotWalkTime = 0f;
        robotWalkProgress = 0f;

        playAnimation("Walk");

        System.out.println("🚶 Robot commence à marcher vers le tableau - RESTERA près du tableau");
    }

    /**
     * ✅ Méthode update à appeler depuis JmeApp.simpleUpdate()
     */
    public void update(float tpf, com.jme3.renderer.Camera cam) {
        //  Gérer le mouvement du robot vers le tableau
        updateRobotWalk(tpf);

        //  Mettre à jour la position de la bulle
        updateInfoBubblePosition(cam);
        updateBubbleTimer(tpf);
    }

    private void updateBubbleTimer(float tpf) {
        if (bubbleDisplayTime > 0) {
            bubbleDisplayTime -= tpf;

            if (bubbleDisplayTime <= 0) {
                // Cacher la bulle après 2 secondes
                if (infoBubbleNode != null) {
                    infoBubbleNode.setCullHint(Spatial.CullHint.Always);
                }
                System.out.println("⏱️ Bulle cachée après 2 secondes");
            }
        }
    }

    /**
     * ✅ Met à jour le mouvement du robot pendant qu'il marche
     */
    private void updateRobotWalk(float tpf) {
        if (robotWalking && robot != null) {
            robotWalkTime += tpf;
            robotWalkProgress = Math.min(robotWalkTime / ROBOT_WALK_DURATION, 1f);

            // Interpolation linéaire entre position de départ et cible
            Vector3f currentPos = robotStartPosition.interpolateLocal(robotTargetPosition, robotWalkProgress);
            robot.setLocalTranslation(currentPos);

            // Fin du mouvement
            if (robotWalkProgress >= 1f) {
                robotWalking = false;
                playAnimation("Talk"); // Revenir à l'animation de parole
                System.out.println("✅ Robot arrivé - RESTE près du tableau");
            }
        }

        // ✅ NOUVEAU : Garder le robot près du tableau actif
        if (robotStayingNearPainting && currentActivePainting != null && robot != null && !robotWalking) {
            // Positionner le robot devant le tableau
            Vector3f paintingPos = currentActivePainting.getWorldTranslation();
            Vector3f paintingNormal = currentActivePainting.getWorldRotation().mult(Vector3f.UNIT_Z);

            Vector3f targetPos = paintingPos.add(paintingNormal.mult(1.5f));
            targetPos.y = robot.getLocalTranslation().y;

            robot.setLocalTranslation(targetPos);

            // ✅ CORRECTION : Garder le robot VERTICAL (pas incliné)
            // Calculer seulement la rotation Y (yaw) pour regarder le tableau
            Vector3f direction = paintingPos.subtract(targetPos);
            direction.y = 0; // Ignorer la composante verticale
            direction.normalizeLocal();

            // Calculer l'angle de rotation autour de l'axe Y
            float angle = FastMath.atan2(direction.x, direction.z);
            robot.setLocalRotation(new Quaternion().fromAngleAxis(angle, Vector3f.UNIT_Y));
        }
    }

    /**
     * ✅ Met à jour la position de la bulle pour qu'elle suive le robot
     */
    private void updateInfoBubblePosition(com.jme3.renderer.Camera cam) {
        // ✅ Afficher la bulle seulement si le timer est actif
        if (bubbleDisplayTime > 0 && robot != null) {
            // ✅ Positionner la bulle AU-DESSUS du robot
            Vector3f robotPos = robot.getWorldTranslation();
            BoundingBox bbox = (BoundingBox) robot.getWorldBound();
            float robotHeight = bbox.getYExtent() * 2;

            Vector3f bubblePos = new Vector3f(
                    robotPos.x,
                    robotPos.y + robotHeight + 1.2f,
                    robotPos.z
            );

            infoBubbleNode.setLocalTranslation(bubblePos);

            // ✅ Orienter la bulle vers la caméra (billboard effect)
            infoBubbleNode.lookAt(cam.getLocation(), Vector3f.UNIT_Y);
            infoBubbleNode.setCullHint(Spatial.CullHint.Never); // Visible
        } else {
            // Cacher la bulle si le timer est écoulé
            if (infoBubbleNode != null) {
                infoBubbleNode.setCullHint(Spatial.CullHint.Always);
            }
        }
    }

}