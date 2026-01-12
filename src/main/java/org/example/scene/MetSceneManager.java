package org.example.scene;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.font.Rectangle;
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
    // REMOVED: AnimComposer and animation variables
    private AssetLoader assetLoader;
    private DBManager db;

    // ✅ Variables pour la bulle d'information
    private Node infoBubbleNode;
    private BitmapText bubbleText;
    private Geometry bubbleBackground;
    private float bubbleDisplayTime = 0f;
    //    private static final float BUBBLE_DURATION = 8f; // 8 secondes d'affichage
    private boolean sceneReady = false;
    private Vector3f currentBubbleTargetPos = new Vector3f();

    // ✅ Chat Panel UI
    private ChatPanelUI chatPanelUI;

    // REMOVED: Movement variables (robotWalking, robotWalkTime, etc.)
    // REMOVED: Painting locking variables (currentActivePainting, robotStayingNearPainting)

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
                .addMaterial("Common/MatDefs/Misc/Unshaded.j3md", "unshMat")
                .addTexture("Textures/marble_floor.png", "marble_floor")
                .addTexture("Textures/black-wall.jpg", "black_wall")
                .addTexture("Textures/black-gold-marble.jpg", "black_gold_marble")
                .addTexture("Textures/gold_texture.jpg", "gold_texture")
                .addModel("Models/Robot/scene.gltf", "robot")
                .addModel("Models/marble_classical_statue_man_01__3d_printable.glb", "statue");

        String[] paths_left = {"painting16.jpg", "painting17.jpg", "painting18.jpg", "painting19.jpg", "painting20.jpg"};
        int i = 1;
        for(String path : paths_left){
            this.assetLoader.addTexture("Textures/"+ path, path.split("\\.[^.]*$")[0] + "_left_"+i);
            i++;
        }

        String[] paths_right = {"painting21.jpg", "painting22.png", "painting23.jpg", "painting24.jpg", "painting25.jpg"};
        i=1;
        for(String path : paths_right){
            this.assetLoader.addTexture("Textures/"+ path, path.split("\\.[^.]*$")[0] +  "_right_"+i);
            i++;
        }

        String[] paths_back = { "painting26.jpg", "painting27.jpg", "painting28.jpg", "painting29.jpg", "painting30.jpg"};
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

        createFloorBorder();
    }

    private void createFloorBorder() {
        float borderWidth = 2f;
        Material borderMat = this.assetLoader.getMaterial("defMat").clone();
        borderMat.setColor("Diffuse", new ColorRGBA(0.7f, 0.6f, 0.4f, 1f));
        borderMat.setBoolean("UseMaterialColors", true);

        Box northBorder = new Box(GALLERY_WIDTH / 2, 0.12f, borderWidth / 2);
        Geometry northGeom = new Geometry("BorderNorth", northBorder);
        northGeom.setMaterial(borderMat);
        northGeom.setLocalTranslation(0, -0.08f, -GALLERY_LENGTH / 2 + borderWidth / 2);
        galleryNode.attachChild(northGeom);

        Geometry southGeom = new Geometry("BorderSouth", northBorder);
        southGeom.setMaterial(borderMat);
        southGeom.setLocalTranslation(0, -0.08f, GALLERY_LENGTH / 2 - borderWidth / 2);
        galleryNode.attachChild(southGeom);
    }

    private void createWalls() {
        createWall("WallLeft", WALL_THICKNESS, GALLERY_HEIGHT, GALLERY_LENGTH,
                -GALLERY_WIDTH / 2, GALLERY_HEIGHT / 2, 0, "black_wall");

        createWall("WallRight", WALL_THICKNESS, GALLERY_HEIGHT, GALLERY_LENGTH,
                GALLERY_WIDTH / 2, GALLERY_HEIGHT / 2, 0, "black_wall");

        createWall("WallBack", GALLERY_WIDTH, GALLERY_HEIGHT, WALL_THICKNESS,
                0, GALLERY_HEIGHT / 2, -GALLERY_LENGTH / 2, "black_wall");

        createWall("WallFront", GALLERY_WIDTH, GALLERY_HEIGHT, WALL_THICKNESS,
                0, GALLERY_HEIGHT / 2, GALLERY_LENGTH / 2, "black_wall");

        createCrownMolding();
    }

    private void createWall(String name, float width, float height, float depth,
                            float x, float y, float z, String textureKey) {
        Box wallBox = new Box(width / 2, height / 2, depth / 2);
        Geometry wall = new Geometry(name, wallBox);
        Material mat = this.assetLoader.getMaterial("defMat").clone();

        try {
            Texture texture = this.assetLoader.getTexture(textureKey);
            texture.setWrap(Texture.WrapMode.Repeat);
            mat.setTexture("DiffuseMap", texture);
            float textureScale = calculateTextureScale(width, height);
            wallBox.scaleTextureCoordinates(new Vector2f(textureScale, textureScale));
            mat.setBoolean("UseMaterialColors", true);
            mat.setColor("Diffuse", ColorRGBA.White);
            mat.setColor("Ambient", ColorRGBA.Gray);
        } catch (Exception e) {
            ColorRGBA fallbackColor = COLOR_WALL_CREAM;
            mat.setColor("Diffuse", fallbackColor);
            mat.setColor("Ambient", fallbackColor);
            mat.setBoolean("UseMaterialColors", true);
        }

        wall.setMaterial(mat);
        wall.setLocalTranslation(x, y, z);
        galleryNode.attachChild(wall);
    }

    private float calculateTextureScale(float width, float height) {
        if (width < height) {
            return 4.0f;
        }
        return 2.0f;
    }

    private void createCrownMolding() {
        Material moldingMat = this.assetLoader.getMaterial("defMat").clone();
        moldingMat.setColor("Diffuse", COLOR_TRIM_GOLD);
        moldingMat.setBoolean("UseMaterialColors", true);

        float moldingHeight = 0.4f;
        float moldingDepth = 0.3f;

        Box leftMolding = new Box(moldingDepth / 2, moldingHeight / 2, GALLERY_LENGTH / 2);
        Geometry leftMoldingGeom = new Geometry("MoldingLeft", leftMolding);
        leftMoldingGeom.setMaterial(moldingMat);
        leftMoldingGeom.setLocalTranslation(-GALLERY_WIDTH / 2 + moldingDepth / 2,
                GALLERY_HEIGHT - moldingHeight / 2, 0);
        galleryNode.attachChild(leftMoldingGeom);

        Geometry rightMoldingGeom = new Geometry("MoldingRight", leftMolding);
        rightMoldingGeom.setMaterial(moldingMat);
        rightMoldingGeom.setLocalTranslation(GALLERY_WIDTH / 2 - moldingDepth / 2,
                GALLERY_HEIGHT - moldingHeight / 2, 0);
        galleryNode.attachChild(rightMoldingGeom);
    }

    private void createClassicalColumns() {
        int numColumns = 6;
        float spacing = GALLERY_LENGTH / (numColumns + 1);

        for (int i = 0; i < numColumns; i++) {
            float z = -GALLERY_LENGTH / 2 + spacing * (i + 1);
            createCorinthianColumn("ColumnLeft_" + i, -16f, z);
            createCorinthianColumn("ColumnRight_" + i, 16f, z);
        }
    }

    private void createCorinthianColumn(String name, float x, float z) {
        Node columnNode = new Node(name);

        Material columnMat = this.assetLoader.getMaterial("defMat").clone();
        columnMat.setColor("Diffuse", COLOR_COLUMN_MARBLE);
        columnMat.setColor("Ambient", COLOR_COLUMN_MARBLE.mult(0.8f));

        Texture columnTexture = this.assetLoader.getTexture("black_gold_marble");
        columnMat.setTexture("DiffuseMap", columnTexture);
        columnMat.setBoolean("UseMaterialColors", false);

        Box base = new Box(1.2f, 0.4f, 1.2f);
        Geometry baseGeom = new Geometry(name + "_Base", base);
        baseGeom.setMaterial(columnMat);
        baseGeom.setLocalTranslation(0, 0.4f, 0);
        columnNode.attachChild(baseGeom);

        float shaftHeight = 10f;
        Cylinder shaft = new Cylinder(16, 32, 0.8f, shaftHeight, true);
        Geometry shaftGeom = new Geometry(name + "_Shaft", shaft);
        shaftGeom.setMaterial(columnMat);
        shaftGeom.setLocalTranslation(0, 0.8f + shaftHeight / 2, 0);
        shaftGeom.rotate(FastMath.HALF_PI, 0, 0);
        columnNode.attachChild(shaftGeom);

        createColumnCapital(name + "_Capital", columnNode, 0, shaftHeight + 0.8f, 0);

        columnNode.setLocalTranslation(x, 0, z);
        galleryNode.attachChild(columnNode);
    }

    private void createColumnCapital(String name, Node parent, float x, float y, float z) {
        Material capitalMat = this.assetLoader.getMaterial("defMat").clone();
        capitalMat.setColor("Diffuse", COLOR_TRIM_GOLD);
        capitalMat.setBoolean("UseMaterialColors", true);

        Box capitalBox = new Box(1.3f, 0.6f, 1.3f);
        Geometry capitalGeom = new Geometry(name, capitalBox);
        capitalGeom.setMaterial(capitalMat);
        capitalGeom.setLocalTranslation(x, y, z);
        parent.attachChild(capitalGeom);

        Cylinder ring = new Cylinder(16, 16, 1.0f, 0.2f, true);
        Geometry ringGeom = new Geometry(name + "_Ring", ring);
        ringGeom.setMaterial(capitalMat);
        ringGeom.setLocalTranslation(x, y - 0.4f, z);
        ringGeom.rotate(FastMath.HALF_PI, 0, 0);
        parent.attachChild(ringGeom);
    }

    private void createCofferedCeiling() {
        Material ceilingMat = this.assetLoader.getMaterial("defMat").clone();
        ColorRGBA ceilingColor = new ColorRGBA(0.92f, 0.90f, 0.85f, 1f);
        ceilingMat.setColor("Diffuse", ceilingColor);
        ceilingMat.setColor("Ambient", ceilingColor);
        ceilingMat.setColor("GlowColor", ceilingColor.mult(0.3f));
        ceilingMat.setBoolean("UseMaterialColors", true);

        Box ceiling = new Box(GALLERY_WIDTH / 2, 0.2f, GALLERY_LENGTH / 2);
        Geometry ceilingGeom = new Geometry("Ceiling", ceiling);
        ceilingGeom.setMaterial(ceilingMat);
        ceilingGeom.setLocalTranslation(0, GALLERY_HEIGHT, 0);
        galleryNode.attachChild(ceilingGeom);

        createCoffers();
    }

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

    private void createLighting() {
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(new ColorRGBA(0.6f, 0.6f, 0.62f, 1f));
        rootNode.addLight(ambient);

        DirectionalLight skylight = new DirectionalLight();
        skylight.setDirection(new Vector3f(0.1f, -1f, 0.05f).normalizeLocal());
        skylight.setColor(new ColorRGBA(1f, 0.98f, 0.95f, 1f));
        rootNode.addLight(skylight);

        createArtworkSpotlights();
    }

    private void createArtworkSpotlights() {
        for (int i = 0; i < 5; i++) {
            float z = -28f + i * 14f;
            createSpotlight(-18f, 10f, z, new Vector3f(1, -0.8f, 0));
        }

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
        spot.setSpotRange(15f);
        spot.setSpotInnerAngle(20f * FastMath.DEG_TO_RAD);
        spot.setSpotOuterAngle(40f * FastMath.DEG_TO_RAD);
        rootNode.addLight(spot);
    }

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

    private void createMetPainting(String name, float x, float y, float z,
                                   float width, float height, float rotationY, String textureKey) {
        Box frame = new Box(width / 2 + 0.15f, height / 2 + 0.15f, 0.08f);
        Geometry frameGeom = new Geometry(name.split("_")[0] + "_Frame", frame);
        Material frameMat = this.assetLoader.getMaterial("defMat").clone();
        frameMat.setColor("Diffuse", COLOR_TRIM_GOLD);
        frameMat.setColor("Ambient", COLOR_TRIM_GOLD.mult(0.7f));
        frameMat.setBoolean("UseMaterialColors", true);
        frameGeom.setMaterial(frameMat);

        Quad canvas = new Quad(width, height);
        Geometry canvasGeom = new Geometry(name.split("_")[0] + "_canvas", canvas);
        Material canvasMat = this.assetLoader.getMaterial("unshMat").clone();

        Texture paintingTex = this.assetLoader.getTexture(textureKey);
        paintingTex.setMinFilter(Texture.MinFilter.Trilinear);
        paintingTex.setMagFilter(Texture.MagFilter.Bilinear);
        paintingTex.setAnisotropicFilter(16);

        // IMPORTANT: Unshaded uses "ColorMap", Lighting uses "DiffuseMap"
        canvasMat.setTexture("ColorMap", paintingTex);

        canvasGeom.setMaterial(canvasMat);
        canvasGeom.setLocalTranslation(-width / 2, -height / 2, 0.09f);

        createPaintingPlacard(name + "_Placard", 0, -height / 2 - 0.4f, 0.09f, width);

        Node paintingNode = new Node(name);
        paintingNode.attachChild(frameGeom);
        paintingNode.attachChild(canvasGeom);
        paintingNode.setLocalTranslation(x, y, z);
        paintingNode.rotate(0, rotationY * FastMath.DEG_TO_RAD, 0);

        galleryNode.attachChild(paintingNode);
    }

    private void createPaintingPlacard(String name, float x, float y, float z, float width) {
        Box placard = new Box(width / 3, 0.15f, 0.02f);
        Geometry placardGeom = new Geometry(name, placard);
        Material mat = this.assetLoader.getMaterial("defMat").clone();
        mat.setColor("Diffuse", new ColorRGBA(0.95f, 0.93f, 0.88f, 1f));
        mat.setBoolean("UseMaterialColors", true);
        placardGeom.setMaterial(mat);
        placardGeom.setLocalTranslation(x, y, z);
    }

    private void createMetBenches() {
        float[] positions = {-25f, 25f};
        for (float z : positions) {
            createUpholsteredBench("Bench_" + z, 0, 0.6f, z);
        }
    }

    private void createUpholsteredBench(String name, float x, float y, float z) {
        Node benchNode = new Node(name);

        Box seat = new Box(4f, 0.3f, 1.2f);
        Geometry seatGeom = new Geometry(name + "_Seat", seat);
        Material seatMat = this.assetLoader.getMaterial("defMat").clone();
        seatMat.setColor("Diffuse", new ColorRGBA(0.5f, 0.2f, 0.15f, 1f));
        seatMat.setColor("Ambient", new ColorRGBA(0.3f, 0.12f, 0.09f, 1f));
        seatMat.setBoolean("UseMaterialColors", true);
        seatGeom.setMaterial(seatMat);
        seatGeom.setLocalTranslation(0, 0.6f, 0);
        benchNode.attachChild(seatGeom);

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

    private void createDisplayCases() {
        createDisplayCase("DisplayCase1", -10f, 0, -15f);
        createDisplayCase("DisplayCase2", 10f, 0, -15f);
        createDisplayCase("DisplayCase3", -10f, 0, 15f);
        createDisplayCase("DisplayCase4", 10f, 0, 15f);
    }

    private void createDisplayCase(String name, float x, float y, float z) {
        Node caseNode = new Node(name);

        Box base = new Box(1.5f, 0.1f, 1.5f);
        Geometry baseGeom = new Geometry(name + "_Base", base);
        Material baseMat = this.assetLoader.getMaterial("defMat").clone();
        baseMat.setColor("Diffuse", new ColorRGBA(0.2f, 0.15f, 0.1f, 1f));
        baseMat.setBoolean("UseMaterialColors", true);
        baseGeom.setMaterial(baseMat);
        baseGeom.setLocalTranslation(0, 0.1f, 0);
        caseNode.attachChild(baseGeom);

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


    /**
     * ✅ MODIFIED: Load robot without animation or floor clamping logic
     */
    public void loadRobot() {
        robotManager = new RobotManager(this.assetManager);
        robotManager.setRobot(rootNode, assetLoader);
        robot = robotManager.getRobot();

        // Slightly smaller scale for a floating companion
        robot.scale(0.8f);

        // NOTE: We do NOT set local translation here anymore.
        // It will be handled in JmeMetApp update() loop relative to camera.

        app.getRootNode().attachChild(robot);

        // Add lights specifically for the robot
        DirectionalLight robotLight = new DirectionalLight();
        robotLight.setColor(ColorRGBA.White.mult(1.2f));
        robotLight.setDirection(new Vector3f(-0.5f, -1f, -0.3f).normalizeLocal());
        robot.addLight(robotLight);

        AmbientLight softAmbient = new AmbientLight();
        softAmbient.setColor(ColorRGBA.White.mult(0.3f));
        robot.addLight(softAmbient);
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
        hideRobot();

        hideBubble();
    }

    /**
     * 🔇 Cache la bulle d'info immédiatement
     */
    public void hideBubble() {
        bubbleDisplayTime = 0f;
        if (infoBubbleNode != null) {
            infoBubbleNode.setCullHint(Spatial.CullHint.Always);
        }
    }

    /**
     * ✅ MODIFIED: Hide robot bubbles only
     */
    public void hideRobot() {
        // Hide the bubble
        if (infoBubbleNode != null) {
            infoBubbleNode.setCullHint(Spatial.CullHint.Always);
        }
    }


    public void initializeClickDetection() {
        infoBubbleNode = new Node("InfoBubble");

        float baseW = 4.2f;
        float baseH = 2.4f;

        // ============ COUCHE 1 : CADRE (Anciennement OuterBorder) ============
        // On ne garde qu'une seule bordure pour définir la forme
        Quad frameQuad = new Quad(baseW + 0.2f, baseH + 0.2f);
        Geometry frame = new Geometry("BubbleFrame", frameQuad);
        Material frameMat = this.assetLoader.getMaterial("unshMat").clone();
        frameMat.setColor("Color", new ColorRGBA(0.25f, 0.45f, 0.75f, 0.8f)); // Bleu clair
        frameMat.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);
        frame.setMaterial(frameMat);
        // Position de base (Z = 0)
        frame.setLocalTranslation(-(baseW + 0.2f)/2f, -0.1f, 0f);
        infoBubbleNode.attachChild(frame);

        // ============ COUCHE 2 : FOND PRINCIPAL ============
        Quad bubbleQuad = new Quad(baseW, baseH);
        bubbleBackground = new Geometry("BubbleBackground", bubbleQuad);
        Material bubbleMat = this.assetLoader.getMaterial("unshMat").clone();
        bubbleMat.setColor("Color", new ColorRGBA(0.05f, 0.1f, 0.2f, 0.95f)); // Bleu foncé profond
        bubbleMat.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);
        bubbleBackground.setMaterial(bubbleMat);
        // ✅ Écart de 0.05f pour séparer nettement du cadre et éviter les lignes
        bubbleBackground.setLocalTranslation(-baseW/2f, 0f, 0.05f);
        infoBubbleNode.attachChild(bubbleBackground);

        // ============ COUCHE 3 : TEXTE (AGRANDI) ============
        BitmapFont font = assetManager.loadFont("Interface/Fonts/Default.fnt");
        bubbleText = new BitmapText(font);


        bubbleText.setSize(0.30f);
        bubbleText.setColor(ColorRGBA.White);

        float padding = 0.35f;
        bubbleText.setBox(new Rectangle(
                -baseW/2f + padding,
                baseH - (padding/2f),
                baseW - (padding*2),
                baseH - padding
        ));

        bubbleText.setAlignment(BitmapFont.Align.Center);
        bubbleText.setVerticalAlignment(BitmapFont.VAlign.Center);

        bubbleText.setLocalTranslation(0f, 0f, 0.1f);

        infoBubbleNode.attachChild(bubbleText);
        infoBubbleNode.setCullHint(Spatial.CullHint.Always);
        rootNode.attachChild(infoBubbleNode);
    }


    /**
     * ✅ MODIFIED: Removed walk logic calls
     */
    public void detectPaintingClick() {
        try {
            CollisionResults results = new CollisionResults();
            Vector2f screenCenter = new Vector2f(app.getCamera().getWidth() / 2f, app.getCamera().getHeight() / 2f);

            Vector3f origin = app.getCamera().getWorldCoordinates(screenCenter, 0f);
            Vector3f dir = app.getCamera().getWorldCoordinates(screenCenter, 1f).subtractLocal(origin).normalizeLocal();

            Ray ray = new Ray(origin, dir);
            rootNode.collideWith(ray, results);

            if (results.size() > 0) {
                CollisionResult closest = results.getClosestCollision();
                Geometry geom = closest.getGeometry();
                String name = geom.getName();

                if (name.contains("canvas") || name.contains("painting")) {
                    currentBubbleTargetPos = geom.getWorldTranslation().clone();

                    String paintingInfo = getPaintingInfo(name);

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

                    showInfoBubble(paintingInfo);
                } else {
                    System.out.println("⚠️ Objet cliqué (pas un tableau) : " + name);
                }
            } else {
                System.out.println("⚠️ Aucun objet cliqué");
            }

        } catch (Exception e) {
            System.err.println("❌ CRITICAL ERROR in detectPaintingClick():");
            e.printStackTrace();
        }
    }

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

    private String getPaintingInfo(String paintingName) {
        if (paintingName.contains("canvas") || paintingName.contains("painting")) {
            paintingName = paintingName.split("_")[0];
        }

        ResultSet res = null;
        try {
            res = this.db.getPaintingById(paintingName);
            if (res != null && res.next()) {
                String description = res.getString("description");
                res.close();
                return description;
            } else {
                return "Painting information not available.";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (res != null) res.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void showRobotSpeech(String text) {
        System.out.println("🤖 AI Speech (Audio only): " + text);
    }

    /**
     * ✅ Affiche la bulle d'information au-dessus du robot
     */
    private void showInfoBubble(String text) {
        if (bubbleText != null && infoBubbleNode != null) {
            bubbleText.setText(text);

            float readingTime = 1.0f + (text.length() * 0.05f);

            bubbleDisplayTime = Math.min(Math.max(readingTime, 3.0f), 15.0f);

            infoBubbleNode.setCullHint(Spatial.CullHint.Never);

            System.out.println("💬 Bulle affichée pour " + bubbleDisplayTime + " secondes");
        }
    }


    public void update(float tpf, com.jme3.renderer.Camera cam) {
        //  Mettre à jour la position de la bulle
        updateInfoBubblePosition(cam);
        updateBubbleTimer(tpf);
    }

    private void updateBubbleTimer(float tpf) {
        if (bubbleDisplayTime > 0) {
            bubbleDisplayTime -= tpf;

            if (bubbleDisplayTime <= 0) {
                if (infoBubbleNode != null) {
                    infoBubbleNode.setCullHint(Spatial.CullHint.Always);
                }
                System.out.println("⏱️ Temps écoulé - Bulle masquée");
            }
        }
    }

    private void updateInfoBubblePosition(com.jme3.renderer.Camera cam) {
        if (bubbleDisplayTime > 0 && currentBubbleTargetPos != null) {
            Vector3f pos = currentBubbleTargetPos.clone();

            pos.y -= 3.2f;

            Vector3f dirToCam = cam.getLocation().subtract(pos).normalizeLocal();

            pos.addLocal(dirToCam.mult(1.8f));

            infoBubbleNode.setLocalTranslation(pos);
            infoBubbleNode.lookAt(cam.getLocation(), Vector3f.UNIT_Y);

            infoBubbleNode.setCullHint(Spatial.CullHint.Never);
        } else {
            if (infoBubbleNode != null) {
                infoBubbleNode.setCullHint(Spatial.CullHint.Always);
            }
        }
    }

    private void createCenterpiece() {
        Node centerpieceNode = new Node("CenterpieceStatue");

        Box pedestalBox = new Box(2f, 0.5f, 2f);
        Geometry pedestal = new Geometry("StatuePedestal", pedestalBox);

        Material marbleMat = this.assetLoader.getMaterial("defMat").clone();
        marbleMat.setColor("Diffuse", COLOR_COLUMN_MARBLE);
        marbleMat.setColor("Ambient", COLOR_COLUMN_MARBLE);
        Texture marbleTex = this.assetLoader.getTexture("black_gold_marble");
        marbleMat.setTexture("DiffuseMap", marbleTex);
        marbleMat.setBoolean("UseMaterialColors", false);

        pedestal.setMaterial(marbleMat);
        pedestal.setLocalTranslation(0, 0.8f, -3);
        centerpieceNode.attachChild(pedestal);

        Spatial realStatue = this.assetLoader.getModel("statue");
        realStatue.setLocalScale(0.9f);
        realStatue.rotate(0, FastMath.HALF_PI, 0);
        realStatue.updateModelBound();
        BoundingBox bounds = (BoundingBox) realStatue.getWorldBound();

        float modelWidth = bounds.getXExtent() * 2;
        float desiredWidth = 2.0f;
        float finalScale = desiredWidth / modelWidth;
        realStatue.setLocalScale(finalScale);

        float feetLevel = bounds.getCenter().y - bounds.getYExtent();
        float pedestalTop = 1.7f;
        float yOffset = pedestalTop - (feetLevel * finalScale);

        realStatue.setLocalTranslation(0, yOffset, -2);
        centerpieceNode.attachChild(realStatue);

        SpotLight centerSpot = new SpotLight();
        centerSpot.setPosition(new Vector3f(0, 14f, 8f));
        centerSpot.setDirection(new Vector3f(0, -1, -0.5f).normalizeLocal());
        centerSpot.setColor(new ColorRGBA(1f, 0.95f, 0.8f, 1.5f));
        centerSpot.setSpotRange(30f);
        centerSpot.setSpotInnerAngle(10f * FastMath.DEG_TO_RAD);
        centerSpot.setSpotOuterAngle(25f * FastMath.DEG_TO_RAD);
        rootNode.addLight(centerSpot);

        galleryNode.attachChild(centerpieceNode);
    }
}