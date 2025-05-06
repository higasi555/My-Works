package application;

import java.io.File;

import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Camera;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
 
public class GameWindowFX {
 
	//windowのインスタンス化（staticなことに注意）
	private static final GameWindowFX gw = new GameWindowFX();
	private Stage wnd;
	private Scene scene;
	private Scene scene2;
	
	private GameViewFX current;
	private boolean isChangeAnimation;
	private boolean isInit;
	
	public static PerspectiveCamera camera;
 
	private GameWindowFX() {}

	public static synchronized void init(Stage stage, double width, double height, GameViewFX initView) {
		if ( gw.isInit ) throw new RuntimeException("既に初期化されています。");
		//windowにsceneを追加
		//static参照のため、本当はただの変数sceneをgw.scene
		gw.scene = new Scene(initView, width, height);
		
				
		stage.setScene(gw.scene);
		gw.wnd = stage;
		gw.current = initView;
		gw.scene.setOnMouseClicked(e -> gw.current.mouseClicked(e));
		gw.scene.setOnMouseMoved(e -> gw.current.mouseMoved(e));
		gw.scene.setOnMouseDragged(e -> gw.current.mouseDragged(e));
		gw.isInit = true;
		
		//アイコン設定（初期化の段階でやってしまう）
		Image img = new Image(new File("img/icon_v1.png").toURI().toString());
		stage.getIcons().add(img);
		stage.setTitle("Tomato fes");
		stage.setWidth(1920);  // Stageの幅を1920に設定
        stage.setHeight(1080);
        stage.setResizable(false);
		
		//camera = new PerspectiveCamera();
		//gw.scene.setCamera(camera);
		
	}
 
	public static synchronized void show() {
		if ( !gw.wnd.isShowing() ) {
			gw.wnd.show();
		}
	}
 
	public static synchronized void close() {
		if ( gw.wnd.isShowing() ) {
			gw.wnd.close();
		}
	}
 
	public static synchronized boolean changeWithAnimation(GameViewFX view) {
		if ( gw.isChangeAnimation ) return false;
		Rectangle rect = new Rectangle(0,0,gw.scene.getWidth(),gw.scene.getHeight());
		rect.setFill(Color.BLACK);
		rect.setViewOrder(Long.MIN_VALUE);
		rect.setOpacity(0);
		gw.current.getChildren().add(rect);
 
		//フェードインフェードアウトアニメーションを実行
		FadeTransition fadeIn = new FadeTransition(Duration.millis(400), rect);
		fadeIn.setFromValue(0);
		fadeIn.setToValue(1);
		fadeIn.setOnFinished(e -> {
			gw.current.getChildren().remove(rect);
			view.getChildren().add(rect);
			//GameWindowFX._change(view);
			gw.scene.setRoot(view);
			gw.current = view;
		});
		FadeTransition fadeOut = new FadeTransition(Duration.millis(400), rect);
		fadeOut.setFromValue(1);
		fadeOut.setToValue(0);
		fadeOut.setOnFinished(e -> {
			view.getChildren().remove(rect);
			gw.isChangeAnimation = false;
		});
		SequentialTransition animation = new SequentialTransition(fadeIn, fadeOut);
		gw.isChangeAnimation = true;
		animation.play();
		return true;
	}
 
	public static synchronized boolean change(GameViewFX view) {
		if ( gw.isChangeAnimation ) return false;
		
		gw.scene.setRoot(view);
		gw.current = view;
		return true;
	}
	
	public static Game gameView;
	public static StackPane uiLayer;
	public static ProgressBar hpBar;
	public static Button btn;
	public static Text timeText;
	public static Group root;
	
