package plugin.enemyDown;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import plugin.enemyDown.command.EnemyDownCommand;

public final class Main extends JavaPlugin {

  //  @Override
  public void onEnable() {
    EnemyDownCommand enemyDownCommand = new EnemyDownCommand();
    Bukkit.getPluginManager().registerEvents(enemyDownCommand, this);
    getCommand("enemyDown").setExecutor(enemyDownCommand);
  }
}
