# 🏛️ Natural History Museum - Guide du Nouveau Musée

## 🎨 Vue d'Ensemble

Le **Natural History Museum** est un nouveau musée magnifique inspiré par l'image fournie, avec une architecture dramatique et élégante. C'est le **troisième musée** de votre application!

### 🌟 Caractéristiques Principales

- ✨ **Architecture Dramatique** - Inspirée des grands musées classiques
- 🎭 **Deux Salles Majestueuses** - Hall principal + Galerie latérale
- 🗿 **Exposition de Statues 3D** - Utilise les modèles existants
- 🖼️ **Galerie de Peintures** - 10 nouveaux tableaux (painting31-40)
- 🏺 **Vitrines d'Exposition** - Avec des socles en marbre
- 💎 **Grand Escalier Central** - Avec tapis rouge élégant
- 🕯️ **Lustres Dorés** - Éclairage d'ambiance somptueux

---

## 🏗️ Architecture du Musée

### **Hall Principal** (60m x 80m x 18m de hauteur)
```
Caractéristiques:
├── Sol à damier noir et blanc (comme l'image de référence)
├── Tapis rouge central menant au grand escalier
├── Arches gothiques sur les côtés (6 paires)
├── Plafond à grille décorative avec poutres en bois
├── Murs en pierre grise avec texture
└── Lustres dorés suspendus (3 unités)
```

### **Galerie Latérale** (40m x 50m)
```
Caractéristiques:
├── Salle dédiée aux peintures
├── 8 tableaux exposés avec éclairage spot
├── Murs crème élégants
├── Entrée par une arche majestueuse
└── Éclairage focalisé sur chaque œuvre
```

### **Grand Escalier Central**
```
Caractéristiques:
├── 15 marches en pierre
├── Tapis rouge luxueux
├── Rambardes dorées des deux côtés
├── Mène vers une plateforme supérieure
└── Point focal de l'architecture
```

---

## 🗿 Expositions

### **Statues Monumentales**
Trois statues classiques en marbre disposées stratégiquement:
- **Statue Gauche** (-15, 0, -10) - Orientée vers la droite
- **Statue Droite** (15, 0, -10) - Orientée vers la gauche
- **Statue Centrale** (0, 0, -30) - Pièce maîtresse, face à l'entrée

Chaque statue est:
- Placée sur un socle en marbre de 5m²
- Entourée de cordons de velours rouge
- Éclairée par des spots dramatiques
- Modelée à partir de `marble_classical_statue_man_01__3d_printable.glb`

### **Galerie de Peintures**
10 nouvelles peintures (painting31-40):
- 5 sur le mur arrière de la galerie
- 3 sur le mur extérieur
- Cadres dorés ornementés
- Éclairage spot sur chaque tableau
- Clickables pour afficher les informations

### **Vitrines d'Exposition**
4 vitrines en verre disposées dans le hall:
- Socles en marbre élégants
- Dômes de verre transparent
- Positionnées aux quatre coins du hall principal
- Prêtes pour des artefacts futurs

---

## 🎨 Palette de Couleurs

Le musée utilise une palette inspirée de l'image:

| Élément | Couleur | RGB | Usage |
|---------|---------|-----|-------|
| **Murs en Pierre** | Gris chaud | (0.45, 0.42, 0.38) | Murs principaux |
| **Sol Marbre** | Crème | (0.92, 0.90, 0.88) | Carreaux blancs |
| **Tapis Rouge** | Bordeaux | (0.65, 0.15, 0.15) | Escalier et allée |
| **Détails Dorés** | Or | (0.85, 0.75, 0.45) | Cadres, lustres |
| **Bois Foncé** | Wengé | (0.25, 0.18, 0.12) | Poutres du plafond |

---

## 💡 Système d'Éclairage

### **Lumière Ambiante**
- Couleur chaude (0.4, 0.38, 0.35)
- Crée l'atmosphère générale du musée

