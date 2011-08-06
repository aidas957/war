package bukkit.tommytony.war.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.tommytony.war.Warzone;
import com.tommytony.war.ZoneLobby;
import com.tommytony.war.mappers.WarMapper;
import com.tommytony.war.mappers.WarzoneMapper;

import bukkit.tommytony.war.War;
import bukkit.tommytony.war.WarCommandHandler;

public class SetZoneConfigCommand extends AbstractZoneMakerCommand {

	public SetZoneConfigCommand(WarCommandHandler handler, CommandSender sender, String[] args) throws NotZoneMakerException {
		super(handler, sender, args);
	}

	@Override
	public boolean handle() {
		Warzone zone = null;
		Player player = null;
		CommandSender commandSender = this.getSender();
		boolean isFirstParamWarzone = false;
		
		if (this.args.length == 0) {
			return false;
		} else {
			if(!this.args[0].contains(":")) {
				// warzone name maybe in first place
				Warzone zoneByName = Warzone.getZoneByName(this.args[0]);
				if (zoneByName != null) {
					zone = zoneByName;
					isFirstParamWarzone = true;
				}
			}
			
			if (this.getSender() instanceof Player) {
				player = (Player)commandSender;
				
				Warzone zoneByLoc = Warzone.getZoneByLocation(player);
				ZoneLobby lobbyByLoc = ZoneLobby.getLobbyByLocation(player);
				if(zoneByLoc == null && lobbyByLoc != null) {
					zoneByLoc = lobbyByLoc.getZone();
				}
				if(zoneByLoc != null) {
					zone = zoneByLoc;
				}				
			}
			
			if (zone == null) {
				// No warzone found, whatever the mean, escape
				return false;
			}
			
			if (isFirstParamWarzone) {
				if(this.args.length == 1) {
					// Only one param: the warzone name - default to usage
					return false;
				}
				// More than one param: the arguments need to be shifted
				String[] newargs = new String[this.args.length - 1];
				for (int i = 1; i < this.args.length; i++) {
					newargs[i-1] = args[i];
				}
				this.args = newargs;
			}
			
			// We have a warzone and indexed-from-0 arguments, let's update
			if (War.war.updateZoneFromNamedParams(zone, player, this.args)) {
				this.msg("Saving config and resetting warzone " + zone.getName() + ".");
				WarzoneMapper.save(zone, false);
				zone.getVolume().resetBlocks();
				if (zone.getLobby() != null) {
					zone.getLobby().getVolume().resetBlocks();
				}
				zone.initializeZone(); // bring back team spawns etc
				this.msg("Warzone config saved. Zone reset.");

				if (War.war.getWarHub() != null) { // maybe the zone was disabled/enabled
					War.war.getWarHub().getVolume().resetBlocks();
					War.war.getWarHub().initialize();
				}
			} else {
				this.badMsg("Failed to read named parameters.");
			}
			
			return true;
		}
	}
}