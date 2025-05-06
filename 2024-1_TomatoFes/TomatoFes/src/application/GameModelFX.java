package application;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.ProgressBar;
import javafx.util.Duration;

class GameModelFX {
    public ArrayList<Projectile> projectiles; // 投擲物のリスト
    public ArrayList<Enemies> enemies; // 敵のリスト
    public Enemies castle;//城は変数で扱う.
    public double projectilesreload[]; //投擲可か判定
    
    public String CurrentProjetile;//選択されている投擲物

    public Player player;// プレイヤーの初期位置, HP
    public String CurrentEnermies;//今出したい敵.String

    public double TimeLimit;//タイムリミット
    
    public boolean isDamaged = false; //ダメージを受けたかどうか
    public int damageBlinkCounter = 0;
    
    private long lastTomatoTime = 0; // 前回トマトが発射された時刻をナノ秒で保存
    
    //タイムライン用
    private Timeline timeline;
    private Timeline timeline2;
    
    public boolean isGameOver = false;//ゲームオーバーか判定
    public boolean isGameClear = false;//ゲームクリア, 城.HP == 0のときtrue.
    public boolean isGamePause = false;//一時停止か判定
    
    public ArrayList<Dataclass> filedata;//読み込んだファイルデータ保存

    //コンストラクタ
    public GameModelFX(String path_to_castle, String path_to_stage) {
        projectiles = new ArrayList<>();	//初期化
        enemies = new ArrayList<>();		//初期化
        
        //projectilesreloadの初期化
        projectilesreload = new double[10];
        for (int i = 0; i < 10; i++) {
            projectilesreload[i]=0;
        }
        
        CurrentProjetile = "Tomato";		//初期化
        CurrentEnermies = "Pig";			//初期化

        player = new Player(500, 830, 100);	//初期化
        player.setId(0);					//初期化
        
        TimeLimit = 61.0;					//初期化
        
        //以下ファイル読み込み
        filedata = new ArrayList<>();//これにファイルデータ入れる
        fileread(path_to_castle, path_to_stage, filedata);//ファイルデータ読み込み. ステージ情報取得
        
        //ファイル取得できたか確認
        //System.out.println("城: "+castle.getPlayerX()+","+castle.getPlayerY()+","+castle.getPlayerHP());
        for (Iterator<Dataclass> it = filedata.iterator(); it.hasNext(); ) {
            Dataclass p = it.next();
            //System.out.println(p.getX()+","+p.getY()+","+p.getKind_Enemy()+","+p.getEnemy_t());
        }
        
        //１秒おきに実行する必要のあるもののループ
        timeline = new Timeline(new KeyFrame(Duration.millis(1000), e -> {
        	if(isGamePause == false) {
        		if(TimeLimit >= 0) {
            		TimeLimit -= 1.0;   //タイムリミットを減らす
            	}
        	}
        	if(isGameOver == true) {
        		timeline.stop();        //ゲームオーバーなら終了
        	}
        	if(isGameClear == true) {
        		timeline.stop();        //ゲームクリアなら終了
        	}
        }));
        timeline.setCycleCount(Timeline.INDEFINITE); // 無限に繰り返す
        timeline.play(); // Timelineを開始
        
        //1/240秒おきに実行する必要のあるもののループ
        //座標計算などはここのループ
        timeline2 = new Timeline(new KeyFrame(Duration.millis(1000/240), e -> {
        	if(isGamePause == false) {
        		updateProjectiles();    //敵と投擲物に関する情報の更新
                updateProjectiles_enemy(System.nanoTime()); //ステージ情報を元に敵を排出
        	}
        	if(isGameOver == true) {
        		timeline2.stop();   //ゲームオーバーなら終了
        	}
        }));
        timeline2.setCycleCount(Timeline.INDEFINITE); // 無限に繰り返す
        timeline2.play(); // Timelineを開始
        
    }
    