### **Lumière Directionnelle Principale**
- Direction: légèrement du haut
- Couleur: blanc chaud
- Simule la lumière naturelle du plafond

### **Lustres (3 unités)**
- 6 bras par lustre
- Point lights sur chaque ampoule
- Couleur: jaune doré chaud
- Rayon: 15m

### **Spots sur Statues**
- Éclairage dramatique du haut
- Cône de lumière focalisé (20-40°)
- Met en valeur les sculptures

### **Spots sur Peintures**
- Éclairage précis sur chaque tableau
- Angle optimal pour éviter les reflets
- Cône étroit (15-30°)

---

## 🎮 Comment Jouer

### **Lancer le Musée**

```bash
# Option 1: Maven
mvn clean install
mvn exec:java -Dexec.mainClass="org.example.scene.JmeThApp"

# Option 2: IDE (IntelliJ IDEA)
# Run > Edit Configurations > Main class: org.example.scene.JmeThApp
```

### **Contrôles**
```
Déplacement:
├── W - Avancer
├── S - Reculer
├── A - Gauche
├── D - Droite
├── E - Monter
└── Q - Descendre

Caméra:
├── Souris - Regarder autour
├── Z - Rotation gauche
└── C - Rotation droite

Interaction:
├── Clic gauche - Interagir avec tableaux
└── ESC - Fermer le panneau de chat
```

### **Réticule Intelligent**
- **Blanc** - Mode normal
- **Vert** - Sur un tableau cliquable

---

## 🤖 Fonctionnalités Interactives

### **Robot Guide**
- Se téléporte devant le tableau cliqué
- Marche vers l'œuvre (2 secondes)
- Affiche une bulle d'information
- Animation "Talk" pendant l'explication
- Reste devant le tableau actif

### **Panneau de Chat**
- S'ouvre automatiquement au clic
- Affiche les détails du tableau
- Interface moderne et professionnelle
- Fermeture avec ESC

### **Bulles d'Information**
- Apparaît au-dessus du robot
- Texte blanc sur fond bleu foncé
- Dure 8 secondes
- Billboard effect (toujours face caméra)
- Bordures multicouches élégantes

---

## 🗂️ Structure des Fichiers

```
src/main/java/org/example/scene/
├── ThSceneManager.java       ← 🏛️ NOUVEAU! Gestion du musée
└── JmeThApp.java              ← 🎮 NOUVEAU! Application principale

src/main/resources/
├── Models/
│   ├── marble_classical_statue_man_01__3d_printable.glb  (utilisé)
│   └── robot.glb  (robot guide)
└── Textures/
    ├── painting31.jpg → painting40.jpg  (nouveaux tableaux)
    ├── marble_floor.png  (sol)
    ├── black-wall.jpg  (murs)
    ├── gold_texture.jpg  (décorations)
    └── museum-32.png  (icône app)
```

---

## 📊 Comparaison avec les Autres Musées

| Caractéristique | Louvre (SceneManager) | Met (MetSceneManager) | **Natural History (ThSceneManager)** |
|-----------------|----------------------|----------------------|--------------------------------------|
| **Style** | Français classique | Américain moderne | **Gothique dramatique** |
| **Peintures** | 15 (1-15) | 15 (16-30) | **10 (31-40)** |
| **Dimensions** | 40x80m | 50x70m | **60x80m** |
| **Hauteur** | 12m | 15m | **18m** |
| **Caractéristique unique** | Arches du Louvre | Colonnes corinthiennes | **Grand escalier + Statues 3D** |
| **Sol** | Marbre uni | Marbre avec bordure | **Damier noir et blanc** |
| **Plafond** | Puits de lumière | Coffrage | **Grille avec poutres** |
| **Couleur dominante** | Beige chaud | Crème | **Gris pierre + Or** |

---

## 🎯 Points Forts du Design

