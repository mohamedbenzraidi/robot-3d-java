package org.example.scene;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.SpotLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.plugins.gltf.GltfLoader;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
import org.example.robot.RobotManager;

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
        createRobot();
    }

    /**
     * Crée le sol en marbre
     */
    private void createFloor() {
        Box floorBox = new Box(GALLERY_WIDTH / 2, 0.1f, GALLERY_LENGTH / 2);
        Geometry floor = new Geometry("Floor", floorBox);
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.setColor("Diffuse", COLOR_FLOOR);
        mat.setColor("Ambient", COLOR_FLOOR);
        mat.setBoolean("UseMaterialColors", true);

        // Texture optionnelle
        Texture floorTex = assetManager.loadTexture("Textures/marble_floor.png");
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
                -GALLERY_WIDTH / 2, GALLERY_HEIGHT / 2, 0, "Textures/wall_marble.png");

        // Mur droit - Texture brique
        createWall("WallRight", WALL_THICKNESS, GALLERY_HEIGHT, GALLERY_LENGTH,
                GALLERY_WIDTH / 2, GALLERY_HEIGHT / 2, 0, "Textures/wall_marble.png");

        // Mur du fond - Texture plâtre blanc
        createWall("WallBack", GALLERY_WIDTH, GALLERY_HEIGHT, WALL_THICKNESS,
                0, GALLERY_HEIGHT / 2, -GALLERY_LENGTH / 2, "Textures/decore_marble_floor.png");

        // Mur d'entrée - Texture béton
        createWall("WallFront", GALLERY_WIDTH, GALLERY_HEIGHT, WALL_THICKNESS,
                0, GALLERY_HEIGHT / 2, GALLERY_LENGTH / 2, "Textures/decore_marble_floor.png");
    }

    /**
     * Crée un mur AVEC TEXTURE
     */
    private void createWall(String name, float width, float height, float depth,
                            float x, float y, float z, String texturePath) {
        Box wallBox = new Box(width / 2, height / 2, depth / 2);
        Geometry wall = new Geometry(name, wallBox);
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");

        try {
            // Charger la texture PNG
            Texture texture = assetManager.loadTexture(texturePath);
            texture.setWrap(Texture.WrapMode.Repeat); // permet la répétition
            mat.setTexture("DiffuseMap", texture);

            // Ajuster l’échelle de la texture selon la taille du mur
            float textureScale = calculateTextureScale(width, height);

            // Répéter la texture sur la surface du mur
            wallBox.scaleTextureCoordinates(new Vector2f(textureScale, textureScale));

            mat.setBoolean("UseMaterialColors", true);
            mat.setColor("Diffuse", ColorRGBA.White);
            mat.setColor("Ambient", ColorRGBA.Gray);

            System.out.println("Texture chargée: " + texturePath);

        } catch (Exception e) {
            // Si la texture n'est pas trouvée, utiliser une couleur par défaut
            e.printStackTrace();
            System.out.println("Texture non trouvée: " + texturePath + " - Utilisation couleur par défaut");

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
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
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
            Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
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
            Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
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

        String[] paths_left = {"painting1.jpg",
                                "painting2.jpeg",
                                "painting3.jpg",
                                "painting4.jpeg",
                                "painting5.jpeg"
                        };

        // Tableaux mur gauche
        createPaintingWall(-GALLERY_WIDTH / 2 + 0.6f, true, paths_left);

        String[] paths_right = {"painting11.jpeg",
                                "painting7.png",
                                "painting8.jpeg",
                                "painting9.jpg",
                                "painting10.jpg"
                        };

        // Tableaux mur droit
        createPaintingWall(GALLERY_WIDTH / 2 - 0.6f, false, paths_right);

        String[] paths_back = { "painting12.jpeg",
                                "painting13.jpg",
                                "painting14.jpg",
                                "painting15.jpg",
                                "painting16.jpg"
                        };


        // Tableaux mur du fond
        createPaintingBackWall(paths_back);
    }

    /**
     * Crée une série de tableaux sur un mur latéral
     */
    private void createPaintingWall(float x, boolean facingRight, String[] paths) {
        int len = paths.length;

        float spacing = GALLERY_LENGTH / (len + 1);

        for (int i = 0; i < len; i++) {
            float z = -GALLERY_LENGTH / 2 + spacing * (i + 1);
            float height = 5f + (i % 3) * 0.5f;
            float width = 2f + (i % 2) * 0.5f;

            createPainting("Painting_" + (facingRight ? "R" : "L") + "_" + i,
                    x, height, z, width, width * 1.3f, facingRight ? 90f : -90f, paths[i]);
        }
    }

    /**
     * Crée des tableaux sur le mur du fond
     */
    private void createPaintingBackWall(String[] paths) {
        float[] positions = {-12f, -6f, 0f, 6f, 12f};

        for (int i = 0; i < positions.length; i++) {
            createPainting("Painting_Back_" + i,
                    positions[i], 5.5f, -GALLERY_LENGTH / 2 + 0.6f,
                    2.5f, 3f, 0f, paths[i]);
        }
    }

    /**
     * Crée un tableau individuel
     */
    private void createPainting(String name, float x, float y, float z,
                                float width, float height, float rotationY, String path) {
        // Cadre
        Box frame = new Box(width / 2 + 0.1f, height / 2 + 0.1f, 0.05f);
        Geometry frameGeom = new Geometry(name + "_Frame", frame);
        Material frameMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        frameMat.setColor("Diffuse", new ColorRGBA(0.2f, 0.15f, 0.1f, 1f));
        frameMat.setColor("Ambient", new ColorRGBA(0.1f, 0.08f, 0.05f, 1f));
        frameMat.setBoolean("UseMaterialColors", true);
        frameGeom.setMaterial(frameMat);

        // Toile
        Quad canvas = new Quad(width, height);
        Geometry canvasGeom = new Geometry(name + "_Canvas", canvas);
        Material canvasMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");

        // IMPORTANT: Charger votre texture ici
        Texture paintingTex = assetManager.loadTexture("Textures/" + path);
        canvasMat.setTexture("DiffuseMap", paintingTex);

        // Couleur temporaire pour démonstration
        canvasMat.setColor("Diffuse", new ColorRGBA(0.8f, 0.75f, 0.7f, 1f));
        canvasMat.setColor("Ambient", new ColorRGBA(0.6f, 0.55f, 0.5f, 1f));
        canvasMat.setBoolean("UseMaterialColors", true);

        canvasGeom.setMaterial(canvasMat);
        canvasGeom.setLocalTranslation(-width / 2, -height / 2, 0.06f);

        // Assemblage
        Node paintingNode = new Node(name);
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
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
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
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
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

        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
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

    public void createRobot(){
        this.robotManager = new RobotManager(this.app);
        this.robotManager.setRobot(rootNode, new Vector3f(0, 3f, 40f));
        this.robot = this.robotManager.getRobot();
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

}