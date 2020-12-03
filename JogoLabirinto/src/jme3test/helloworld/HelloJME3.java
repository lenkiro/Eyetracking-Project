/*
package jme3test.helloworld;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;

/**
 * This is the Main Class of your Game. You should only do initialization here.
 * Move your Logic into AppStates or Controls
 * @author normenhansen
 *//*
public class HelloJME3 extends SimpleApplication {

    public static void main(String[] args) {
        HelloJME3 app = new HelloJME3();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        Box b = new Box(1, 1, 1);
        Geometry geom = new Geometry("Box", b);

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        geom.setMaterial(mat);

        rootNode.attachChild(geom);
    }

    @Override
    public void simpleUpdate(float tpf) {
        //TODO: add update code
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
}
*/

package jme3test.helloworld;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.Animation;
import com.jme3.animation.LoopMode;
import com.jme3.animation.SpatialTrack;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import static com.jme3.bullet.PhysicsSpace.getPhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.GhostControl;
import com.jme3.bullet.control.PhysicsControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Quad;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import java.io.BufferedWriter;
import java.util.Random;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Example 9 - How to make walls and floors solid.
 * This collision code uses Physics and a custom Action Listener.
 * @author normen, with edits by Zathras
 */
public class HelloJME3 extends SimpleApplication
        implements ActionListener, ScreenController, PhysicsCollisionListener {

    private Spatial terrain;
    //private Spatial ceiling;
    private BulletAppState bulletAppState;
    private RigidBodyControl landscape;
    //private RigidBodyControl ceilingLandscape;
    private CharacterControl player;
    float airTime = 0;
    //private BetterCharacterControl player;
    private Vector3f walkDirection = new Vector3f();
    private boolean left = false, right = false, up = false, down = false;

    //Temporary vectors used on each frame.
    //They here to avoid instanciating new vectors on each frame
    private Vector3f camDir = new Vector3f();
    private Vector3f camLeft = new Vector3f();
    private Nifty nifty;
    private Node ghostControlToHub;
    private Node ghostControlNextLevel;
    private Node ghostControlMainKey;
    private Node collisionNode;
    private int fase = 0;
    private int noMapas;
    private Scanner scan;
    private Scanner config;
    private boolean hubWorld;
    private boolean mainKey = true;
    private Mapa[] mapas;
    private Node[] entrances;
    private BufferedWriter partidaBW;

    public static void main(String[] args) throws FileNotFoundException{
      HelloJME3 app = new HelloJME3();
      app.setPauseOnLostFocus(false);
      app.start();
    }

    public void simpleInitApp() {
        Date date = new Date(); // This object contains the current date value
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        writeOnPartidaTxt(formatter.format(date));
        setUpKeys();
        setUpLight();
        
        //ghostControlToHub = addCubeCollision(30*3,35,30*1);
        
        //ghostControlNextLevel = addCubeCollision(30*3,2,30*1);
        
        
        try {
            File mapFile = new File(System.getProperty("user.dir") + "\\src\\jme3test\\helloworld\\mapa.txt");
            this.scan = new Scanner(mapFile);
            this.noMapas = scan.nextInt();
            this.mapas = new Mapa[noMapas];
            for(int i = 0; i<this.noMapas;i++){
                int colunas = scan.nextInt();
                int linhas = scan.nextInt();
                scan.nextLine();
                this.mapas[i] = new Mapa();
                this.mapas[i].matriz  = new char [linhas][colunas];
                for(int y = 0;y<linhas;y++){
                    String s = scan.nextLine();
                    for(int x = 0; x<colunas;x++){
                        this.mapas[i].matriz[y][x] = s.charAt(x);
                    }
                }
                this.mapas[i].xchave = scan.nextInt();
                this.mapas[i].zchave = scan.nextInt();
                this.mapas[i].xobjetivo = scan.nextInt();
                this.mapas[i].zobjetivo = scan.nextInt();
                int numOfGates = scan.nextInt();
                this.mapas[i].numOfGates = numOfGates;
                this.mapas[i].xkey = new int[numOfGates];
                this.mapas[i].zkey = new int[numOfGates];
                this.mapas[i].xgate = new int[numOfGates];
                this.mapas[i].zgate = new int[numOfGates];
                this.mapas[i].gate = new Node[numOfGates];
                this.mapas[i].gateKey = new Node[numOfGates];
                this.mapas[i].hasGateKey = new boolean[numOfGates];
                for(int g = 0; g < numOfGates; g++){
                    this.mapas[i].xkey[g] = scan.nextInt();
                    this.mapas[i].zkey[g] = scan.nextInt();
                    this.mapas[i].xgate[g] = scan.nextInt();
                    this.mapas[i].zgate[g] = scan.nextInt();
                    this.mapas[i].hasGateKey[g] = false;
                }
                
            }
            
            this.config = new Scanner(new File(System.getProperty("user.dir") + "\\src\\jme3test\\helloworld\\config.txt"));
            config.findInLine("hubWorld=");
            this.hubWorld = config.nextBoolean();
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(HelloJME3.class.getName()).log(Level.SEVERE, null, ex);
        }
        begin();
        if(this.hubWorld) createHubworldSelection();
        else ghostControlNextLevel = addCubeCollision(20*5,2,20*5,"Black");
        
        
        char[][] mazeTest = {{'9','2','1','B'},  //                                     _       _   
                             {'2','8','A','3'},  //0: __ v  2: | <  4:I_^>  6: _I^<  8:I v>  A:  I v< 
                             {'2','4','6','3'},  //                                     _       _
                             {'5','0','0','2'}}; //1: __ ^  3: | >  5:I_v<  7: _Iv>  9:I ^<  B:  I ^>
        char[][] maze2 = {{'0','4','5','0'},
                          {'0','A','B','0'},
                          {'0','0','0','0'},
                          {'1','0','1','0'}};




        
        addCubeBlue(0,0,0);
        addCubeBlue(2,0,0);
        addCubeBlue(4,0,0);
        addCubeRed(0,0,2);
        addCubeRed(0,0,4);
        
        /*
        // Create model
        Box box = new Box(1, 1, 1);
        Geometry geom = new Geometry("box", box);
        geom.setMaterial(assetManager.loadMaterial("Textures/Terrain/BrickWall/BrickWall.j3m"));
        Node model = new Node("model");
        
        model.attachChild(geom);

        Box child = new Box(0.5f, 0.5f, 0.5f);
        Geometry childGeom = new Geometry("box", child);
        childGeom.setMaterial(assetManager.loadMaterial("Textures/Terrain/BrickWall/BrickWall.j3m"));
        Node childModel = new Node("childmodel");
        childModel.setLocalTranslation(2, 2, 2);
        childModel.attachChild(childGeom);
        model.attachChild(childModel);
        
        //animation parameters
        float animTime = 5;
        int fps = 25;
        float totalXLength = 10;
        
        //calculating frames
        int totalFrames = (int) (fps * animTime);
        float dT = animTime / totalFrames, t = 0;
        float dX = totalXLength / totalFrames, x = 0;
        float[] times = new float[totalFrames];
        Vector3f[] translations = new Vector3f[totalFrames];
        Quaternion[] rotations = new Quaternion[totalFrames];
        Vector3f[] scales = new Vector3f[totalFrames];
	for (int i = 0; i < totalFrames; ++i) {
        	times[i] = t;
        	t += dT;
        	translations[i] = new Vector3f(30, x, -30);
        	x += dX;
        	rotations[i] = Quaternion.IDENTITY;
        	scales[i] = Vector3f.UNIT_XYZ;
        }
        SpatialTrack spatialTrack = new SpatialTrack(times, translations, rotations, scales);
        
        //creating the animation
        Animation spatialAnimation = new Animation("anim", animTime);
        spatialAnimation.setTracks(new SpatialTrack[] { spatialTrack });
        
        //create spatial animation control
        AnimControl control = new AnimControl();
        HashMap<String, Animation> animations = new HashMap<String, Animation>();
        animations.put("anim", spatialAnimation);
        control.setAnimations(animations);
        model.addControl(control);
        rootNode.attachChild(model);
        
        //run animation
        control.createChannel().setAnim("anim");
        */
    }
    
    void createHubworldSelection(){
        entrances = new Node [this.noMapas];
        for(int i=0; i<noMapas;i++){
            this.entrances[i] = addCubeCollision(30*i,2,-30*2,"Black");
        }
    }
    
    void begin(){
        /** Set up Physics */
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        //bulletAppState.setDebugEnabled(true);
        
        // We re-use the flyby camera for rotation, while positioning is handled by physics
        viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));


        // We load the scene from the zip file and adjust its size.

        Material terrainMat = new Material(assetManager,
                "Common/MatDefs/Terrain/Terrain.j3md");
        terrainMat.setTexture("Alpha", assetManager.loadTexture(
                "Textures/Terrain/splat/alphamap.png"));
        AbstractHeightMap heightmap = null;
        Texture heightMapImage = assetManager.loadTexture(
                "Textures/Terrain/splat/mountains512.png");
        Texture grass = assetManager.loadTexture(
                "Textures/Terrain/splat/grass.jpg");
        grass.setWrap(WrapMode.Repeat);
        terrainMat.setTexture("Tex1", grass);
        terrainMat.setFloat("Tex1Scale", 64f);
        Texture dirt = assetManager.loadTexture(
                "Textures/Terrain/splat/dirt.jpg");
        dirt.setWrap(WrapMode.Repeat);
        terrainMat.setTexture("Tex2", dirt);
        terrainMat.setFloat("Tex2Scale", 32f);
        Texture rock = assetManager.loadTexture(
                "Textures/Terrain/splat/road.jpg");
        rock.setWrap(WrapMode.Repeat);
        terrainMat.setTexture("Tex3", rock);
        terrainMat.setFloat("Tex3Scale", 128f);
        heightmap = new ImageBasedHeightMap(heightMapImage.getImage());
        terrain = new TerrainQuad("my terrain", 65, 513, heightmap.getHeightMap());
        terrain.setMaterial(terrainMat);

        terrain.setLocalScale(2f);

        // We set up collision detection for the scene by creating a
        // compound collision shape and a static RigidBodyControl with mass zero.
        CollisionShape sceneShape =
                CollisionShapeFactory.createMeshShape(terrain);
        landscape = new RigidBodyControl(sceneShape, 0);
        terrain.addControl(landscape);
        
        /*
        Material ceilingMat = new Material(assetManager,
                "Common/MatDefs/Terrain/Terrain.j3md");
        
        ceilingMat.setFloat("Tex3Scale", 128f);
        ceiling = new TerrainQuad("my ceiling", 65, 513, heightmap.getHeightMap());
        ceiling.setMaterial(terrainMat);

        ceiling.setLocalScale(2f);

        // We set up collision detection for the scene by creating a
        // compound collision shape and a static RigidBodyControl with mass zero.
        CollisionShape ceilingShape =
                CollisionShapeFactory.createMeshShape(ceiling);
        ceilingLandscape = new RigidBodyControl(ceilingShape, 0);
        ceiling.addControl(ceilingLandscape);
        rootNode.attachChild(ceiling);
        bulletAppState.getPhysicsSpace().add(ceilingLandscape);
        */
        
        /**
         * We set up collision detection for the player by creating
         * a capsule collision shape and a CharacterControl.
         * The CharacterControl offers extra settings for
         * size, stepheight, jumping, falling, and gravity.
         * We also put the player in its starting position.
         */
        CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(1.5f, 6f, 1);
        player = new CharacterControl(capsuleShape, 0.05f);
        player.setJumpSpeed(20);
        player.setFallSpeed(30);
        player.setPhysicsLocation(new Vector3f(0, 10, 0));
        player.setUp(new Vector3f(0, 1, 0)); 
        //player = new BetterCharacterControl(1.5f, 6f, 1f); 
        
        // We attach the scene and the player to the rootnode and the physics space,
        // to make them appear in the game world.
        rootNode.attachChild(terrain);
        bulletAppState.getPhysicsSpace().add(landscape);
        bulletAppState.getPhysicsSpace().add(player);

        // You can change the gravity of individual physics objects after they are
        // added to the PhysicsSpace.
        player.setGravity(new Vector3f(0,-30f,0));
        cam.lookAtDirection(new Vector3f(-10f, 0, -20f), new Vector3f(0,90f,0));
    }
    
    void fileMaze(int height, int width){
        if(fase <= noMapas){
            Node world = new Node("world");
            createMaze(mapas[this.fase-1].matriz,30,30,world); 
        }else{
        }
    }

    void resetAll(){
        rootNode.detachAllChildren();
    }

    void randomMaze(int height, int width){
        Random rand = new Random();
        char[] caracteres = {'0','1','2','3','4','5','6','7','8','9','A','B'};
        char[][] maze = new char [6][6];
        for(int i = 0; i<maze.length; i++){
            for(int j = 0; j<maze[i].length; j++){
                maze[i][j] = caracteres[rand.nextInt(caracteres.length)];
            }
        }
        createMaze(maze, height, width, new Node("world"));
    }
  
    
    
    void createMaze(char[][] matriz, int height, int width, Node world){
        for(int i = 0; i<matriz.length; i++){
            for(int j = 0; j<matriz[i].length; j++){
                switch(matriz[i][j]){
                    case '0':
                        addWall(height, width,j,0,i,world,"dh");
                        break;
                    case '1':
                        addWall(height, width,j,0,i,world,"uh");
                        break;
                    case '2':
                        addWall(height, width,j,0,i,world,"lv");
                        break;
                    case '3':
                        addWall(height, width,j,0,i,world,"rv");
                        break;

                    case '4':
                        addHalfWall(height, width,j,0,i,world,"ruh");
                        addHalfWall(height, width,j,0,i,world,"trv");
                        break;

                    case '5':
                        addHalfWall(height, width,j,0,i,world,"rdh");
                        addHalfWall(height, width,j,0,i,world,"tlv");
                        break;

                    case '6':
                        addHalfWall(height, width,j,0,i,world,"luh");
                        addHalfWall(height, width,j,0,i,world,"tlv");
                        break;

                    case '7':
                        addHalfWall(height, width,j,0,i,world,"ldh");
                        addHalfWall(height, width,j,0,i,world,"trv");
                        break;

                    case '8':
                        addHalfWall(height, width,j,0,i,world,"rdh");
                        addHalfWall(height, width,j,0,i,world,"brv");
                        break;
                    case '9':
                        addHalfWall(height, width,j,0,i,world,"ruh");
                        addHalfWall(height, width,j,0,i,world,"blv");
                        break;
                    case 'A':
                        addHalfWall(height, width,j,0,i,world,"ldh");
                        addHalfWall(height, width,j,0,i,world,"blv");
                        break;
                    case 'B':
                        addHalfWall(height, width,j,0,i,world,"luh");
                        addHalfWall(height, width,j,0,i,world,"brv");
                        break;
                }
                
            }
        }


        rootNode.attachChild(world);
    }
    
    void writeOnPartidaTxt(String message){
        
        try{
            File partida = new File(System.getProperty("user.dir") + "\\src\\jme3test\\helloworld\\partida.txt");
            FileWriter fw = new FileWriter(partida, true);
            this.partidaBW = new BufferedWriter(fw);
            partidaBW.write(message);
            partidaBW.newLine();
            partidaBW.close();
        }
        catch(Exception e){
            System.out.println("Oh no");
            System.out.println(e);
        }
    }
    
    void addWall(float height, float width, float moveX, float moveY, float moveZ, Node world, String version){
        Quad quad = new Quad(width,height);
        Geometry walle = new Geometry("Wall-e",quad);
        Material walleMat = new Material(assetManager,"Common/MatDefs/Light/Lighting.j3md");

        Texture image = assetManager.loadTexture("Textures/DuplicatedBrickWall.jpg");

        walleMat.setTexture("DiffuseMap", image);
        walleMat.setBoolean("UseMaterialColors",true);
        walleMat.setColor("Diffuse",ColorRGBA.White);  // minimum material color
        walleMat.setColor("Specular",ColorRGBA.White); // for shininess
        walleMat.setFloat("Shininess", 8f); // [1,128] for shininess
        walleMat.setColor("Ambient",ColorRGBA.White.mult(0.3f));
        walleMat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
        //System.out.println(walleMat.getParams());
        walle.setMaterial(walleMat);
        Node walleNod = new Node("walleNod");
        world.attachChild(walleNod);
        walleNod.attachChild(walle);
        
        
        switch(version){
            case "dh":
                walleNod.move(moveX*width , moveY*height, moveZ*width + width/2);
                walleNod.rotate(0,0,0);
                break;
            case "uh":
                walleNod.move((moveX+1)*width, moveY*height, moveZ*width + width/2);
                walleNod.rotate(0,(float)Math.PI,0);
                break;
            case "lv":
                walleNod.move(moveX*width + width/2, moveY*height, moveZ*width);
                walleNod.rotate(0,3*(float)Math.PI/2,0);
                break;
            case "rv":
                walleNod.move(moveX*width + width/2, moveY*height, (1+moveZ)*width);
                walleNod.rotate(0,(float)Math.PI/2,0);
                break;
            
        }
        


        CollisionShape walleCol = CollisionShapeFactory.createMeshShape(walle);
        RigidBodyControl walleBod = new RigidBodyControl(walleCol, 0);
        walle.addControl(walleBod);
        bulletAppState.getPhysicsSpace().add(walleBod);
    }
    
    void addHalfWall(float height, float width, float moveX, float moveY, float moveZ, Node world, String version){
        Quad quad = new Quad(width/2,height);
        Geometry walle = new Geometry("Wall-e",quad);
        Material walleMat = new Material(assetManager,"Common/MatDefs/Light/Lighting.j3md");

        Texture image = assetManager.loadTexture("Textures/BrickWall.jpg");

        walleMat.setTexture("DiffuseMap", image);
        walleMat.setBoolean("UseMaterialColors",true);
        walleMat.setColor("Diffuse",ColorRGBA.White);  // minimum material color
        walleMat.setColor("Specular",ColorRGBA.White); // for shininess
        walleMat.setFloat("Shininess", 8f); // [1,128] for shininess
        walleMat.setColor("Ambient",ColorRGBA.White.mult(0.3f));
        walleMat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
        //System.out.println(walleMat.getParams());
        walle.setMaterial(walleMat);
        Node walleNod = new Node("walleNod");
        world.attachChild(walleNod);
        walleNod.attachChild(walle);
        
        
        switch(version){
            case "ldh":
                walleNod.move(moveX*width , moveY*height, moveZ*width + width/2);
                walleNod.rotate(0,0,0);
                break;
            case "luh":
                walleNod.move((moveX+1)*width - width/2, moveY*height, moveZ*width + width/2);
                walleNod.rotate(0,(float)Math.PI,0);
                break;
            case "rdh":
                walleNod.move(moveX*width + width/2, moveY*height, moveZ*width + width/2);
                walleNod.rotate(0,0,0);
                break;
            case "ruh":
                walleNod.move((moveX+1)*width, moveY*height, moveZ*width + width/2);
                walleNod.rotate(0,(float)Math.PI,0);
                break;
            case "tlv":
                walleNod.move(moveX*width + width/2, moveY*height, moveZ*width);
                walleNod.rotate(0,3*(float)Math.PI/2,0);
                break;
            case "trv":
                walleNod.move(moveX*width + width/2, moveY*height, (1+moveZ)*width - width/2);
                walleNod.rotate(0,(float)Math.PI/2,0);
                break;    
            case "blv":
                walleNod.move(moveX*width + width/2, moveY*height, moveZ*width + width/2);
                walleNod.rotate(0,3*(float)Math.PI/2,0);
                break;
            case "brv":
                walleNod.move(moveX*width + width/2, moveY*height, (1+moveZ)*width);
                walleNod.rotate(0,(float)Math.PI/2,0);
                break;
        }
        
        CollisionShape walleCol = CollisionShapeFactory.createMeshShape(walle);
        RigidBodyControl walleBod = new RigidBodyControl(walleCol, 0);
        walle.addControl(walleBod);
        bulletAppState.getPhysicsSpace().add(walleBod);
    }
    
    Node addCubeCollision(float x, float y, float z,String color){
        GhostControl ghostControl = new GhostControl(new BoxCollisionShape(new Vector3f(1,1,1)));  // a box-shaped ghost
        
        Node cNode = new Node("cNode");
        // Optional: Add a Geometry, or other controls, to the node if you need to
        
        // attach everything to activate it
        Box b = new Box(1, 1, 1);
        Geometry geom = new Geometry("Box", b);
        
        cNode.attachChild(geom);
        cNode.move(x, y, z);
        cNode.addControl(ghostControl);                         // the ghost follows this node
        
        
        
        CollisionShape wallShape = CollisionShapeFactory.createBoxShape(geom);
        RigidBodyControl thing = new RigidBodyControl(wallShape, 0);
        
        rootNode.attachChild(cNode);
        
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        if(color.equals("Red")) mat.setColor("Color", ColorRGBA.Red);
        else if(color.equals("Blue")) mat.setColor("Color", ColorRGBA.Blue);
        else if(color.equals("Brown")) mat.setColor("Color", ColorRGBA.Brown);
        else mat.setColor("Color", ColorRGBA.Black);
        geom.setMaterial(mat);
        bulletAppState.getPhysicsSpace().add(thing);
        
        getPhysicsSpace().add(ghostControl);
        return cNode;
        
        /*
        */
    }
    
    