  //ファイル読み込み関数.dataclassに情報入れる
    public void fileread(String path_castle, String path_stage, ArrayList<Dataclass> dataclass) {
    	try {
            //城の情報
        	File f2;
        	f2 = new File(path_castle); 
            BufferedReader br2 = new BufferedReader(new FileReader(f2));//ファイルを開く
            
            String line;
            
            if((line = br2.readLine()) != null) {   //1行ずつ読み込み
            	String[] data = line.split(",", 0); // 行をカンマ区切りで配列に変換
                //各種情報取得
            	double x = Double.parseDouble(data[0]); double y = Double.parseDouble(data[1]);
            	double hp = Double.parseDouble(data[2]);
            	
            	castle = new Castle(x,y);
            	
            	//ここで敵配列に追加
            	enemies.add(castle);
            }br2.close();//ファイルを閉じる
            
            //敵の情報
            File f1;
        	f1 = new File(path_stage);
            BufferedReader br1 = new BufferedReader(new FileReader(f1));//ファイルを開く

            double x,y; String kind_enemy;
            double enemy_t;
            while ((line = br1.readLine()) != null) {   //1行ずつ読み込み
            	String[] data = line.split(",", 0); // 行をカンマ区切りで配列に変換
                //各種情報取得
            	 x = Double.parseDouble(data[0]); y = Double.parseDouble(data[1]);
            	 kind_enemy = data[2];
            	 enemy_t = Double.parseDouble(data[3]);
            	filedata.add(new Dataclass(x, y, kind_enemy, enemy_t));
            }
            br1.close();//ファイルを閉じる
       
          } catch (IOException e) {
            System.out.println(e);
          }
    }
    
