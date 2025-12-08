package org.example.scene;

import com.jme3.anim.AnimComposer;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.collision.Collidable;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
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

/**
 * 🏛️ ThSceneManager - MUSÉE D'ORSAY STYLE
 * Grand hall majestueux avec verrière, architecture élégante et 6 statues
 * Inspiré du célèbre Musée d'Orsay de Paris
 */
public class ThSceneManager {
    private final SimpleApplication app;
    private final AssetManager assetManager;
    private final Node rootNode;
    private final Node museumNode;
    private RobotManager robotManager;
    private Spatial robot;
    private AnimComposer animComposer;
    private AssetLoader assetLoader;
    private DBManager db;

    // Info bubble and chat variables
    private Node infoBubbleNode;
    private BitmapText bubbleText;
    private Geometry bubbleBackground;
    private float bubbleDisplayTime = 0f;
    private static final float BUBBLE_DURATION = 3f; // Réduit à 3 secondes
    private boolean sceneReady = false;
    private ChatPanelUI chatPanelUI;

    // Robot movement variables
    private boolean robotWalking = false;
    private float robotWalkTime = 0f;
    private static final float ROBOT_WALK_DURATION = 2f;
    private Vector3f robotTargetPosition = new Vector3f();
    private Vector3f robotStartPosition = new Vector3f();
    private float robotWalkProgress = 0f;
    private Geometry currentActivePainting = null;
    private boolean robotStayingNearPainting = false;

    // 🏛️ Musée d'Orsay dimensions (grand hall)
    private static final float HALL_WIDTH = 80f;
    private static final float HALL_LENGTH = 120f;
    private static final float HALL_HEIGHT = 25f;
    private static final float ARCH_HEIGHT = 30f;
    private static final float WALL_THICKNESS = 1.5f;

    // 🎨 Palette élégante Musée d'Orsay
    private static final ColorRGBA COLOR_CREAM_STONE = new ColorRGBA(0.96f, 0.94f, 0.88f, 1f);
    private static final ColorRGBA COLOR_WARM_BEIGE = new ColorRGBA(0.92f, 0.88f, 0.80f, 1f);
    private static final ColorRGBA COLOR_GOLD_ACCENT = new ColorRGBA(0.85f, 0.72f, 0.40f, 1f);
    private static final ColorRGBA COLOR_DARK_BRONZE = new ColorRGBA(0.30f, 0.25f, 0.18f, 1f);
    private static final ColorRGBA COLOR_BLACK_IRON = new ColorRGBA(0.15f, 0.15f, 0.18f, 1f);
    private static final ColorRGBA COLOR_MARBLE_WHITE = new ColorRGBA(0.98f, 0.97f, 0.95f, 1f);
    private static final ColorRGBA COLOR_MARBLE_CREAM = new ColorRGBA(0.94f, 0.91f, 0.85f, 1f);
    private static final ColorRGBA COLOR_GLASS_TINT = new ColorRGBA(0.85f, 0.92f, 0.98f, 0.3f);

    public ThSceneManager(SimpleApplication app) {
        this.app = app;
        this.assetManager = app.getAssetManager();
        this.rootNode = app.getRootNode();
        this.museumNode = new Node("MuseeDOrsayNode");
        rootNode.attachChild(museumNode);
        this.assetLoader = new AssetLoader();
        app.getStateManager().attach(this.assetLoader);
        this.db = new DBManager();
        
        System.out.println("🏛️ Initializing Musée d'Orsay Style Hall...");
    }

    public void initializeScene() {
        System.out.println("🏛️ === CONSTRUCTION DU GRAND HALL STYLE ORSAY ===");
        createElegantFloor();
        createGrandHallWalls();
        createArchedGlassCeiling();
        createDecorativeBalconies();
        createSixStatueExhibits();
        createGalleryPaintings();
        createMuseumLighting();
        createDecorations();
        loadRobot();
        System.out.println("✅ Grand Hall Musée d'Orsay construit!");
    }

    public void setAssetsToLoad() {
        this.assetLoader
                .addMaterial("Common/MatDefs/Light/Lighting.j3md", "defMat")
                .addTexture("Textures/marble_floor.png", "marble_floor")
                .addTexture("Textures/wood_floor.jpg", "wood_floor")
                .addTexture("Textures/gold_texture.jpg", "gold_texture")
                .addModel("Models/robot.glb", "robot")
                .addTexture("Textures/texture.png", "robotTexture")
                
                // 🗿 Chargement des 6 statues avec noms CORRECTS
                .addModel("Models/colossal_bust_ramesses_ii.glb", "ramesses")
                .addModel("Models/head_of_king_menkaure.glb", "athena")
                .addModel("Models/statue_dathena.glb", "angel")
                .addModel("Models/elisabeth.glb", "elisabeth")
                .addModel("Models/nepomuk.glb", "nepomuk")
                .addModel("Models/marble_classical_statue_man_01__3d_printable.glb", "classical_man");

        // 🖼️ Chargement des peintures (31-40)
        for (int i = 31; i <= 40; i++) {
            String filename = "painting" + i + ".jpg";
            if (i == 38) filename = "painting38.png";
            this.assetLoader.addTexture("Textures/" + filename, "painting" + i);
        }

        // Callback quand tout est chargé
        this.assetLoader.onComplete(() -> {
            System.out.println("✅ Tous les assets du Musée chargés!");
            initializeScene();
            initializeClickDetection();
            ((JmeThApp) app).showCrosshair();
           app.getInputManager().setCursorVisible(false);
            sceneReady = true;

            System.out.println("🚀 Initializing ChatPanelUI...");
            try {
                this.chatPanelUI = new ChatPanelUI(app, this);
                System.out.println("✅ ChatPanelUI initialized successfully");
            } catch (Exception e) {
                System.err.println("❌ FATAL ERROR: Failed to initialize ChatPanelUI!");
                e.printStackTrace();
                this.chatPanelUI = null;
            }
        });
    }

