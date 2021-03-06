package com.igufguf.kingdomcraft.commands.admin;

import com.igufguf.kingdomcraft.KingdomCraft;
import com.igufguf.kingdomcraft.api.models.commands.CommandBase;
import com.igufguf.kingdomcraft.api.models.kingdom.Kingdom;
import com.igufguf.kingdomcraft.api.models.kingdom.KingdomUser;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyrighted 2018 iGufGuf
 *
 * This file is part of KingdomCraft.
 *
 * Kingdomcraft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * KingdomCraft is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with KingdomCraft.  If not, see <http://www.gnu.org/licenses/>.
 *
 **/
public class KickCommand extends CommandBase {

	private final KingdomCraft plugin;

	public KickCommand(KingdomCraft plugin) {
		super("kick", "kingdom.kick", true, "<player>");

		this.plugin = plugin;
	}
	
	@Override
	public List<String> tabcomplete(CommandSender sender, String[] args) {
		if ( sender.hasPermission(this.getPermission() + ".other") ) {
			return Bukkit.getOnlinePlayers().stream().filter(p -> p != sender).filter(p -> p.getName().startsWith(args[0])).map(HumanEntity::getName).collect(Collectors.toList());
		}

		KingdomUser user = plugin.getApi().getUserHandler().getUser((Player) sender);
		Kingdom ko = plugin.getApi().getUserHandler().getKingdom(user);
		if ( ko == null ) return null;

		return plugin.getApi().getKingdomHandler().getOnlineMembers(ko).stream().filter(p -> p != sender).filter(p -> p.getName().startsWith(args[0])).map(HumanEntity::getName).collect(Collectors.toList());
	}
	
	@Override
	public boolean execute(CommandSender sender, String[] args) {
		Player p = (Player) sender;
		
		if ( args.length != 1 ) {
			return false;
		}
		String username = args[0];

		KingdomUser user = plugin.getApi().getUserHandler().getUser(username);
		if ( user == null ) {
			user = plugin.getApi().getUserHandler().getOfflineUser(null, username);

			if (user == null) {
				plugin.getMsg().send(sender, "cmdDefaultNoPlayer", username);
				return true;
			}
		}

		if ( user.getKingdom() == null ) {
			plugin.getMsg().send(sender, "cmdDefaultTargetNoKingdom", user.getName());
			return true;
		}
		
		if ( p.hasPermission(this.getPermission() + ".other")
				|| (plugin.getApi().getUserHandler().getUser(p) != null && plugin.getApi().getUserHandler().getUser(p).getKingdom().equals(user.getKingdom())) ) {
			Kingdom kingdom = plugin.getApi().getUserHandler().getKingdom(user);

			plugin.getApi().getUserHandler().setKingdom(user, null);

			for ( Player member : plugin.getApi().getKingdomHandler().getOnlineMembers(kingdom) ) {
				plugin.getMsg().send(member, "cmdLeaveSuccessMembers", user.getName());
			}

			plugin.getMsg().send(sender, "cmdKickSender", user.getName(), kingdom.getName());

			if ( user.getPlayer() != null ) {
				plugin.getMsg().send(user.getPlayer(), "cmdKickTarget", kingdom.getName());
			}
			
			if ( plugin.getCfg().getBoolean("spawn-on-kingdom-leave") && user.getPlayer() != null ) {
				Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "spawn " + user.getName());
			}
		} else {
			plugin.getMsg().send(sender, "noPermissionCmd");
		}
		
		//save user when player is not online
		if ( user.getPlayer() == null ) {
			plugin.getApi().getUserHandler().save(user);
		}
		
		return true;
	}
}
