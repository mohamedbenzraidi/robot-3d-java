package org.example.scene;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;

/**
 * 🎨 Modern Loading Screen with Frosted Glass Effect
 * Clean design with blurred overlay and elegant animations
 */
public class LoadingScreen extends BaseAppState {
    private Node guiNode;
    private BitmapText loadingText;
    private BitmapText progressText;
    private BitmapText tipText;
    private Geometry progressBar;
    private Geometry progressBarBackground;
    private Geometry progressBarGlow;
    private Geometry backgroundImage;
    private Geometry blurOverlay;
    private Node spinnerNode;
    private Geometry[] spinnerDots;
    private float progress = 0f;
    private float animationTime = 0f;
    private int screenWidth;
    private int screenHeight;

    // 🎨 Modern Color Palette
    private static final ColorRGBA COLOR_PRIMARY = new ColorRGBA(0.2f, 0.6f, 0.95f, 1f);
    private static final ColorRGBA COLOR_GLOW = new ColorRGBA(0.3f, 0.7f, 1f, 0.5f);
    private static final ColorRGBA COLOR_BLUR_OVERLAY = new ColorRGBA(1f, 1f, 1f, 0.15f);
    private static final ColorRGBA COLOR_TEXT = new ColorRGBA(1f, 1f, 1f, 1f);
    private static final ColorRGBA COLOR_TEXT_SECONDARY = new ColorRGBA(0.9f, 0.9f, 0.9f, 0.85f);

    // Loading messages
    private static final String[] LOADING_TIPS = {
            "Initializing virtual museum...",
            "Loading 3D models...",
            "Setting up galleries...",
            "Preparing interactive elements...",
            "Optimizing experience...",
            "Almost ready..."
    };
    private int currentTipIndex = 0;
    private float tipTimer = 0f;

    @Override
    protected void initialize(Application app) {
        guiNode = new Node("Loading Gui");
        AssetManager assetManager = app.getAssetManager();

        screenWidth = app.getContext().getSettings().getWidth();
        screenHeight = app.getContext().getSettings().getHeight();

        // ===== 1. BACKGROUND IMAGE (FIT, NO ZOOM) =====
        createBackgroundImage(assetManager);

        // ===== 2. FROSTED GLASS / BLUR OVERLAY =====
        createBlurOverlay(assetManager);

        // ===== 3. ANIMATED SPINNER =====
        createMinimalSpinner(assetManager);

        // ===== 4. PROFESSIONAL TEXT =====
        createProfessionalTexts(assetManager);

        // ===== 5. MODERN PROGRESS BAR =====
        createModernProgressBar(assetManager);
    }

    /**
     * 🖼️ Background image - proper fit without zoom
     */
    private void createBackgroundImage(AssetManager assetManager) {
        try {
            Texture loadingTex = assetManager.loadTexture("Models/loading.jpg");

            // Calculate proper scaling to fit image without zoom
            float imgWidth = loadingTex.getImage().getWidth();
            float imgHeight = loadingTex.getImage().getHeight();
            float screenRatio = (float) screenWidth / screenHeight;
            float imgRatio = imgWidth / imgHeight;

            float finalWidth, finalHeight;
            float offsetX = 0, offsetY = 0;

            if (screenRatio > imgRatio) {
                // Screen is wider - fit to width
                finalWidth = screenWidth;
                finalHeight = screenWidth / imgRatio;
                offsetY = (screenHeight - finalHeight) / 2;
            } else {
                // Screen is taller - fit to height
                finalHeight = screenHeight;
                finalWidth = screenHeight * imgRatio;
                offsetX = (screenWidth - finalWidth) / 2;
            }

            Quad bgQuad = new Quad(finalWidth, finalHeight);
            backgroundImage = new Geometry("BackgroundImage", bgQuad);
            Material bgMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            bgMat.setTexture("ColorMap", loadingTex);
            backgroundImage.setMaterial(bgMat);
            backgroundImage.setLocalTranslation(offsetX, offsetY, -10);
            guiNode.attachChild(backgroundImage);

            System.out.println("✅ Background image loaded properly");
        } catch (Exception e) {
            System.err.println("⚠️ loading.jpg not found, using gradient");
            createGradientBackground(assetManager);
        }
    }

    /**
     * Create gradient background as fallback
     */
    private void createGradientBackground(AssetManager assetManager) {
        Quad bgQuad = new Quad(screenWidth, screenHeight);
        backgroundImage = new Geometry("BackgroundImage", bgQuad);
        Material bgMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        bgMat.setColor("Color", new ColorRGBA(0.1f, 0.12f, 0.15f, 1f));
        backgroundImage.setMaterial(bgMat);
        backgroundImage.setLocalTranslation(0, 0, -10);
        guiNode.attachChild(backgroundImage);
    }