    /**
     * 🏛️ Sol élégant en marbre avec motif géométrique complexe
     */
    private void createElegantFloor() {
        System.out.println("🔨 Construction du sol en marbre élégant...");
        
        // Sol principal - grand damier sophistiqué
        float tileSize = 4f;
        int tilesX = (int) (HALL_WIDTH / tileSize);
        int tilesZ = (int) (HALL_LENGTH / tileSize);

        for (int x = 0; x < tilesX; x++) {
            for (int z = 0; z < tilesZ; z++) {
                Box tile = new Box(tileSize / 2 - 0.02f, 0.15f, tileSize / 2 - 0.02f);
                Geometry tileGeom = new Geometry("FloorTile_" + x + "_" + z, tile);
                
                Material tileMat = assetLoader.getMaterial("defMat").clone();
                
                // Motif élaboré: losanges alternés
                int pattern = (x + z) % 3;
                switch (pattern) {
                    case 0:
                        tileMat.setColor("Diffuse", COLOR_MARBLE_WHITE);
                        break;
                    case 1:
                        tileMat.setColor("Diffuse", COLOR_MARBLE_CREAM);
                        break;
                    case 2:
                        tileMat.setColor("Diffuse", COLOR_WARM_BEIGE);
                        break;
                }
                
                tileMat.setColor("Ambient", COLOR_MARBLE_CREAM.mult(0.6f));
                tileMat.setBoolean("UseMaterialColors", true);
                tileMat.setFloat("Shininess", 64f);
                tileMat.setColor("Specular", ColorRGBA.White.mult(0.5f));
                
                tileGeom.setMaterial(tileMat);
                tileGeom.setLocalTranslation(
                    (x - tilesX / 2f) * tileSize + tileSize / 2,
                    -0.15f,
                    (z - tilesZ / 2f) * tileSize + tileSize / 2
                );
                
                museumNode.attachChild(tileGeom);
            }
        }
        
        // Bordure décorative centrale
        createCentralFloorDecoration();
        
        System.out.println("✅ Sol en marbre sophistiqué créé: " + (tilesX * tilesZ) + " dalles");
    }

    /**
     * 🎨 Décoration centrale du sol (allée principale)
     */
    private void createCentralFloorDecoration() {
        // Allée centrale surélevée en marbre noir
        Box centralPath = new Box(6f, 0.05f, HALL_LENGTH / 2 - 5f);
        Geometry pathGeom = new Geometry("CentralPath", centralPath);
        Material pathMat = assetLoader.getMaterial("defMat").clone();
        pathMat.setColor("Diffuse", COLOR_BLACK_IRON);
        pathMat.setColor("Ambient", COLOR_BLACK_IRON.mult(0.4f));
        pathMat.setBoolean("UseMaterialColors", true);
        pathMat.setFloat("Shininess", 128f);
        pathGeom.setMaterial(pathMat);
        pathGeom.setLocalTranslation(0, 0.05f, 0);
        museumNode.attachChild(pathGeom);
        
        // Bordures dorées
        for (float xOffset : new float[]{-6.2f, 6.2f}) {
            Box border = new Box(0.15f, 0.1f, HALL_LENGTH / 2 - 5f);
            Geometry borderGeom = new Geometry("GoldBorder", border);
            Material borderMat = assetLoader.getMaterial("defMat").clone();
            borderMat.setColor("Diffuse", COLOR_GOLD_ACCENT);
            borderMat.setBoolean("UseMaterialColors", true);
            borderMat.setFloat("Shininess", 96f);
            borderGeom.setMaterial(borderMat);
            borderGeom.setLocalTranslation(xOffset, 0.1f, 0);
            museumNode.attachChild(borderGeom);
        }
    }

    /**
     * 🏛️ Murs du grand hall avec architecture Haussmannienne
     */
    private void createGrandHallWalls() {
        System.out.println("🔨 Construction des murs style Haussmann...");
        
        Material wallMat = assetLoader.getMaterial("defMat").clone();
        wallMat.setColor("Diffuse", COLOR_CREAM_STONE);
        wallMat.setColor("Ambient", COLOR_CREAM_STONE.mult(0.7f));
        wallMat.setBoolean("UseMaterialColors", true);
        wallMat.setFloat("Shininess", 16f);

        // Mur arrière avec détails architecturaux
        Box backWall = new Box(HALL_WIDTH / 2, HALL_HEIGHT / 2, WALL_THICKNESS / 2);
        Geometry backWallGeom = new Geometry("BackWall", backWall);
        backWallGeom.setMaterial(wallMat.clone());
        backWallGeom.setLocalTranslation(0, HALL_HEIGHT / 2, -HALL_LENGTH / 2);
        museumNode.attachChild(backWallGeom);

        // Murs latéraux avec galeries
        createSideWallWithGallery(-HALL_WIDTH / 2, wallMat.clone());
        createSideWallWithGallery(HALL_WIDTH / 2, wallMat.clone());
        
        System.out.println("✅ Murs Haussmanniens érigés");
    }

    /**
     * 🏛️ Mur latéral avec galerie à deux niveaux
     */
    private void createSideWallWithGallery(float xPos, Material baseMat) {
        float sign = Math.signum(xPos);
        
        // Mur principal
        Box wall = new Box(WALL_THICKNESS / 2, HALL_HEIGHT / 2, HALL_LENGTH / 2);
        Geometry wallGeom = new Geometry("SideWall", wall);
        wallGeom.setMaterial(baseMat);
        wallGeom.setLocalTranslation(xPos, HALL_HEIGHT / 2, 0);
        museumNode.attachChild(wallGeom);
        
        // Pilastres décoratifs le long du mur
        for (int i = 0; i < 8; i++) {
            float z = -HALL_LENGTH / 2 + 8f + i * 14f;
            createPilaster(xPos - sign * 0.5f, 0, z, sign);
        }
        
        // Corniche supérieure
        Box cornice = new Box(1f, 0.5f, HALL_LENGTH / 2);
        Geometry corniceGeom = new Geometry("Cornice", cornice);
        Material corniceMat = baseMat.clone();
        corniceMat.setColor("Diffuse", COLOR_GOLD_ACCENT);
        corniceMat.setFloat("Shininess", 64f);
        corniceGeom.setMaterial(corniceMat);
        corniceGeom.setLocalTranslation(xPos - sign * 1.5f, HALL_HEIGHT - 0.5f, 0);
        museumNode.attachChild(corniceGeom);
    }