	static Text pointsText;
	public static Text icon1Text;
	public static Text icon2Text;
	public static Text icon3Text;
	public static Text icon4Text;
	public static Text icon5Text;
	
	
	public static synchronized boolean change_to_game(int gamestage, double fps, String path_to_castle, String path_to_stage) {
		if ( gw.isChangeAnimation ) return false;
		
		root = new Group(); // ゲームの描画とUIを含むルートノード
		Group temp = new Group(); //subscene作成用の仮グループ
		
		gw.scene.setCursor(Cursor.NONE);
		
		SubScene subscene = new SubScene(temp, 1920, 1080);
		
		camera = new PerspectiveCamera();
		subscene.setCamera(camera);
		
        gameView = new Game(gamestage, fps, path_to_castle, path_to_stage); // ゲームのビュー
        
        subscene.setRoot(gameView);
        
        root.getChildren().add(subscene);

        // UIレイヤーの作成と配置
        uiLayer = new StackPane();
        uiLayer.setPrefSize(1920, 1080); // ウィンドウサイズに合わせる
        root.getChildren().add(uiLayer); // UIレイヤーをルートに追加

        //HPバーの配置
        hpBar = new ProgressBar(1.0);
        hpBar.setTranslateX(0);
        hpBar.setTranslateY(-500);
        hpBar.setPrefHeight(30);
        hpBar.setPrefWidth(500);
        uiLayer.getChildren().add(hpBar);
		
        String keifont = new File( "fonts/keifont.ttf" ).toURI().toString();
        
        //HP
        Text hpText = new Text("HP");
        hpText.setFont(Font.loadFont( keifont , 40));
        hpText.setFill( Color.rgb(100, 100, 100, 1) );//中塗り
        hpText.setStroke( Color.rgb(255, 255, 255, 1) );//枠塗り
        hpText.setStrokeWidth(2);
        hpText.setTranslateX(-300);
        hpText.setTranslateY(-500);
        uiLayer.getChildren().add(hpText);
        
        
        
        //終了画像
        Image img_esc = new Image(new File("img_ui/esc_v1.png").toURI().toString(), 100, 100, true, true);
        ImageView imgv_esc = new ImageView(img_esc);
        imgv_esc.setOnMouseClicked(event -> {
        	if(gameView.getGamePause() == false) {
        		gameView.setGamePause(true);
        		gw.scene.setCursor(Cursor.DEFAULT);
        	}else {
        		gameView.setGamePause(false);
        		gw.scene.setCursor(Cursor.NONE);
        	}
        });
        imgv_esc.setTranslateX(-880);
        imgv_esc.setTranslateY(-475);
        uiLayer.getChildren().add(imgv_esc);
        
        //投擲物選択
        Image img_icon1 = new Image(new File("img_ui/icon1.png").toURI().toString(), 100, 100, true, true);
        ImageView imgv_icon1 = new ImageView(img_icon1);
        imgv_icon1.setOnMouseClicked(event -> {
        	gameView.model.setCurrentPj("Orange");
        });
        imgv_icon1.setTranslateX(-880);
        imgv_icon1.setTranslateY(-180);
        uiLayer.getChildren().add(imgv_icon1);
        //残り何秒で投擲可能か
        icon1Text = new Text("Ready");
        icon1Text.setOnMouseClicked(event -> {
        	gameView.model.setCurrentPj("Orange");
        });
        icon1Text.setFont(Font.loadFont( keifont , 30));
        icon1Text.setFill( Color.rgb(100, 100, 100, 1) );//中塗り
        icon1Text.setStroke( Color.rgb(255, 255, 255, 1) );//枠塗り
        icon1Text.setStrokeWidth(1);
        icon1Text.setTranslateX(-880);
        icon1Text.setTranslateY(-180);
        uiLayer.getChildren().add(icon1Text);
        
        //投擲物選択
        Image img_icon2 = new Image(new File("img_ui/icon2.png").toURI().toString(), 100, 100, true, true);
        ImageView imgv_icon2 = new ImageView(img_icon2);
        imgv_icon2.setOnMouseClicked(event -> {
        	gameView.model.setCurrentPj("Tomato");
        });
        imgv_icon2.setTranslateX(-880);
        imgv_icon2.setTranslateY(-300);
        uiLayer.getChildren().add(imgv_icon2);
        //残り何秒で投擲可能か
        icon2Text = new Text("Ready");
        icon2Text.setOnMouseClicked(event -> {
        	gameView.model.setCurrentPj("Tomato");
        });
        icon2Text.setFont(Font.loadFont( keifont , 30));
        icon2Text.setFill( Color.rgb(100, 100, 100, 1) );//中塗り
        icon2Text.setStroke( Color.rgb(255, 255, 255, 1) );//枠塗り
        icon2Text.setStrokeWidth(1);
        icon2Text.setTranslateX(-880);
        icon2Text.setTranslateY(-300);
        uiLayer.getChildren().add(icon2Text);
        
        //投擲物選択
        Image img_icon3 = new Image(new File("img_ui/icon3.png").toURI().toString(), 100, 100, true, true);
        ImageView imgv_icon3 = new ImageView(img_icon3);
        imgv_icon3.setOnMouseClicked(event -> {
        	gameView.model.setCurrentPj("Pear");
        });
        imgv_icon3.setTranslateX(-880);
        imgv_icon3.setTranslateY(-60);
        uiLayer.getChildren().add(imgv_icon3);
        //残り何秒で投擲可能か
        icon3Text = new Text("Ready");
        icon3Text.setOnMouseClicked(event -> {
        	gameView.model.setCurrentPj("Pear");
        });
        icon3Text.setFont(Font.loadFont( keifont , 30));
        icon3Text.setFill( Color.rgb(100, 100, 100, 1) );//中塗り
        icon3Text.setStroke( Color.rgb(255, 255, 255, 1) );//枠塗り
        icon3Text.setStrokeWidth(1);
        icon3Text.setTranslateX(-880);
        icon3Text.setTranslateY(-60);
        uiLayer.getChildren().add(icon3Text);
        
        //投擲物選択
        Image img_icon4 = new Image(new File("img_ui/icon4.png").toURI().toString(), 100, 100, true, true);
        ImageView imgv_icon4 = new ImageView(img_icon4);
        imgv_icon4.setOnMouseClicked(event -> {
        	gameView.model.setCurrentPj("Peach");
        });
        imgv_icon4.setTranslateX(-880);
        imgv_icon4.setTranslateY(60);
        uiLayer.getChildren().add(imgv_icon4);
        //残り何秒で投擲可能か
        icon4Text = new Text("Ready");
        icon4Text.setOnMouseClicked(event -> {
        	gameView.model.setCurrentPj("Peach");
        });
        icon4Text.setFont(Font.loadFont( keifont , 30));
        icon4Text.setFill( Color.rgb(100, 100, 100, 1) );//中塗り
        icon4Text.setStroke( Color.rgb(255, 255, 255, 1) );//枠塗り
        icon4Text.setStrokeWidth(1);
        icon4Text.setTranslateX(-880);
        icon4Text.setTranslateY(60);
        uiLayer.getChildren().add(icon4Text);
        
        //投擲物選択
        Image img_icon5 = new Image(new File("img_ui/icon5.png").toURI().toString(), 100, 100, true, true);
        ImageView imgv_icon5 = new ImageView(img_icon5);
        imgv_icon5.setOnMouseClicked(event -> {
        	gameView.model.setCurrentPj("Apple");
        });
        imgv_icon5.setTranslateX(-880);
        imgv_icon5.setTranslateY(180);
        uiLayer.getChildren().add(imgv_icon5);
        //残り何秒で投擲可能か
        icon5Text = new Text("Ready");
        icon5Text.setOnMouseClicked(event -> {
        	gameView.model.setCurrentPj("Apple");
        });
        icon5Text.setFont(Font.loadFont( keifont , 30));
        icon5Text.setFill( Color.rgb(100, 100, 100, 1) );//中塗り
        icon5Text.setStroke( Color.rgb(255, 255, 255, 1) );//枠塗り
        icon5Text.setStrokeWidth(1);
        icon5Text.setTranslateX(-880);
        icon5Text.setTranslateY(180);
        uiLayer.getChildren().add(icon5Text);
        
         
        //ポイント表示
        pointsText = new Text("Points: 0");
	    pointsText.setFont(Font.loadFont( keifont , 60));
	    pointsText.setFill( Color.rgb(31, 189, 0, 1) );//中塗り
	    pointsText.setStroke( Color.rgb(255, 255, 255, 1) );//枠塗り
	    pointsText.setStrokeWidth(2); // 枠線の幅を3ピクセルに設定
	    pointsText.setTranslateX(755);
	    pointsText.setTranslateY(-400);
	    uiLayer.getChildren().add(pointsText);
        
        
        //タイマー
        timeText = new Text();
        timeText.setFont(Font.loadFont( keifont , 100));
        timeText.setFill( Color.rgb(237, 130, 130, 1) );//中塗り
        timeText.setStroke( Color.rgb(255, 255, 255, 1) );//枠塗り
        timeText.setStrokeWidth(4); // 枠線の幅を3ピクセルに設定
        timeText.setTranslateX(760);
        timeText.setTranslateY(-475);
        uiLayer.getChildren().add(timeText);
        
		gw.scene.setRoot(root);
		gw.current = gameView;
		
		//root.setMouseTransparent(true);
		gameView.requestFocus();

		
	    return true;
	    
	}
	
