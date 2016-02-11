package com.dumptruckman.lockandkey;

import com.dumptruckman.lockandkey.util.ActionBarUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pluginbase.bukkit.messaging.BukkitMessager;
import pluginbase.messages.ChatColor;
import pluginbase.messages.Message;
import pluginbase.minecraft.BasePlayer;

public enum Messages implements Message {
    IN_GAME_ONLY("commands.in_game_only", "To use from console, use \"-p <name>\" to specify a player"),
    INVALID_AMOUNT("commands.invalid_amount", "'%s' is not a valid amount!"),
    INVALID_PLAYER("commands.invalid_player", "'%s' is not a valid player!"),
    INVALID_LOCK_TYPE("commands.givelock.invalid_lock_type", "'%s' is not a valid lock type!"),
    LOCK_TYPES_AVAILABLE("commands.givelock.available_lock_types", "Lock types available: ", "%s"),
    GAVE_ITEM("commands.gave_item", "Gave " + ChatColor.GRAY + "%s " + ChatColor.ITALIC + ChatColor.GREEN + "%s %s"),
    PLACED_LOCK("locks.placed_lock", ChatColor.AQUA + "You placed a Locked %s!"),
    NEED_KEY("locks.need_key", ChatColor.RED + "You need a key to use that %s"),
    MUST_BE_OWNER("locks.must_be_owner", ChatColor.RED + "You must be the owner to configure the lock!"),
    ONE_KEY_AT_A_TIME("locks.one_at_a_time", ChatColor.RED + "You can only cut one key at a time!"),
    KEY_CUT_FOR_EXISTING_LOCK("locks.key_cut_for_existing_lock", ChatColor.GREEN + "Key has been cut to existing lock!"),
    KEY_CUT_FOR_NEW_LOCK("locks.key_cut_for_new_lock", ChatColor.GREEN + "Key has been cut to new lock!"),
    LOCK_ALREADY_HAS_KEY("locks.lock_already_has_key", ChatColor.RED + "This lock already uses a different key."),
    NEW_LOCK_INSTALLED_FOR_KEY("locks.new_lock_installed_for_key", ChatColor.GREEN + "New lock installed for this key!"),
    ;

    private static LockAndKeyPlugin plugin;
    private static BukkitMessager messager;

    static void setPlugin(LockAndKeyPlugin plugin) {
        Messages.plugin = plugin;
        Messages.messager = plugin != null ? (BukkitMessager) plugin.getPluginBase().getMessager() : null;
    }

    @NotNull
    private final Message message;

    Messages(@NotNull String key, @NotNull String defaultMessage, String... additionalLines) {
        this.message = Message.createMessage(key, defaultMessage, additionalLines);
    }

    @Override
    @NotNull
    public String getDefault() {
        return message.getDefault();
    }

    @Override
    @Nullable
    public Object[] getKey() {
        return message.getKey();
    }

    @Override
    public int getArgCount() {
        return message.getArgCount();
    }

    public void sendByChat(@NotNull BasePlayer player, Object... args) {
        if (messager != null) {
            messager.message(player, this, args);
        } else {
            player.sendMessage(String.format(getDefault(), args));
        }
    }

    public void sendByChat(@NotNull CommandSender sender, Object... args) {
        if (messager != null) {
            messager.message(sender, this, args);
        } else {
            sender.sendMessage(String.format(getDefault(), args));
        }
    }

    public void sendByActionBar(@NotNull Player player, Object... args) {
        if (messager != null) {
            ActionBarUtil.sendActionBarMessage(player, messager.getLocalizedMessage(this, args));
        } else {
            ActionBarUtil.sendActionBarMessage(player, String.format(getDefault(), args));
        }
    }

    public void sendByActionBarExtended(@NotNull Player player, int duration, Object... args) {
        if (messager != null) {
            ActionBarUtil.sendActionBarMessage(player, messager.getLocalizedMessage(this, args), duration, plugin);
        } else {
            ActionBarUtil.sendActionBarMessage(player, String.format(getDefault(), args));
        }
    }
}
