package com.virtualvisit.scene;


import com.jme3.app.SimpleApplication;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Quad;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.renderer.RenderManager;
import com.jme3.system.AppSettings;

/**
 * Application Musée 3D - Visite virtuelle immersive
 * Inspirée du Louvre avec architecture néoclassique
 *
 * Fonctionnalités:
 * - Navigation libre dans le musée
 * - Éclairage réaliste (lumière naturelle + spots)
 * - Architecture détaillée (sol marbre, murs pierre, colonnes)
 * - Œuvres d'art interactives
 * - Animations et effets visuels
 *
 * Contrôles:
 * - ZQSD/Flèches : Déplacement
 * - Souris : Rotation de la caméra
 * - ESPACE : Activer/Désactiver animations
 * - I : Afficher informations
 * - ESC : Quitter
 */
public class JmeApp extends SimpleApplication implements ActionListener {

    // Nodes pour organisation de la scène
    private Node museumNode;
    private Node artworksNode;
    private Node decorationsNode;

    // Éléments animés
    private Geometry floatingSculpture;
    private float animationTime = 0f;
    private boolean animationsEnabled = true;

    // Paramètres du musée
    private static final float ROOM_WIDTH = 20f;
    private static final float ROOM_HEIGHT = 6f;
    private static final float ROOM_DEPTH = 20f;

    public static void main(String[] args) {
        JmeApp app = new JmeApp();

        // Configuration de la fenêtre
        AppSettings settings = new AppSettings(true);
        settings.setTitle("Musée 3D - Louvre Virtual Tour");
        settings.setResolution(1280, 720);
        settings.setVSync(true);
        settings.setSamples(4); // Anti-aliasing

        app.setSettings(settings);
        app.setShowSettings(false); // Pas de dialogue au démarrage
        app.start();
    }

    @Override
    public void simpleInitApp() {
        System.out.println("════════════════════════════════════════");
        System.out.println("   🏛️  MUSÉE 3D - LOUVRE VIRTUAL TOUR");
        System.out.println("════════════════════════════════════════");
        System.out.println("Initialisation...");

        // 1. Initialisation des nodes
        initializeNodes();

        // 2. Configuration caméra
        setupCamera();

        // 3. Configuration éclairage
        setupLighting();

        // 4. Construction du musée
        buildMuseum();

        // 5. Ajout des œuvres d'art
        addArtworks();

        // 6. Éléments décoratifs
        addDecorations();

        // 7. Configuration des contrôles
        setupControls();

        System.out.println("✅ Initialisation terminée!");
        System.out.println("\n📋 CONTRÔLES:");
        System.out.println("   ZQSD/Flèches : Déplacement");
        System.out.println("   Souris : Rotation caméra");
        System.out.println("   ESPACE : Toggle animations");
        System.out.println("   I : Informations");
        System.out.println("════════════════════════════════════════\n");
    }

    /**
     * Initialisation des nodes de la scène
     */
    private void initializeNodes() {
        museumNode = new Node("MuseumNode");
        artworksNode = new Node("ArtworksNode");
        decorationsNode = new Node("DecorationsNode");

        rootNode.attachChild(museumNode);
        rootNode.attachChild(artworksNode);
        rootNode.attachChild(decorationsNode);
    }

    /**
     * Configuration de la caméra
     * Position initiale au centre de la galerie
     */
    private void setupCamera() {
        // Position: au centre, hauteur œil (1.7m), légèrement en retrait
        cam.setLocation(new Vector3f(0, 1.7f, 12));
        cam.lookAtDirection(new Vector3f(0, 0, -1), Vector3f.UNIT_Y);

        // Configuration FlyCam (navigation libre)
        flyCam.setMoveSpeed(8f);
        flyCam.setEnabled(true);
    }