    /**
     * 🌫️ Flowing noise blur overlay (like frosted glass with grain)
     */
    private void createBlurOverlay(AssetManager assetManager) {
        // Create animated noise pattern for blur effect
        int gridSize = 100;
        float cellWidth = (float) screenWidth / gridSize;
        float cellHeight = (float) screenHeight / gridSize;

        // Create noise grid for flowing blur effect
        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                if (FastMath.rand.nextFloat() > 0.7f) { // 30% density
                    float noise = FastMath.rand.nextFloat();
                    float size = cellWidth * (0.5f + noise * 1.5f);

                    Quad noiseQuad = new Quad(size, size);
                    Geometry noisePixel = new Geometry("Noise_" + x + "_" + y, noiseQuad);
                    Material noiseMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");

                    float alpha = 0.02f + noise * 0.06f;
                    noiseMat.setColor("Color", new ColorRGBA(1f, 1f, 1f, alpha));
                    noiseMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

                    noisePixel.setMaterial(noiseMat);
                    noisePixel.setLocalTranslation(
                            x * cellWidth + FastMath.rand.nextFloat() * cellWidth,
                            y * cellHeight + FastMath.rand.nextFloat() * cellHeight,
                            -5 + noise * 0.5f
                    );
                    noisePixel.setUserData("noiseSpeed", 10f + noise * 30f);
                    noisePixel.setUserData("noisePhase", FastMath.rand.nextFloat() * FastMath.TWO_PI);

                    guiNode.attachChild(noisePixel);
                }
            }
        }
    }

    /**
     * ⭕ Animated spinner with golden dots (original spiral style)
     */
    private void createMinimalSpinner(AssetManager assetManager) {
        spinnerNode = new Node("SpinnerNode");
        spinnerNode.setLocalTranslation(screenWidth / 2f, screenHeight / 2f + 80f, 0);

        int numDots = 12;
        float radius = 40f;
        spinnerDots = new Geometry[numDots];

        for (int i = 0; i < numDots; i++) {
            float angle = (float) i / numDots * FastMath.TWO_PI;
            float x = FastMath.cos(angle) * radius;
            float y = FastMath.sin(angle) * radius;

            float dotSize = 6f + (i % 3) * 2f;
            Quad dotQuad = new Quad(dotSize, dotSize);
            spinnerDots[i] = new Geometry("SpinnerDot_" + i, dotQuad);

            Material dotMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            float alpha = 0.3f + (float) i / numDots * 0.7f;
            dotMat.setColor("Color", new ColorRGBA(COLOR_PRIMARY.r, COLOR_PRIMARY.g, COLOR_PRIMARY.b, alpha));
            dotMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
            spinnerDots[i].setMaterial(dotMat);
            spinnerDots[i].setLocalTranslation(x - dotSize / 2, y - dotSize / 2, 1);

            spinnerNode.attachChild(spinnerDots[i]);
        }

        // Center dot
        Quad centerQuad = new Quad(30, 30);
        Geometry center = new Geometry("SpinnerCenter", centerQuad);
        Material centerMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        centerMat.setColor("Color", COLOR_PRIMARY);
        center.setMaterial(centerMat);
        center.setLocalTranslation(-15, -15, 2);
        spinnerNode.attachChild(center);

        guiNode.attachChild(spinnerNode);
    }

    /**
     * 📝 Professional clean text
     */
    private void createProfessionalTexts(AssetManager assetManager) {
        BitmapFont font = assetManager.loadFont("Interface/Fonts/Default.fnt");

        // Main title
        loadingText = new BitmapText(font);
        loadingText.setText("VIRTUAL MUSEUM");
        loadingText.setSize(font.getCharSet().getRenderedSize() * 3f);
        loadingText.setColor(COLOR_TEXT);
        float titleWidth = loadingText.getLineWidth();
        loadingText.setLocalTranslation(
                (screenWidth - titleWidth) / 2,
                screenHeight / 2f + 200f,
                5
        );
        guiNode.attachChild(loadingText);

        // Subtitle / tip
        tipText = new BitmapText(font);
        tipText.setText(LOADING_TIPS[0]);
        tipText.setSize(font.getCharSet().getRenderedSize() * 1.3f);
        tipText.setColor(COLOR_TEXT_SECONDARY);
        centerText(tipText, screenHeight / 2f + 20f);
        guiNode.attachChild(tipText);

        // Percentage with modern styling
        progressText = new BitmapText(font);
        progressText.setText("0%");
        progressText.setSize(font.getCharSet().getRenderedSize() * 2.2f);
        progressText.setColor(COLOR_PRIMARY);
        centerText(progressText, screenHeight / 2f - 85f);
        guiNode.attachChild(progressText);
    }

    /**
     * 📊 Segmented neon-style progress bar with particles
     */
    private void createModernProgressBar(AssetManager assetManager) {
        float barWidth = 500f;
        float segmentWidth = 8f;
        float segmentHeight = 20f;
        float gap = 4f;
        float barY = screenHeight / 2f - 140f;
        float startX = (screenWidth - barWidth) / 2;

        int numSegments = (int)(barWidth / (segmentWidth + gap));

        // Store segments in a node for easy management
        Node segmentsNode = new Node("SegmentsNode");
        segmentsNode.setUserData("numSegments", numSegments);
        segmentsNode.setUserData("segmentWidth", segmentWidth);
        segmentsNode.setUserData("gap", gap);
        segmentsNode.setUserData("startX", startX);
        segmentsNode.setUserData("barY", barY);

        // Create all segments
        for (int i = 0; i < numSegments; i++) {
            float x = startX + i * (segmentWidth + gap);

            // Main segment
            Quad segQuad = new Quad(segmentWidth, segmentHeight);
            Geometry segment = new Geometry("Segment_" + i, segQuad);
            Material segMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            segMat.setColor("Color", new ColorRGBA(1f, 1f, 1f, 0.15f));
            segMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
            segment.setMaterial(segMat);
            segment.setLocalTranslation(x, barY, 3);
            segment.setUserData("index", i);
            segment.setUserData("active", false);
            guiNode.attachChild(segment);

            // Glow layer for active segments
            Quad glowQuad = new Quad(segmentWidth + 4, segmentHeight + 6);
            Geometry glow = new Geometry("Glow_" + i, glowQuad);
            Material glowMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            glowMat.setColor("Color", new ColorRGBA(COLOR_PRIMARY.r, COLOR_PRIMARY.g, COLOR_PRIMARY.b, 0f));
            glowMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
            glow.setMaterial(glowMat);
            glow.setLocalTranslation(x - 2, barY - 3, 2.5f);
            glow.setUserData("index", i);
            guiNode.attachChild(glow);
        }

        // Dummy geometries for compatibility
        progressBar = new Geometry("ProgressBarDummy", new Quad(1, 1));
        progressBarGlow = new Geometry("ProgressBarGlowDummy", new Quad(1, 1));
        progressBarBackground = new Geometry("ProgressBarBGDummy", new Quad(1, 1));
    }

    private void centerText(BitmapText text, float y) {
        float textWidth = text.getLineWidth();
        text.setLocalTranslation((screenWidth - textWidth) / 2, y, 5);
    }

    @Override
    protected void cleanup(Application app) {
        guiNode.detachAllChildren();
    }

    @Override
    protected void onEnable() {
        if (getApplication() instanceof SimpleApplication) {
            ((SimpleApplication) getApplication()).getGuiNode().attachChild(guiNode);
        }
    }

    @Override
    protected void onDisable() {
        guiNode.removeFromParent();
    }

    @Override
    public void update(float tpf) {
        animationTime += tpf;

        // ===== Flowing noise animation =====
        for (int i = 0; i < guiNode.getChildren().size(); i++) {
            if (guiNode.getChild(i).getName() != null &&
                    guiNode.getChild(i).getName().startsWith("Noise_")) {
                Geometry noise = (Geometry) guiNode.getChild(i);
                Float speed = noise.getUserData("noiseSpeed");
                Float phase = noise.getUserData("noisePhase");

                if (speed != null && phase != null) {
                    float wave = FastMath.sin(animationTime * speed + phase) * 0.5f + 0.5f;
                    Material mat = noise.getMaterial();
                    ColorRGBA color = mat.getParamValue("Color");
                    mat.setColor("Color", new ColorRGBA(1f, 1f, 1f, color.a * wave));
                }
            }
        }

        // ===== Spinner ALWAYS rotating (continuous animation) =====
        if (spinnerNode != null) {
            spinnerNode.rotate(0, 0, -tpf * 2f); // Continuous rotation

            // Pulsation of dots (always animating)
            for (int i = 0; i < spinnerDots.length; i++) {
                float pulse = FastMath.sin(animationTime * 3f + i * 0.5f) * 0.3f + 0.7f;
                Material mat = spinnerDots[i].getMaterial();
                ColorRGBA color = new ColorRGBA(COLOR_PRIMARY.r, COLOR_PRIMARY.g, COLOR_PRIMARY.b, pulse);
                mat.setColor("Color", color);
            }
        }

        // ===== Segmented progress bar animation =====
        updateSegmentedProgressBar(tpf);

        // ===== Percentage update with color transition =====
        int percentage = (int)(progress * 100);
        progressText.setText(percentage + "%");

        // Color changes as progress increases
        float colorProgress = progress;
        ColorRGBA percentColor = new ColorRGBA(
                COLOR_PRIMARY.r * (1 - colorProgress * 0.3f),
                COLOR_PRIMARY.g * (1 + colorProgress * 0.2f),
                COLOR_PRIMARY.b,
                1f
        );
        progressText.setColor(percentColor);
        centerText(progressText, screenHeight / 2f - 85f);

        // ===== Tips animation (every 2.5 seconds) =====
        tipTimer += tpf;
        if (tipTimer > 2.5f) {
            tipTimer = 0f;
            currentTipIndex = (currentTipIndex + 1) % LOADING_TIPS.length;
            tipText.setText(LOADING_TIPS[currentTipIndex]);
            centerText(tipText, screenHeight / 2f + 20f);
        }

        // ===== Subtle title pulse =====
        float titlePulse = FastMath.sin(animationTime * 0.8f) * 0.05f + 0.95f;
        loadingText.setColor(new ColorRGBA(
                COLOR_TEXT.r * titlePulse,
                COLOR_TEXT.g * titlePulse,
                COLOR_TEXT.b * titlePulse,
                1f
        ));
    }

    /**
     * Update segmented progress bar with wave animation
     */
    private void updateSegmentedProgressBar(float tpf) {
        int totalSegments = 0;

        // Count total segments
        for (int i = 0; i < guiNode.getChildren().size(); i++) {
            if (guiNode.getChild(i).getName() != null &&
                    guiNode.getChild(i).getName().startsWith("Segment_")) {
                totalSegments++;
            }
        }

        if (totalSegments == 0) return;

        int activeSegments = (int)(progress * totalSegments);

        // Update each segment
        for (int i = 0; i < guiNode.getChildren().size(); i++) {
            if (guiNode.getChild(i).getName() != null &&
                    guiNode.getChild(i).getName().startsWith("Segment_")) {
                Geometry segment = (Geometry) guiNode.getChild(i);
                Integer index = segment.getUserData("index");

                if (index != null) {
                    boolean shouldBeActive = index < activeSegments;
                    Boolean wasActive = segment.getUserData("active");

                    // Wave animation on active segments
                    float wave = FastMath.sin(animationTime * 4f - index * 0.3f) * 0.3f + 0.7f;

                    if (shouldBeActive) {
                        // Active segment - full color with wave
                        Material mat = segment.getMaterial();
                        float intensity = 0.7f + wave * 0.3f;
                        mat.setColor("Color", new ColorRGBA(
                                COLOR_PRIMARY.r * intensity,
                                COLOR_PRIMARY.g * intensity,
                                COLOR_PRIMARY.b * intensity,
                                1f
                        ));

                        // Update glow
                        Geometry glow = (Geometry) guiNode.getChild("Glow_" + index);
                        if (glow != null) {
                            Material glowMat = glow.getMaterial();
                            float glowIntensity = wave * 0.4f;
                            glowMat.setColor("Color", new ColorRGBA(
                                    COLOR_PRIMARY.r,
                                    COLOR_PRIMARY.g,
                                    COLOR_PRIMARY.b,
                                    glowIntensity
                            ));
                        }

                        segment.setUserData("active", true);
                    } else {
                        // Inactive segment
                        Material mat = segment.getMaterial();
                        mat.setColor("Color", new ColorRGBA(1f, 1f, 1f, 0.15f));

                        // Turn off glow
                        Geometry glow = (Geometry) guiNode.getChild("Glow_" + index);
                        if (glow != null) {
                            Material glowMat = glow.getMaterial();
                            glowMat.setColor("Color", new ColorRGBA(
                                    COLOR_PRIMARY.r,
                                    COLOR_PRIMARY.g,
                                    COLOR_PRIMARY.b,
                                    0f
                            ));
                        }

                        segment.setUserData("active", false);
                    }
                }
            }
        }
    }

    public void setProgress(float progress) {
        this.progress = Math.max(0f, Math.min(1f, progress));
    }

    public void setLoadingText(String text) {
        if (tipText != null) {
            tipText.setText(text);
            centerText(tipText, screenHeight / 2f + 20f);
        }
    }
}