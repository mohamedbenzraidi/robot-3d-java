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
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Quad;
import com.jme3.scene.shape.Torus;
import com.jme3.texture.Texture;
import org.example.DB.DBManager;
import org.example.robot.RobotManager;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MetSceneManager {
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

    // Met Museum dimensions - more spacious rectangular layout
    private static final float GALLERY_WIDTH = 50f;
    private static final float GALLERY_LENGTH = 70f;
    private static final float GALLERY_HEIGHT = 15f;
    private static final float WALL_THICKNESS = 0.5f;

    // Met Museum color palette - cream, beige, and warm tones
    private static final ColorRGBA COLOR_WALL_CREAM = new ColorRGBA(0.96f, 0.94f, 0.89f, 1f);
    private static final ColorRGBA COLOR_COLUMN_MARBLE = new ColorRGBA(0.93f, 0.91f, 0.85f, 1f);
    private static final ColorRGBA COLOR_FLOOR_POLISHED = new ColorRGBA(0.88f, 0.85f, 0.80f, 1f);
    private static final ColorRGBA COLOR_TRIM_GOLD = new ColorRGBA(0.85f, 0.75f, 0.45f, 1f);

    public MetSceneManager(SimpleApplication app) {
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
     * Initialize the complete Met Museum scene
     */
    public void initializeScene() {
        createFloor();
        createWalls();
        createClassicalColumns();
        createCofferedCeiling();
        createGrandSkylight();
        createLighting();
        createPaintings();
        createMetBenches();
        createDisplayCases();
        loadRobot();
        createCenterpiece();
    }

    public void setAssetsToLoad(){
        this.assetLoader
                .addMaterial("Common/MatDefs/Light/Lighting.j3md", "defMat")
                .addTexture("Textures/marble_floor.png", "marble_floor")
                .addTexture("Textures/black-wall.jpg", "black_wall")
                .addTexture("Textures/black-gold-marble.jpg", "black_gold_marble")
                .addTexture("Textures/gold_texture.jpg", "gold_texture")
                .addModel("Models/robot.glb", "robot")
                .addModel("Models/marble_classical_statue_man_01__3d_printable.glb", "statue")
                .addTexture("Textures/texture.png", "robotTexture");

        String[] paths_left = {"painting16.jpg",
                "painting17.jpg",
                "painting8.jpg",
                "painting19.jpg",
                "painting20.jpg"
        };

        int i = 1;
        for(String path : paths_left){
            this.assetLoader.addTexture("Textures/"+ path, path.split("\\.[^.]*$")[0] + "_left_"+i);
            i++;
        }

        String[] paths_right = {"painting21.jpg",
                "painting22.png",
                "painting23.jpg",
                "painting24.jpg",
                "painting25.jpg"
        };

        i=1;
        for(String path : paths_right){
            this.assetLoader.addTexture("Textures/"+ path, path.split("\\.[^.]*$")[0] +  "_right_"+i);
            i++;
        }

        String[] paths_back = { "painting26.jpg",
                "painting27.jpg",
                "painting28.jpg",
                "painting29.jpg",
                "painting30.jpg"
        };

        i=1;
        for(String path : paths_back){
            this.assetLoader.addTexture("Textures/"+ path,path.split("\\.[^.]*$")[0] + "_back_"+i);
            i++;
        }

        this.assetLoader.onComplete(() -> {
            System.out.println("Callback: Loading finished!");
            initializeScene();
            initializeClickDetection();
            ((JmeMetApp)app).showCrosshair();
        });
        this.assetLoader.onComplete(() -> {
            // This callback runs when loading is complete
            System.out.println("Callback: Loading finished!");
            initializeScene();
            initializeClickDetection();
            ((JmeMetApp)app).showCrosshair();
            sceneReady = true;

            // Initialize Chat Panel
            System.out.println("🚀 Initializing ChatPanelUI...");
            try {
                this.chatPanelUI = new ChatPanelUI(app, (MetSceneManager)this);
                System.out.println("✅ ChatPanelUI initialized successfully");
            } catch (Exception e) {
                System.err.println("❌ FATAL ERROR: Failed to initialize ChatPanelUI!");
                e.printStackTrace();
                this.chatPanelUI = null;
            }
        });
    }

    /**
     * Create polished marble floor with pattern
     */
    private void createFloor() {
        Box floorBox = new Box(GALLERY_WIDTH / 2, 0.1f, GALLERY_LENGTH / 2);
        Geometry floor = new Geometry("Floor", floorBox);
        Material mat = this.assetLoader.getMaterial("defMat").clone();
        mat.setColor("Diffuse", COLOR_FLOOR_POLISHED);
        mat.setColor("Ambient", COLOR_FLOOR_POLISHED);
        mat.setBoolean("UseMaterialColors", true);

        Texture floorTex = this.assetLoader.getTexture("marble_floor");
        floorTex.setWrap(Texture.WrapMode.Repeat);
        mat.setTexture("DiffuseMap", floorTex);

        floor.setMaterial(mat);
        floor.setLocalTranslation(0, -0.1f, 0);
        galleryNode.attachChild(floor);

        // Add decorative floor border
        createFloorBorder();
    }

    /**
     * Create decorative border around the floor
     */
    private void createFloorBorder() {
        float borderWidth = 2f;
        Material borderMat = this.assetLoader.getMaterial("defMat").clone();
        borderMat.setColor("Diffuse", new ColorRGBA(0.7f, 0.6f, 0.4f, 1f));
        borderMat.setBoolean("UseMaterialColors", true);

        // North border
        Box northBorder = new Box(GALLERY_WIDTH / 2, 0.12f, borderWidth / 2);
        Geometry northGeom = new Geometry("BorderNorth", northBorder);
        northGeom.setMaterial(borderMat);
        northGeom.setLocalTranslation(0, -0.08f, -GALLERY_LENGTH / 2 + borderWidth / 2);
        galleryNode.attachChild(northGeom);

        // South border
        Geometry southGeom = new Geometry("BorderSouth", northBorder);
        southGeom.setMaterial(borderMat);
        southGeom.setLocalTranslation(0, -0.08f, GALLERY_LENGTH / 2 - borderWidth / 2);
        galleryNode.attachChild(southGeom);
    }

    /**
     * Create main walls with Met's cream color scheme
     */
    private void createWalls() {
        createWall("WallLeft", WALL_THICKNESS, GALLERY_HEIGHT, GALLERY_LENGTH,
                -GALLERY_WIDTH / 2, GALLERY_HEIGHT / 2, 0, "black_wall");

        createWall("WallRight", WALL_THICKNESS, GALLERY_HEIGHT, GALLERY_LENGTH,
                GALLERY_WIDTH / 2, GALLERY_HEIGHT / 2, 0, "black_wall");

        createWall("WallBack", GALLERY_WIDTH, GALLERY_HEIGHT, WALL_THICKNESS,
                0, GALLERY_HEIGHT / 2, -GALLERY_LENGTH / 2, "black_wall");

        createWall("WallFront", GALLERY_WIDTH, GALLERY_HEIGHT, WALL_THICKNESS,
                0, GALLERY_HEIGHT / 2, GALLERY_LENGTH / 2, "black_wall");

        // Add crown molding
        createCrownMolding();
    }

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
        ColorRGBA fallbackColor = COLOR_WALL_CREAM;

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
     * Add decorative crown molding at the top of walls
     */
    private void createCrownMolding() {
        Material moldingMat = this.assetLoader.getMaterial("defMat").clone();
        moldingMat.setColor("Diffuse", COLOR_TRIM_GOLD);
        moldingMat.setBoolean("UseMaterialColors", true);

        float moldingHeight = 0.4f;
        float moldingDepth = 0.3f;

        // Left wall molding
        Box leftMolding = new Box(moldingDepth / 2, moldingHeight / 2, GALLERY_LENGTH / 2);
        Geometry leftMoldingGeom = new Geometry("MoldingLeft", leftMolding);
        leftMoldingGeom.setMaterial(moldingMat);
        leftMoldingGeom.setLocalTranslation(-GALLERY_WIDTH / 2 + moldingDepth / 2,
                GALLERY_HEIGHT - moldingHeight / 2, 0);
        galleryNode.attachChild(leftMoldingGeom);

        // Right wall molding
        Geometry rightMoldingGeom = new Geometry("MoldingRight", leftMolding);
        rightMoldingGeom.setMaterial(moldingMat);
        rightMoldingGeom.setLocalTranslation(GALLERY_WIDTH / 2 - moldingDepth / 2,
                GALLERY_HEIGHT - moldingHeight / 2, 0);
        galleryNode.attachChild(rightMoldingGeom);
    }

    /**
     * Create classical Corinthian-style columns (Met Museum signature)
     */
    private void createClassicalColumns() {
        int numColumns = 6;
        float spacing = GALLERY_LENGTH / (numColumns + 1);

        for (int i = 0; i < numColumns; i++) {
            float z = -GALLERY_LENGTH / 2 + spacing * (i + 1);

            // Columns on both sides
            createCorinthianColumn("ColumnLeft_" + i, -16f, z);
            createCorinthianColumn("ColumnRight_" + i, 16f, z);
        }
    }

    /**
     * Create a single Corinthian column with base, shaft, and capital
     */
    private void createCorinthianColumn(String name, float x, float z) {
        Node columnNode = new Node(name);

        Material columnMat = this.assetLoader.getMaterial("defMat").clone();
        columnMat.setColor("Diffuse", COLOR_COLUMN_MARBLE);
        columnMat.setColor("Ambient", COLOR_COLUMN_MARBLE.mult(0.8f));

        Texture columnTexture = this.assetLoader.getTexture("black_gold_marble");
        columnMat.setTexture("DiffuseMap", columnTexture);
        columnMat.setBoolean("UseMaterialColors", false); // Ignore default colors

//        Material shaftMat = this.assetLoader.getMaterial("defMat").clone();
//        Texture columnTexture = this.assetLoader.getTexture("black_gold_marble");
//        shaftMat.setTexture("DiffuseMap", columnTexture);
//        shaftMat.setBoolean("UseMaterialColors", false); // Ignore default colors

        // Base (square)
        Box base = new Box(1.2f, 0.4f, 1.2f);
        Geometry baseGeom = new Geometry(name + "_Base", base);
        baseGeom.setMaterial(columnMat);
        baseGeom.setLocalTranslation(0, 0.4f, 0);
        columnNode.attachChild(baseGeom);


        // Shaft (cylindrical, slightly tapered)
        float shaftHeight = 10f;
        Cylinder shaft = new Cylinder(16, 32, 0.8f, shaftHeight, true);
        Geometry shaftGeom = new Geometry(name + "_Shaft", shaft);
        shaftGeom.setMaterial(columnMat);
//        shaftGeom.setMaterial(shaftMat);
        shaftGeom.setLocalTranslation(0, 0.8f + shaftHeight / 2, 0);
        shaftGeom.rotate(FastMath.HALF_PI, 0, 0);
        columnNode.attachChild(shaftGeom);

        // Capital (ornate top)
        createColumnCapital(name + "_Capital", columnNode, 0, shaftHeight + 0.8f, 0);

        columnNode.setLocalTranslation(x, 0, z);
        galleryNode.attachChild(columnNode);
    }

    /**
     * Create ornate column capital
     */
    private void createColumnCapital(String name, Node parent, float x, float y, float z) {
        Material capitalMat = this.assetLoader.getMaterial("defMat").clone();
        capitalMat.setColor("Diffuse", COLOR_TRIM_GOLD);
        capitalMat.setBoolean("UseMaterialColors", true);

        // Main capital block
        Box capitalBox = new Box(1.3f, 0.6f, 1.3f);
        Geometry capitalGeom = new Geometry(name, capitalBox);
        capitalGeom.setMaterial(capitalMat);
        capitalGeom.setLocalTranslation(x, y, z);
        parent.attachChild(capitalGeom);

        // Decorative ring
        Cylinder ring = new Cylinder(16, 16, 1.0f, 0.2f, true);
        Geometry ringGeom = new Geometry(name + "_Ring", ring);
        ringGeom.setMaterial(capitalMat);
        ringGeom.setLocalTranslation(x, y - 0.4f, z);
        ringGeom.rotate(FastMath.HALF_PI, 0, 0);
        parent.attachChild(ringGeom);
    }

    /**
     * Create coffered ceiling (recessed panels)
     */
    private void createCofferedCeiling() {
        Material ceilingMat = this.assetLoader.getMaterial("defMat").clone();
        ColorRGBA ceilingColor = new ColorRGBA(0.92f, 0.90f, 0.85f, 1f);
        ceilingMat.setColor("Diffuse", ceilingColor);
        ceilingMat.setColor("Ambient", ceilingColor);
        ceilingMat.setColor("GlowColor", ceilingColor.mult(0.3f)); // Make it emit some light
        ceilingMat.setBoolean("UseMaterialColors", true);

        // Main ceiling plane
        Box ceiling = new Box(GALLERY_WIDTH / 2, 0.2f, GALLERY_LENGTH / 2);
        Geometry ceilingGeom = new Geometry("Ceiling", ceiling);
        ceilingGeom.setMaterial(ceilingMat);
        ceilingGeom.setLocalTranslation(0, GALLERY_HEIGHT, 0);
        galleryNode.attachChild(ceilingGeom);

        // Create recessed coffers (decorative panels)
        createCoffers();
    }

    /**
     * Create individual ceiling coffers
     */
    private void createCoffers() {
        Material cofferMat = this.assetLoader.getMaterial("defMat").clone();
        cofferMat.setColor("Diffuse", new ColorRGBA(0.85f, 0.82f, 0.75f, 1f));
        cofferMat.setBoolean("UseMaterialColors", true);

        int numX = 4;
        int numZ = 6;
        float cofferSize = 6f;
        float spacing = 2f;

        for (int i = 0; i < numX; i++) {
            for (int j = 0; j < numZ; j++) {
                float x = -12f + i * (cofferSize + spacing);
                float z = -25f + j * (cofferSize + spacing);

                Box coffer = new Box(cofferSize / 2, 0.3f, cofferSize / 2);
                Geometry cofferGeom = new Geometry("Coffer_" + i + "_" + j, coffer);
                cofferGeom.setMaterial(cofferMat);
                cofferGeom.setLocalTranslation(x, GALLERY_HEIGHT - 0.4f, z);
                galleryNode.attachChild(cofferGeom);
            }
        }
    }

    /**
     * Create grand central skylight
     */
    private void createGrandSkylight() {
        Box skylight = new Box(15f, 0.3f, 25f);
        Geometry skylightGeom = new Geometry("GrandSkylight", skylight);
        Material mat = this.assetLoader.getMaterial("defMat").clone();
        mat.setColor("Diffuse", new ColorRGBA(0.85f, 0.90f, 0.95f, 0.4f));
        mat.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);
        mat.setBoolean("UseMaterialColors", true);
        skylightGeom.setMaterial(mat);
        skylightGeom.setLocalTranslation(0, GALLERY_HEIGHT - 0.2f, 0);
        galleryNode.attachChild(skylightGeom);
    }

    /**
     * Configure Met Museum lighting - bright and even
     */
    private void createLighting() {
        // Bright ambient light for museum visibility
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(new ColorRGBA(0.6f, 0.6f, 0.62f, 1f));
        rootNode.addLight(ambient);

        // Natural skylight from above
        DirectionalLight skylight = new DirectionalLight();
        skylight.setDirection(new Vector3f(0.1f, -1f, 0.05f).normalizeLocal());
        skylight.setColor(new ColorRGBA(1f, 0.98f, 0.95f, 1f));
        rootNode.addLight(skylight);

        // Spotlights for artwork
        createArtworkSpotlights();
    }

    /**
     * Create focused spotlights for paintings
     */
    private void createArtworkSpotlights() {
        // Left wall spotlights
        for (int i = 0; i < 5; i++) {
            float z = -28f + i * 14f;
            createSpotlight(-18f, 10f, z, new Vector3f(1, -0.8f, 0));
        }

        // Right wall spotlights
        for (int i = 0; i < 5; i++) {
            float z = -28f + i * 14f;
            createSpotlight(18f, 10f, z, new Vector3f(-1, -0.8f, 0));
        }
    }

    private void createSpotlight(float x, float y, float z, Vector3f direction) {
        SpotLight spot = new SpotLight();
        spot.setPosition(new Vector3f(x, y, z));
        spot.setDirection(direction.normalizeLocal());
        spot.setColor(new ColorRGBA(1f, 0.99f, 0.97f, 1f));
        spot.setSpotRange(12f);
        spot.setSpotInnerAngle(20f * FastMath.DEG_TO_RAD);
        spot.setSpotOuterAngle(40f * FastMath.DEG_TO_RAD);
        rootNode.addLight(spot);
    }

    /**
     * Create paintings with Met Museum style frames
     */
    private void createPaintings() {
        String[] keysLeft = new String[5];
        for(int i=0; i < keysLeft.length; i++){
            keysLeft[i] = "painting" + (i+16) + "_left_" + (i+1);
        }

        createPaintingWall(-GALLERY_WIDTH / 2 + 0.6f, true, keysLeft);

        String[] keysRight = new String[5];
        for(int i=0; i < keysRight.length; i++){
            keysRight[i] = "painting" + (i+21) + "_right_" + (i+1);
        }

        createPaintingWall(GALLERY_WIDTH / 2 - 0.6f, false, keysRight);

        String[] keysBack = new String[5];
        for(int i=0; i < keysBack.length; i++){
            keysBack[i] = "painting" + (i+26) + "_back_" + (i+1);
        }

        createPaintingBackWall(keysBack);
    }

    private void createPaintingWall(float x, boolean facingRight, String[] keys) {
        int len = keys.length;
        float spacing = GALLERY_LENGTH / (len + 1);

        for (int i = 0; i < len; i++) {
            float z = -GALLERY_LENGTH / 2 + spacing * (i + 1);
            float height = 6f;
            float width = 2.5f + (i % 2) * 0.5f;

            createMetPainting(keys[i].split("_")[0] + "_"  + (facingRight ? "R" : "L") + "_" + i,
                    x, height, z, width, width * 1.4f, facingRight ? 90f : -90f, keys[i]);
        }
    }

    private void createPaintingBackWall(String[] keys) {
        float[] positions = {-15f, -7.5f, 0f, 7.5f, 15f};

        for (int i = 0; i < positions.length; i++) {
            createMetPainting(keys[i].split("_")[0] + "_Back_" + i,
                    positions[i], 6.5f, -GALLERY_LENGTH / 2 + 0.6f,
                    3f, 4f, 0f, keys[i]);
        }
    }

    /**
     * Create painting with ornate gold frame (Met style)
     */
    private void createMetPainting(String name, float x, float y, float z,
                                   float width, float height, float rotationY, String textureKey) {
        // Thick ornate gold frame
        Box frame = new Box(width / 2 + 0.15f, height / 2 + 0.15f, 0.08f);
        Geometry frameGeom = new Geometry(name.split("_")[0] + "_Frame", frame);
        Material frameMat = this.assetLoader.getMaterial("defMat").clone();
        frameMat.setColor("Diffuse", COLOR_TRIM_GOLD);
        frameMat.setColor("Ambient", COLOR_TRIM_GOLD.mult(0.7f));
        frameMat.setBoolean("UseMaterialColors", true);
        frameGeom.setMaterial(frameMat);

        // Canvas
        Quad canvas = new Quad(width, height);
        Geometry canvasGeom = new Geometry(name.split("_")[0] + "_canvas", canvas);
        Material canvasMat = this.assetLoader.getMaterial("defMat").clone();

        Texture paintingTex = this.assetLoader.getTexture(textureKey);
        canvasMat.setTexture("DiffuseMap", paintingTex);

        canvasGeom.setMaterial(canvasMat);
        canvasGeom.setLocalTranslation(-width / 2, -height / 2, 0.09f);

        // Placard beneath painting
        createPaintingPlacard(name + "_Placard", 0, -height / 2 - 0.4f, 0.09f, width);

        Node paintingNode = new Node(name);
        paintingNode.attachChild(frameGeom);
        paintingNode.attachChild(canvasGeom);
        paintingNode.setLocalTranslation(x, y, z);
        paintingNode.rotate(0, rotationY * FastMath.DEG_TO_RAD, 0);

        galleryNode.attachChild(paintingNode);
    }

    /**
     * Create information placard below painting
     */
    private void createPaintingPlacard(String name, float x, float y, float z, float width) {
        Box placard = new Box(width / 3, 0.15f, 0.02f);
        Geometry placardGeom = new Geometry(name, placard);
        Material mat = this.assetLoader.getMaterial("defMat").clone();
        mat.setColor("Diffuse", new ColorRGBA(0.95f, 0.93f, 0.88f, 1f));
        mat.setBoolean("UseMaterialColors", true);
        placardGeom.setMaterial(mat);
        placardGeom.setLocalTranslation(x, y, z);
    }

    /**
     * Create upholstered benches (Met style)
     */
    private void createMetBenches() {
        float[] positions = {-25f, 25f};

        for (float z : positions) {
            createUpholsteredBench("Bench_" + z, 0, 0.6f, z);
        }
    }

    /**
     * Create an upholstered bench
     */
    private void createUpholsteredBench(String name, float x, float y, float z) {
        Node benchNode = new Node(name);

        // Upholstered seat
        Box seat = new Box(4f, 0.3f, 1.2f);
        Geometry seatGeom = new Geometry(name + "_Seat", seat);
        Material seatMat = this.assetLoader.getMaterial("defMat").clone();
        seatMat.setColor("Diffuse", new ColorRGBA(0.5f, 0.2f, 0.15f, 1f)); // Burgundy
        seatMat.setColor("Ambient", new ColorRGBA(0.3f, 0.12f, 0.09f, 1f));
        seatMat.setBoolean("UseMaterialColors", true);
        seatGeom.setMaterial(seatMat);
        seatGeom.setLocalTranslation(0, 0.6f, 0);
        benchNode.attachChild(seatGeom);

        // Wooden legs
        Material legMat = this.assetLoader.getMaterial("defMat").clone();
        legMat.setColor("Diffuse", new ColorRGBA(0.3f, 0.2f, 0.1f, 1f));
        legMat.setBoolean("UseMaterialColors", true);

        float[] legPositions = {-3.5f, -1.2f, 1.2f, 3.5f};
        for (float legX : legPositions) {
            Box leg = new Box(0.15f, 0.3f, 0.15f);
            Geometry legGeom = new Geometry(name + "_Leg", leg);
            legGeom.setMaterial(legMat);
            legGeom.setLocalTranslation(legX, 0.25f, 0);
            benchNode.attachChild(legGeom);
        }

        benchNode.setLocalTranslation(x, y, z);
        galleryNode.attachChild(benchNode);
    }

    /**
     * Create glass display cases for artifacts
     */
    private void createDisplayCases() {
        createDisplayCase("DisplayCase1", -10f, 0, -15f);
        createDisplayCase("DisplayCase2", 10f, 0, -15f);
        createDisplayCase("DisplayCase3", -10f, 0, 15f);
        createDisplayCase("DisplayCase4", 10f, 0, 15f);
    }

    /**
     * Create a single glass display case
     */
    private void createDisplayCase(String name, float x, float y, float z) {
        Node caseNode = new Node(name);

        // Base
        Box base = new Box(1.5f, 0.1f, 1.5f);
        Geometry baseGeom = new Geometry(name + "_Base", base);
        Material baseMat = this.assetLoader.getMaterial("defMat").clone();
        baseMat.setColor("Diffuse", new ColorRGBA(0.2f, 0.15f, 0.1f, 1f));
        baseMat.setBoolean("UseMaterialColors", true);
        baseGeom.setMaterial(baseMat);
        baseGeom.setLocalTranslation(0, 0.1f, 0);
        caseNode.attachChild(baseGeom);

        // Glass case
        Box glass = new Box(1.4f, 1.2f, 1.4f);
        Geometry glassGeom = new Geometry(name + "_Glass", glass);
        Material glassMat = this.assetLoader.getMaterial("defMat").clone();
        glassMat.setColor("Diffuse", new ColorRGBA(0.8f, 0.9f, 0.95f, 0.3f));
        glassMat.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);
        glassMat.setBoolean("UseMaterialColors", true);
        glassGeom.setMaterial(glassMat);
        glassGeom.setLocalTranslation(0, 1.4f, 0);
        caseNode.attachChild(glassGeom);

        caseNode.setLocalTranslation(x, y, z);
        galleryNode.attachChild(caseNode);
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
            System.out.println("✅ Animations available: " + animComposer.getAnimClipsNames());
            if (animComposer.getAnimClipsNames().contains("Idle")) {
                animComposer.setCurrentAction("Idle");
            } else {
                String firstAnim = animComposer.getAnimClipsNames().stream().findFirst().orElse(null);
                if (firstAnim != null) animComposer.setCurrentAction(firstAnim);
            }
        } else {
            System.out.println("⚠️ No AnimComposer found on model.");
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
            System.out.println("🎥 Animation played: " + animName);
        } else {
            System.out.println("⚠️ Animation '" + animName + "' not found.");
        }
    }

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
            return name.contains("Canvas") || name.contains("Painting");
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

    /**
     * Create a central statue centerpiece (Abstract Classical Style)
     */
    private void createCenterpiece() {
        Node centerpieceNode = new Node("CenterpieceStatue");

        // --- 1. The Pedestal (Large Marble Base) ---
        Box pedestalBox = new Box(2f, 0.5f, 2f);
        Geometry pedestal = new Geometry("StatuePedestal", pedestalBox);

        // Reuse the marble material setup from columns
        Material marbleMat = this.assetLoader.getMaterial("defMat").clone();
        marbleMat.setColor("Diffuse", COLOR_COLUMN_MARBLE);
        marbleMat.setColor("Ambient", COLOR_COLUMN_MARBLE);
        Texture marbleTex = this.assetLoader.getTexture("black_gold_marble");
        marbleMat.setTexture("DiffuseMap", marbleTex);
        marbleMat.setBoolean("UseMaterialColors", false);

        pedestal.setMaterial(marbleMat);
        pedestal.setLocalTranslation(0, 0.8f, -3); // Sit on floor
        centerpieceNode.attachChild(pedestal);

        // --- 3. The Statue (Smart Auto-Fit) ---
        Spatial realStatue = this.assetLoader.getModel("statue");

        // 1. Reset scale to 1 to get accurate measurements first
        realStatue.setLocalScale(0.9f);

        // 2. Rotate it to face the camera (adjust -HALF_PI if needed)
        realStatue.rotate(0, FastMath.HALF_PI, 0);

        // 3. Update geometric state to calculate the bounding box correctly
        realStatue.updateModelBound();
        BoundingBox bounds = (BoundingBox) realStatue.getWorldBound();

        // --- CALCULATE SCALE ---
        // The pedestal is 5.0f units wide (2.5 ext * 2).
        // We want the statue to take up about 80% of that width (4.0f).
        float modelWidth = bounds.getXExtent() * 2;

        float desiredWidth = 2.0f;

        float finalScale = desiredWidth / modelWidth;

        realStatue.setLocalScale(finalScale);

        // --- CALCULATE POSITION ---
        // We need to know where the "feet" of the model are relative to its center.
        // The "minY" is the lowest point of the model.
        float feetLevel = bounds.getCenter().y - bounds.getYExtent();

        // The top of your pedestal is at Y = 1.7f (Base 0.8*2 + Trim 0.1)
        float pedestalTop = 1.7f;

        // Calculate how much we need to lift the model so feetLevel hits pedestalTop
        // Note: We multiply feetLevel by finalScale because we just resized it!
        float yOffset = pedestalTop - (feetLevel * finalScale);

        realStatue.setLocalTranslation(0, yOffset, -2);

        centerpieceNode.attachChild(realStatue);

        // --- 4. Special Lighting for the Centerpiece ---
        SpotLight centerSpot = new SpotLight();
        centerSpot.setPosition(new Vector3f(0, 14f, 8f)); // From ceiling, angled slightly
        centerSpot.setDirection(new Vector3f(0, -1, -0.5f).normalizeLocal());
        centerSpot.setColor(new ColorRGBA(1f, 0.95f, 0.8f, 1.5f)); // Warm, bright light
        centerSpot.setSpotRange(30f);
        centerSpot.setSpotInnerAngle(10f * FastMath.DEG_TO_RAD);
        centerSpot.setSpotOuterAngle(25f * FastMath.DEG_TO_RAD);
        rootNode.addLight(centerSpot);

        // Add collision to the statue so the robot doesn't walk through it
        // (Optional: requires adding a physics control if using physics,
        // otherwise purely visual in this basic setup)

        galleryNode.attachChild(centerpieceNode);
    }



}