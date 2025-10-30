package com.monprojet.app;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Quad;
import com.jme3.math.Vector3f;

/**
* JmeApp : Le point d'entrée de notre application 3D
*
* PRINCIPE : Cette classe initialise le moteur 3D et configure
* la scène de base (cube + sol)
  */
  public class JmeApp extends SimpleApplication {

  // ============================================
  // MÉTHODE PRINCIPALE - Point de départ du programme
  // ============================================
  public static void main(String[] args) {
  JmeApp app = new JmeApp();
  app.start(); // Lance la boucle de jeu (60 FPS par défaut)
  }

  // ============================================
  // simpleInitApp() - Appelée UNE SEULE FOIS au démarrage
  // ============================================
  /**
    * LOGIQUE : Tout ce que vous voulez créer au début de votre
    * application va ici (objets 3D, lumières, caméra, etc.)
      */
      @Override
      public void simpleInitApp() {
      // 1. CRÉER LE SOL
      creerSol();

      // 2. CRÉER UN CUBE
      creerCube();

      // 3. CONFIGURER LA CAMÉRA
      configurerCamera();

      System.out.println("✅ Scène 3D initialisée avec succès !");
      }

  // ============================================
  // MÉTHODE : Créer le sol
  // ============================================
  /**
    * PRINCIPE DES OBJETS 3D :
    * 1. Forme (Shape) → définit la géométrie
    * 2. Géométrie (Geometry) → combine forme + position
    * 3. Matériau (Material) → définit la couleur/texture
    * 4. Attacher au rootNode → rendre visible dans la scène
         */
         private void creerSol() {
         // Quad = un rectangle plat (parfait pour un sol)
         // Paramètres : largeur (10), hauteur (10)
         Quad solShape = new Quad(10, 10);

    // Geometry = l'objet 3D final avec un nom
    Geometry sol = new Geometry("Sol", solShape);

    // Material = l'apparence visuelle
    Material matSol = new Material(assetManager,
    "Common/MatDefs/Misc/Unshaded.j3md"); // Shader simple sans lumière
    matSol.setColor("Color", ColorRGBA.Green); // Couleur verte

    sol.setMaterial(matSol);

    // ROTATION : Le Quad est créé verticalement, on le tourne à plat
    // rotate(angleX, angleY, angleZ) en radians
    sol.rotate(-1.57f, 0, 0); // -90° en X (PI/2 ≈ 1.57)

    // POSITION : Centrer le sol sous le cube
    sol.setLocalTranslation(-5, 0, 5); // (x, y, z)

    // ATTACHER À LA SCÈNE (sinon invisible !)
    rootNode.attachChild(sol);
    }

  // ============================================
  // MÉTHODE : Créer un cube
  // ============================================
  private void creerCube() {
  // Box = un cube (ou parallélépipède)
  // Paramètres : demi-largeur, demi-hauteur, demi-profondeur
  Box cubeShape = new Box(1, 1, 1); // Cube de 2x2x2

       Geometry cube = new Geometry("MonCube", cubeShape);
       
       // Matériau bleu
       Material matCube = new Material(assetManager, 
           "Common/MatDefs/Misc/Unshaded.j3md");
       matCube.setColor("Color", ColorRGBA.Blue);
       
       cube.setMaterial(matCube);
       
       // Position : 2 unités au-dessus du sol
       cube.setLocalTranslation(0, 2, 0);
       
       rootNode.attachChild(cube);
  }

  // ============================================
  // MÉTHODE : Configurer la caméra
  // ============================================
  /**
    * PRINCIPE DE LA CAMÉRA :
    * - La caméra est comme vos yeux dans le monde 3D
    * - Par défaut, elle est à (0, 0, 10) et regarde vers (0, 0, 0)
        */
        private void configurerCamera() {
        cam.setLocation(new Vector3f(0, 5, 15)); // Position (x, y, z)
        cam.lookAt(new Vector3f(0, 2, 0), Vector3f.UNIT_Y); // Regarder le cube

    // Désactiver le contrôle par défaut (mouvement avec souris)
    flyCam.setMoveSpeed(10); // Vitesse si activé
    }

  // ============================================
  // simpleUpdate() - Appelée à CHAQUE FRAME (~60x/seconde)
  // ============================================
  /**
    * LOGIQUE : Mettre ici tout ce qui doit évoluer dans le temps
    * (animations, rotations, détection de collision, etc.)
    *
    * @param tpf = Time Per Frame (temps écoulé depuis la dernière frame)
      */
      @Override
      public void simpleUpdate(float tpf) {
      // Pour l'instant, on laisse vide
      // On ajoutera des animations plus tard
      }
      }