    /**
     * 🏛️ Pilastre décoratif (colonne murale)
     */
    private void createPilaster(float x, float y, float z, float direction) {
        Node pilasterNode = new Node("Pilaster");
        
        // Base du pilastre
        Box base = new Box(0.8f, 0.4f, 0.6f);
        Geometry baseGeom = new Geometry("PilasterBase", base);
        Material baseMat = assetLoader.getMaterial("defMat").clone();
        baseMat.setColor("Diffuse", COLOR_WARM_BEIGE);
        baseMat.setBoolean("UseMaterialColors", true);
        baseGeom.setMaterial(baseMat);
        baseGeom.setLocalTranslation(0, 0.4f, 0);
        pilasterNode.attachChild(baseGeom);
        
        // Fût du pilastre
        Box shaft = new Box(0.5f, HALL_HEIGHT / 2 - 2f, 0.4f);
        Geometry shaftGeom = new Geometry("PilasterShaft", shaft);
        Material shaftMat = assetLoader.getMaterial("defMat").clone();
        shaftMat.setColor("Diffuse", COLOR_CREAM_STONE);
        shaftMat.setBoolean("UseMaterialColors", true);
        shaftMat.setFloat("Shininess", 24f);
        shaftGeom.setMaterial(shaftMat);
        shaftGeom.setLocalTranslation(0, HALL_HEIGHT / 2, 0);
        pilasterNode.attachChild(shaftGeom);
        
        // Chapiteau doré
        Box capital = new Box(0.7f, 0.6f, 0.5f);
        Geometry capitalGeom = new Geometry("PilasterCapital", capital);
        Material capitalMat = assetLoader.getMaterial("defMat").clone();
        capitalMat.setColor("Diffuse", COLOR_GOLD_ACCENT);
        capitalMat.setBoolean("UseMaterialColors", true);
        capitalMat.setFloat("Shininess", 96f);
        capitalGeom.setMaterial(capitalMat);
        capitalGeom.setLocalTranslation(0, HALL_HEIGHT - 1.5f, 0);
        pilasterNode.attachChild(capitalGeom);
        
        pilasterNode.setLocalTranslation(x, y, z);
        museumNode.attachChild(pilasterNode);
    }

    /**
     * 🏛️ Plafond voûté en verre avec structure métallique (signature Orsay)
     */
    private void createArchedGlassCeiling() {
        System.out.println("🔨 Construction de la verrière monumentale...");
        
        // Structure métallique arquée
        int archSegments = 12;
        float archWidth = HALL_WIDTH - 10f;
        
        for (int z = 0; z < 10; z++) {
            float zPos = -HALL_LENGTH / 2 + 10f + z * 12f;
            createArchStructure(zPos, archWidth, archSegments);
        }
        
        // Panneaux de verre
        createGlassPanels();
        
        // Fermettes transversales
        for (int i = 0; i < 9; i++) {
            float zPos = -HALL_LENGTH / 2 + 16f + i * 12f;
            createCrossBeam(zPos);
        }
        
        System.out.println("✅ Verrière monumentale achevée");
    }

    /**
     * 🏛️ Arc structural en métal
     */
    private void createArchStructure(float zPos, float width, int segments) {
        Material metalMat = assetLoader.getMaterial("defMat").clone();
        metalMat.setColor("Diffuse", COLOR_DARK_BRONZE);
        metalMat.setColor("Ambient", COLOR_DARK_BRONZE.mult(0.5f));
        metalMat.setBoolean("UseMaterialColors", true);
        metalMat.setFloat("Shininess", 32f);
        
        // Arcs paraboliques
        for (int i = 0; i < segments; i++) {
            float angle = (float) i / segments * FastMath.PI;
            float nextAngle = (float) (i + 1) / segments * FastMath.PI;
            
            float x1 = -width / 2 * FastMath.cos(angle);
            float y1 = HALL_HEIGHT + (ARCH_HEIGHT - HALL_HEIGHT) * FastMath.sin(angle);
            float x2 = -width / 2 * FastMath.cos(nextAngle);
            float y2 = HALL_HEIGHT + (ARCH_HEIGHT - HALL_HEIGHT) * FastMath.sin(nextAngle);
            
            // Poutre de l'arc
            Vector3f start = new Vector3f(x1, y1, zPos);
            Vector3f end = new Vector3f(x2, y2, zPos);
            createMetalBeam(start, end, 0.2f, metalMat.clone());
        }
    }

    /**
     * 🏛️ Poutre métallique entre deux points
     */
    private void createMetalBeam(Vector3f start, Vector3f end, float radius, Material mat) {
        Vector3f direction = end.subtract(start);
        float length = direction.length();
        
        Cylinder beam = new Cylinder(8, 12, radius, length, true);
        Geometry beamGeom = new Geometry("MetalBeam", beam);
        beamGeom.setMaterial(mat);
        
        // Positionnement et orientation
        Vector3f midpoint = start.add(end).mult(0.5f);
        beamGeom.setLocalTranslation(midpoint);
        
        Quaternion rotation = new Quaternion();
        rotation.lookAt(direction.normalize(), Vector3f.UNIT_Y);
        rotation.multLocal(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_X));
        beamGeom.setLocalRotation(rotation);
        
