package plugin.enemyDown.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * コマンドを実行してプラグイン処理を動かす基底クラス
 */
public abstract class BaseCommand implements CommandExecutor {

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (sender instanceof Player player) {
      return onExecutePlayerCommand(player);
    } else {
      return onExecuteNPCCommand(sender);
    }
  }

  /**
   * プレイヤーがコマンドを実行したときに処理が行われる
   *
   * @param player コマンドを実行したプレイヤー
   * @return コマンドの実行有無
   */
  public abstract boolean onExecutePlayerCommand(Player player);

  /**
   * プレイヤー以外がコマンド実行したときに処理が行われる
   *
   * @param sender コマンド実行者
   * @return コマンドの実行有無
   */
  public abstract boolean onExecuteNPCCommand(CommandSender sender);

}