	public static void setCursor(Cursor arg0){
		gw.scene.setCursor(arg0);
	}

	
	public static synchronized boolean change_from_game(GameViewFX view) {
		if ( gw.isChangeAnimation ) return false;	

		//btn.setOnAction(null);
		gameView.setGameOver(true);
		
		gameView.getGameModelFX().removeObserver(gameView);
		
		gameView.getChildren().clear();
		root.getChildren().clear();
		
		gw.scene.setRoot(view);
		gw.current = view;
		
		return true;
	}
	
	
	//ポイント表示
	public static void updatePlayerPoints(int points) {
	    pointsText.setText("Points: " + points);
	}
	
	//ポイント表示
	public static void update_isPjready(int iconNumber, double sec) {
	    if (getIconText(iconNumber) != null) {
	        if (sec != 0) {
	        	getIconText(iconNumber).setText(String.format("%.2f", sec));
	        } else {
	        	getIconText(iconNumber).setText("Ready");
	        }
	    }
	}

	//アイコン番号に応じたText
	public static Text getIconText(int iconNumber) {
	    switch (iconNumber) {
	        case 1:
	            return icon1Text;
	        case 2:
	            return icon2Text;
	        case 3:
	            return icon3Text;
	        case 4:
	            return icon4Text;
	        case 5:
	            return icon5Text;
	        default:
	            return null;
	    }
	}
	
	
}