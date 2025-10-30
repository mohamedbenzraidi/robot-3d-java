package com.virtualvisit.scene;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;

/**
 * SceneManager - Gestionnaire de la scène 3D du musée
 *
 * Responsabilités:
 * - Chargement des modèles 3D (.obj, .j3o, .gltf)
 * - Gestion des animations de la scène
 * - Organisation hiérarchique des objets
 * - Optimisation du rendu
 *
 * Intégration:
 * Ce manager centralise toute la logique de gestion de scène,
 * permettant de séparer la logique métier (JmeApp) de la gestion
 * des objets 3D.
 */
public class SceneManager {

    private AssetManager assetManager;
    private Node rootNode;
    private Node modelsNode;
    private boolean isAnimating = false;
    private float animationTime = 0f;

    // Collection d'objets animés (sculptures, éléments décoratifs)
    private Geometry floatingSphere;

    public SceneManager(AssetManager assetManager, Node rootNode) {
        this.assetManager = assetManager;
        this.rootNode = rootNode;
        this.modelsNode = new Node("ModelsNode");
        rootNode.attachChild(modelsNode);

        initializeModels();
    }

    /**
     * Initialisation des modèles 3D
     * Dans un projet réel, on chargerait des fichiers .obj ou .j3o
     */
    private void initializeModels() {
        // Exemple: Sphère flottante représentant une sculpture moderne
        createFloatingSculpture();

        // Dans un projet réel, on ferait:
        // Spatial model = loadModel("Models/Sculpture/Venus.j3o");
        // modelsNode.attachChild(model);
    }

    /**
     * Création d'une sculpture moderne (sphère dorée flottante)
     * Représente une œuvre d'art contemporaine dans le musée
     */
    private void createFloatingSculpture() {
        Sphere sphere = new Sphere(32, 32, 0.5f);
        floatingSphere = new Geometry("FloatingSculpture", sphere);

        Material sculptMat = new Material(assetManager,
                "Common/MatDefs/Light/Lighting.j3md");

        // Matériau doré brillant (bronze poli)
        sculptMat.setColor("Diffuse", new ColorRGBA(0.72f, 0.52f, 0.04f, 1.0f));
        sculptMat.setColor("Ambient", new ColorRGBA(0.60f, 0.45f, 0.05f, 1.0f));
        sculptMat.setColor("Specular", new ColorRGBA(1.0f, 0.85f, 0.3f, 1.0f));
        sculptMat.setFloat("Shininess", 128f); // Très brillant

        floatingSphere.setMaterial(sculptMat);
        floatingSphere.setLocalTranslation(0, 3f, 5);

        modelsNode.attachChild(floatingSphere);
    }

    /**
     * Chargement d'un modèle 3D externe
     * Formats supportés: .obj, .j3o (format natif JME3), .gltf
     *
     * @param modelPath Chemin relatif dans le dossier Assets/
     * @return Le modèle chargé ou null si erreur
     *
     * Exemple d'utilisation:
     * Spatial venus = loadModel("Models/Sculptures/Venus_de_Milo.j3o");
     */
    public Spatial loadModel(String modelPath) {
        try {
            Spatial model = assetManager.loadModel(modelPath);

            // Configuration post-chargement
            configureLoadedModel(model);

            modelsNode.attachChild(model);
            System.out.println("✅ Modèle chargé: " + modelPath);
            return model;

        } catch (Exception e) {
            System.err.println("❌ Erreur chargement modèle: " + modelPath);
            System.err.println("   Raison: " + e.getMessage());
            return null;
        }
    }

    /**
     * Configuration d'un modèle après chargement
     * - Ajustement de l'échelle
     * - Application de matériaux réalistes
     * - Optimisation du rendu
     */
    private void configureLoadedModel(Spatial model) {
        // Échelle par défaut (ajuster selon le modèle)
        model.setLocalScale(1.0f);

        // Centrage au sol
        model.move(0, 0, 0);

        // Si le modèle est un Node (contient plusieurs géométries)
        if (model instanceof Node) {
            Node modelNode = (Node) model;
            // Appliquer des matériaux réalistes à toutes les géométries
            applyRealisticMaterials(modelNode);
        }
    }

