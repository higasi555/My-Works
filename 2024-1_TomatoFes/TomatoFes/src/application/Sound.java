package application;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
 
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.UnsupportedAudioFileException;
 
public class Sound {
	public static void playSE(String directory) throws InterruptedException {
		File path = new File(directory);
 
		try {
			//指定されたURLのオーディオ入力ストリームを取得
			AudioInputStream ais = AudioSystem.getAudioInputStream(path);
 
			//ファイルの形式取得
			AudioFormat af = ais.getFormat();
 
			//単一のオーディオ形式を含む指定した情報からデータラインの情報オブジェクトを構築
			DataLine.Info dataLine = new DataLine.Info(SourceDataLine.class,af);
 
			//指定された Line.Info オブジェクトの記述に一致するラインを取得
			SourceDataLine s = (SourceDataLine)AudioSystem.getLine(dataLine);
 
			//再生準備完了
			s.open();
			
			//ラインの処理を開始
			s.start();
			
			//読み込みサイズ
			byte[] data = new byte[s.getBufferSize()];
			
			//ループ回数
			int loopCounta = 0;
			
			//初期再生位置の指定
			//初期再生位置の指定は音声データの開始から3秒後
			//サンプルレート*1サンプル当たりのbit数*チャンネル数
			//ais.skip((int)af.getSampleRate() * af.getSampleSizeInBits() * af.getChannels() / 16 * 3);
			
			//読み込んだサイズ
			int size = -1;
			
			//再生処理のループ
			for(int i = 20;;i--) {
				//オーディオデータの読み込み
				size = ais.read(data);
				if ( size == -1 ) {
					ais.close();
					if ( loopCounta >= 0 ) {
						//既に一回ループされていたら終了
						break;
					} else {
						//読み込み位置をファイルの始点に戻してループさせます。
						ais = AudioSystem.getAudioInputStream(path);
						loopCounta++;
						continue;
					}
				}
				//ラインにオーディオデータの書き込み
				s.write(data, 0, size);
				
				/*if ( i >= 0 ) {
					//音量の変更
					FloatControl ctrl = (FloatControl)s.getControl(FloatControl.Type.MASTER_GAIN);
					ctrl.setValue((float)Math.log10((float)i / 20)*20);
				}*/
			}
			
			//残ったバッファをすべて再生するまで待つ
			s.drain();
 
			//ライン停止
			s.stop();
 
			//リソース解放
			s.close();
 
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}
	
	private Clip clip;
    private AudioInputStream ais;
    private boolean isPlaying;

    public Sound(String filePath) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        File audioFile = new File(filePath);
        ais = AudioSystem.getAudioInputStream(audioFile);
        clip = AudioSystem.getClip();
        clip.open(ais);
    }

    public void play() {
        if (!isPlaying) {
            clip.start();
            isPlaying = true;
        }
    }

    public void stop() {
        if (isPlaying) {
            clip.stop();
            clip.flush(); // 現在の再生位置をリセット
            isPlaying = false;
        }
    }

    public void pause() {
        if (isPlaying) {
            clip.stop();
            isPlaying = false;
        }
    }

    public void resume() {
        if (!isPlaying) {
            clip.start();
            isPlaying = true;
        }
    }

    public void loop(int count) {
        clip.loop(count);
    }

    public void close() {
        clip.close();
    }
    
    public Clip getClip() {
    	return clip;
    }
	
}