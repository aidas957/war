package bukkit.tommytony.war.command;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.tommytony.war.Team;
import com.tommytony.war.TeamKind;
import com.tommytony.war.TeamKinds;
import com.tommytony.war.Warzone;
import com.tommytony.war.mappers.WarzoneMapper;

import bukkit.tommytony.war.NoZoneMakerException;
import bukkit.tommytony.war.WarCommandHandler;

public class SetTeamFlagCommand extends AbstractZoneMakerCommand {
	public SetTeamFlagCommand(WarCommandHandler handler, CommandSender sender, String[] args) throws NoZoneMakerException {
		super(handler, sender, args);
	}

	@Override
	public boolean handle() {
		if (!(this.sender instanceof Player)) {
			return true;
		}

		Player player = (Player) this.sender;

		if (this.args.length != 1) {
			return false;
		}
		Warzone zone = Warzone.getZoneByLocation(player);

		if (zone == null) {
			return false;
		}

		TeamKind kind = TeamKinds.teamKindFromString(this.args[0]);
		Team team = zone.getTeamByKind(kind);
		if (team == null) {
			// no such team yet
			this.msg("Place the team spawn first.");
		} else if (team.getFlagVolume() == null) {
			// new team flag
			team.setTeamFlag(player.getLocation());
			Location playerLoc = player.getLocation();
			player.teleport(new Location(playerLoc.getWorld(), playerLoc.getBlockX() + 1, playerLoc.getBlockY(), playerLoc.getBlockZ()));
			this.msg("Team " + team.getName() + " flag added here.");
			WarzoneMapper.save(zone, false);
		} else {
			// relocate flag
			team.getFlagVolume().resetBlocks();
			team.setTeamFlag(player.getLocation());
			Location playerLoc = player.getLocation();
			player.teleport(new Location(playerLoc.getWorld(), playerLoc.getBlockX() + 1, playerLoc.getBlockY(), playerLoc.getBlockZ() + 1));
			this.msg("Team " + team.getName() + " flag moved.");
			WarzoneMapper.save(zone, false);
		}

		return true;
	}
}
