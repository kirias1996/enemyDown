package plugin.enemyDown.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.SplittableRandom;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import plugin.enemyDown.data.PlayerScore;

public class EnemyDownCommand implements CommandExecutor, Listener {

  private List<PlayerScore> playerScoreList = new ArrayList<>();

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (sender instanceof Player player) {

      if (playerScoreList.isEmpty()) {
        addPlayer(createPlayer(player));
      } else {
        PlayerScore tempPlayerScore = null;
        boolean isAddPlayer = false;
        for (PlayerScore playerScore : playerScoreList) {
          //リストにコマンドを実行したプレイヤーが入っていない場合
          if (!playerScore.getPlayerName().equals(player.getName())) {
            tempPlayerScore = createPlayer(player);
            isAddPlayer = true;
          }
        }
        if (isAddPlayer) {
          addPlayer(tempPlayerScore);
        }
      }

      World world = player.getWorld();

      initPlayerStatus(player);

      world.spawnEntity(getEnemySpawnLocation(player, world), getEnemy());
    }
    return false;
  }

  /**
   * 　プレイヤーのスコア情報を作成する
   *
   * @param player コマンドを実行したプレイヤー
   */
  private PlayerScore createPlayer(Player player) {
    PlayerScore playerScore = new PlayerScore();
    playerScore.setPlayerName(player.getName());
    return playerScore;
  }

  /**
   * 　プレイヤーのスコア情報をリストに追加する
   *
   * @param player コマンドを実行したプレイヤー
   */
  private void addPlayer(PlayerScore playerScore) {
    playerScoreList.add(playerScore);
  }

  @EventHandler
  public void enemyDeathEvent(EntityDeathEvent e) {
    Player player = e.getEntity().getKiller();

    if (playerScoreList.isEmpty() || Objects.isNull(player)) {
      return;
    }

    for (PlayerScore playerScore : playerScoreList) {
      if (playerScore.getPlayerName().equals(player.getName())) {
        playerScore.setScore(playerScore.getScore() + 10);
        player.sendMessage("敵を倒しました。現在のスコアは" + playerScore.getScore() + "点です。");
      }
    }
  }


  /**
   * ゲーム開始前にプレイヤーの初期状態を設定する 体力と空腹度を最大にして、初期装備をダイヤモンドに設定する
   *
   * @param player コマンドを実行したプレイヤー
   */
  private static void initPlayerStatus(Player player) {
    player.setHealth(20);
    player.setFoodLevel(20);

    PlayerInventory playerInventory = player.getInventory();
    playerInventory.setHelmet(new ItemStack(Material.DIAMOND_HELMET));
    playerInventory.setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
    playerInventory.setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
    playerInventory.setBoots(new ItemStack(Material.DIAMOND_BOOTS));
    playerInventory.setItemInMainHand(new ItemStack((Material.DIAMOND_SWORD)));
  }


  /**
   * 敵の出現エリアを取得します。 出現エリアはX軸とZ軸は自分の一からプラス,ランダムで-10~9の値が設定されます。 出現エリアはY軸はプレイヤーと同じ位置になります。
   *
   * @param player コマンドを実行したプレイヤー
   * @param world  　コマンドを実行したプレイヤーが所属するワールド
   * @return
   */
  private Location getEnemySpawnLocation(Player player, World world) {
    Location playerLocation = player.getLocation();
    int randomX = new SplittableRandom().nextInt(20) - 10;
    int randomZ = new SplittableRandom().nextInt(20) - 10;

    double x = playerLocation.getX() + randomX;
    double y = playerLocation.getY();
    double z = playerLocation.getZ() + randomZ;

    return new Location(world, x, y, z);
  }

  /**
   * ランダムで敵を抽選してその結果の敵を取得します。
   *
   * @return　敵
   */
  private EntityType getEnemy() {
    List<EntityType> enemyList = List.of(EntityType.ZOMBIE, EntityType.SPIDER, EntityType.SKELETON, EntityType.ZOMBIE_VILLAGER);
    int random = new SplittableRandom().nextInt(4);
    return enemyList.get(random);
  }
}
