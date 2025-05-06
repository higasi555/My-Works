package application;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Camera;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;

 
public class TomatoFes extends Application{

	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		//ゲームウィンドウ初期化処理
		GameWindowFX.init(primaryStage, 1920, 1080, new Title());
		GameWindowFX.show();
	}
}
 

 
//タイトル画面
class Title extends GameViewFX{
	Button btn = new Button("設定へ");
	GameViewFX Game;
	Thread bgmThread;
	
	String selectedfps;
	String selectedstage_string;
	
	String path2castle;
	String path2stage;
	
	double selectedfps_v;
	int selectedstage;
	
	ComboBox<String> cmbbox_fps;
	ComboBox<String> cmbbox_stage;
	
	private volatile boolean running = true; //スレッド制御フラグ
    
	public Title() {
		// BGM再生用のスレッドを生成
        bgmThread = new Thread(new Runnable() {
            public void run() {
            	try {
					Thread.sleep(500);
					while (running == true) {
						Appmanager.startBGM_opening();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
                //Appmanager.startBGM_opening();
            }
        });
        //bgmThread.start(); // BGM再生スレッドの実行

        Image img_BG = new Image(new File("img_title/title_v4.gif").toURI().toString(), 1920, 1080, false, true);
        ImageView imgv_BG = new ImageView(img_BG);
        this.getChildren().add(imgv_BG);

		
		//設定ボタンをset
		btn.setMaxWidth(100); // 横幅を指定
	    btn.setMaxHeight(50); // 高さを指定
        //getChildren().add(btn);
        
        //fps設定
        cmbbox_fps = new ComboBox<>();
        cmbbox_fps.setPromptText("fps設定");
        cmbbox_fps.setTranslateX(100);
        cmbbox_fps.setTranslateY(0);
        cmbbox_fps.setPrefHeight(30);
        cmbbox_fps.setPrefWidth(80);
        cmbbox_fps.getItems().addAll("30","60","120","240","360");
        selectedfps_v = 240;

        cmbbox_fps.setOnAction(event -> {
        	selectedfps = cmbbox_fps.getValue();
        	
        switch (selectedfps) {
        	case "30":
        		selectedfps_v = 30;
        		break;
            case "60":
            	selectedfps_v = 60;
            	break;
            case "120":
            	selectedfps_v = 120;
            	break;
            case "240":
            	selectedfps_v = 240;
            	break;
            case "360":
            	selectedfps_v = 360;
            	break;
            //default:
            	//selectedfps_v = 360;
        }

        });
        
        getChildren().add(cmbbox_fps);
        
        
        //Stage設定
        cmbbox_stage = new ComboBox<>();
        cmbbox_stage.setPromptText("ステージを選択してください！");
        cmbbox_stage.setTranslateX(960-200/2);
        cmbbox_stage.setTranslateY(500);
        cmbbox_stage.setPrefHeight(50);
        cmbbox_stage.setPrefWidth(200);
        cmbbox_stage.getItems().addAll("Stage1(初級)","Stage1(中級)","Stage1(上級)","Stage2(初級)","Stage2(中級)","Stage2(上級)");
        selectedstage = 1;
        path2castle = "stagedata/setCastle.txt";
		path2stage = "stagedata/setStage_1.txt";

        cmbbox_stage.setOnAction(event -> {
        	selectedstage_string = cmbbox_stage.getValue();
        	
        switch (selectedstage_string) {
        	case "Stage1(初級)":
        		selectedstage = 1;
        		path2castle = "stagedata/setCastle.txt";
        		path2stage = "stagedata/setStage_1.txt";
        		break;
            case "Stage1(中級)":
            	selectedstage = 1;
            	path2castle = "stagedata/setCastle2.txt";
        		path2stage = "stagedata/setStage_3.txt";
            	break;
            case "Stage1(上級)":
            	selectedstage = 1;
            	path2castle = "stagedata/setCastle3.txt";
        		path2stage = "stagedata/setStage_2.txt";
            	break;
            case "Stage2(初級)":
        		selectedstage = 2;
        		path2castle = "stagedata/setCastle.txt";
        		path2stage = "stagedata/setStage_1.txt";
        		break;
            case "Stage2(中級)":
            	selectedstage = 2;
            	path2castle = "stagedata/setCastle2.txt";
        		path2stage = "stagedata/setStage_3.txt";
            	break;
            case "Stage2(上級)":
            	selectedstage = 2;
            	path2castle = "stagedata/setCastle3.txt";
        		path2stage = "stagedata/setStage_2.txt";
            	break;
            //default:
            	//selectedfps_v = 360;
        }

        });
        
        getChildren().add(cmbbox_stage);
		
	}
 
	@Override
	public void mouseClicked(MouseEvent e) {
		if ( e.getButton() == MouseButton.PRIMARY ) {
			//bgmThread.stop();
			running = false;
			
			//ゲーム画面へ遷移
			GameWindowFX.change_to_game(selectedstage, selectedfps_v, path2castle, path2stage);
			
		}
	}
	

	
	public GameViewFX getGame() {
		return Game;
	}
}


class Game extends GameViewFX implements GameModelObserver {
    GameModelFX model;
    private Canvas canvas;
    
    private Image img_Player;
    private Image img_Player_damaged;
    private Image img_Caterpillar;
    private Image img_Butterfly;
    private Image img_Pig;
    private Image img_Castle;
    
    private Image cursorImage; // 照準イラストの画像
    private Image img_Tomato;
    private Image img_Orange;
    private Image img_Apple;
    private Image img_Peach;
    private Image img_Pear;
        
    private double cursorX, cursorY; // 照準の現在の位置

    
    //キーイベント用
    private boolean movingLeft = false;
    private boolean movingRight = false;    
    
    //タイムライン用
    private Timeline timeline;
    
    //オーディオ用
    private Sound sound;
    
    //game内fps設定
    public double fps;
    
    public Game(int gamestage, double gamefps, String path_to_castle, String path_to_stage) {

		//fps（レンダリング頻度）設定
    	fps = gamefps;
    	
        model = new GameModelFX(path_to_castle, path_to_stage);
        model.addObserver(this);

        img_Tomato = new Image(new File("img_pj/tomato_v5.png").toURI().toString(), 150, 150, true, true);
        img_Orange = new Image(new File("img/orange_v1.png").toURI().toString(), 100, 100, true, true);
        img_Apple = new Image(new File("img_pj/apple_v5.png").toURI().toString(), 150, 150, true, true);
        img_Peach = new Image(new File("img_pj/peach_v5.png").toURI().toString(), 300, 300, true, true);
        img_Pear = new Image(new File("img_pj/pear_v5.png").toURI().toString(), 150, 150, true, true);
        
        img_Player = new Image(new File("img/player_v5.gif").toURI().toString(), 300, 300, true, true);
        img_Player_damaged = new Image(new File("img/player_v5_damaged.gif").toURI().toString(), 300, 300, true, true);
        img_Caterpillar = new Image(new File("enemy/enemy1_v2.gif").toURI().toString(), 300, 300, true, true);
        img_Butterfly = new Image(new File("enemy/enemy2_v2.gif").toURI().toString(), 300, 300, true, true);
        img_Pig = new Image(new File("enemy/pig_v2.gif").toURI().toString(), 500, 500, true, true);
        
        
        cursorImage = new Image(new File("img/cursor.png").toURI().toString(), 50, 0, true, false);
        
        switch(gamestage) {
        	case 1 :
        		//long startTime = System.nanoTime();
        		//背景の設定
        		img_Castle = ImageManager_Stage1.img_Castle;
        		ImageManager_Stage1.setImage(this);
        		//long endTime = System.nanoTime();
        		//long duration = (endTime - startTime) / 1_000_000;
        		//System.out.println("Stage1読み込み時間：" + " ms");
        		break;
        	case 2:
        		//long startTime2 = System.nanoTime();
        		//背景の設定
        		img_Castle = ImageManager_Stage2.img_Castle;
        		ImageManager_Stage2.setImage(this);
        		//long endTime2 = System.nanoTime();
        		//long duration2 = (endTime2 - startTime2) / 1_000_000;
        		//System.out.println("Stage2読み込み時間：" + " ms");
        		break;
        }
        
        
        //Scene(Game)の上にcanvas
        canvas = new Canvas(8000, 1080);// Canvasサイズを設定
        //canvas.setTranslateZ(10);
        getChildren().add(canvas);
               
        
        //1/fps秒おきに再レンダリング
        timeline = new Timeline(new KeyFrame(Duration.millis(1000/fps), e -> {
        	if(model.isGamePause == false) {                
                updateTimeText();         
                update();
                
                sound.resume();
        	}
        	else {
        		sound.pause();
        	}
        	if(model.isGameOver == true) {
        		sound.stop();
        		sound.close();
        		
        		GameWindowFX.setCursor(Cursor.DEFAULT);
        		timeline.stop(); // ゲームオーバー時にアニメーションタイマーを停止
                cleanup();   // リソースの解放
                GameWindowFX.change_from_game(new GameOver());
        	}
        	else if(model.isGameClear == true) {
        		sound.stop();
        		sound.close();
        		
        		GameWindowFX.setCursor(Cursor.DEFAULT);
        		timeline.stop(); // ゲームオーバー時にアニメーションタイマーを停止
                cleanup();   // リソースの解放
                GameWindowFX.change_from_game(new GameClear());
        	}
        }));
        timeline.setCycleCount(Timeline.INDEFINITE); // 無限に繰り返す
        timeline.play(); // Timelineを開始
        
        
        //カメラの設定
        GameWindowFX.camera.setNearClip(0);
        GameWindowFX.camera.setFarClip(10000.0);
        GameWindowFX.camera.setFieldOfView(90); // 視野角の設定
        GameWindowFX.camera.setTranslateX(-200);
        GameWindowFX.camera.setTranslateY(0);
               
	    
	    // キーボードイベントの追加
        setFocusTraversable(true); // キーボードイベントを受け取るために必要
        addEventHandler(KeyEvent.KEY_PRESSED, this::keyPressed);
        addEventHandler(KeyEvent.KEY_RELEASED, this::keyReleased);
        
        
        // BGM再生用のスレッドを生成
        try {
			sound = new Sound("SE/6035134_MotionElements_positive-acoustic-country_16bit.wav");
		} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		sound.play();

    }

  
    public void addBG(Image img, ImageView imgv, double x, double y, double z, double rx_angle) {
    	imgv.setTranslateX(x - img.getWidth() / 2); //配置が中心座標基準になるように変換
        imgv.setTranslateY(y - img.getHeight() / 2);
        imgv.setTranslateZ(z);
        
        Rotate rotate = new Rotate();
        rotate.setAngle(rx_angle); // 回転角度
        rotate.setAxis(Rotate.X_AXIS); // x軸周りに回転
        rotate.setPivotX(img.getWidth() / 2);
        rotate.setPivotY(img.getHeight() / 2);
        imgv.getTransforms().add(rotate);
        
        this.getChildren().add(imgv);
    }
    
    public void addBG_new(Image img, double x, double y, double z) {
    	ImageView imgv = new ImageView(img);
    	imgv.setTranslateX(x - img.getWidth() / 2); //配置が中心座標基準になるように変換
        imgv.setTranslateY(y - img.getHeight() / 2);
        imgv.setTranslateZ(z);

        this.getChildren().add(imgv);
    }
    
    //fps関連
    public void setFps(double x) {
    	this.fps = x;
    }
    
    public double getFps() {
    	return this.fps;
    }
    
    public void setGameOver(boolean x) {
    	model.isGameOver = x;
    }
    
    public void setGamePause(boolean x) {
    	model.isGamePause = x;
    }
    
    public boolean getGamePause() {
    	return model.isGamePause;
    }
    
    public Canvas getCanvas() {
    	return canvas;
    }
    
    public GameModelFX getGameModelFX() {
    	return model;
    }
    
    
    
    private double newX;
    
    public void update() {
    	if (model.isGameOver == false) {
    		GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());// 画面クリア

            //敵の描画
            for (Enemies p : model.getEnemies()) {
            	if (p != null) {
            		//敵のクラスにより、描画画像を変更
                	switch (p.getClass().getSimpleName()) {
                    case "Pig":
                    	drawEnemy(gc, p, img_Pig);
                    	break;
                    case "Caterpillar": 
                    	drawEnemy(gc, p, img_Caterpillar);
                    	break;
                    case "Butterfly": 
                    	drawEnemy(gc, p, img_Butterfly);
                    	break;
                    case "Castle": 
                    	drawEnemy(gc, p, img_Castle);
                    	break;
                    default:
                    	break;
                	}
            	}
            }
            
            //投擲物の描画
            for (Projectile p : model.getProjectiles()) {            	
            	switch (p.getClass().getSimpleName()) {
                case "Tomato":
                	drawRotatedProjectile(gc, p, img_Tomato, p.getX()-(img_Tomato.getWidth()/2), p.getY()-(img_Tomato.getHeight()/2));// 回転の更新
                	break;
                case "Orange":
                	drawRotatedProjectile(gc, p, img_Orange, p.getX()-(img_Orange.getWidth()/2), p.getY()-(img_Orange.getHeight()/2));// 回転の更新
                	break;
                case "Apple":
                	drawRotatedProjectile(gc, p, img_Apple, p.getX()-(img_Apple.getWidth()/2), p.getY()-(img_Apple.getHeight()/2));// 回転の更新
                	break;
                case "Peach":
                	drawRotatedProjectile(gc, p, img_Peach, p.getX()-(img_Peach.getWidth()/2), p.getY()-(img_Peach.getHeight()/2));// 回転の更新
                	break;
                case "Pear":
                	drawRotatedProjectile(gc, p, img_Pear, p.getX()-(img_Pear.getWidth()/2), p.getY()-(img_Pear.getHeight()/2));// 回転の更新
                	break;
                default:
                    break;
            	}  
            }
            
            //最後に、プレイヤーの描画
            if(model.isDamaged == true) {
            	if (model.damageBlinkCounter % (fps/4) < 10) {
                    // プレイヤーの描画
                    gc.drawImage(img_Player_damaged, model.getPlayerX()-(img_Player.getWidth()/2), model.getPlayerY()-(img_Player.getHeight()/2));
                }

                model.damageBlinkCounter++;
                if (model.damageBlinkCounter > fps*0.5) { //0.5秒間点滅した後、通常状態に戻す
                    model.isDamaged = false;
                }
            }else {
            	gc.drawImage(img_Player, model.getPlayerX()-(img_Player.getWidth()/2), model.getPlayerY()-(img_Player.getHeight()/2));
            }
            
            
            // 照準の描画
            gc.drawImage(cursorImage, cursorX, cursorY);

            
            // プレイヤーのX座標に基づいてカメラのX座標を更新
            double targetX = model.getPlayerX() - 500; // カメラが目指すX座標
            double currentX = GameWindowFX.camera.getTranslateX(); // 現在のカメラのX座標
            //double currentX = GameUI.camera.getTranslateX();
            newX = currentX + (targetX - currentX) * 0.025*360/fps; // 新しいカメラのX座標
            double dnewX = (targetX - currentX) * 0.025*360/fps;

            GameWindowFX.camera.setTranslateX(newX);

            //あとで値をdv二変更
            if (movingLeft) {
                model.setPlayerX(model.getPlayerX() - 10*60/fps);
            }
            if (movingRight) {
                model.setPlayerX(model.getPlayerX() + 10*60/fps);
            }
            
            
            //HPbarの更新
            double progress = model.player.getPlayerHP()/model.player.getPlayerDefaultHP();
            //HPbar.setProgress(progress);
            GameWindowFX.hpBar.setProgress(progress);
            
            cursorX = cursorX + dnewX;
            
            //ポイント表示をアップデート
            GameWindowFX.updatePlayerPoints(model.player.getPlayerPoint());
            
            GameWindowFX.update_isPjready(1, model.projectilesreload[1]);
            GameWindowFX.update_isPjready(2, model.projectilesreload[0]);
            GameWindowFX.update_isPjready(3, model.projectilesreload[4]);
            GameWindowFX.update_isPjready(4, model.projectilesreload[3]);
            GameWindowFX.update_isPjready(5, model.projectilesreload[2]);

            
    	}      
    }

    //以下タイマー表示に関する関数
    private void updateTimeText() {
        int totalSeconds = (int)model.getTimeLimit();
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        String time = String.format("%02d:%02d", minutes, seconds);
        //System.out.println(time);
        GameWindowFX.timeText.setText(time);
    }
    
    public void drawEnemy(GraphicsContext gc, Enemies e, Image image) {
    	double x = e.getPlayerX()-(image.getWidth()/2);
    	double y = e.getPlayerY()-(image.getHeight()/2);
    	//double y = e.getPlayerY() - image.getHeight();
		gc.drawImage(image, x, y);
		drawEnemyHpBar(gc, e);
    }
    
    public void drawEnemyHpBar(GraphicsContext gc, Enemies enemy) {
        double x = enemy.getPlayerX();
        double y = enemy.getPlayerY();
        double hpPercentage = enemy.getHP() / enemy.getPlayerDefaultHP();
        //double hpDecrease = enemy.getHP() - enemy.getPlayerDefaultHP();
        
        double barWidth = 100; // HPバーの幅
        double barHeight = 10; // HPバーの高さ
        double barX = x - barWidth / 2; // 中心に配置
        double barY = y - 30; // 敵の頭上に配置

        gc.setFill(Color.GRAY);
        gc.fillRect(barX - 2, barY - 2, barWidth + 4, barHeight + 4);
        
        //灰色バー（背景）
        gc.setFill(Color.WHITE);
        gc.fillRect(barX, barY, barWidth, barHeight);

        //赤色バー
        gc.setFill(Color.RED);
        gc.fillRect(barX, barY, barWidth * hpPercentage, barHeight);
        
        
    }
    
    public void drawRotatedProjectile(GraphicsContext gc, Projectile projectile, Image image, double x, double y) {
        gc.save();// 現在の描画状態を保存
        gc.translate(x + image.getWidth() / 2, y + image.getHeight() / 2);// 描画原点をプロジェクタイルの中心に移動
        projectile.updateRotation();
        gc.rotate(projectile.getRotationAngle());// 回転
        gc.drawImage(image, -image.getWidth() / 2, -image.getHeight() / 2);// プロジェクタイル描画
        gc.restore();// 描画状態を元に戻す
    }
    
    //以下本来はController
    
    @Override
    public void mouseClicked(MouseEvent e) {
		if ( e.getButton() == MouseButton.PRIMARY ) {
			double x = e.getX() + newX;
			double y = e.getY();
			
			double theta;
			
			int shooterId = model.player.getId();
	        //double v = 19;
	        //theta = Math.atan(((double)(model.getPlayerY() - y)/(double)(x - model.getPlayerX())));
			theta = Math.atan2(model.getPlayerY() - y, x - model.getPlayerX());
		        
	        model.addProjectile(model.getPlayerX(), model.getPlayerY(), theta, shooterId);
	        
	        //cursorX = x - cursorImage.getWidth() / 2;
	        //cursorY = y - cursorImage.getHeight() / 2;
	        
	        //uiへのイベントの伝播を止める
	        e.consume();
		}
	}
    
    @Override
    public void mouseMoved(MouseEvent e) {
    	cursorX = e.getX() - cursorImage.getWidth() / 2 + newX;
        cursorY = e.getY() - cursorImage.getHeight() / 2;
    }
    
    @Override
    public void mouseDragged(MouseEvent e) {
    	if ( e.getButton() == MouseButton.PRIMARY ) {
    		cursorX = e.getX() - cursorImage.getWidth() / 2 + newX;
            cursorY = e.getY() - cursorImage.getHeight() / 2;
    	}
    }
    
    public void keyPressed(KeyEvent e) {
        switch (e.getCode()) {
            case A:
                if (model.getPlayerX() < 500) {
                    movingLeft = false;
                } else {
                    movingLeft = true;
                }
                break;
            case D:
                movingRight = true;
                break;
            case DIGIT1:
                model.setCurrentPj("Tomato");
                break;
            case DIGIT2:
                model.setCurrentPj("Orange");
                break;
            case DIGIT3:
                model.setCurrentPj("Pear");
                break;
            case DIGIT4:
                model.setCurrentPj("Peach");
                break;
            case DIGIT5:
                model.setCurrentPj("Apple");
                break;
            default:
                break;
        }
    }

    public void keyReleased(KeyEvent e) {
        if (e.getCode() == KeyCode.A) {
            movingLeft = false;
        } else if (e.getCode() == KeyCode.D) {
            movingRight = false;
        }
    }
    
    
    //イベントハンドラの解除
    public void cleanup() {
        canvas.removeEventHandler(MouseEvent.MOUSE_MOVED, this::mouseMoved);
        canvas.removeEventHandler(MouseEvent.MOUSE_DRAGGED, this::mouseDragged);
        this.removeEventHandler(KeyEvent.KEY_PRESSED, this::keyPressed);
        this.removeEventHandler(KeyEvent.KEY_RELEASED, this::keyReleased); 
    }

	@Override
	public void onEnemyRemoved(Enemies enemy) {
		Platform.runLater(() -> getChildren().remove(enemy.getHPBar()));
		
	}
	
	
    
}



//ゲームオーバー画面
class GameOver extends GameViewFX{
	public GameOver() {
		//Appmanager.startBGM_over();
		
		Image img_BG = new Image(new File("img_title/over0.jpg").toURI().toString(), 1920, 1080, false, true);
        ImageView imgv_BG = new ImageView(img_BG);
        this.getChildren().add(imgv_BG);

	}
 
	public void mouseClicked(MouseEvent e) {
		if ( e.getButton() == MouseButton.PRIMARY ) {
			//タイトル画面に遷移
			GameWindowFX.changeWithAnimation(new Title());
		}
	}
}
 
//ゲームクリア画面
class GameClear extends GameViewFX{
	public GameClear() {
		Image img_BG = new Image(new File("img_title/clear0.jpg").toURI().toString(), 1920, 1080, false, true);
        ImageView imgv_BG = new ImageView(img_BG);
        this.getChildren().add(imgv_BG);
		
	}
 
	public void mouseClicked(MouseEvent e) {
		if ( e.getButton() == MouseButton.PRIMARY ) {
			//タイトル画面に遷移
			GameWindowFX.changeWithAnimation(new Title());
		}
	}
}



