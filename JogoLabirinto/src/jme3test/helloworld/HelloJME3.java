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
import java.util.Random;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
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
    private BulletAppState bulletAppState;
    private RigidBodyControl landscape;
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
    private int fase;
    private int noMapas;
    private Scanner scan;
    private Scanner config;
    private boolean hubWorld;
    private boolean mainKey = true;
    private Mapa[] mapas;
    private Node[] entrances;

    public static void main(String[] args) throws FileNotFoundException{
      HelloJME3 app = new HelloJME3();
      app.setPauseOnLostFocus(false);
      app.start();
    }

    public void simpleInitApp() {
        
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
                this.mapas[i].ychave = scan.nextInt();
                this.mapas[i].xobjetivo = scan.nextInt();
                this.mapas[i].yobjetivo = scan.nextInt();
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
        flyCam.setMoveSpeed(100);


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
    }
    
    void fileMaze(int height, int width){
        this.fase++;
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
      if (isPressed && player.onGround()) { player.jump(new Vector3f(0,20f,0));}
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
            walkDirection.addLocal(camDir);
        }
        if (down) {
            walkDirection.addLocal(camDir.negate());
        }if (!player.onGround()) {
            airTime = airTime + tpf;
        } else {
            airTime = 0;
        }
        player.setWalkDirection(walkDirection);
        cam.setLocation(player.getPhysicsLocation());
        System.out.println(mainKey);
        if(hubWorld){
            for(int i = 0; i<this.noMapas;i++){
                if(entrances[i] != null){
                    if(entrances[i].getControl(GhostControl.class).getOverlappingCount() == 1){
                        resetAll();
                        begin();
                        Node world = new Node("world");
                        createMaze(this.mapas[i].matriz,20,20,world);
                        ghostControlToHub = addCubeCollision(20*mapas[i].xobjetivo,2,20*mapas[i].yobjetivo,"Black");
                        ghostControlMainKey = addCubeCollision(20*mapas[i].xchave,2,20*mapas[i].ychave,"Brown");
                        this.mainKey = false;
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
                fileMaze(20,20);
                if(this.fase < this.noMapas)
                ghostControlMainKey = addCubeCollision(20*mapas[this.fase -1].xchave,2,20*mapas[this.fase -1].ychave,"Brown");
                ghostControlNextLevel = addCubeCollision(20*mapas[this.fase -1].xobjetivo,2,20*mapas[this.fase-1].yobjetivo,"Black");
                this.mainKey = false;
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
    int xchave,ychave,xobjetivo,yobjetivo;
    public Mapa(){
    }
}


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
*/