    /**
     * Application de matériaux réalistes à un modèle
     * Parcourt toutes les géométries et améliore leur rendu
     */
    private void applyRealisticMaterials(Node modelNode) {
        for (Spatial child : modelNode.getChildren()) {
            if (child instanceof Geometry) {
                Geometry geom = (Geometry) child;
                Material mat = geom.getMaterial();

                // Si pas de matériau, en créer un
                if (mat == null) {
                    mat = new Material(assetManager,
                            "Common/MatDefs/Light/Lighting.j3md");
                    mat.setColor("Diffuse", ColorRGBA.White);
                    mat.setColor("Ambient", ColorRGBA.LightGray);
                    geom.setMaterial(mat);
                }

                // Améliorer le rendu avec specular
                if (!mat.getParam("Specular").toString().isEmpty()) {
                    mat.setColor("Specular", new ColorRGBA(0.3f, 0.3f, 0.3f, 1.0f));
                    mat.setFloat("Shininess", 32f);
                }
            } else if (child instanceof Node) {
                // Récursif pour les sous-nodes
                applyRealisticMaterials((Node) child);
            }
        }
    }

    /**
     * Chargement multiple de modèles
     * Utile pour charger plusieurs œuvres d'art en une fois
     *
     * @param modelPaths Tableau de chemins de modèles
     */
    public void loadModels(String... modelPaths) {
        for (String path : modelPaths) {
            loadModel(path);
        }
    }

    /**
     * Démarrage des animations de la scène
     * Active les mouvements des éléments décoratifs
     */
    public void startAnimation() {
        isAnimating = true;
        animationTime = 0f;
        System.out.println("🎨 Animations de la scène activées");
    }

    /**
     * Arrêt des animations
     */
    public void stopAnimation() {
        isAnimating = false;
        System.out.println("🎨 Animations de la scène désactivées");
    }

    /**
     * Mise à jour de la scène (appelé chaque frame)
     * Gère les animations: rotation, flottement, etc.
     *
     * @param tpf Time Per Frame (temps écoulé depuis la dernière frame)
     */
    public void update(float tpf) {
        if (isAnimating) {
            animationTime += tpf;

            // Animation de la sphère flottante
            animateFloatingSphere(tpf);

            // Autres animations peuvent être ajoutées ici
            // Exemple: rotation de sculptures, lumières qui changent, etc.
        }
    }

    /**
     * Animation de la sphère flottante
     * Mouvement sinusoïdal vertical + rotation lente
     */
    private void animateFloatingSphere(float tpf) {
        if (floatingSphere != null) {
            // Mouvement vertical (flottement)
            float yOffset = (float) Math.sin(animationTime * 1.5f) * 0.3f;
            Vector3f currentPos = floatingSphere.getLocalTranslation();
            floatingSphere.setLocalTranslation(currentPos.x, 3f + yOffset, currentPos.z);

            // Rotation lente sur l'axe Y
            floatingSphere.rotate(0, tpf * 0.5f, 0);
        }
    }

    /**
     * Suppression d'un modèle de la scène
     *
     * @param modelName Nom du modèle à supprimer
     */
    public void removeModel(String modelName) {
        Spatial model = modelsNode.getChild(modelName);
        if (model != null) {
            modelsNode.detachChild(model);
            System.out.println("🗑️ Modèle supprimé: " + modelName);
        }
    }

    /**
     * Récupération d'un modèle par son nom
     *
     * @param modelName Nom du modèle
     * @return Le modèle ou null si non trouvé
     */
    public Spatial getModel(String modelName) {
        return modelsNode.getChild(modelName);
    }

    /**
     * Réinitialisation de la scène
     * Supprime tous les modèles chargés
     */
    public void reset() {
        modelsNode.detachAllChildren();
        isAnimating = false;
        animationTime = 0f;

        // Recréer les éléments de base
        initializeModels();

        System.out.println("🔄 Scène réinitialisée");
    }

    /**
     * Obtenir le nombre de modèles dans la scène
     */
    public int getModelCount() {
        return modelsNode.getChildren().size();
    }

    /**
     * Afficher des informations de debug sur la scène
     */
    public void printDebugInfo() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("📊 SceneManager Debug Info");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("Nombre de modèles: " + getModelCount());
        System.out.println("Animation active: " + isAnimating);
        System.out.println("Temps d'animation: " + String.format("%.2f", animationTime) + "s");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
}