    /**
     * Configuration de l'éclairage
     * Simule l'éclairage naturel d'un musée (lumière du jour)
     */
    private void setupLighting() {
        // Lumière ambiante RÉDUITE (pour créer des ombres)
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(new ColorRGBA(0.3f, 0.3f, 0.28f, 1.0f)); // Plus sombre
        rootNode.addLight(ambient);

        // Lumière directionnelle principale (soleil par fenêtres) - PLUS FORTE
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.5f, -1.0f, -0.3f).normalizeLocal());
        sun.setColor(new ColorRGBA(0.9f, 0.88f, 0.82f, 1.0f)); // Légèrement réduit
        rootNode.addLight(sun);

        // Lumière secondaire RÉDUITE (pour contraste)
        DirectionalLight fill = new DirectionalLight();
        fill.setDirection(new Vector3f(0.8f, -0.3f, 0.5f).normalizeLocal());
        fill.setColor(new ColorRGBA(0.2f, 0.2f, 0.18f, 1.0f)); // Beaucoup plus faible
        rootNode.addLight(fill);
    }

    /**
     * Construction de l'architecture du musée
     */
    private void buildMuseum() {
        createFloor();
        createWalls();
        createCeiling();
        createColumns();
        createMouldings();
    }

    /**
     * Sol en marbre blanc avec reflets
     */
    private void createFloor() {
        Quad floorQuad = new Quad(ROOM_WIDTH, ROOM_DEPTH);
        Geometry floor = new Geometry("Floor", floorQuad);

        Material floorMat = new Material(assetManager,
                "Common/MatDefs/Light/Lighting.j3md");

        // Marbre blanc brillant
        floorMat.setColor("Diffuse", new ColorRGBA(0.95f, 0.95f, 0.92f, 1.0f));
        floorMat.setColor("Ambient", new ColorRGBA(0.9f, 0.9f, 0.88f, 1.0f));
        floorMat.setColor("Specular", new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));
        floorMat.setFloat("Shininess", 64f); // Très brillant

        floor.setMaterial(floorMat);
        floor.rotate((float) -Math.PI / 2, 0, 0);
        floor.setLocalTranslation(-ROOM_WIDTH/2, 0, ROOM_DEPTH/2);

        museumNode.attachChild(floor);

        // Motif central (rosace)
        createFloorRosace();
    }

    /**
     * Rosace décorative au centre du sol
     */
    private void createFloorRosace() {
        Cylinder rosace = new Cylinder(32, 32, 2f, 0.05f, true);
        Geometry rosaceGeom = new Geometry("Rosace", rosace);

        Material rosaceMat = new Material(assetManager,
                "Common/MatDefs/Light/Lighting.j3md");
        rosaceMat.setColor("Diffuse", new ColorRGBA(0.6f, 0.5f, 0.4f, 1.0f));
        rosaceMat.setColor("Ambient", new ColorRGBA(0.5f, 0.4f, 0.3f, 1.0f));

        rosaceGeom.setMaterial(rosaceMat);
        rosaceGeom.rotate((float) Math.PI / 2, 0, 0);
        rosaceGeom.setLocalTranslation(0, 0.06f, 0);

        museumNode.attachChild(rosaceGeom);
    }

    /**
     * Murs en pierre calcaire
     */
    private void createWalls() {
        Material wallMat = new Material(assetManager,
                "Common/MatDefs/Light/Lighting.j3md");

        // Pierre calcaire beige
        wallMat.setColor("Diffuse", new ColorRGBA(0.92f, 0.88f, 0.82f, 1.0f));
        wallMat.setColor("Ambient", new ColorRGBA(0.85f, 0.82f, 0.78f, 1.0f));
        wallMat.setColor("Specular", new ColorRGBA(0.15f, 0.15f, 0.15f, 1.0f));
        wallMat.setFloat("Shininess", 12f);

        // Mur gauche
        Box leftWallBox = new Box(0.3f, ROOM_HEIGHT/2, ROOM_DEPTH/2);
        Geometry leftWall = new Geometry("LeftWall", leftWallBox);
        leftWall.setMaterial(wallMat);
        leftWall.setLocalTranslation(-ROOM_WIDTH/2, ROOM_HEIGHT/2, 0);
        museumNode.attachChild(leftWall);

        // Mur droit
        Geometry rightWall = new Geometry("RightWall", leftWallBox);
        rightWall.setMaterial(wallMat);
        rightWall.setLocalTranslation(ROOM_WIDTH/2, ROOM_HEIGHT/2, 0);
        museumNode.attachChild(rightWall);

        // Mur arrière
        Box backWallBox = new Box(ROOM_WIDTH/2, ROOM_HEIGHT/2, 0.3f);
        Geometry backWall = new Geometry("BackWall", backWallBox);
        backWall.setMaterial(wallMat);
        backWall.setLocalTranslation(0, ROOM_HEIGHT/2, -ROOM_DEPTH/2);
        museumNode.attachChild(backWall);
    }

    /**
     * Plafond voûté
     */
    private void createCeiling() {
        Quad ceilingQuad = new Quad(ROOM_WIDTH, ROOM_DEPTH);
        Geometry ceiling = new Geometry("Ceiling", ceilingQuad);

        Material ceilingMat = new Material(assetManager,
                "Common/MatDefs/Light/Lighting.j3md");

        // Plâtre blanc cassé
        ceilingMat.setColor("Diffuse", new ColorRGBA(0.98f, 0.96f, 0.94f, 1.0f));
        ceilingMat.setColor("Ambient", new ColorRGBA(0.95f, 0.93f, 0.91f, 1.0f));

        ceiling.setMaterial(ceilingMat);
        ceiling.rotate((float) Math.PI / 2, 0, 0);
        ceiling.setLocalTranslation(-ROOM_WIDTH/2, ROOM_HEIGHT, -ROOM_DEPTH/2);

        museumNode.attachChild(ceiling);
    }

    /**
     * Colonnes corinthiennes
     */
    private void createColumns() {
        Material columnMat = new Material(assetManager,
                "Common/MatDefs/Light/Lighting.j3md");

        // Marbre crème
        columnMat.setColor("Diffuse", ColorRGBA.White);
        columnMat.setColor("Ambient", new ColorRGBA(0.95f, 0.95f, 0.93f, 1.0f));
        columnMat.setColor("Specular", new ColorRGBA(0.3f, 0.3f, 0.3f, 1.0f));
        columnMat.setFloat("Shininess", 32f);

        float columnRadius = 0.4f;
        float columnHeight = ROOM_HEIGHT - 0.5f;

        // Positions des colonnes
        float[] columnPositions = {-7f, -3.5f, 3.5f, 7f};

        for (float xPos : columnPositions) {
            // Colonne gauche
            createColumn(columnMat, xPos, -8f, columnRadius, columnHeight);
            // Colonne droite
            createColumn(columnMat, xPos, 8f, columnRadius, columnHeight);
        }
    }

    /**
     * Création d'une colonne individuelle
     */
    private void createColumn(Material mat, float x, float z, float r, float h) {
        Cylinder column = new Cylinder(16, 16, r, h, true);
        Geometry columnGeom = new Geometry("Column", column);
        columnGeom.setMaterial(mat);
        columnGeom.setLocalTranslation(x, h/2, z);

        museumNode.attachChild(columnGeom);

        // Chapiteau (sommet de colonne)
        Cylinder capital = new Cylinder(16, 16, r * 1.3f, 0.3f, true);
        Geometry capitalGeom = new Geometry("Capital", capital);
        capitalGeom.setMaterial(mat);
        capitalGeom.setLocalTranslation(x, h - 0.15f, z);

        museumNode.attachChild(capitalGeom);
    }

    /**
     * Moulures décoratives
     */
    private void createMouldings() {
        Material mouldingMat = new Material(assetManager,
                "Common/MatDefs/Light/Lighting.j3md");
        mouldingMat.setColor("Diffuse", ColorRGBA.White);
        mouldingMat.setColor("Ambient", new ColorRGBA(0.98f, 0.98f, 0.96f, 1.0f));

        // Corniche haute
        Box cornice = new Box(ROOM_WIDTH/2 - 0.5f, 0.2f, 0.2f);
        Geometry corniceBack = new Geometry("CorniceBack", cornice);
        corniceBack.setMaterial(mouldingMat);
        corniceBack.setLocalTranslation(0, ROOM_HEIGHT - 0.3f, -ROOM_DEPTH/2 + 0.5f);

        museumNode.attachChild(corniceBack);
    }

    /**
     * Ajout des œuvres d'art
     */
    private void addArtworks() {
        // Tableaux sur les murs
        createPaintingFrame(-8, 2.5f, -9.7f, 2f, 3f, "La Joconde");
        createPaintingFrame(0, 2.5f, -9.7f, 3f, 2f, "Les Noces de Cana");
        createPaintingFrame(8, 2.5f, -9.7f, 2f, 3f, "La Liberté guidant le peuple");

        // Sculptures sur piédestaux
        createSculpture(-6, 0.5f, 0, "Vénus de Milo");
        createSculpture(6, 0.5f, 0, "La Victoire de Samothrace");
    }

    /**
     * Création d'un cadre de tableau
     */
    private void createPaintingFrame(float x, float y, float z,
                                     float w, float h, String title) {
        // Cadre doré
        Material frameMat = new Material(assetManager,
                "Common/MatDefs/Light/Lighting.j3md");
        frameMat.setColor("Diffuse", new ColorRGBA(0.83f, 0.69f, 0.22f, 1.0f));
        frameMat.setColor("Ambient", new ColorRGBA(0.7f, 0.58f, 0.18f, 1.0f));
        frameMat.setColor("Specular", new ColorRGBA(1.0f, 0.9f, 0.5f, 1.0f));
        frameMat.setFloat("Shininess", 96f);

        float frameThickness = 0.15f;

        // Cadre extérieur
        Box frame = new Box(w/2 + frameThickness, h/2 + frameThickness, 0.08f);
        Geometry frameGeom = new Geometry("Frame_" + title, frame);
        frameGeom.setMaterial(frameMat);
        frameGeom.setLocalTranslation(x, y, z);

        artworksNode.attachChild(frameGeom);

        // Toile (rectangle coloré)
        Material canvasMat = new Material(assetManager,
                "Common/MatDefs/Light/Lighting.j3md");

        // Couleur selon le titre (simulation)
        if (title.contains("Joconde")) {
            canvasMat.setColor("Diffuse", new ColorRGBA(0.4f, 0.35f, 0.25f, 1.0f));
        } else if (title.contains("Noces")) {
            canvasMat.setColor("Diffuse", new ColorRGBA(0.5f, 0.4f, 0.35f, 1.0f));
        } else {
            canvasMat.setColor("Diffuse", new ColorRGBA(0.35f, 0.4f, 0.45f, 1.0f));
        }

        Box canvas = new Box(w/2, h/2, 0.02f);
        Geometry canvasGeom = new Geometry("Canvas_" + title, canvas);
        canvasGeom.setMaterial(canvasMat);
        canvasGeom.setLocalTranslation(x, y, z + 0.1f);

        artworksNode.attachChild(canvasGeom);
    }

    /**
     * Création d'une sculpture sur piédestal
     */
    private void createSculpture(float x, float y, float z, String name) {
        // Piédestal en marbre
        Material pedestalMat = new Material(assetManager,
                "Common/MatDefs/Light/Lighting.j3md");
        pedestalMat.setColor("Diffuse", new ColorRGBA(0.85f, 0.85f, 0.83f, 1.0f));
        pedestalMat.setColor("Ambient", new ColorRGBA(0.8f, 0.8f, 0.78f, 1.0f));

        Box pedestal = new Box(0.6f, 1f, 0.6f);
        Geometry pedestalGeom = new Geometry("Pedestal_" + name, pedestal);
        pedestalGeom.setMaterial(pedestalMat);
        pedestalGeom.setLocalTranslation(x, y, z);

        artworksNode.attachChild(pedestalGeom);

        // Sculpture (sphère simplifiée)
        Material sculptureMat = new Material(assetManager,
                "Common/MatDefs/Light/Lighting.j3md");
        sculptureMat.setColor("Diffuse", ColorRGBA.White);
        sculptureMat.setColor("Ambient", new ColorRGBA(0.95f, 0.95f, 0.95f, 1.0f));
        sculptureMat.setColor("Specular", new ColorRGBA(0.4f, 0.4f, 0.4f, 1.0f));
        sculptureMat.setFloat("Shininess", 48f);

        Sphere sculpture = new Sphere(24, 24, 0.5f);
        Geometry sculptureGeom = new Geometry("Sculpture_" + name, sculpture);
        sculptureGeom.setMaterial(sculptureMat);
        sculptureGeom.setLocalTranslation(x, y * 2 + 0.5f, z);

        artworksNode.attachChild(sculptureGeom);
    }

    /**
     * Éléments décoratifs et sculpture animée
     */
    private void addDecorations() {
        // Sculpture moderne flottante (centre de la galerie)
        Sphere sphere = new Sphere(32, 32, 0.6f);
        floatingSculpture = new Geometry("FloatingSculpture", sphere);

        Material goldMat = new Material(assetManager,
                "Common/MatDefs/Light/Lighting.j3md");

        // Or brillant
        goldMat.setColor("Diffuse", new ColorRGBA(0.83f, 0.69f, 0.22f, 1.0f));
        goldMat.setColor("Ambient", new ColorRGBA(0.7f, 0.58f, 0.18f, 1.0f));
        goldMat.setColor("Specular", new ColorRGBA(1.0f, 0.95f, 0.6f, 1.0f));
        goldMat.setFloat("Shininess", 128f);

        floatingSculpture.setMaterial(goldMat);
        floatingSculpture.setLocalTranslation(0, 3f, 0);

        decorationsNode.attachChild(floatingSculpture);
    }

    /**
     * Configuration des contrôles clavier
     */
    private void setupControls() {
        inputManager.addMapping("ToggleAnimation", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("ShowInfo", new KeyTrigger(KeyInput.KEY_I));

        inputManager.addListener(this, "ToggleAnimation", "ShowInfo");
    }

    /**
     * Gestion des actions clavier
     */
    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (!isPressed) return;

        if ("ToggleAnimation".equals(name)) {
            animationsEnabled = !animationsEnabled;
            System.out.println("🎨 Animations: " + (animationsEnabled ? "ON" : "OFF"));
        }
        else if ("ShowInfo".equals(name)) {
            printMuseumInfo();
        }
    }

    /**
     * Affichage des informations du musée
     */
    private void printMuseumInfo() {
        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.println("║   📊 INFORMATIONS DU MUSÉE           ║");
        System.out.println("╠══════════════════════════════════════╣");
        System.out.println("║ Dimensions: " + ROOM_WIDTH + "x" + ROOM_DEPTH + "x" + ROOM_HEIGHT + "m       ║");
        System.out.println("║ Œuvres exposées: 6                   ║");
        System.out.println("║ Style: Néoclassique                  ║");
        System.out.println("║ FPS: " + Math.round(timer.getFrameRate()) + "                              ║");
        System.out.println("║ Position caméra: " +
                String.format("%.1f, %.1f, %.1f",
                        cam.getLocation().x,
                        cam.getLocation().y,
                        cam.getLocation().z) + "   ║");
        System.out.println("╚══════════════════════════════════════╝\n");
    }

    /**
     * Boucle de mise à jour (chaque frame)
     */
    @Override
    public void simpleUpdate(float tpf) {
        if (animationsEnabled) {
            animationTime += tpf;
            updateAnimations(tpf);
        }
    }

    /**
     * Mise à jour des animations
     */
    private void updateAnimations(float tpf) {
        // Animation de la sculpture flottante
        if (floatingSculpture != null) {
            // Mouvement vertical sinusoïdal
            float yOffset = (float) Math.sin(animationTime * 1.2f) * 0.4f;
            Vector3f pos = floatingSculpture.getLocalTranslation();
            floatingSculpture.setLocalTranslation(pos.x, 3f + yOffset, pos.z);

            // Rotation lente
            floatingSculpture.rotate(0, tpf * 0.8f, 0);
        }
    }

    @Override
    public void simpleRender(RenderManager rm) {
        // Post-processing si nécessaire
    }
}