package plugin.enemyDown.data;

import lombok.Getter;
import lombok.Setter;

/**
 * プレイヤーのスコア情報を保存するクラス プレイヤーの名前、スコア、日時などを持つ
 */
@Getter
@Setter
public class PlayerScore {

  private String playerName;
  private int score;

}