### **1. Inspiré de l'Image**
- ✅ Sol à damier (comme l'image)
- ✅ Grand escalier central avec tapis rouge
- ✅ Architecture avec arches
- ✅ Exposition de grandes sculptures
- ✅ Plafond à grille décorative
- ✅ Éclairage par lustres suspendus

### **2. Utilisation des Assets Existants**
- ✅ Statue 3D (`marble_classical_statue_man_01__3d_printable.glb`)
- ✅ Robot guide
- ✅ Textures existantes (marble_floor, black-wall, gold_texture)
- ✅ Nouveaux tableaux (painting31-40)

### **3. Architecture Impressionnante**
- ✅ Plus grand volume (18m de haut)
- ✅ Arches dramatiques (6 paires)
- ✅ Escalier monumental
- ✅ Galerie latérale pour les peintures
- ✅ Socles et vitrines élégants

---

## 🔧 Configuration Technique

### **Dimensions du Musée**
```java
MAIN_HALL_WIDTH = 60f;      // 60 mètres de large
MAIN_HALL_LENGTH = 80f;     // 80 mètres de long
SIDE_ROOM_WIDTH = 40f;      // Galerie: 40m de large
SIDE_ROOM_LENGTH = 50f;     // Galerie: 50m de long
HALL_HEIGHT = 18f;          // 18 mètres de haut!
```

### **Performance**
- Optimisé avec culling
- Textures partagées
- Géométrie batched où possible
- Lumières stratégiquement placées (< 20 total)

### **Compatibilité**
- ✅ Fonctionne avec le même système de database
- ✅ Compatible avec SceneManager et MetSceneManager
- ✅ Utilise les mêmes tableaux painting31-40
- ✅ Même système de robot et bulles

---

## 📝 Base de Données

### **Ajouter les Peintures au Setup SQL**

Ajoutez ces lignes à `setup_database.sql`:

```sql
-- Natural History Museum paintings (painting31-40)
INSERT INTO Paintings (id, title, artist, year, description) VALUES
('painting31', 'The Night Watch', 'Rembrandt van Rijn', 1642, 'Rembrandt''s most famous painting depicts a militia company in Amsterdam. The dramatic use of light and shadow, along with the sense of motion, revolutionized group portraiture.'),
('painting32', 'The Birth of Venus', 'Sandro Botticelli', 1485, 'This iconic Renaissance painting shows the goddess Venus emerging from the sea as a grown woman. It is one of the most recognizable works in Western art.'),
('painting33', 'American Gothic', 'Grant Wood', 1930, 'This painting depicts a farmer and his daughter standing before a house in the Carpenter Gothic style. It has become one of the most iconic images in American art.'),
('painting34', 'The Kiss', 'Gustav Klimt', 1908, 'A symbol of Vienna Secession, this painting shows a couple embracing, their bodies entwined in elaborate robes. The work is Klimt''s most popular masterpiece.'),
('painting35', 'Las Meninas', 'Diego Velázquez', 1656, 'One of the most analyzed works in Western painting, it shows the Spanish royal family with Velázquez himself at his easel. The complex composition plays with perspective and reality.'),
('painting36', 'The School of Athens', 'Raphael', 1511, 'A fresco representing Philosophy, it depicts the greatest mathematicians, philosophers and scientists from classical antiquity gathered together. Plato and Aristotle are at the center.'),
('painting37', 'Nighthawks', 'Edward Hopper', 1942, 'This painting depicts people in a downtown diner late at night. It is Hopper''s most famous work and one of the most recognizable images in American art, conveying urban isolation.'),
('painting38', 'The Creation of Adam', 'Michelangelo', 1512, 'A fresco painting on the ceiling of the Sistine Chapel, it illustrates the Biblical creation narrative where God gives life to Adam. The nearly touching hands have become iconic.'),
('painting39', 'Water Lilies', 'Claude Monet', 1906, 'Part of Monet''s extensive Water Lilies series, this painting captures the play of light on the water of his garden pond at Giverny. It represents the culmination of Impressionist technique.'),
('painting40', 'The Garden of Earthly Delights', 'Hieronymus Bosch', 1510, 'A triptych depicting paradise, the earthly world, and hell. The fantastical imagery and intricate details have made it one of the most analyzed paintings in art history.');
```

---

## 🎉 Fonctionnalités Cool

### **1. Sol à Damier Procédural**
```java
// Génère automatiquement un motif d'échecs
for (int x = 0; x < tilesX; x++) {
    for (int z = 0; z < tilesZ; z++) {
        if ((x + z) % 2 == 0) {
            // Carreau blanc
        } else {
            // Carreau noir
        }
    }
}
```

### **2. Lustres avec Vraies Lumières**
Chaque lustre a:
- 6 bras rotatifs
- 6 ampoules avec PointLight
- Géométrie dorée
- Effet de lueur

### **3. Arches Procédurales**
- Générées algorithmiquement
- 16 segments par arche
- Piliers + capitale dorée
- Positionnement automatique

### **4. Statues Auto-Scaled**
- Détection automatique de la taille du modèle
- Positionnement parfait sur le socle
- Rotation pour meilleure présentation

---

## 🚀 Améliorations Futures

Idées pour étendre le musée:

1. **Salle Supplémentaire**
   - Ajouter une aile droite symétrique
   - Plus de peintures ou artefacts

2. **Animations**
   - Portes qui s'ouvrent
   - Statues qui tournent lentement
   - Effets de particules (poussière de lumière)

3. **Audio**
   - Musique d'ambiance classique
   - Sons de pas sur le marbre
   - Murmures de foule

4. **Interactivité**
   - Zoom sur les peintures
   - Minimap du musée
   - Téléportation entre salles

5. **Modèles 3D**
   - Ajouter plus de statues
   - Squelettes de dinosaures
   - Vases et artefacts dans les vitrines

---

## 💡 Conseils d'Utilisation

### **Meilleurs Points de Vue**
1. **Entrée Principale** (0, 3, 30) - Vue d'ensemble
2. **Devant l'Escalier** (0, 2, 15) - Architecture impressionnante
3. **En Haut de l'Escalier** (0, 7, 5) - Vue plongeante
4. **Galerie de Peintures** (40, 3, 0) - Focus sur l'art

### **Parcours Recommandé**
```
Start → Entrée principale
  ↓
Admirer les statues de chaque côté
  ↓
Monter le grand escalier
  ↓
Vue d'en haut
  ↓
Descendre et aller à la galerie latérale
  ↓
Explorer les 8 peintures
  ↓
Retour au hall principal
```

---

## 🎨 Crédits

**Design Inspiré Par:**
- L'image fournie (hall de musée avec squelettes)
- Natural History Museum (Londres)
- Metropolitan Museum of Art (New York)
- Musée du Louvre (Paris)

**Assets Utilisés:**
- `marble_classical_statue_man_01__3d_printable.glb` - Statue classique
- `robot.glb` - Guide virtuel
- Textures de marbre, pierre, et or
- Peintures painting31-40

**Créé Par:** AI Assistant
**Date:** December 2024
**Version:** 1.0

---

## 📞 Support

Si vous rencontrez des problèmes:

1. **Vérifiez que la base de données est lancée:**
   ```bash
   docker ps
   docker start my-postgres-container
   ```

2. **Ajoutez les peintures 31-40 à la BD:**
   ```bash
   docker exec -i my-postgres-container psql -U user -d tour_3d_db < setup_database.sql
   ```

3. **Vérifiez les assets:**
   - Models/marble_classical_statue_man_01__3d_printable.glb
   - Models/robot.glb
   - Textures/painting31.jpg → painting40.jpg

---

**🏛️ Profitez de votre visite au Natural History Museum! 🎭**

Un musée où l'art rencontre l'histoire dans une architecture époustouflante! ✨