void addCubeRed(float x, float y, float z){
    Box b = new Box(1, 20, 1);
    Geometry geom = new Geometry("Box", b);
    Node cNode = new Node("cNode"); 
    cNode.attachChild(geom);
    cNode.move(x, y, z);
        
        
        
    CollisionShape wallShape = CollisionShapeFactory.createBoxShape(geom);
    RigidBodyControl thing = new RigidBodyControl(wallShape, 0);
        
    rootNode.attachChild(cNode);
        
    Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    mat.setColor("Color", ColorRGBA.Red);
    
    geom.setMaterial(mat);
    bulletAppState.getPhysicsSpace().add(thing);
}

void addCubeBlue(float x, float y, float z){
    Box b = new Box(1, 20, 1);
    Geometry geom = new Geometry("Box", b);
    Node cNode = new Node("cNode"); 
    cNode.attachChild(geom);
    cNode.move(x, y, z);
        
        
        
    CollisionShape wallShape = CollisionShapeFactory.createBoxShape(geom);
    RigidBodyControl thing = new RigidBodyControl(wallShape, 0);
        
    rootNode.attachChild(cNode);
        
    Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    mat.setColor("Color", ColorRGBA.Blue);
    geom.setMaterial(mat);
    bulletAppState.getPhysicsSpace().add(thing);
}

  private void setUpLight() {
    // We add light so we see the scene
    AmbientLight al = new AmbientLight();
    al.setColor(ColorRGBA.White.mult(1.3f));
    rootNode.addLight(al);

    DirectionalLight dl = new DirectionalLight();
    dl.setColor(ColorRGBA.White);
    dl.setDirection(new Vector3f(2.8f, -2.8f, -2.8f).normalizeLocal());
    rootNode.addLight(dl);
  }

  /** We over-write some navigational key mappings here, so we can
   * add physics-controlled walking and jumping: */
  private void setUpKeys() {
    inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
    inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
    inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
    inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
    inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
    inputManager.addListener(this, "Left");
    inputManager.addListener(this, "Right");
    inputManager.addListener(this, "Up");
    inputManager.addListener(this, "Down");
    inputManager.addListener(this, "Jump");
  }
  
  Node addGate(float height, float width, float moveX, float moveY, float moveZ, Node world, String version){
        Box quad = new Box(width, height, 1);
        //Could be CSGBox
        Geometry walle = new Geometry("Box", quad);
      
      
        //Quad quad = new Quad(width,height);
        //Geometry walle = new Geometry("Wall-e",quad);
        Material walleMat = new Material(assetManager,"Common/MatDefs/Light/Lighting.j3md");

        Texture image = assetManager.loadTexture("Textures/MetalGate.jpg");

        walleMat.setTexture("DiffuseMap", image);
        walleMat.setBoolean("UseMaterialColors",true);
        walleMat.setColor("Diffuse",ColorRGBA.White);  // minimum material color
        walleMat.setColor("Specular",ColorRGBA.White); // for shininess
        walleMat.setFloat("Shininess", 8f); // [1,128] for shininess
        walleMat.setColor("Ambient",ColorRGBA.White.mult(0.3f));
        walleMat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
        //System.out.println(walleMat.getParams());
        walle.setMaterial(walleMat);
        Node walleNod = new Node("walleNod");
        world.attachChild(walleNod);
        walleNod.attachChild(walle);
        
        
        switch(version){
            case "dh":
                walleNod.move(moveX*width , moveY*height + height, moveZ*width);
                walleNod.rotate(0,0,0);
                break;
            case "uh":
                walleNod.move((moveX+1)*width, moveY*height + height, moveZ*width);
                walleNod.rotate(0,(float)Math.PI,0);
                break;
            case "lv":
                walleNod.move(moveX*width , moveY*height + height, moveZ*width);
                walleNod.rotate(0,3*(float)Math.PI/2,0);
                break;
            case "rv":
                walleNod.move(moveX*width, moveY*height + height, (1+moveZ)*width);
                walleNod.rotate(0,(float)Math.PI/2,0);
                break;
            
        }
        
        GhostControl ghostControl = new GhostControl(new BoxCollisionShape(new Vector3f(width*1.0f,height,1.0f)));
        walleNod.addControl(ghostControl);   
        getPhysicsSpace().add(ghostControl);


        CollisionShape walleCol = CollisionShapeFactory.createMeshShape(walle);
        RigidBodyControl walleBod = new RigidBodyControl(walleCol, 0);
        walle.addControl(walleBod);
        bulletAppState.getPhysicsSpace().add(walleBod);
        return walleNod;
    }
  
  public void createGateAnimation(){
      if(this.fase > 0 && mapas[this.fase-1].gate != null){
            for(int j = 0;j<this.mapas[this.fase-1].numOfGates;j++){
                    float animTime = 5*3;
                    int fps = 25;
                    float totalXLength = 10;

                    //calculating frames
                    int totalFrames = (int) (fps * animTime);
                    float dT = animTime / totalFrames, t = 0;
                    float dX = totalXLength / totalFrames, x = 0;
                    float[] times = new float[totalFrames*3];
                    Vector3f[] translations = new Vector3f[totalFrames*3];
                    Quaternion[] rotations = new Quaternion[totalFrames*3];
                    Vector3f[] scales = new Vector3f[totalFrames*3];
                    for (int i = 0; i < totalFrames*3; ++i) {
                        times[i] = t;
                        t += dT;
                        rotations[i] = Quaternion.IDENTITY;
                        scales[i] = Vector3f.UNIT_XYZ;
                        translations[i] = new Vector3f(mapas[this.fase-1].xgate[j]*10, x+10, mapas[this.fase-1].zgate[j]*10);
                        if(i<totalFrames/3){
                            x += dX*3;
                        }
                        else if(i>totalFrames*2/3){
                            x -= dX*3;
                        }
                    }
                    SpatialTrack spatialTrack = new SpatialTrack(times, translations, rotations, scales);

                    //creating the animation
                    Animation spatialAnimation = new Animation("anim", animTime);
                    spatialAnimation.setTracks(new SpatialTrack[] { spatialTrack });

                    //create spatial animation control
                    AnimControl control = new AnimControl();
                    HashMap<String, Animation> animations = new HashMap<String, Animation>();
                    animations.put("anim", spatialAnimation);
                    control.setAnimations(animations);
                    mapas[this.fase-1].gate[j].addControl(control);
                    //rootNode.attachChild(model);
            }
      }
  }
  

  /** These are our custom actions triggered by key presses.
   * We do not walk yet, we just keep track of the direction the user pressed. */
  public void onAction(String binding, boolean isPressed, float tpf) {
    if (binding.equals("Left")) {
      left = isPressed;
    } else if (binding.equals("Right")) {
      right= isPressed;
    } else if (binding.equals("Up")) {
      up = isPressed;
    } else if (binding.equals("Down")) {
      down = isPressed;
    } else if (binding.equals("Jump")) {
        if (isPressed) {
            //if(player.onGround()){
                player.jump(new Vector3f(0,20f,0));}
          //}
          
    }
  }

  /**
   * This is the main event loop--walking happens here.
   * We check in which direction the player is walking by interpreting
   * the camera direction forward (camDir) and to the side (camLeft).
   * The setWalkDirection() command is what lets a physics-controlled player walk.
   * We also make sure here that the camera moves with player.
   */
  @Override
    public void simpleUpdate(float tpf) {
        camDir.set(cam.getDirection()).multLocal(0.6f);
        camLeft.set(cam.getLeft()).multLocal(0.4f);
        walkDirection.set(0, 0, 0);
        if (left) {
            walkDirection.addLocal(camLeft);
        }
        if (right) {
            walkDirection.addLocal(camLeft.negate());
        }
        if (up) {
            walkDirection.addLocal(camDir.x,0,camDir.z);
        }
        if (down) {
            walkDirection.addLocal(-camDir.x,0,-camDir.z);
        }if (!player.onGround()) {
            airTime = airTime + tpf;
        } else {
            airTime = 0; 
        }
        player.setWalkDirection(walkDirection);
        cam.setLocation(player.getPhysicsLocation());
        writeOnPartidaTxt("Mouse:" + this.inputManager.getCursorPosition().toString() + " L: " + left+ " R: " + right + " U: " + up + " D: " + down + " Player Location: " + player.getPhysicsLocation() + " Camera Direction: " + cam.getDirection());
        if(hubWorld){
            for(int i = 0; i<this.noMapas;i++){
                if(entrances[i] != null){
                    if(entrances[i].getControl(GhostControl.class).getOverlappingCount() == 1){
                        resetAll();
                        begin();
                        Node world = new Node("world");
                        Mapa mapaAtual = this.mapas[i];
                        createMaze(mapaAtual.matriz,20,20,world);
                        ghostControlToHub = addCubeCollision(20*mapaAtual.xobjetivo,2,20*mapaAtual.zobjetivo,"Black");
                        ghostControlMainKey = addCubeCollision(20*mapaAtual.xchave,2,20*mapaAtual.zchave,"Brown");
                        this.mainKey = false;
                        this.fase = i + 1;
                        for(int j = 0; j<mapaAtual.numOfGates;j++){
                            mapas[this.fase-1].gate[j] = addGate(10, 10, mapaAtual.xgate[j], 0, mapaAtual.zgate[j], world, "dh");
                            mapas[this.fase-1].gateKey[j] = addCubeCollision(10*mapaAtual.xkey[j], 2 , 10*mapaAtual.zkey[j], "Red");
                        }
                        createGateAnimation();
                    }
                }
            }
        }
        if(this.fase > 0 && mapas[this.fase-1].gate != null){
            for(int j = 0;j<this.mapas[this.fase-1].numOfGates;j++){
                System.out.println(mapas[this.fase-1].gate[j].getControl(GhostControl.class).getOverlappingCount());
                if(mapas[this.fase-1].gateKey[j].getControl(GhostControl.class).getOverlappingCount() == 1){
                    mapas[this.fase-1].hasGateKey[j] = true;
                    mapas[this.fase-1].gateKey[j].removeFromParent();
                }
                if(mapas[this.fase-1].gate[j].getControl(GhostControl.class).getOverlappingCount() == 3 && mapas[this.fase-1].hasGateKey[j]){
                    if(mapas[this.fase-1].gate[j].getControl(AnimControl.class).getNumChannels() < 1){
                        //run animation
                        AnimChannel anime = mapas[this.fase-1].gate[j].getControl(AnimControl.class).createChannel();
                        anime.setAnim("anim");
                        anime.setLoopMode(LoopMode.DontLoop);
                    }
                    
                    else{
                        AnimChannel aniChannel = mapas[this.fase-1].gate[j].getControl(AnimControl.class).getChannel(0);
                        if(aniChannel.getTime() == aniChannel.getAnimMaxTime()){
                            aniChannel.setAnim("anim");
                        }
                        
                    }
                    

                    

                }
        }
        if(ghostControlMainKey != null)
            if(ghostControlMainKey.getControl(GhostControl.class).getOverlappingCount() == 1){
                this.mainKey = true;
                ghostControlMainKey.removeFromParent();
            }
        if(ghostControlToHub != null)
            if(ghostControlToHub.getControl(GhostControl.class).getOverlappingCount() == 1 && this.mainKey == true){
                resetAll();
                this.fase = 0;
                begin();
                createHubworldSelection();
                //ghostControlNextLevel = addCubeCollision(20*5,2,20*5);
                //ghostControlToHub = addCubeCollision(20*5,35,20*5);
            }
        if(ghostControlNextLevel != null)
            if(ghostControlNextLevel.getControl(GhostControl.class).getOverlappingCount() == 1 && this.mainKey){
                resetAll();
                begin();
                //ghostControlToHub = addCubeCollision(20*5,35,20*5);
                this.fase++;
                fileMaze(20,20);
                if(this.fase < this.noMapas)
                ghostControlMainKey = addCubeCollision(20*mapas[this.fase -1].xchave,2,20*mapas[this.fase -1].zchave,"Brown");
                ghostControlNextLevel = addCubeCollision(20*mapas[this.fase -1].xobjetivo,2,20*mapas[this.fase-1].zobjetivo,"Black");
                this.mainKey = false;
            }
        }
    }
    
    private PhysicsSpace getPhysicsSpace(){
        return bulletAppState.getPhysicsSpace();
    }
    
    @Override
    public void bind(Nifty nifty, Screen screen) {
        System.out.println("bind( " + screen.getScreenId() + ")");
    }

    @Override
    public void onStartScreen() {
        System.out.println("onStartScreen");
    }

    @Override
    public void onEndScreen() {
        System.out.println("onEndScreen");
    }

    public void quit(){
        nifty.gotoScreen("end");
    }

    @Override
    public void collision(PhysicsCollisionEvent event) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
}