    //ポイントをファイルに書き出す（ゲーム終了時）
    public void filewrite() {
        try {
            FileWriter file = new FileWriter("stagedata/setPointdata.txt", true);
            PrintWriter pw = new PrintWriter(new BufferedWriter(file));//ファイルを開く
            pw.println(Integer.toString(player.getPlayerPoint()));//ファイルに上書き保存
            pw.close();//ファイルを閉じる
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //set関数群
    public void setPlayerX(double x) {
    	player.setPlayerX(x);
    }
    public void setPlayerY(double y) {
    	player.setPlayerY(y);
    }
    public void setCurrentPj(String name) {
    	CurrentProjetile = name;
    }
    
    public void setreloadtime(int num, double x){
    	projectilesreload[num]=x;
    }
    
    
    
    //get関数群
    public double getPlayerX() {
        return player.getPlayerX();
    }
    public double getPlayerY() {
        return player.getPlayerY();
    }
    public double getPlayerHP() {
    	return player.getPlayerHP();
    }
    public ArrayList<Projectile> getProjectiles() {
        return projectiles;
    }
    public ArrayList<Enemies> getEnemies() {
        return enemies;
    }
    public Enemies getCastle() {
    	return castle;
    }
    public int getsizeofprojectiles() {
    	return projectiles.size();
    }
    //投擲物の残りリロードタイムを返す
    public double[] getReloadTime(){
        return projectilesreload;
    }
    public double getTimeLimit() {
    	return TimeLimit;
    }
    
    

    //選択されている投擲物を変更
    public void ChangeCurrentProjectile(String name){
        CurrentProjetile=name;
    }

    //現在出される敵を変更
    public void ChangeCurrentEnemies(String enemies){
        CurrentEnermies=enemies;
    }

    // 投擲物を追加
    public void addProjectile(double x, double y, double theta, int shooterId) {
    	Projectile newProjectile = ProjectileFactory.createProjectile(CurrentProjetile, x, y, theta, shooterId);
    	if(projectilesreload[newProjectile.getID()]<=0) {
    		projectiles.add(newProjectile);	//投擲物追加
    		projectilesreload[newProjectile.getID()]=newProjectile.getreloadtime();
    	}
    }
    //トマト追加専用
    public void addProjectile_Tomato(double x, double y, double theta, int shooterId) {
    	Projectile newProjectile = ProjectileFactory.createProjectile("Tomato", x, y, theta, shooterId);
        projectiles.add(newProjectile);
    }
    //オレンジ追加専用
    public void addProjectile_Orange(double x, double y, double theta, int shooterId) {
    	Projectile newProjectile = ProjectileFactory.createProjectile("Orange", x, y, theta, shooterId);
        projectiles.add(newProjectile);
    }
    

    //敵を追加
    public void addEnemies(double x, double y) {
    	Enemies newEnemy = EnemiesFactory.createEnemies(CurrentEnermies, x, y);
        enemies.add(newEnemy);
    }
    //豚追加専用
    public void addEnemies_Pig(double x, double y) {
    	Enemies newEnemy = EnemiesFactory.createEnemies("Pig", x, y);
        enemies.add(newEnemy);
    }
    //城追加専用
    public void addEnemies_Castle(double x, double y) {
    	Enemies newEnemy = EnemiesFactory.createEnemies("Castle", x, y);
        enemies.add(newEnemy);
    }

    //キャラ同士の接触判定
    public int DotouchEtoP(Enemies it1){
        if(onCollisionCircle(it1.getPlayerX(),it1.getPlayerY(),it1.getR(),player.getPlayerX(),player.getPlayerY(),player.getR())==1){
            return 1;
        }
        return 0;
    }

    //投擲物と敵の接触判定
    public int isTouch_PjtoE(Projectile it2, Enemies p1){
    	int far;
    	switch (p1.getClass().getSimpleName()) {
        case "Pig":
            far = 22500;
            break;
        case "Castle": 
        	far = 100000;
        	break;
        case "Caterpillar": 
        	far = 22500;
        	break;
        case "Butterfly": 
        	far = 22500;
        	break;
        default:
        	far = 22500;
        	break;
    	}
    	
    	if(it2.getShooterId() != 0) {
    		return 0;
    	}
        if(onCollisionCircle(it2.getX(),it2.getY(),it2.getR(),p1.getPlayerX(),p1.getPlayerY(),p1.getR())==1){
           return 1;
        }
        return 0;
    }
    
    
    //プレイヤーと投擲物
    public int DotouchPtoP(Projectile it2){
    	if(it2.getInvincible() > 0.0) {
    		return 0;
    	}
    	else if(onCollisionCircle(it2.getX(),it2.getY(),it2.getR(),player.getPlayerX(),player.getPlayerY(),player.getR())==1){
        	return 1;
        }
        return 0;
    }
    
    //投擲物同士の当たり判定
    public int isTouch_PjtoPj(Projectile it1, Projectile it2){
    	if(it1 == it2) {
    		return 0;
    	}
    	else if(onCollisionCircle(it1.getX(),it1.getY(),it1.getR(),it2.getX(),it2.getY(),it2.getR())==1){
    		if(it1.getShooterId() == 0 && it2.getShooterId() != 0) {
    			player.setaddPlayerPoint(1);
    		}
        	return 1;
        }
        return 0;
    }
    
    //一般的な当たり判定。x1,y1,x2,y2,r1,r2で計算.当たれば１そうでなければ０
    public int onCollisionCircle(double x1,double y1,double r1,double x2,double y2,double r2) {
    	if(Math.pow(x1-x2, 2.0)+Math.pow(y1-y2, 2.0) < Math.pow(r1+r2, 2.0)) {
    		return 1;
    	}
    	return 0;
    }

    //投擲物の位置と敵を更新するメソッド(上のループで1/240秒ごとに呼び出される)
    public void updateProjectiles() {
    	//敵に関する処理のループ
        for (Iterator<Enemies> it1 = enemies.iterator(); it1.hasNext(); ) {
            Enemies p1 = it1.next();
            
            //プレイヤーに向かって動く
            double p1X = p1.getPlayerX();
            double p1V = p1.getSpeed();
            if(p1X < player.getPlayerX()-400 && p1V > 0) {  //敵の位置と速度の正負で判定
            	p1.setSpeed(-p1.getSpeed()); // 敵を左に動かす
            }
            else if(p1X > player.getPlayerX()+400 && p1V < 0) { //敵の位置と速度の正負で判定
            	p1.setSpeed(-p1.getSpeed()); // 敵を右に動かす
            }
            p1X -= p1.getSpeed();
            p1.setPlayerX(p1X);
            
            //プレイヤーとの当たり判定を確認してプレイヤーのHPを削るメソッドを実行。さらにHPがセロになるとゲームオーバーに遷移
            if(DotouchEtoP(p1)==1){
            	double playerHP = player.getPlayerHP();
                playerHP -= p1.getDamage()/240;//プレイヤーHPを削る
                player.setPlayerHP(playerHP);

              //以下画像点滅関連
                isDamaged = true;
                damageBlinkCounter = 0;
                
            }
            
            //敵と投擲物
            for (Iterator<Projectile> it2 = projectiles.iterator(); it2.hasNext(); ) {
                Projectile p2 = it2.next();
              //敵と投擲物の当たり判定と処理
                if(isTouch_PjtoE(p2, p1)==1){
                	Appmanager.startSE_hit();
                	
                	double playerHP = p1.getPlayerHP();
                	playerHP -= p2.getDamage(); //プレイヤーHPを削る
                    if(playerHP <= 0) { //HP０以下なら削除
                    	enemies.remove(p1);
                    	removeEnemy(p1);
                    	//ポイントゲット
                    	player.setaddPlayerPoint(p1.getPlayerPoint());
                    	if("Castle".equals(p1.getClass().getSimpleName()) == true) {//倒されたのは城かどうかの判定
                    		filewrite();    //ゲーム終了時にポイントをファイルに書き出す
                    		isGameClear = true;
                    	}
                    }else {
                    	p1.setPlayerHP(playerHP);
                    }
                    projectiles.remove(p2);
                }
            }     
            //敵のHPbarをセット
            p1.setHPBar();
        }
    	
        
        //投擲物に関する処理のループ
        for (Iterator<Projectile> it2 = projectiles.iterator(); it2.hasNext(); ) {
            Projectile p2 = it2.next();
            p2.setsubInvincible(1.0);
            
            double p2X = p2.getX();
            p2X += (int)(p2.getV()*Math.cos(p2.getTheta())); // 投擲物を右に動かす
            p2.setX(p2X);
            
            double p2Y = p2.getY();
            p2Y -= (int)(p2.getV()*Math.sin(p2.getTheta()) - (9.8/240)*p2.getT() - 0);// 投擲物を上に動かす
            p2.setY(p2Y);
            p2.setaddT(1.0);

            //×画面⚪︎ステージ外に出たら削除
            if (p2.getX() > 8000||p2.getY() > 1000) {
                it2.remove(); 
            }
                        
            //投擲物とプレイヤーの当たり判定と処理
            if(DotouchPtoP(p2) == 1) {
            	Appmanager.startSE_hit();
            	
            	double playerHP = player.getPlayerHP();
                playerHP -= p2.getDamage();
                player.setPlayerHP(playerHP);   //プレイヤーHPを削る
                projectiles.remove(p2);//投擲物削除
                
                //以下画像点滅関連
                isDamaged = true;
                damageBlinkCounter = 0;
            }
            
            //投擲物同士が当たった場合
            for (Iterator<Projectile> it3 = projectiles.iterator(); it3.hasNext(); ) {
                Projectile p3 = it3.next();
                if(isTouch_PjtoPj(p2, p3) == 1) {
                	//投擲物削除
                	projectiles.remove(p2);
                	projectiles.remove(p3);
                }
            }
        }
        
        //ゲームオーバーか判定
        if(player.getPlayerHP() <= 0 || getTimeLimit() <= 0) {
        	filewrite();
        	isGameOver = true;
        }
        
        //リロードタイムを減らす。
        for(int i=0;i<10;++i) {
        	if(projectilesreload[i]>=0.0) {
        		projectilesreload[i]-=1.0/240.0;
        		if(projectilesreload[i]<0) {
        			projectilesreload[i] = 0;
        		}
        	}
        }
    }
    
    //敵を排出する関数
    public void updateProjectiles_enemy(long now) { // 現在の時間（ナノ秒）を引数として受け取る

        if (lastTomatoTime == 0 || (now - lastTomatoTime) >= 1_000_000_000L) {
            //自然に投げられる投擲物
        	addProjectile_Tomato(3000, 0, 400.0*3.14/360.0, -1);
            addProjectile_Tomato(4000, 0, 400.0*3.14/360.0, -1);
            addProjectile_Tomato(5000, 0, 400.0*3.14/360.0, -1);
            addProjectile_Tomato(2000, 0, 350.0*3.14/360.0, -1);
            addProjectile_Tomato(2000, 0, -300.0*3.14/360.0, -1);

            //現在の経過時間と比較して敵を出す
            for (Iterator<Dataclass> it = filedata.iterator(); it.hasNext(); ) {
                Dataclass p = it.next();
                if(60.0 - TimeLimit == p.getEnemy_t()) {
                	ChangeCurrentEnemies(p.getKind_Enemy());
                	addEnemies(p.getX(), p.getY()); //敵追加
                	it.remove();
                }
            }
            
            //敵が投擲物を投げる
            for (Iterator<Enemies> it1 = enemies.iterator(); it1.hasNext(); ) {
                Enemies p1 = it1.next();
                int shooterId = p1.getId();

                double r = Math.random();
                double r2 = Math.random();
                if(r > 0.4) {
                	addProjectile_Tomato(p1.getPlayerX(), p1.getPlayerY()-100, 300.0*3.14/360.0, shooterId);
                }
                if(r2> 0.2) {
                	addProjectile_Tomato(p1.getPlayerX(), p1.getPlayerY(), 350.0*3.14/360.0, shooterId);
                }
                
                if("Castle".equals(p1.getClass().getSimpleName())) {
                	addProjectile_Tomato(p1.getPlayerX(), p1.getPlayerY()-200, -20, shooterId);
                    addProjectile_Tomato(p1.getPlayerX(), p1.getPlayerY()-400, -25, shooterId);
                }
            }
            
            lastTomatoTime = now;
        }
        else if (lastTomatoTime == 0 ||(now - lastTomatoTime) == 0_500_000_000L) {
        }
    }

	//以下敵の削除を通知するobserver
    private List<GameModelObserver> observers = new ArrayList<>();

    public void addObserver(GameModelObserver observer) {
        observers.add(observer);
    }
    
    public void removeObserver(GameModelObserver observer) {
        observers.remove(observer);
    }

    private void notifyEnemyRemoved(Enemies enemy) {
        for (GameModelObserver observer : observers) {
            observer.onEnemyRemoved(enemy);
        }
    }

    // 敵を削除するメソッド内で、敵が削除されたときにオブザーバーに通知
    public void removeEnemy(Enemies enemy) {
        enemies.remove(enemy);
        notifyEnemyRemoved(enemy);
    }
}

//importしたファイルの情報を格納するArrayListの要素
class Dataclass{
	private double X, Y;        //敵のx座標, y座標
	private String Kind_Enemy;  //敵の種類
	private double Enemy_t;     //敵排出時間
    //コンストラクタ
	public Dataclass(double x, double y, String kind_enemy,double enemy_t) {
		this.X = x;
		this.Y = y;
		this.Kind_Enemy = kind_enemy;
		this.Enemy_t = enemy_t;
	}
	
	//get関数群
	public double getX() {
		return X;
	}
	public double getY() {
		return Y;
	}
	public String getKind_Enemy() {
		return Kind_Enemy;
	}
	public double getEnemy_t() {
		return Enemy_t;
	}
}



interface Projectile {//publicにしたければ、個別ファイルに移動
    // 投擲物の初期設定メソッド
    void setProjectile(double x, double y, double theta, double v);
    
    void setX(double x);
    void setY(double y);
    void setR(double r);
    void setaddT(double dt);
    void setInvincible(double dt);
    void setInvincible_enemy(double dt);
    void setsubInvincible(double dt);
    void setsubInvincible_enemy(double dt);
    //void setThrowby(double who);
    
    double getX();
    double getY();
    double getV();
    double getR();
    double getTheta();
    double getT();
    double getreloadtime();
    double getDamage();
    double getInvincible();
    double getInvincible_enemy();
    public int getShooterId();
    public int getID();
    // 投擲物の更新（任意で追加可能）

    // 投擲物の描画に必要な他のメソッド（例：回転角度の取得）
    double getRotationAngle();

    // 投擲物の回転角度を更新（任意で追加可能）
    void updateRotation();
}

class Tomato implements Projectile {//publicにしたければ、個別ファイルに移動
	protected double x, y, r, reloadTime;
    protected double t, theta, v, damage;
    protected double rotationAngle;
    protected double invincible, invincible_enemy; //無敵timeと、敵から発射の場合の無敵time
    private int shooterId; // 発射元のID
    protected int id;

    public Tomato(double x, double y, double theta, int shooterId) {
    	setProjectile(x, y, theta, 10);
        //this.v = 19;
        this.reloadTime = 0.1;
        this.damage = 5;
        this.rotationAngle = 0;
        this.invincible = 120.0;
        this.shooterId = shooterId;
        this.r = 30;
        this.id = 0;//種類を特定するために使う。
    }
    
    //set関数
    @Override
    public void setProjectile(double x, double y, double theta, double v) {
        this.x = x;
        this.y = y;
        this.theta = theta;
        this.v = v;
        this.t = 0.0;
    }

    @Override
    public void setX(double x) {
    	this.x = x;
    }
    
    @Override
    public void setY(double y) {
    	this.y = y;
    }
    
    @Override
    public void setR(double r) {
    	this.r=r;
    }
    
    @Override
	public void setaddT(double dt) {
    	this.t += dt;
    }
    
    @Override
    public void setInvincible(double dt) {
    		this.invincible = dt;
    }
    
    @Override
    public void setInvincible_enemy(double dt) {
    		this.invincible_enemy = dt;
    }
    
    @Override
    public void setsubInvincible(double dt) {
    	if(this.invincible <= 0.0) {}
    	else{
    		this.invincible -= dt;
    	}
    }
    
    @Override
    public void setsubInvincible_enemy(double dt) {
    	if(this.invincible_enemy == 0.0) {}
    	else{
    		this.invincible_enemy -= dt;
    	}
    }
    public void setID(int id) {
    	this.id = id;
    }
    
    //get関数
    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public double getR() {
    	return r;
    }
    
    @Override
    public double getV() {
        return v;
    }
    
    @Override
    public double getTheta() {
    	return theta;
    }
    
    @Override
    public double getT() {
    	return t;
    }
    
    @Override
    public double getDamage() {
    	return damage;
    }
    
    @Override
    public double getInvincible() {
    	return invincible;
    }
    
    @Override
    public double getInvincible_enemy() {
    	return invincible_enemy;
    }
    
    @Override
    public int getShooterId() {
        return shooterId;
    }

    @Override
    public double getRotationAngle() {
        return rotationAngle;
    }
    public int getID() {
    	return id;
    }

    @Override
    public double getreloadtime() {
    	return reloadTime;
    }
    
    @Override
    public void updateRotation() {
        this.rotationAngle -= 1; // 回転速度の例
        if (rotationAngle >= 360) {
            rotationAngle -= 360;
        }
    }
    
    
}


class Orange extends Tomato implements Projectile {//publicにしたければ、個別ファイルに移動

    public Orange(double x, double y, double theta, int shooterId) {
    	super(x, y, theta, shooterId);
        this.v = 7;
        this.reloadTime = 1.0;
        this.damage = 5;
        this.r = 15;
        setID(1);
    }
 

    @Override
    public void updateRotation() {
        this.rotationAngle -= 2; // 回転速度の例
        if (rotationAngle >= 360) {
            rotationAngle -= 360;
        }
    }

}

class Apple extends Tomato implements Projectile {//publicにしたければ、個別ファイルに移動

    public Apple(double x, double y, double theta, int shooterId) {
    	super(x, y, theta, shooterId);
        this.v = 4;
        this.reloadTime = 2.0;
        this.damage = 5;
        this.r = 40;
        setID(2);
    }
 

    @Override
    public void updateRotation() {
        this.rotationAngle -= 2; // 回転速度の例
        if (rotationAngle >= 360) {
            rotationAngle -= 360;
        }
    }

}

class Peach extends Tomato implements Projectile {//publicにしたければ、個別ファイルに移動

    public Peach(double x, double y, double theta, int shooterId) {
    	super(x, y, theta, shooterId);
        this.v = 6.5;
        this.reloadTime = 2.0;
        this.damage = 8;
        this.r = 40;
        setID(3);
    }
 

    @Override
    public void updateRotation() {
        this.rotationAngle -= 2; // 回転速度の例
        if (rotationAngle >= 360) {
            rotationAngle -= 360;
        }
    }

}

class Pear extends Tomato implements Projectile {//publicにしたければ、個別ファイルに移動

    public Pear(double x, double y, double theta, int shooterId) {
    	super(x, y, theta, shooterId);
        this.v = 8;
        this.reloadTime = 2.5;
        this.damage = 15;
        this.r = 40;
        setID(4);
    }
 

    @Override
    public void updateRotation() {
        this.rotationAngle -= 2; // 回転速度の例
        if (rotationAngle >= 360) {
            rotationAngle -= 360;
        }
    }

}

//投擲物生成クラス
class ProjectileFactory {
  //投擲物生成関数
	public static Projectile createProjectile(String type, double x, double y, double theta, int shooterId) {
      switch (type) { //受け取った文字列を比較し, 投擲物生成
          case "Tomato":
              return new Tomato(x, y, theta, shooterId);
          case "Orange":
              return new Orange(x, y, theta, shooterId);
          case "Apple":
              return new Apple(x, y, theta, shooterId);
          case "Peach":
              return new Peach(x, y, theta, shooterId);
          case "Pear":
              return new Pear(x, y, theta, shooterId);
          // その他の型...
          default:
              return null;
      }
  }
}

//敵生成クラス
class EnemiesFactory {
  //敵生成関数
	public static Enemies createEnemies(String type, double x, double y) {
      switch (type) { //受け取った文字列を比較し, 敵生成
          case "Pig":
              return new Pig(x, y);
          case "Castle": 
          	return new Castle(x,y);
          case "Caterpillar": 
          	return new Caterpillar(x,y);
          case "Butterfly": 
          	return new Butterfly(x,y);
          // その他の型...
          default:
          	return new Caterpillar(x,y);
      }
  }
}

interface GameModelObserver {
    void onEnemyRemoved(Enemies enemy);
}


//playerクラス（要移植）
class Player {
  //プレイヤーの座標
  private double x, y, r;
  protected double hp;
  protected double defaulthp;
  //プレイヤーのポイントは増えていく（敵は増えない）
  protected int point;
  //自爆防止用
  private static int nextId = 0;//ID用の静的カウンター
  private int id; //プレイヤID

  public Player (double x, double y, double hp){
      this.x=x;
      this.y=y;
      this.hp=hp;
      this.defaulthp = hp;
      this.id = nextId++;
      this.point = 0;
      this.r = 100;
  }
  
  //set
  public void setPlayerX(double x) {
	  this.x=x;
  }
  public void setPlayerAddX(double x) {
	  this.x += x;
  }
  public void setPlayerY(double y) {
	  this.y=y;
  }
  public void setPlayerAddY(double y) {
	  this.y += y;
  }
  public void setPlayerHP(double hp) {
	  this.hp=hp;
  }
  public void setPlayerDefaultHP(double defaulthp) {
	  this.defaulthp=defaulthp;
  }
  public void setId(int id) {
	  this.id = id;
  }
  public void setPlayerPoint(int p) {
	  this.point = p;
  }
  public void setaddPlayerPoint(int p) {
	  this.point += p;
  }
  public void setR(double r) {
		this.r = r;
  }
  
  //get
  public double getPlayerX() {
	  return this.x;
  }
  public double getPlayerY() {
	  return this.y;
  }
  public double getPlayerHP() {
	  return this.hp;
  }
  public double getPlayerDefaultHP() {
	  return this.defaulthp;
  }
  public int getId() {
      return id;
  }
  public int getPlayerPoint() {
	  return this.point;
  }
  public double getR() {
		return r;
}
  
}

class Enemies extends Player {
	protected ProgressBar HPBar;
	protected double damage, speed;
	//public int SpawnTime ; //出現時間間隔
  
	public Enemies(double x, double y, double hp) {
			super(x, y, hp);
			// TODO 自動生成されたコンストラクター・スタブ
			
			// ProgressBarの初期化
	        this.HPBar = new ProgressBar(1.0);
	}
  
	//プレイヤーとの当たり判定を確認してプレイヤーのHPを削るメソッド
  
	//set
	public void setHP(double hp) {
		setPlayerHP(hp);
	}
	public void setDamage(double damage) {
		this.damage = damage;
	}
	public void setSpeed(double speed) {
		this.speed = speed;
	}
	public void setHPBar() {
		this.HPBar.setProgress(hp / defaulthp);
	}
	
	
	//get
	public double getHP() {
		return getPlayerHP();
	}
	public double getDamage() {
		return this.damage;
	}
	public double getSpeed() {
		return this.speed;
	}
	public ProgressBar getHPBar() {
        return HPBar;
    }
}

class Pig extends Enemies{
	public Pig(double x, double y){
		super(x, y, 10.0);
		//setHP(100.0);
		setDamage(20.0);
		setSpeed(1.0);
		//敵のポイントは変えない
		setPlayerPoint(1);
		setR(100);
	}
}

class Caterpillar extends Enemies{
	public Caterpillar(double x, double y){
		super(x, y, 10.0);
		//setHP(100.0);
		setDamage(20.0);
		setSpeed(1.5);
		//敵のポイントは変えない
		setPlayerPoint(3);
		setR(50);
	}
}

class Butterfly extends Enemies{
	public Butterfly(double x, double y){
		super(x, y, 10.0);
		//setHP(100.0);
		setDamage(20.0);
		setSpeed(2.0);
		//敵のポイントは変えない
		setPlayerPoint(5);
		setR(50);
	}
}

class Castle extends Enemies{
	//ボス城
	/*public Castle(double x, double y, double hp){
		super(x, y, hp);
		//setHP(100.0);
		setDamage(0.0);
		setSpeed(0.0);
	}*/
	//モブ城もある
	public Castle(double x, double y){
		super(x, y, 200);
		//setHP(100.0);
		setDamage(0.0);
		setSpeed(0.0);
		//敵のポイントは変えない
		setPlayerPoint(100);
		setR(300);
	}
}
