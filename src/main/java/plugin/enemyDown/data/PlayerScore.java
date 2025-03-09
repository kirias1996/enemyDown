package plugin.enemyDown.data;

import java.util.Objects;
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
  private int gameTime;

  public PlayerScore(String playerName) {
    this.playerName = playerName;
  }

  public PlayerScore() {
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PlayerScore playerScore = (PlayerScore) o;
    return playerName.equals(playerScore.playerName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(playerName);
  }
}