        museumNode.attachChild(beamGeom);
    }

    /**
     * 🏛️ Panneaux de verre de la verrière
     */
    private void createGlassPanels() {
        Material glassMat = assetLoader.getMaterial("defMat").clone();
        glassMat.setColor("Diffuse", COLOR_GLASS_TINT);
        glassMat.setColor("Ambient", new ColorRGBA(0.9f, 0.95f, 1f, 0.2f));
        glassMat.setBoolean("UseMaterialColors", true);
        glassMat.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);
        glassMat.setFloat("Shininess", 128f);
        
        // Grands panneaux de verre en forme de voûte
        for (int z = 0; z < 9; z++) {
            float zPos = -HALL_LENGTH / 2 + 10f + z * 12f + 6f;
            
            Box glassPanel = new Box(HALL_WIDTH / 2 - 8f, 0.05f, 5.5f);
            Geometry glassGeom = new Geometry("GlassPanel", glassPanel);
            glassGeom.setMaterial(glassMat.clone());
            glassGeom.setLocalTranslation(0, ARCH_HEIGHT - 2f, zPos);
            museumNode.attachChild(glassGeom);
        }
    }

    /**
     * 🏛️ Traverse transversale
     */
    private void createCrossBeam(float zPos) {
        Material metalMat = assetLoader.getMaterial("defMat").clone();
        metalMat.setColor("Diffuse", COLOR_BLACK_IRON);
        metalMat.setBoolean("UseMaterialColors", true);
        
        Box beam = new Box(HALL_WIDTH / 2 - 5f, 0.15f, 0.15f);
        Geometry beamGeom = new Geometry("CrossBeam", beam);
        beamGeom.setMaterial(metalMat);
        beamGeom.setLocalTranslation(0, ARCH_HEIGHT - 1f, zPos);
        museumNode.attachChild(beamGeom);
    }

    /**
     * 🏛️ Balcons décoratifs des deux côtés
     */
    private void createDecorativeBalconies() {
        System.out.println("🔨 Installation des galeries supérieures...");
        
        float balconyHeight = 12f;
        float balconyDepth = 4f;
        
        // Galeries gauche et droite
        for (float xSign : new float[]{-1f, 1f}) {
            float xPos = xSign * (HALL_WIDTH / 2 - balconyDepth / 2 - 2f);
            
            // Sol du balcon
            Box balconyFloor = new Box(balconyDepth / 2, 0.3f, HALL_LENGTH / 2 - 10f);
            Geometry floorGeom = new Geometry("BalconyFloor", balconyFloor);
            Material floorMat = assetLoader.getMaterial("defMat").clone();
            floorMat.setColor("Diffuse", COLOR_CREAM_STONE);
            floorMat.setBoolean("UseMaterialColors", true);
            floorGeom.setMaterial(floorMat);
            floorGeom.setLocalTranslation(xPos, balconyHeight, 0);
            museumNode.attachChild(floorGeom);
            
            // Balustrade
            createBalustrade(xPos - xSign * (balconyDepth / 2 - 0.3f), balconyHeight + 0.3f);
        }
        
        System.out.println("✅ Galeries installées");
    }

    /**
     * 🏛️ Balustrade élégante
     */
    private void createBalustrade(float xPos, float yPos) {
        Material balustradeMat = assetLoader.getMaterial("defMat").clone();
        balustradeMat.setColor("Diffuse", COLOR_MARBLE_WHITE);
        balustradeMat.setBoolean("UseMaterialColors", true);
        balustradeMat.setFloat("Shininess", 48f);
        
        // Main courante
        Box rail = new Box(0.1f, 0.1f, HALL_LENGTH / 2 - 10f);
        Geometry railGeom = new Geometry("Handrail", rail);
        railGeom.setMaterial(balustradeMat.clone());
        railGeom.setLocalTranslation(xPos, yPos + 1f, 0);
        museumNode.attachChild(railGeom);
        
        // Balustre (petites colonnes)
        for (int i = 0; i < 20; i++) {
            float zPos = -HALL_LENGTH / 2 + 15f + i * 5f;
            
            Cylinder baluster = new Cylinder(8, 12, 0.05f, 0.9f, true);
            Geometry balusterGeom = new Geometry("Baluster", baluster);
            balusterGeom.setMaterial(balustradeMat.clone());
            balusterGeom.rotate(FastMath.HALF_PI, 0, 0);
            balusterGeom.setLocalTranslation(xPos, yPos + 0.5f, zPos);
            museumNode.attachChild(balusterGeom);
        }
    }

    /**
     * 🗿 Crée les 6 statues avec les vrais modèles 3D - disposition Musée d'Orsay
     */
    private void createSixStatueExhibits() {
        System.out.println("🔨 Installation des 6 sculptures...");

        // ===== STATUE CENTRALE (Au sol - inchangée) =====
        createFloorStatue("Angel_Central", 0, 0, -20f, 0, "angel", 3.5f, "😇 Archange Central");

        // ===== STATUES SUR PIÉDESTAUX (Redimensionnement dynamique) =====
        // Note: Le paramètre 'scale' devient maintenant la LARGEUR DÉSIRÉE (en unités world)
        // Pour Ramsès, cela reste l'échelle brute (0.18f).

        // 2. ROI MENKAURE
        createStatueOnPedestal("Menkaure", -18f, 0, 15f, FastMath.QUARTER_PI, "athena", 4.0f,
                "🇪🇬 Roi Menkaure", 3f, COLOR_GOLD_ACCENT);

        // 3. SAINTE ELISABETH
        createStatueOnPedestal("Elisabeth", -18f, 0, -15f, FastMath.QUARTER_PI, "elisabeth", 4.0f,
                "👼 Sainte Elisabeth", 3f, COLOR_MARBLE_CREAM);

        // 4. HOMME CLASSIQUE
        createStatueOnPedestal("Classical_Man", 18f, 0, 15f, -FastMath.QUARTER_PI, "classical_man", 3.0f,
                "🏛️ Homme Classique", 3f, COLOR_MARBLE_WHITE);

        // 5. SAINT NÉPOMUK
        createStatueOnPedestal("Nepomuk", 18f, 0, -15f, -FastMath.QUARTER_PI, "nepomuk", 4.0f,
                "🙏 Saint Népomuk", 3f, COLOR_MARBLE_CREAM);

        // 6. RAMSÈS II (Exception : On garde l'échelle manuelle 0.18f)
        createStatueOnPedestal("Ramesses_II", 0, 0, -45f, 0, "ramesses", 3.0f,
                "🇪🇬 Ramsès II", 3f, COLOR_GOLD_ACCENT);

        System.out.println("✅ 6 sculptures installées!");
    }

    /**
     * 🗿 Crée une statue SUR un piédestal - MODIFIÉ pour redimensionnement automatique
     */
    private void createStatueOnPedestal(String name, float x, float y, float z,
                                        float rotation, String modelKey, float scaleOrWidth,
                                        String label, float pedestalHeight, ColorRGBA pedestalColor) {
        Node displayNode = new Node(name + "_Display");
        float finalScale = scaleOrWidth;
        float pedestalRadius; // Will be calculated based on statue width

        Spatial statue = null;

        // 1️⃣ ÉTAPE 1 : Charger et analyser la statue D'ABORD
        try {
            statue = assetLoader.getModel(modelKey).clone();

            // On réinitialise l'échelle à 1 pour mesurer la taille réelle du modèle brut
            statue.setLocalScale(1f);
            statue.rotate(0, rotation, 0);
            statue.updateModelBound();

            BoundingBox rawBounds = (BoundingBox) statue.getWorldBound();
            float modelRawWidth = rawBounds.getXExtent() * 2; // Largeur réelle (X)

            // --- LOGIQUE DE REDIMENSIONNEMENT ---
            float desiredWidth = scaleOrWidth;

            if (modelRawWidth > 0) {
                finalScale = desiredWidth / modelRawWidth;
            } else {
                finalScale = 1f; // Sécurité si bounds=0
            }

            // Le socle s'adapte à la largeur de la statue
            pedestalRadius = (desiredWidth / 2) * 1.1f;

            statue.setLocalScale(finalScale);

            System.out.println("  📏 " + name + " -> Width: " + modelRawWidth + " | Final Scale: " + finalScale);

        } catch (Exception e) {
            System.err.println("❌ Erreur chargement modèle: " + e.getMessage());
            // Valeurs par défaut si échec
            pedestalRadius = pedestalHeight / 3;
        }

        // 2️⃣ ÉTAPE 2 : Créer le Piédestal (maintenant qu'on a le bon rayon)

        // Base noire
        float baseHeight = 0.3f;
        float baseRadius = pedestalRadius + 0.3f; // Base un peu plus large que le cylindre
        Cylinder pedestalBase = new Cylinder(24, 32, baseRadius, baseHeight, true);
        Geometry baseGeom = new Geometry(name + "_Base", pedestalBase);
        Material baseMat = assetLoader.getMaterial("defMat").clone();
        baseMat.setColor("Diffuse", COLOR_BLACK_IRON);
        baseMat.setBoolean("UseMaterialColors", true);
        baseMat.setFloat("Shininess", 128f);
        baseGeom.setMaterial(baseMat);
        baseGeom.rotate(FastMath.HALF_PI, 0, 0);
        baseGeom.setLocalTranslation(0, baseHeight / 2, 0);
        displayNode.attachChild(baseGeom);

        // Socle principal
        Cylinder pedestal = new Cylinder(24, 32, pedestalRadius, pedestalHeight, true);
        Geometry pedestalGeom = new Geometry(name + "_Pedestal", pedestal);
        Material pedestalMat = assetLoader.getMaterial("defMat").clone();
        pedestalMat.setColor("Diffuse", pedestalColor);
        pedestalMat.setBoolean("UseMaterialColors", true);
        pedestalGeom.setMaterial(pedestalMat);
        pedestalGeom.rotate(FastMath.HALF_PI, 0, 0);
        pedestalGeom.setLocalTranslation(0, baseHeight + pedestalHeight / 2, 0);
        displayNode.attachChild(pedestalGeom);

        // Anneau doré
        Cylinder goldRing = new Cylinder(16, 32, pedestalRadius + 0.05f, 0.1f, true);
        Geometry ringGeom = new Geometry(name + "_Ring", goldRing);
        Material ringMat = assetLoader.getMaterial("defMat").clone();
        ringMat.setColor("Diffuse", COLOR_GOLD_ACCENT);
        ringMat.setBoolean("UseMaterialColors", true);
        ringGeom.setMaterial(ringMat);
        ringGeom.rotate(FastMath.HALF_PI, 0, 0);
        ringGeom.setLocalTranslation(0, baseHeight + pedestalHeight, 0);
        displayNode.attachChild(ringGeom);

        // 3️⃣ ÉTAPE 3 : Attacher la statue (si chargée correctement)
        if (statue != null) {
            statue.updateModelBound();
            BoundingBox bounds = (BoundingBox) statue.getWorldBound();

            // Calculer la position pour que les pieds touchent le haut du socle
            // Note: On utilise bounds.getYExtent() * finalScale si les bounds n'ont pas été refresh après scale
            float modelBottomY = bounds.getCenter().y - bounds.getYExtent();
            float pedestalTopY = baseHeight + pedestalHeight;
            float statueY = pedestalTopY - modelBottomY;

            statue.setLocalTranslation(0, statueY, 0);
            displayNode.attachChild(statue);

            System.out.println("  ✅ " + label + " placée sur socle large.");
        }

        // 💡 Lumière
        SpotLight statueSpot = new SpotLight();
        statueSpot.setPosition(new Vector3f(x, ARCH_HEIGHT - 5, z));
        statueSpot.setDirection(new Vector3f(0, -1, 0));
        statueSpot.setColor(new ColorRGBA(1f, 0.98f, 0.92f, 1f));
        statueSpot.setSpotOuterAngle(35f * FastMath.DEG_TO_RAD);
        rootNode.addLight(statueSpot);

        displayNode.setLocalTranslation(x, y, z);
        museumNode.attachChild(displayNode);
    }
    /**
     * 🗿 Crée une statue directement au sol (sans piédestal) - pour la pièce centrale
     */
    private void createFloorStatue(String name, float x, float y, float z,
                                   float rotation, String modelKey, float scale, String label) {
        Node displayNode = new Node(name + "_Display");

        // 🗿 Charger le modèle 3D directement au sol
        try {
            Spatial statue = assetLoader.getModel(modelKey).clone();
            statue.setLocalScale(scale);
            statue.rotate(0, rotation, 0);

            statue.updateModelBound();
            BoundingBox bounds = (BoundingBox) statue.getWorldBound();

            // Placer directement au sol (y = 0)
            float modelBottom = bounds.getCenter().y - bounds.getYExtent();
            float yOffset = -(modelBottom * scale);

            statue.setLocalTranslation(0, yOffset, 0);
            displayNode.attachChild(statue);

            System.out.println("  ✅ " + label + " " + name + " placée au sol (échelle: " + scale + ")");

            // 💡 Éclairage spot dramatique
            SpotLight statueSpot = new SpotLight();
            statueSpot.setPosition(new Vector3f(x, ARCH_HEIGHT - 5, z));
            statueSpot.setDirection(new Vector3f(0, -1, 0));
            statueSpot.setColor(new ColorRGBA(1f, 0.98f, 0.92f, 1f));
            statueSpot.setSpotRange(35f);
            statueSpot.setSpotInnerAngle(15f * FastMath.DEG_TO_RAD);
            statueSpot.setSpotOuterAngle(35f * FastMath.DEG_TO_RAD);
            rootNode.addLight(statueSpot);

        } catch (Exception e) {
            System.err.println("❌ Impossible de charger '" + modelKey + "': " + e.getMessage());
            e.printStackTrace();
        }

        displayNode.setLocalTranslation(x, y, z);
        museumNode.attachChild(displayNode);
    }

    /**
     * 🖼️ Crée les peintures sur les murs latéraux
     */
    private void createGalleryPaintings() {
        System.out.println("🔨 Installation des tableaux de la galerie...");
        
        float paintingHeight = 5f;
        float paintingWidth = paintingHeight * 0.8f;
        float wallOffset = HALL_WIDTH / 2 - WALL_THICKNESS - 0.3f;
        
        int paintingNum = 31;
        
        // Tableaux mur gauche
        for (int i = 0; i < 4; i++) {
            float z = -30f + i * 18f;
            createElegantPainting("painting" + paintingNum, -wallOffset, 7f, z, 
                        paintingWidth, paintingHeight, 90f, "painting" + paintingNum);
            paintingNum++;
        }
        
        // Tableaux mur droit
        for (int i = 0; i < 4; i++) {
            float z = -30f + i * 18f;
            createElegantPainting("painting" + paintingNum, wallOffset, 7f, z, 
                        paintingWidth, paintingHeight, -90f, "painting" + paintingNum);
            paintingNum++;
        }
        
        // Tableaux mur arrière (au-dessus de Ramsès)
        createElegantPainting("painting39", -15f, 10f, -HALL_LENGTH / 2 + WALL_THICKNESS + 0.5f,
                    paintingWidth * 1.2f, paintingHeight * 1.2f, 0f, "painting39");
        createElegantPainting("painting40", 15f, 10f, -HALL_LENGTH / 2 + WALL_THICKNESS + 0.5f,
                    paintingWidth * 1.2f, paintingHeight * 1.2f, 0f, "painting40");
        
        System.out.println("✅ 10 tableaux installés!");
    }

    /**
     * Crée un tableau élégant avec cadre doré raffiné
     */
    private void createElegantPainting(String name, float x, float y, float z,
                             float width, float height, float rotationY, String textureKey) {
        Node paintingNode = new Node(name + "_Painting");

        // Cadre extérieur doré
        Box outerFrame = new Box(width / 2 + 0.25f, height / 2 + 0.25f, 0.12f);
        Geometry outerFrameGeom = new Geometry(name + "_OuterFrame", outerFrame);
        Material outerMat = assetLoader.getMaterial("defMat").clone();
        outerMat.setColor("Diffuse", COLOR_GOLD_ACCENT);
        outerMat.setColor("Ambient", COLOR_GOLD_ACCENT.mult(0.5f));
        outerMat.setBoolean("UseMaterialColors", true);
        outerMat.setFloat("Shininess", 128f);
        outerFrameGeom.setMaterial(outerMat);
        paintingNode.attachChild(outerFrameGeom);

        // Cadre intérieur (baguette)
        Box innerFrame = new Box(width / 2 + 0.1f, height / 2 + 0.1f, 0.06f);
        Geometry innerFrameGeom = new Geometry(name + "_InnerFrame", innerFrame);
        Material innerMat = assetLoader.getMaterial("defMat").clone();
        innerMat.setColor("Diffuse", COLOR_DARK_BRONZE);
        innerMat.setBoolean("UseMaterialColors", true);
        innerFrameGeom.setMaterial(innerMat);
        innerFrameGeom.setLocalTranslation(0, 0, 0.07f);
        paintingNode.attachChild(innerFrameGeom);

        // 🎨 Toile
        Quad canvas = new Quad(width, height);
        Geometry canvasGeom = new Geometry(name + "_canvas", canvas);
        Material canvasMat = assetLoader.getMaterial("defMat").clone();
        
        try {
            Texture paintingTex = assetLoader.getTexture(textureKey);
            // Améliorer la qualité de la texture à distance
            paintingTex.setMinFilter(Texture.MinFilter.Trilinear);
            paintingTex.setMagFilter(Texture.MagFilter.Bilinear);
            paintingTex.setAnisotropicFilter(8); // Meilleure qualité à distance
            canvasMat.setTexture("DiffuseMap", paintingTex);
            canvasMat.setBoolean("UseMaterialColors", true);
            canvasMat.setColor("Ambient", ColorRGBA.White.mult(1.0f)); // Plus lumineux
            canvasMat.setColor("Diffuse", ColorRGBA.White); // Blanc pur pour bien voir les textures
        } catch (Exception e) {
            System.err.println("⚠️ Texture non trouvée: " + textureKey);
            canvasMat.setColor("Diffuse", COLOR_WARM_BEIGE);
            canvasMat.setBoolean("UseMaterialColors", true);
        }
        
        canvasGeom.setMaterial(canvasMat);
        canvasGeom.setLocalTranslation(-width / 2, -height / 2, 0.13f);
        paintingNode.attachChild(canvasGeom);

        // Lumière plus forte au-dessus du tableau pour visibilité à distance
        PointLight paintingLight = new PointLight();
        paintingLight.setPosition(new Vector3f(x, y + height / 2 + 1.5f, z));
        paintingLight.setColor(new ColorRGBA(1f, 0.98f, 0.92f, 1f).mult(1.2f)); // Plus lumineux
        paintingLight.setRadius(25f); // Rayon augmenté pour visibilité à distance
        rootNode.addLight(paintingLight);

        paintingNode.setLocalTranslation(x, y, z);
        paintingNode.rotate(0, rotationY * FastMath.DEG_TO_RAD, 0);
        museumNode.attachChild(paintingNode);
    }

    /**
     * 💡 Éclairage naturel style musée (lumière du jour à travers la verrière)
     */
    private void createMuseumLighting() {
        System.out.println("🔨 Éclairage naturel du musée...");
        
        // Lumière ambiante douce et chaude
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(new ColorRGBA(0.75f, 0.73f, 0.70f, 1f));
        rootNode.addLight(ambient);

        // Lumière directionnelle du soleil (à travers la verrière)
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.2f, -0.9f, -0.3f).normalizeLocal());
        sun.setColor(new ColorRGBA(1f, 0.98f, 0.92f, 1f).mult(0.8f));
        rootNode.addLight(sun);

        // Lumière secondaire pour équilibrer
        DirectionalLight fill = new DirectionalLight();
        fill.setDirection(new Vector3f(0.3f, -0.5f, 0.4f).normalizeLocal());
        fill.setColor(new ColorRGBA(0.9f, 0.92f, 0.98f, 1f).mult(0.3f));
        rootNode.addLight(fill);
        
        System.out.println("✅ Éclairage naturel installé");
    }

    /**
     * 🎨 Décorations additionnelles (bancs, plantes)
     */
    private void createDecorations() {
        System.out.println("🔨 Ajout des décorations...");
        
        // Bancs élégants le long de l'allée centrale
        for (int i = 0; i < 4; i++) {
            float z = -25f + i * 15f;
            createBench(-9f, 0, z);
            createBench(9f, 0, z);
        }
        
        System.out.println("✅ Décorations installées");
    }

    /**
     * 🪑 Banc de musée élégant
     */
    private void createBench(float x, float y, float z) {
        Node benchNode = new Node("Bench");
        
        // Assise
        Box seat = new Box(2f, 0.15f, 0.6f);
        Geometry seatGeom = new Geometry("BenchSeat", seat);
        Material seatMat = assetLoader.getMaterial("defMat").clone();
        seatMat.setColor("Diffuse", COLOR_DARK_BRONZE);
        seatMat.setBoolean("UseMaterialColors", true);
        seatMat.setFloat("Shininess", 32f);
        seatGeom.setMaterial(seatMat);
        seatGeom.setLocalTranslation(0, 0.5f, 0);
        benchNode.attachChild(seatGeom);
        
        // Pieds
        for (float xOff : new float[]{-1.7f, 1.7f}) {
            Box leg = new Box(0.1f, 0.25f, 0.5f);
            Geometry legGeom = new Geometry("BenchLeg", leg);
            legGeom.setMaterial(seatMat.clone());
            legGeom.setLocalTranslation(xOff, 0.25f, 0);
            benchNode.attachChild(legGeom);
        }
        
        benchNode.setLocalTranslation(x, y, z);
        museumNode.attachChild(benchNode);
    }

    /**
     * 🤖 Charge le robot guide
     */
    public void loadRobot() {
        System.out.println("🤖 Chargement du robot guide...");
        
        robotManager = new RobotManager(this.assetManager);
        robotManager.setRobot(rootNode, assetLoader);
        robot = robotManager.getRobot();
        robot.scale(1f);

        BoundingBox bbox = (BoundingBox) robot.getWorldBound();
        float minY = 0.1f + bbox.getYExtent();
        robot.setLocalTranslation(0, minY, 0);

        rootNode.attachChild(robot);

        animComposer = robot.getControl(AnimComposer.class);
        if (animComposer != null) {
            System.out.println("✅ Animations disponibles : " + animComposer.getAnimClipsNames());
            if (animComposer.getAnimClipsNames().contains("Idle")) {
                animComposer.setCurrentAction("Idle");
            } else {
                String firstAnim = animComposer.getAnimClipsNames().stream().findFirst().orElse(null);
                if (firstAnim != null) animComposer.setCurrentAction(firstAnim);
            }
        }

        DirectionalLight robotLight = new DirectionalLight();
        robotLight.setColor(ColorRGBA.White.mult(1.2f));
        robotLight.setDirection(new Vector3f(-0.5f, -1f, -0.3f).normalizeLocal());
        robot.addLight(robotLight);

        AmbientLight softAmbient = new AmbientLight();
        softAmbient.setColor(ColorRGBA.White.mult(0.3f));
        robot.addLight(softAmbient);
        
        System.out.println("✅ Robot guide prêt!");
    }

    public void playAnimation(String animName) {
        if (animComposer != null && animComposer.getAnimClipsNames().contains(animName)) {
            animComposer.setCurrentAction(animName);
        }
    }

    public Node getMuseumNode() {
        return museumNode;
    }

    public Spatial getRobot() {
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
        // Forcer la fermeture de la bulle aussi
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

    public void hideRobot() {
        robotStayingNearPainting = false;
        currentActivePainting = null;

        if (infoBubbleNode != null) {
            infoBubbleNode.setCullHint(Spatial.CullHint.Always);
        }
        bubbleDisplayTime = 0f; // Reset le timer

        if (animComposer != null) {
            playAnimation("Idle");
        }
    }

    public void initializeClickDetection() {
        infoBubbleNode = new Node("InfoBubble");

        // Design de la bulle (style élégant doré)
        Quad shadowQuad = new Quad(7.4f, 2.6f);
        Geometry shadow = new Geometry("BubbleShadow", shadowQuad);
        Material shadowMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        shadowMat.setColor("Color", new ColorRGBA(0f, 0f, 0f, 0.5f));
        shadowMat.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);
        shadow.setMaterial(shadowMat);
        shadow.setLocalTranslation(-3.7f, -0.15f, 0f);
        infoBubbleNode.attachChild(shadow);

        Quad outerBorderQuad = new Quad(7.2f, 2.4f);
        Geometry outerBorder = new Geometry("BubbleOuterBorder", outerBorderQuad);
        Material outerBorderMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        outerBorderMat.setColor("Color", COLOR_GOLD_ACCENT.mult(0.9f));
        outerBorderMat.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);
        outerBorder.setMaterial(outerBorderMat);
        outerBorder.setLocalTranslation(-3.6f, -0.1f, 0.005f);
        infoBubbleNode.attachChild(outerBorder);

        Quad innerBorderQuad = new Quad(7f, 2.2f);
        Geometry innerBorder = new Geometry("BubbleInnerBorder", innerBorderQuad);
        Material innerBorderMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        innerBorderMat.setColor("Color", COLOR_DARK_BRONZE.mult(0.95f));
        innerBorderMat.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);
        innerBorder.setMaterial(innerBorderMat);
        innerBorder.setLocalTranslation(-3.5f, -0.05f, 0.01f);
        infoBubbleNode.attachChild(innerBorder);

        Quad bubbleQuad = new Quad(6.8f, 2f);
        bubbleBackground = new Geometry("BubbleBackground", bubbleQuad);
        Material bubbleMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        bubbleMat.setColor("Color", new ColorRGBA(0.12f, 0.10f, 0.08f, 0.95f));
        bubbleMat.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);
        bubbleBackground.setMaterial(bubbleMat);
        bubbleBackground.setLocalTranslation(-3.4f, 0f, 0.015f);
        infoBubbleNode.attachChild(bubbleBackground);

        Quad highlightQuad = new Quad(6.6f, 0.3f);
        Geometry highlight = new Geometry("BubbleHighlight", highlightQuad);
        Material highlightMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        highlightMat.setColor("Color", COLOR_GOLD_ACCENT.mult(0.4f));
        highlightMat.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);
        highlight.setMaterial(highlightMat);
        highlight.setLocalTranslation(-3.3f, 1.6f, 0.02f);
        infoBubbleNode.attachChild(highlight);

        BitmapFont font = assetManager.loadFont("Interface/Fonts/Default.fnt");
        bubbleText = new BitmapText(font);
        bubbleText.setSize(0.22f);
        bubbleText.setColor(COLOR_GOLD_ACCENT);
        bubbleText.setText(null);
        bubbleText.setLocalTranslation(-3.2f, 1.1f, 0.025f);
        infoBubbleNode.attachChild(bubbleText);

        infoBubbleNode.setCullHint(Spatial.CullHint.Always);
        rootNode.attachChild(infoBubbleNode);

        System.out.println("✅ Bulle d'info élégante créée");
    }

    public void detectPaintingClick() {
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
            CollisionResult closest = results.getClosestCollision();
            Geometry geom = closest.getGeometry();

            String name = geom.getName();
            if (name.contains("canvas") || name.contains("painting")) {
                System.out.println("🖱️ Tableau cliqué : " + name);

                if (currentActivePainting != geom) {
                    robotStayingNearPainting = false;
                    currentActivePainting = null;
                }

                String paintingInfo = getPaintingInfo(name);

                if (chatPanelUI != null) {
                    chatPanelUI.show(paintingInfo);
                }

                showInfoBubble(paintingInfo);

                if (robot != null) {
                    startRobotWalkTowardsPainting(geom);
                    playAnimation("Talk");
                }
            }
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
            if (this.db== null) {
                System.err.println("❌ DATABASE ERROR: No active database connection.");
                return "Erreur: Base de données non connectée.";
            }
            res = this.db.getPaintingById(paintingName);

            if (res != null && res.next()) {
                String description = res.getString("description");
                res.close();
                return description;
            } else {
                return "Aucune information disponible pour ce tableau.";
            }
        } catch (SQLException e) {
            System.err.println("❌ DATABASE ERROR: " + e.getMessage());
            return "Erreur lors de la récupération des informations.";
        } finally {
            try {
                if (res != null) res.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void showRobotSpeech(String text) {
        if (bubbleText != null) {
            String display = text.length() > 100 ? text.substring(0, 97) + "..." : text;
            showInfoBubble(display);
        }
    }

    private void showInfoBubble(String text) {
        if (bubbleText != null && infoBubbleNode != null) {
            bubbleText.setText(text);
            infoBubbleNode.setCullHint(Spatial.CullHint.Never);
            bubbleDisplayTime = BUBBLE_DURATION;
        }
    }

    private void startRobotWalkTowardsPainting(Geometry painting) {
        if (robot == null) return;

        currentActivePainting = painting;
        robotStayingNearPainting = true;

        robotStartPosition.set(robot.getLocalTranslation());

        Vector3f paintingPos = painting.getWorldTranslation();
        Vector3f paintingNormal = painting.getWorldRotation().mult(Vector3f.UNIT_Z);

        robotTargetPosition.set(paintingPos.add(paintingNormal.mult(1.5f)));
        robotTargetPosition.y = robotStartPosition.y;

        Vector3f direction = paintingPos.subtract(robotStartPosition);
        direction.y = 0;
        direction.normalizeLocal();

        float angle = FastMath.atan2(direction.x, direction.z);
        robot.setLocalRotation(new Quaternion().fromAngleAxis(angle, Vector3f.UNIT_Y));

        robotWalking = true;
        robotWalkTime = 0f;
        robotWalkProgress = 0f;

        playAnimation("Walk");
    }

    public void update(float tpf, com.jme3.renderer.Camera cam) {
        updateRobotWalk(tpf);
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
            }
        }
    }

    private void updateRobotWalk(float tpf) {
        if (robotWalking && robot != null) {
            robotWalkTime += tpf;
            robotWalkProgress = Math.min(robotWalkTime / ROBOT_WALK_DURATION, 1f);

            Vector3f currentPos = robotStartPosition.interpolateLocal(robotTargetPosition, robotWalkProgress);
            robot.setLocalTranslation(currentPos);

            if (robotWalkProgress >= 1f) {
                robotWalking = false;
                playAnimation("Talk");
            }
        }

        if (robotStayingNearPainting && currentActivePainting != null && robot != null && !robotWalking) {
            Vector3f paintingPos = currentActivePainting.getWorldTranslation();
            Vector3f paintingNormal = currentActivePainting.getWorldRotation().mult(Vector3f.UNIT_Z);

            Vector3f targetPos = paintingPos.add(paintingNormal.mult(1.5f));
            targetPos.y = robot.getLocalTranslation().y;

            robot.setLocalTranslation(targetPos);

            Vector3f direction = paintingPos.subtract(targetPos);
            direction.y = 0;
            direction.normalizeLocal();

            float angle = FastMath.atan2(direction.x, direction.z);
            robot.setLocalRotation(new Quaternion().fromAngleAxis(angle, Vector3f.UNIT_Y));
        }
    }

    private void updateInfoBubblePosition(com.jme3.renderer.Camera cam) {
        if (bubbleDisplayTime > 0 && robot != null) {
            Vector3f robotPos = robot.getWorldTranslation();
            BoundingBox bbox = (BoundingBox) robot.getWorldBound();
            float robotHeight = bbox.getYExtent() * 2;

            Vector3f bubblePos = new Vector3f(
                    robotPos.x,
                    robotPos.y + robotHeight + 1.2f,
                    robotPos.z
            );

            infoBubbleNode.setLocalTranslation(bubblePos);
            infoBubbleNode.lookAt(cam.getLocation(), Vector3f.UNIT_Y);
            infoBubbleNode.setCullHint(Spatial.CullHint.Never);
        } else {
            if (infoBubbleNode != null) {
                infoBubbleNode.setCullHint(Spatial.CullHint.Always);
            }
        }
    }

}
