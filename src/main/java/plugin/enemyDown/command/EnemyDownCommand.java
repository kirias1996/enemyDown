package plugin.enemyDown.command;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.SplittableRandom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.Title.Times;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import plugin.enemyDown.Main;
import plugin.enemyDown.data.PlayerScore;

/**
 * 制限時間内にランダムで出現する敵を倒し、スコアを獲得するゲームを起動するコマンドです。
 * 敵の種類に応じて加算されるスコアが変わり、倒せた敵の合計によってスコアが変動します。
 * 結果はプレイヤー名、点数、日時などで保存されます。
 */
public class EnemyDownCommand extends BaseCommand implements Listener {

  private List<PlayerScore> playerScoreList = new ArrayList<>();
  private List<Entity> spawnEnemyList = new ArrayList<>();

  private Main main;
  private final int INITIAL_GAME_TIME = 20;
  private final int INITIAL_SCORE = 0;

  public EnemyDownCommand(Main main) {
    this.main = main;
  }

  @Override
  public boolean onExecutePlayerCommand(Player player) {
    // コマンド実行プレイヤーを PlayerScoreとして格納する
    PlayerScore commandExecutorPlayer = getPlayerScore(player);
    initializePlayerScore(commandExecutorPlayer);

    initializePlayerStatus(player);

    gamePlay(player, commandExecutorPlayer, player.getWorld());
    return false;
  }


  @Override
  public boolean onExecuteNPCCommand(CommandSender sender) {
    return false;
  }

  /**
   * リストが空 or コマンドを実行したプレイヤーがリストに存在しない場合、プレイヤースコア情報をを新規作成。
   * コマンドを実行したプレイヤーが既にリストに存在する場合、コマンドを実行したプレイヤースコア情報を返す
   *
   * @param player コマンドを実行したプレイヤー
   * @return　コマンドを実行したプレイヤースコア情報
   */
  private PlayerScore getPlayerScore(Player player) {
    Optional<PlayerScore> foundPlayer = playerScoreList.stream()
        .filter(n -> n.equals(new PlayerScore(player.getName())))
        .findFirst();
    /*
       StreamAPIで取得した要素が空のとき → プレイヤースコア情報を新規作成
       プレイヤースコア情報が取得できた時→リストのプレイヤースコア情報を返す
     */
    return foundPlayer.orElseGet(() -> addPlayerList(player));

  }

  /**
   * 　プレイヤーのスコア情報を作成する
   *
   * @param player コマンドを実行したプレイヤー
   */
  private PlayerScore addPlayerList(Player player) {
    PlayerScore playerScore = new PlayerScore(player.getName());
    playerScoreList.add(playerScore);
    return playerScore;
  }

  /**
   * プレイヤースコア情報のスコアを0にクリア、ゲーム時間を設定する
   *
   * @param playerScore プレイヤースコア情報
   */
  private void initializePlayerScore(PlayerScore playerScore) {
    playerScore.setScore(INITIAL_SCORE);
    playerScore.setGameTime(INITIAL_GAME_TIME);
  }

  @EventHandler
  public void enemyDeathEvent(EntityDeathEvent e) {
    LivingEntity enemy = e.getEntity();
    Player player = enemy.getKiller();

    if (playerScoreList.isEmpty() || spawnEnemyList.stream().noneMatch(p -> p.equals(enemy))) {
      return;
    }

    playerScoreList.stream()
        .filter(p -> p.getPlayerName().equals(player.getName()))
        .findFirst()
        .ifPresent(playerScore -> {
          int enemyDestroyScore = getEnemyDestroyScore(enemy);
          playerScore.setScore(playerScore.getScore() + enemyDestroyScore);
          player.sendMessage("敵を倒しました。現在のスコアは" + playerScore.getScore() + "点です。");
        });
  }

  /**
   * 敵の種類に応じて取得するスコアを設定
   *
   * @param enemy 敵
   * @return 取得する点数
   */
  private int getEnemyDestroyScore(LivingEntity enemy) {
    int enemyDestroyScore;
    switch (enemy.getType()) {
      case ZOMBIE, ZOMBIE_VILLAGER -> enemyDestroyScore = 10;
      case SPIDER -> enemyDestroyScore = 20;
      case SKELETON -> enemyDestroyScore = 30;
      default -> enemyDestroyScore = 0;
    }
    return enemyDestroyScore;
  }

  /**
   * ゲーム開始前にプレイヤーの初期状態を設定する 体力と空腹度を最大にして、初期装備をダイヤモンドに設定する
   *
   * @param player コマンドを実行したプレイヤー
   */
  private void initializePlayerStatus(Player player) {
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
   * ゲームを開始し、制限時間内に敵を倒すとスコアが加算されます。合計スコアを加算します。
   *
   * @param player                プレイヤー
   * @param commandExecutorPlayer コマンド実行プレイヤー
   * @param world                 　MineCraftのワールド情報
   */
  private void gamePlay(Player player, PlayerScore commandExecutorPlayer, World world) {
    Bukkit.getScheduler().runTaskTimer(main, Runnable -> {
      if (commandExecutorPlayer.getGameTime() <= 0) {
        Runnable.cancel();
        Title title = Title.title(getGameEndTitle(), getGameEndSubTitle(commandExecutorPlayer),
            Times.times(Duration.ofMillis(0), Duration.ofMillis(3000), Duration.ofMillis(0)));
        world.showTitle(title);
        spawnEnemyList.forEach(Entity::remove);
        spawnEnemyList = new ArrayList<>();
        return;
      }
      spawnEnemyList.add(world.spawnEntity(getEnemySpawnLocation(player, world), getEnemy()));
      commandExecutorPlayer.setGameTime(commandExecutorPlayer.getGameTime() - 5);
    }, 0, 5 * 20);
  }

  /**
   * ゲーム終了時に表示するタイトルを取得する
   *
   * @return ゲーム終了メッセージ
   */
  private TextComponent getGameEndTitle() {
    return Component.text("ゲームが終了しました。");
  }

  /**
   * ゲーム終了時に取得した点数を表示するサブタイトル
   *
   * @param commandExecutorPlayer コマンド実行プレイヤー
   * @return 合計スコアを表示するメッセージ
   */
  private TextComponent getGameEndSubTitle(PlayerScore commandExecutorPlayer) {
    return Component.text(commandExecutorPlayer.getPlayerName() + "の合計点数は" + commandExecutorPlayer.getScore() + "点!");
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
//    int randomX = new SplittableRandom().nextInt(20) - 10;
//    int randomZ = new SplittableRandom().nextInt(20) - 10;
    int randomX = new SplittableRandom().nextInt(10) - 5;
    int randomZ = new SplittableRandom().nextInt(10) - 5;
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
    int random = new SplittableRandom().nextInt(enemyList.size());
    return enemyList.get(random);
  }
}