class Mapa{
    char[][] matriz;
    int xchave,zchave,xobjetivo,zobjetivo;
    int numOfGates,xkey[], zkey[], xgate[], zgate[];
    boolean hasGateKey[];
    Node gate[], gateKey[];
    public Mapa(){
    }
}

/*
Fontes:
    Imagens: https://i.pinimg.com/originals/76/99/ba/7699ba4de8e66f222c848105c6ccfa1f.jpg
*/
/*
13/03
Estudar Nifty por fora
Montar troca de mundos na mão
Se não conseguir fazer if detectar colisão, fazer detectar posição no mundo
24/03
Fazer o código receber labirintos por arquivos txt
Fazer txt de configuração (definir se, após uma fase, volta ao 'menu')
Resolver problema dos cubos vermelhos e da luz
Implementar movimentação mais realista
04/06
Fazer mapas mais complexos com o esquema de chave para liberar saída
Ver como capturar todos os inputs de teclas e mouse
30/07
Fazer com que coordenadas da câmera e teclas pressionadas sejam gravadas num txt "partida"
27/08
Entender as informações da câmera e colocar posição do personagem e informações do mouse no partida.txt
01/10
Fazer animação para porta e ter múltiplas portas
19/11
Fazer teto, semáforo e colisão seguir porta
*/